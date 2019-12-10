==lighting==

::pass: depth::
phase: depth
vertex file: zero.vert
fragment file: zero.frag
blend mode: off
test depth?: true
write depth?: true
write color?: false
per light?: false

::pass: lighting::
phase: solid
vertex file: shader.vert
fragment file: shader.frag
blend mode: additive
test depth?: true
write depth?: false
write color?: true
per light?: true