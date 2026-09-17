# Hardcore Lite compatibility testing

Compatibility target: **Hardcore Lite 1.0.0 (NeoForge 1.21–1.21.1)**.

Development launch:

```powershell
.\gradlew.bat runClient -PcompatProfile=hardcore_lite
```

The integration is optional. Afterlife's Delight must continue to start and behave normally when Hardcore Lite is not installed.

## Intended behavior

With Hardcore Lite installed, ordinary deaths consume one Hardcore Lite heart and use the normal respawn flow. Afterlife's Delight should only create a ghost on the death that would otherwise leave the player with no hearts and switch them to spectator mode.

When that final death occurs, Afterlife's Delight preserves one Hardcore Lite heart for the ghost. After resurrection the player therefore returns alive with **1 heart of maximum health**, and another death can become a final death again.

## Test 1 — regression without Hardcore Lite

Launch without a compatibility profile.

1. Enter a normal test world.
2. Die.
3. Respawn.

Expected:

- existing Afterlife's Delight death → ghost flow is unchanged;
- ghost appears near the death location;
- resurrection food and ghost restrictions behave as before.

## Test 2 — ordinary Hardcore Lite death

Launch with `-PcompatProfile=hardcore_lite`.

1. Set the player to several hearts, for example:

```mcfunction
/playerhearts 5
```

2. Die once.
3. Respawn.

Expected:

- Hardcore Lite removes one heart;
- player respawns normally;
- Afterlife's Delight does **not** create a ghost record;
- no ghost atmosphere, flight, interaction restrictions or resurrection flow starts;
- death-following Afterlife food is not extracted for ghost delivery.

## Test 3 — final Hardcore Lite death

1. Set the player to one heart:

```mcfunction
/playerhearts 1
```

2. Die.
3. Press Respawn.

Expected:

- this death enters Afterlife's Delight's ghost flow instead of leaving the player as a spectator;
- ghost appears safely near the death location;
- ghost atmosphere, visuals and restrictions activate normally;
- the player remains playable as a ghost rather than being stuck in spectator mode;
- Hardcore Lite's saved state is preserved at one remaining heart for later resurrection.

## Test 4 — resurrection after final death

Continue from Test 3.

1. Resurrect using any valid Afterlife's Delight resurrection dish.

Expected:

- player becomes living again;
- maximum health is 2 HP / 1 heart;
- current health is restored to that maximum;
- hunger is restored by Afterlife's Delight as usual;
- player is not left in spectator mode.

Then die again.

Expected:

- the death again counts as Hardcore Lite's final death;
- ghost flow starts again;
- the integration does not get stuck after the first resurrection.

## Test 5 — death-following food

1. Set the player above one heart.
2. Carry Deathless Devotion or Unbroken Reunion food.
3. Die normally.

Expected:

- no special Afterlife transfer occurs because this is not a ghost death.

Then:

1. Set the player to one heart.
2. Carry the same food.
3. Die.

Expected:

- final death creates a ghost;
- food configured to follow the soul is transferred through death exactly once;
- no duplication occurs.

## Test 6 — relog as ghost

1. Trigger the final Hardcore Lite death.
2. Respawn as a ghost.
3. Leave the world/server and reconnect.

Expected:

- player is still a ghost;
- player is not restored to spectator mode;
- ghost flight, visuals and restrictions are restored normally.

## Test 7 — enchanted golden apple after resurrection

Hardcore Lite can restore hearts by enchanted golden apple when its gamerule allows it.

1. Resurrect from the final-death ghost state.
2. Confirm maximum health is one heart.
3. Eat an enchanted golden apple with Hardcore Lite heart restoration enabled.

Expected:

- Hardcore Lite can increase the player's heart count normally;
- Afterlife's Delight does not interfere with later heart restoration.

## Failure diagnostics

If the log contains:

`Hardcore Lite was detected, but Afterlife's Delight could not preserve its final heart`

the optional reflection bridge no longer matches Hardcore Lite's implementation. Do not publish the compatibility claim until the bridge is updated and the scenarios above pass.
