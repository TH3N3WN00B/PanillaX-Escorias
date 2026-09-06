# PanillaX
PanillaX (the name) is a combination of the word Packet and Vanilla (as in Vanilla Minecraft).

PanillaX is a fork of [Panilla](https://www.spigotmc.org/resources/65694/) by ds58 that only supports recent Paper servers and is easier to compile.

![bStats](https://bstats.org/signatures/bukkit/PanillaX.svg)

## Overview
PanillaX is software to prevent abusive NBT and packets on Minecraft servers.

With this software, you will be able to prevent:

- Unobtainable Enchantments (eg. Sharpness X)
- Unobtainable Potions (eg. Insta-kill)
- Unobtainable Fireworks
- Crash Books
- Crash Signs
- Crash Chests/Shulker Boxes
- Crash Potions (invalid CustomPotionColor\s)
- Oversized packets (which crash the client)
- Long item names/item lore
- Additional "AttributeModifiers" on items (eg. Speed)
- Unbreakable items
- and more abusive NBT

## Installation

Download the jar that matches your server and drop it into your `plugins/` folder.

| Jar | Java | Minecraft versions | NMS bundles included |
| --- | ---- | ------------------ | -------------------- |
| `PanillaX-1.2.0.jar` | 21 | 1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10 | `v1_21_4` + `v1_21_5` |
| `PanillaX-1.2.0-java25.jar` | 25 | 1.21.11, 26.x | `v1_21_11` |

Use the plain jar on Java 21 servers and the `-java25` jar on Java 25 servers. If you put the wrong jar on your server, PanillaX will warn you and try to use the newest bundle it has.

## What changed in 1.2.0

- Restored support for Minecraft 1.21 up to 1.21.10 (the bundles `v1_21_4` and `v1_21_5` are back).
- Added support for 1.21.11 and 26.x through the `v1_21_11` bundle.
- Two separate builds: one for Java 21 servers and one for Java 25 servers.
- The plugin now picks the right module by itself based on the server's data version.
- Fixed an inverted check in the inventory-items packet that could let items avoid being inspected.
- Fixed the plugin version not being written into the plugin file.
- Fixed a crash when the enchantment overrides section was missing from the config.
- Added the new Companion enchantment from Minecraft 1.21.9.

## Supported Platforms

**PanillaX is provided as-is, with no guarantees. At the moment, it just adds compatibility for newer versions to old checks. Thus, it could cause issues. Please report them if you find any. **
Note: 1.21.x servers run on Java 21 and 26.x servers run on Java 25, so pick the jar that matches your Java version.

**This fork does NOT support CraftBukkit/Spigot**. It supports Paper derivatives (Purpur, etc.) including Folia.

## Commands
`panilla`: Information about your PanillaX instance (permission: panilla.command)\
`panilla debug`: Show debug information (permission: panilla.command.debug)

## Permissions
`panilla.log.chat`: View warning logs in chat if enabled in the config\
`panilla.bypass`: Bypass packet checks

## Compiling
This fork uses Paperweight, so you won't need to compile Spigot or Paper beforehand.
You can compile the project with the shadowJar task or by running `./gradlew build`. The output plugin jars will be located in the `target/` directory.

- The module `panillax-paper` produces `PanillaX-<version>.jar` for Java 21 servers.
- The module `panillax-paper-v1_21_11` produces `PanillaX-<version>-java25.jar` for Java 25 servers.

Java 21 is required to build the plugin. The Gradle build is set up to use the JDK defined in `gradle.properties` (`org.gradle.java.home`).