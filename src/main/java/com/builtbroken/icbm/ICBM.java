package com.builtbroken.icbm;

import com.builtbroken.icbm.client.ICBMCreativeTab;
import com.builtbroken.icbm.content.blast.effect.ExAntiPlant;
import com.builtbroken.icbm.content.blast.effect.ExEnderBlocks;
import com.builtbroken.icbm.content.blast.effect.ExTorchEater;
import com.builtbroken.icbm.content.blast.entity.ExplosiveHandlerSpawn;
import com.builtbroken.icbm.content.blast.explosive.BlastPathTester;
import com.builtbroken.icbm.content.blast.explosive.ExAntimatter;
import com.builtbroken.icbm.content.blast.explosive.ExMicroQuake;
import com.builtbroken.icbm.content.blast.fire.ExFireBomb;
import com.builtbroken.icbm.content.blast.fire.ExFlashFire;
import com.builtbroken.icbm.content.blast.fragment.ExFragment;
import com.builtbroken.icbm.content.blast.temp.ExEndoThermic;
import com.builtbroken.icbm.content.blast.temp.ExExoThermic;
import com.builtbroken.icbm.content.blast.thaum.ThaumBlastLoader;
import com.builtbroken.icbm.content.blast.util.ExRegen;
import com.builtbroken.icbm.content.blast.util.ExRegenLocal;
import com.builtbroken.icbm.content.crafting.missile.casing.MissileCasings;
import com.builtbroken.icbm.content.crafting.missile.engine.Engines;
import com.builtbroken.icbm.content.crafting.missile.engine.ItemEngineModules;
import com.builtbroken.icbm.content.crafting.missile.guidance.GuidanceModules;
import com.builtbroken.icbm.content.crafting.missile.guidance.ItemGuidanceModules;
import com.builtbroken.icbm.content.crafting.missile.warhead.WarheadCasings;
import com.builtbroken.icbm.content.crafting.parts.ItemExplosive;
import com.builtbroken.icbm.content.crafting.parts.ItemExplosiveParts;
import com.builtbroken.icbm.content.crafting.parts.ItemMissileParts;
import com.builtbroken.icbm.content.crafting.parts.MissileCraftingParts;
import com.builtbroken.icbm.content.debug.BlockExplosiveMarker;
import com.builtbroken.icbm.content.debug.TileRotationTest;
import com.builtbroken.icbm.content.display.TileMissile;
import com.builtbroken.icbm.content.display.TileMissileDisplay;
import com.builtbroken.icbm.content.launcher.block.BlockLaunchPad;
import com.builtbroken.icbm.content.launcher.block.BlockLauncherFrame;
import com.builtbroken.icbm.content.launcher.block.BlockLauncherPart;
import com.builtbroken.icbm.content.launcher.controller.TileController;
import com.builtbroken.icbm.content.launcher.items.ItemGPSFlag;
import com.builtbroken.icbm.content.launcher.items.ItemLinkTool;
import com.builtbroken.icbm.content.launcher.launcher.large.TileLargeLauncher;
import com.builtbroken.icbm.content.launcher.launcher.medium.TileMediumLauncher;
import com.builtbroken.icbm.content.launcher.launcher.small.TileSmallLauncher;
import com.builtbroken.icbm.content.launcher.silo.TileSmallSilo;
import com.builtbroken.icbm.content.missile.EntityMissile;
import com.builtbroken.icbm.content.missile.ItemMissile;
import com.builtbroken.icbm.content.missile.tracking.MissileTracker;
import com.builtbroken.icbm.content.rocketlauncher.ItemRocketLauncher;
import com.builtbroken.icbm.content.warhead.TileWarhead;
import com.builtbroken.icbm.server.CommandICBM;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.content.resources.items.ItemSheetMetal;
import com.builtbroken.mc.lib.mod.AbstractMod;
import com.builtbroken.mc.lib.mod.ModCreativeTab;
import com.builtbroken.mc.lib.mod.compat.nei.NEIProxy;
import com.builtbroken.mc.lib.world.explosive.ExplosiveRegistry;
import com.builtbroken.mc.prefab.explosive.ExplosiveHandler;
import com.builtbroken.mc.prefab.recipe.item.sheetmetal.RecipeSheetMetal;
import com.builtbroken.mc.prefab.tile.item.ItemBlockMetadata;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;

