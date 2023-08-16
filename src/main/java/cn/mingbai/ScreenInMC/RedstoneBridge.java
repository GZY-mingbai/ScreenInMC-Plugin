package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Utils.CraftUtils.CraftUtils;
import cn.mingbai.ScreenInMC.Utils.ImmediatelyCancellableBukkitRunnable;
import cn.mingbai.ScreenInMC.Utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.mingbai.ScreenInMC.Utils.CraftUtils.CraftUtils.checkRedstoneWire;
import static cn.mingbai.ScreenInMC.Utils.CraftUtils.CraftUtils.setRedstoneWirePower;

public class RedstoneBridge implements Cloneable {
    private Core core;
    public RedstoneBridge(Core core){
        this.core = core;
    }
    public static List<RedstoneSignalInterface> outputBlocks = new ArrayList<>();
    public static List<RedstoneSignalInterface> inputBlocks = new ArrayList<>();

    public static class RedstoneSignalInterface implements Cloneable{
        private Location block = null; //Must be redstone_wire.
        private boolean isConnected = false;
        private boolean isInput = false;
        private RedstoneBridge bridge;

        public Location getBlockLocation() {
            return block;
        }

        public RedstoneSignalInterface(boolean isInput){
            this.isInput=isInput;
        }

        public boolean isConnected() {
            return isConnected;
        }
        public static class ConnectException extends RuntimeException{
            public static final int SET_OUTPUT = 0;
            public static final int SET_INPUT = 1;
            private int type = 0;
            public ConnectException(int type){
                this.type = type;
            }

            public int getType() {
                return type;
            }

            @Override
            public String getMessage() {
                return type==0?"Has set to output.":"Has set to input.";
            }
        }

        public void connect(Location block){
            if(isConnected){
                disconnect();
            }
            synchronized (this) {
                Object wire = checkRedstoneWire(block);
                if (wire != null) {
                    this.block = block;
                    isConnected = true;
                    if (isInput) {
                        synchronized (outputBlocks){
                            for(RedstoneSignalInterface loc:outputBlocks){
                                if(loc.getBlockLocation().equals(block)){
                                    throw new ConnectException(ConnectException.SET_OUTPUT);
                                }
                            }
                        }
                        synchronized (inputBlocks) {
                            inputBlocks.add(this);
                        }
                    } else {
                        synchronized (outputBlocks){
                            for(RedstoneSignalInterface loc:outputBlocks){
                                if(loc.getBlockLocation().equals(block)){
                                    throw new ConnectException(ConnectException.SET_OUTPUT);
                                }
                            }
                        }
                        synchronized (inputBlocks){
                            for(RedstoneSignalInterface loc:inputBlocks){
                                if(loc.getBlockLocation().equals(block)){
                                    throw new ConnectException(ConnectException.SET_INPUT);
                                }
                            }
                        }
                        synchronized (outputBlocks) {
                            outputBlocks.add(this);
                        }
                    }
                }
            }
        }
        public void tryReceiveRedstoneSignal(int value){
            boolean disconnect = false;
            synchronized (this) {
                Object wire = checkRedstoneWire(block);
                if(wire==null){
                    disconnect=true;
                }else {
                    if (isInput) {
                        onReceiveRedstoneSignal(bridge.core,value);
                    }
                }
            }
            if(disconnect){
                disconnect();
            }
        }
        public boolean isInput() {
            return isInput;
        }

        public void onReceiveRedstoneSignal(Core core,int strength/*From 1 to 15*/) {

        }
        public void disconnect(){
            synchronized (this) {
                if(isInput){
                    synchronized (inputBlocks) {
                        inputBlocks.remove(this);
                    }
                }else{
                    synchronized (outputBlocks) {
                        outputBlocks.remove(this);
                    }
                }
                this.block = null;
                isConnected = false;
            }
        }
        private ImmediatelyCancellableBukkitRunnable resetRunnable = null;
        public void sendRedstoneSignal(int strength) {
            synchronized (this) {
                if (!isConnected) {
                    return;
                }
                if (!isInput) {
                    if (block.getBlock().getType().equals(Material.REDSTONE_WIRE)) {
                        Object wire = checkRedstoneWire(block);
                        if (wire != null) {
                            final RedstoneSignalInterface signalInterface = this;
                            BukkitRunnable runnable = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Object wire = checkRedstoneWire(signalInterface.block);
                                    if (wire != null) {
                                        setRedstoneWirePower(signalInterface.block.getBlock(),wire,strength);
                                    }
                                    if (wire == null) {
                                        disconnect();
                                    }
                                }
                            };
                            if(resetRunnable!=null&&!resetRunnable.isCancelled()){
                                resetRunnable.cancel();
                            }
                            resetRunnable = new ImmediatelyCancellableBukkitRunnable() {
                                @Override
                                public void run() {
                                    if(this.isCancelled()){
                                        return;
                                    }
                                    Object wire = checkRedstoneWire(signalInterface.block);
                                    if (wire != null) {
                                        signalInterface.block.getBlock().getState().update();
                                    }
                                    if (wire == null) {
                                        disconnect();
                                    }
                                }
                            };
                            runnable.runTask(Main.thisPlugin());
                            resetRunnable.runTaskLater(Main.thisPlugin(), 1L);
                        }
                        if (wire == null) {
                            disconnect();
                        }

                    }
                } else {
                    this.onReceiveRedstoneSignal(bridge.core,strength);
                }
            }
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            RedstoneSignalInterface newInterface = (RedstoneSignalInterface) super.clone();
            newInterface.isConnected=false;
            return newInterface;
        }
    }
    private Map<String,RedstoneSignalInterface> interfaces = new LinkedHashMap<>();
    public RedstoneSignalInterface addRedstoneSignalInterface(String id,RedstoneSignalInterface signalInterface){
        if(interfaces.containsKey(id)){
            throw new RuntimeException("Interface with id: "+id+" is existed.");
        }
        try {
            RedstoneSignalInterface newInterface = (RedstoneSignalInterface) signalInterface.clone();
            newInterface.bridge=this;
            interfaces.put(id, newInterface);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return signalInterface;
    }
    public List<Utils.Pair<String,RedstoneSignalInterface>> getRedstoneSignalInterfaces(){
        List<Utils.Pair<String,RedstoneSignalInterface>> list = new ArrayList<>();
        for(String i:interfaces.keySet()){
            list.add(new Utils.Pair<>(i,interfaces.get(i)));
        }
        return list;
    }
    public RedstoneSignalInterface getRedstoneSignalInterface(String id){
        return interfaces.get(id);
    }
    public void removeRedstoneSignalInterface(String id){
        if(interfaces.containsKey(id)) {
            interfaces.remove(id);
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    public Object clone(Core core) throws CloneNotSupportedException {
        RedstoneBridge newBridge = (RedstoneBridge) super.clone();
        newBridge.core = core;
        Map<String,RedstoneSignalInterface> oldInterfaces = newBridge.interfaces;
        newBridge.interfaces = new LinkedHashMap<>();
        for(String i:oldInterfaces.keySet()){
            RedstoneSignalInterface newInterface = (RedstoneSignalInterface) oldInterfaces.get(i).clone();
            newInterface.bridge=newBridge;
            newBridge.interfaces.put(i, newInterface);
        }
        return newBridge;
    }
}
