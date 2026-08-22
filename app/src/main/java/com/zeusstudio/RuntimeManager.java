package com.zeusstudio;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RuntimeManager {

    private final Context context;
    private final File home;
    private final File bin;

    public RuntimeManager(Context context) {
        this.context = context.getApplicationContext();
        home = new File(this.context.getFilesDir(), "zeus-home");
        bin = new File(home, "bin");
    }

    public synchronized void initialize() {
        home.mkdirs();
        bin.mkdirs();
        new File(home, "projects").mkdirs();
        new File(home, "cache").mkdirs();
        new File(home, "tmp").mkdirs();

        writeEnvironmentFile();
        installRuntimeTestCommand();
    }

    private void writeEnvironmentFile() {
        File config = new File(home, "environment.properties");

        if (!config.exists()) {
            try (FileOutputStream out = new FileOutputStream(config)) {
                String text =
                        "ZEUS_STUDIO_RUNTIME=phase1.1A\n" +
                        "HOME=" + home.getAbsolutePath() + "\n" +
                        "BIN=" + bin.getAbsolutePath() + "\n" +
                        "TMPDIR=" + new File(home, "tmp").getAbsolutePath() + "\n";
                out.write(text.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ignored) {
            }
        }
    }

    private void installRuntimeTestCommand() {
        File test = new File(bin, "zeus-runtime-test");

        String script =
                "#!/system/bin/sh\n" +
                "echo ZEUS_RUNTIME_TEST_OK\n" +
                "echo HOME=$HOME\n" +
                "echo ARCH=$(uname -m)\n" +
                "echo PREFIX=$(dirname \"$0\")\n";

        try (FileOutputStream out = new FileOutputStream(test)) {
            out.write(script.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
            return;
        }

        // This test intentionally uses the app-private filesystem.
        // The executable permission is required for PATH-based execution.
        test.setExecutable(true, true);
        test.setReadable(true, true);
    }

    public File getHome() {
        initialize();
        return home;
    }

    public File getBin() {
        initialize();
        return bin;
    }

    public String getArchitecture() {
        String[] abis = android.os.Build.SUPPORTED_ABIS;
        return abis.length > 0 ? abis[0] : "unknown";
    }

    public String[] getSupportedArchitectures() {
        return android.os.Build.SUPPORTED_ABIS.clone();
    }

    public boolean commandExists(String command) {
        initialize();
        File candidate = new File(bin, command);
        return candidate.exists() && candidate.canExecute();
    }

    public String[] environment() {
        initialize();

        return new String[] {
                "HOME=" + home.getAbsolutePath(),
                "PATH=" + bin.getAbsolutePath() + ":/system/bin:/system/xbin",
                "TMPDIR=" + new File(home, "tmp").getAbsolutePath()
        };
    }
}
