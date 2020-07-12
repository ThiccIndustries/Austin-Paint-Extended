# Austin Paint Extended
The useless editor for a useless, proprietary format.

By reading the source code you agree to not sue me for any strokes that may occur

## Application Controls
###  General:
|Key| Function
|--|--|
|Plus| Increase window scale
|Minus| Decrease window scale
|Escape| Draw mode
|Shift| Selection mode
|E| Palette edit mode
|CTRL-O| Open file
[CTRL-S| Save File
###  Drawing:
|Key| Function
|--|--|
|CTRL-Z| Undo
|CTRL-F| Fill color
|Left Bracket| Previous preset palette
|Right Bracket| Next preset palette
|Z|Lock Vertical
|X|Lock Horizontal
###  Selection:
|Key| Function
|--|--|
|Left Mouse| Set Point (First point must be Top-Left)
|Arrow Keys| Move copy of selection
|Enter| Paste selection
###  Palette Mode:
|Key| Function
|--|--|
|H| Enter exact Hex value

## Austin Paint 2 (.ap2) Format:
|Structure|Bytes  |Description
|--|--|--|
| Watermark | 0x00 - 0x0F |Austin Paint Watermark
|Color Table|0x10 - 0x3F|Contains 3 bytes for each of the 16 colors in an RR GG BB format|
|Pixel Data|0x40 - 0x230|4 Bits for each pixel corresponding to the index for the color of the pixel
