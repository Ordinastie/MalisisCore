#version 120

#define ONEV2 vec2(1, 1)

uniform sampler2D tex;

float interp(in float v, in float min, in float max)
{
	return min + v * (max - min);
}
vec2 interp(in vec2 v, in vec2 min, in vec2 max)
{
	return min + v * (max - min);
}

void main() 
{
	vec2 pos = gl_TexCoord[0].pq;
	vec2 uv = pos;
	
 	vec2 minUV = gl_TexCoord[1].st; // min/max texture UV coordinates for that component
	vec2 maxUV = gl_TexCoord[1].pq;
	
    vec2 textureSize = gl_TexCoord[2].st; // total size of the texture in pixels
    vec2 quadSize = gl_TexCoord[2].pq; // total component size in pixels
    
    vec2 borderSize = gl_TexCoord[3].ss; // border size in pixels, specifies how many pixels on screen will be used for border    
    vec2 borderUV = borderSize / textureSize; // size of border in texture UV coordinates, specifies which parts of textures are used as border


//	minUV = vec2(100.0/300.0, 70.0/100.0);
//	maxUV = vec2(120.0/300.0, 90.0/100.0);
//	minUV = vec2(0, 0);
//	maxUV = vec2(1, 1);
//	textureSize = vec2(300.0, 100.0);
//	quadSize = vec2(100.0, 100.0);
//	borderSize = vec2(2.0, 2.0);
//	borderUV = borderSize / textureSize;
	
	
	vec4 white = vec4(1, 1, 1, 1);
	vec4 red = vec4(1, 0, 0, 1);
	vec4 yellow = vec4(1, 1, 0, 1);
	
	vec4 color = white;
	vec4 border = red;
	
	// inputs - end

    vec2 renderSizeToTextureSizeRatio = quadSize/textureSize;
    
    // scale the uv coordinates to tile the center texture
    vec2 uvOut = (pos-borderUV) * renderSizeToTextureSizeRatio;
    // and tile the texture
    uvOut = mod(uvOut, vec2(1.0, 1.0));
   	// use the tiled uv that is between 0 and 1 to interpolate between 
    // minUV + borderUV and maxUV - borderUV
    uvOut = mix(minUV + borderUV, maxUV - borderUV, uvOut);
    
    // texture coordinates  that would be used if it was border
    vec2 minBorderOut = mix(minUV, minUV+borderUV, pos/borderUV);
    vec2 maxBorderOut = mix(maxUV, maxUV-borderUV, (ONEV2 - pos)/borderUV);
    
    // set texture coordinates to border coordinates if it's actually in borders
    if(pos.x < borderUV.x) {
        uvOut.x = minBorderOut.x;
    }
    
    if(pos.x > 1.0 - borderUV.x) {
        uvOut.x = maxBorderOut.x;
    }
    
    if(pos.y < borderUV.y) {
        uvOut.y = minBorderOut.y;
    }
    
    if(pos.y > 1.0 - borderUV.y) {
        uvOut.y = maxBorderOut.y;
    }

	//uvOut = mix(minUV, maxUV, pos);
	//uvOut = gl_TexCoord[0].st; 
	//uvOut = pos;
	//uvOut.x =  mix(minUV.x, maxUV.x, pos.x);
	//uvOut.y =  mix(minUV.y, maxUV.y, pos.y);
	gl_FragColor = texture2D(tex, uvOut) * color; 
	
	
}
