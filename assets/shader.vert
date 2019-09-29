#version 130
uniform mat3 W_N;
uniform mat4 W;
uniform mat4 V;
uniform mat4 P;
uniform vec4 in_light_position;
uniform vec4 in_light_color;
uniform vec4 in_ambient_color;

in vec3 in_position;
in vec2 in_tex_coords;
in vec3 in_normal;

out vec3 position;
out vec3 normal;
out vec4 light_position;
out vec4 light_color;
out vec4 ambient_color;

void main() {
    light_position = in_light_position;
    light_color = in_light_color;
    ambient_color = in_ambient_color;

    normal = normalize(W_N * in_normal);
    position = (W * vec4(in_position, 1.0)).xyz;
    gl_Position = P * V * W * vec4(in_position, 1.0);
}