/**
 * Main Mod class for ICBM, Loads up everything needs when called by FML/Forge
 *
 * @author DarkGuardsman, [Original Author Calclavia]
 */
@Mod(modid = ICBM.DOMAIN, name = ICBM.NAME, version = ICBM.VERSION, dependencies = ICBM.DEPENDENCIES)
public final class ICBM extends AbstractMod
{
    //Meta
    public static final String NAME = "ICBM";
    public static final String DOMAIN = "icbm";
    public static final String PREFIX = DOMAIN + ":";

    // Version numbers
    public static final String MAJOR_VERSION = "@MAJOR@";
    public static final String MINOR_VERSION = "@MINOR@";
    public static final String REVISION_VERSION = "@REVIS@";
    public static final String BUILD_VERSION = "@BUILD@";
    public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION + "." + BUILD_VERSION;
    //http://www.minecraftforge.net/wiki/Developing_Addons_for_Existing_Mods
    public static final String DEPENDENCIES = "required-after:VoltzEngine";


    @Instance(DOMAIN)
    public static ICBM INSTANCE;

    @SidedProxy(clientSide = "com.builtbroken.icbm.client.ClientProxy", serverSide = "com.builtbroken.icbm.server.ServerProxy")
    public static CommonProxy proxy;


    public static boolean ANTIMATTER_BREAK_UNBREAKABLE = true;
    public static boolean DEBUG_MISSILE_MANAGER = false;

    public static float missile_firing_volume = 1f;

    public static int ENTITY_ID_PREFIX = 50;

    // Blocks
    public static Block blockWarhead;
    public static Block blockExplosiveMarker;
    public static Block blockMissileDisplay;
    public static Block blockMissile;

    public static Block blockSiloController;
    public static Block blockMissileWorkstation;

    public static Block blockSmallPortableLauncher;

    public static Block blockStandardLauncher;
    public static Block blockMediumLauncher;
    public static Block blockLargeLauncher;

    public static Block blockSmallSilo;

    public static Block blockLauncherFrame;
    public static Block blockLauncherParts;
    public static Block blockLaunchPad;

    // Items
    public static Item itemMissile;
    public static Item itemRocketLauncher;
    public static Item itemLinkTool;
    public static Item itemGPSTool;
    public static ItemEngineModules itemEngineModules;
    public static ItemGuidanceModules itemGuidanceModules;
    public static Item itemMissileParts;
    public static Item itemExplosive;
    public static Item itemExplosivePart;

    public final ModCreativeTab CREATIVE_TAB;

    private static boolean registerExplosives;

    public ICBM()
    {
        super(DOMAIN, "ICBM");
        CREATIVE_TAB = new ICBMCreativeTab();
        super.manager.setTab(CREATIVE_TAB);
    }


    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(proxy);
        FMLCommonHandler.instance().bus().register(this);

        //Request Engine to load items for use
        Engine.heatedRockRequested = true;
        Engine.requestOres();
        Engine.requestResources();
        Engine.requestSheetMetalContent();
        Engine.requestMultiBlock();
        Engine.requestSimpleTools();
        Engine.requestCircuits();

        //Loads thaumcraft support
        if (Loader.isModLoaded("Thaumcraft") && !getConfig().getBoolean("DisableThaumSupport", "ModSupport", false, "Allows disabling thaumcraft support, if issues arise or game play balance is required."))
        {
            loader.applyModule(ThaumBlastLoader.class);
        }

        // Configs TODO load up using config system, and separate file
        ANTIMATTER_BREAK_UNBREAKABLE = getConfig().getBoolean("Antimatter_Destroy_Unbreakable", Configuration.CATEGORY_GENERAL, true, "Allows antimatter to break blocks that are unbreakable, bedrock for example.");
        DEBUG_MISSILE_MANAGER = getConfig().getBoolean("Missile_Manager", "Debug", Engine.runningAsDev, "Adds additional info to the console");
        missile_firing_volume = getConfig().getFloat("missile_firing_volume", "volume", 1.0F, 0, 4, "How loud the missile is when fired from launchers");


