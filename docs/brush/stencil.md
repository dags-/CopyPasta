# Stencil
The stencil brush converts an image to a line stencil that can then be pasted into the world with varying materials
 and heights.

## Actions
- Left-click - undo
- Right-click - paste
- Sneak-left-click - clear the current the stencil

## Options
Stencil inherits options from the Clipboard wand, plus:
- `depth` - the number layers the stencil should be stacked

## Stencil
To load a stencil you must provide a URL to an image (jpg/png):
```
/stencil <url> <samples> <threshold>
```
Where:
- `<samples>` - the number of pixels to be averaged into the space of a single block (smaller values produce larger
 stencils, larger values produce better quality lines but smaller stencils)
- `<threshold>` - controls how dark a pixel in the image must be to included as a block in the stencil. Values range
 from 0.0 to 1.0 (ie 0% to 100% black)

## Palette
The default material for the Stencil wand is white wool, you can however create a custom palette of materials that
 will be used randomly instead.

1. Clear your current palette:
```
/palette clear
```

2. Add a material to the palette:
```
/palette <blockstate> <weight>
```
Where:
- `<blockstate>` - the _full_ id of the material (ie `minecraft:stone[variant=stone]` - tab completions available!)
- `<weight>` - optional, the likelihood of the material being selected (relative to the other weights you provide) -
 defaults to 1 if not provided