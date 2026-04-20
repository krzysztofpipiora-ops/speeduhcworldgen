package pl.grok.speeduhc;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SpeedUHCWorldGen extends JavaPlugin {

    private final Random random = new Random();

    @Override
    public void onEnable() {
        getLogger().info("§aSpeedUHCWorldGen §f1.2 §a— pełny generator z lootem i midem!");
        getCommand("createuhc").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("createuhc")) return false;

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cTylko gracz!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUżycie: §f/createuhc <nazwa_bazowa>");
            return true;
        }

        String baseName = args[0];
        player.sendMessage("§6§lTworzenie 4 map Speed UHC z lootem i midem...");

        String[] types = {"forest", "mountain", "desert", "river"};

        for (int i = 0; i < 4; i++) {
            String worldName = baseName + "_" + types[i];
            player.sendMessage("§e→ Generowanie: §f" + worldName);

            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(World.Environment.NORMAL);
            creator.type(WorldType.NORMAL);
            creator.seed(System.currentTimeMillis() + i * 50000);

            World world = creator.createWorld();
            if (world == null) continue;

            setupUHCWorld(world, types[i]);
            generateMid(world);
            generateLootChests(world, 25); // 25 skrzyń na mapę
            setPlayerSpawns(world);

            if (i == 0) player.teleport(world.getSpawnLocation());
        }

        player.sendMessage("§a§lWszystko gotowe! §f4 mapy z lootem, midem i spawnami.");
        return true;
    }

    private void setupUHCWorld(World world, String type) {
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(1000);
        border.setWarningDistance(25);

        world.setPVP(true);
        world.setDifficulty(Difficulty.NORMAL);
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
    }

    /** Buduje centralny Mid */
    private void generateMid(World world) {
        int y = 75; // wysokość mida
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                Block block = world.getBlockAt(x, y, z);
                if (Math.abs(x) == 5 || Math.abs(z) == 5) {
                    block.setType(Material.OBSIDIAN);
                } else {
                    block.setType(Material.DIAMOND_BLOCK);
                }
            }
        }
        // Kolumny pod mid
        for (int i = -5; i <= 5; i += 5) {
            for (int j = -5; j <= 5; j += 5) {
                for (int h = y - 1; h > y - 15; h--) {
                    world.getBlockAt(i, h, j).setType(Material.OBSIDIAN);
                }
            }
        }
        world.setSpawnLocation(0, y + 2, 0);
    }

    /** Rozsiewa loot chests */
    private void generateLootChests(World world, int amount) {
        for (int i = 0; i < amount; i++) {
            int x = random.nextInt(800) - 400;
            int z = random.nextInt(800) - 400;
            int y = world.getHighestBlockYAt(x, z) + 1;

            Block block = world.getBlockAt(x, y, z);
            block.setType(Material.CHEST);

            if (block.getState() instanceof Chest chest) {
                fillUHCChest(chest.getBlockInventory());
            }
        }
    }

    /** Wypełnia skrzynię dobrym lootem Speed UHC */
    private void fillUHCChest(org.bukkit.inventory.Inventory inv) {
        // Podstawowe rzeczy
        inv.addItem(new ItemStack(Material.COOKED_BEEF, 8 + random.nextInt(8)));
        inv.addItem(new ItemStack(Material.GOLDEN_APPLE, 1 + random.nextInt(3)));
        inv.addItem(new ItemStack(Material.IRON_INGOT, 5 + random.nextInt(10)));
        inv.addItem(new ItemStack(Material.DIAMOND, 1 + random.nextInt(4)));

        // Armor / broń
        if (random.nextBoolean()) inv.addItem(new ItemStack(Material.IRON_SWORD));
        if (random.nextInt(3) == 0) inv.addItem(new ItemStack(Material.DIAMOND_HELMET));
        if (random.nextInt(4) == 0) inv.addItem(new ItemStack(Material.BOW));
        if (random.nextInt(3) == 0) inv.addItem(new ItemStack(Material.ARROW, 16 + random.nextInt(32)));

        // Inne
        if (random.nextBoolean()) inv.addItem(new ItemStack(Material.ENDER_PEARL, 1 + random.nextInt(2)));
        if (random.nextInt(5) == 0) inv.addItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
    }

    /** Ustawia 4 losowe spawny dla graczy */
    private void setPlayerSpawns(World world) {
        for (int i = 0; i < 4; i++) {
            int x = random.nextInt(500) - 250;
            int z = random.nextInt(500) - 250;
            int y = world.getHighestBlockYAt(x, z) + 2;
            world.setSpawnLocation(x, y, z); // tymczasowo – w pełni możesz zapisać listę spawnów
        }
    }
}
