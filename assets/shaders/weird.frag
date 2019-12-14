#version 130
in vec3 position;
in vec3 normal;
in vec4 light_position;
in vec4 light_color;
in vec4 ambient_color;

vec3 getLightDirection() {
    return (light_position.xyz - position) * light_position.w -
    light_position.xyz * (1.0 - light_position.w);
}

vec4 gammaConvert(vec4 linear) {
    return vec4(pow(linear.rgb, vec3(1.0/2.2)), linear.a);
}

void main() {
    vec3 N = normalize(normal);
    vec3 V = normalize(-position);
    vec3 L = getLightDirection();
    float dist = 1.0/length(L);
    L *= dist;
    dist = (dist*dist) * light_position.w + (1.0 - light_position.w);

    float diffuse = round(max(dot(L,N),0.0));
    float rim = pow(1.0 - dot(N, V), 5.0);

    gl_FragColor = gammaConvert(light_color * (rim + diffuse) * dist + ambient_color);
}