package cn.mingbai.ScreenInMC.BrowserCoreInitialization;

import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.HttpUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import com.google.gson.Gson;
import org.bukkit.Bukkit;

import java.util.function.Function;
import java.util.logging.Logger;

public class ChromiumCore implements BrowserCore{
    @Override
    public void installCore() {
        Logger logger = Main.getPluginLogger();
        logger.info(LangUtils.getText("start-download-chromium-core"));
        int type = Main.getConfiguration().getInt("download-browser-core.jcef-download-url.type");
        String downloadUrl="";
        String httpProxyUrl = Main.getConfiguration().getString("download-browser-core.http-proxy");
        switch (type){
            case 0:
                downloadUrl=Main.getConfiguration().getString("download-browser-core.jcef-download-url.url");
                break;
            case 1:
                //https://github.com/jcefmaven/jcefbuild
                String githubProxyUrl = Main.getConfiguration().getString("download-browser-core.github-proxy");
                String repoUrl=Main.getConfiguration().getString("download-browser-core.jcef-download-url.url");
                String version = Main.getConfiguration().getString("download-browser-core.jcef-download-url.version");
                String systemType = Main.getConfiguration().getString("download-browser-core.jcef-download-url.system-type");
                String systemName;
                String systemArch;
                if(systemType.length()==0) {
                    systemName=System.getProperty("os.name").replace(" ","").toLowerCase();
                    systemArch=System.getProperty("os.arch");
                }else{
                    String[] systemNameAndArch = systemType.split("-",1);
                    systemName = systemNameAndArch[0];
                    systemArch = systemNameAndArch[1];
                }
                if(systemName.indexOf("windows")!=0){
                    systemName = "windows";
                } else
                if (systemName.indexOf("linux")!=0) {
                    systemName = "linux";
                }else
                if (systemName.indexOf("macosx")!=0) {
                    systemName = "macosx";
                }else{
                    throw new RuntimeException("Current system is not supported: "+systemName+".");
                }
                if(systemArch.equals("x86_64")||systemArch.equals("amd64")||systemArch.equals("x64")||systemArch.equals("ia64")){
                    systemArch="amd64";
                }else
                if(systemArch.equals("x86_32")||systemArch.equals("i386")||systemArch.equals("x86")||systemArch.equals("x32")||systemArch.equals("ia32")){
                    systemArch="i386";
                }else
                if(systemArch.equals("arm")||systemArch.equals("arm32")||systemArch.equals("aarch32")){
                    systemArch="arm";
                }else
                if(systemArch.equals("arm64")||systemArch.equals("aarch64")){
                    systemArch="arm64";
                }else{
                    throw new RuntimeException("Current arch is not supported: "+systemArch+".");
                }
                if(version.length()==0){
                    String repoReleasesUrl = repoUrl.replace("github.com","api.github.com/repos")+"/releases";
                    String repoReleasesString = HttpUtils.getString(repoReleasesUrl,httpProxyUrl);
                    Gson gson = new Gson();
                    HttpUtils.GithubReleasesObject[] repoReleasesObject = gson.fromJson(repoReleasesString,HttpUtils.GithubReleasesObject[].class);
                    if(repoReleasesObject.length==0){
                        throw new RuntimeException("Get Github Repositories TagName failed.");
                    }
                    version = repoReleasesObject[0].tag_name;
                }
                downloadUrl=repoUrl+"/releases/download/"+version+"/"+systemName+"-"+systemArch+".tar.gz";
                if(githubProxyUrl.length()!=0) {
                    downloadUrl = githubProxyUrl.replace("%URL%",downloadUrl);
                }
                String progressText = LangUtils.getText("download-progress");
                HttpUtils.downloadFile(downloadUrl, Main.PluginFilesPath + "Temp/jcef.tar.gz", httpProxyUrl, new Function<Utils.Pair<Long,Long>, Void>() {
                    long count = 0;
                    @Override
                    public Void apply(Utils.Pair<Long, Long> progress) {
                        count++;
                        if(count%10==0){
                            logger.info(progressText.replace("%%",String.format("%.2f",(float)((double) progress.getValue() /
                                    (double) progress.getKey() * 100.0))+"%"));
                        }
                        return null;
                    }
                });
                logger.info(LangUtils.getText("download-success"));
                break;
        }
    }
}
