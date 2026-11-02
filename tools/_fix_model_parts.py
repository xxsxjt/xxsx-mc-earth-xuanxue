import io
from pathlib import Path

MODELS = Path(r'D:\AGENT\WORKSPACES\Projects\mc-agent\earth-series\xuanhuan_earth'
              r'\neoforge-26.2\src\main\java\com\xxsx\xuanhuanearth\client\model')

# Crane: head is registered under neck > upper.
p = MODELS / 'EmberCraneModel.java'
s = io.open(p, encoding='utf-8').read()
old = '        this.head = this.neck.getChild("head");'
new = '        this.head = this.neck.getChild("upper").getChild("head");'
assert old in s, 'crane head line not found'
s = s.replace(old, new, 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('crane fixed')

# Goat: horns are three sibling segments; point the fields at the base segment.
p = MODELS / 'StonehornGoatModel.java'
s = io.open(p, encoding='utf-8').read()
old = ('        this.leftHorn = this.head.getChild("left_horn");\n'
       '        this.rightHorn = this.head.getChild("right_horn");')
new = ('        this.leftHorn = this.head.getChild("left_horn_base");\n'
       '        this.rightHorn = this.head.getChild("right_horn_base");')
assert old in s, 'goat horn lines not found'
s = s.replace(old, new, 1)
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('goat fixed')
