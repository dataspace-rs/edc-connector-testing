package org.testing.edc.identityhub;

import org.eclipse.edc.identityhub.spi.participantcontext.IdentityHubParticipantContextService;
import org.eclipse.edc.identityhub.spi.participantcontext.model.ParticipantManifest;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Creates an admin participant context at boot, so the Identity API can be used with a known API key
 * (IdentityHub has no super-user of its own since 1.0). The key is {@code base64(<id>).<secret>}, the
 * format IdentityHub's API key resolver expects. For tests only.
 */
public class SuperUserSeedExtension implements ServiceExtension {

    @Setting(key = "testing.ih.superuser.id", description = "Participant context id of the admin", defaultValue = "super-user")
    private String id;
    @Setting(key = "testing.ih.superuser.secret", description = "Secret part of the admin API key", defaultValue = "super-secret")
    private String secret;

    @Inject
    private IdentityHubParticipantContextService participantContextService;
    @Inject
    private Vault vault;
    @Inject
    private Monitor monitor;

    @Override
    public String name() {
        return "Testing super-user seed";
    }

    @Override
    public void start() {
        if (participantContextService.getParticipantContext(id).succeeded()) {
            return;
        }
        var alias = id + "-apikey";
        var apiKey = Base64.getEncoder().encodeToString(id.getBytes(StandardCharsets.UTF_8)) + "." + secret;
        vault.storeSecret(id, alias, apiKey);
        var manifest = ParticipantManifest.Builder.newInstance()
                .participantContextId(id)
                .did("did:web:" + id)
                .active(false)
                .provisionApiKey(false)
                .provisionStsAccount(false)
                .apiKeyAlias(alias)
                .scopes(List.of("identity-api:admin"))
                .build();
        participantContextService.createParticipantContext(manifest)
                .onSuccess(r -> monitor.info("Created admin participant context '%s'; API key: %s".formatted(id, apiKey)))
                .onFailure(f -> monitor.severe("Creating admin participant context failed: " + f.getFailureDetail()));
    }
}
