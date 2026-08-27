# Ghost atmosphere testing

## Main scenario

1. Die and press Respawn.
2. During the next three seconds, the following effects should fade in smoothly:
   - sparse gray ash and occasional pale particles;
   - quiet Soul Sand Valley ambience;
   - distant cold fog;
   - a cold blue-gray filter and subtle vignette;
   - gentle camera sway and field-of-view distortion.
3. Nearby terrain must remain readable. The result must not resemble `Blindness` or `Darkness`.
4. The HUD, chat, inventory, and item tooltips must retain their normal colors.
5. Health, hunger, armor, mount health, and air meters must stay hidden while the local player is a ghost.
6. In water and lava, the atmospheric fog and screen layer must not replace vanilla fluid fog.
7. Resurrect with any afterlife food. The ambience and visual effects should fade out smoothly over roughly two seconds.

## Particles

- Particles must be distributed through the surrounding space rather than orbiting in a dense ring around the player.
- Minecraft's All, Decreased, and Minimal particle settings must affect their density.
- Atmospheric particles must not spawn underwater.
- Dimension changes and relogs must not duplicate the particle emitter.

## Sound

- The ambience must use the Ambient/Environment sound category.
- `ghostAmbientVolume = 0.35` should produce roughly 70% of the source sound's original volume before the global Ambient/Environment slider is applied.
- In a Soul Sand Valley, a second copy of the same ambience must not overlap the vanilla one.
- No looping sound may remain after resurrection, leaving a world, or reconnecting.

## Fog and filter

- Vanilla fog must continue to hide the actual chunk-render boundary.
- Ghost fog should begin fading in at roughly 18 blocks and become dense toward 152 blocks without disabling vanilla render-distance fog.
- The dimension's original color palette should remain recognizable, but appear colder and less saturated.
- Sunrise, sunset, and views below the horizon must not reveal debug rectangles, black bands, or the void renderer.
- The filter must not break the alpha channel of leaves, water, clouds, or other translucent geometry.
- The sun, moon, and clouds must retain their normal shapes while the cold filter is enabled.
- Test the Overworld, Nether, End, water, and lava at several render distances and after switching between Fast and Fancy graphics.
- With Iris installed, test at least once without a shader pack and once with an active shader pack.
- If a particular shader pack is incompatible, set visual intensity to `0.0` and confirm that ambience can remain enabled separately.

## Client configuration

After the first launch, open the Afterlife's Delight client settings:

```toml
enableGhostAtmosphere = true
ghostAtmosphereIntensity = 1.0
ghostAmbientVolume = 0.35
enableGhostTransitionCamera = true
ghostTransitionCameraIntensity = 1.0
```

- `enableGhostAtmosphere` enables or disables particles, fog, the cold filter, ambience, and ghost rendering.
- `ghostAtmosphereIntensity` controls particles, ghost fog, and the cold filter from `0.0` to `1.0`.
- `ghostAmbientVolume` controls ambience separately from `0.0` to `1.0`.
- `enableGhostTransitionCamera` enables or disables camera motion when entering and leaving Ghost State.
- `ghostTransitionCameraIntensity` controls camera sway and FOV distortion from `0.0` to `2.0`.

Test changes both before entering Ghost State and while already in it. Server-common settings must not become editable from a client connected to someone else's server.
