package pl.grok.speeduhc;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class SpeedUHCWorldGen extends JavaPlugin {

    private final Random random = new Random();

    @Override
    public void onEnable() {
        getLogger().info("§aSpeedUHCWorldGen §f1.4 §a— uruchomiony!");
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
            player.sendMessage("§cUżycie: §f/createuhc <nazwa> [typ]");
            player.sendMessage("§7Typy: forest, mountain, desert, river");
            return true;
        }

        String baseName = args[0];
        String type = args.length > 1 ? args[1].toLowerCase() : "forest";

        player.sendMessage("§6§lRozpoczynam generowanie mapy §e" + baseName + "_" + type);
        getLogger().info("[SpeedUHC] Gracz " + player.getName() + " zaczął tworzyć świat: " + baseName + "_" + type);

        // Tworzenie w głównym wątku z opóźnieniem (bezpieczniej)
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    createWorldSync(player, baseName, type);
                } catch (Exception e) {
                    getLogger().severe("[SpeedUHC] BŁĄD: " + e.getMessage());
                    e.printStackTrace();
                    Bukkit.getScheduler().runTask(SpeedUHCWorldGen.this, () ->
                        player.sendMessage("§cBłąd podczas generowania: " + e.getMessage())
                    );
                }
            }
        }.runTaskLater(this, 20L); // 1 sekunda opóźnienia

        return true;
    }

    private void createWorldSync(Player player, String baseName, String type) {
        String worldName = baseName + "_" + type;
        getLogger().info("[SpeedUHC] Tworzenie świata: " + worldName);

        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.type(WorldType.NORMAL);
        creator.seed(System.currentTimeMillis());

        player.sendMessage("§eGenerowanie terenu... (to może zająć 10-30 sekund)");

        World world = creator.createWorld();

        if (world == null) {
            player.sendMessage("§cNie udało się stworzyć świata!");
            getLogger().severe("[SpeedUHC] createWorld() zwróciło null!");
            return;
        }

        getLogger().info("[SpeedUHC] Świat " + worldName + " stworzony pomyślnie!");

        setupUHCWorld(world);
        generateMid(world);
        generateLootChests(world, 18);

        player.sendMessage("§a§lMapa §f" + worldName + " §a§lgotowa!");
        player.sendMessage("§7Mid + 18 skrzyń z lootem wygenerowane.");
        player.teleport(world.getSpawnLocation().add(0.5, 2, 0.5));
    }

    private void setupUHCWorld(World world) {
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(1000);

        world.setPVP(true);
        world.setDifficulty(Difficulty.NORMAL);
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, false);
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
        inv.addItem(new ItemStack(Material.IRON_INGOT, 6 + random.nextInt(10)));
        inv.addItem(new ItemStack(Material.DIAMOND, 1 + random.nextInt(3)));
        if (random.nextBoolean()) inv.addItem(new ItemStack(Material.IRON_SWORD));
        if (random.nextInt(3) == 0) inv.addItem(new ItemStack(Material.BOW));
    }
}
