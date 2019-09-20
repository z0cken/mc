package com.z0cken.mc.raid;

import com.z0cken.mc.core.bukkit.Menu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class TeamAttacker extends Team {

    private final Map<Integer, Kit> kits = new HashMap<>();
    //private final File kitFile = new File(PCS_Raid.getInstance().getDataFolder(), getName() + "-kits.json");
    private Menu kitMenu;
    private final Set<Player> canSwitchKit = new HashSet<>();

    public TeamAttacker(Raid raid) throws FileNotFoundException {
        super(raid, "attacker", Color.RED, ChatColor.RED);
        //Util.loadMap(kits, kitFile, new TypeToken<HashMap<Integer, Kit>>() {});
        loadKits();
        kitMenu = makeKitMenu();
        System.out.println(kits.size() + " Kits loaded");
    }

    @Override
    public void spawn(Player player) {
        super.spawn(player);
        player.sendMessage(ChatColor.GRAY + "Nutze /kit um dein Kit zu wechseln");
        GamePlayer gp = getGamePlayer(player);

        canSwitchKit.add(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                canSwitchKit.remove(player);
            }
        }.runTaskLater(PCS_Raid.getInstance(), 10);

        final Kit kit = gp.getKit();
        if(kit == null) player.openInventory(kitMenu);
        else {
            kit.apply(player, false);
        }
        player.getInventory().setItemInOffHand(new ItemStack(Material.LEAD, 1));
    }

    @Override
    public void addPlayer(UUID uuid) {
        super.addPlayer(uuid);
        final Player player = Bukkit.getPlayer(uuid);
        if(player != null) {
            player.sendTitle(ChatColor.RED + "Angreifer", "Rette die Aliens", 5, 60, 5);
        }
    }

    public void loadKits() {
        kits.clear();
        ConfigurationSection section = getConfig().getConfigurationSection("kits");
        if(section == null) return;
        for(String s : section.getKeys(false)) {
            int i = Integer.parseInt(s);
            final Kit kit = section.getSerializable(s, Kit.class);
            if(kit == null) {
                System.out.println("Kit " + s + " is null!");
                continue;
            }
            kits.put(i, kit);
            System.out.println("Kit added: " + kit.getName());
        }
    }

    public Menu makeKitMenu() {
        Menu m = new Menu(PCS_Raid.getInstance(), 1, "Kits");

        System.out.println("Kits found: " + kits.size());

        kits.forEach((i, k) -> {
            if(i == null || k == null) return;
            Menu.Button b = new Menu.Button((menu, button, player, inventoryClickEvent) -> {
                final Team team = PCS_Raid.getRaid().getTeam(player);
                final GamePlayer gp = team.getGamePlayer(player);
                Kit currentKit = gp.getKit();
                gp.setKit(k);
                if(currentKit == null || canSwitchKit.contains(player)) gp.getKit().apply(player, true);
                player.closeInventory();
            }, k.getButton().getType(), k.getButton().getAmount());
            b.setItemMeta(k.getButton().getItemMeta());
            m.setItem(i, b);
        });

        return m;
    }

    public Menu getKitMenu() {
        return kitMenu;
    }

    public Map<Integer, Kit> getKits() {
        return kits;
    }

    public void save() throws IOException {
        super.save();
        System.out.println("Saving kits");
        kits.forEach((i, k) -> getConfig().set("kits." + i.toString(), k));
        //Util.saveMap(kits, kitFile);
    }
}
