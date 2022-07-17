package net.migats21.sculkinfected.network;

import net.migats21.sculkinfected.SculkInfected;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(SculkInfected.MODID, "main"), () -> PROTOCOL_VERSION, (protocol_version) -> PROTOCOL_VERSION.equals(protocol_version) || NetworkRegistry.ACCEPTVANILLA.equals(protocol_version) || NetworkRegistry.ABSENT.equals(protocol_version), (protocol_version) -> PROTOCOL_VERSION.equals(protocol_version) || NetworkRegistry.ACCEPTVANILLA.equals(protocol_version) || NetworkRegistry.ABSENT.equals(protocol_version));

    private PacketHandler() {

    }

    public static void register() {
        int i = 0;
        CHANNEL.messageBuilder(ClientboundInfectionUpdatePacket.class, i++, NetworkDirection.PLAY_TO_CLIENT).encoder(ClientboundInfectionUpdatePacket::write).decoder(ClientboundInfectionUpdatePacket::new).consumerNetworkThread(ClientboundInfectionUpdatePacket::handle).add();
    }

}
