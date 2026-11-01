package dev.customclaims.combatlog.fabric;

import dev.customclaims.combatlog.platform.IPlatformHelper;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
