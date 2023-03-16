package com.fazziclay.openjavalauncher;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.openjavalauncher.datafixer.DataFixer;
import com.fazziclay.openjavalauncher.gui.LauncherWindow;
import com.fazziclay.openjavalauncher.launcher.ConfigurationManager;
import com.fazziclay.openjavalauncher.launcher.GameProfile;
import com.fazziclay.openjavalauncher.launcher.UserProfile;
import com.fazziclay.openjavalauncher.launcher.VersionManifest;
import com.fazziclay.openjavalauncher.operation.MinecraftLaunchOperation;
import com.fazziclay.openjavalauncher.operation.Operation;
import com.fazziclay.openjavalauncher.operation.UpdateVersionManifestOperation;
import com.fazziclay.openjavalauncher.util.Lang;
import com.fazziclay.openjavalauncher.util.Logger;
import com.fazziclay.openjavalauncher.util.NetworkUtil;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.fazziclay.openjavalauncher.util.Lang.t;

public class OpenJavaLauncher {
    private static final String VERSION_MANIFEST_V2_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String TAG = "OpenJavaLauncher";
    private static final String DEFAULT_JVM_ARGUMENTS = "-Xmx2048M -Xms2048M -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:+ParallelRefProcEnabled -Dfile.encoding=UTF-8";
    private static volatile OpenJavaLauncher instance;

    public static void main(String[] args) {
        final String MAIN_TAG = "main";

        Logger.d(MAIN_TAG, "Hello!");
        Logger.d(MAIN_TAG, "OpenJavaLauncher created by: FazziCLAY");
        instance = new OpenJavaLauncher(args);
        int exit = instance.run();
        Logger.d(MAIN_TAG, "OpenJavaLauncher exit with " + exit + " code!");
        Logger.d(MAIN_TAG, "Good Bye!");
        System.exit(exit);
    }

    public static OpenJavaLauncher getInstance() {
        return instance;
    }


    private final LauncherWindow window;
    private final File launcherDir;
    private final File versionsDir;
    private final File librariesDir;
    private VersionManifest versionManifest = null;
    private ConfigurationManager configurationManager;
    private final List<Operation> operations = new ArrayList<>();

    public OpenJavaLauncher(String[] args) {
        if (args.length > 0) {
            String dir = args[0];
            this.launcherDir = new File(dir);
        } else {
            this.launcherDir = new File("openjavalauncher");
        }
        DataFixer dataFixer = new DataFixer(launcherDir);
        if (dataFixer.isFixNeed()) {
            dataFixer.tryFix();
        }

        this.configurationManager = new ConfigurationManager(launcherDir, new File(this.launcherDir, "openjavalauncher.json"));
        try {
            Lang.setLanguage(configurationManager.getLanguage());
        } catch (Exception e) {
            Logger.d(TAG, "Unknown " + configurationManager.getLanguage() + " language. Fixing...");
            configurationManager.setLanguage(Lang.DEFAULT_LANGUAGE);
        }

        this.window = new LauncherWindow(configurationManager.getWindowStartWidth(), configurationManager.getWindowStartHeight());
        this.versionsDir = new File(launcherDir, "versions");
        this.librariesDir = new File(launcherDir, "libraries");
    }

    private int run() {
        SwingUtilities.invokeLater(() -> this.window.run(new WindowListener()));
        setupVersionManifest();

        while (window.isExists()) {
            tick();
            try {
                Thread.sleep(1000 / 10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return 0;
    }

    private void tick() {
        SwingUtilities.invokeLater(() -> {
            window.updateOperations(operations);
            window.tick();
        });
    }

    private void setupVersionManifest() {
        Thread thread = new Thread(() -> {
            final String THREAD_TAG = "SetupVersionManifestThread";
            UpdateVersionManifestOperation operation = new UpdateVersionManifestOperation();
            addOperation(operation);

            try {
                operation.setState(t("operation.updateVersionManifest.state.loadingLocalCache"));
                final File localManifestCache = new File(versionsDir, "version_manifest_v2.json");
                if (FileUtil.isExist(localManifestCache)) {
                    versionManifest = new VersionManifest(new JSONObject(FileUtil.getText(localManifestCache, "{}")), false);
                    updateVersionManifest(versionManifest);
                }

                operation.setState(t("operation.updateVersionManifest.state.downloading"));
                String parsed = NetworkUtil.parseTextPage(VERSION_MANIFEST_V2_URL);
                versionManifest = new VersionManifest(new JSONObject(parsed), true);
                updateVersionManifest(versionManifest);
                operation.setState(t("operation.updateVersionManifest.state.savingToCache"));
                FileUtil.setText(localManifestCache, parsed);

            } catch (Exception e) {
                Logger.e(THREAD_TAG, "Exception", e);
                updateVersionManifest(versionManifest);
            }

            operation.setState(t("operation.updateVersionManifest.state.finished"));
            removeOperation(operation);
        });
        thread.setName("SetupVersionManifestThread");
        thread.start();
    }

    private void updateVersionManifest(VersionManifest manifest) {
        SwingUtilities.invokeLater(() -> window.updateVersionManifest(manifest));
    }

    public VersionManifest getVersionManifest() {
        return versionManifest;
    }

    private void startMinecraft(final GameProfile gameProfile, final UserProfile userProfile) {
        Thread thread = new Thread(() -> {
            final MinecraftLaunchOperation operation = new MinecraftLaunchOperation(gameProfile, userProfile);
            addOperation(operation);
            operation.run(versionsDir, librariesDir);
            removeOperation(operation);
        });
        thread.setName("MinecraftThread");
        thread.start();
    }

    private void addOperation(Operation operation) {
        this.operations.add(operation);
    }

    private void removeOperation(Operation operation) {
        this.operations.remove(operation);
    }

    public class WindowListener {

        public void fileOpenClicked() {
        }

        public void addFakeOperationClicked() {
            addOperation(new Operation() {
                String id = new Random().nextInt()+"";
                boolean can = new Random().nextBoolean();
                @Override
                public String getTitle() {
                    return "Fake operation";
                }

                @Override
                public String getDescription() {
                    return id;
                }

                @Override
                public void cancel() {
                    removeOperation(this);
                }

                @Override
                public boolean isCancelable() {
                    return can;
                }
            });
        }

        public void clearOperations() {
            operations.clear();
        }

        public void updateVersionManifest() {
            setupVersionManifest();
        }

        public String getCurrentUserProfile() {
            try {
                return configurationManager.getSelectedUserProfile().getNickname();
            } catch (Exception e) {
                return "Unknown";
            }
        }

        public String getCurrentGameProfile() {
            try {
                return configurationManager.getSelectedGameProfile().getVersionId();
            } catch (Exception e) {
                return "Unknown";
            }
        }

        public void startClicked() {
            startMinecraft(configurationManager.getSelectedGameProfile(), configurationManager.getSelectedUserProfile());
        }

        public void addUserProfileClicked() {
            configurationManager.getUserProfileList().add(new UserProfile(UUID.randomUUID(), "Nickname", UUID.randomUUID().toString().replace("-", ""), false));
            configurationManager.save();
        }

        public void addGameProfileClicked() {
            configurationManager.getGameProfileList().add(new GameProfile(UUID.randomUUID(), versionManifest.getLatest().getRelease(), DEFAULT_JVM_ARGUMENTS, new File(launcherDir, "gameDirectory"), 0, 0));
            configurationManager.save();
        }
    }
}
