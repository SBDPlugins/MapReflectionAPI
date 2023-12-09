# MapReflectionAPI

This plugin helps developer with displaying images on maps. It supports Spigot 1.12 - 1.20.

## Usage:

### Using the API:

First, include the API using Maven:

```xml
<repository>
  <id>sbdevelopment-repo-releases</id>
  <name>SBDevelopment Repository</name>
  <url>https://repo.sbdevelopment.tech/releases</url>
</repository>

<dependency>
  <groupId>tech.sbdevelopment</groupId>
  <artifactId>MapReflectionAPI</artifactId>
  <version>1.6</version>
  <scope>provided</scope>
</dependency>
```

Then, use our API. Below is an example.

```java
//--- Wrap image ---
MapWrapper wrapper = MapReflectionAPI.getMapManager().wrapImage(ImageIO.read(new File("image.png")));
MapController controller = wrapper.getController();

final Player p = Bukkit.getPlayer("SBDeveloper");

//--- Add viewer ---
try {
   controller.addViewer(p);
} catch (MapLimitExceededException e) {
   e.printStackTrace();
   return;
}
controller.sendContent(p);

//--- Show in frame ---
ItemFrame frame = ...; //This is your ItemFrame.
controller.showInFrame(p, frame, true);

//--- Or show in hand ---
controller.showInHand(p, true);
```

It's also possible to split one image onto multiple itemframes. For example using the following code.

```java
//--- Wrap image (into 2 rows and 2 columns) ---
MultiMapWrapper wrapper = MapReflectionAPI.getMapManager().wrapMultiImage(ImageIO.read(new File("image.png")), 2, 2);
MultiMapController controller = wrapper.getController();

final Player p = Bukkit.getPlayer("SBDeveloper");

//--- Add viewer ---
try {
   controller.addViewer(p);
} catch (MapLimitExceededException e) {
   e.printStackTrace();
   return;
}
controller.sendContent(p);

//--- Show in frames ---
//These are your itemframes
ItemFrame leftTopFrame = ...;
ItemFrame leftBottomFrame = ...;
ItemFrame rightTopFrame = ...;
ItemFrame rightBottomFrame = ...;
ItemFrame[][] frames = {
    {leftTopFrame, rightTopFrame},
    {leftBottomFrame, rightBottomFrame}
};
controller.showInFrames(p, frames, true);
```

More information can be found on the [JavaDoc](https://sbdevelopment.tech/javadoc/mapreflectionapi/).

### Notes on map render distance:

MapReflectionAPI does not implement render distance to images shown on maps. This should be implemented by yourself. An example of this is below.

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class MapRenderDistanceListener implements Listener {
    private static final int MAP_RENDER_DISTANCE_SQUARED = 1024;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(MyPluginInstance, () -> {
            //Show the maps to the player which are within distance
        });
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(MyPluginInstance, () -> {
            //Hide the maps to the player which are within distance
        });
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
        //Hide all the maps in the e.getFrom() world

        Bukkit.getScheduler().runTaskLaterAsynchronously(MyPluginInstance, () -> {
            //Show the maps to the player which are within distance in e.getPlayer().getWorld()
        }, 20);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent e) {
        //Hide all the maps in the e.getEntity().getWorld()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(MyPluginInstance, () -> {
            //Show the maps to the player which are within distance in e.getPlayer().getWorld()
        }, 20);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        //Hide all the maps in the e.getFrom() world
        //Show the maps to the player which are within distance in e.getTo().getWorld()
    
        //FOR EXAMPLE:
        if (e.getTo() == null) return;
        if (e.getFrom().getChunk().equals(e.getTo().getChunk())) return;
        
        for (Frame frame : API.getFramesInWorld(e.getPlayer().getWorld())) {
            double distanceSquared = e.getTo().distanceSquared(frame.getLocation());
            
            if (distanceSquared > MAP_RENDER_DISTANCE_SQUARED) {
                API.hideFrame(e.getPlayer(), frame);
            } else {
                API.showFrame(e.getPlayer(), frame);
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        //Hide all the maps in the e.getFrom() world
        //Show the maps to the player which are within distance in e.getTo().getWorld()
    
        //SEE EXAMPLE ABOVE
    }
}
```

## Credits:

This is a fork of [MapManager](https://github.com/InventivetalentDev/MapManager). It updates the API to 1.19 and uses
other dependencies.

This plugin includes classes from BKCommonLib. Please checkout the README in that package for more information.
