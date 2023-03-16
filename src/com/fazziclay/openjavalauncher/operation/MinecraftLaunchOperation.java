package com.fazziclay.openjavalauncher.operation;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.openjavalauncher.OpenJavaLauncher;
import com.fazziclay.openjavalauncher.launcher.GameProfile;
import com.fazziclay.openjavalauncher.launcher.RulesParser;
import com.fazziclay.openjavalauncher.launcher.UserProfile;
import com.fazziclay.openjavalauncher.launcher.VersionManifest;
import com.fazziclay.openjavalauncher.util.CryptoUtil;
import com.fazziclay.openjavalauncher.util.Logger;
import com.fazziclay.openjavalauncher.util.NetworkUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.fazziclay.openjavalauncher.util.Lang.t;

public class MinecraftLaunchOperation extends Operation {
    private static final String TAG = "MinecraftLaunchOperation";
    private final GameProfile gameProfile;
    private final UserProfile userProfile;
    private short percentage = -1;
    private String state;
    private Process process;

    public MinecraftLaunchOperation(GameProfile gameProfile, UserProfile userProfile) {
        Objects.requireNonNull(gameProfile, "gameProfile");
        Objects.requireNonNull(userProfile, "userProfile");
        this.gameProfile = gameProfile;
        this.userProfile = userProfile;
    }

