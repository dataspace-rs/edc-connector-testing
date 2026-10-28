package org.testing.edc.dataplane;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;

/**
 * The control plane sends no {@code Authorization} header to a data plane registered without an
 * authorization profile, but the SDK's signaling controller refuses requests without one before any
 * {@link org.eclipse.dataplane.domain.registration.Authorization} is consulted. This fills in a
 * placeholder, which {@link NoAuthorization} accepts.
 */
class AnonymousSignalingFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext request) {
        if (request.getHeaderString(HttpHeaders.AUTHORIZATION) == null) {
            request.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, "anonymous");
        }
    }
}
