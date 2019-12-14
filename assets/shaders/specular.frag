#version 130
in vec3 position;
in vec3 normal;

in vec4 light_position;
in vec4 light_color;
in vec4 ambient_color;

out vec4 frag_color;

const float gamma = 2.2;

vec3 getLightDirection(vec4 light) {
    return (light.xyz - position) * light.w - light.xyz * (1.0 - light.w);
}

void main() {
    vec3 N = normalize(normal);
    vec3 lightDir = getLightDirection(light_position);
    float lightDist = length(lightDir);
    lightDir /= lightDist;
    lightDist *= lightDist;

    vec3 light = light_color.rgb * light_color.a / lightDist;

    float diffuse = max(dot(lightDir, normal), 0.0);

    vec3 viewDir = normalize(-position);
    vec3 halfDir = normalize(lightDir + viewDir);
    float specAngle = max(dot(halfDir, normal), 0.0);
    float specular = pow(specAngle, 10.0);

    vec3 linearColor = ambient_color.xyz * ambient_color.a +
    diffuse * light +
    specular * light;
    gl_FragColor = vec4(pow(linearColor, vec3(1.0/gamma)), 1.0);
    //gl_FragColor = vec4(viewDir - vec3(0.5), 1.0);
}