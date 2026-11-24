Legends of Valor

// launch

Windows PowerShell:

$files = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $files
java -cp out game.Main

macOS/Linux:

javac -d out $(find src -name '*.java')
java -cp out game.Main


// Gameplay

Choose three different heroes, assign each a different starting lane. Each round gives hero one valid action, then each monster acts once. Reach the monsters' Nexus to win; a monster reaching the heroes' Nexus ends the game in defeat.

| Command | Action |

| W/A/S/D | Move north/west/south/east |
| F | Attack one monster in range |
| C | Cast a spell |
| E | Equip or unequip weapon/armor |
| P | Use a potion |
| T | Teleport beside or behind an ally in another lane |
| R | Recall to your own Nexus |
| O | Clear an adjacent obstacle |
| X | Pass turn |
| M | Friendly Nexus market (buy/sell/repair) |
| I / V | Information / current hero's inventory |
| H / Q | Help / quit |
| B | Back from item and target selections |

Buying, selling, repairing, viewing information and invalid or cancelled choices do not consume a hero's action. 
The board shows both terrain and all occupants: N Nexus, I wall, X obstacle, P plain, B bush, C cave, K Koulou; H1–H3 are heroes and M1, M2, ... are monsters.

//Design choices

Combat, items, equipment, market, and data loading are reused from the previous game.

 Bush, Cave, and Koulou increase dexterity, agility, and strength by 10%, respectively.

-Defeating a monster grants each hero 100 × monster level gold and 2 × monster level EXP.

Living heroes recover 10% HP and MP each round. Dead heroes respawn at their original Nexus with full HP and MP when the spawn space is available.

Three monsters spawn initially, with additional waves every eight rounds. Monster levels match the highest hero level.

Monsters attack when heroes are in range; otherwise, they move south or wait if blocked.

// Integration checks

After compiling the source files, run:

javac -cp out -d out tests/ValorIntegrationTest.java
java -cp out ValorIntegrationTest


See `docs/design-evaluation.md` for the design evaluatio.