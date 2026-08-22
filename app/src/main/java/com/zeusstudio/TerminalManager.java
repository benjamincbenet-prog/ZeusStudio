package com.zeusstudio;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TerminalManager {

    private final RuntimeManager runtime;

    public TerminalManager(RuntimeManager runtime) {
        this.runtime = runtime;
    }

    public CommandResult execute(String command) {
        try {
            runtime.initialize();

            ProcessBuilder pb = new ProcessBuilder(
                    "/system/bin/sh", "-c", command);

            pb.directory(runtime.getHome());
            pb.redirectErrorStream(true);

            for (String env : runtime.environment()) {
                int separator = env.indexOf('=');
                if (separator > 0) {
                    pb.environment().put(
                            env.substring(0, separator),
                            env.substring(separator + 1));
                }
            }

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append('\n');
            }

            int exitCode = process.waitFor();

            return new CommandResult(output.toString().trim(), exitCode);

        } catch (Exception e) {
            return new CommandResult(
                    e.getClass().getSimpleName() + ": " + e.getMessage(), -1);
        }
    }
}
