package io.github.dairn.cairn

import io.github.dairn.core.CharacterBackground

internal object CairnBackgrounds {
    val all = listOf(
        background("aurifex", "Aurifex", "Hestia, Basil, Rune, Prism, Ember, Quintess, Aludel, Mordant, Salaman, Jazia", "3d6 Gold Pieces|Rations (3 uses)|Lantern|Oil Can (6 uses)|Needle-knife (d6)|Protective Gloves (petty)"),
        background("barber-surgeon", "Barber-Surgeon", "Wilmot, Patch, Lancet, Sawbones, Theo, Cutwell, Humor, Landsford, Goodeye, Johanna", "3d6 Gold Pieces|Rations (3 uses)|Torch (3 uses)|Bonesaw (d6)|Bandages (3 uses)|Leech (restores 1 STR, 3 uses)|Stained Medical Finery (petty)"),
        background("beast-handler", "Beast Handler", "Amara, Wulf, Mireille, Soren, Freki, Aster, Gerrik, Boreas, Veda, Matheus", "3d6 Gold Pieces|Rations (3 uses)|Torch (3 uses)|Leather Whip (d6)|Soporific Darts (STR save or fall asleep, 6 uses)|Lure|Rope (25ft)"),
        background("bonekeeper", "Bonekeeper", "Rook, Ebon, Moro, Yew, Pall, Leth, Bea, Barnaby, Vesper, Leder", "3d6 Gold Pieces|Rations (3 uses)|Lantern|Oil Can (6 uses)|Stake (d6)|Chains (10ft)"),
        background("cutpurse", "Cutpurse", "Arlo, Lyra, Eamon, Salina, Elara, Freya, Bull, Sparrow, Ivy, Silas", "3d6 Gold Pieces|Rations (3 uses)|Torch (3 uses)|Twin Daggers (d6+d6, bulky)|Padded Leather (1 Armor)|Lockpicks|Black Outfit (petty)"),
        background("fieldwarden", "Fieldwarden", "Seed, Thresh, Dibber, Sow, Stalk, Harrow, Cobb, Flax, Briar, Rye", "3d6 Gold Pieces|Rations (3 uses)|Torch (3 uses)|Brigandine (1 Armor, bulky)|Sling (d6)|Hand Axe (d6)|Repellent (pick the type, 3 uses)"),
        background("fletchwind", "Fletchwind", "Flint, Feather, Crier, Thunder, Falcon, Pluck, Needle, Warsong, Hawk, Cai", "3d6 Gold Pieces|Rations (3 uses)|Torch (3 uses)|Bow (see background table)|Serrated Knife (d6)|Boiled Leather (1 Armor)|Heartroot Salve (restores 1d4 STR, 1 use)"),
        background("foundling", "Foundling", "Faunus, Snowdrop, Wisp, Silverdew, Brim, Solstice, Steeleye, Artea, Gossamer, Hazel", "3d6 Gold Pieces|Rations (3 uses)|Torch (3 uses)|Salt Pouch|Heirloom Amulet (petty)|Sling (d6)|Dagger (d6)"),
        background("fungal-forager", "Fungal Forager", "Unther, Woozy, Hilda, Current, Leif, Ratan, Mourella, Lal, Per, Madrigal", "3d6 Gold Pieces|Rations (3 uses)|Sharpened Trowel (d6)|Candle Helmet (+1 Armor, dim, 6 uses)|Rope (25ft)|Metal Pail"),
        background("greenwise", "Greenwise", "Gunther, Moss, Fern, Lichen, Root, Willow, Sage, Yarrow, Rowan, Ash", "3d6 Gold Pieces|Rations (3 uses)|Torch (3 uses)|Iron Pot|Root Knife (d6)|Healing Salve (restores 1d4 STR, 1 use)|Twine Bauble (petty, Ward once per day)"),
        background("half-witch", "Half-Witch", "Solena, Veles, Bryn, Sabine, Razvan, Rowena, Galen, Nyx, Vex, Iwan", "3d6 Gold Pieces|Rations (3 uses)|Torch (3 uses)|Spellbook (Thicket)|Iron Dagger (d6)|Herbs Pouch (restores 1 STR, 3 uses)|Ghillie Suit"),
        background("hexenbane", "Hexenbane", "Percival, Felix, Isolde, Wolfram, Aldric, Eira, Oswin, Ivor, Brunhilda, Beatrix", "3d6 Gold Pieces|Rations (3 uses)|Torch (3 uses)|Vestments of the Order (petty)|Blessed Tinctures|Silver Knife (d6)|Crossbow (d8, bulky)"),
        background("jongleur", "Jongleur", "Jax, Selene, Baladria, Ada, Mort, Saylor, Tripp, Lantos, Echo, Jubilo", "3d6 Gold Pieces|Rations (3 uses)|Torch (3 uses)|Costume|Simple Instrument|Lucky Jerkin (+1 Armor)|Sling (d6)"),
        background("kettlewright", "Kettlewright", "Fergus, Eon, Bram, Idris, Hester, Darragh, Seren, Rónán, Berek, Lorenz", "3d6 Gold Pieces|Rations (3 uses)|Torch (3 uses)|Pincers|Roll of Tin|Gloves (petty)|Hammer (d6)"),
        background("marchguard", "Marchguard", "Gann, Light, Saoirse, Frost, Thorn, Reed, Dirk, Ragnar, Brie, Aasim", "3d6 Gold Pieces|Rations (3 uses)|Lantern|Oil Can (6 uses)|Long Sword (d10, bulky)|Boiled Leather (1 Armor)"),
        background("mountebank", "Mountebank", "Ambrose, Lucius, Beauregard, Cornelius, Aria, Toph, Indigo, Delphine, Solene, Noa", "3d6 Gold Pieces|Rations (3 uses)|Torch (3 uses)|Cart (+4 slots, bulky when pulled)|Trick Playing Cards|Fancy Hat (petty)|Cane Sword (d6)"),
        background("outrider", "Outrider", "Drake, Cyra, Keir, Darius, Valen, Rorik, Yara, Rui, Talon, Jory", "3d6 Gold Pieces|Rations (3 uses)|Torch (3 uses)|Long Sword (d10, bulky)|Leather Jerkin (1 Armor)|Crossbow (d8, bulky)|Spyglass"),
        background("prowler", "Prowler", "Winda, Brielle, Theron, Chayse, Nuja, Dev, Raven, Arawan, Sable, Baruani", "3d6 Gold Pieces|Rations (3 uses)|Torch (3 uses)|Tarp|Boiled Leather (1 Armor)|Short Sword (d6)|Spring-Loaded Trap (4 STR damage)"),
        background("rill-runner", "Rill Runner", "Gale, Piper, Brook, Adair, Stone, Dale, Wren, Cliff, Rain, Robin", "3d6 Gold Pieces|Rations (3 uses)|Torch (3 uses)|Water Shoes|Brigandine (1 Armor, bulky)|Compass|Dagger (d6)"),
        background("scrivener", "Scrivener", "Lazlo, Stilo, Akshara, Pisa, Ji-Yun, Kalamos, Hugo, Shui, Kalam, Julius", "3d6 Gold Pieces|Rations (3 uses)|Torch (3 uses)|Quill & Ink|Blank Book|Awl (d6)|Badge (petty)"),
    )

    private fun background(id: String, name: String, names: String, equipment: String) = CharacterBackground(
        id = id,
        name = name,
        names = names.split(", "),
        startingEquipment = equipment.split("|").filterNot { it == "3d6 Gold Pieces" },
    )
}
