package dev.customclaims.combatlog.platform;

public final class PlatformHelperHolder {
    private static IPlatformHelper platformHelper;

    private PlatformHelperHolder() {
    }

    public static void setPlatformHelper(IPlatformHelper helper) {
        platformHelper = helper;
    }

    public static IPlatformHelper get() {
        if (platformHelper == null) {
            throw new IllegalStateException("PlatformHelper has not been initialized!");
        }
        return platformHelper;
    }
}
