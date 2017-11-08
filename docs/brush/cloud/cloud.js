let shape = new Circle();
let octaves = 3;
let frequency = 0.01;

let height = 16;
let heightRange = (2 * height) + 1;
let offset = 4;
let detail = 4;
let density = 0.5;
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
  let noise0 = getValue(px, 0, pz, frequency, octaves) / maxNoise;
  let elevation = (noise0 * heightRange * hmod);

  let startY = -Math.round(elevation * center);
  let endY = startY + elevation;
  let sectionY = offset > 1 ? -1 : 0;
  let feather = getFeather(dist2, shape.featherRadius, shape.featherRange);

  let planAlpha = getAlpha(px, 0, pz, startY, endY, feather);
  buffer.drawPlan(x, z, 255, 255, 255, planAlpha);

  if (z === 0) {
    for (let dy = startY; dy <= endY; dy++) {
      let sectionAlpha = getAlpha(px, dy, pz, startY, endY, feather);
      buffer.drawSection(x, -dy, 255, 255, 255, sectionAlpha);
    }
  }
}

function getAlpha(x, y, z, mn, mx, feather) {
  let vmod = mod(y, y < 0.5 ? Math.min(-1, mn) : Math.max(1, mx));
  let noise = getValue(x * detail, y, z * detail, frequency, octaves) / maxNoise;
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
  register(createSlider('scale', 1, 96, 64, 1, (val) => frequency = 1 / val));
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
  register(createSlider('detail', 10, 50, 17, 10, (val) => detail = val));
  register(createSlider('height', 4, 48, 20, 1, (val) => {
    height = val;
    heightRange = (2 * height) + 1;
  }));
  register(createSlider('center', 0, 100, 30, 100, (val) => center = val));
  register(createSlider('bit-depth', 1, 32, 15, 1, (val) => bitrate = val));
}

function writeOutput(output) {
  let scale = Math.round(1 / frequency);
  output.value = `/set ${scale};${octaves};${shape.radius};${shape.feather};${density};${detail};${height};${center}`;
}
