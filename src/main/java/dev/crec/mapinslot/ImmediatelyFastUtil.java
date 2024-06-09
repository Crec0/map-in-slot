package dev.crec.mapinslot;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ImmediatelyFastUtil {
    private static @Nullable Boolean cachedIsModAvailable;

    public static class ImmediatelyFastBatchingAccessWrapper {
        private final Method beginHudBatchingMethod, endHudBatchingMethod, isHudBatching;
        private final Object batchingAccessInstance;

        public ImmediatelyFastBatchingAccessWrapper(Object batchingAccessInstance) throws ClassNotFoundException, NoSuchMethodException {
            this.batchingAccessInstance = batchingAccessInstance;
            if(batchingAccessInstance == null)
                throw new IllegalArgumentException("ImmediatelyFastBatchingAccessWrapper: Instance is null!");
            this.beginHudBatchingMethod = getBatchingAccessClass().getDeclaredMethod("beginHudBatching");
            this.endHudBatchingMethod = getBatchingAccessClass().getDeclaredMethod("endHudBatching");
            this.isHudBatching = getBatchingAccessClass().getDeclaredMethod("isHudBatching");
        }

        public void beginHudBatching() throws InvocationTargetException, IllegalAccessException {
            beginHudBatchingMethod.invoke(batchingAccessInstance);
        }

        public void endHudBatching() throws InvocationTargetException, IllegalAccessException {
            endHudBatchingMethod.invoke(batchingAccessInstance);
        }

        public boolean isHudBatching() throws InvocationTargetException, IllegalAccessException {
            return (boolean) isHudBatching.invoke(batchingAccessInstance);
        }
    }

    public static boolean isModAvailable() {
        if(cachedIsModAvailable == null) {
            cachedIsModAvailable = FabricLoader.getInstance().isModLoaded("immediatelyfast");
            if(cachedIsModAvailable)
                MapInSlot.getLogger().info("Detected the Mod \"Immediately Fast\". Will use workaround to prevent render glitches in hotbar.");
        }
        return cachedIsModAvailable;
    }

    private static Class<?> getApiClass() throws ClassNotFoundException {
        return Class.forName("net.raphimc.immediatelyfastapi.ImmediatelyFastApi");
    }

    private static Class<?> getApiAccessClass() throws ClassNotFoundException {
        return Class.forName("net.raphimc.immediatelyfastapi.ApiAccess");
    }

    private static Class<?> getBatchingAccessClass() throws ClassNotFoundException {
        return Class.forName("net.raphimc.immediatelyfastapi.BatchingAccess");
    }

    public static Object getApiAccessInstance() {
        try {
            Method getApiImplMethod = getApiClass().getDeclaredMethod("getApiImpl");
            return getApiImplMethod.invoke(null);
        }catch (Exception ex) {
            MapInSlot.getLogger().error("Failed to get ApiAccess instance.", ex);
            return null;
        }
    }

    public static Object getBatchingAccessInstance(Object apiAccessInstance) {
        try {
            Method getBatchingMethod = getApiAccessClass().getDeclaredMethod("getBatching");
            return getBatchingMethod.invoke(apiAccessInstance);
        }catch (Exception ex) {
            MapInSlot.getLogger().error("Failed to get ApiAccess instance.", ex);
            return null;
        }
    }

    public static @Nullable ImmediatelyFastBatchingAccessWrapper getBatchingAccessWrapper() {
        try {
            Object batchingAccessInstance = getBatchingAccessInstance(getApiAccessInstance());
            return new ImmediatelyFastBatchingAccessWrapper(batchingAccessInstance);
        }catch (Exception ex) {
            MapInSlot.getLogger().error("Failed to get ApiAccess instance.", ex);
            return null;
        }
    }

}
