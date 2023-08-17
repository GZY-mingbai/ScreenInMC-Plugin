# ScreenInMC-Plugin
![ScreenInMC Logo(Temporary) By zhuWin](https://ghproxy.com/https://github.com/GZY-mingbai/ScreenInMC-Plugin/blob/master/logo.png?raw=true)
# 欢迎使用 ScreenInMC Bukkit插件！
* [到 Actions 中下载插件](https://github.com/GZY-mingbai/ScreenInMC-Plugin/actions/workflows/build-all.yml)

此插件可以通过地图画，在Minecraft中拼接出一个显示器

![Example](https://ghproxy.com/https://github.com/GZY-mingbai/ScreenInMC-Plugin/blob/master/example.png?raw=true)  
通过对 Minecraft 实际环境高度优化的抖色技术，我们将生成地图画的质量和速度推向了 Minecraft 的极限。
1. 内置远程桌面客户端(VNC Client)功能。
2. 可选的现代网页浏览器内核(Chromium)，让您在 Minecraft 中也可体验 Web 的乐趣。
3. 跨平台的兼容性，保持了 Java 平台的优越性。  
目前，您可以在搭载了 Windows 的 x86, x86_64 平台和搭载了 Linux 的 x86_64 平台上完美使用 ScreenInMC 插件。  
在搭载了 macOS 平台上使用部分功能(浏览器、OpenCL加速不可用)。  

# 感兴趣？有建议？又或者是有BUG？
欢迎前来提交 Issues。  
https://github.com/GZY-mingbai/ScreenInMC-Plugin/issues  
最新构建可在 Actions 中获取。  
https://github.com/GZY-mingbai/ScreenInMC-Plugin/actions  

使用 MIT 许可证。

# 运行要求: 
1. 1.8-1.20 Bukkit 服务端 (推荐PaperMC) (必须)
2. OpenCL 支持 (可选)
3. 64位 Windows 10+ 系统 (可选)
4. Java 8+ (必须)

该项目还在开发改进中

# 目前可用指令:
```
1. 放置一块屏幕:  
   /screen putScreen <世界名> <X> <Y> <Z> <方向> <长度> <宽度> <核心>
2. 移除一块屏幕:  
   /screen removeScreen <屏幕UUID>
3. 列出所有OpenCL设备:  
   /screen listDevices
4. 设置抖色分块大小(仅使用OpenCL设备进行抖色时生效):  
   /screen setPieceSize <大小(1/2/4/8/16)>
5. 浏览器核心操作:  
    打开一个URL:  
       /screen browser <屏幕UUID> openurl <URL>  
    刷新网页:  
       /screen browser <屏幕UUID> refresh  
6. 输入文本到屏幕:  
   /screen input <屏幕UUID> <文本> ...
7. ★获取 ScreenInMC 控制器:  
   /screen controller
8. 获取帮助:  
   /screen help
```

# 浏览器说明:
1. 可通过 /screen installChromium 指令安装Chromium浏览器(JCEF)  
默认情况下，将会从 https://github.com/jcefmaven/jcefbuild 仓库下载最新构建(不支持h264解码)  
若要支持h264解码能力的JCEF(可观看B站等)，需自行下载并覆盖已安装的JCEF(版本不限)
2. 放置WebBrowser内核的屏幕后，需使用 ScreenInMC 控制器 手动选择安装的核心(若未安装 则不显示)
3. 若配置正确，且JCEF安装正确，则会出现画面。(已知BUG: 部分网页无法通过点击跳转)
# 其他功能:
1. 通过java -jar ScreenInMC.jar可使用ScreenInMC CLI，可用于生成像素画视频 (具体功能见控制台输出)  
……
# 采用或相关的开源项目:
1. [Vernacular VNC](https://github.com/shinyhut/vernacular-vnc) Java中的VNC客户端
2. [FFmpeg](https://github.com/ffmpeg/ffmpeg) 视频读取和处理
3. [Java CEF](https://github.com/chromiumembedded/java-cef) Java中的Chromium浏览器
4. [FreeRDP](https://github.com/FreeRDP/FreeRDP) (未来可能使用) 用于RDP客户端   
......