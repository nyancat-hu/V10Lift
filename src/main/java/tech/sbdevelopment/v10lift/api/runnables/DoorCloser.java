package tech.sbdevelopment.v10lift.api.runnables;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import tech.sbdevelopment.v10lift.V10LiftPlugin;
import tech.sbdevelopment.v10lift.api.V10LiftAPI;
import tech.sbdevelopment.v10lift.managers.DataManager;

import java.util.function.Consumer;

/**
 * The DoorCloser runnable, used for checking if the door can be closed.
 */
public class DoorCloser implements Runnable , Consumer<ScheduledTask> {
    private final String liftName;
    private final ScheduledTask taskID;

    public DoorCloser(String liftName) {
        this.liftName = liftName;

        final long doorCloseTime = V10LiftPlugin.getSConfig().getFile().getLong("DoorCloseTime");

        this.taskID = Bukkit.getRegionScheduler().runAtFixedRate(V10LiftPlugin.getInstance(), DataManager.getLift(liftName).getBlocks().first().getCentorLocation(), this, doorCloseTime, doorCloseTime);
    }

    @Override
    public void run() {
        if (V10LiftAPI.getInstance().closeDoor(liftName)) stop();
    }

    public void stop() {
        taskID.cancel();
//        Bukkit.getScheduler().cancelTask(taskID);
        if (DataManager.containsLift(liftName)) DataManager.getLift(liftName).setDoorCloser(null);
    }

    @Override
    public void accept(ScheduledTask scheduledTask) {
        if (V10LiftAPI.getInstance().closeDoor(liftName)) stop();
    }
}
