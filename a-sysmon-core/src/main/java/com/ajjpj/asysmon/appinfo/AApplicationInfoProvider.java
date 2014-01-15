package com.ajjpj.asysmon.appinfo;


/**
 * This interface makes details about the measured application available to A-SysMon. That is particularly relevant
 *  when several servers offload their data to a central server.<p />
 *
 * Configuration specifies an implementation of this interface rather than the values themselves to allow an application
 *  to retrieve some or all of the values at runtime, e.g. from a database.
 *
 * @author arno
 */
public interface AApplicationInfoProvider {
    /**
     * This is a high-level name of the application per se, regardless of version or deployment. This could be
     *  something like 'product server' or 'customer portal'.
     */
    String getApplicationName();

    /**
     * This is the logical name of the software version.
     */
    String getVersion();

    /**
     * This string identifies one 'instance' of a system. This should be a unique identifier for every JVM across
     *  the network (except for cluster nodes, which should share the same value). Values should stay the same
     *  across reboots, restarts and re-deployments. <p />
     *
     * This value is intended to allow aggregation (or comparison) of values from several JVMs in a central database.<p />
     *
     * This could be the name of a stage, e.g. 'prod' or 'qa-1', or for local developer deployments something like
     *  'local-arno'.
     */
    String getDeployment();

    /**
     * This string is relevant only in a clustered deployment, where it distinguishes between different nodes of a
     *  cluster.
     */
    String getNodeId();

    /**
     * This color code is used in presentation to allows users to distinguish between different deployments at a glance, e.g.
     *  using 'red' for production environments. <p />
     *
     * There is a generic implementation that calculates the HTML color code from the other values.
     */
    String getHtmlColorCode();
}
