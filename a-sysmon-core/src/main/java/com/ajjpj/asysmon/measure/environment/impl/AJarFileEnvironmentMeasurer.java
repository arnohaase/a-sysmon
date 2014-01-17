package com.ajjpj.asysmon.measure.environment.impl;

import com.ajjpj.asysmon.measure.environment.AEnvironmentMeasurer;
import com.ajjpj.asysmon.util.AOption;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author arno
 */
public class AJarFileEnvironmentMeasurer implements AEnvironmentMeasurer {
    public static final String KEY_PREFIX_JAR_VERSION = "jar-version";

    @Override public void contributeMeasurements(EnvironmentCollector data) throws Exception {
        final Enumeration<URL> enumeration = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
        while(enumeration.hasMoreElements()) {
            final URL manifestUrl = enumeration.nextElement();
            final AOption<String> jarName = extractJarName(manifestUrl);
            if(jarName.isEmpty()) {
                // MANIFEST.MF outside a JAR file
                continue;
            }

            final String version = extractVersion(manifestUrl);

            data.add(version, KEY_PREFIX_JAR_VERSION, jarName.get());
        }
    }

    private String extractVersion(URL manifestUrl) throws IOException {
        final Manifest manifest = new Manifest(manifestUrl.openStream());

        final Attributes mainAttribs = manifest.getMainAttributes();
        final String version = mainAttribs.getValue("Implementation-Version");
        if (version == null) {
            return "???";
        }
        return version;
    }

    private AOption<String> extractJarName(URL manifestUrl) {
        final String s = manifestUrl.toExternalForm();

        final int idxExclamation = s.lastIndexOf('!');
        if(idxExclamation == -1) {
            return AOption.none();
        }

        final String jarPath = s.substring(0, idxExclamation);
        final int idxSlash     = jarPath.lastIndexOf('/');
        final int idxBackslash = jarPath.lastIndexOf('\\');
        final int idxSep = Math.max(idxSlash, idxBackslash);

        return AOption.some(jarPath.substring(idxSep + 1));
    }

    @Override public void shutdown() throws Exception {
    }
}
