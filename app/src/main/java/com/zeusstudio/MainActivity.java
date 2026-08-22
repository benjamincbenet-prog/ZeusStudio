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
    private TextView testStatus;

    private NodeRuntimeManager nodeRuntimeManager;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        nodeRuntimeManager =
                new NodeRuntimeManager(getApplicationContext());

        buildInterface();
        refreshStatus();
    }

    private int dp(float value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics()
                        .density + 0.5f
        );
    }

    private TextView label(String text, float size) {
        TextView v = new TextView(this);
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
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        return b;
    }

    private void buildInterface() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(
                Color.rgb(16, 17, 20)
        );

        // Toolbar
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
                label("Zeus Studio", 20);

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

        settings.setOnClickListener(v ->
                appendOutput(
                        "Settings will be added in a later phase."
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

        // Environment title
        TextView envTitle =
                label(
                        "DEVELOPMENT ENVIRONMENT",
                        13
                );

        envTitle.setTextColor(Color.LTGRAY);

        root.addView(envTitle);

        LinearLayout environment =
                new LinearLayout(this);

        environment.setOrientation(
                LinearLayout.VERTICAL
        );

        environment.setPadding(
                dp(12),
                0,
                dp(12),
                0
        );

        // Node status
        nodeStatus =
                label("", 15);

        environment.addView(nodeStatus);

        // Install Runtime button
        Button installRuntime =
                button("Install Runtime");

        installRuntime.setOnClickListener(
                v -> installRuntime()
        );

        environment.addView(
                installRuntime
        );

        // Runtime test button
        Button testRuntime =
                button("Test Runtime");

        testRuntime.setOnClickListener(
                v -> testRuntime()
        );

        environment.addView(
                testRuntime
        );

        // Test status
        testStatus =
                label("", 15);

        environment.addView(testStatus);

        root.addView(environment);

        // Output
        TextView outputTitle =
                label("OUTPUT", 13);

        outputTitle.setTextColor(Color.LTGRAY);

        root.addView(outputTitle);

        ScrollView scroll =
                new ScrollView(this);

        output =
                label("", 14);

        output.setTextIsSelectable(true);

        scroll.addView(output);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        setContentView(root);
    }

    private void refreshStatus() {

        if (nodeRuntimeManager.isInstalled()) {

            nodeStatus.setText(
                    "Node Runtime: Installed\n"
                            + nodeRuntimeManager
                            .getNodeExecutable()
                            .getAbsolutePath()
            );

        } else {

            nodeStatus.setText(
                    "Node Runtime: Not installed"
            );
        }
    }

    private void installRuntime() {

        appendOutput(
                "Installing Node runtime..."
        );

        setButtonsEnabled(false);

        new Thread(() -> {

            try {

                nodeRuntimeManager.installRuntime();

                String version =
                        nodeRuntimeManager
                                .getNodeVersion();

                runOnUiThread(() -> {

                    nodeStatus.setText(
                            "Node Runtime: Installed\n"
                                    + "Version: "
                                    + version
                    );

                    testStatus.setText(
                            "Runtime installation successful."
                    );

                    appendOutput(
                            "Node installed successfully."
                    );

                    appendOutput(
                            "Version: "
                                    + version
                    );

                    appendOutput(
                            "Location: "
                                    + nodeRuntimeManager
                                    .getNodeExecutable()
                                    .getAbsolutePath()
                    );

                    setButtonsEnabled(true);
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    testStatus.setText(
                            "Runtime installation failed."
                    );

                    appendOutput(
                            "ERROR: "
                                    + e.getMessage()
                    );

                    setButtonsEnabled(true);
                });
            }

        }).start();
    }

    private void testRuntime() {

        appendOutput(
                "Testing Node runtime..."
        );

        new Thread(() -> {

            try {

                String version =
                        nodeRuntimeManager
                                .getNodeVersion();

                runOnUiThread(() -> {

                    testStatus.setText(
                            "Runtime OK: "
                                    + version
                    );

                    appendOutput(
                            "Node runtime test passed."
                    );

                    appendOutput(
                            "Node version: "
                                    + version
                    );
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    testStatus.setText(
                            "Runtime test failed."
                    );

                    appendOutput(
                            "ERROR: "
                                    + e.getMessage()
                    );
                });
            }

        }).start();
    }

    private void setButtonsEnabled(
            boolean enabled
    ) {
        // Buttons will be wired here
        // in the next UI refinement.
    }

    private void appendOutput(String text) {

        if (output == null) {
            return;
        }

        output.append(
                text + "\n"
        );
    }
}