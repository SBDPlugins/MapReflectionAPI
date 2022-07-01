# MapReflectionAPI

This API helps developer with viewing images on maps. It supports Spigot 1.12 - 1.19.
It currently has **no** support for GIFs.

## Dependencies:

- [BKCommonLib](https://www.spigotmc.org/resources/bkcommonlib.39590/)
- [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)

## Usage:

First, include the API using Maven:

```xml
<repository>
    <id>sbdevelopment-repo</id>
    <url>https://repo.sbdevelopment.tech/repository/maven-releases/</url>
</repository>

<dependency>
    <groupId>tech.sbdevelopment</groupId>
    <artifactId>MapReflectionAPI</artifactId>
    <version>1.1</version>
    <scope>provided</scope>
</dependency>
```

Then, use our API. Below is an example.

```java
//Wrap image
MapWrapper wrapper = MapReflectionAPI.getMapManager().wrapImage(ImageIO.read(new File("image.png")));
MapController controller = wrapper.getController();

final Player p = Bukkit.getPlayer("SBDeveloper");

//Add viewer
try {
   controller.addViewer(p);
} catch (MapLimitExceededException e) {
   e.printStackTrace();
   return;
}
controller.sendContent(p);

//Show in frame
ItemFrame frame = ...; //This is your ItemFrame.
controller.showInFrame(p, frame, true);

//Or show in hand
controller.showInHand(p, true);
```

## Credits:

This is a fork of [MapManager](https://github.com/InventivetalentDev/MapManager). It updates the API to 1.19 and uses
other dependencies.