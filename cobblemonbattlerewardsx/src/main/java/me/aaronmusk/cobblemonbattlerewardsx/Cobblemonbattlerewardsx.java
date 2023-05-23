package me.aaronmusk.cobblemonbattlerewardsx;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonConfiguredFeatures;
import com.cobblemon.mod.common.CobblemonImplementation;
import com.cobblemon.mod.common.CobblemonNetwork;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.scheduling.ScheduledTask;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BattleCaptureAction;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.forge.client.CobblemonForgeClient;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import net.minecraft.server.MinecraftServer;


import java.util.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Cobblemonbattlerewardsx.MODID)
public class Cobblemonbattlerewardsx {

    public double moneymultiplier = 1.25;
    public int chancebattlekey = 1000;

    // Define mod id in a common place for everything to reference
    public static final String MODID = "cobblemonbattlerewardsx";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "cobblemonbattlerewardsx" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "cobblemonbattlerewardsx" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    // Creates a new Block with the id "cobblemonbattlerewardsx:example_block", combining the namespace and path
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of(Material.STONE)));
    // Creates a new BlockItem with the id "cobblemonbattlerewardsx:example_block", combining the namespace and path
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));


    public Cobblemonbattlerewardsx() {
        MinecraftForge.EVENT_BUS.register(this);
        /*IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();


        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);*/

        CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, event -> {
            if (event.getBattle().isPvW()) {
                List<ServerPlayer> playerWinners = event.getWinners().stream().filter(actor -> actor instanceof PlayerBattleActor)
                        .map(actor -> ((PlayerBattleActor) actor).getEntity())
                        .filter(Objects::nonNull).toList();
                for (ServerPlayer player : playerWinners) {
                    //player.sendSystemMessage(Component.literal("Test"));

                    MinecraftServer source = player.getServer();

                    List<Pokemon> pokemon = event.component2().get(0).getPokemonList().get(0).getFacedOpponents().stream().filter(battlePokemon -> battlePokemon instanceof BattlePokemon)
                            .map(battlePokemon -> ((BattlePokemon) battlePokemon).getOriginalPokemon())
                            .filter(Objects::nonNull).toList();

                    if (pokemon.get(0).getCurrentHealth() > 0) {
                        if (pokemon.get(0).isLegendary()) {

                            MiniMessage legendaryCatchMessage = MiniMessage.miniMessage();
                            String message = "<green>$Test</green>";
                            /*TextComponent legendaryCatchMessage = net.kyori.adventure.text.Component.text("[!]").color(NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true)
                                    .append(net.kyori.adventure.text.Component.text(String.valueOf(player.getName())).color(NamedTextColor.GREEN))
                                    .append(net.kyori.adventure.text.Component.text(" caught the legendary Pokemon ").color(NamedTextColor.YELLOW))
                                    .append(net.kyori.adventure.text.Component.text(String.valueOf(pokemon.get(0).getDisplayName())).color(NamedTextColor.GREEN))
                                    .append(net.kyori.adventure.text.Component.text("!").color(NamedTextColor.YELLOW));*/
                            sendGlobalMessage(source, legendaryCatchMessage.deserialize(message));
                        }
                        else if (pokemon.get(0).isUltraBeast()) {

                        }
                    }
                    else {
                        int levelpokemon = pokemon.get(0).getLevel();
                        int moneyreward = (int) (levelpokemon * moneymultiplier);
                        player.sendSystemMessage(Component.literal("You earned $" + moneyreward + " for defeating a level " + levelpokemon + " Pokemon."));

                        Random randI = new Random();
                        int chancenumber = randI.nextInt(chancebattlekey);

                        String playername = player.getName().getString();
                        // eco give {player} amount
                        // excellentcrates key give {player} battle_key
                        /*try {
                            source.getCommands().getDispatcher().execute("eco give " + playername + " " + moneyreward, source.createCommandSourceStack());
                            if (chancenumber == 1) {
                                player.sendSystemMessage(Component.literal("You found a battle crate key!"));
                                source.getCommands().getDispatcher().execute("excellentcrates key give " + playername + " battle_key", source.createCommandSourceStack());
                            }
                        } catch (CommandSyntaxException e) {
                            throw new RuntimeException(e);
                        }*/
                    }
                }
            }




                    //source.getCommands().performCommand(source.createCommandSourceStack(), "say hi");

                    /*player.sendSystemMessage(Component.literal(event.component2().toString()));
                    player.sendSystemMessage(Component.literal(event.component2().get(0).getPokemonList().toString()));*/
                    /*event.component1().getActivePokemon().forEach(pokemon -> {
                        assert pokemon.getBattlePokemon() != null;
                        player.sendSystemMessage(Component.literal(String.valueOf(pokemon.getBattlePokemon().getHealth())));
                    });*/
                    /*event.component1().getActivePokemon().forEach(pokemon -> {
                        assert pokemon.getBattlePokemon() != null;
                        player.sendSystemMessage(Component.literal(String.valueOf(pokemon.getBattlePokemon().getOriginalPokemon().getLevel())));
                    });*/




                    //player.sendSystemMessage(Component.literal(event.component2().get(1).getPokemonList().get(0).getFacedOpponents().));
                    //player.sendSystemMessage(Component.literal(String.valueOf(event.component1().getActivePokemon().)));
                    //player.sendSystemMessage(Component.literal(String.valueOf(event.component2().get(1).getPokemonList().get(0).getOriginalPokemon().getLevel())));

                    //int pokemon = Objects.requireNonNull(event.getBattle().getSide2().getActivePokemon().get(0).getBattlePokemon()).getOriginalPokemon().getLevel();

                    //player.sendSystemMessage(Component.literal(String.valueOf(pokemon)));

            /*if (event.getBattle().isPvW())
            {
                List<ServerPlayerEntity> playerWinners = event.getWinners().stream().filter(actor -> actor instanceof PlayerBattleActor)
                        .map(actor -> ((PlayerBattleActor) actor).getEntity())
                        .filter(Objects::nonNull).toList();
                for (ServerPlayerEntity player : playerWinners)
                {
                    player.sendMessage(Text.literal("Test"));
                }
            }*/
            return null;
        });
    }


    public void sendGlobalMessage(MinecraftServer server, net.kyori.adventure.text.Component message) {
        List<ServerPlayer> users = new ArrayList<>();
        PlayerList scm = server.getPlayerList();
        for (ServerPlayer entity : scm.getPlayers()) {
            if (entity != null) {
                users.add(entity);
            }
        }
        for (ServerPlayer p : users) {
            p.sendSystemMessage((Component) message);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    /*@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }*/
}
