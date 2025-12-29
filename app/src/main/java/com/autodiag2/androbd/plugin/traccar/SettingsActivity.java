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

public class SettingsActivity extends PreferenceActivity {

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

        EditText hostEdit = new EditText(this);
        hostEdit.setHint("ip / hostname");
        hostEdit.setText("172.20.10.4");
        hostEdit.setFocusable(true);
        hostEdit.setFocusableInTouchMode(true);
        hostEdit.setClickable(true);
        hostEdit.clearFocus();
        root.addView(hostEdit, lp(32));

        EditText portEdit = new EditText(this);
        portEdit.setHint("port");
        portEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        portEdit.setText("5055");
        portEdit.setFocusable(true);
        portEdit.setFocusableInTouchMode(true);
        portEdit.setClickable(true);
        portEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.post(v::requestFocus);
            }
        });
        root.addView(portEdit, lp(16));

        EditText deviceEdit = new EditText(this);
        deviceEdit.setHint("my device");
        deviceEdit.setText("some");
        deviceEdit.setFocusable(true);
        deviceEdit.setFocusableInTouchMode(true);
        deviceEdit.setClickable(true);
        deviceEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.post(v::requestFocus);
            }
        });
        root.addView(deviceEdit, lp(16));

        Button connectBtn = new Button(this);
        connectBtn.setText("Connect");
        root.addView(connectBtn, lp(32));

        TextView status = new TextView(this);
        status.setTextSize(14f);
        root.addView(status, lp(24));

        list.addHeaderView(scroll);
        setContentView(list);

        OkHttpClient http = new OkHttpClient();

        connectBtn.setOnClickListener(v -> {
            status.setText("Checking server…");
            status.setTextColor(getResources().getColor(R.color.status_error));

            String host = hostEdit.getText().toString().trim();
            String port = portEdit.getText().toString().trim();
            String device = deviceEdit.getText().toString().trim();

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
}