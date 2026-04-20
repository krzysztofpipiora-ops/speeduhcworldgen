package pl.grok.speeduhc;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class SpeedUHCWorldGen extends JavaPlugin {

    private final Random random = new Random();

    @Override
    public void onEnable() {
        getLogger().info("§aSpeedUHCWorldGen §f1.6 §a— gładkie pagórki!");
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
            player.sendMessage("§cUżycie: §f/createuhc <nazwa_świata>");
            return true;
        }

        String worldName = args[0];

        player.sendMessage("§6§lGenerowanie mapy z naturalnymi pagórkami...");
        player.sendMessage("§7Płaski teren + gładkie, naturalne wzniesienia");

        new BukkitRunnable() {
            @Override
            public void run() {
                createSmoothUHCWorld(player, worldName);
            }
        }.runTaskLater(this, 30L);

        return true;
    }

    private void createSmoothUHCWorld(Player player, String worldName) {
        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.generator(new SmoothFlatGenerator());
        creator.seed(System.currentTimeMillis());

        player.sendMessage("§eGenerowanie terenu...");

        World world = creator.createWorld();
        if (world == null) {
            player.sendMessage("§cBłąd tworzenia świata!");
            return;
        }

        setupUHCSettings(world);
        generateMid(world);
        generateLootChests(world, 20);

        player.sendMessage("§a§lMapa §f" + worldName + " §a§lgotowa!");
        player.teleport(world.getSpawnLocation().add(0.5, 2, 0.5));
    }

    private void setupUHCSettings(World world) {
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(1000);

        world.setPVP(true);
        world.setDifficulty(Difficulty.NORMAL);
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, false);
    }

    /** Lepszy generator — gładkie, naturalne pagórki */
    private class SmoothFlatGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random rand, int chunkX, int chunkZ, BiomeGrid biome) {
            ChunkData chunk = createChunkData(world);

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int wx = chunkX * 16 + x;
                    int wz = chunkZ * 16 + z;

                    // Bardziej naturalna funkcja wysokości
                    double heightNoise = 
                        Math.sin(wx / 45.0) * 2.5 +
                        Math.cos(wz / 38.0) * 2.2 +
                        Math.sin(wx / 12.0 + wz / 15.0) * 0.8;

                    int height = 63 + (int) heightNoise;

                    // Bardzo rzadkie większe pagórki
                    if ((wx % 47 == 0) && (wz % 53 == 0)) {
                        height += 3 + random.nextInt(4);
                    }

                    // Warstwy terenu
                    chunk.setBlock(x, 0, z, Material.BEDROCK);
                    for (int y = 1; y < height - 3; y++) chunk.setBlock(x, y, z, Material.STONE);
                    for (int y = height - 3; y < height; y++) chunk.setBlock(x, y, z, Material.DIRT);

                    chunk.setBlock(x, height, z, Material.GRASS_BLOCK);

                    // Roślinność
                    if (random.nextInt(6) == 0) {
                        chunk.setBlock(x, height + 1, z, random.nextBoolean() ? Material.SHORT_GRASS : Material.POPPY);
                    }

                    biome.setBiome(x, z, Biome.PLAINS);
                }
            }
            return chunk;
        }
    }

    private void generateMid(World world) {
        int y = 72;
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                Block b = world.getBlockAt(x, y, z);
                b.setType(Math.abs(x) == 5 || Math.abs(z) == 5 ? Material.OBSIDIAN : Material.DIAMOND_BLOCK);
            }
        }
        world.setSpawnLocation(0, y + 2, 0);
    }

    private void generateLootChests(World world, int amount) {
        for (int i = 0; i < amount; i++) {
            int x = random.nextInt(700) - 350;
            int z = random.nextInt(700) - 350;
            int y = world.getHighestBlockYAt(x, z) + 1;

            Block block = world.getBlockAt(x, y, z);
            block.setType(Material.CHEST);

            if (block.getState() instanceof Chest chest) {
                fillUHCChest(chest.getBlockInventory());
            }
        }
    }

    private void fillUHCChest(org.bukkit.inventory.Inventory inv) {
        inv.addItem(new ItemStack(Material.COOKED_BEEF, 8 + random.nextInt(8)));
        inv.addItem(new ItemStack(Material.GOLDEN_APPLE, 1 + random.nextInt(3)));
        inv.addItem(new ItemStack(Material.IRON_INGOT, 8 + random.nextInt(12)));
        inv.addItem(new ItemStack(Material.DIAMOND, 2 + random.nextInt(3)));
        if (random.nextBoolean()) inv.addItem(new ItemStack(Material.IRON_SWORD));
        if (random.nextInt(3) == 0) inv.addItem(new ItemStack(Material.BOW));
    }
}
