#version 150

uniform sampler2D DiffuseSampler;
uniform float Progress;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 scene = texture(DiffuseSampler, texCoord);
    float luminance = dot(scene.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec3 coldMonochrome = vec3(
        luminance * 0.66,
        luminance * 0.86,
        min(1.0, luminance * 1.12 + 0.03)
    );
    vec3 color = mix(scene.rgb, coldMonochrome, 0.64 * Progress);

    vec2 centered = texCoord - vec2(0.5);
    float vignette = smoothstep(0.32, 0.72, length(centered));
    color = mix(color, vec3(0.028, 0.052, 0.078), vignette * 0.22 * Progress);

    fragColor = vec4(color, 1.0);
}
