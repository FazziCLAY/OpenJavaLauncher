package com.fazziclay.openjavalauncher.launcher.launch;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.openjavalauncher.OpenJavaLauncher;
import com.fazziclay.openjavalauncher.launcher.GameProfile;
import com.fazziclay.openjavalauncher.launcher.UserProfile;
import com.fazziclay.openjavalauncher.launcher.VersionManifest;
import com.fazziclay.openjavalauncher.util.CryptoUtil;
import com.fazziclay.openjavalauncher.util.Logger;
import com.fazziclay.openjavalauncher.util.NetworkUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class MinecraftInstance {
    private static final String TAG = "MinecraftInstance";
    private final GameProfile gameProfile;
    private final UserProfile userProfile;
    private final VersionManifest versionManifest;
    private final File versionsDir;
    private final File librariesDir;
    private final File assetsDir;
    private State state = State.PREPARING;
    private Process process;
    private List<LogRecord> logs = new ArrayList<>();
    private Runnable logsChangedListener;


    public MinecraftInstance(GameProfile gameProfile, UserProfile userProfile, VersionManifest versionManifest, File versionsDir, File librariesDir, File assetsDir) {
        this.gameProfile = gameProfile;
        this.userProfile = userProfile;
        this.versionManifest = versionManifest;
        this.versionsDir = versionsDir;
        this.librariesDir = librariesDir;
        this.assetsDir = assetsDir;
    }

    public void setLogsChangedListener(Runnable logsChangedListener) {
        this.logsChangedListener = logsChangedListener;
    }

    public LogRecord getLastLog() {
        if (logs.isEmpty()) return new LogRecord().l("No logs...");
        return logs.get(logs.size()-1);
    }

    enum State {
        PREPARING,
        LIBRARIES,
        ASSETS,
        LAUNCH;

        int progress;
        int progressMax;
    }

    public void run() {
        try {
            state = State.PREPARING;
            final String versionId = gameProfile.getVersionId(); // E.g. 1.12, 1.19, 21w40a, 1.19.2-rc2, 1.19.1-pre1
            final VersionManifest.Version manifestVersion = OpenJavaLauncher.getInstance().getVersionManifest().getVersionById(versionId);
            final boolean isVersionOfficial = manifestVersion != null; // is gameProfile.id contains in version_manifest_v2.json
            final File versionDir = new File(versionsDir, versionId); // .../.../.../21w40a/
            final File versionInfoFile = new File(versionDir, String.format("%s.json", versionId)); // ...../21w40a/21w40a.json
            final File versionClientFile = new File(versionDir, String.format("%s.jar", versionId)); // ..../1.19/1.19.jar

            if (!isVersionOfficial && (!FileUtil.isExist(versionInfoFile) || !FileUtil.isExist(versionClientFile))) {
                throw new RuntimeException("Unofficial version missing files!");
            }

            if (isVersionOfficial) upgradeFile(versionInfoFile, manifestVersion.getSha1(), manifestVersion.getUrl()); // (1.19 for example) try upgrade 1.19.json
            final VersionInfo versionInfo = parseVersionInfo(new JSONObject(FileUtil.getText(versionInfoFile))); // parse 1.19.json
            upgradeFile(versionClientFile, versionInfo.downloads.client.sha1, versionInfo.downloads.client.url); // try upgrade 1.19.jar

            // Libraries
            state = State.LIBRARIES;
            List<File> libraries = loadLibraries(versionInfo.libraries);
            libraries.add(versionClientFile); // add client.jar to libraries (for classpath)

            // Assets
            state = State.ASSETS;
            loadAssets(versionInfo.assets, versionInfo.assetsIndexInfo);

            // Launch
            state = State.LAUNCH;
            final List<String> command = new ArrayList<>();
            command.add(gameProfile.getJVMPath()); // 0 argument is java command or path to java
            command.addAll(loadJVMArguments(versionInfo.jvmArguments, gameProfile.getJVMArguments(), makeClassPath(libraries))); // add arguments
            command.add(versionInfo.mainClass); // add mainClass
            command.addAll(loadGameArguments(versionInfo.minecraftArguments, versionInfo)); // add game arguments e.g. `--username Notch`

            Logger.d(TAG, "Launch command:");
            for (String s : command) {
                Logger.d(TAG, s);
            }

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            process = processBuilder.start();
            Logger.d(TAG, "Process created! PID="+process.pid());
            Console console = new Console(process);
            console.start();
            int exitCode = process.waitFor(); // <----------------------------------- BUSY method!
            Logger.d(TAG, "Process finished with exit code "+exitCode+"! PID="+process.pid());

        } catch (Exception e) {
            Logger.e("MinecraftThread", "Exception", e);
        }
    }

    private String makeClassPath(List<File> libraries) {
        StringBuilder classpath = new StringBuilder();
        for (File lib : libraries) {
            classpath.append(lib.getAbsolutePath()).append(":");
        }
        return classpath.substring(0, classpath.length() - 1); // remove latest char (100% is ':')
    }

    private List<String> loadGameArguments(JSONArray gameArguments, VersionInfo versionInfo) {
        final List<String> args = new ArrayList<>();

        final JSONObject rulesFeatures = new JSONObject()
                .put("is_demo_user", userProfile.isDemo())
                .put("has_custom_resolution", gameProfile.getWindowWidth() > 0 && gameProfile.getWindowHeight() > 0);

        List<String> a = parseArguments(gameArguments, rulesFeatures);
        for (String s : a) {
            args.add(s.replace("${auth_player_name}", userProfile.getNickname())
                    .replace("${version_name}", versionInfo.id)
                    .replace("${game_directory}", gameProfile.getGameDirectory().getAbsolutePath())
                    .replace("${assets_root}", assetsDir.getAbsolutePath())
                    .replace("${assets_index_name}", versionInfo.assets)
                    .replace("${auth_uuid}", userProfile.getUuid())
                    .replace("${auth_access_token}", "")
                    .replace("${clientid}", "")
                    .replace("${auth_xuid}", "")
                    .replace("${user_type}", "legacy")
                    .replace("${version_type}", versionInfo.type)
                    .replace("${resolution_width}", "" + gameProfile.getWindowWidth())
                    .replace("${resolution_height}", "" + gameProfile.getWindowHeight()));
        }
        return args;
    }

    private List<String> loadJVMArguments(JSONArray jvmArguments, String userJvm, String classpath) {
        final List<String> args = new ArrayList<>(Arrays.asList(userJvm.split(" ")));
        if (jvmArguments != null) {
            List<String> a = parseArguments(jvmArguments, null);
            for (String s : a) {
                args.add(s.replace("${classpath}", classpath)
                        .replace("${natives_directory}", gameProfile.getGameDirectory().getAbsolutePath()) // TODO: 3/16/23 what?
                        .replace("${launcher_name}", "OpenJavaLauncher")
                        .replace("${launcher_version}", OpenJavaLauncher.VERSION_NAME));
            }
        }
        return args;
    }

    private List<String> parseArguments(JSONArray jArguments, JSONObject rulesFeatures) {
        final List<String> args = new ArrayList<>();

        for (Object jArgument : jArguments) {
            List<String> value = new ArrayList<>();

            if (jArgument instanceof String) value.add((String) jArgument);
            if (jArgument instanceof JSONObject) {
                JSONObject jarg = (JSONObject) jArgument;
                JSONArray rules = jarg.getJSONArray("rules");
                if (!RulesParser.isRuleAllow(rules, rulesFeatures)) continue;

                Object jValue = jarg.get("value");
                if (jValue instanceof String) {
                    value.add((String) jValue);
                } else if (jValue instanceof JSONArray) {
                    for (Object o : ((JSONArray) jValue)) {
                        value.add(o.toString());
                    }
                }
            }

            args.addAll(value);
        }
        return args;
    }

    private void loadAssets(String assetsId, JSONObject assetsIndexInfo) throws Exception {
        String assetsIndexUrl = assetsIndexInfo.getString("url");
        String assetsIndexSha1 = assetsIndexInfo.getString("sha1");
        int totalSize = assetsIndexInfo.getInt("totalSize");
        File localIndexes = new File(assetsDir, "indexes");
        File localObjects = new File(assetsDir, "objects");
        File assetsIndexFile = new File(localIndexes, String.format("%s.json", assetsId));

        upgradeFile(assetsIndexFile, assetsIndexSha1, assetsIndexUrl); // try repair `assets/indexes/1.19.json` file
        // if gameProfile allow check missing files
        if (gameProfile.isDownloadMissingAssets()) {
            final LogRecord l = log();
            JSONObject assetsIndex = new JSONObject(FileUtil.getText(assetsIndexFile));
            JSONObject assetsIndexObjects = assetsIndex.getJSONObject("objects");
            int i = 0;
            int max = assetsIndexObjects.length();
            long size = 0;
            for (String assetPath : assetsIndexObjects.keySet()) {
                // assetPath e.g. `icons/icon_16x16.png`
                short p = (short) (((float) size / (float) totalSize) * 100f);
                l.p(p).l("[Assets] " + assetPath);
                Logger.d(TAG, "["+p+"%] [Assets] Checking " + assetPath);


                JSONObject object = assetsIndexObjects.getJSONObject(assetPath);
                String hash = object.getString("hash"); // this is sha1 (I tested!)
                final String path = hash.substring(0, 2) + "/" + hash; // ab/abcdeeeeeeeeeeeeeeeeeeeee
                String url = "https://resources.download.minecraft.net/" + path;
                File localObjectFile = new File(localObjects, path);

                upgradeFile(localObjectFile, hash, url);
                size+= Files.size(localObjectFile.toPath());
                i++;
            }
        }
    }

    private List<File> loadLibraries(JSONArray jLibraries) throws Exception {
        final List<File> files = new ArrayList<>();
        int i = 0;
        while (i < jLibraries.length()) {
            final JSONObject jLib = jLibraries.getJSONObject(i);
            i++;

            final String name = jLib.getString("name");
            final JSONArray rules = jLib.optJSONArray("rules");
            if (!RulesParser.isRuleAllow(rules)) {
                Logger.d(TAG, "Library " + name + " disallow by rules!");
                continue;
            }


            JSONObject downloads = jLib.getJSONObject("downloads");


            if (jLib.has("natives")) {
                JSONObject natives = jLib.getJSONObject("natives");
                String os = RulesParser.getOperatingSystemType();
                if (natives.has(os)) {
                    String keyForNative = natives.getString(os);

                    JSONObject classifers;
                    try {
                        classifers = downloads.getJSONObject("classifiers");
                    } catch (Exception e) {
                        Logger.d(TAG, jLib.toString(2));
                        throw new RuntimeException(e);
                    }
                    JSONObject nativeArtefact = classifers.getJSONObject(keyForNative);

                    // DRY
                    String path = nativeArtefact.getString("path");
                    String sha1 = nativeArtefact.getString("sha1");
                    String url = nativeArtefact.getString("url");

                    File libraryFile = new File(librariesDir, path);
                    files.add(libraryFile);

                    upgradeFile(libraryFile, sha1, url); // Try to fix file
                    Logger.d(TAG, "Library NATIVE " + name + " loaded!");
                    // DRY
                }
            }

            JSONObject dArtefact = downloads.getJSONObject("artifact");

            String path = dArtefact.getString("path");
            String sha1 = dArtefact.getString("sha1");
            String url = dArtefact.getString("url");

            File libraryFile = new File(librariesDir, path);
            files.add(libraryFile);

            upgradeFile(libraryFile, sha1, url); // Try to fix file
            Logger.d(TAG, "Library " + name + " loaded!");
        }

        return files;
    }

    private VersionInfo parseVersionInfo(JSONObject j) {
        VersionInfo r = new VersionInfo();
        r.complianceLevel = j.getInt("complianceLevel");
        r.id = j.getString("id");
        r.type = j.getString("type");
        r.mainClass = j.getString("mainClass");
        r.assets = j.getString("assets");

        JSONObject jDownloads = j.getJSONObject("downloads");
        r.downloads = new VersionInfo.Downloads();
        r.downloads.client = new VersionInfo.Downloads.DownloadFile();
        r.downloads.client.sha1 = jDownloads.getJSONObject("client").getString("sha1");
        r.downloads.client.url = jDownloads.getJSONObject("client").getString("url");

        r.libraries = j.getJSONArray("libraries");
        r.assetsIndexInfo = j.getJSONObject("assetIndex");

        if (j.has("arguments")) { // NEW
            JSONObject args = j.getJSONObject("arguments");
            r.minecraftArguments = args.getJSONArray("game");
            r.jvmArguments = args.getJSONArray("jvm");

        } else if (j.has("minecraftArguments")) { // OLD
            r.minecraftArguments = new JSONArray().put(j.getString("minecraftArguments"));
        } else {
            throw new RuntimeException("Game/JVM arguments not found!");
        }

        return r;
    }

    private void upgradeFile(File file, String sha1, String url) throws Exception {
        if (isFileBroken(file, sha1)) {
            LogRecord log = log();
            NetworkUtil.downloadFile(file, url, (progress, currentRead, total) -> {
                short p = (short) (((float) progress / (float) total) * 100f);
                log.l("Downloading " + file.getName()).p(p);
                Logger.d(TAG, "["+p+"%] Downloading " + file.getName() + " " + progress + "/" + total);
            }, sha1);
        }
    }

    private boolean isFileBroken(File file, String sha1) throws Exception {
        return !(FileUtil.isExist(file) && CryptoUtil.isNormal(file, sha1));
    }

    private LogRecord log() {
        LogRecord l = new LogRecord();
        logs.add(l);
        return l;
    }

    public boolean isCancelable() {
        return process != null && process.isAlive();
    }

    public void cancel() {
        process.destroy();
    }

    public class LogRecord {
        private String text;
        private short progress = -1;

        public LogRecord() {}

        public LogRecord l(String l) {
            this.text = l;
            if (logsChangedListener != null) {
                logsChangedListener.run();
            }
            return this;
        }

        public LogRecord p(short s) {
            progress = s;
            if (logsChangedListener != null) {
                logsChangedListener.run();
            }
            return this;
        }

        public String getText() {
            return text;
        }

        public short getProgress() {
            return progress;
        }
    }
}
