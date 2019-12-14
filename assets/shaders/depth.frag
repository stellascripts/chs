#version 130

float linearize(in float f) {
    float zNear = 1.0;
    float zFar = 20.0;
    float depth = f;
    return (2.0 * zNear) / (zFar + zNear - depth * (zFar - zNear));
}

void main() {
    gl_FragColor = vec4(vec3(linearize(gl_FragCoord.z)), 1.0);
}
