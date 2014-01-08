package com.ajjpj.asysmon.measure.environment;


import com.ajjpj.asysmon.util.AList;
import com.ajjpj.asysmon.util.UnixCommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This measurer collects information about mounted file systems from several sources.
 *
 * @author arno
 */
public class AFileSystemsEnvironmentMeasurer implements AEnvironmentMeasurer {
    public static final String KEY_FILESYSTEMS = "Mounted File Systems";

    @Override public void contributeMeasurements(Map<AList<String>, AEnvironmentData> data) throws Exception {
        contributeMtab(data);
        contributeDf(data);
    }

    private void contributeDf(Map<AList<String>, AEnvironmentData> data) throws Exception {
        for(String line: new UnixCommand("df", "-P").getOutput()) {
            System.out.println(line);
            if(! line.startsWith("/dev/")) {
                continue;
            }

            final String[] split = line.split("\\s+");
            System.out.println("  --> " + split.length + ": " + Arrays.asList(split));
            if(split.length != 6) {
                continue;
            }

            System.out.println("  !");
            final String device = split[0];
            final String size = split[1];
            final String used = split[2];
            final String available = split[3];
            final String usedPercent = split[4];

            add(data, device, "Size Total (1k Blocks)", size);
            add(data, device, "Size Used (1k Blocks)", used);
            add(data, device, "Size Available (1k Blocks)", available);
            add(data, device, "Size Used (%)", usedPercent);
        }
    }

    private void contributeMtab(Map<AList<String>, AEnvironmentData> data) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(new File("/etc/mtab")));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();

                if(! line.startsWith("/dev/")) {
                    continue;
                }

                final String[] split = line.split(" ");
                if(split.length < 4) {
                    continue; // should not happen, but you never know
                }

                final String device = split[0];
                final String mountPoint = split[1];
                final String fsType = split[2];
                final String flags = split[3];

                add(data, device, "Mount Point", mountPoint);
                add(data, device, "Type", fsType);
                add(data, device, "Flags", flags);
            }
        }
        finally {
            br.close();
        }
    }

    private void add(Map<AList<String>, AEnvironmentData> data, String device, String key, String value) {
        final AList<String> fullKey = fullKey(device, key);
        data.put(fullKey, new AEnvironmentData(fullKey, value));
    }

    private AList<String> fullKey(String device, String key) {
        return AList.create(ACpuEnvironmentMeasurer.KEY_HW, KEY_FILESYSTEMS, device, key);
    }
}
