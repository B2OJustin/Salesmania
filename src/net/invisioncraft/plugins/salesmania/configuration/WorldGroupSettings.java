/*
This file is part of Salesmania.

    Salesmania is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Salesmania is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Salesmania.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.invisioncraft.plugins.salesmania.configuration;

import net.invisioncraft.plugins.salesmania.Salesmania;
import net.invisioncraft.plugins.salesmania.worldgroups.WorldGroup;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorldGroupSettings implements ConfigurationHandler  {
    private FileConfiguration config;
    private Settings settings;
    private Salesmania plugin;

    public WorldGroupSettings(Settings settings) {
        this.settings = settings;
        plugin = settings.getPlugin();
        update();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<WorldGroup> parseGroups() {
        ArrayList<WorldGroup> worldGroups = new ArrayList<WorldGroup>();
        List<Map<?, ?>> groupData = config.getMapList("Auction.WorldGroups.groups");
        for(Map<?, ?> data : groupData) {
            if((Boolean)data.get("enabled")) {
                try {
                    WorldGroup worldGroup = new WorldGroup(plugin,
                            (String)data.get("groupName"),  (ArrayList<String>)data.get("worlds"));
                    if(data.containsKey("channels")) {
                        for(String channelName : (ArrayList<String>)data.get("channels")) {
                            worldGroup.addChannel(channelName);
                        }
                    }
                    worldGroups.add(worldGroup);
                } catch (ClassCastException ex) {
                    corruptionWarning();
                }
            }
        }
        return worldGroups;
    }

    public void corruptionWarning() {
        plugin.getLogger().severe("World groups configuration is invalid.");
    }

    @Override
    // TODO make world groups reinitialize properly.
    public void update() {
        config = settings.getConfig();
    }
}
