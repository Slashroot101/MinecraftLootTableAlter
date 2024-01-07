package me.slashroot.minecraftlootalter;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Main extends JavaPlugin implements Listener {

    private FileConfiguration lootConfig;


    public void onEnable() {
        saveResource("loottables.yml", false);
        getServer().getPluginManager().registerEvents(this, this);
        lootConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "loottables.yml"));
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void onChunkPopulate(ChunkPopulateEvent event) {
        // Iterate through all tile entities in the chunk
        for (BlockState tileEntity : event.getChunk().getTileEntities()) {
            // Check if the entity is a chest
            if (tileEntity instanceof Chest) {
                Chest chest = (Chest) tileEntity;
                getLogger().info("Found chest, customizing loot");
                // Customize the chest's loot here
                customizeChestLoot(chest);
            }
        }
    }

    private void customizeChestLoot(Chest chest) {
        // Get the biome of the chest
        Biome biome = chest.getWorld().getBiome(chest.getX(), chest.getY(), chest.getZ());

        // Clear the chest
        chest.getBlockInventory().clear();

        // Get the loot list for the biome
        List<Map<?, ?>> lootList = lootConfig.getMapList(biome.name());
        for (Map<?, ?> itemInfo : lootList) {
            String itemType = (String) itemInfo.get("TYPE");
            double chance = itemInfo.containsKey("CHANCE") ? (Double) itemInfo.get("CHANCE") : 0.0;

            if (Math.random() <= chance) {
                if ("VANILLA".equals(itemType)) {
                    Material material = Material.valueOf((String) itemInfo.get("MATERIAL"));
                    int amount = (Integer) itemInfo.get("AMOUNT");
                    chest.getBlockInventory().addItem(new ItemStack(material, amount));
                } else if ("ORAXEN".equals(itemType)) {
                    String oraxenId = (String) itemInfo.get("ORAXEN_ID");
                    ItemStack oraxenItem = OraxenItems.getItemById(oraxenId).build();
                    oraxenItem.setAmount((Integer) itemInfo.get("AMOUNT"));
                    chest.getBlockInventory().addItem(oraxenItem);
                } else {
                    getLogger().warning("Invalid item type specified in loot configuration.");
                }
                getLogger().info("Added item to chest at " + chest.getLocation());
            } else {
                getLogger().info("Item not added to chest at " + chest.getLocation() + " due to chance.");
            }
        }
    }
}