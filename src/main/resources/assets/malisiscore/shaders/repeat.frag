#version 120

uniform sampler2D tex; 
uniform vec2 iconOffset; 
uniform vec2 iconSize;

void main() 
{
	gl_FragColor = texture2D(tex, iconOffset + fract(gl_TexCoord[0].st) * iconSize) * gl_Color;
}