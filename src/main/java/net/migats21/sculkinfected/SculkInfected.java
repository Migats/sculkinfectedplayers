package net.migats21.sculkinfected;

import com.mojang.logging.LogUtils;
import net.migats21.sculkinfected.client.ModSoundEvents;
import net.migats21.sculkinfected.network.PacketHandler;
import net.migats21.sculkinfected.server.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SculkInfected.MODID)
public class SculkInfected
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "sculkinfected";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public SculkInfected()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register ourselves for server and other game events we are interested in
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ModSoundEvents.register(modEventBus));
        Items.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }
    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(PacketHandler::register);
    }

}
