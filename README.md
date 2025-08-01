# The Annihilation
> Telling untold stories

A Minecraft minigame
## Administration
### Minecraft Compatabilities
TheAnnihilation is natively compiled against Paper API 1.21.8, Java 21.

TheAnnihilation is designed to support a minimal of Paper 1.21.6, Java 21.
### Dependencies
- [ProtocolLib](https://ci.dmulloy2.net/job/ProtocolLib/)
- [NBTAPI](https://github.com/tr7zw/Item-NBT-API/releases)
- [Citizens](https://ci.citizensnpcs.co/job/Citizens2/)
- CraftEngine
  - [Community Edition](https://github.com/Xiao-MoMi/craft-engine/releases)
  - Build from the [dev branch](https://github.com/Xiao-MoMi/craft-engine/tree/dev)
- [Multiverse](https://hangar.papermc.io/Multiverse/Multiverse-Core/versions)
- [PlaceholderAPI](https://ci.extendedclip.com/job/PlaceholderAPI/)
### Miscellaneous
#### When something doesn't work
Add `-Dnet.megavex.scoreboardlibrary.forceModern=true` to the server's startup flags.

Ensure `world.enforce-gamemode` is set to `false` in Multiverse's configuration.