        // Functional Blocks
        blockWarhead = manager.newBlock(TileWarhead.class);
        blockMissileDisplay = manager.newBlock(TileMissileDisplay.class);
        blockLauncherFrame = manager.newBlock("icbmLauncherFrame", BlockLauncherFrame.class, ItemBlockMetadata.class);
        blockLauncherParts = manager.newBlock("icbmLauncherParts", BlockLauncherPart.class, ItemBlockMetadata.class);

        //Decor Blocks
        blockLaunchPad = manager.newBlock("icbmDecorLaunchPad", BlockLaunchPad.class, ItemBlockMetadata.class);

        //Launchers
        blockSmallPortableLauncher = manager.newBlock(TileSmallLauncher.class);
        blockSmallSilo = manager.newBlock(TileSmallSilo.class);
        blockMediumLauncher = manager.newBlock(TileMediumLauncher.class);
        blockLargeLauncher = manager.newBlock(TileLargeLauncher.class);

        //Clear launcher creative tab to prevent placement by user by mistake

        blockMediumLauncher.setCreativeTab(null);
        blockLargeLauncher.setCreativeTab(null);
        NEIProxy.hideItem(blockMediumLauncher);
        NEIProxy.hideItem(blockLargeLauncher);

        //Missile workstation is loaded in the proxy
        blockSiloController = manager.newBlock("SiloController", TileController.class);

        // Decor Blocks
        blockMissile = manager.newBlock(TileMissile.class);

        // Debug Only blocks
        if (Engine.runningAsDev)
        {
            blockExplosiveMarker = manager.newBlock(BlockExplosiveMarker.class, ItemBlockMetadata.class);
            manager.newBlock(TileRotationTest.class);
        }

        // ITEMS
        itemMissile = manager.newItem("missile", ItemMissile.class);
        itemRocketLauncher = manager.newItem("rocketLauncher", ItemRocketLauncher.class);
        itemEngineModules = manager.newItem("engineModules", ItemEngineModules.class);
        itemGuidanceModules = manager.newItem("guidanceModules", ItemGuidanceModules.class);
        itemLinkTool = manager.newItem("siloLinker", ItemLinkTool.class);
        itemGPSTool = manager.newItem("gpsFlag", ItemGPSFlag.class);
        itemMissileParts = manager.newItem("missileParts", ItemMissileParts.class);
        itemExplosive = manager.newItem("explosiveUnit", ItemExplosive.class);
        itemExplosivePart = manager.newItem("explosiveUnitParts", ItemExplosiveParts.class);
        NEIProxy.hideItem(ItemExplosive.ExplosiveItems.NBT.newItem());

        // Register modules, need to do this or they will not build from ItemStacks
        MissileCasings.register();
        WarheadCasings.register();
        Engines.register();
        GuidanceModules.register();

