package com.autodiag2.androbd.plugin.traccar;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ListView;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.text.TextWatcher;
import android.text.Editable;

public class SettingsActivity extends PreferenceActivity {

    private EditText hostEdit;
    private EditText portEdit;
    private EditText deviceEdit;

    private static LinearLayout.LayoutParams lp(int topMargin) {
        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        p.topMargin = topMargin;
        return p;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView list = new ListView(this);
        list.setId(android.R.id.list);
        list.setFocusable(false);
        list.setFocusableInTouchMode(false);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setOnTouchListener((v, event) -> {
            v.requestFocus();
            return false;
        });

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 48, 48, 48);
        root.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("Traccar server");
        title.setTextSize(20f);
        root.addView(title, lp(0));

        this.hostEdit = new EditText(this);
        String savedHost = SettingsStore.getHost(this);
        this.hostEdit.setHint("ip / hostname");
        this.hostEdit.setText(savedHost);
        this.hostEdit.setFocusable(true);
        this.hostEdit.setFocusableInTouchMode(true);
        this.hostEdit.setClickable(true);
        this.hostEdit.clearFocus();
        root.addView(this.hostEdit, lp(32));

        this.portEdit = new EditText(this);
        this.portEdit.setHint("port");
        this.portEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        int savedPort = SettingsStore.getPort(this);
        this.portEdit.setText(String.valueOf(savedPort));
        this.portEdit.setFocusable(true);
        this.portEdit.setFocusableInTouchMode(true);
        this.portEdit.setClickable(true);
        this.portEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.post(v::requestFocus);
            }
        });
        root.addView(this.portEdit, lp(16));

        this.deviceEdit = new EditText(this);
        this.deviceEdit.setHint("my device");
        String savedDevice = SettingsStore.getDeviceId(this);
        this.deviceEdit.setText(savedDevice);
        this.deviceEdit.setFocusable(true);
        this.deviceEdit.setFocusableInTouchMode(true);
        this.deviceEdit.setClickable(true);
        this.deviceEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.post(v::requestFocus);
            }
        });
        root.addView(this.deviceEdit, lp(16));

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveSettings();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        hostEdit.addTextChangedListener(watcher);
        portEdit.addTextChangedListener(watcher);
        deviceEdit.addTextChangedListener(watcher);

        Button connectBtn = new Button(this);
        connectBtn.setText("Connect");
        root.addView(connectBtn, lp(32));

        TextView status = new TextView(this);
        status.setTextSize(14f);
        root.addView(status, lp(24));

        TextView note = new TextView(this);
        note.setText("Note: this plugin depends on AndrOBD GPS plugin");
        note.setTextSize(13f);
        note.setTextColor(getResources().getColor(R.color.note_text));
        root.addView(note, lp(8));

        list.addHeaderView(scroll);
        setContentView(list);

        OkHttpClient http = new OkHttpClient();

        connectBtn.setOnClickListener(v -> {
            status.setText("Checking server…");
            status.setTextColor(getResources().getColor(R.color.status_error));

            String host = this.hostEdit.getText().toString().trim();
            String port = this.portEdit.getText().toString().trim();
            String device = this.deviceEdit.getText().toString().trim();

            if (host.isEmpty() || port.isEmpty() || device.isEmpty()) {
                status.setText("Invalid configuration");
                status.setTextColor(getResources().getColor(R.color.status_error));
                return;
            }

            String url = "http://" + host + ":" + port + "/?id=" + device;

            new Thread(() -> {
                Log.d("SettingsActivity", "Trying URL: " + url);
                try {
                    Request req = new Request.Builder()
                            .url(url)
                            .get()
                            .build();

                    Response resp = http.newCall(req).execute();
                    int code = resp.code();
                    resp.close();

                    runOnUiThread(() -> {
                        Log.d("SettingsActivity", "Server returned" + code);
                        if (code == 200) {
                            status.setText("Server reachable, device exists");
                            status.setTextColor(getResources().getColor(R.color.status_ok));
                        } else if (code == 400) {
                            status.setText("Server reachable, device not found");
                            status.setTextColor(getResources().getColor(R.color.status_warning));
                        } else {
                            status.setText("Server reachable, unexpected response: " + code);
                            status.setTextColor(getResources().getColor(R.color.status_error));
                        }
                    });

                } catch (Exception e) {
                    Log.e("SettingsActivity", "connection failed", e);
                    runOnUiThread(() -> {
                        status.setText("Server not reachable");
                        status.setTextColor(getResources().getColor(R.color.status_error));
                    });
                }
            }).start();
        });
    }

    private void saveSettings() {
        String host = this.hostEdit.getText().toString().trim();
        String portStr = this.portEdit.getText().toString().trim();
        String device = this.deviceEdit.getText().toString().trim();

        if (host.isEmpty() || portStr.isEmpty() || device.isEmpty()) return;

        try {
            int port = Integer.parseInt(portStr);
            SettingsStore.set(this, host, port, device);
        } catch (NumberFormatException ignored) {
        }
    }
}