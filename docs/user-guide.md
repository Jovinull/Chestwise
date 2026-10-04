# Chestwise user guide

## Getting started

Install the artifact matching both the Minecraft version and loader. Fabric and
Quilt also require the matching Fabric API. Craft a Storage Terminal with a
crafting table in the center, glass panes above and below, redstone on each side,
and iron ingots in the four corners. Place it in the storage room and open it.

By default, the terminal discovers up to 128 inventories in loaded chunks within
16 blocks on each axis. It stores no items itself. Breaking or unloading a
container removes it from the index after the bounded refresh cycle.

## Search and sorting

Plain words match localized item names or registry identifiers. Multiple words
must all match. `@create` restricts results to the `create` namespace;
`@minecraft` selects vanilla items; `#logs` requires the `logs` item tag.

Search updates after a five-tick client debounce. The `S` button cycles quantity,
name, namespace, and registry order. The last selected mode is stored locally.
The arrow buttons page through 45 results at a time.

## Moving items

- Left-click a result to take as much as fits on the cursor, up to one stack.
- Right-click a result to take half of one normal stack, or the remaining total
  when less is available.
- Shift-click a result to move one stack to the player inventory.
- Middle-click a result to show its nearest physical container with coordinates
  and a brief end-rod marker.
- Shift-click a player stack to deposit it.
- Middle-click a player inventory slot to protect or unprotect it from bulk
  deposit actions. Protected slot positions are stored in the local client
  preferences and restored when a terminal is opened again.

`Guardar Iguais` / Deposit Matching transfers only eligible, unprotected items
whose exact variant already exists in storage. This is the safe quick-stack
operation. Deposit All may also use empty space in other indexed inventories.
Neither operation recursively opens backpacks, shulker boxes, or other inventory
items stored inside containers. Deposit actions only inspect the 36 main
inventory/hotbar positions; equipped armor and offhand items are never eligible.

Optional wheel transfer is disabled by default. Set `mouseWheelTransfer=true`
in `config/chestwise-client.properties`; scrolling up over a result withdraws one
item and scrolling down deposits one exact matching item. The server validates
every operation.

## Restock targets

Open **Targets**, choose **Add item**, then click an item in your main inventory
or hotbar. Its target starts at one normal stack. Select a target to edit its
quantity or remove it; quantities may span multiple stacks. Press **Restock**
to request deficits from this terminal's nearby physical inventories.
Restock is best-effort: it takes only available items that fit and processes
each exact item variant independently. Targets are saved by player UUID in the
world's Overworld data, so they survive logout, death, dimension changes, and
server restarts.

Only the 36 main-inventory/hotbar slots are eligible. Protected slots count
toward a target, but Restock never changes them or inserts into them. Armor,
offhand, crafting slots, and items inside backpacks are not included. A target
is capped at the lower of 4,096 items or 36 stacks of that exact variant.

## Crafting

Use the 3 × 3 grid like a vanilla crafting table. Withdraw ingredients from the
indexed results, place them in the grid, and take the recipe output. Closing the
screen returns remaining grid contents through vanilla menu cleanup behavior.
Chestwise has no built-in recipe browser or recipe autofill. Optional compatible
recipe viewers can transfer recipes into the terminal's crafting grid; Chestwise
does not provide industrial autocrafting.
