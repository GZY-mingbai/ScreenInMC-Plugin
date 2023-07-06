package cn.mingbai.ScreenInMC.Utils;

import org.bukkit.scheduler.BukkitRunnable;

public abstract class ImmediatelyCancellableBukkitRunnable extends BukkitRunnable {
    private boolean cancelled = false;
    public ImmediatelyCancellableBukkitRunnable(){}

    public abstract void run();

    @Override
    public synchronized void cancel() throws IllegalStateException {
        cancelled=true;
        super.cancel();
    }

    @Override
    public boolean isCancelled() {
        if(cancelled||super.isCancelled()){
            return true;
        }else{
            return false;
        }
    }
}
