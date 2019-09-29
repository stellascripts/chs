#version 130
in vec3 position;
in vec3 normal;
in vec4 light_position;
in vec4 light_color;
in vec4 ambient_color;

out vec4 frag_color;

void main() {
    vec3 light_direction = -light_position.xyz * (1.0 - light_position.w) +
    (light_position.xyz - position) * light_position.w;
    float light_length = length(light_direction);
    light_direction /= light_length;
    float light_distance = light_length*light_position.w + (1.0 - light_position.w);
    float light_intensity = (light_color.a * 0.18) / (light_distance*light_distance);

    vec4 diffuse = light_color * max(0.0, dot(normal, light_direction));
    frag_color = vec4(diffuse.xyz * light_intensity, 1.0) + ambient_color;
}