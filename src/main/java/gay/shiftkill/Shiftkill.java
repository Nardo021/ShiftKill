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

    private final Map<Player, Double> progressBars = new HashMap<>();
    private final double progressBarIncrement = 10.0; // 进度条每次增加的值
    private ConfigManager configManager;
    private Map<Player, Player> shifters = new HashMap<>();

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

        // 发送启用消息
        getLogger().info("ShiftKill已启用！你可以积攒进度条击杀玩家了！");
    }

    @Override
    public void onDisable() {
        // 发送关闭消息
        getLogger().info("ShiftKill已关闭！");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("shiftkill")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                // 检查发送者是否有重新加载配置的权限
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (!player.hasPermission("shiftkill.reload")) {
                        player.sendMessage(ChatColor.RED + "你没有权限使用此命令。");
                        return true;
                    }
                }
                // 重新加载配置
                configManager.setupConfig();
                sender.sendMessage(ChatColor.GREEN + "Shiftkill 配置已重新加载！");
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player sneaker = event.getPlayer();
        Player shifter = shifters.get(sneaker);

        if (event.isSneaking()) {
            // 玩家按下Shift键
            if (shifter != null) {
                // 已有玩家积攒进度条，不执行任何操作
            } else {
                // 玩家按下Shift键并且没有积攒进度条的目标
                Player target = getTarget(sneaker);
                if (target != null && !target.isDead()) {
                    // 将玩家设置为积攒进度条的目标
                    shifters.put(sneaker, target);
                    target.sendMessage(ChatColor.RED + "你正在被 " + sneaker.getName() + " 撅！享受吧！");
                    sneaker.sendMessage(ChatColor.YELLOW + "你正在积攒进度条...");
                }
            }
        } else {
            // 玩家松开Shift键
            if (shifter != null) {
                // 检查玩家之间的距离
                double distance = sneaker.getLocation().distance(shifter.getLocation());

                if (distance > 2.0) {
                    // 玩家相距超过2格，重置进度条为零
                    progressBars.remove(sneaker);
                    sneaker.sendMessage(ChatColor.RED + "进度条已重置为零，你和目标相距太远！");
                } else {
                    double progress = progressBars.getOrDefault(sneaker, 0.0);
                    progress += progressBarIncrement;

                    if (progress >= 100.0) {
                        // 进度条已满，执行击杀操作
                        Player target = shifters.get(sneaker);
                        target.setHealth(0); // 杀死前面的玩家
                        sneaker.sendMessage(ChatColor.GREEN + "你成功击杀了 " + target.getName() + "!");
                        progressBars.remove(sneaker);
                        shifters.remove(sneaker);
                    } else {
                        // 更新进度条
                        progressBars.put(sneaker, progress);
                        sneaker.sendMessage(ChatColor.YELLOW + "进度条: " + progress + "%");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        progressBars.remove(player); // 玩家死亡时清零进度条
        shifters.values().remove(player); // 移除积攒目标
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
}
