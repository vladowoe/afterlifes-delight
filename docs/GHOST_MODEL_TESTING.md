# Ghost model testing

## Third-person view

1. Before dying, switch to third-person view and note the normal appearance of the skin.
2. Die and enter Ghost State.
3. Over roughly three seconds, the model should:
   - become strongly translucent;
   - gain a cold blue-gray tint;
   - retain recognizable skin details;
   - gain a subtle emissive layer that remains visible in darkness;
   - stop casting a normal solid shadow.
4. Walking, flying, sneaking, item dropping, pie placement, and eating animations must remain normal.

## Motion trail

1. Switch to third-person view and walk or fly as a ghost.
2. Three pale-blue echo copies should appear at the model's recent positions.
3. The nearest copy should be strongest and the farthest copy weakest.
4. After stopping, the trail should catch up and disappear without leaving a stationary halo.
5. Teleportation must not leave a long chain of copies between the origin and destination.
6. The local player's copies must not obstruct first-person view.

The trail creates no entities and sends no additional network packets. At most three copies are rendered for each of the four nearest ghosts within 32 blocks.

## First-person view

- The empty hand and outer sleeve layer should be as translucent and cold-tinted as the third-person model.
- A held item remains material and does not need to become translucent.
- Translucency must not create black polygons or sleeve flicker.

## Resurrection

Resurrect with any afterlife food. As the atmosphere fades, the model, arms, lighting, and shadow must return to their normal appearance.

## Other players

Multiplayer checks:

- a living player must see the translucent ghost model;
- a living player must see the motion trail of a ghost within 32 blocks;
- a living player model must not receive the ghost effect;
- one player's Ghost State must not enable the screen filter for another player.

## Visual compatibility

Test classic and slim arm models, Fast and Fancy graphics, and Iris both without a shader pack and with one enabled. Include a screenshot and \`latest.log\` with any rendering artifact.
