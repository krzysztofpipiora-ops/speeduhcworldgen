package pl.grok.speeduhc;

import org.bukkit.*;
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
        getLogger().info("§aSpeedUHCWorldGen §f1.5 §a— custom flat UHC generator włączony!");
        getCommand("createuhc").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("createuhc")) return false;

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cTylko gracz może używać tej komendy!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUżycie: §f/createuhc <nazwa_świata>");
            return true;
        }

        String worldName = args[0];

        player.sendMessage("§6§lGenerowanie customowej mapy Speed UHC...");
        player.sendMessage("§7Płaski teren + pagórki (idealny do PVP chase)");

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    createCustomUHCWorld(player, worldName);
                } catch (Exception e) {
                    getLogger().severe("BŁĄD przy generowaniu: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.runTaskLater(this, 40L); // lekkie opóźnienie

        return true;
    }

    private void createCustomUHCWorld(Player player, String worldName) {
        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.type(WorldType.NORMAL);
        creator.generator(new FlatUHCGeneator());   // ← nasz custom generator
        creator.seed(System.currentTimeMillis());

        player.sendMessage("§eGenerowanie terenu... (może chwilę potrwać)");

        World world = creator.createWorld();

        if (world == null) {
            player.sendMessage("§cNie udało się stworzyć świata!");
            return;
        }

        setupUHCSettings(world);
        generateMid(world);
        generateLootChests(world, 22);

        player.sendMessage("§a§lMapa §f" + worldName + " §a§lgotowa!");
        player.sendMessage("§7• Płaski teren + pagórki");
        player.sendMessage("§7• Mid + 22 skrzynie z lootem");

        player.teleport(world.getSpawnLocation().add(0.5, 2, 0.5));
    }

    private void setupUHCSettings(World world) {
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(1000);
        border.setWarningDistance(25);
        border.setDamageAmount(1.0);

        world.setPVP(true);
        world.setDifficulty(Difficulty.NORMAL);
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
    }

    /** Custom generator – głównie płasko z pojedynczymi pagórkami */
    private class FlatUHCGeneator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random rand, int chunkX, int chunkZ, BiomeGrid biome) {
            ChunkData chunk = createChunkData(world);

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int worldX = chunkX * 16 + x;
                    int worldZ = chunkZ * 16 + z;

                    // Bazowa wysokość
                    int height = 64;

                    // Delikatne pagórki (pojedyncze, niskie)
                    double hill = Math.sin(worldX / 25.0) * 1.5 + Math.cos(worldZ / 30.0) * 1.8;
                    height += (int) hill;

                    // Rzadkie większe pagórki
                    if (random.nextInt(35) == 0) {
                        height += 2 + random.nextInt(3);
                    }

                    // Warstwy
                    chunk.setBlock(x, 0, z, Material.BEDROCK);
                    for (int y = 1; y < height - 4; y++) chunk.setBlock(x, y, z, Material.STONE);
                    for (int y = height - 4; y < height; y++) chunk.setBlock(x, y, z, Material.DIRT);
                    chunk.setBlock(x, height, z, Material.GRASS_BLOCK);

                    // Rzadkie kwiaty/trawa
                    if (random.nextInt(8) == 0) {
                        chunk.setBlock(x, height + 1, z, random.nextBoolean() ? Material.POPPY : Material.GRASS);
                    }

                    // Ustaw biome na Plains (płaski, otwarty)
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
            int x = random.nextInt(760) - 380;
            int z = random.nextInt(760) - 380;
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
        if (random.nextInt(4) == 0) inv.addItem(new ItemStack(Material.ARROW, 20));
    }
}
