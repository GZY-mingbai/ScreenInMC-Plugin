package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Utils.IOUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils.imageToMapColors;

public class VideoProcessor {

    static final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
    static final int[] bOffs = {0,1,2};
    static final ComponentColorModel colorModel = new ComponentColorModel(cs, false, false,
            Transparency.OPAQUE,
            DataBuffer.TYPE_BYTE);
    public static GenerateProcess generateDitheredVideo(File ffmpegFile, File videoFile, File outputFile, boolean useGzip, String[] ffmpegArgs) {
        GenerateProcess generateProcess = new GenerateProcess();
        generateProcess.nowState = 0;
        try {
            VideoReader reader = new VideoReader(
                    ffmpegFile,
                    videoFile.getAbsolutePath(),
                    ffmpegArgs,System.out
            );
            if(outputFile.isDirectory()){
                throw new Exception(outputFile.getAbsolutePath()+" is a directory.");
            }
            if(outputFile.isFile()&&outputFile.exists()){
                outputFile.delete();
            }
            if(outputFile.exists()){
                throw new Exception(outputFile.getAbsolutePath()+" can't be deleted.");
            }

            Thread onExit = new Thread(){
                @Override
                public void run() {
                    System.out.println("Stopping ffmpeg...");
                    reader.stop();
                }
            };
            Runtime.getRuntime().addShutdownHook(onExit);

            int width = reader.getWidth();
            int height = reader.getHeight();
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write((byte) 0xE7);
            outputStream.write((byte) 0xAA);
            outputStream.write((byte) 0x9C);
            outputStream.write((byte) 0x09);
            outputStream.write((byte) 20);
            if (useGzip) {
                outputStream.write((byte) 1);
            } else {
                outputStream.write((byte) 0);
            }
            outputStream.write((width >> 8) & 0xFF);
            outputStream.write(width & 0xFF);
            outputStream.write((height >> 8) & 0xFF);
            outputStream.write(height & 0xFF);
            OutputStream newOutputStream;
            if(useGzip){
                newOutputStream = new GZIPOutputStream(outputStream);
            }else{
                newOutputStream = outputStream;
            }

                try {
                    reader.start(0,reader.getVideoTime(),new Function<VideoReader.VideoReaderCallback, Void>() {
                        DecimalFormat decimalFormat = new DecimalFormat("#0.0");
                        @Override
                        public Void apply(VideoReader.VideoReaderCallback data) {
                            try {
                                newOutputStream.write(data.getData());
                                generateProcess.process = ((float) data.getNowFrame()) / ((float) (data.getFramesCount()));
                                System.out.println("Processed: " + data.getNowFrame() + "/" + data.getFramesCount()+" ("+decimalFormat.format(generateProcess.process*100f)+"%).");
                                synchronized (generateProcess) {
                                    generateProcess.notify();
                                }
                            } catch (IOException e) {
                            }
                            return null;
                        }
                    });
                    System.out.println("Completed.");
                    Runtime.getRuntime().removeShutdownHook(onExit);

                } catch (Exception e) {
                    e.printStackTrace();
                    generateProcess.error = e;
                    generateProcess.nowState = -1;
                    synchronized (generateProcess) {
                        generateProcess.notify();
                    }
                }

        } catch (Exception e) {
            generateProcess.error = e;
            generateProcess.nowState = -1;
            synchronized (generateProcess) {
                generateProcess.notify();
            }
        }
        return generateProcess;
    }

