# Replace
The replacer brush matches certain blocks/blockstates and replaces them with others, whilst keeping common properties
 the same. This allows you to replace stairs from one type to another, without messing up their orientation, in one go.

## Actions
- Left-click - under
- Right-click - apply

## Options
- `radius` - the radius of the brush

## Mappers
```
/mapper <match_rule> <replace_rule>
```
_You can use this command multiple times to add multiple rules to your brush._

To remove all rules, use:
```
/mapper clear
```

### Example Rules

1. Replace all oak stairs with acacia stairs:  
`/mapper minecraft:oak_stairs minecraft:acacia_stairs`

2. Replace all bottom-half oak stairs with acacia:  
`/mapper minecraft:oak_stairs[half=bottom] minecraft:acacia_stairs`

3. Replace all bottom-half oak stairs with top-half acacia stairs:  
`/mapper minecraft:oak_stairs[half=bottom] minecraft:acacia_stairs[half=top]`

4. Replace any block that possess the 'facing' property (doors, stairs etc) with acacia stairs:  
`/mapper *[facing=*] minecraft:acacia_stairs`

5. Replace the direction of any north-facing block with the south-facing variant:  
`/mapper *[facing=north] *[facing=south]`