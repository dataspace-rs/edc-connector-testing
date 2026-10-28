# edc-connector-testing

EDC runtimes packaged as Docker images for testing other connectors against EDC.
CI publishes them to `ghcr.io/dataspace-rs/<image>`, tagged with the project
version, `latest` and the commit sha.

| Image | What it is |
|---|---|
| `controlplane` | EDC control plane with mock identity (`iam-mock`). |
| `virtual-controlplane` | EDC virtual (multi-participant) control plane with mock identity. |
| `controlplane-dcp` | EDC control plane with DCP identity and an embedded testing data plane, in memory. |
| `identityhub` | EDC IdentityHub (DID documents, Credential Service, STS, Identity API), in memory, with a seeded admin API key. |

`controlplane-dcp` and `identityhub` together make one DCP participant:

- **The IdentityHub** holds the participant's key, DID document and credentials.
- **The control plane** gets its self-issued tokens from the IdentityHub's STS.

Everything is in memory, so state is lost on restart. Both images are for tests
only: data plane signaling is unauthenticated.

## controlplane-dcp

Configured through environment variables (EDC maps `A_B_C` to `a.b.c`).

| Setting | Example | Notes |
|---|---|---|
| `EDC_PARTICIPANT_ID`, `EDC_PARTICIPANT_DID` | `did:web:127.0.0.1%3A10100:edc` | The participant's DID, served by its IdentityHub. |
| `WEB_HTTP_PORT` / `WEB_HTTP_PATH` | `18180` / `/api` | Default context. |
| `WEB_HTTP_MANAGEMENT_PORT` / `_PATH` | `18181` / `/management` | Management API. |
| `WEB_HTTP_MANAGEMENT_AUTH_TYPE` / `_KEY` | `tokenbased` / `password` | Sent as `x-api-key`. |
| `WEB_HTTP_PROTOCOL_PORT` / `_PATH` | `18282` / `/protocol` | DSP; 2025-1 is at `<path>/2025-1`. |
| `EDC_DSP_CALLBACK_ADDRESS` | `http://127.0.0.1:18282/protocol` | |
| `WEB_HTTP_CONTROL_PORT` / `_PATH`, `WEB_HTTP_SIGNALING_PORT` / `_PATH`, `WEB_HTTP_VERSION_PORT` / `_PATH` | | Other contexts; set the ports so nothing collides. |
| `WEB_HTTP_TESTING_PORT` / `_PATH` | `18999` / `/testing` | Embedded data plane: signaling at `/v1/dataflows`, data proxy, EDRs. |
| `TESTING_DATAPLANE_PUBLIC_URL` | `http://127.0.0.1:18999/testing` | How other participants reach the proxy. |
| `TESTING_DATAPLANE_CONTROLPLANE_SIGNALING_URL` | `http://127.0.0.1:18182/api/signaling` | The control plane's signaling API. |
| `EDC_IAM_DID_WEB_USE_HTTPS` | `false` | Resolve did:web over http. |
| `EDC_IAM_TRUSTEDISSUER_<ALIAS>_ID` | `did:web:issuer` | Trusted credential issuers (`_SUPPORTEDTYPES` optional). |
| `EDC_IAM_DCP_SCOPES_<ALIAS>_ID` / `_TYPE` / `_VALUE` | `membership` / `DEFAULT` / `org.eclipse.dspace.dcp.vc.type:MembershipCredential:read` | At least one DEFAULT scope is required. |
| `EDC_IAM_STS_OAUTH_TOKEN_URL` | `http://127.0.0.1:19292/api/sts/token` | The IdentityHub's STS. |
| `EDC_IAM_STS_OAUTH_CLIENT_ID` | the participant DID | |
| `EDC_IAM_STS_OAUTH_CLIENT_SECRET_ALIAS` | `edc-sts-client-secret` | Store the secret at runtime (step 1 below). |
| `TESTING_EDC_VAULTS_<N>_KEY` / `_VALUE` | | Optional secrets seeded into the vault at boot. |

After start:

1. **Store the STS client secret** that the IdentityHub returned when it created the participant:
   `POST /management/v4/secrets` with
   `{"@context": ["https://w3id.org/edc/connector/management/v2"], "@type": "Secret", "@id": "<alias>", "value": "<secret>"}`.
2. **Register the embedded data plane:** `PUT /management/v4/dataplanes` with
   `{"dataplaneId": "embedded", "endpoint": "<testing url>/v1/dataflows", "transferTypes": ["HttpData-PULL"], "labels": [], "authorization": null}`.

What the data plane does:

- **As provider,** it answers `HttpData-PULL` transfers with an address on its proxy,
  `<testing url>/proxy/<flow>`, with the token in the `authorization` endpoint property.
  The proxy serves a fixed JSON payload.
- **As consumer,** it keeps the address the provider sent, at `GET <testing url>/edrs/<transferProcessId>`.

## identityhub

| Setting | Example | Notes |
|---|---|---|
| `WEB_HTTP_PORT` / `_PATH` | `19180` / `/api` | Default context. |
| `WEB_HTTP_DID_PORT` / `_PATH` | `10100` / `/` | DID documents; the DID is derived from this host and port. |
| `WEB_HTTP_CREDENTIALS_PORT` / `_PATH` | `13131` / `/api/credentials` | Credential Service (DCP presentation API). |
| `WEB_HTTP_IDENTITY_PORT` / `_PATH` | `15151` / `/api/identity` | Identity API (`x-api-key`). |
| `WEB_HTTP_STS_PORT` / `_PATH` | `19292` / `/api/sts` | STS for the control plane. |
| `WEB_HTTP_ACCOUNTS_PORT`, `WEB_HTTP_VERSION_PORT` (+ `_PATH`) | | Other contexts. |
| `EDC_IAM_DID_WEB_USE_HTTPS` | `false` | |
| `TESTING_IH_SUPERUSER_SECRET` | `super-secret` | The admin API key is `base64("super-user").<secret>`, i.e. `c3VwZXItdXNlcg==.super-secret` by default. |

With the admin key:

- **Create the participant:** `POST /api/identity/v1/participants` with
  - its DID,
  - an EC key,
  - a `CredentialService` endpoint,
  - `"additionalProperties": {"clientSecret": "<alias>"}`.

  The response carries the STS client secret.
- **Add credentials:** `POST /api/identity/v1/participants/<id>/credentials`, format `VC1_0_JWT`.
