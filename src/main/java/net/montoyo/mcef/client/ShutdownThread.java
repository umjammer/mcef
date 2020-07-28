package net.montoyo.mcef.client;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.client.MinecraftClient;
import net.montoyo.mcef.MCEF;
import net.montoyo.mcef.utilities.Log;

public class ShutdownThread extends Thread {

    private Field running = null;
    private MinecraftClient mc = MinecraftClient.getInstance();

    public ShutdownThread() {
        super("MCEF-Shutdown");
        setDaemon(false);

        try {
            Field[] fields = MinecraftClient.class.getDeclaredFields();

            for(Field f: fields) {
                if(f.getType().equals(Boolean.TYPE)) {
                    //Log.info("Minecraft.%s: %s", f.getName(), Modifier.toString(f.getModifiers()));

                    if(f.getModifiers() == Modifier.VOLATILE) {
                        f.setAccessible(true);
                        running = f;
                        Log.info("volatile boolean Minecraft.running => %s", f.getName());
                        break;
                    }
                }
            }
        } catch(Throwable t) {
            Log.warning("Can't detect Minecraft shutdown:");
            t.printStackTrace();
        }
    }

    @Override
    public void run() {
        if(running == null)
            return;

        Log.info("Minecraft shutdown detection thread started.");

        while(true) {
            try {
                if(!running.getBoolean(mc))
                    break;
            } catch(Throwable t) {
                Log.warning("Can't detect Minecraft shutdown:");
                t.printStackTrace();
                return;
            }

            try {
                sleep(100);
            } catch(Throwable t) {}
        }

        MCEF.PROXY.onShutdown();
    }
}
