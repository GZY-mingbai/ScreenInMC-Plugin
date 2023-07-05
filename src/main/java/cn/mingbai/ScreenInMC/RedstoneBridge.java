package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Utils.CraftUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RedstoneBridge implements Cloneable {
    private Core core;
    public RedstoneBridge(Core core){
        this.core = core;
    }
    public static List<Location> outputBlocks = new ArrayList<>();
    public static List<Location> inputBlocks = new ArrayList<>();

    public abstract static class RedstoneSignalInterface implements Cloneable{
        private Location block = null; //Must be redstone_wire.
        private boolean isConnected = false;
        private boolean isInput = false;
        private BukkitRunnable listener = null;
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
                RedstoneWire wire = checkRedstone(block);
                if (wire != null) {
                    this.block = block;
                    isConnected = true;
                    final RedstoneSignalInterface signalInterface = this;
                    if (isInput) {
                        synchronized (outputBlocks){
                            for(Location loc:outputBlocks){
                                if(loc.equals(block)){
                                    throw new ConnectException(ConnectException.SET_OUTPUT);
                                }
                            }
                        }
                        synchronized (inputBlocks) {
                            inputBlocks.add(block);
                        }
                    } else {
                        synchronized (outputBlocks){
                            for(Location loc:outputBlocks){
                                if(loc.equals(block)){
                                    throw new ConnectException(ConnectException.SET_OUTPUT);
                                }
                            }
                        }
                        synchronized (inputBlocks){
                            for(Location loc:inputBlocks){
                                if(loc.equals(block)){
                                    throw new ConnectException(ConnectException.SET_INPUT);
                                }
                            }
                        }
                        synchronized (outputBlocks) {
                            outputBlocks.add(block);
                        }
                    }
                    listener = new BukkitRunnable() {
                        @Override
                        public void run() {
                            boolean disconnect = false;
                            synchronized (signalInterface) {
                                RedstoneWire wire = checkRedstone(signalInterface.block);
                                if(wire==null){
                                    disconnect=true;
                                }else {
                                    if (isInput) {
                                        onReceiveRedstoneSignal(bridge.core,wire.getPower());
                                    }
                                }
                            }
                            if(disconnect){
                                disconnect();
                            }
                        }
                    };
                    listener.runTaskTimer(Main.thisPlugin(),0,1L);
                }
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
                        inputBlocks.remove(block);
                    }
                }else{
                    synchronized (outputBlocks) {
                        outputBlocks.remove(block);
                    }
                }
                this.block = null;
                isConnected = false;
                if(listener!=null){
                    listener.cancel();
                }
            }
        }
        private BukkitRunnable resetRunnable = null;
        public void sendRedstoneSignal(int strength) {
            synchronized (this) {
                if (!isConnected) {
                    return;
                }
                if (!isInput) {
                    if (block.getBlock().getType().equals(Material.REDSTONE_WIRE)) {
                        RedstoneWire wire = checkRedstone(block);
                        if (wire != null) {
                            final RedstoneSignalInterface signalInterface = this;
                            BukkitRunnable runnable = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    RedstoneWire wire = checkRedstone(signalInterface.block);
                                    if (wire != null) {
                                        wire.setPower(strength);
                                        signalInterface.block.getBlock().setBlockData(wire);
                                    }
                                    if (wire == null) {
                                        disconnect();
                                    }
                                }
                            };
                            if(resetRunnable!=null&&!resetRunnable.isCancelled()){
                                resetRunnable.cancel();
                            }
                            resetRunnable = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if(this.isCancelled()){
                                        return;
                                    }
                                    RedstoneWire wire = checkRedstone(signalInterface.block);
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
            newInterface.listener=null;
            newInterface.isConnected=false;
            return newInterface;
        }
    }
    private Map<String,RedstoneSignalInterface> interfaces = new LinkedHashMap<>();
    public void addRedstoneSignalInterface(String id,RedstoneSignalInterface signalInterface){
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
    private static RedstoneWire checkRedstone(Location location){
        if(location==null){
            return null;
        }
        if(location.getBlock()==null){
            return null;
        }
        BlockData data = location.getBlock().getBlockData();
        if(data instanceof RedstoneWire) {
            RedstoneWire wire = (RedstoneWire) data;
            return wire;
        }
        return null;
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
