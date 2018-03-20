# Clipboard
The clipboard brush is used for copying and pasting parts of the world from one place to another.
Once copied, the clipboard can be rotated, flipped, translated, and have blocks replaced for others.

## Actions
### Copy Mode
- Left-click - set pos1
- Right-click - set pos2
- Sneak-left-click - clear points pos1 and pos2
- Sneak-right-click - copy the blocks between pos1 and pos2 to your clipboard and set the wand to paste mode

### Paste Mode
- Left-click - undo
- Right-click - paste
- Sneak-left-click - clear the clipboard and set the wand back to copy mode

## Options
- `flip`
 - `x` - flip the clipboard in the x direction
 - `y` - flip the clipboard in the y direction
 - `z` - flip the clipboard in the z direction
 - `auto` - flip the clipboard in the y-axis whilst looking up/down
 - `random` - randomly flip the clipboard in the x/z axes
- `rotate.auto` - rotate the clipboard with your facing direction
- `rotate.random` - randomly rotate the clipboard 0-270 degrees (in multiples of 90)
- `air.replace` - only replace air when pasting the clipboard
- `air.paste` - paste air contained in the clipboard
- `offset` - adjust the paste position by some x, y, z vector
- `paste` - change the 'paste mode' for the clipboard:
  - `normal` - pastes the clipboard at the position you are targeting
  - `surface` - finds the closest surface block and pastes the clipboard relative to it
  - `overlay` - overlays the clipboard on the surface