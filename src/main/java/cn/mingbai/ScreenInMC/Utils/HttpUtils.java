package cn.mingbai.ScreenInMC.Utils;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class HttpUtils {
    public class GithubReleasesObject{
        public String tag_name;
    }
    public static String getString(String url,String proxyUrlString){
        try{
            URLConnection urlConnection;
            if(proxyUrlString==null||proxyUrlString.length()==0){
                urlConnection = new URL(url).openConnection();
            }else{
                URL proxyUrl = new URL(proxyUrlString);
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort()));
                urlConnection = new URL(url).openConnection(proxy);
            }
            HttpURLConnection connection = (HttpURLConnection) urlConnection;
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                return new String(IOUtils.readInputStream(inputStream),StandardCharsets.UTF_8);
            }
        }catch (Exception e){
            throw (RuntimeException)e;
        }
        return "";
    }
}
