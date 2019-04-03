package eubr.atmosphere.tma.analyze.utils;

import java.util.ArrayList;
import java.util.List;

public final class Constants {

    private Constants() {
        // restrict instantiation
    }

    public static final int cpuDescriptionId = 27;
    public static final int memoryDescriptionId = 28;

    public static final int responseTimeDescriptionId = 29; // mean response time (s) of the last second
    public static final int throughputDescriptionId = 30; // throughput (req/s) of the last second

    //public static final int podId = 8; // kafka
    public static final int podId = 9; // wildfly-0
    public static final int nodeId = 11; // worker node

    public static final List<Integer> monitorizedPods = new ArrayList<Integer>() {{
        add(9);
        add(13);
        add(14);
        }};

    // Metrics related to the node
    public static final Double maxCPU = 4165.0;
    public static final Double maxMemory = 7966.0;

    // ProbeId of Resource Consumption (probe-k8s-metrics-server)
    public static final int probeIdResourceConsumption = 7;

    // ProbeId of Performance (client)
    public static final int probeIdPerformance = 8;
}