        //Set tab item last so to avoid NPE
        CREATIVE_TAB.itemStack = MissileCasings.SMALL.newModuleStack();
        getProxy().registerExplosives();

    }

    /**
     * Registers the explosives. Under normal runtime never call
     * this method outside of the ICBM.class.
     */
    public static void registerExplosives()
    {
        if (!registerExplosives)
        {
            registerExplosives = true;
            //Create Explosives
            ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "EntitySpawn", new ExplosiveHandlerSpawn());
            ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "ExoThermic", new ExEndoThermic());
            ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "EndoThermic", new ExExoThermic());
            ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "Fragment", new ExFragment());
            ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "ArrowFragment", ExplosiveRegistry.get("Fragment"));
            ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "Antimatter", new ExAntimatter());
            ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "FireBomb", new ExFireBomb());
            ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "FlashFire", new ExFlashFire());
            ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "EnderBlocks", new ExEnderBlocks());
            ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "TorchEater", new ExTorchEater());
            ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "AntiPlant", new ExAntiPlant());
            ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "Regen", new ExRegen());
            ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "RegenLocal", new ExRegenLocal());
            ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "MicroQuake", new ExMicroQuake());
            if (Engine.runningAsDev)
            {
                ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "SimplePathTest1", new ExplosiveHandler("SimplePathTest1", BlastPathTester.class, 1));
                ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "SimplePathTest2", new ExplosiveHandler("SimplePathTest2", BlastPathTester.class, 2));
                ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "SimplePathTest3", new ExplosiveHandler("SimplePathTest3", BlastPathTester.class, 3));
                ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "SimplePathTest10", new ExplosiveHandler("SimplePathTest10", BlastPathTester.class, 10));
                ExplosiveRegistry.registerOrGetExplosive(DOMAIN, "SimplePathTest20", new ExplosiveHandler("SimplePathTest20", BlastPathTester.class, 20));
            }
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        super.init(event);

        //Register Entities
        EntityRegistry.registerGlobalEntityID(EntityMissile.class, "ICBMMissile", EntityRegistry.findGlobalUniqueEntityId());
        EntityRegistry.registerModEntity(EntityMissile.class, "ICBMMissile", ENTITY_ID_PREFIX + 3, this, 500, 1, true);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
        /** LOAD. */
        ArrayList dustCharcoal = OreDictionary.getOres("dustCharcoal");
        ArrayList dustCoal = OreDictionary.getOres("dustCoal");
        // Sulfur
        if (getConfig().getBoolean("Charcoal_gunpowder_recipe", "Extra", true, "Enables a dust recipe of sulfur, saltpeter, and charcoal to make gunpowder"))
        {
            //TODO see about ore dictionary for coal
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.gunpowder, 2), "dustSulfur", "dustSaltpeter", Items.coal));
        }
        if (getConfig().getBoolean("Charcoal_gunpowder_recipe", "Extra", true, "Enables a recipe of sulfur, saltpeter, and charcoal to make gunpowder"))
        {
            //TODO see about ore dictionary for charcoal
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.gunpowder, 2), "dustSulfur", "dustSaltpeter", new ItemStack(Items.coal, 1, 1)));
        }
        if (getConfig().getBoolean("Charcoal_dust_gunpowder_recipe", "Extra", true, "Enables a recipe of sulfur, saltpeter, and charcoal to make gunpowder") && dustCharcoal != null && dustCharcoal.size() > 0)
        {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.gunpowder, 2), "dustSulfur", "dustSaltpeter", "dustCharcoal"));
        }
        if (getConfig().getBoolean("Coal_dust_gunpowder_recipe", "Extra", true, "Enables a dust recipe of sulfur, saltpeter, and coal to make gunpowder") && dustCoal != null && dustCoal.size() > 0)
        {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.gunpowder, 2), "dustSulfur", "dustSaltpeter", "dustCoal"));
        }
        if (getConfig().getBoolean("Redstone_TNT_recipe", "Extra", true, "Enables a cheaper/easier tnt recipe using gunpowder surrounding a redstone dust. This recipe is designed to make it easier to craft tnt without mining a lot of material."))
        {
            //TODO see about ore dictionary for redstone, and gunpowder
            GameRegistry.addRecipe(new ShapedOreRecipe(Blocks.tnt, "@@@", "@R@", "@@@", '@', Items.gunpowder, 'R', Items.redstone));
        }

        //Sheet metal crafting recipes
        if (Engine.itemSheetMetal != null && Engine.itemSheetMetalTools != null)
        {
            GameRegistry.addRecipe(new RecipeSheetMetal(MissileCraftingParts.SMALL_MISSILE_CASE.stack(), "CRC", " H ", 'C', ItemSheetMetal.SheetMetal.CYLINDER.stack(), 'R', ItemSheetMetal.SheetMetal.RIVETS.stack(), 'H', Engine.itemSheetMetalTools.getHammer()));
        }
        else
        {
            logger().error("In order to craft the missile casing you need to enable sheet metal tools and parts in Voltz Engine");
        }
    }

    @Override
    public CommonProxy getProxy()
    {
        return proxy;
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        // Setup command
        ICommandManager commandManager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
        ServerCommandManager serverCommandManager = ((ServerCommandManager) commandManager);
        serverCommandManager.registerCommand(new CommandICBM());

    }

    @SubscribeEvent
    public void onWorldTickEnd(TickEvent.WorldTickEvent evt)
    {
        if (evt.side == Side.SERVER && evt.phase == TickEvent.Phase.END)
        {
            MissileTracker tracker = MissileTracker.getTrackerForWorld(evt.world);
            if (tracker != null)
            {
                tracker.update(evt.world);
            }
        }
    }
}
