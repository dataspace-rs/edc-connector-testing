package org.testing.edc.dataplane;

import org.eclipse.dataplane.domain.Result;
import org.eclipse.dataplane.domain.registration.Authorization;
import org.eclipse.dataplane.domain.registration.AuthorizationProfile;

/**
 * Signaling without credentials: every caller is the one registered control plane. The control plane
 * sends no {@code Authorization} header to a data plane registered without an authorization profile.
 */
class NoAuthorization implements Authorization {

    private final String controlPlaneId;

    NoAuthorization(String controlPlaneId) {
        this.controlPlaneId = controlPlaneId;
    }

    @Override
    public String type() {
        return "none";
    }

    @Override
    public Result<String> authorizationHeader(AuthorizationProfile profile) {
        return Result.failure(new IllegalStateException("no authorization"));
    }

    @Override
    public Result<String> extractCallerId(String authorizationHeader) {
        return Result.success(controlPlaneId);
    }
}
