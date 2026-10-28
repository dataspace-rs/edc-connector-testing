package org.testing.edc.dataplane;

import org.eclipse.dataplane.Dataplane;
import org.eclipse.dataplane.domain.DataAddress;
import org.eclipse.dataplane.domain.Result;
import org.eclipse.dataplane.domain.dataflow.DataFlow;
import org.eclipse.dataplane.domain.registration.ControlPlaneRegistrationMessage;
import org.eclipse.dataplane.port.DataPlaneSignalingApiController;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;
import org.eclipse.edc.web.spi.configuration.PortMapping;
import org.eclipse.edc.web.spi.configuration.PortMappingRegistry;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A data plane for tests, embedded in the control plane runtime and spoken to over Data Plane Signaling.
 * <ul>
 *     <li>As provider, it starts every {@code *-PULL} flow at once and hands out an address on its own
 *     proxy, which answers a fixed payload to callers presenting the flow's token.</li>
 *     <li>As consumer, it accepts every flow and keeps the address the provider handed out, served as
 *     an EDR at {@code GET /edrs/{transferProcessId}}: EDC 1.0 has no EDR API of its own.</li>
 * </ul>
 * Signaling is unauthenticated, so this is for tests only. Register the data plane with the control plane
 * through the management API ({@code PUT /v4/dataplanes}) with the endpoint {@code <testing url>/v1/dataflows}.
 */
public class EmbeddedDataPlaneExtension implements ServiceExtension {

    public static final String CONTEXT = "testing";
    /** The id the control plane is known by; signaling carries no credentials to tell it apart. */
    public static final String CONTROL_PLANE_ID = "controlplane";

    @Setting(key = "web.http.testing.port", description = "Port of the embedded data plane: signaling, proxy and EDRs", defaultValue = "9999")
    private int port;
    @Setting(key = "web.http.testing.path", description = "Path of the embedded data plane", defaultValue = "/testing")
    private String path;
    @Setting(key = "testing.dataplane.public.url", description = "Base URL other participants reach this context at, e.g. http://127.0.0.1:9999/testing")
    private String publicUrl;
    @Setting(key = "testing.dataplane.controlplane.signaling.url", description = "The control plane's signaling API, e.g. http://127.0.0.1:8182/api/signaling")
    private String controlPlaneSignalingUrl;

    @Inject
    private WebService webService;
    @Inject
    private PortMappingRegistry portMappingRegistry;
    @Inject
    private Monitor monitor;

    private final ConcurrentHashMap<String, String> tokens = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DataAddress> edrs = new ConcurrentHashMap<>();

    @Override
    public String name() {
        return "Embedded testing data plane";
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        portMappingRegistry.register(new PortMapping(CONTEXT, port, path));
        var base = publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl;

        var dataplane = Dataplane.newInstance()
                .id("embedded-dataplane")
                .endpoint(URI.create(base + "/v1/dataflows"))
                .registerAuthorization(new NoAuthorization(CONTROL_PLANE_ID))
                .profile("HttpData-PULL")
                .onPrepare(Result::success)
                .onStart(flow -> start(flow, base))
                .onStarted(this::started)
                .onSuspend(Result::success)
                .onResume(Result::success)
                .onCompleted(Result::success)
                .onTerminate(this::terminated)
                .build();
        dataplane.registerControlPlane(new ControlPlaneRegistrationMessage(CONTROL_PLANE_ID, URI.create(controlPlaneSignalingUrl)))
                .orElseThrow(e -> new IllegalStateException("registering the control plane: " + e.getMessage()));

        webService.registerResource(CONTEXT, new DataPlaneSignalingApiController(dataplane));
        webService.registerResource(CONTEXT, new TestingController(tokens, edrs));
        monitor.info("Embedded data plane signaling at %s/v1/dataflows".formatted(base));
    }

    /** Provider: the flow's data is served by the proxy, to holders of a fresh token. */
    private Result<DataFlow> start(DataFlow flow, String base) {
        var token = UUID.randomUUID().toString();
        tokens.put(flow.getId(), token);
        var address = new DataAddress("https://w3id.org/idsa/v4.1/HTTP", base + "/proxy/" + flow.getId(),
                List.of(new DataAddress.EndpointProperty("EndpointProperty", "authorization", token)));
        flow.setDataAddress(address);
        return Result.success(flow);
    }

    /** Consumer: the provider's address arrived; keep it as the EDR. */
    private Result<DataFlow> started(DataFlow flow) {
        if (flow.getDataAddress() != null) {
            edrs.put(flow.getId(), flow.getDataAddress());
        }
        return Result.success(flow);
    }

    private Result<DataFlow> terminated(DataFlow flow) {
        tokens.remove(flow.getId());
        edrs.remove(flow.getId());
        return Result.success(flow);
    }
}
