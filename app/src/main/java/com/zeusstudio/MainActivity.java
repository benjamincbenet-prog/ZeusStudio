package com.zeusstudio;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private NodeRuntimeManager runtimeManager;

    private TextView nodeStatus;
    private TextView statusText;
    private Button installButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        runtimeManager =
                new NodeRuntimeManager(this);

        buildInterface();

        refreshRuntimeStatus();
    }

    private void buildInterface() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                48,
                48,
                48,
                48
        );

        ScrollView scrollView =
                new ScrollView(this);

        scrollView.addView(root);

        TextView title =
                new TextView(this);

        title.setText(
                "ZEUS STUDIO"
        );

        title.setTextSize(28);

        title.setGravity(
                Gravity.CENTER
        );

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "Zepp OS Development Environment"
        );

        subtitle.setTextSize(16);

        subtitle.setGravity(
                Gravity.CENTER
        );

        root.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        addSpacer(root, 32);

        TextView runtimeTitle =
                new TextView(this);

        runtimeTitle.setText(
                "Runtime"
        );

        runtimeTitle.setTextSize(22);

        root.addView(
                runtimeTitle
        );

        addSpacer(root, 16);

        nodeStatus =
                new TextView(this);

        nodeStatus.setTextSize(18);

        root.addView(
                nodeStatus
        );

        addSpacer(root, 8);

        TextView architecture =
                new TextView(this);

        architecture.setText(
                "Architecture: "
                        + android.os.Build.SUPPORTED_ABIS[0]
        );

        architecture.setTextSize(16);

        root.addView(
                architecture
        );

        addSpacer(root, 24);

        installButton =
                new Button(this);

        installButton.setText(
                "Install Runtime"
        );

        root.addView(
                installButton,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        addSpacer(root, 24);

        TextView statusTitle =
                new TextView(this);

        statusTitle.setText(
                "Status"
        );

        statusTitle.setTextSize(22);

        root.addView(
                statusTitle
        );

        addSpacer(root, 8);

        statusText =
                new TextView(this);

        statusText.setTextSize(16);

        root.addView(
                statusText
        );

        installButton.setOnClickListener(
                view -> installRuntime()
        );

        setContentView(scrollView);
    }

    private void refreshRuntimeStatus() {

        if (runtimeManager.isRuntimeInstalled()) {

            nodeStatus.setText(
                    "Node.js: "
                            + runtimeManager.getNodeVersion()
            );

            statusText.setText(
                    "Runtime installed and executable."
            );

            installButton.setText(
                    "Reinstall Runtime"
            );

        } else {

            nodeStatus.setText(
                    "Node.js: Not installed"
            );

            statusText.setText(
                    "Ready to install."
            );

            installButton.setText(
                    "Install Runtime"
            );
        }
    }

    private void installRuntime() {

        installButton.setEnabled(false);

        statusText.setText(
                "Starting runtime installation..."
        );

        runtimeManager.installRuntime(
                new NodeRuntimeManager.InstallCallback() {

                    @Override
                    public void onStatus(
                            String status
                    ) {

                        runOnUiThread(() ->
                                statusText.setText(
                                        status
                                )
                        );
                    }

                    @Override
                    public void onSuccess(
                            String version
                    ) {

                        runOnUiThread(() -> {

                            nodeStatus.setText(
                                    "Node.js: "
                                            + version
                            );

                            statusText.setText(
                                    "Runtime installed successfully."
                            );

                            installButton.setEnabled(
                                    true
                            );

                            installButton.setText(
                                    "Reinstall Runtime"
                            );
                        });
                    }

                    @Override
                    public void onError(
                            String error
                    ) {

                        runOnUiThread(() -> {

                            statusText.setText(
                                    "Installation failed:\n\n"
                                            + error
                            );

                            installButton.setEnabled(
                                    true
                            );
                        });
                    }
                }
        );
    }

    private void addSpacer(
            LinearLayout parent,
            int height
    ) {

        TextView spacer =
                new TextView(this);

        parent.addView(
                spacer,
                new LinearLayout.LayoutParams(
                        1,
                        height
                )
        );
    }
}