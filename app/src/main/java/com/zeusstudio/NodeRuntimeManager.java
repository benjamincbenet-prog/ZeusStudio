package com.zeusstudio;

import android.content.Context;
import android.os.Build;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class NodeRuntimeManager {

    private static final String RUNTIME_DIR_NAME = "zeus-runtime";
    private static final String NODE_RELATIVE_PATH =
            "data/data/com.termux/files/usr/bin/node";

    /*
     * We'll replace these with the GitHub Release URLs once
     * we publish the first runtime release.
     */
    private static final String RUNTIME_URL =
            "https://github.com/benjamincbenet-prog/ZeusStudio/releases/download/v0.1.0-runtime/zeus-node-runtime-android-arm64.tar.gz";

    private static final String CHECKSUM_URL =
            "https://github.com/benjamincbenet-prog/ZeusStudio/releases/download/v0.1.0-runtime/zeus-node-runtime-android-arm64.sha256";

    private final Context context;

    public NodeRuntimeManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public File getRuntimeDirectory() {
        return new File(context.getFilesDir(), RUNTIME_DIR_NAME);
    }

    public File getNodeFile() {
        return new File(
                getRuntimeDirectory(),
                NODE_RELATIVE_PATH
        );
    }

    public boolean isRuntimeInstalled() {
        File node = getNodeFile();

        return node.exists()
                && node.isFile()
                && node.canExecute();
    }

    public String getNodeVersion() {
        if (!isRuntimeInstalled()) {
            return "Node runtime not installed";
        }

        try {
            ProcessBuilder builder = new ProcessBuilder(
                    getNodeFile().getAbsolutePath(),
                    "--version"
            );

            builder.directory(getRuntimeDirectory());

            builder.redirectErrorStream(true);

            Process process = builder.start();

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    process.getInputStream()
                            )
                    );

            StringBuilder output = new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append('\n');
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return "Node failed: " + output;
            }

            return output.toString().trim();

        } catch (Exception e) {
            return "Node error: " + e.getMessage();
        }
    }

    public void installRuntime(
            InstallCallback callback
    ) {

        Executors.newSingleThreadExecutor().execute(() -> {

            try {

                callback.onStatus(
                        "Downloading Zeus Node runtime..."
                );

                File runtimeDir = getRuntimeDirectory();

                if (!runtimeDir.exists()
                        && !runtimeDir.mkdirs()) {

                    throw new IOException(
                            "Unable to create runtime directory"
                    );
                }

                File archive = new File(
                        context.getCacheDir(),
                        "zeus-node-runtime.tar.gz"
                );

                downloadFile(
                        RUNTIME_URL,
                        archive
                );

                callback.onStatus(
                        "Downloading checksum..."
                );

                File checksumFile = new File(
                        context.getCacheDir(),
                        "zeus-node-runtime.sha256"
                );

                downloadFile(
                        CHECKSUM_URL,
                        checksumFile
                );

                callback.onStatus(
                        "Verifying runtime..."
                );

                String expected =
                        readChecksum(checksumFile);

                String actual =
                        sha256(archive);

                if (!expected.equalsIgnoreCase(actual)) {

                    throw new IOException(
                            "Runtime checksum mismatch\n"
                                    + "Expected: "
                                    + expected
                                    + "\nActual: "
                                    + actual
                    );
                }

                callback.onStatus(
                        "Extracting runtime..."
                );

                deleteRecursive(runtimeDir);

                if (!runtimeDir.mkdirs()) {

                    throw new IOException(
                            "Unable to recreate runtime directory"
                    );
                }

                extractTarGz(
                        archive,
                        runtimeDir
                );

                File node = getNodeFile();

                if (!node.exists()) {

                    throw new IOException(
                            "Node executable was not found after extraction:\n"
                                    + node.getAbsolutePath()
                    );
                }

                if (!node.setExecutable(true, false)) {

                    throw new IOException(
                            "Unable to make Node executable"
                    );
                }

                callback.onStatus(
                        "Testing Node..."
                );

                String version =
                        getNodeVersion();

                if (!version.startsWith("v")) {

                    throw new IOException(
                            "Node test failed: " + version
                    );
                }

                callback.onSuccess(version);

            } catch (Exception e) {

                callback.onError(
                        e.getMessage() != null
                                ? e.getMessage()
                                : e.toString()
                );
            }
        });
    }

    private void downloadFile(
            String urlString,
            File destination
    ) throws IOException {

        HttpURLConnection connection =
                (HttpURLConnection)
                        new URL(urlString)
                                .openConnection();

        connection.setConnectTimeout(30000);
        connection.setReadTimeout(120000);
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);

        int responseCode =
                connection.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {

            throw new IOException(
                    "Download failed: HTTP "
                            + responseCode
            );
        }

        try (
                InputStream input =
                        new BufferedInputStream(
                                connection.getInputStream()
                        );

                OutputStream output =
                        new BufferedOutputStream(
                                new FileOutputStream(
                                        destination
                                )
                        )
        ) {

            byte[] buffer = new byte[8192];

            int count;

            while ((count = input.read(buffer)) != -1) {

                output.write(
                        buffer,
                        0,
                        count
                );
            }

            output.flush();

        } finally {

            connection.disconnect();
        }
    }

    private String readChecksum(
            File checksumFile
    ) throws IOException {

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        new FileInputStream(
                                                checksumFile
                                        )
                                )
                        )
        ) {

            String line =
                    reader.readLine();

            if (line == null
                    || line.trim().isEmpty()) {

                throw new IOException(
                        "Checksum file is empty"
                );
            }

            /*
             * sha256sum normally produces:
             *
             * HASH  filename
             *
             * We only need the hash.
             */
            return line.trim()
                    .split("\\s+")[0];
        }
    }

    private String sha256(
            File file
    ) throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance(
                        "SHA-256"
                );

        try (
                InputStream input =
                        new BufferedInputStream(
                                new FileInputStream(
                                        file
                                )
                        )
        ) {

            byte[] buffer =
                    new byte[8192];

            int count;

            while ((count =
                    input.read(buffer)) != -1) {

                digest.update(
                        buffer,
                        0,
                        count
                );
            }
        }

        StringBuilder result =
                new StringBuilder();

        for (byte b : digest.digest()) {

            result.append(
                    String.format(
                            "%02x",
                            b
                    )
            );
        }

        return result.toString();
    }

    private void extractTarGz(
            File archive,
            File destination
    ) throws IOException {

        /*
         * Java's standard library does not contain
         * a TAR reader. We deliberately don't use
         * native C/C++ here.
         *
         * This method will be replaced with our
         * small pure-Java TAR/GZIP extractor in
         * the next step.
         */

        throw new IOException(
                "TAR extraction not implemented yet"
        );
    }

    private void deleteRecursive(
            File file
    ) {

        if (file == null
                || !file.exists()) {
            return;
        }

        if (file.isDirectory()) {

            File[] children =
                    file.listFiles();

            if (children != null) {

                for (File child : children) {

                    deleteRecursive(child);
                }
            }
        }

        file.delete();
    }

    public interface InstallCallback {

        void onStatus(String status);

        void onSuccess(String version);

        void onError(String error);
    }
}