package com.ajjpj.asysmon.appinfo;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * @author arno
 */
public class ADefaultApplicationInfoProvider implements AApplicationInfoProvider {
    private final String applicationName;
    private final String version;
    private final String deployment;
    private final String nodeId;

    public ADefaultApplicationInfoProvider(String applicationName, String version) throws UnknownHostException {
        this(applicationName, version, calcDeployment(), "the-node");
    }

    public ADefaultApplicationInfoProvider(String applicationName, String version, String deployment, String nodeId) {
        this.applicationName = applicationName;
        this.version = version;
        this.deployment = deployment;
        this.nodeId = nodeId;
    }

    @Override public String getApplicationName() {
        return applicationName;
    }

    @Override public String getVersion() {
        return version;
    }

    @Override public String getDeployment() {
        return deployment;
    }

    @Override public String getNodeId() {
        return nodeId;
    }

    @Override
    public String getHtmlColorCode() {
        return calcHtmlColor(this);
    }

    /**
     * provides a 'deployment' string based on machine and user
     */
    public static String calcDeployment() throws UnknownHostException {
        return System.getProperty("user.name") + "@" + InetAddress.getLocalHost();
    }

    /**
     * calculates an HTML color code based on the hash codes of the application info values.
     */
    public static String calcHtmlColor(AApplicationInfoProvider appInfo) {
        int hash = appInfo.getApplicationName().hashCode();
        hash ^= appInfo.getDeployment().hashCode();
        hash ^= appInfo.getNodeId().hashCode();
        hash ^= appInfo.getVersion().hashCode();

        hash = hash & 0xffffff;
        hash = hash + 0x1000000;

        return "#" + Integer.toHexString(hash).substring(1);
    }
}
