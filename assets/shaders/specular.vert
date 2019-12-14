#version 130
uniform mat3 W_N;
uniform mat4 W;
uniform mat4 V;
uniform mat4 P;
uniform vec4 in_light_position;
uniform vec4 in_light_color;
uniform vec4 in_ambient_color;

in vec3 in_position;
in vec3 in_normal;
in vec2 in_tex_coords;

invariant gl_Position;
out vec3 position;
out vec3 normal;
out vec4 light_position;
out vec4 light_color;
out vec4 ambient_color;

void main() {
    light_position = V * in_light_position;
    light_color = in_light_color;
    ambient_color = in_ambient_color;

    normal = W_N * in_normal;
    vec4 pos = V * W * vec4(in_position, 1.0);
    position = pos.xyz / pos.w;
    gl_Position = P * pos;
}