package com.autodiag2.androbd.plugin.traccar;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.fr3ts0n.androbd.plugin.Plugin;
import com.fr3ts0n.androbd.plugin.PluginInfo;

public class TraccarIntegration
		extends Plugin
		implements
		Plugin.DataProvider,
		Plugin.ConfigurationHandler,
		Plugin.ActionHandler
{
    static final PluginInfo myInfo = new PluginInfo("Traccar",
		TraccarIntegration.class,
		"AndrOBD Traccar integration",
		"Copyright (C) 2025 by autodiag2",
		"GPLV3+",
		"https://github.com/autodiag2/AndrOBD-traccar"
	);

    @Override
	public void onCreate() {
		super.onCreate();
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

}