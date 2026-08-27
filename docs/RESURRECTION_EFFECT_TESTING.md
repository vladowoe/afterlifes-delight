# Death and resurrection effects testing

## Entering Ghost State

1. Die and remain on the death screen.
2. No ghost ambience, particles, filter, camera sway, or death sound should begin yet.
3. Press Respawn or **Enter the Spirit World** in Hardcore.
4. Once Ghost State is confirmed, \`minecraft:entity.warden.sonic_charge\` should play from the player.
5. The camera transition and atmosphere should fade in without delaying controls.

A canceled death must not play the death sound or start any Ghost State effect.

## Resurrection scene

1. Enter Ghost State and switch to third-person view.
2. Resurrect with Memorial Stew or Memorial Apple Pie.
3. Three translucent echo shells should merge smoothly into the body at the established pace.
4. \`minecraft:entity.warden.sonic_boom\` should play from the player's position.
5. The resurrection itself must not spawn an additional cheap-looking particle burst.
6. The scene should last roughly one and a half seconds without blocking controls.
7. The player must finish fully alive, opaque, and free of ghost atmosphere.

## Seasoning variants

- Memorial and Deathless Devotion food uses blue echo shells.
- Rejoining and Unbroken Reunion food uses purple echo shells. Teleportation must finish first, then the scene must play at the destination.

Every variant must restore full health and hunger. Food containing Ender Seasoning must retain the safe destination behavior near the saved death position.

## Atmosphere and sound

- The blue filter, vignette, and ghost fog must look the same as before the resurrection scene was added.
- They fade out with Ghost State; the scene must not bypass or prematurely disable the post-processing filter.
- When death-following food reaches the soul, the chorus-fruit teleport sound and portal particles must play once.
- When Ender Seasoning returns the soul to its death position, the same teleport effect must play at the destination.
- The default resurrection sound is \`minecraft:entity.warden.sonic_boom\`.
- The common \`deathSound\` and \`resurrectionSound\` settings accept \`sound_id volume pitch\`, a legacy sound ID by itself, or \`none\`.

Candidates for later comparison: \`minecraft:block.sculk_shrieker.shriek\`, \`minecraft:block.trial_spawner.spawn_mob\`, \`minecraft:entity.warden.dig\`, \`minecraft:entity.warden.heartbeat\`, and \`minecraft:entity.warden.nearby_close\`.

## Camera and graphics

Test first- and third-person views, 16:9 and 21:9 resolutions, rapid camera switching, Fast and Fancy graphics, and Iris both without and with a shader pack. Echo shells must not fill the view with a solid color, and ghost fog or the cold filter must not disappear before Ghost State ends.

Set \`enableGhostTransitionCamera = false\` and confirm that only camera motion is disabled. Set \`ghostTransitionCameraIntensity\` to \`0.0\`, \`1.0\`, and \`2.0\` and confirm that the transition scales without changing resurrection timing.

## Repetition and relogging

- Two consecutive resurrections must not leave old echo shells, camera motion, or sound.
- Leaving the world during the scene must clear the client animation.
- Resurrecting after relogging as a ghost must start the same scene.
