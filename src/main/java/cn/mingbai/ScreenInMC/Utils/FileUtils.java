package cn.mingbai.ScreenInMC.Utils;


import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

public class FileUtils {
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

    public static void decompressTarGz(String filePath, String outPath, Function<String, Void> callback) {
        try {
            File file = new File(filePath);
            File out = new File(outPath);
            FileInputStream inputStream = new FileInputStream(file);
            GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
            TarArchiveInputStream stream = new TarArchiveInputStream(gzipInputStream);
            ReadableByteChannel readableByteChannel = Channels.newChannel(stream);
            if (out.exists()) {
                deleteDir(out);
            }
            out.mkdirs();
            TarArchiveEntry entry = null;
            Throwable error = null;
            try {
                while ((entry = stream.getNextTarEntry()) != null) {
                    if (entry.isDirectory()) {
                        new File(outPath + entry.getName()).mkdirs();
                    } else {
                        FileOutputStream outputStream = new FileOutputStream(outPath + entry.getName());
                        FileChannel channel = outputStream.getChannel();
                        channel.transferFrom(readableByteChannel, 0, stream.available());
                        channel.close();
                        outputStream.close();
                        callback.apply(entry.getName());
                    }
                }
            } catch (Throwable e) {
                error = e;
            }
            readableByteChannel.close();
            stream.close();
            gzipInputStream.close();
            inputStream.close();
            if (error != null) {
                throw error;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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
                Throwable error = null;
                try {
                    fileChannel.transferFrom(callbackByteChannel, 0, Long.MAX_VALUE);
                } catch (Exception e) {
                    error = e;
                }
                fileChannel.close();
                fileOutputStream.close();
                callbackByteChannel.close();
                inputStream.close();
                if (error != null) {
                    throw error;
                }
                return size;
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public static void deleteDir(File file) {
        if (file.isFile()) {
            file.delete();
        } else {
            File[] files = file.listFiles();
            if (files == null) {
                file.delete();
            } else {
                for (int i = 0; i < files.length; i++) {
                    deleteDir(files[i]);
                }
                file.delete();
            }
        }
    }

    public static void deleteDirOnExit(File file) {
        if (file.isFile()) {
            file.deleteOnExit();
        } else {
            File[] files = file.listFiles();
            if (files == null) {
                file.deleteOnExit();
            } else {
                for (int i = 0; i < files.length; i++) {
                    deleteDir(files[i]);
                }
                file.deleteOnExit();
            }
        }
    }

    public static void streamToFile(InputStream stream, File file) {
        try {
            ReadableByteChannel inputChannel = Channels.newChannel(stream);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileChannel.transferFrom(inputChannel, 0, Long.MAX_VALUE);
            fileChannel.close();
            fileOutputStream.close();
            inputChannel.close();
            stream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class CallbackByteChannel implements ReadableByteChannel {
        private final long size;
        private final ReadableByteChannel channel;
        private final Function<Utils.Pair<Long, Long>, Void> callback;
        private long sizeRead;

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

    public static class GithubReleasesObject {
        public String tag_name;
    }
}
