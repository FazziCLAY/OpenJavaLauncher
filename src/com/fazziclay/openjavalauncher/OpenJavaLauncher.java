package com.fazziclay.openjavalauncher;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.openjavalauncher.datafixer.DataFixer;
import com.fazziclay.openjavalauncher.operation.MinecraftLaunchOperation;
import com.fazziclay.openjavalauncher.operation.Operation;
import com.fazziclay.openjavalauncher.operation.UpdateVersionManifestOperation;
import com.fazziclay.openjavalauncher.util.Lang;
import com.fazziclay.openjavalauncher.util.Logger;
import com.fazziclay.openjavalauncher.util.NetworkUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.fazziclay.openjavalauncher.util.Lang.t;

public class OpenJavaLauncher {
    private static final String VERSION_MANIFEST_V2_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String TAG = "OpenJavaLauncher";

    public static void main(String[] args) {
        final String MAIN_TAG = "main";

        Logger.d(MAIN_TAG, "Hello!");
        Logger.d(MAIN_TAG, "OpenJavaLauncher created by: FazziCLAY");
        final OpenJavaLauncher launcher = new OpenJavaLauncher(args);
        int exit = launcher.run();
        Logger.d(MAIN_TAG, "OpenJavaLauncher exit with " + exit + " code!");
        Logger.d(MAIN_TAG, "Good Bye!");
        System.exit(exit);
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

    private void startMinecraft(final GameProfile gameProfile, final UserProfile userProfile) {
        Thread thread = new Thread(() -> {
            final VersionManifest.Version version = versionManifest.getVersionById(gameProfile.getId());
            if (version == null) throw new RuntimeException("Version '"+gameProfile.getId()+"' not found");
            final MinecraftLaunchOperation operation = new MinecraftLaunchOperation(version);
            addOperation(operation);
            try {
                operation.setState(t("operation.minecraftLaunch.state.prepare"));
                File versionDir = new File(versionsDir, version.getId());
                File versionJsonFile = new File(versionDir, version.getId() + ".json");
                File versionJar = new File(versionDir, version.getId() + ".jar");

                JSONObject versionJson;
                if (FileUtil.isExist(versionJsonFile)) {
                    versionJson = new JSONObject(FileUtil.getText(versionJsonFile));
                } else {
                    operation.setState(t("operation.minecraftLaunch.state.downloadingVersionInfo"));

                    String parsed = NetworkUtil.parseTextPage(version.getUrl());
                    FileUtil.setText(versionJsonFile, parsed);
                    versionJson = new JSONObject(parsed);
                }

                if (!FileUtil.isExist(versionJar)) {
                    operation.setState(t("operation.minecraftLaunch.state.downloadingVersionJar"));

                    NetworkUtil.downloadFile(versionJar, versionJson.getJSONObject("downloads").getJSONObject("client").getString("url"));
                }

                List<File> resultLibs = new ArrayList<>();
                JSONArray libs = versionJson.getJSONArray("libraries");
                int i = 0;
                while (i < libs.length()) {
                    JSONObject lib = libs.getJSONObject(i);
                    String libName = lib.getString("name");
                    System.out.println("Lib " + libName);
                    JSONArray rules = lib.optJSONArray("rules");
                    JSONObject downloads = lib.getJSONObject("downloads");
                    JSONObject dArtefact;
                    try {
                        dArtefact = downloads.getJSONObject("artifact");
                    } catch (Exception e) {
                        Logger.e("w", lib.toString(2), e);
                        throw new RuntimeException(e);
                    }
                    File path = new File(librariesDir, dArtefact.getString("path"));
                    if (!FileUtil.isExist(path)) {
                        operation.setState(t("operation.minecraftLaunch.state.downloadingLibrary", libName));

                        NetworkUtil.downloadFile(path, dArtefact.getString("url"));
                    }
                    resultLibs.add(path);
                    i++;
                }


                resultLibs.add(versionJar);

                String jvm = "-XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:+ParallelRefProcEnabled -Xmx2048M -Xms2048M -Dfile.encoding=UTF-8 -Xss1M -Dminecraft.launcher.brand=java-minecraft-launcher -Dminecraft.launcher.version=1.6.84-j";
                String classpath = "";
                for (File resultLib : resultLibs) {
                    classpath = classpath + resultLib.getAbsolutePath() + ":";
                }
                classpath = classpath.substring(0, classpath.length() - 1);
                String mainClass = versionJson.getString("mainClass");
                String gameArguments = "--username fazzitl --version OptiFine 1.19.2 --gameDir /home/l/Minecraft --assetsDir /home/l/Minecraft/assets --assetIndex 1.19 --uuid 750494a191733fd79336798470bf2dd8 --accessToken 750494a191733fd79336798470bf2dd8 --clientId  --xuid  --userType legacy --versionType modified --width 925 --height 530 --server localhost --port 25565";


                List<String> list = new ArrayList<>();
                list.add("/usr/lib/jvm/java-17-openjdk-amd64/bin/java");
                list.addAll(Arrays.asList(jvm.split(" ")));
                list.add("-cp");
                list.add(classpath);
                list.add(mainClass);
                list.addAll(Arrays.asList(gameArguments.split(" ")));

                List<String> temp = new ArrayList<>();
                temp.add("java");
                temp.add("-cp");
                temp.add(versionJar.getAbsolutePath());
                temp.add("net.minecraft.client.main.Main");

                System.out.println("ARGS " + list);

                operation.setState("Launching!");
                ProcessBuilder processBuilder = new ProcessBuilder(list);

                Process process = processBuilder.start();
                operation.setProcess(process);
                operation.setState(t("operation.minecraftLaunch.state.launched", process.pid()));
                int exitCode = process.waitFor();

                operation.setState(t("operation.minecraftLaunch.state.finished", version.getId(), exitCode));
                removeOperation(operation);
            } catch (Exception e) {
                Logger.e("MinecraftThread", "Exception", e);
            }
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
            JOptionPane.showConfirmDialog(null, "File");
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
            return configurationManager.getSelectedUserProfile().getNickname();
        }

        public String getCurrentGameProfile() {
            return configurationManager.getSelectedGameProfile().getId();
        }

        public void startClicked() {
            startMinecraft(configurationManager.getSelectedGameProfile(), configurationManager.getSelectedUserProfile());
        }
    }
}
