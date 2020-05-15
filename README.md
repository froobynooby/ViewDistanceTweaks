# View Distance Tweaks

**Plugin page**: [https://www.spigotmc.org/resources/75164/](https://www.spigotmc.org/resources/75164/)

## About
View Distance Tweaks is a Spigot plugin that allows for dynamic per-world view distances. View distance can be adjusted based off of player-loaded chunk counts or TPS - or both! Some additional features are available to those using [Paper](https://papermc.io/), including the option to dynamically adjust the no-tick view distance of worlds.

## Building

1. Install dependency NabConfiguration to maven local
```bash
git clone https://github.com/froobynooby/nab-configuration
cd nab-configuration
./gradlew clean install
```
2. Clone ViewDistanceTweaks and build
```bash
git clone https://github.com/froobynooby/ViewDistanceTweaks
cd ViewDistanceTweaks
./gradlew clean build
```

3. Find jar in `ViewDistanceTweaks/build/libs`