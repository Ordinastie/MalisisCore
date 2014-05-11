package net.malisis.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import cpw.mods.fml.client.FMLFileResourcePack;
import cpw.mods.fml.client.FMLFolderResourcePack;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import net.malisis.core.asm.MalisisCorePlugin;
import net.malisis.core.client.gui.renderer.DynamicTexture;
import net.malisis.core.client.gui.util.shape.Rectangle;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.util.Arrays;

public class MalisisCore extends DummyModContainer
{
    public static final String modid = "malisiscore";
    public static final String modname = "Malisis Core";
    public static final String version = "1.7.2-0.7";
    public static final String url = "http://github.com/Ordinastie/MalisisCore";

    public static MalisisCore instance;
    public static Debug debug;

    public static boolean isObfEnv = false;

    public MalisisCore()
    {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = modid;
        meta.name = modname;
        meta.version = version;
        meta.authorList = Arrays.asList("Ordinastie");
        meta.url = "";
        meta.description = "Add hooks";

        instance = this;
        // debug = new Debug();
    }

    public static void Message(Object text)
    {
        if (text != null)
        {
            ChatComponentText msg = new ChatComponentText(text.toString());
            MinecraftServer.getServer().getConfigurationManager().sendChatMsg(msg);
        }
    }

    @Subscribe
    public void preInit(FMLPreInitializationEvent event)
    {
        if (event.getSide() == Side.CLIENT)
        {
            if (debug != null)
                MinecraftForge.EVENT_BUS.register(debug);
        }
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
        bus.register(this);
        return true;
    }

    @Subscribe
    public void serverStart(FMLServerStartingEvent event)
    {
        MinecraftServer server = MinecraftServer.getServer();
        ((ServerCommandManager) server.getCommandManager()).registerCommand(new MalisisCommand());
    }

    @Override
    public File getSource()
    {
        return MalisisCorePlugin.source;
    }

    @Override
    public Class<?> getCustomResourcePackClass()
    {
        return getSource().isDirectory() ? FMLFolderResourcePack.class : FMLFileResourcePack.class;
    }
}
