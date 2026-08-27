# Death-storage compatibility testing

These scenarios verify the separation of responsibilities:

- Afterlife's Delight stores the soul state and death position.
- Corpse or GraveStone Mod stores the player's inventory.
- Sophisticated Backpacks preserves its nested contents inside the backpack ItemStack.
- A ghost cannot recover ordinary belongings before leaving Ghost State.
- Afterlife food with Netherite Powder follows the soul without remaining in death storage or being duplicated.

Use a separate test world for each death-storage mod. Do not run Corpse and GraveStone Mod together.

## Launch profiles

### Corpse

\`\`\`powershell
.\gradlew.bat runClient -PcompatProfile=corpse
\`\`\`

Profile version: Corpse \`1.21.1-1.1.13\`.

### GraveStone Mod

\`\`\`powershell
.\gradlew.bat runClient -PcompatProfile=gravestone
\`\`\`

Profile version: GraveStone Mod \`1.21.1-1.0.38\`.

### Corpse with a stored backpack

\`\`\`powershell
.\gradlew.bat runClient -PcompatProfile=corpse_backpacks
\`\`\`

### GraveStone Mod with a stored backpack

\`\`\`powershell
.\gradlew.bat runClient -PcompatProfile=gravestone_backpacks
\`\`\`

The backpack profiles use Sophisticated Backpacks \`1.21.1-3.25.77.2086\` and Sophisticated Core \`1.21.1-1.4.87.2270\`.

## Vanilla drops

1. Launch the regular \`runClient\` profile without \`compatProfile\`.
2. Take several uniquely identifiable items and die.
3. Confirm that each item drops exactly once near the death position.
4. Confirm that the ghost cannot pick up those items.
5. Resurrect with Memorial Stew or a serving of Memorial Apple Pie.
6. Pick up the items while alive and confirm that the total count of every item is unchanged.

## Corpse

1. Take a unique set of items, armor, and an offhand item.
2. Die and press Respawn.
3. The player must enter Ghost State near the death position, while Corpse creates a body containing the items.
4. Right-click the corpse normally. Its inventory menu must not open.
5. Open Death History with \`U\` and try to access the death inventory. No server inventory menu should open.
6. Relog and repeat the access attempts.
7. Resurrect with Memorial Stew or Memorial Apple Pie.
8. Open the corpse and recover the items.
9. Confirm that the corpse empties through its normal behavior and that no item is duplicated.

## GraveStone Mod

1. Take a unique set of items, armor, and an offhand item.
2. Die and press Respawn.
3. The player must enter Ghost State near the death position, while the items are stored in a grave.
4. Confirm that the obituary does not appear in the ghost's active inventory.
5. Try to break the grave. This must not be possible.
6. Stand on the grave and sneak. The sneak-pickup option must not return items to the ghost or destroy the grave.
7. Relog and repeat the access attempts.
8. Resurrect, then recover the items through GraveStone's normal interaction.
9. Confirm that no item is duplicated.

## Sophisticated Backpacks

1. Create a backpack and fill it with several easily counted items.
2. Place the backpack itself in a normal inventory slot, not inside another container.
3. Die while using \`corpse_backpacks\` or \`gravestone_backpacks\`.
4. The backpack must appear inside the active death storage as one ItemStack with all nested contents preserved.
5. The ghost must not be able to open the backpack by keybind or item use.
6. Resurrect, recover the backpack, and check its contents.
7. The total number of backpacks and nested items must not increase.

## Food that follows the soul

Run this scenario once with Corpse and once with GraveStone Mod.

1. Place multiple Stews of Deathless Devotion, Stews of Unbroken Reunion, matching whole pies, and matching pie slices across normal inventory slots and the offhand.
2. Fill the remaining inventory slots so that the ghost has no free space after respawning.
3. Die and press Respawn.
4. Every death-following dish must be removed from the dying inventory before death storage captures it.
5. About one second after Ghost State begins, every dish must be delivered to the soul. Overflow items may appear at the ghost's feet and must be collectible by the ghost.
6. The corpse or grave must contain none of those dishes.
7. A chorus-fruit teleport sound and portal-particle burst must play once for the complete delivery.
8. Confirm that the total number of dishes is unchanged and that no duplicates exist.
9. Memorial and Rejoining dishes without Netherite Powder must remain subject to normal death-storage behavior.

## Respawn-position priority

For every profile, test death:

- near a valid bed;
- in the Nether;
- in the End;
- after a full game restart.

The death-storage mod may choose a position for its corpse or grave, but the ghost must appear near the actual death position saved by Afterlife's Delight. The client must not briefly show the bed or world-spawn position before arriving there.

## Information to include with a bug report

- the active \`compatProfile\`;
- exact reproduction steps;
- \`run/logs/latest.log\`;
- the crash report, if one was created;
- where the items ended up: on the ghost, in the corpse or grave, on the ground, or in more than one place.
