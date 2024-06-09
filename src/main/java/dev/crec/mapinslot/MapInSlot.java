package dev.crec.mapinslot;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapInSlot {

    private static @Nullable Logger LOGGER = null;

    public static Logger getLogger() {
        if(LOGGER == null)
            LOGGER = LoggerFactory.getLogger("mapinslot");
        return LOGGER;
    }

}
