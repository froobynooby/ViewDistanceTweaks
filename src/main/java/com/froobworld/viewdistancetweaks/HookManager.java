package com.froobworld.viewdistancetweaks;

import com.froobworld.viewdistancetweaks.hook.tick.PaperTickHook;
import com.froobworld.viewdistancetweaks.hook.tick.SpigotTickHook;
import com.froobworld.viewdistancetweaks.hook.tick.TickHook;
import com.froobworld.viewdistancetweaks.hook.viewdistance.PaperViewDistanceHook;
import com.froobworld.viewdistancetweaks.hook.viewdistance.SpigotViewDistanceHook;
import com.froobworld.viewdistancetweaks.hook.viewdistance.ViewDistanceHook;
import com.froobworld.viewdistancetweaks.hook.viewdistance.notick.NoTickViewDistanceHook;
import com.froobworld.viewdistancetweaks.hook.viewdistance.notick.PaperNoTickViewDistanceHook;
import com.froobworld.viewdistancetweaks.util.ChunkCounter;
import com.froobworld.viewdistancetweaks.util.NoTickChunkCounter;
import com.froobworld.viewdistancetweaks.util.PreferenceChooser;
import com.froobworld.viewdistancetweaks.util.StandardChunkCounter;

public class HookManager {
    private final ViewDistanceTweaks viewDistanceTweaks;
    private TickHook tickHook;
    private ViewDistanceHook viewDistanceHook;
    private NoTickViewDistanceHook noTickViewDistanceHook;
    private ChunkCounter chunkCounter;
    private ChunkCounter noTickChunkCounter;
    private ChunkCounter actualChunkCounter;
    private ChunkCounter actualNoTickChunkCounter;

    public HookManager(ViewDistanceTweaks viewDistanceTweaks) {
        this.viewDistanceTweaks = viewDistanceTweaks;
    }


    public TickHook getTickHook() {
        return tickHook;
    }

    public ViewDistanceHook getViewDistanceHook() {
        return viewDistanceHook;
    }

    public NoTickViewDistanceHook getNoTickViewDistanceHook() {
        return noTickViewDistanceHook;
    }

    public ChunkCounter getChunkCounter() {
        return chunkCounter;
    }

    public ChunkCounter getNoTickChunkCounter() {
        return noTickChunkCounter;
    }

    public ChunkCounter getActualChunkCounter() {
        return actualChunkCounter;
    }

    public ChunkCounter getActualNoTickChunkCounter() {
        return actualNoTickChunkCounter;
    }

    public void init() {
        viewDistanceHook = PreferenceChooser
                .bestChoice(PaperViewDistanceHook::new, PaperViewDistanceHook::isCompatible)
                .defaultChoice(SpigotViewDistanceHook::new);
        viewDistanceTweaks.getLogger().info("Using " + viewDistanceHook.getClass().getSimpleName() + " for the view distance hook.");

        noTickViewDistanceHook = PreferenceChooser
                .bestChoice(PaperNoTickViewDistanceHook::new, PaperNoTickViewDistanceHook::isCompatible)
                .get();
        viewDistanceTweaks.getLogger().info(noTickViewDistanceHook != null ?
                "Using " + noTickViewDistanceHook.getClass().getSimpleName() + " for the no-tick view distance hook." :
                "No hook available for no-tick view distance.");

        tickHook = PreferenceChooser
                .bestChoice(PaperTickHook::new, PaperTickHook::isCompatible)
                .defaultChoice(SpigotTickHook::new);
        tickHook.register(viewDistanceTweaks);
        viewDistanceTweaks.getLogger().info("Using " + tickHook.getClass().getSimpleName() + " for the tick hook.");

        chunkCounter = new StandardChunkCounter(
                world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).chunkWeight.get(),
                world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).chunkCounter.excludeOverlap.get()
        );
        noTickChunkCounter = new NoTickChunkCounter(
                viewDistanceHook,
                world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).chunkWeight.get(),
                world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).chunkCounter.excludeOverlap.get()
        );
        actualChunkCounter = new StandardChunkCounter(
                world -> 1.0,
                world -> true
        );
        actualNoTickChunkCounter = new NoTickChunkCounter(
                viewDistanceHook,
                world -> 1.0,
                world -> true
        );
    }

}
