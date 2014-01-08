package com.ajjpj.asysmon.measure.environment;


import com.ajjpj.asysmon.util.UnixCommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/**
 * This measurer collects information about mounted file systems from several sources.
 *
 * @author arno
 */
public class AFileSystemsEnvironmentMeasurer implements AEnvironmentMeasurer {
    public static final String KEY_FILESYSTEMS = "file systems";

    @Override public void contributeMeasurements(EnvironmentCollector data) throws Exception {
        contributeMtab(data);
        contributeDf(data);
    }

    private void contributeDf(EnvironmentCollector data) throws Exception {
        for(String line: new UnixCommand("df", "-P").getOutput()) {
            if(! line.startsWith("/dev/")) {
                continue;
            }

            final String[] split = line.split("\\s+");
            if(split.length != 6) {
                continue;
            }

            final String device = split[0];
            final String size = split[1];
            final String used = split[2];
            final String available = split[3];
            final String usedPercent = split[4];
            final String mountPoint = split[5];

            add(data, device, "Size Total (1k Blocks)", size);
            add(data, device, "Size Used (1k Blocks)", used);
            add(data, device, "Size Available (1k Blocks)", available);
            add(data, device, "Size Used (%)", usedPercent);
        }
    }

    private void contributeMtab(EnvironmentCollector data) throws IOException {
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

                add(data, device, "Type", fsType);
                add(data, device, "Flags", flags);

                data.add(mountPoint, ACpuEnvironmentMeasurer.KEY_HW, KEY_FILESYSTEMS, device);
            }
        }
        finally {
            br.close();
        }
    }

    private void add(EnvironmentCollector data, String device, String key, String value) {
        data.add(value, ACpuEnvironmentMeasurer.KEY_HW, KEY_FILESYSTEMS, device, key);
    }
}
