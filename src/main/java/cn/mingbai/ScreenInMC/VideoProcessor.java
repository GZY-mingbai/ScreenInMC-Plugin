package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Utils.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.channels.*;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static cn.mingbai.ScreenInMC.Utils.ImageUtils.*;

public class VideoProcessor {
    public static class GenerateProcess {
        private float process;
        private int nowState;
        public static final int ERROR = -1;
        public static final int GENERATING = 0;
        public static final int SUCCESS = 1;
        private Exception error;

        public Exception getError() {
            return error;
        }

        public int getNowState() {
            return nowState;
        }

        public float getProcess() {
            return process;
        }
        private GenerateProcess(){}

        Thread thread;
    }

    public static class DitheredVideo {
        private InputStream stream;
        private boolean useGzip;
        private int width;
        private int height;
        private int size;
        private boolean loop;
        private Function<Void, InputStream> reopenStream;
        private DitheredVideo(){}

        public void close() {
            try {
                stream.close();
            } catch (Exception e) {
            }
        }
        private void reopen(){
            if(reopenStream==null){
                try {
                    stream.reset();
                }catch (Exception e){
                    throw new RuntimeException(e);
                }
            }else{
                close();
                this.stream = reopenStream.apply(null);
            }
            try {
                stream.skip(10);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
        public synchronized byte[] readAFrame() {
            try {
                if (useGzip) {
                    byte[] bytes = new byte[4];
                    int length = stream.read(bytes);
                    if (length != 4) {
                        if(loop){
                            reopen();
                            return readAFrame();
                        }
                        close();
                        return new byte[0];
                    }
                    int readLen = (bytes[0] & 0xFF) << 24 |
                            (bytes[1] & 0xFF) << 16 |
                            (bytes[2] & 0xFF) << 8 |
                            (bytes[3] & 0xFF);
                    bytes = new byte[readLen];
                    length = stream.read(bytes);
                    if (length != readLen) {
                        close();
                        return new byte[0];
                    }
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                    GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
                    byte[] buffer = new byte[1024];
                    int n;
                    while ((n = gzipInputStream.read(buffer)) >= 0) {
                        byteArrayOutputStream.write(buffer, 0, n);
                    }
                    byte[] frame = byteArrayOutputStream.toByteArray();
                    if(frame.length!=size){
                        close();
                        return new byte[0];
                    }
                    byteArrayOutputStream.close();
                    gzipInputStream.close();
                    byteArrayInputStream.close();
                    return frame;
                } else {
                    byte[] frame = new byte[size];
                    int length = stream.read(frame);
                    if (length != size) {
                        if(loop){
                            reopen();
                            return readAFrame();
                        }
                        close();
                        return new byte[0];
                    }
                    return frame;
                }
            } catch (Exception e) {
                e.printStackTrace();
                close();
                return new byte[0];
            }
        }

        public synchronized byte[] readAFrameIn50ms() {
            long start = System.currentTimeMillis();
            byte[] data = readAFrame();
            long wait = 50 - (System.currentTimeMillis() - start);
            if (wait > 0) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return data;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    public static GenerateProcess generateDitheredVideo(File ffmpegFile, File tempDir, File videoFile, File outputFile, boolean useGzip,String[] ffmpegArgs) {
        GenerateProcess generateProcess = new GenerateProcess();
        generateProcess.nowState = 0;
        try {
            Runtime runtime = Runtime.getRuntime();
            if (tempDir.exists() && !tempDir.isDirectory()) {
                throw new Exception();
            }
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            Process process;
            String[] args = new String[11+ffmpegArgs.length];
            if (ffmpegFile == null) {
                args[0]="ffmpeg";
            } else {
                args[0]=ffmpegFile.getAbsolutePath();
            }
            args[1] = "-i";
            args[2] = videoFile.getAbsolutePath();
            args[3] = "-r";
            args[4] = "20";
            args[5] = "-pix_fmt";
            args[6] = "rgba";
            for(int i=0;i<ffmpegArgs.length;i++){
                args[i+7] = ffmpegArgs[i];
            }
            args[7+ffmpegArgs.length] = "-f";
            args[8+ffmpegArgs.length] = "image2";
            args[9+ffmpegArgs.length] = tempDir + "/%01d.png";
            args[10+ffmpegArgs.length] = "-y";

            process = runtime.exec(args);
            InputStream stream = process.getErrorStream();
            generateProcess.thread = new Thread(() -> {
                try {
                    InputStreamReader reader = new InputStreamReader(stream);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String str = null;
                    while (((str = bufferedReader.readLine()) != null) && process.isAlive()) {
                        System.out.println(str);
                    }
                    process.waitFor();
                    if (process.exitValue() == 0) {
                        File[] f = tempDir.listFiles();
                        File[] files=new File[f.length];
                        for(int i=0;i<f.length;i++){
                            for(File file:f){
                                if(file.getName().equals((i+1)+".png")){
                                    files[i] = file;
                                }
                            }
                        }
                        if (files.length > 0) {
                            File file = files[0];
                            int width, height, size;
                            BufferedImage image = ImageIO.read(file);
                            width = image.getWidth();
                            height = image.getHeight();
                            size = width * height;
                            File out = new File(tempDir.getAbsoluteFile() + "/out.smv");
                            FileOutputStream outputStream = new FileOutputStream(out);
                            outputStream.write((byte) 0xE7);
                            outputStream.write((byte) 0xAA);
                            outputStream.write((byte) 0x9C);
                            outputStream.write((byte) 0x09);
                            outputStream.write((byte) 19);
                            if (useGzip) {
                                outputStream.write((byte) 1);
                            } else {
                                outputStream.write((byte) 0);
                            }
                            outputStream.write((width >> 8) & 0xFF);
                            outputStream.write(width & 0xFF);
                            outputStream.write((height >> 8) & 0xFF);
                            outputStream.write(height & 0xFF);
                            for (int i = 0; i < files.length; i++) {
                                image = ImageIO.read(files[i]);
                                byte[] data = imageToMapColors(image);
                                if (data.length > size) {
                                    byte[] d = data;
                                    data = new byte[size];
                                    System.arraycopy(d, 0, data, 0, size);
                                }
                                if (data.length < size) {
                                    byte[] d = data;
                                    data = new byte[size];
                                    System.arraycopy(d, 0, data, 0, data.length);
                                }
                                if (useGzip) {
                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
                                    gzipOutputStream.write(data);
                                    gzipOutputStream.finish();
                                    gzipOutputStream.flush();
                                    data = byteArrayOutputStream.toByteArray();
                                    gzipOutputStream.close();
                                    byteArrayOutputStream.close();
                                    outputStream.write((data.length >> 24) & 0xFF);
                                    outputStream.write((data.length >> 16) & 0xFF);
                                    outputStream.write((data.length >> 8) & 0xFF);
                                    outputStream.write(data.length & 0xFF);
                                }
                                outputStream.write(data);
                                generateProcess.process = ((float) i) / ((float) (files.length - 1));
                                synchronized (generateProcess){
                                    generateProcess.notify();
                                }
                                System.out.println("Dither: " + (i + 1) + "/" + files.length);
                            }
                            outputStream.close();
                            FileChannel inputChannel = new FileInputStream(out).getChannel();
                            FileChannel outputChannel = new FileOutputStream(outputFile).getChannel();
                            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
                            inputChannel.close();
                            outputChannel.close();
                        }
                        FileUtils.deleteDir(tempDir);
                        System.out.println("Finished");
                    } else {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    generateProcess.error = e;
                    generateProcess.nowState = -1;
                    synchronized (generateProcess){
                        generateProcess.notify();
                    }
                }
            });
            generateProcess.thread.start();

        } catch (Exception e) {
            generateProcess.error = e;
            generateProcess.nowState = -1;
            synchronized (generateProcess){
                generateProcess.notify();
            }
        }
        return generateProcess;
    }
    public static DitheredVideo readDitheredVideo(String path,boolean loop){
        try{
            URL url=null;
            try{
                url = new URL(path);
            }catch (Exception e){
            }
            if(url!=null){
                return readDitheredVideo(url,loop);
            }else{
                return readDitheredVideo(new File(path),loop);
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public static DitheredVideo readDitheredVideo(File file,boolean loop){
        try{
            DitheredVideo video = readDitheredVideo(new FileInputStream(file),loop);
            video.reopenStream = new Function<Void, InputStream>() {
                @Override
                public InputStream apply(Void unused) {
                    try {
                        return new FileInputStream(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            return video;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public static DitheredVideo readDitheredVideo(URL url, boolean loop){
        try{
            DitheredVideo video = readDitheredVideo(url.openStream(),loop);
            video.reopenStream = new Function<Void, InputStream>() {
                @Override
                public InputStream apply(Void unused) {
                    try {
                        return url.openStream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            return video;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public static DitheredVideo readDitheredVideo(InputStream inputStream,boolean loop) {
        try {
            byte[] bytes = new byte[10];
            inputStream.read(bytes);
            if (bytes[0] == (byte) 0xE7 && bytes[1] == (byte) 0xAA && bytes[2] == (byte) 0x9C && bytes[3] == (byte) 0x09) {
                if (bytes[4] != (byte) 19) {
                    throw new Exception("File's version is not supported: " + bytes[4]);
                }
                DitheredVideo video = new DitheredVideo();
                video.stream = inputStream;
                video.useGzip = (bytes[5] == (byte) 0) ? false : true;
                video.width = ((bytes[6] & 0xFF) << 8) | (bytes[7] & 0xFF);
                video.height = ((bytes[8] & 0xFF) << 8) | (bytes[9] & 0xFF);
                video.size = video.width * video.height;
                video.loop=loop;
                return video;
            } else {
                throw new Exception("File is not .smv");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