    public static DitheredVideo readDitheredVideo(String path, boolean loop) {
        try {
            URL url = null;
            try {
                url = new URL(path);
            } catch (Exception e) {
            }
            if (url != null) {
                return readDitheredVideo(url, loop);
            } else {
                return readDitheredVideo(new File(path), loop);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public static DitheredVideo readDitheredVideo(File file, boolean loop) {
        try {
            DitheredVideo video = readDitheredVideo(new FileInputStream(file), loop);
            video.reopenStream = new Function<Void, InputStream>() {
                @Override
                public InputStream apply(Void unused) {
                    try {
                        InputStream stream = new FileInputStream(file);
                        stream.skip(10);
                        if(video.useGzip){
                            return new GZIPInputStream(stream);
                        }else {
                            return stream;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            return video;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    //在MC使用插件时才可使用该函数(CLI模式下不可用)
    public static DitheredVideo readDitheredVideoWithPlugin(URI url, boolean loop) {
        try {
            DitheredVideo video = readDitheredVideo(Utils.getStreamFromURI(url), loop);
            video.reopenStream = new Function<Void, InputStream>() {
                @Override
                public InputStream apply(Void unused) {
                    try {
                        InputStream stream = Utils.getStreamFromURI(url);
                        stream.skip(10);
                        if(video.useGzip){
                            return new GZIPInputStream(stream);
                        }else {
                            return stream;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            return video;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public static DitheredVideo readDitheredVideo(URL url, boolean loop) {
        try {
            DitheredVideo video = readDitheredVideo(url.openStream(), loop);
            video.reopenStream = new Function<Void, InputStream>() {
                @Override
                public InputStream apply(Void unused) {
                    try {
                        InputStream stream = url.openStream();
                        stream.skip(10);
                        if(video.useGzip){
                            return new GZIPInputStream(stream);
                        }else {
                            return stream;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            return video;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static DitheredVideo readDitheredVideo(InputStream inputStream, boolean loop) {
        try {
            byte[] bytes = new byte[10];
            inputStream.read(bytes,0,10);
            if (bytes[0] == (byte) 0xE7 && bytes[1] == (byte) 0xAA && bytes[2] == (byte) 0x9C && bytes[3] == (byte) 0x09) {
                if (bytes[4] != (byte) 20) {
                    throw new Exception("File's version is not supported: " + bytes[4]);
                }
                DitheredVideo video = new DitheredVideo();
                video.stream = inputStream;
                video.useGzip = (bytes[5] == (byte) 0) ? false : true;
                video.width = ((bytes[6] & 0xFF) << 8) | (bytes[7] & 0xFF);
                video.height = ((bytes[8] & 0xFF) << 8) | (bytes[9] & 0xFF);
                video.size = video.width * video.height;
                video.loop = loop;
                if(video.useGzip){
                    video.stream = new GZIPInputStream(video.stream);
                }
                return video;
            } else {
                throw new Exception("File is not .smv");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class GenerateProcess {
        public static final int ERROR = -1;
        public static final int GENERATING = 0;
        public static final int SUCCESS = 1;
        private float process;
        private int nowState;
        private Exception error;

        private GenerateProcess() {
        }

        public Exception getError() {
            return error;
        }

        public int getNowState() {
            return nowState;
        }

        public float getProcess() {
            return process;
        }
    }

    public static class DitheredVideo {
        private InputStream stream;
        private boolean useGzip;
        private int width;
        private int height;
        private int size;
        private boolean loop;
        private Function<Void, InputStream> reopenStream;
        private byte[] frame;

        private DitheredVideo() {
        }

        public void close() {
            try {
                stream.close();
            } catch (Exception e) {
            }
        }

        private void reopen() {
            close();
            this.stream = reopenStream.apply(null);
        }

        public synchronized byte[] readAFrame() {
            if(frame==null){
                frame = new byte[size];
            }
            try {
                int length=0;
                while (length<size){
                    int l = stream.read(frame,length,size-length);
                    if (l <= 0) {
                        if (loop) {
                            reopen();
                            return readAFrame();
                        }
                        close();
                        return new byte[0];
                    }
                    length+=l;
                }
                return frame;
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
    public static class VideoReader{
        private int width;
        private int height;
        private long frames;
        private int size;
        private long time;
        private static class VideoData{
            public int width=-1;
            public int height=-1;
            public long frames=-1;
            public long time=-1;
        }

        private static VideoData getVideoData(File ffmpegFile, String videoPath) throws IOException {
            List<String> args = new ArrayList<String>();
            if (ffmpegFile == null) {
                args.add("ffmpeg");
            } else {
                args.add(ffmpegFile.getAbsolutePath());
            }
            args.add("-i");
            args.add(videoPath);
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            processBuilder.environment().put("LC_ALL", "en_US.UTF-8");
            Process process = processBuilder.start();
            InputStream stream = process.getErrorStream();
            byte[] data = IOUtils.readInputStream(stream);
            stream.close();
            String output = new String(data);
            VideoData videoData = new VideoData();
            getData:
            for(String i:output.split("\n")){
                if((videoData.width==-1||videoData.height==-1)&&i.toLowerCase().replace(" ","").startsWith("stream")&&i.toLowerCase().contains("video")){
                    String[] values = i.split(",");
                    for(String j:values){
                        if(j.contains("x")){
                            String[] size = j.split(" ");
                            for(String k:size){
                                if(k.contains("x")){
                                    String[] widthHeight = k.split("x");
                                    try {
                                        videoData.width = Integer.parseInt(widthHeight[0]);
                                        videoData.height = Integer.parseInt(widthHeight[1]);
                                        continue getData;
                                    }catch (NumberFormatException e){
                                        continue;
                                    }catch (ArrayIndexOutOfBoundsException e){
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                }
                if(videoData.frames==-1&&i.toLowerCase().replace(" ","").startsWith("duration")){
                    String[] values = i.split(",");
                    for(String j:values){
                        if(j.contains(":")){
                            String[] time = j.split(" ");
                            for(String k:time){
                                if(k.contains(":")){
                                    String[] times = k.split("[:.]");
                                    try {
                                        long hours = Long.parseLong(times[0]);
                                        long minutes = Long.parseLong(times[1]);
                                        long seconds = Long.parseLong(times[2]);
                                        String msStr = times[3];
                                        if(msStr.length()>3){
                                            msStr = msStr.substring(0,3);
                                        }else
                                        if(msStr.length()<3){
                                            while (msStr.length()<3){
                                                msStr = msStr+"0";
                                            }
                                        }
                                        long milliseconds = Long.parseLong(msStr);
                                        videoData.time = hours*3600000l+minutes*60000l+seconds*1000l+milliseconds;
                                        videoData.frames = (long) Math.ceil(((double)  videoData.time)/50d);
                                        continue getData;
                                    }catch (NumberFormatException e){
                                        continue;
                                    }catch (ArrayIndexOutOfBoundsException e){
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(videoData.width==-1||videoData.height==-1||videoData.frames==-1){
                return null;
            }
            return videoData;
        }
        private Thread listenLogThread;
        private Process process;
        private PrintStream logger;
        private File ffmpegFile;
        private String videoPath;
        private String[] ffmpegArgs;
        private static String getTimeString(long time){
            long hours = time/3600000l;
            long minutes = (time-hours*3600000l)/60000l;
            long seconds = (time-hours*3600000l-minutes*60000l)/1000l;
            long milliseconds = (time-hours*3600000l-minutes*60000l-seconds*1000l);
            String ms = String.valueOf(milliseconds);
            if(ms.length()>2){
                ms = ms.substring(0,2);
            }else if(ms.length()==1){
                ms = "0"+ms;
            }
            return hours+":"+minutes+":"+seconds+"."+ms;
        }

        public long getVideoTime() {
            return time;
        }

        public VideoReader(File ffmpegFile, String videoPath, String[] ffmpegArgs, PrintStream logger) throws Exception {
            VideoData videoData = getVideoData(ffmpegFile,videoPath);
            if(videoData==null){
                throw new Exception("Read video data error.");
            }
            this.width = videoData.width;
            this.height = videoData.height;
            this.frames = videoData.frames;
            this.size = width*height;
            this.time = videoData.time;
            this.ffmpegFile = ffmpegFile;
            this.videoPath = videoPath;
            this.ffmpegArgs = ffmpegArgs;
            this.logger = logger;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public long getFramesCount() {
            return frames;
        }
        public static class VideoReaderCallback{
            private byte[] data;
            private int framesCount;
            private int nowFrame;
            private VideoReaderCallback(byte[] data,int framesCount,int nowFrame){
                this.data=data;
                this.framesCount=framesCount;
                this.nowFrame=nowFrame;
            }

            public byte[] getData() {
                return data;
            }

            public int getFramesCount() {
                return framesCount;
            }

            public int getNowFrame() {
                return nowFrame;
            }
        }
        public void stop(){
            if(process!=null){
                if(process.isAlive()){
                    process.destroy();
                    process.destroyForcibly();
                    if(thread!=null&&thread.isAlive()){
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                        }
                        thread=null;
                    }
                    if(listenLogThread!=null&&listenLogThread.isAlive()){
                        try {
                            listenLogThread.join();
                        }catch (InterruptedException e){
                        }
                        listenLogThread=null;
                    }
                    process=null;
                }
            }
        }
        private Thread thread;
        public void start(long start,long end,Function<VideoReaderCallback,Void> callback){
            stop();
            thread = new Thread(){
                @Override
                public void run() {
                    List<String> args = new ArrayList<String>();
                    if (ffmpegFile == null) {
                        args.add("ffmpeg");
                    } else {
                        args.add(ffmpegFile.getAbsolutePath());
                    }
                    args.add("-i");
                    args.add(videoPath);
                    args.add("-r");
                    args.add("20");
                    args.add("-pix_fmt");
                    args.add("rgba");
                    if(ffmpegArgs!=null) {
                        for (int i = 0; i < ffmpegArgs.length; i++) {
                            args.add(ffmpegArgs[i]);
                        }
                    }
                    args.add("-f");
                    args.add("image2pipe");
                    args.add("-ss");
                    args.add(getTimeString(start));
                    args.add("-to");
                    args.add(getTimeString(end));
                    args.add("-vcodec");
                    args.add("rawvideo");
                    args.add("-y");
                    args.add("-");
                    if(logger!=null){
                        logger.println("Start ffmpeg with args: "+String.join(" ",args));
                    }
                    ProcessBuilder processBuilder = new ProcessBuilder(args);
                    processBuilder.environment().put("LC_ALL", "en_US.UTF-8");
                    try {
                        process = processBuilder.start();
                    }catch (Exception e){
                        VideoReader.this.stop();
                        if(logger!=null){
                            e.printStackTrace(logger);
                        }
                        return;
                    }
                    InputStream logStream = process.getErrorStream();
                    InputStream stream = process.getInputStream();
                    if(logger!=null){
                        listenLogThread = new Thread(){
                            @Override
                            public void run() {
                                try {
                                    InputStreamReader reader = new InputStreamReader(logStream, StandardCharsets.UTF_8);
                                    BufferedReader bufferedReader = new BufferedReader(reader);
                                    String str = null;
                                    while (((str = bufferedReader.readLine()) != null) && process.isAlive()) {
                                        logger.println(str);
                                    }
                                    bufferedReader.close();
                                    reader.close();
                                    logStream.close();
                                }catch (Exception e){
                                }
                            }
                        };
                        listenLogThread.start();
                    }
                    int processedFrames = 0;
                    int imageDataSize = size*4;
                    byte[] frameData = new byte[imageDataSize];
                    byte[] receivedData = new byte[65536];
                    int received = 0;
                    while (process.isAlive()){
                        try {
                            int r=stream.read(receivedData,0,receivedData.length);
                            if(r==0) return;
                            int t = received+r;
                            if(t>imageDataSize){
                                System.arraycopy(receivedData,0,frameData,received, imageDataSize-received);
                            }else {
                                System.arraycopy(receivedData,0,frameData,received,r);
                            }
                            received=t;
                            if(received>=imageDataSize){
                                DataBufferByte dataBuffer = new DataBufferByte(frameData, frameData.length);
                                WritableRaster raster = Raster.createInterleavedRaster(dataBuffer, width, height, width*4, 4, bOffs, null);
                                BufferedImage image = new BufferedImage(colorModel,raster,colorModel.isAlphaPremultiplied(),null);
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
                                processedFrames++;
                                callback.apply(new VideoReaderCallback(data, (int) frames,processedFrames));
                                System.arraycopy(receivedData,imageDataSize-received+r,frameData,0, received-imageDataSize);
                                received -= imageDataSize;
                            }
                        }catch (Exception e){
                            VideoReader.this.stop();
                        }
                    }
                }
            };
            thread.start();
        }
    }
}


