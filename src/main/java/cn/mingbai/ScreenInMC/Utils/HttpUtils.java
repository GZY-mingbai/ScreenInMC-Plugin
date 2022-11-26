package cn.mingbai.ScreenInMC.Utils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class HttpUtils {
    public static String getString(String url, String proxyUrlString) {
        try {
            URLConnection urlConnection;
            if (proxyUrlString == null || proxyUrlString.length() == 0) {
                urlConnection = new URL(url).openConnection();
            } else {
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
                return new String(IOUtils.readInputStream(inputStream), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw (RuntimeException) e;
        }
        return "";
    }

    public static long downloadFile(String url, String path, String proxyUrlString, Function<Utils.Pair<Long, Long>, Void> callback) {
        try {
            URLConnection urlConnection;
            if (proxyUrlString == null || proxyUrlString.length() == 0) {
                urlConnection = new URL(url).openConnection();
            } else {
                URL proxyUrl = new URL(proxyUrlString);
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort()));
                urlConnection = new URL(url).openConnection(proxy);
            }
            HttpURLConnection connection = (HttpURLConnection) urlConnection;
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                long size = connection.getContentLength();
                File file = new File(path);
                if (file.exists()) {
                    if (!file.delete()) {
                        throw new RuntimeException("Delete file \"" + path + "\" failed.");
                    }
                } else {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                InputStream inputStream = connection.getInputStream();
                CallbackByteChannel callbackByteChannel = new CallbackByteChannel(Channels.newChannel(inputStream), size, callback);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                FileChannel fileChannel = fileOutputStream.getChannel();
                fileChannel.transferFrom(callbackByteChannel, 0, Long.MAX_VALUE);
                fileChannel.close();
                fileOutputStream.close();
                callbackByteChannel.close();
                inputStream.close();
                return size;
            }
        } catch (Exception e) {
            throw (RuntimeException) e;
        }
        return 0;
    }

    public static class CallbackByteChannel implements ReadableByteChannel {
        private final long size;
        private final ReadableByteChannel channel;
        private long sizeRead;
        private final Function<Utils.Pair<Long, Long>, Void> callback;

        CallbackByteChannel(ReadableByteChannel channel, long size,
                            Function<Utils.Pair<Long, Long>, Void> function) {
            this.size = size;
            this.channel = channel;
            this.callback = function;
        }

        public void close() throws IOException {
            channel.close();
        }

        public boolean isOpen() {
            return channel.isOpen();
        }

        public int read(ByteBuffer buffer) throws IOException {
            int n;
            double progress;
            if ((n = channel.read(buffer)) > 0) {
                sizeRead += n;
                callback.apply(new Utils.Pair<Long, Long>(size, sizeRead));
            }
            return n;
        }
    }

    public class GithubReleasesObject {
        public String tag_name;
    }
}
