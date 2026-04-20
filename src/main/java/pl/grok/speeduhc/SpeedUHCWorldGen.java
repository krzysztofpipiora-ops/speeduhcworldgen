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
        getLogger().info("§aSpeedUHCWorldGen §f1.5-fix3 §a— czysta płaska mapa!");
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

        player.sendMessage("§6§lGenerowanie czystej płaskiej mapy Speed UHC...");
        player.sendMessage("§7Bez dirtowych słupów — tylko delikatne pagórki");

        new BukkitRunnable() {
            @Override
            public void run() {
                createCleanUHCWorld(player, worldName);
            }
        }.runTaskLater(this, 30L);

        return true;
    }

    private void createCleanUHCWorld(Player player, String worldName) {
        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.generator(new CleanFlatGenerator());
        creator.seed(System.currentTimeMillis());

        player.sendMessage("§eGenerowanie terenu...");

        World world = creator.createWorld();
        if (world == null) {
            player.sendMessage("§cNie udało się stworzyć świata!");
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

    /** NAPRAWIONY generator — zero słupów */
    private class CleanFlatGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random rand, int chunkX, int chunkZ, BiomeGrid biome) {
            ChunkData chunk = createChunkData(world);

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int wx = chunkX * 16 + x;
                    int wz = chunkZ * 16 + z;

                    // Bazowa wysokość
                    int height = 64 
                        + (int)(Math.sin(wx / 40.0) * 1.8) 
                        + (int)(Math.cos(wz / 35.0) * 1.6);

                    // Bardzo rzadkie małe pagórki
                    if (random.nextInt(50) == 0) {
                        height += random.nextInt(3) + 1;
                    }

                    // Warstwy
                    chunk.setBlock(x, 0, z, Material.BEDROCK);
                    for (int y = 1; y < height - 3; y++) {
                        chunk.setBlock(x, y, z, Material.STONE);
                    }
                    for (int y = height - 3; y < height; y++) {
                        chunk.setBlock(x, y, z, Material.DIRT);
                    }

                    // Poprawnie ustawiamy trawę na wysokości "height"
                    chunk.setBlock(x, height, z, Material.GRASS_BLOCK);

                    // Trawa i kwiaty nad ziemią
                    if (random.nextInt(7) == 0) {
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
