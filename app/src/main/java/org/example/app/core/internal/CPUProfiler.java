package core.internal;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class CPUProfiler {

    private static final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    // Get the current CPU usage of the system
    public static double getCurrentCpuLoad() {
        return osBean.getSystemCpuLoad() * 100;
    }

    // Get the system's CPU usage for the given process
    public static double getProcessCpuLoad() {
        return osBean.getProcessCpuLoad() * 100;
    }
}
