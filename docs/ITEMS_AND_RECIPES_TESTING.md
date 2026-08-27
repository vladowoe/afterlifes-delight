# Items and recipes testing for 1.1.0

Launch the regular profile:

```powershell
.\gradlew.bat runClient
```

## 1. Creative tab and localization

The dedicated Afterlife's Delight creative tab must contain:

- Wooden, Stone, Iron, Golden, Diamond, and Netherite Pestles;
- Echo Dust, Netherite Powder, Ender Seasoning, and Echo Pie Crust;
- four stews;
- four whole apple pies and their four matching slices.

Every item must have its own transparent texture and English and Russian names. The creative tab title must be **Afterlife's Delight** in English and **Загробные Изыски** in Russian.

## 2. Pestles

| Material | Durability | Attack damage | Attack speed |
| --- | ---: | ---: | ---: |
| Wood | 59 | 2 | 1.2 |
| Stone | 131 | 3 | 1.2 |
| Iron | 250 | 4 | 1.2 |
| Gold | 32 | 2 | 1.2 |
| Diamond | 1561 | 5 | 1.2 |
| Netherite | 2031 | 6 | 1.2 |

One grinding operation or successful hit consumes one durability point. Every pestle must accept Unbreaking and Mending. The tooltip's main-hand attributes must display attack damage and attack speed.

The Wooden Pestle accepts any planks. The Stone Pestle accepts regular stone materials, cobblestone, tuff, deepslate, and blackstone. The Netherite Pestle is obtained by upgrading the Diamond Pestle at a Smithing Table.

## 3. Cutting Board

- `Echo Shard -> 4 Echo Dust` works with every pestle.
- `Chorus Fruit -> 4 Ender Seasoning` works with Iron, Golden, Diamond, or Netherite Pestles.
- `Netherite Scrap -> 4 Netherite Powder` works only with the Netherite Pestle.

Confirm that using an insufficient pestle does not consume the ingredient or damage the tool.

## 4. Four stews

Every stew cooks in the Cooking Pot for 400 ticks. The common ingredients are:

- Minced Beef;
- Onion;
- Echo Dust;
- Bone Broth in the container slot.

Add Netherite Powder for Stew of Deathless Devotion, Ender Seasoning for Rejoining Stew, or both for Stew of Unbroken Reunion.

Every stew has a maximum stack size of one and provides 6 nutrition with a 0.8 saturation modifier. Taking the finished dish must consume the Bone Broth itself: its bowl becomes the finished stew's bowl and no extra empty bowl may drop from the Cooking Pot. Eating the stew returns one empty bowl.

Cook and collect both recipes containing Netherite Powder and confirm that the container-slot Bone Broth does not prevent either result from being awarded.

## 5. Echo Pie Crust and apple pies

Craft Echo Pie Crust from one Echo Dust, three Wheat, and one Milk Bucket. The bucket must remain as the normal crafting remainder.

Every whole pie uses:

- three Wheat;
- two Enchanted Golden Apples;
- one Echo Pie Crust;
- Echo Dust;
- optional Netherite Powder and Ender Seasoning matching the four stew combinations.

Confirm all four shaped recipes in both the recipe book and JEI.

Place every pie in the world and consume all four servings. A serving provides 2 nutrition with a 0.3 saturation modifier. Cutting a whole pie on the Cutting Board must produce four matching slices, and crafting four matching slices together must restore the corresponding whole pie.

The top texture must match the selected seasoning combination, while all four pies share the same side and bottom textures.

## 6. Food behavior

- Living players receive only normal nutrition from stews and pie servings.
- Ghosts receive the matching resurrection effect.
- Stews restore full health and hunger when they resurrect a ghost.
- Eating a serving from a placed pie restores full health and hunger when it resurrects a ghost.
- No food item should display non-canonical descriptive lore.
