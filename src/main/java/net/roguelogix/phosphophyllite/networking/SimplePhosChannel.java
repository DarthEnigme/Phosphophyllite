package net.roguelogix.phosphophyllite.networking;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.roguelogix.phosphophyllite.Phosphophyllite;
import net.roguelogix.phosphophyllite.registry.OnModLoad;
import net.roguelogix.phosphophyllite.robn.ROBN;
import net.roguelogix.phosphophyllite.serialization.PhosphophylliteCompound;
import net.roguelogix.phosphophyllite.util.NonnullDefault;
import net.roguelogix.phosphophyllite.util.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

@NonnullDefault
public class SimplePhosChannel {
    
    private static final List<Pair<CustomPacketPayload.Type<CompoundPacket>, IPayloadHandler<CompoundPacket>>> channelsToRegister = new ObjectArrayList<>();
    public static void register(final RegisterPayloadHandlersEvent event) {
        for (var pair : channelsToRegister) {
            final var type = pair.first();
            final StreamCodec<FriendlyByteBuf, CompoundPacket> codec = StreamCodec.of(
                    (buf, packet) -> buf.writeByteArray(ROBN.toROBN(packet.compound).toByteArray()),
                    (buf) -> new CompoundPacket((PhosphophylliteCompound) ROBN.fromROBN(ByteArrayList.wrap(buf.readByteArray())), type)
            );
            event.registrar(type.id().getNamespace()).playBidirectional(type, codec, pair.second());
        }
    }
    
    @OnModLoad
    private static void onModLoad() {
        ModLoadingContext.get().getActiveContainer().getEventBus().addListener(SimplePhosChannel::register);
    }

    private final CustomPacketPayload.Type<CompoundPacket> type;
    @Nullable
    private final BiConsumer<PhosphophylliteCompound, IPayloadContext> clientCallback;
    @Nullable
    private final BiConsumer<PhosphophylliteCompound, IPayloadContext> serverCallback;
    
    public SimplePhosChannel(ResourceLocation id, @Nullable BiConsumer<PhosphophylliteCompound, IPayloadContext> clientCallback, @Nullable BiConsumer<PhosphophylliteCompound, IPayloadContext> serverCallback) {
        this.type = new CustomPacketPayload.Type<>(id);
        channelsToRegister.add(new Pair<>(type, this::handler));
        this.clientCallback = clientCallback;
        this.serverCallback = serverCallback;
    }
    
    private void handler(CompoundPacket compoundPacket, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            @Nullable final var callbackToUse = ctx.flow().isClientbound() ? clientCallback : serverCallback;
            if (callbackToUse != null) {
                callbackToUse.accept(compoundPacket.compound, ctx);
            }
        });
    }
    
    public void sendToServer(PhosphophylliteCompound compound) {
        PacketDistributor.sendToServer(new CompoundPacket(compound, type));
    }
    
    public void sendToPlayer(ServerPlayer serverPlayer, PhosphophylliteCompound compound) {
        PacketDistributor.sendToPlayer(serverPlayer, new CompoundPacket(compound, type));
    }
    
    public void forceClassLoad() {
    }
    
    private static class CompoundPacket implements CustomPacketPayload {
        
        public final PhosphophylliteCompound compound;
        public final Type<CompoundPacket> type;
        
        private CompoundPacket(PhosphophylliteCompound compound, Type<CompoundPacket> type) {
            this.compound = compound;
            this.type = type;
        }
        
        @Override
        public Type<CompoundPacket> type() {
            return type;
        }
    }
}
