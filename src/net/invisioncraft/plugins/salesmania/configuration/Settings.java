package net.invisioncraft.plugins.salesmania.configuration;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Owner: Justin
 * Date: 5/16/12
 * Time: 7:20 PM
 */
public class Settings extends Configuration {

    public Settings(JavaPlugin plugin) {
        super(plugin, "config.yml");
    }

    // Main
    public String getDefaultLocale() {
        return getConfig().getString("Main.defaultLocale");
    }

    public List<String> getLocales() {
        return getConfig().getStringList("Main.availableLocale");
    }

    public boolean getAllowCreative() {
        return getConfig().getBoolean("Auction.allowCreative");
    }

    // Bidding
    public long getCooldown() {
        return getConfig().getLong("Auction.cooldown");
    }

    public float getMinStart() {
        return getConfig().getLong("Auction.minStart");
    }

    public float getMaxStart() {
        return getConfig().getLong("Auction.maxStart");
    }

    public float getMinIncrement() {
        return getConfig().getInt("Auction.Bidding.minIncrement");
    }

    public float getMaxIncrement() {
        return getConfig().getInt("Auction.Bidding.maxIncrement");
    }

    public int getDefaultTime() {
        return getConfig().getInt("Auction.Bidding.defaultTime");
    }

    public int getSnipeTime() {
        return getConfig().getInt("Auction.Bidding.snipeTime");
    }

    // Logging
    public boolean isLoggingEnabled() {
        return getConfig().getBoolean("Auction.Logging.enabled");
    }

    public String getLogFilename() {
        return getConfig().getString("Auction.Logging.filename");
    }

    public String getLogType() {
        return getConfig().getString("Auction.Logging.type");
    }

    public String getMysqlHost() {
        return getConfig().getString("Auction.Logging.mysqlHost");
    }

    public int getMysqlPort() {
        return getConfig().getInt("Auction.Logging.mysqlPort");
    }

    public String getMysqlUsername() {
        return getConfig().getString("Auction.Logging.mysqlUsername");
    }

    public String getMysqlPassword() {
        return getConfig().getString("Auction.Logging.mysqlPassword");
    }

    public String getMysqlDatabase() {
        return getConfig().getString("Auction.Logging.mysqlDatabase");
    }

    public String getMysqlTable() {
        return getConfig().getString("Auction.Logging.mysqlTable");
    }

    // Blacklist
    public List<String> getBlacklist() {
        return getConfig().getStringList("Auction.Blacklist");
    }
}
