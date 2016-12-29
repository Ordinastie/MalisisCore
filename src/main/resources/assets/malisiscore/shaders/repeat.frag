#version 120

uniform sampler2D tex;

float interp(in float v, in float min, in float max)
{
	return min + v * (max - min);
}

void main() 
{
	vec2 min = gl_TexCoord[1].st;
	vec2 max = gl_TexCoord[1].pq;
	vec2 size = gl_TexCoord[2].st;
	vec2 borderSize = gl_TexCoord[2].pq;
	vec2 pos = gl_TexCoord[3].st;
	float border =  gl_TexCoord[3].p;
	
	
	vec2 t;
	bool c = false;
	if(pos.x < border)
	{
		t.x = interp(min.x, borderSize.x, pos.x / border);
		c = true;
	}
	else if(pos.x > size.x - border)
	{
		float l = size.x - border;
		float x  = 	(pos.x - l) / border;		
		t.x = interp(max.x - borderSize.x, max.x, x);
		c = true;
	}
	
	if(pos.y < border || pos.y > size.y - border)
	{
		float y = pos.y / size.y;
		//t.y = mix(y, min.y, max.y);
		c = true;
	}
	
	
//	if(!c)
//		t = gl_TexCoord[0].st;
	if(c) 
		gl_FragColor = texture2D(tex, t) * gl_Color;
	else
		gl_FragColor = vec4(0.2, 0.4, 0.7, 0.25);
}


