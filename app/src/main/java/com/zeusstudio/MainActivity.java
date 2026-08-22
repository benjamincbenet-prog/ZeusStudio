package com.zeusstudio;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView output;
    private TextView nodeStatus;
    private TextView npmStatus;
    private TextView zeusStatus;
    private TextView testStatus;

    private TerminalManager terminal;
    private RuntimeManager runtime;
    private NodeRuntimeManager nodeRuntimeManager;

    private static boolean nodeStarted = false;

    private int dp(float value) {
        return (int) (
                value *
                getResources()
                        .getDisplayMetrics()
                        .density +
                0.5f
        );
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        /*
         * Existing Zeus Studio runtime.
         */
        runtime =
                new RuntimeManager(
                        getApplicationContext()
                );

        terminal =
                new TerminalManager(runtime);

        /*
         * Phase 1.1B managed Node runtime.
         */
        nodeRuntimeManager =
                new NodeRuntimeManager(this);

        /*
         * Initialize the managed Zeus directory
         * structure.
         */
        boolean initialized =
                nodeRuntimeManager.initialize();

        if (initialized) {
            appendOutput(
                    "Zeus Node runtime environment initialized."
            );
        } else {
            appendOutput(
                    "WARNING: Could not initialize " +
                    "Zeus Node runtime environment."
            );
        }

        buildUi();
        refreshStatus();
    }

    private TextView label(
            String text,
            float size
    ) {

        TextView v =
                new TextView(this);

        v.setText(text);
        v.setTextColor(Color.WHITE);
        v.setTextSize(size);

        v.setPadding(
                dp(12),
                dp(8),
                dp(12),
                dp(8)
        );

        return v;
    }

    private Button button(String text) {

        Button b =
                new Button(this);

        b.setText(text);
        b.setAllCaps(false);

        return b;
    }

    private void buildUi() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(
                Color.rgb(16, 17, 20)
        );

        /*
         * -------------------------------------------------
         * TOOLBAR
         * -------------------------------------------------
         */

        LinearLayout toolbar =
                new LinearLayout(this);

        toolbar.setGravity(
                Gravity.CENTER_VERTICAL
        );

        toolbar.setPadding(
                dp(8),
                dp(4),
                dp(8),
                dp(4)
        );

        TextView title =
                label(
                        "Zeus Studio",
                        20
                );

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        toolbar.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        dp(56),
                        1
                )
        );

        Button settings =
                button("⚙");

        settings.setOnClickListener(
                v -> appendOutput(
                        "Settings will be added " +
                        "in a later phase."
                )
        );

        toolbar.addView(
                settings,
                new LinearLayout.LayoutParams(
                        dp(56),
                        dp(56)
                )
        );

        root.addView(toolbar);

        /*
         * -------------------------------------------------
         * DEVELOPMENT ENVIRONMENT
         * -------------------------------------------------
         */

        TextView envTitle =
                label(
                        "DEVELOPMENT ENVIRONMENT",
                        13
                );

        envTitle.setTextColor(
                Color.LTGRAY
        );

        root.addView(envTitle);

        LinearLayout env =
                new LinearLayout(this);

        env.setOrientation(
                LinearLayout.VERTICAL
        );

        env.setPadding(
                dp(12),
                0,
                dp(12),
                0
        );

        nodeStatus =
                label("", 15);

        npmStatus =
                label("", 15);

        zeusStatus =
                label("", 15);

        testStatus =
                label("", 15);

        env.addView(nodeStatus);
        env.addView(npmStatus);
        env.addView(zeusStatus);
        env.addView(testStatus);

        /*
         * Environment buttons.
         */

        LinearLayout actions =
                new LinearLayout(this);

        actions.setOrientation(
                LinearLayout.HORIZONTAL
        );

        Button initialize =
                button("Initialize");

        initialize.setOnClickListener(
                v -> initializeEnvironment()
        );

        actions.addView(
                initialize,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        Button runtimeTest =
                button("Node Test");

        runtimeTest.setOnClickListener(
                v -> runNodeRuntimeTest()
        );

        actions.addView(
                runtimeTest,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        env.addView(actions);

        root.addView(env);

        /*
         * -------------------------------------------------
         * TERMINAL
         * -------------------------------------------------
         */

        TextView termTitle =
                label(
                        "TERMINAL",
                        13
                );

        termTitle.setTextColor(
                Color.LTGRAY
        );

        root.addView(termTitle);

        ScrollView scroll =
                new ScrollView(this);

        output =
                new TextView(this);

        output.setTextColor(
                Color.rgb(
                        220,
                        220,
                        220
                )
        );

        output.setTextSize(13);

        output.setTypeface(
                Typeface.MONOSPACE
        );

        output.setPadding(
                dp(12),
                dp(12),
                dp(12),
                dp(12)
        );

        output.setBackgroundColor(
                Color.rgb(
                        8,
                        9,
                        11
                )
        );

        scroll.addView(output);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        /*
         * -------------------------------------------------
         * COMMAND BAR
         * -------------------------------------------------
         */

        LinearLayout commandBar =
                new LinearLayout(this);

        commandBar.setPadding(
                dp(6),
                dp(4),
                dp(6),
                dp(4)
        );

        /*
         * Existing runtime test.
         */

        Button test =
                button("Runtime Test");

        test.setOnClickListener(
                v -> run(
                        "zeus-runtime-test"
                )
        );

        commandBar.addView(
                test,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        /*
         * Shell.
         */

        Button shell =
                button("Shell");

        shell.setOnClickListener(
                v -> run(
                        "echo $HOME && " +
                        "echo $PATH && " +
                        "uname -m"
                )
        );

        commandBar.addView(
                shell,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        /*
         * Existing managed-runtime Node test.
         *
         * This uses RuntimeManager rather than
         * the removed native Node library.
         */

        Button node =
                button("node --version");

        node.setOnClickListener(
                v -> run(
                        "node --version"
                )
        );

        commandBar.addView(
                node,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        /*
         * Zeus CLI.
         */

        Button zeus =
                button("zeus --version");

        zeus.setOnClickListener(
                v -> run(
                        "zeus --version"
                )
        );

        commandBar.addView(
                zeus,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        root.addView(commandBar);

        setContentView(root);
    }

    /*
     * -----------------------------------------------------
     * OUTPUT
     * -----------------------------------------------------
     */

    private void appendOutput(String text) {

        runOnUiThread(() -> {

            if (output != null) {

                output.append(
                        text + "\n"
                );

                /*
                 * Keep the terminal scrolled toward
                 * the newest output.
                 */
                output.post(() -> {

                    if (output.getParent()
                            instanceof ScrollView) {

                        ScrollView parent =
                                (ScrollView)
                                        output.getParent();

                        parent.fullScroll(
                                ScrollView.FOCUS_DOWN
                        );
                    }
                });
            }
        });
    }

    /*
     * -----------------------------------------------------
     * EXISTING RUNTIME COMMAND
     * -----------------------------------------------------
     */

    private void run(String command) {

        appendOutput(
                "$ " + command
        );

        new Thread(() -> {

            CommandResult result =
                    terminal.execute(command);

            appendOutput(
                    result.output
            );

            if (result.exitCode != 0) {

                appendOutput(
                        "exit code: " +
                        result.exitCode
                );
            }

        }).start();
    }

    /*
     * -----------------------------------------------------
     * ENVIRONMENT INITIALIZATION
     * -----------------------------------------------------
     */

    private void initializeEnvironment() {

        appendOutput(
                "Initializing Zeus Studio runtime..."
        );

        new Thread(() -> {

            runtime.initialize();

            /*
             * Also initialize the managed Node
             * environment.
             */
            boolean nodeInitialized =
                    nodeRuntimeManager.initialize();

            appendOutput(
                    "Runtime initialized."
            );

            appendOutput(
                    "HOME: " +
                    runtime.getHome()
                            .getAbsolutePath()
            );

            appendOutput(
                    "BIN: " +
                    runtime.getBin()
                            .getAbsolutePath()
            );

            appendOutput(
                    "Architecture: " +
                    runtime.getArchitecture()
            );

            appendOutput(
                    "Node runtime environment: " +
                    (
                            nodeInitialized
                                    ? "initialized"
                                    : "FAILED"
                    )
            );

            runOnUiThread(
                    this::refreshStatus
            );

        }).start();
    }

    /*
     * -----------------------------------------------------
     * PHASE 1.1B NODE RUNTIME TEST
     * -----------------------------------------------------
     */

    private void runNodeRuntimeTest() {

        if (nodeStarted) {

            appendOutput(
                    "Node runtime test is already running."
            );

            return;
        }

        nodeStarted = true;

        appendOutput(
                "\n=== ZEUS NODE RUNTIME TEST ==="
        );

        new Thread(() -> {

            try {

                /*
                 * Report architecture.
                 */

                appendOutput(
                        "Architecture: " +
                        nodeRuntimeManager
                                .getArchitecture()
                );

                /*
                 * Report Zeus managed home.
                 */

                appendOutput(
                        "Node Home: " +
                        nodeRuntimeManager
                                .getZeusHome()
                                .getAbsolutePath()
                );

                /*
                 * Report Node executable.
                 */

                appendOutput(
                        "Node executable: " +
                        nodeRuntimeManager
                                .getNodeExecutable()
                                .getAbsolutePath()
                );

                /*
                 * Check whether Node exists.
                 */

                boolean installed =
                        nodeRuntimeManager
                                .isNodeInstalled();

                appendOutput(
                        "Node installed: " +
                        installed
                );

                /*
                 * Run Node if available.
                 */

                NodeRuntimeManager.ProcessResult result =
                        nodeRuntimeManager.testNode(
                                line -> appendOutput(line)
                        );

                appendOutput(
                        "Exit code: " +
                        result.getExitCode()
                );

                if (result.isSuccess()) {

                    appendOutput(
                            "✓ Node runtime test passed."
                    );

                } else {

                    appendOutput(
                            "✗ Node runtime test failed."
                    );
                }

            } catch (Exception e) {

                appendOutput(
                        "ERROR: " +
                        e.getClass()
                                .getSimpleName() +
                        ": " +
                        e.getMessage()
                );

            } finally {

                nodeStarted = false;
            }

        }).start();
    }

    /*
     * -----------------------------------------------------
     * STATUS
     * -----------------------------------------------------
     */

    private void refreshStatus() {

        runtime.initialize();

        nodeStatus.setText(
                "Node.js          " +
                (
                        runtime.commandExists(
                                "node"
                        )
                                ? "✓ available"
                                : "○ not installed"
                )
        );

        npmStatus.setText(
                "npm              " +
                (
                        runtime.commandExists(
                                "npm"
                        )
                                ? "✓ available"
                                : "○ not installed"
                )
        );

        zeusStatus.setText(
                "Zeus CLI         " +
                (
                        runtime.commandExists(
                                "zeus"
                        )
                                ? "✓ available"
                                : "○ not installed"
                )
        );

        testStatus.setText(
                "Runtime Test     " +
                (
                        runtime.commandExists(
                                "zeus-runtime-test"
                        )
                                ? "✓ installed/executable"
                                : "✕ unavailable"
                )
        );

        appendOutput(
                "Zeus Studio Phase 1.1B"
        );

        appendOutput(
                "Gradle target: 8.13.1"
        );

        appendOutput(
                "Runtime home: " +
                runtime.getHome()
                        .getAbsolutePath()
        );

        appendOutput(
                "Architecture: " +
                runtime.getArchitecture()
        );

        StringBuilder abis =
                new StringBuilder(
                        "Supported ABIs: "
                );

        for (
                String abi :
                runtime.getSupportedArchitectures()
        ) {

            if (abis.length() > 17) {
                abis.append(", ");
            }

            abis.append(abi);
        }

        appendOutput(
                abis.toString()
        );

        /*
         * Phase 1.1B Node runtime information.
         */

        appendOutput(
                "Managed Node home: " +
                nodeRuntimeManager
                        .getZeusHome()
                        .getAbsolutePath()
        );

        appendOutput(
                "Managed Node installed: " +
                nodeRuntimeManager
                        .isNodeInstalled()
        );
    }
}