package net.roguelogix.phosphophyllite.multiblock;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.roguelogix.phosphophyllite.multiblock.validated.IValidatedMultiblock;
import net.roguelogix.phosphophyllite.registry.OnModLoad;
import net.roguelogix.phosphophyllite.util.Util;

public final class MultiblockRegistry {
    
    private static final Object2ObjectOpenHashMap<ServerLevel, ObjectArrayList<MultiblockController<?, ?, ?>>> controllersToTick = new Object2ObjectOpenHashMap<>();
    private static final ObjectArrayList<MultiblockController<?, ?, ?>> newControllers = new ObjectArrayList<>();
    private static final ObjectArrayList<MultiblockController<?, ?, ?>> oldControllers = new ObjectArrayList<>();
    
    public static void addController(MultiblockController<?, ?, ?> controller) {
        newControllers.add(controller);
    }
    
    public static void removeController(MultiblockController<?, ?, ?> controller) {
        oldControllers.add(controller);
    }
    
    @OnModLoad
    private static void onModLoad() {
        NeoForge.EVENT_BUS.register(MultiblockRegistry.class);
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    static void onWorldUnload(final LevelEvent.Unload worldUnloadEvent) {
        if (!worldUnloadEvent.getLevel().isClientSide()) {
            //noinspection SuspiciousMethodCalls
            ObjectArrayList<MultiblockController<?, ?, ?>> controllersToTick = MultiblockRegistry.controllersToTick.remove(worldUnloadEvent.getLevel());
            if (controllersToTick != null) {
                for (MultiblockController<?, ?, ?> multiblockController : controllersToTick) {
                    multiblockController.suicide();
                }
            }
            // stragglers will exist
            newControllers.removeIf(multiblockController -> multiblockController.level == worldUnloadEvent.getLevel());
            oldControllers.removeIf(multiblockController -> multiblockController.level == worldUnloadEvent.getLevel());
        }
    }
    
    @SubscribeEvent
    static void onServerStop(final ServerStoppedEvent serverStoppedEvent) {
        controllersToTick.clear();
        newControllers.clear();
        oldControllers.clear();
    }
    
    @SubscribeEvent
    static void tickServer(ServerTickEvent.Pre e) {
        for (MultiblockController<?, ?, ?> newController : newControllers) {
            controllersToTick.computeIfAbsent((ServerLevel) newController.level, k -> new ObjectArrayList<>()).add(newController);
        }
        newControllers.clear();
        for (MultiblockController<?, ?, ?> oldController : oldControllers) {
            //noinspection SuspiciousMethodCalls
            var controllers = controllersToTick.get(oldController.level);
            controllers.remove(oldController);
        }
        oldControllers.clear();
    }
    
    @SubscribeEvent
    static void tickWorldStart(LevelTickEvent.Pre e) {
        if (!(e.getLevel() instanceof ServerLevel)) {
            return;
        }
        Util.updateBlockStates(e.getLevel());
    }
    
    @SubscribeEvent
    static void tickWorld(LevelTickEvent.Post e) {
        if (!(e.getLevel() instanceof ServerLevel)) {
            return;
        }
        Util.updateBlockStates(e.getLevel());
        
        var controllersToTick = MultiblockRegistry.controllersToTick.get(e.getLevel());
        if (controllersToTick != null) {
            for (var controller : controllersToTick) {
                if (controller != null) {
                    controller.update();
                }
            }
        }
    }
    
    public static void revalidateAll() {
        controllersToTick.forEach((serverLevel, multiblockControllers) -> multiblockControllers.forEach(controller -> {
            if (controller instanceof IValidatedMultiblock<?, ?, ?> validatedMultiblock) {
                validatedMultiblock.requestValidation();
            }
        }));
    }
}
