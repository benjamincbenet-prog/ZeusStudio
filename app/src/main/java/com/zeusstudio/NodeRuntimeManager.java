package com.zeusstudio;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeRuntimeManager {

    public interface OutputListener {
        void onOutput(String line);
    }

    private final Context context;

    private final File zeusHome;
    private final File binDirectory;
    private final File runtimeDirectory;
    private final File projectsDirectory;
    private final File cacheDirectory;
    private final File tmpDirectory;

    public NodeRuntimeManager(Context context) {
        this.context = context.getApplicationContext();

        zeusHome = new File(context.getFilesDir(), "zeus-home");

        binDirectory = new File(zeusHome, "bin");
        runtimeDirectory = new File(zeusHome, "runtime");
        projectsDirectory = new File(zeusHome, "projects");
        cacheDirectory = new File(zeusHome, "cache");
        tmpDirectory = new File(zeusHome, "tmp");
    }

   public boolean initialize() {

        boolean success = true;

        success &= ensureDirectory(zeusHome);
        success &= ensureDirectory(binDirectory);
        success &= ensureDirectory(runtimeDirectory);
        success &= ensureDirectory(projectsDirectory);
        success &= ensureDirectory(cacheDirectory);
        success &= ensureDirectory(tmpDirectory);
    
        return success;
    }

    private boolean ensureDirectory(File directory) {
    
        if (directory.exists()) {
            return directory.isDirectory();
        }
    
        return directory.mkdirs();
    }
    
    public File getZeusHome() {
        return zeusHome;
    }

    public File getBinDirectory() {
        return binDirectory;
    }

    public File getRuntimeDirectory() {
        return runtimeDirectory;
    }

    public File getProjectsDirectory() {
        return projectsDirectory;
    }

    public File getCacheDirectory() {
        return cacheDirectory;
    }

    public File getTmpDirectory() {
        return tmpDirectory;
    }

    public File getNodeExecutable() {
        return new File(binDirectory, "node");
    }

    public boolean isNodeInstalled() {
        File node = getNodeExecutable();

        return node.exists() && node.canExecute();
    }

    public String getArchitecture() {
        return android.os.Build.SUPPORTED_ABIS.length > 0
                ? android.os.Build.SUPPORTED_ABIS[0]
                : "unknown";
    }

    public ProcessResult execute(
            List<String> command,
            OutputListener listener
    ) throws IOException, InterruptedException {

        ProcessBuilder builder = new ProcessBuilder(command);

        Map<String, String> environment = builder.environment();

        environment.put("HOME", zeusHome.getAbsolutePath());
        environment.put("TMPDIR", tmpDirectory.getAbsolutePath());

        String existingPath = environment.get("PATH");

        String path = binDirectory.getAbsolutePath();

        if (existingPath != null && !existingPath.isEmpty()) {
            path += File.pathSeparator + existingPath;
        }

        environment.put("PATH", path);

        builder.directory(zeusHome);

        builder.redirectErrorStream(true);

        Process process = builder.start();

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                process.getInputStream()
                        )
                );

        String line;

        while ((line = reader.readLine()) != null) {

            if (listener != null) {
                listener.onOutput(line);
            }
        }

        int exitCode = process.waitFor();

        return new ProcessResult(exitCode);
    }

    public ProcessResult testNode(
            OutputListener listener
    ) throws IOException, InterruptedException {

        if (!isNodeInstalled()) {

            if (listener != null) {
                listener.onOutput(
                        "Node is not installed."
                );
            }

            return new ProcessResult(-1);
        }

        List<String> command = Arrays.asList(
                getNodeExecutable().getAbsolutePath(),
                "-e",
                "console.log('ZEUS_NODE_TEST_OK');" +
                "console.log('version=' + process.version);" +
                "console.log('platform=' + process.platform);" +
                "console.log('arch=' + process.arch);"
        );

        return execute(command, listener);
    }

    public ProcessResult testShell(
            OutputListener listener
    ) throws IOException, InterruptedException {

        List<String> command = new ArrayList<>();

        command.add("/system/bin/sh");
        command.add("-c");

        command.add(
                "echo ZEUS_SHELL_TEST_OK; " +
                "echo HOME=$HOME; " +
                "echo TMPDIR=$TMPDIR; " +
                "echo PATH=$PATH"
        );

        return execute(command, listener);
    }

    public static class ProcessResult {

        private final int exitCode;

        public ProcessResult(int exitCode) {
            this.exitCode = exitCode;
        }

        public int getExitCode() {
            return exitCode;
        }

        public boolean isSuccess() {
            return exitCode == 0;
        }
    }
}