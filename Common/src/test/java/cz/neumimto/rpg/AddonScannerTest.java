package cz.neumimto.rpg;

import cz.neumimto.rpg.api.logging.Log;
import cz.neumimto.rpg.common.AddonScanner;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class AddonScannerTest {

    @Test
    public void test() {
        Log.setLogger(LoggerFactory.getLogger(AddonScannerTest.class));
        AddonScanner.setDeployedDir(Paths.get("L:\\mc\\NT-RPG\\Peristence\\Database\\build\\libss\\"));
        AddonScanner.setAddonDir(Paths.get("L:\\mc\\NT-RPG\\Peristence\\Database\\build\\libs\\"));
        AddonScanner.prepareAddons();
    }
}