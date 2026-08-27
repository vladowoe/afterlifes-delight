# Ghost State testing

## Launch

1. Launch `runClient` from the IDE or run `.\gradlew.bat runClient`.
2. Create a test world.
3. Confirm that Afterlife's Delight and Farmer's Delight both appear in the Mods list.
4. Open Afterlife's Delight from the Mods menu and confirm that the client settings screen is available.

## Main scenario

1. Record the living player's coordinates and dimension.
2. Die normally and remain on the death screen for several seconds.
3. Ghost particles, ambience, camera motion, and the cold filter must not begin before Respawn is pressed.
4. Press Respawn. In a Hardcore world, the button must read **Enter the Spirit World**.
5. Confirm that the player appears directly near the actual death position rather than briefly appearing at a bed or world spawn first.
6. Confirm Ghost State using the restrictions below.
7. Try to take damage from a mob, another player, fire, lava, falling, drowning, and the void.
8. Try to break or place an ordinary block, open a chest, use a door or button, attack a mob, and pick up an ordinary item.
9. Walk over a pressure plate, approach a sculk sensor, and try to enter a boat or minecart.
10. Confirm that hostile mobs do not target the ghost.
11. Confirm that health, hunger, armor, mount health, and air meters are hidden.
12. Drop an item from the inventory. Dropping must be allowed.
13. Pick up an afterlife dish, place a whole afterlife pie, and confirm that ordinary items still cannot be collected.
14. With `allowGhostFlight` enabled, toggle flight by double-tapping jump.
15. Leave the world and reconnect. Ghost State and its restrictions must persist.
16. Fully restart the game and reconnect. Ghost State must still persist.
17. Resurrect with Memorial Stew or a serving of Memorial Apple Pie. Normal abilities, interactions, HUD, and mob targeting must return immediately.

## Dimensions, portals, and the void

1. Die in the Nether and End and confirm that Ghost State begins in the same dimension near the death position.
2. Use an existing portal while in Ghost State. Portals must remain functional.
3. Move below the world's minimum build height. The ghost must return to the last saved safe position without being resurrected.
4. Die in lava or another unsafe location. The ghost must appear at the nearest non-colliding safe position.

## Flight configuration

After the first launch, set the following value in the common configuration:

```toml
allowGhostFlight = false
```

After a restart, ghosts must no longer receive flight. Resurrection must not remove Creative flight or flight granted by another mod.

## Multiplayer

1. Join the same server with two players.
2. Kill one player and leave the other alive.
3. Only the dead player must enter Ghost State and receive its local atmosphere.
4. The living player must see the ghost model and trail but retain normal HUD, controls, targeting, and interactions.
5. Let both players die. The world must remain playable as ghosts, but resurrection must require available afterlife food or a newly joined living cook.
6. Resurrect one player and confirm that the other remains a ghost.

## Hardcore behavior

- The Hardcore death screen must offer **Enter the Spirit World** instead of forcing Spectator mode or returning to the title screen.
- The player must still complete a real Minecraft death, allowing Corpse and grave mods to create their storage normally.
- Entering Ghost State must occur only after the player confirms the death screen.
