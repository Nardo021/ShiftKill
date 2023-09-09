package gay.shiftkill;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class Shiftkill extends JavaPlugin implements Listener {

    private final Map<Player, Player> attackers = new HashMap<>();
    private ConfigManager configManager;
    private Buffermilk buffermilk;

    @Override
    public void onEnable() {
        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(this, this);

        // 初始化配置管理器
        configManager = new ConfigManager(this);

        // 读取配置文件
        configManager.setupConfig();

        // 设置默认配置
        configManager.setDefaultConfig();

        // 注册指令
        getCommand("shiftkill").setExecutor(this);

        // 初始化 Buffermilk 类，并将插件实例传递给它
        //buffermilk = new Buffermilk(this);

        // 发送启用消息
        getLogger().info("ShiftKill已启用！你可以撅死人了（确信");
    }

    @Override
    public void onDisable() {
        // 发送关闭消息
        getLogger().info("ShiftKill已关闭！你撅不了别人了（悲");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("shiftkill")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                // Check if the sender has permission to reload the config
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (!player.hasPermission("shiftkill.reload")) {
                        player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                        return true;
                    }
                }
                // Reload the configuration
                configManager.setupConfig();
                sender.sendMessage(ChatColor.GREEN + "Shiftkill 重载成功，你又可以撅人了（喜");
                return true;
            }
        }
        return false;
    }


    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player sneaker = event.getPlayer();
        if (!sneaker.isSneaking()) {
            return; // 玩家不再潜行，不执行任何操作
        }

        Player target = attackers.get(sneaker);

        // 如果目标不为空并且仍然存活，执行伤害操作
        if (target != null && !target.isDead()) {
            // 检查玩家之间的距离
            double distance = sneaker.getLocation().distance(target.getLocation());

            // 检查玩家之间的距离是否小于1个方块的长度（考虑方块的尺寸）
            if (distance < 1.3) { // 方块的长度约为1.3
                // 扣血
                double damageAmount = 2.0; // 每次扣血的数量
                target.damage(damageAmount);

                if (target.getHealth() <= 0) {
                    // 目标玩家死亡
                    generateNamedItem(target.getLocation(), target.getName()); // 在死亡玩家的位置生成带有名称的物品
                    Bukkit.broadcastMessage(target.getName() + "被" + sneaker.getName() + "撅死了！");
                    target.setHealth(0);
                    attackers.remove(sneaker); // 移除攻击者
                }
            }
        } else {
            // 获取潜行者后面的玩家
            Player newTarget = getTarget(sneaker);
            if (newTarget != null) {
                attackers.put(sneaker, newTarget); // 记录攻击目标
            }
        }
    }

    // 获取潜行者后面的玩家
    private Player getTarget(Player sneaker) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != sneaker) {
                // 计算玩家之间的水平距离
                double horizontalDistance = Math.sqrt(
                        Math.pow(sneaker.getLocation().getX() - player.getLocation().getX(), 2) +
                                Math.pow(sneaker.getLocation().getZ() - player.getLocation().getZ(), 2)
                );

                // 检查玩家之间的垂直距离
                double verticalDistance = Math.abs(sneaker.getLocation().getY() - player.getLocation().getY());

                if (horizontalDistance < 1.0 && verticalDistance < 2.0) {
                    return player;
                }
            }
        }
        return null;
    }

    // 生成带有名称的物品在指定位置
    private void generateNamedItem(Location location, String playerName) {
        ItemStack itemStack = new ItemStack(configManager.getDropMaterial());
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(playerName + "的精华");
        itemStack.setItemMeta(itemMeta);

        location.getWorld().dropItemNaturally(location, itemStack);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // 在玩家死亡时触发，你可以在这里添加额外的处理逻辑
    }
}
