# MapReflectionAPI

This plugin helps developer with displaying images on maps. It supports Spigot 1.12 - 1.20.

## Usage:

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

## Credits:

This is a fork of [MapManager](https://github.com/InventivetalentDev/MapManager). It updates the API to 1.19 and uses
other dependencies.

This plugin includes classes from BKCommonLib. Please checkout the README in that package for more information.
