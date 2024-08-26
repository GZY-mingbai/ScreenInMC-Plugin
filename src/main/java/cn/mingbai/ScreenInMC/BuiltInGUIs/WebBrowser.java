package cn.mingbai.ScreenInMC.BuiltInGUIs;

import cn.mingbai.ScreenInMC.Browsers.Browser;
import cn.mingbai.ScreenInMC.Controller.EditGUI;
import cn.mingbai.ScreenInMC.Core;
import cn.mingbai.ScreenInMC.MGUI.Alignment;
import cn.mingbai.ScreenInMC.MGUI.ClickType;
import cn.mingbai.ScreenInMC.MGUI.Controls.MButton;
import cn.mingbai.ScreenInMC.MGUI.Controls.MButtonWithProgressBar;
import cn.mingbai.ScreenInMC.MGUI.Controls.MTextBlock;
import cn.mingbai.ScreenInMC.MGUI.MContainer;
import cn.mingbai.ScreenInMC.MGUI.MControl;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.RedstoneBridge;
import cn.mingbai.ScreenInMC.Utils.ImageUtils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.ImmediatelyCancellableBukkitRunnable;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import org.bukkit.Material;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class WebBrowser extends Core {
    private MContainer installer=null;
    private Browser browser = null;
    private ImmediatelyCancellableBukkitRunnable renderRunnable = null;
    private boolean unloaded = false;
    private Object setLock =null;

    public WebBrowser() {
        super("WebBrowser");
    }

    public Browser getBrowser() {
        return browser;
    }
    public static class WebBrowserStoredData implements StoredData {
        public String browser;
        public String uri;
        public int frameRateLimit=Main.defaultFrameRateLimit;

        @Override
        public StoredData clone() {
            WebBrowserStoredData data = new WebBrowserStoredData();
            data.browser = this.browser;
            data.uri = this.uri;
            data.frameRateLimit = this.frameRateLimit;
            return data;
        }
    }

    @Override
    public StoredData createStoredData() {
        return new WebBrowserStoredData();
    }

    @Override
    public void onCreate() {
        setLock =new Object();
        try {
            WebBrowserStoredData data = (WebBrowserStoredData)getStoredData();
            if(data.browser==null) {
                createInstaller();
                return;
            }
            browser = Browser.getBrowser(data.browser);
            loadBrowser();
        } catch (Exception e) {
            onUnload();
            e.printStackTrace();
        }
    }
    private void reloadButtonText(MControl area){
        try {
            MButton button1= (MButton) area.getChildControl(0);
            MButton button2= (MButton) area.getChildControl(1);
            MTextBlock textBlock = (MTextBlock) area.getChildControl(2);
            if(Browser.getBrowser("Chromium").getCoreState()==Browser.NOT_INSTALLED){
                button2.setDisabled(true);
                button1.setDisabled(false);
            }else {
                button1.setDisabled(true);
                button2.setDisabled(false);
            }
            textBlock.setText(LangUtils.getText("web-browser-tips"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void createInstaller(){
        if(installer!=null){
            installer.reRenderAll();
        }
        installer = new MContainer(getScreen());
        MControl control = new MControl();
        MTextBlock textBlock = new MTextBlock();
        MButtonWithProgressBar button1 = new MButtonWithProgressBar(LangUtils.getText("web-browser-install-chromium")){
            @Override
            public void onClick(int x, int y, ClickType type) {
                super.onClick(x, y, type);
                if(!isProcessingChromium) {
                    installChromium(this,textBlock,control);
                }
            }
        };
        MButtonWithProgressBar button2 = new MButtonWithProgressBar(LangUtils.getText("web-browser-uninstall-chromium")){
            @Override
            public void onClick(int x, int y, ClickType type) {
                super.onClick(x, y, type);
                if(!isProcessingChromium) {
                    uninstallChromium(this,textBlock,control);
                }
            }
        };
        button1.setWidth(128);
        button1.setHeight(64);
        button2.setWidth(128);
        button2.setHeight(64);
        textBlock.setHeight(16);
        textBlock.setWidth(384);
        textBlock.setLeft(-128);
        control.addChildControl(button1);
        control.addChildControl(button2);
        control.addChildControl(textBlock);
        control.setHeight(156);
        control.setWidth(128);
        button2.setTop(72);
        textBlock.setTop(140);
        installer.addChildControl(control);
        reloadButtonText(control);
        control.setHorizontalAlignment(Alignment.HorizontalAlignment.Center);
        control.setVerticalAlignment(Alignment.VerticalAlignment.Center);
        installer.setBackground(Color.WHITE);
        installer.load();
    }
    private static boolean isProcessingChromium = false;
    private void uninstallChromium(MButtonWithProgressBar button,MTextBlock textBlock,MControl buttonArea){
        if(installer==null) return;
        button.setDisabled(true);
        isProcessingChromium = true;
        Browser.BrowserCallback callback = new Browser.BrowserCallback() {
            @Override
            protected void handle(String message, float progress) {
                textBlock.setText(message);
                button.setProgress(progress);
            }

            @Override
            protected void complete() {
                textBlock.setText("");
                button.setProgress(0);
            }
        };
        ImmediatelyCancellableBukkitRunnable runnable = new ImmediatelyCancellableBukkitRunnable() {
            @Override
            public void run() {
                try {
                    Browser.getBrowser("Chromium").uninstallCore(callback);
                }catch (Throwable e){
                    e.printStackTrace();
                    textBlock.setText("");
                    button.setProgress(0);
                }
                isProcessingChromium = false;
                reloadButtonText(buttonArea);
                buttonArea.reRender();
            }
        };
        runnable.runTaskAsynchronously(Main.thisPlugin());
    }
    private void installChromium(MButtonWithProgressBar button,MTextBlock textBlock,MControl buttonArea){
        if(installer==null) return;
        button.setDisabled(true);
        isProcessingChromium = true;
        Browser.BrowserCallback callback = new Browser.BrowserCallback() {
            @Override
            protected void handle(String message, float progress) {
                textBlock.setText(message);
                button.setProgress(progress);
            }

            @Override
            protected void complete() {
                textBlock.setText("");
                button.setProgress(0);
            }
        };
        ImmediatelyCancellableBukkitRunnable runnable = new ImmediatelyCancellableBukkitRunnable() {
            @Override
            public void run() {
                try {
                    Browser.getBrowser("Chromium"). installCore(callback);
                }catch (Throwable e){
                    e.printStackTrace();
                    textBlock.setText("");
                    button.setProgress(0);
                }
                isProcessingChromium = false;
                reloadButtonText(buttonArea);
                buttonArea.reRender();
            }
        };
        runnable.runTaskAsynchronously(Main.thisPlugin());


    }
    @Override
    public void reRender() {
        if(installer!=null){
            installer.reRenderAll();
        }
    }
    private void loadBrowser(){
        WebBrowserStoredData data = (WebBrowserStoredData)getStoredData();
        if(browser.getCoreState()==Browser.NOT_INSTALLED){
            Main.getPluginLogger().warning("Can't use "+browser.getName()+" browser because it is not installed.");
            createInstaller();
            return;
        }
        if (browser.getCoreState() != Browser.LOADED) {
            try {
                browser.loadCore();
            } catch (Exception e) {
            }
        }
        if (browser.getCoreState() == Browser.LOADED) {
            String defaultURI;
            if(data!=null&&data.uri!=null&&data.uri.length()!=0){
                defaultURI=data.uri;
            }else{
                defaultURI=Main.getConfiguration().getString("browser-main-page");
            }
            try {
                browser.createBrowser(getScreen(), getScreen().getWidth() * 128, getScreen().getHeight() * 128,defaultURI);
            }catch (Error e){
                e.printStackTrace();
                return;
            }
            catch (RuntimeException e){
                e.printStackTrace();
                return;
            }
            catch (Throwable e){
                e.printStackTrace();
                return;
            }
            renderRunnable = new ImmediatelyCancellableBukkitRunnable() {
                @Override
                public void run() {
                    final WebBrowserStoredData storedData = (WebBrowserStoredData)getStoredData();
                    while (!this.isCancelled() && browser != null && !unloaded) {
                        long startTime = System.currentTimeMillis();
                        Utils.Pair<Utils.Pair<Integer, Integer>, int[]> image = browser.onRender(getScreen());
                        if (image.getValue().length != 0) {
                            if(!getScreen().canSleep()) {
                                byte[] data = ImageUtils.imageToMapColors(image.getValue(), image.getKey().getKey(), image.getKey().getValue());
                                if (data != null) getScreen().sendView(data);
                            }
                        }
                        int fps = 20;
                        if(setLock !=null&&storedData.frameRateLimit>=1&&storedData.frameRateLimit<=20) {
                            synchronized (setLock) {
                                fps=storedData.frameRateLimit;
                            }
                        }
                        long waitTime = (long)((int)(1000/fps)) - (System.currentTimeMillis() - startTime);
                        if (waitTime > 0) {
                            try {
                                Thread.sleep(waitTime);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            };
            renderRunnable.runTaskAsynchronously(Main.thisPlugin());
        }
        if(installer!=null) {
            installer.unload();
            installer = null;
        }
    }
    @Override
    public void onUnload() {
        if (browser != null) {
            if (browser.getCoreState() == Browser.LOADED) {
                browser.destroyBrowser(getScreen());
                browser = null;
                if (renderRunnable != null) {
                    renderRunnable.cancel();
                }
            }
        }
        unloaded = true;
    }

    @Override
    public void onMouseClick(int x, int y, Utils.MouseClickType type) {
        if (browser != null) {
            if(browser.getCoreState()==Browser.LOADED) {
                browser.clickAt(getScreen(), x, y, type);
            }
        }else if(installer!=null){
            installer.clickAt(x,y,ClickType.Left);
        }
    }

    @Override
    public void onTextInput(String text) {
        if (browser != null) {
            if(browser.getCoreState()==Browser.LOADED) {
                browser.inputText(getScreen(), text);
            }
        }
    }
    public static class BrowserInstalledCoresList implements EditGUI.EditGUICoreInfo.EditGUICoreSettingsList {
        private static List<Browser> getInstalledBrowserList(){
            List<Browser> browsers = new ArrayList<>();
            for(Browser i:Browser.getAllBrowsers()){
                if(i.getCoreState()!=Browser.NOT_INSTALLED){
                    browsers.add(i);
                }
            }
            return browsers;
        }
        @Override
        public String[] getList() {
            List<Browser> list = getInstalledBrowserList();
            String[] array = new String[list.size()];
            for(int i=0;i<list.size();i++){
                array[i]=list.get(i).getName();
            }
            return array;
        }

    }
    @Override
    public void addToEditGUI() {
        EditGUI.registerCoreInfo(new EditGUI.EditGUICoreInfo(
                "@controller-editor-cores-browser-name",
                this,
                "@controller-editor-cores-browser-details",
                "green",
                Material.GLASS,
                new LinkedHashMap(){
                    {
                        put("@controller-editor-cores-browser-uri", String.class);
                        put("@controller-editor-cores-browser-refresh", Boolean.class);
                        put("@controller-editor-cores-browser-core", BrowserInstalledCoresList.class);
                        put("@controller-editor-cores-frame-rate-limit", Integer.class);
                        put("@controller-editor-cores-browser-devtools",Boolean.class);
                    }
                }));
    }

    @Override
    public Object getEditGUISettingValue(String name) {
        switch (name){
            case "@controller-editor-cores-browser-uri":
                if(browser!=null){
                    return browser.getNowURL(getScreen());
                }
                return "";
            case "@controller-editor-cores-browser-refresh":
                return false;
            case "@controller-editor-cores-browser-core":
                if(browser!=null){
                    List<Browser> list = BrowserInstalledCoresList.getInstalledBrowserList();
                    for(int i=0;i<list.size();i++){
                        if(list.get(i).getName().equals(browser.getName())){
                            return i;
                        }
                    }
                }
                return -1;
            case "@controller-editor-cores-frame-rate-limit":
                WebBrowserStoredData data = (WebBrowserStoredData)getStoredData();
                return (int)data.frameRateLimit;
            case "@controller-editor-cores-browser-devtools":
                if(browser==null){
                    return false;
                }
                return browser.isInDeveloperMode(getScreen());
        }
        return null;
    }

    @Override
    public void setEditGUISettingValue(String name, Object value) {
        WebBrowserStoredData data = (WebBrowserStoredData)getStoredData();
        switch (name){
            case "@controller-editor-cores-browser-uri":
                if(browser!=null){
                    browser.openURL(getScreen(),(String) value);
                    data.uri = (String) value;
                }
                break;
            case "@controller-editor-cores-browser-refresh":
                if(browser!=null&&value.equals(true)){
                    browser.refreshPage(getScreen());
                }
                break;
            case "@controller-editor-cores-browser-core":
                if(browser!=null) {
                    browser.destroyBrowser(getScreen());
                }
                if((int)value==-1) {browser=null;return;}
                Browser newBrowser = Browser.getBrowser(new BrowserInstalledCoresList().getList()[(int) value]);
                data.browser=newBrowser.getName();
                browser = newBrowser;
                loadBrowser();
                break;
            case "@controller-editor-cores-frame-rate-limit":
                int v = (int)value;
                if(v>=1&&v<=20){
                    if(setLock !=null) {
                        synchronized (setLock) {
                            data.frameRateLimit = v;
                        }
                    }
                }
                break;
            case "@controller-editor-cores-browser-devtools":
                if(browser==null){
                    return;
                }
                browser.setDeveloperMode(getScreen(),(boolean)value);
                break;
        }
    }
    public void onRedstoneInput(int id,int value){
        synchronized (Browser.getRedstoneInputListenersMap()) {
            if(Browser.getRedstoneInputListenersMap().get(this.getScreen())==null){
                return;
            }
            for (Browser.RedstoneInputCallback callback : Browser.getRedstoneInputListenersMap().get(this.getScreen())) {
                if (callback.getID() == id) {
                    callback.onInput(value);
                }
            }
        }
    }
    public void redstoneOutput(int id,int value){
        List<RedstoneBridge.RedstoneSignalInterface> list = new ArrayList();
        for(Utils.Pair<String,RedstoneBridge.RedstoneSignalInterface> signalInterface:getRedstoneBridge().getRedstoneSignalInterfaces()){
            if(!signalInterface.getValue().isInput()){
                list.add(signalInterface.getValue());
            }
        }
        if(id>=1&&id<=54){
            list.get(id-1).sendRedstoneSignal(value);
        }
    }
    @Override
    public void registerRedstoneBridge() {
        for(int i=1;i<55;i++) {
            final int index = i;
            getRedstoneBridge().addRedstoneSignalInterface("Input " + i, new RedstoneBridge.RedstoneSignalInterface(true) {
                @Override
                public void onReceiveRedstoneSignal(Core core, int strength) {
                    ((WebBrowser)core).onRedstoneInput(index,strength);
                }
            });
            getRedstoneBridge().addRedstoneSignalInterface("Output " + i, new RedstoneBridge.RedstoneSignalInterface(false));
        }
    }
}