    public void setPercentage(short percentage) {
        this.percentage = percentage;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    @Override
    public String getTitle() {
        return t("operation.minecraftLaunch.title", gameProfile.getVersionId());
    }

    @Override
    public String getDescription() {
        return (percentage < 0 ? "" : percentage + "% ") + state;
    }

    @Override
    public void cancel() {
        process.destroy();
    }

    @Override
    public boolean isCancelable() {
        return process != null;
    }

    public void run(File versionsDir, File librariesDir) {
        try {
            VersionManifest.Version version = OpenJavaLauncher.getInstance().getVersionManifest().getVersionById(gameProfile.getVersionId());
            setState(t("operation.minecraftLaunch.state.prepare"));
            File versionDir = new File(versionsDir, version.getId());
            File versionJsonFile = new File(versionDir, String.format("%s.json", version.getId()));
            File versionJar = new File(versionDir, String.format("%s.jar", version.getId()));

            if (!(FileUtil.isExist(versionJsonFile) && CryptoUtil.isNormal(versionJsonFile, version.getSha1()))) {
                setState(t("operation.minecraftLaunch.state.downloadingVersionInfo"));
                NetworkUtil.downloadFile(versionJsonFile, version.getUrl(), (progress, currentRead, total) -> percentage = (short) (((float) progress / (float) total) * 100f), version.getSha1());
            }
            JSONObject versionJson = new JSONObject(FileUtil.getText(versionJsonFile));

            String versionJarSha1 = versionJson.getJSONObject("downloads").getJSONObject("client").getString("sha1");
            String versionJarUrl = versionJson.getJSONObject("downloads").getJSONObject("client").getString("url");
            if (!(FileUtil.isExist(versionJar) && CryptoUtil.isNormal(versionJar, versionJarSha1))) {
                setState(t("operation.minecraftLaunch.state.downloadingVersionJar"));
                NetworkUtil.downloadFile(versionJar, versionJarUrl, (progress, currentRead, total) -> percentage = (short) (((float) progress / (float) total) * 100f), versionJarSha1);
            }

            // Libraries
            List<File> librariesFiles = new ArrayList<>();
            librariesFiles.add(versionJar);
            JSONArray jsonLibs = versionJson.getJSONArray("libraries");
            int i = 0;
            while (i < jsonLibs.length()) {
                JSONObject jsonLib = jsonLibs.getJSONObject(i);
                String name = jsonLib.getString("name");
                JSONArray rules = jsonLib.optJSONArray("rules");
                if (!RulesParser.isRuleAllow(rules)) {
                    Logger.d(TAG, "Lib not used by rules!" + jsonLib);
                    i++;
                    continue;
                }
                JSONObject downloads = jsonLib.getJSONObject("downloads");
                JSONObject dArtefact;
                dArtefact = downloads.getJSONObject("artifact");
                File libraryFile = new File(librariesDir, dArtefact.getString("path"));
                String sha1 = dArtefact.getString("sha1");
                if (!(FileUtil.isExist(libraryFile) && CryptoUtil.isNormal(libraryFile, sha1))) {
                    setState(t("operation.minecraftLaunch.state.downloadingLibrary", name));
                    NetworkUtil.downloadFile(libraryFile, dArtefact.getString("url"), (progress, currentRead, total) -> percentage = (short) (((float) progress / (float) total) * 100f), sha1);
                }
                librariesFiles.add(libraryFile);
                i++;
            }
            percentage = -1;

            String jvm = gameProfile.getJVMArguments();
            StringBuilder classpath = new StringBuilder();
            for (File resultLib : librariesFiles) {
                classpath.append(resultLib.getAbsolutePath()).append(":");
            }
            classpath = new StringBuilder(classpath.substring(0, classpath.length() - 1));
            String mainClass = versionJson.getString("mainClass");
            String gameArguments = "--username fazzitl --version OptiFine 1.19.2 --gameDir /home/l/Minecraft --assetsDir /home/l/Minecraft/assets --assetIndex 1.19 --uuid 750494a191733fd79336798470bf2dd8 --accessToken 750494a191733fd79336798470bf2dd8 --clientId  --xuid  --userType legacy --versionType modified --width 925 --height 530 --server localhost --port 25565";


            List<String> launchCommand = new ArrayList<>();
            launchCommand.add("/usr/lib/jvm/java-17-openjdk-amd64/bin/java");
            launchCommand.addAll(Arrays.asList(jvm.split(" ")));
            launchCommand.add("-cp");
            launchCommand.add(classpath.toString());
            launchCommand.add(mainClass);
            JSONObject features = new JSONObject()
                    .put("is_demo_user", userProfile.isDemo())
                    .put("has_custom_resolution", gameProfile.getWindowWidth() > 0 && gameProfile.getWindowHeight() > 0);
            JSONArray gameArgumentsJson = versionJson.getJSONObject("arguments").getJSONArray("game");
            for (Object o : gameArgumentsJson) {
                String[] arguments = new String[0];
                if (o instanceof String) {
                    arguments = new String[]{(String) o};
                }
                if (o instanceof JSONObject) {
                    JSONObject j = (JSONObject) o;
                    if (!RulesParser.isRuleAllow(j.getJSONArray("rules"), features)) {
                        continue;
                    }
                    Object value = j.get("value");
                    if (value instanceof String) {
                        arguments = new String[]{(String) value};
                    } else if (value instanceof JSONArray) {
                        arguments = new String[((JSONArray) value).length()];
                        i = 0;
                        for (Object o1 : ((JSONArray) value)) {
                            arguments[i] = (String) o1;
                            i++;
                        }
                    }
                }

                for (String argument : arguments) {
                    launchCommand.add(argument
                            .replace("${auth_player_name}", userProfile.getNickname())
                            .replace("${version_name}", version.getId())
                            .replace("${game_directory}", gameProfile.getGameDirectory().getAbsolutePath())
                            .replace("${assets_root}", "/home/l/Minecraft/assets")
                            .replace("${assets_index_name}", "1.19")
                            .replace("${auth_uuid}", userProfile.getUuid())
                            .replace("${auth_access_token}", "")
                            .replace("${clientid}", "")
                            .replace("${auth_xuid}", "")
                            .replace("${user_type}", "legacy")
                            .replace("${version_type}", "vanilla")
                            .replace("${resolution_width}", "122")
                            .replace("${resolution_height}", "122")
                    );
                }
            }

            Logger.d(TAG, "launchCommand: " + launchCommand);
            ProcessBuilder processBuilder = new ProcessBuilder(launchCommand);
            process = processBuilder.start();
            setState(t("operation.minecraftLaunch.state.launched", process.pid()));
            int exitCode = process.waitFor();

            setState(t("operation.minecraftLaunch.state.finished", version.getId(), exitCode));
        } catch (Exception e) {
            Logger.e("MinecraftThread", "Exception", e);
        }
    }
}
