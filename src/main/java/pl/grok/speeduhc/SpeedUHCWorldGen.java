package pl.grok.speeduhc;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SpeedUHCWorldGen extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("§aSpeedUHCWorldGen §f1.0 §azostał włączony!");
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

        player.sendMessage("§6§lGenerowanie świata §e" + worldName + " §6§l(1000×1000)...");

        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.type(WorldType.NORMAL);

        World world = creator.createWorld();

        if (world == null) {
            player.sendMessage("§cNie udało się stworzyć świata!");
            return true;
        }

        // WorldBorder 1000x1000
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(1000);
        border.setWarningDistance(25);
        border.setDamageAmount(1.0);

        // Ustawienia UHC
        world.setPVP(true);
        world.setDifficulty(Difficulty.NORMAL);
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);

        player.sendMessage("§a§lGotowe! §f" + worldName + " §a§lzostał utworzony.");
        player.sendMessage("§7• Border: §f1000×1000");
        player.sendMessage("§7• PVP: §fWłączone");

        player.teleport(world.getSpawnLocation().add(0.5, 1, 0.5));

        return true;
    }
}
