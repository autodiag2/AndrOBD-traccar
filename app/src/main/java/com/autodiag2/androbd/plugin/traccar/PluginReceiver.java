package com.autodiag2.androbd.plugin.traccar;

public class PluginReceiver
    extends com.fr3ts0n.androbd.plugin.PluginReceiver
{
    /**
     * Get class of plugin implementation
     *
     * @return Plugin implementation class
     */
    @Override
    public Class getPluginClass()
    {
        return TraccarIntegration.class;
    }
}
