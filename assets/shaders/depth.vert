#version 130
uniform mat4 W;
uniform mat4 V;
uniform mat4 P;

in vec3 in_position;

void main() {
    vec4 v4p = P * V * W * vec4(in_position, 1.0);
    gl_Position = v4p;
}
