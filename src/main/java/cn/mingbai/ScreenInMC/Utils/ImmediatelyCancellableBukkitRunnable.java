package cn.mingbai.ScreenInMC.Utils;

import org.bukkit.scheduler.BukkitRunnable;

public abstract class ImmediatelyCancellableBukkitRunnable extends BukkitRunnable {
    private boolean cancelled = false;
    public ImmediatelyCancellableBukkitRunnable(){}

    public abstract void run();

    @Override
    public synchronized void cancel(){
        cancelled=true;
        if(cancelled) return;
        try {
            super.cancel();
        }catch (Exception e){
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
