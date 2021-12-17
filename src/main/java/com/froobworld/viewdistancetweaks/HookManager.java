package com.froobworld.viewdistancetweaks;

import com.froobworld.viewdistancetweaks.hook.tick.PaperTickHook;
import com.froobworld.viewdistancetweaks.hook.tick.SpigotTickHook;
import com.froobworld.viewdistancetweaks.hook.tick.TickHook;
import com.froobworld.viewdistancetweaks.hook.viewdistance.*;
import com.froobworld.viewdistancetweaks.util.ChunkCounter;
import com.froobworld.viewdistancetweaks.util.NoTickChunkCounter;
import com.froobworld.viewdistancetweaks.util.PreferenceChooser;
import com.froobworld.viewdistancetweaks.util.StandardChunkCounter;

public class HookManager {
    private final ViewDistanceTweaks viewDistanceTweaks;
    private TickHook tickHook;
    private SimulationDistanceHook simulationDistanceHook;
    private ViewDistanceHook viewDistanceHook;
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

    public SimulationDistanceHook getSimulationDistanceHook() {
        return simulationDistanceHook;
    }

    public ViewDistanceHook getViewDistanceHook() {
        return viewDistanceHook;
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
        simulationDistanceHook = PreferenceChooser
                .bestChoice(PaperSimulationDistanceHook::new, PaperSimulationDistanceHook::isCompatible)
                .nextBestChoice(SpigotSimulationDistanceHook::new, SpigotSimulationDistanceHook::isCompatible)
                .get();
        if (simulationDistanceHook == null) {
            throw new IllegalStateException("No simulation distance hook is available. Incompatible version?");
        }
        viewDistanceTweaks.getLogger().info("Using " + simulationDistanceHook.getClass().getSimpleName() + " for the simulation distance hook.");

        viewDistanceHook = PreferenceChooser
                .bestChoice(PaperViewDistanceHook::new, PaperViewDistanceHook::isCompatible)
                .nextBestChoice(() -> new SpigotViewDistanceHook(viewDistanceTweaks.getClientViewDistanceManager()), SpigotViewDistanceHook::isCompatible)
                .get();
        viewDistanceTweaks.getLogger().info(viewDistanceHook != null ?
                "Using " + viewDistanceHook.getClass().getSimpleName() + " for the view distance hook." :
                "No hook available for view distance.");

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
                simulationDistanceHook,
                world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).chunkWeight.get(),
                world -> viewDistanceTweaks.getVdtConfig().worldSettings.of(world).chunkCounter.excludeOverlap.get()
        );
        actualChunkCounter = new StandardChunkCounter(
                world -> 1.0,
                world -> true
        );
        actualNoTickChunkCounter = new NoTickChunkCounter(
                simulationDistanceHook,
                world -> 1.0,
                world -> true
        );
    }

}
