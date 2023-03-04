int bars=30; // = 30
float amplitude=2.0; // = 2
float noise=0.1; // = 0.1
float frequency=0.5; // = 0.5
float dripScale=0.5; // = 0.5

float rand(int num) {
    return fract(mod(float(num) * 67123.313, 12.0) * sin(float(num) * 10.3) * cos(float(num)));
}

float wave(int num) {
    float fn = float(num) * frequency * 0.1 * float(bars);
    return cos(fn * 0.5) * cos(fn * 0.13) * sin((fn+10.0) * 0.3) / 2.0 + 0.5;
}

float drip(int num) {
    return sin(float(num) / float(bars - 1) * 3.141592) * dripScale;
}

float pos(int num) {
    return (noise == 0.0 ? wave(num) : mix(wave(num), rand(num), noise)) + (dripScale == 0.0 ? 0.0 : drip(num));
}

vec4 transition(vec2 uv) {
    int bar = int(uv.x * (float(bars)));
    float scale = 1.0 + pos(bar) * amplitude;
    float phase = progress * scale;
    float posY = uv.y / vec2(1.0).y;
    vec2 p;
    vec4 c;
    if (phase + posY < 1.0) {
        p = vec2(uv.x, uv.y + mix(0.0, vec2(1.0).y, phase)) / vec2(1.0).xy;
        c = getFromColor(p);
    } else {
        p = uv.xy / vec2(1.0).xy;
        c = getToColor(p);
    }
    return c;
}