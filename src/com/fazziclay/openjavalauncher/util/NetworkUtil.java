package com.fazziclay.openjavalauncher.util;

import com.fazziclay.javaneoutil.FileUtil;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * for network
 * **/
public class NetworkUtil {
    public static final List<LogInterface> NETWORK_LISTENERS = new ArrayList<>();

    /**
     * @return text of site
     * **/
    public static String parseTextPage(String url) throws IOException {
        int logKey = 0;
        if (NETWORK_LISTENERS.size() > 0) logKey = generateLogKey();
        for (LogInterface logInterface : NETWORK_LISTENERS) {
            logInterface.parseTextPage(logKey, 0, url, null);
        }

        StringBuilder result = new StringBuilder();
        URL pageUrl = new URL(url);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(pageUrl.openStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line).append("\n");
        }
        bufferedReader.close();

        String ret = result.substring(0, result.lastIndexOf("\n"));
        for (LogInterface logInterface : NETWORK_LISTENERS) {
            logInterface.parseTextPage(logKey, 1, url, ret);
        }

        return ret;
    }

    private static int generateLogKey() {
        int logKey = new Random().nextInt();
        if (logKey < 0) logKey *= -1;
        return logKey;
    }

    public static void downloadFile(File file, String url) throws IOException {
        InputStream in = new URL(url).openStream();
        FileUtil.setText(file, "");
        Files.copy(in, Paths.get(file.toURI()), StandardCopyOption.REPLACE_EXISTING);
    }

    public interface LogInterface {
        void parseTextPage(int logKey, int state, String url, String result);
    }
}
