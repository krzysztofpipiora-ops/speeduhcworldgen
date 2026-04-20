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
        getLogger().info("§aSpeedUHCWorldGen §f1.3 §a— bezpieczna wersja uruchomiona!");
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
            player.sendMessage("§cUżycie: §f/createuhc <nazwa> [typ]");
            player.sendMessage("§7Typy: forest, mountain, desert, river");
            return true;
        }

        String baseName = args[0];
        String type = args.length > 1 ? args[1].toLowerCase() : "forest";

        player.sendMessage("§6§lRozpoczynam generowanie mapy: §e" + baseName + "_" + type);
        player.sendMessage("§7To może chwilę potrwać...");

        // Tworzenie świata asynchronicznie
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    createCustomWorld(player, baseName, type);
                } catch (Exception e) {
                    Bukkit.getScheduler().runTask(SpeedUHCWorldGen.this, () ->
                            player.sendMessage("§cBłąd podczas tworzenia świata: " + e.getMessage())
                    );
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);

        return true;
    }

    private void createCustomWorld(Player player, String baseName, String type) {
        String worldName = baseName + "_" + type;

        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.type(WorldType.NORMAL);
        creator.seed(System.currentTimeMillis());

        World world = creator.createWorld();

        if (world == null) {
            Bukkit.getScheduler().runTask(this, () -> player.sendMessage("§cNie udało się stworzyć świata!"));
            return;
        }

        Bukkit.getScheduler().runTask(this, () -> {
            setupUHCWorld(world);
            generateMid(world);
            generateLootChests(world, 20);
            player.sendMessage("§a§lMapa §f" + worldName + " §azostała pomyślnie utworzona!");
            player.teleport(world.getSpawnLocation().add(0.5, 2, 0.5));
        });
    }

    private void setupUHCWorld(World world) {
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

    private void generateMid(World world) {
        int y = 72;
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                Block b = world.getBlockAt(x, y, z);
                if (Math.abs(x) == 5 || Math.abs(z) == 5) {
                    b.setType(Material.OBSIDIAN);
                } else {
                    b.setType(Material.DIAMOND_BLOCK);
                }
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
        inv.addItem(new ItemStack(Material.COOKED_BEEF, 6 + random.nextInt(10)));
        inv.addItem(new ItemStack(Material.GOLDEN_APPLE, 1 + random.nextInt(3)));
        inv.addItem(new ItemStack(Material.IRON_INGOT, 8 + random.nextInt(12)));
        inv.addItem(new ItemStack(Material.DIAMOND, 2 + random.nextInt(3)));

        if (random.nextBoolean()) inv.addItem(new ItemStack(Material.IRON_SWORD));
        if (random.nextInt(3) == 0) inv.addItem(new ItemStack(Material.BOW));
        if (random.nextInt(3) == 0) inv.addItem(new ItemStack(Material.ARROW, 20 + random.nextInt(20)));
        if (random.nextBoolean()) inv.addItem(new ItemStack(Material.ENDER_PEARL));
    }
}
