package com.autodiag2.androbd.plugin.traccar;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.os.Bundle;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.fr3ts0n.androbd.plugin.Plugin;
import com.fr3ts0n.androbd.plugin.PluginInfo;

public class TraccarIntegration
		extends Plugin
		implements
		Plugin.DataProvider,
		Plugin.ConfigurationHandler,
		Plugin.ActionHandler,
		Plugin.DataReceiver
{
    static final PluginInfo myInfo = new PluginInfo("Traccar",
		TraccarIntegration.class,
		"AndrOBD Traccar integration",
		"Copyright (C) 2025 by autodiag2",
		"GPLV3+",
		"https://github.com/autodiag2/AndrOBD-traccar"
	);

	private OkHttpClient http;

	private Double lat = null;
	private Double lon = null;
	private Float speed = null;
	private Float bearing = null;

    @Override
	public void onCreate() {
		super.onCreate();
		http = new OkHttpClient();
    }

    @Override
	public void onDestroy() {
		super.onDestroy();
	}

    @Override
	public PluginInfo getPluginInfo() {
		return myInfo;
	}

	@Override
	public void performConfigure() {
		Intent cfgIntent = new Intent(getApplicationContext(), SettingsActivity.class);
		cfgIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(cfgIntent);
	}

    @Override
	public void performAction() {

    }

	@Override
	public void onDataUpdate(String key, String value) {

		try {
			if ("GPS_LATITUDE".equals(key)) {
				lat = Double.parseDouble(value);

			} else if ("GPS_LONGITUDE".equals(key)) {
				lon = Double.parseDouble(value);

			} else if ("GPS_SPEED".equals(key)) {
				speed = Float.parseFloat(value);

			} else if ("GPS_BEARING".equals(key)) {
				bearing = Float.parseFloat(value);
			}

		} catch (NumberFormatException e) {
			return;
		}

		if (lat != null && lon != null) {
			sendToTraccar(
					lat,
					lon,
					speed,
					bearing
			);
		}
	}

	@Override
	public void onDataListUpdate(String csvString) {

		String[] lines = csvString.split("\n");

		for (String line : lines) {
			String[] cols = line.split(";");
			if (cols.length < 3) continue;

			String key = cols[0];
			String val = cols[2];

			try {
				switch (key) {
					case "GPS_LATITUDE":
						lat = Double.parseDouble(val);
						break;
					case "GPS_LONGITUDE":
						lon = Double.parseDouble(val);
						break;
					case "GPS_SPEED":
						speed = Float.parseFloat(val);
						break;
					case "GPS_BEARING":
						bearing = Float.parseFloat(val);
						break;
				}
			} catch (NumberFormatException e) {
				continue;
			}
		}

		if (lat != null && lon != null) {
			sendToTraccar(
					lat,
					lon,
					speed,
					bearing
			);
		}
	}

	private void sendToTraccar(double lat, double lon, Float speed, Float bearing) {

		if ( speed == null ) {
			speed = 0f;
		}
		if ( bearing == null ) {
			bearing = 0f;
		}

        String host = SettingsStore.getHost(getApplicationContext());
        int port = SettingsStore.getPort(getApplicationContext());
        String deviceId = SettingsStore.getDeviceId(getApplicationContext());

        if (host == null || deviceId == null) return;

        String url =
                "http://" + host + ":" + port +
                "/?id=" + deviceId +
                "&lat=" + lat +
                "&lon=" + lon +
                "&speed=" + speed +
                "&bearing=" + bearing;

        new Thread(() -> {
			Log.d("TraccarIntegration", "Sending to: " + url);
            try {
                Request req = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                Response resp = http.newCall(req).execute();
                resp.close();

                Log.d("TraccarIntegration", "Position sent to Traccar");

            } catch (Exception e) {
                Log.e("TraccarIntegration", "Failed to send to Traccar", e);
            }
        }).start();
    }
}