package com.fazziclay.openjavalauncher;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.openjavalauncher.util.NetworkUtil;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class OpenJavaLauncher {
    private static final String VERSION_MANIFEST_V2_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    public static void main(String[] args) {
        System.out.println("[main] Hello!");
        System.out.println("[main] OpenJavaLauncher created by: FazziCLAY");
        int exit = new OpenJavaLauncher().run();
        System.out.println("[main] OpenJavaLauncher exit with " + exit + " code!");
        System.out.println("[main] Good Bye!");
    }


    private final LauncherWindow window;
    private final File launcherDir;

    public OpenJavaLauncher() {
        this.window = new LauncherWindow(500, 700);
        this.launcherDir = new File("openjavalauncher");
    }

    private void update(boolean isFresh, JSONObject manifest) {
        JSONObject latest = manifest.getJSONObject("latest");
        String release = latest.getString("release");
        String snapshot = latest.getString("snapshot");

        window.setLatest(isFresh, release, snapshot);
    }

    private int run() {
        this.window.run();

        try {
            File localManifestCache = new File(launcherDir, "version_manifest_v2.json");
            if (FileUtil.isExist(localManifestCache)) {
                update(false, new JSONObject(FileUtil.getText(localManifestCache, "{}")));
            }

            String parsed = NetworkUtil.parseTextPage(VERSION_MANIFEST_V2_URL);
            update(true, new JSONObject(parsed));
            FileUtil.setText(localManifestCache, parsed);

        } catch (Exception e) {
            e.printStackTrace();
        }

        while (window.isExists()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return 0;
    }
}
