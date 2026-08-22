package com.zeusstudio;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileOutputStream;

public class NodeRuntimeManager {

    private final Context context;

    private static final String NODE_ASSET = "node";
    private static final String RUNTIME_DIR = "node-runtime";

    public NodeRuntimeManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public File getRuntimeDirectory() {
        return new File(context.getFilesDir(), RUNTIME_DIR);
    }

    public File getNodeExecutable() {
        return new File(getRuntimeDirectory(), "node");
    }

    public boolean isInstalled() {
        File node = getNodeExecutable();

        return node.exists()
                && node.isFile()
                && node.canExecute();
    }

    public boolean installRuntime() throws IOException {
        File runtimeDir = getRuntimeDirectory();

        if (!runtimeDir.exists() && !runtimeDir.mkdirs()) {
            throw new IOException(
                    "Unable to create runtime directory: "
                            + runtimeDir.getAbsolutePath()
            );
        }

        File node = getNodeExecutable();

        try (InputStream input = context.getAssets().open(NODE_ASSET);
             OutputStream output = new FileOutputStream(node)) {

            byte[] buffer = new byte[8192];
            int count;

            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
        }

        if (!node.setExecutable(true, false)) {
            throw new IOException(
                    "Unable to mark Node executable: "
                            + node.getAbsolutePath()
            );
        }

        if (!node.exists() || node.length() == 0) {
            throw new IOException("Node runtime was not installed correctly.");
        }

        return true;
    }

    public String getNodeVersion() throws IOException {
        if (!isInstalled()) {
            throw new IOException("Node runtime is not installed.");
        }

        ProcessBuilder builder = new ProcessBuilder(
                getNodeExecutable().getAbsolutePath(),
                "--version"
        );

        builder.directory(getRuntimeDirectory());
        builder.redirectErrorStream(true);

        Process process = builder.start();

        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line).append('\n');
            }
        }

        try {
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new IOException(
                        "Node exited with code " + exitCode
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Node execution was interrupted.", e);
        }

        return result.toString().trim();
    }

    public String getStatus() {
        if (isInstalled()) {
            return "Installed";
        }

        return "Not installed";
    }
}