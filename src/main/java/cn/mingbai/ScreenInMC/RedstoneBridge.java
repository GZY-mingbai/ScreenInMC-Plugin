package cn.mingbai.ScreenInMC;

import cn.mingbai.ScreenInMC.Utils.CraftUtils.CraftUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.mingbai.ScreenInMC.Utils.CraftUtils.CraftUtils.checkRedstoneRepeater;

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
                if (checkRedstoneRepeater(block)) {
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
                    this.block = block;
                    isConnected = true;
//                    resetBlock(block);
                }
            }
        }
        public void tryReceiveRedstoneSignal(int value){
            boolean disconnect = false;
            synchronized (this) {
                if(!checkRedstoneRepeater(block)){
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
                Location oldBlock =this.block;
                this.block = null;
                isConnected = false;

            }
        }
        private int nowPower = 0;
//        private void resetBlock(Location loc){
//            if (checkRedstoneRepeater(loc)) {
////                Block block = loc.getBlock();
////                setRepeaterFacing(block,face);
//            }
//        }


        public void sendRedstoneSignal(int strength) {
            synchronized (this) {
                if (!isConnected) {
                    return;
                }
                if (!isInput) {
                    if (checkRedstoneRepeater(block)) {
                        nowPower = strength;
//                        CraftUtils.inactiveRepeater(block.getBlock());
//                        final Location lastBlock = this.block;
//                        BukkitRunnable runnable = new ImmediatelyCancellableBukkitRunnable() {
//                            @Override
//                            public void run() {
//                                if(RedstoneSignalInterface.this.block.equals(lastBlock))
//                                CraftUtils.activeRepeater(RedstoneSignalInterface.this.block.getBlock());
//                            }
//                        };
//                        runnable.runTaskLater(Main.thisPlugin(),1L);
                    }else {
                        disconnect();
                    }
                } else {
                    this.onReceiveRedstoneSignal(bridge.core,strength);
                }
            }
        }
        public boolean checkRedstoneRepeaterAndUpdate(){
            if(!CraftUtils.checkRedstoneRepeater(block)){
                disconnect();
                return false;
            }
            return true;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            RedstoneSignalInterface newInterface = (RedstoneSignalInterface) super.clone();
            newInterface.isConnected=false;
            return newInterface;
        }

        public int getNowPower() {
            return nowPower;
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
