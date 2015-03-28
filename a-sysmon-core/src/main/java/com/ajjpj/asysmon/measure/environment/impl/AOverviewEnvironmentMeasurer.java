package com.ajjpj.asysmon.measure.environment.impl;

import com.ajjpj.afoundation.proc.CliCommand;
import com.ajjpj.asysmon.measure.environment.AEnvironmentMeasurer;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;


/**
 * @author arno
 */
public class AOverviewEnvironmentMeasurer implements AEnvironmentMeasurer {
    public static final String KEY_OVERVIEW = "overview";

    @Override public void contributeMeasurements(EnvironmentCollector data) throws Exception {
        data.add(System.getProperty("user.name"), KEY_OVERVIEW, "user");
        data.add(System.getProperty("java.vm.name") + " " + System.getProperty("java.version"), KEY_OVERVIEW, "jvm");
        data.add(System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ") " + kernelVersion(), KEY_OVERVIEW, "os");

        registerHostName(data);
//        registerJvmMem(data);

        data.add(physicalMem(), KEY_OVERVIEW, "physical memory");

        //TODO num processors
        //TODO max. memory sizes per memory type on the JVM
    }

    private void registerJvmMem(EnvironmentCollector data) {
        for (MemoryPoolMXBean mxBean : ManagementFactory.getMemoryPoolMXBeans()) {
            final String key = "max. " +  mxBean.getName();
            data.add(String.valueOf(mxBean.getUsage().getMax() / 1024 / 1024) + "MB", KEY_OVERVIEW, key);
        }
    }

    private String physicalMem() {
        try {
            return new AMemInfoEnvironmentMeasurer().read().get(AMemInfoEnvironmentMeasurer.KEY_DETAIL_MEM_TOTAL);
        } catch (Exception e) {
            return "N/A";
        }
    }

    private String kernelVersion() {
        try {
            return new CliCommand(1, "uname", "-v").getOutput().get(0);
        } catch (Exception e) {
            return "";
        }
    }

    private void registerHostName(EnvironmentCollector data) {
        try {
            data.add(new CliCommand(1, "uname", "-n").getOutput().get(0), KEY_OVERVIEW, "hostname");
        } catch (Exception e) {
        }
    }

    @Override
    public void shutdown() throws Exception {
    }
}
