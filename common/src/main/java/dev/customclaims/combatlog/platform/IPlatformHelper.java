package dev.customclaims.combatlog.platform;

import java.nio.file.Path;

public interface IPlatformHelper {
    String getPlatformName();
    boolean isModLoaded(String modId);
    Path getConfigDirectory();
}
