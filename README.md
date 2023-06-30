# ScreenInMC-Plugin
![ScreenInMC Logo(Temporary) By zhuWin](https://github.com/GZY-mingbai/ScreenInMC-Plugin/blob/master/logo.png?raw=true)
# 欢迎使用 ScreenInMC Bukkit插件！
此插件可以通过地图画，在Minecraft中拼接出一个显示器

![Example](https://github.com/GZY-mingbai/ScreenInMC-Plugin/blob/master/example.png?raw=true)
通过对 Minecraft 实际环境高度优化的抖色技术，我们将生成地图画的质量和速度推向了 Minecraft 的极限。
1. 内置远程桌面客户端(VNC Client)功能。
2. 可选的现代网页浏览器内核(Chromium)，让您在 Minecraft 中也可体验 Web 的乐趣。
3. 跨平台的兼容性，保持了 Java 平台的优越性。  
目前，您可以在搭载了 Windows 的 x86, x86_64 平台上完美使用 ScreenInMC 插件。  
在搭载了 Linux 的 x86_64 平台上使用部分功能(浏览器不可用)。  
未来，我们将进一步提高对Linux的兼容性 并支持macOS、Windows arm64等平台。  

# 感兴趣？有建议？
欢迎前来提交 Issues。  
https://github.com/GZY-mingbai/ScreenInMC-Plugin/issues  
最新构建可在 Actions 中获取。  
https://github.com/GZY-mingbai/ScreenInMC-Plugin/actions  

使用 MIT 许可证。

# 完美运行要求(当前): 
1. 1.19.2 Bukkit 服务端 (推荐PaperMC) (必须)
2. OpenCL 支持 (可选)
3. 64位 Windows 10+ 系统
4. 64位 Java 17

该项目还在开发改进中

# 目前可用指令:
1. /screen controller 获取屏幕控制器  
(仅支持放置屏幕，可在服务器关闭后，通过修改ScreenInMC/screens.json，设置屏幕内核)
2. /screen putScreen <世界> <X> <Y> <Z> <方向> <宽度> <高度> <屏幕内核(推荐WebBrowser、VNCClient)> 放置屏幕
3. /screen removeScreen <ID> 删除屏幕
4. /screen input <ID> <文本> 输入文本到屏幕
5. /screen browser <ID> openurl <URL> 打开网页(仅支持WebBrowser内核的屏幕)
6. /screen browser <ID> refresh 刷新当前网页(仅支持WebBrowser内核的屏幕)
7. /screen listDevices 列出OpenCL设备
8. /screen setPieceSize <大小> 动态设置抖色分块大小

# 浏览器说明:
1. 可通过 /screen installChromium 指令安装Chromium浏览器(JCEF)  
默认情况下，将会从 https://github.com/jcefmaven/jcefbuild 仓库下载最新构建(不支持h264解码)  
若要支持h264解码能力的JCEF(可观看B站等)，需自行下载并覆盖已安装的JCEF(版本不限)
2. 放置WebBrowser内核的屏幕后，需要在关闭服务器后，修改ScreenInMC/screens.json  
```   
"core": {
   "coreClassName": "cn.mingbai.ScreenInMC.BuiltInGUIs.WebBrowser",
   "data": {
      "browser": "Chromium"
   }
}
```
3. 开启服务器后，若配置正确，且JCEF安装正确，则会出现画面。(已知BUG: 部分网页无法通过点击跳转)
# 其他功能:
1. 通过java -jar ScreenInMC.jar可使用ScreenInMC CLI，可用于生成像素画视频
2. ……