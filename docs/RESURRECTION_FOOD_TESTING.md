# Resurrection food testing

Test Corpse and GraveStone profiles separately whenever death storage is involved.

## 1. Living players

- Memorial Stew, Stew of Deathless Devotion, Rejoining Stew, and Stew of Unbroken Reunion provide 6 nutrition with a 0.8 saturation modifier and return an empty bowl.
- Each apple pie contains four servings. A serving or slice provides 2 nutrition with a 0.3 saturation modifier.
- Living players receive no supernatural effect from any dish.
- No resurrection or teleport debug message may appear.

## 2. Memorial

As a ghost, consume Memorial Stew or a serving of Memorial Apple Pie. The player must resurrect at the soul's current position with full health and hunger.

## 3. Deathless Devotion

Distribute several Stews of Deathless Devotion, Apple Pies of Deathless Devotion, matching slices, and Unbroken Reunion variants across different inventory slots and the offhand. Die. Expected behavior:

- every dish containing Netherite Powder is removed from the dying inventory;
- about one second after the ghost appears, every removed dish is delivered to the soul;
- no transferred dish remains in the corpse or grave and no duplicate exists;
- one chorus-fruit teleport sound and one portal-particle burst play for the delivery;
- no transfer debug message appears;
- Deathless Devotion food resurrects at the soul's current position.

Memorial and Rejoining food without Netherite Powder must follow the normal death-storage rules and must not move to the soul automatically.

## 4. Rejoining

Travel far from the death position as a ghost or move to another dimension, then consume Rejoining Stew or a serving of Rejoining Apple Pie. The player must teleport to a safe point near the saved death position and resurrect. Portal particles and the chorus-fruit teleport sound must play once at the destination.

## 5. Unbroken Reunion

Every Stew of Unbroken Reunion, Apple Pie of Unbroken Reunion, and matching slice must follow the soul through death. Consuming one must return the player to a safe point near the death position, play the teleport effect, and resurrect.

## 6. Whole pies and item rules

1. As a ghost, drop a whole afterlife pie. A living player must be able to collect and place it.
2. Drop an ordinary item. Dropping must also be allowed.
3. Try to pick the ordinary item back up. Pickup must fail.
4. Pick up the afterlife pie. Pickup must succeed.
5. Place the pie as a ghost and consume one serving. The correct resurrection effect must trigger.
6. Confirm that an ordinary block still cannot be placed.

## 7. Full inventory

Fill every inventory slot, then die with multiple death-following dishes in the inventory and offhand. All dishes must still be delivered. Any item that cannot fit may appear at the ghost's feet, remain collectible by the ghost, and must not be duplicated in death storage.

## 8. Persistence

Relog and fully restart the game while in Ghost State. Ghost State and all transferred afterlife food must persist.

## Information to include with a bug report

- the active launch profile;
- the exact stew, whole pie, or slice being tested;
- where it was stored before death;
- counts before death, on the ghost, and in death storage;
- the final lines of \`run/logs/latest.log\`.
