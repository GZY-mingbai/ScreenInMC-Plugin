package cn.mingbai.ScreenInMC.BrowserCoreInitialization;

import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Utils.HttpUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import com.google.gson.Gson;
import org.bukkit.Bukkit;

public class ChromiumCore implements BrowserCore{
    @Override
    public void installCore() {
        Main.getPluginLogger().info(LangUtils.getText("start-download-chromium-core"));
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
                if(systemType.length()==0) {

                }else{

                }
                downloadUrl=repoUrl+"/releases/download/"+version+"/"+"";
                if(githubProxyUrl.length()!=0) {
                    downloadUrl = githubProxyUrl.replace("%URL%",downloadUrl);
                }
                Bukkit.broadcastMessage(downloadUrl);
                break;
        }
    }
}
