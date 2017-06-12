## Usage
### Using a wand
A wand is an item (of your choosing) that, when right-clicked, will 'find' the first non-air block that you are looking at.

Wands search up to a limited range.  
The limit is 5 blocks by default and can be modified with the `/wand range <distance>` command.

If a solid block is not found within the range, the position _at_ the limit will be used instead (even if that position is an air block).

### Copying a volume of blocks
1. Select an item to use as your wand, hold it, and enter `/copy`
2. Right-click two points containing the volume of blocks you want to copy
3.  1. Sneak-click a third point to copy the volume - this third point becomes your clipboards 'origin'
    2. Click without sneaking to reset the points in order to start the selection again

Note - the direction you are facing is also recorded during step 3

### Pasting your clipboard
Once you have copied a volume of blocks to your clipboard, subsequent right-clicks with your wand will paste the
clipboard relative to the block that you target.

#### Auto rotate
Your clipboard will automatically rotate to the nearest cardinal direction (NSEW) as you change your facing direction.  
You can toggle this functionality by entering the command `/copy auto rotate`.

#### Auto flip
If you happened to be looking downwards whilst making the copy, then pasting whilst looking upwards will
flip your clipboard upside down and (vice versa if you were looking upwards whilst making the copy).  
You can toggle this functionality by entering the command `/copy auto flip`

#### Manual flip
You can manually set your clipboard to flip in one or more directions every time you paste by looking in the desired
flip direction and using the command `/copy flip`. Do so again to toggle the behaviour.

#### Random flips and rotations
You can set your clipboard to flip and/or rotate randomly with each paste using:  
`/copy random rotate` and `/copy random flip`.

Random flips will occur vertically if you are looking upwards or downwards when issuing the random flip command, otherwise
randomly on the horizontal (x/z) axes.

### Undoing pastes
Pastes can be 'un-done' by left-clicking with the wand.

Your clipboard records your 5 most recent pastes and the "undo's" are performed from most recent to oldest.

### Clearing the clipboard
Sneak-left click to clear your clipboard in-order to start a new selection.

**This also clears any undo history associated with the clipboard.**

