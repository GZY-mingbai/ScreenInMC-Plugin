package cn.mingbai.ScreenInMC.BrowserCoreInitializations;

import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.HttpUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import com.google.gson.Gson;

import java.util.function.Function;
import java.util.logging.Logger;

public class ChromiumCoreInitialization implements BrowserCoreInitialization {
    @Override
    public void installCore() {
        Logger logger = Main.getPluginLogger();
        logger.info(LangUtils.getText("start-download-chromium-core"));
        int type = Main.getConfiguration().getInt("download-browser-core.jcef-download-url.type");
        String downloadUrl = "";
        String httpProxyUrl = Main.getConfiguration().getString("download-browser-core.http-proxy");
        switch (type) {
            case 0:
                downloadUrl = Main.getConfiguration().getString("download-browser-core.jcef-download-url.url");
                break;
            case 1:
                //https://github.com/jcefmaven/jcefbuild
                String githubProxyUrl = Main.getConfiguration().getString("download-browser-core.github-proxy");
                String repoUrl = Main.getConfiguration().getString("download-browser-core.jcef-download-url.url");
                String version = Main.getConfiguration().getString("download-browser-core.jcef-download-url.version");
                String systemType = Main.getConfiguration().getString("download-browser-core.jcef-download-url.system-type");
                String systemName;
                String systemArch;
                if (systemType.length() == 0) {
                    Utils.Pair<String,String> systemNameAndArch = Utils.getSystem();
                    systemName = systemNameAndArch.getKey();
                    systemArch = systemNameAndArch.getValue();
                } else {
                    String[] systemNameAndArch = systemType.split("-", 1);
                    systemName = systemNameAndArch[0];
                    systemArch = systemNameAndArch[1];
                }
                if (systemName == null) {
                    throw new RuntimeException("Current system is not supported: " + systemName + ".");
                }
                if (systemArch == null) {
                    throw new RuntimeException("Current arch is not supported: " + systemArch + ".");
                }
                if (version.length() == 0) {
                    String repoReleasesUrl = repoUrl.replace("github.com", "api.github.com/repos") + "/releases";
                    String repoReleasesString = HttpUtils.getString(repoReleasesUrl, httpProxyUrl);
                    Gson gson = new Gson();
                    HttpUtils.GithubReleasesObject[] repoReleasesObject = gson.fromJson(repoReleasesString, HttpUtils.GithubReleasesObject[].class);
                    if (repoReleasesObject.length == 0) {
                        throw new RuntimeException("Get Github Repositories TagName failed.");
                    }
                    version = repoReleasesObject[0].tag_name;
                }
                downloadUrl = repoUrl + "/releases/download/" + version + "/" + systemName + "-" + systemArch + ".tar.gz";
                if (githubProxyUrl.length() != 0) {
                    downloadUrl = githubProxyUrl.replace("%URL%", downloadUrl);
                }
                String progressText = LangUtils.getText("download-progress");
                HttpUtils.downloadFile(downloadUrl, Main.PluginFilesPath + "Temp/jcef.tar.gz", httpProxyUrl, new Function<Utils.Pair<Long, Long>, Void>() {
                    long count = 0;

                    @Override
                    public Void apply(Utils.Pair<Long, Long> progress) {
                        count++;
                        if (count % 10 == 0) {
                            logger.info(progressText.replace("%%", String.format("%.2f", (float) ((double) progress.getValue() /
                                    (double) progress.getKey() * 100.0)) + "%"));
                        }
                        return null;
                    }
                });
                logger.info(LangUtils.getText("download-success"));
                break;
        }
    }
}
