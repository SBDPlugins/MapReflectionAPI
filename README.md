# MapReflectionAPI

This API helps developer with viewing images on maps. It supports Spigot 1.12 - 1.19.
It currently has **no** support for GIFs.

## Dependencies:

- [BKCommonLib](https://www.spigotmc.org/resources/bkcommonlib.39590/)

## Building:

To build MapReflectionAPI yourself, you need to run BuildTools for 1.12.2, 1.13.2, 1.14.4, 1.15.2, 1.16.5, 1.17.1,
1.18.2 and 1.19.
Then run `mvn clean package`.

## Credits:

The source is based on [MapManager](https://github.com/InventivetalentDev/MapManager). It removes the PacketListenerAPI
dependency and uses NMS with submodules instead of reflection to make the code easier to edit.