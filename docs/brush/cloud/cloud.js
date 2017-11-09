let shape = new Circle();
let scale = 64;
let scaleFrequency = 1 / scale;
let octaves = 3;

let height = 16;
let heightRange = (2 * height) + 1;
let density = 0.5;
let turbulence = 0.3;
let turbulenceFrequency = turbulence * 0.1;
let center = 0.5;

let maxNoise = 1;
let range = 255;
let air = 0;
let bitrate = 15;

let xOff = 0;
let zOff = 0;

function draw(buffer) {
  shape.iterate(perform);
}

function perform(buffer, x, z, dist2) {
  let px = x + xOff;
  let pz = z + zOff;

  let hmod = mod(dist2, shape.radius2);
  let elevation = getElevation(px, pz, hmod);

  let lower = -Math.max(1, Math.round(elevation * center));
  let upper = lower + elevation;
  let feather = getFeather(dist2, shape.featherRadius, shape.featherRange);

  let plan = getAlpha(px, 0, pz, lower, upper, feather);
  buffer.drawPlan(x, z, 255, 255, 255, plan);

  if (z === 0) {
    for (let dy = lower; dy <= upper; dy++) {
      let section = getAlpha(px, dy, pz, lower, upper, feather);
      buffer.drawSection(x, -dy, 255, 255, 255, section);
    }
  }
}

function getElevation(x, z, hmod) {
  let noise = getValue(x, 0, z, scaleFrequency, octaves) / maxNoise;
  return noise * heightRange * hmod;
}

function getAlpha(x, y, z, lower, upper, feather) {
  let vmod = mod(y, y < 0.5 ? lower : upper);
  let noise = getValue(x, y, z, turbulenceFrequency, octaves) / maxNoise;
  let alpha = (range * noise * feather * vmod) - air;
  return clampAlpha(alpha * 1.5, bitrate);
}

function getFeather(val, bound, range) {
  if (val > bound) {
    let d = val - bound;
    return 1 - (d / range);
  }
  return 1;
}

function clampAlpha(value, bitrate) {
    let perc = value / 255.0;
    let steps = Math.round(bitrate * perc);
    return Math.min(255, Math.max(0, steps * Math.round(255 / bitrate)));
}

function mod(val, range) {
  let mod = val / range;
  return 1 - (mod * mod);
}

function setPos(x, z) {
  xOff = x;
  zOff = z;
}

function registerOptions(register) {
  register(createSlider('scale', 8, 128, 64, 1, (val) => {
    scale = val;
    scaleFrequency = 1 / scale;
  }));
  register(createSlider('octaves', 1, 6, 4, 1, (val) => {
    octaves = val;
    maxNoise = maxValue(octaves);
  }));
  register(createSlider('radius', 8, 70, 55, 1, (val) => shape.radius = val));
  register(createSlider('feather', 0, 100, 50, 100, (val) => shape.feather = val));
  register(createSlider('density', 0, 100, 85, 100, (val) => {
    density = val;
    air = 255 * (1.0 - density);
    range = 255 + air;
  }));
  register(createSlider('turbulence', 0, 100, 25, 100, (val) => {
    turbulence = val;
    turbulenceFrequency = turbulence * 0.1;
  }));
  register(createSlider('height', 4, 48, 20, 1, (val) => {
    height = val;
    heightRange = (2 * height) + 1;
  }));
  register(createSlider('center', 0, 100, 30, 100, (val) => center = val));
  register(createSlider('bit-depth', 1, 32, 15, 1, (val) => bitrate = val));
}

function writeOutput(output) {
  output.value = `/set ${scale};${octaves};${shape.radius};${shape.feather};${density};${turbulence};${height};${center}`;
}
