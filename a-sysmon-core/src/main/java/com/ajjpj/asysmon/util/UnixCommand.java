package com.ajjpj.asysmon.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * This utility class executes a command as a separate process, returning its output (i.e. stdout) as a list of strings, with each
 *  line as a separate element.<p/>
 *
 * To guard the calling JVM against overflow, it limits the number of lines of output that are stored.<p />
 *
 * NB: This class executes the command *synchronously*, blocking until it returns. For long running commands, that may
 *  or may not be a problem for calling code.<p />
 *
 * @author arno
 */
public class UnixCommand {
    private final List<String> stdout = new ArrayList<String>();
    private final int returnCode;

    //TODO limit number of *bytes* that are stored?
    //TODO stderr?
    public UnixCommand(String... cmd) throws IOException, InterruptedException {
        this(1000, cmd);
    }

    public UnixCommand(int maxLinesOfOutput, String... cmd) throws IOException, InterruptedException {
        final ProcessBuilder pb = new ProcessBuilder(cmd);
        final Process process = pb.start();
        final BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream())); // platform encoding is intended here!

        String line;
        while(stdout.size() < maxLinesOfOutput && (line = in.readLine()) != null) {
            stdout.add(line);
        }

        returnCode = process.waitFor();
    }

    public List<String> getOutput() {
        return stdout;
    }

    public int getReturnCode() {
        return returnCode;
    }
}
