Monsters and Heroes

A Java turn-based RPG for CS611 using object-oriented design.
Players select 1–3 heroes, explore a random map, trade items, and battle monsters.

// Requirements
Java 11+
Legends_Monsters_and_Heroes/ data folder

Run
From the project root:

bash
javac -d out src/*.java
java -cp out Game.Main

// Controls
W/A/S/D: Move
M: Market
I: Information
V: Inventory
E: Equipment
P: Potion
H: Help
Q: Quit
B: Back

In battle, use A to attack and S to cast spells.

Map symbols: `P` (party), `M` (market), `#` (blocked), `.` (normal).

//Features
Random maps and monster encounters
Three hero classes and three monster classes
Turn-based combat, leveling, and rewards
Weapons, armor, spells, and potions
Markets for buying, selling, and repairing

// Design
Character → Hero / Monster
Item → Weapon / Armor / Potion / Spell
Game and Battle manage gameplay.
ConsoleUI handles input and output.
DataLoader reads TXT files.
WorldFactory and MonsterFactory generate game content.

// Limitations
No save/load feature.
Combat uses simplified balancing rules.
