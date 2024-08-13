# View Distance Tweaks

**Plugin page**: [https://www.spigotmc.org/resources/75164/](https://www.spigotmc.org/resources/75164/)

## About
View Distance Tweaks is a Spigot plugin that allows for dynamic per-world simulation distance and view distance.

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
./gradlew clean shadowJar
```

3. Find jar in `ViewDistanceTweaks/build/libs`
