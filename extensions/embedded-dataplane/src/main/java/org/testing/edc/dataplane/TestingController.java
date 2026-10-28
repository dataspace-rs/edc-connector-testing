package org.testing.edc.dataplane;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.dataplane.domain.DataAddress;

import java.util.Map;

/** The provider's data proxy and the consumer's EDRs. */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class TestingController {

    private final Map<String, String> tokens;
    private final Map<String, DataAddress> edrs;

    TestingController(Map<String, String> tokens, Map<String, DataAddress> edrs) {
        this.tokens = tokens;
        this.edrs = edrs;
    }

    @GET
    @Path("proxy/{flowId}")
    public Response data(@PathParam("flowId") String flowId, @HeaderParam("Authorization") String authorization) {
        var token = tokens.get(flowId);
        if (token == null || authorization == null || !(authorization.equals(token) || authorization.equals("Bearer " + token))) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return Response.ok(Map.of("message", "data from EDC", "flowId", flowId)).build();
    }

    @GET
    @Path("edrs/{transferProcessId}")
    public Response edr(@PathParam("transferProcessId") String transferProcessId) {
        var address = edrs.get(transferProcessId);
        return address == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(address).build();
    }
}
