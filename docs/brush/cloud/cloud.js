let brush = new Brush(perform);
let octaves = 3;
let frequency = 0.01;

let height = 16;
let offset = 4;
let detail = 4;
let density = 0.5;

let max = 1;
let range = 255;
let emptiness = 0;

let xOff = 0;
let zOff = 0;

function perform(buffer, x, z, dist2) {
  let px = x + xOff;
  let pz = z + zOff;

  let hmod = mod(dist2, brush.radius2);
  let opac = opacity(dist2, brush.featherRadius, brush.featherRange);
  let noise0 = getValue(px, 0, pz, frequency, octaves) / max;
  let elevation = (noise0 * height * hmod);

  let startY = -offset;
  let endY = offset + elevation;
  let sectionY = offset > 1 ? -1 : 0;

  for (let dy = startY; dy <= endY; dy++) {
    let denom = dy < 0 ? Math.min(-1, startY - 1) : Math.max(1, endY + 1);
    let vmod = mod(dy, denom);
    let noise1 = getValue(px * detail, dy, pz * detail, frequency, octaves) / max;
    let alpha = (range * noise1 * opac * vmod) - emptiness;
    
    alpha = clampAlpha(alpha * 1.5, 15); // clamps the alpha to a value 0-15. the x1.5 just brightens it up a little

    if (dy === sectionY) {
      buffer.setPlan(x, z, 255, 255, 255, alpha);
    }

    if (z === buffer.planCenterY) {
      buffer.setSection(x, buffer.sectionCenterY - dy, 255, 255, 255, alpha);
    }
  }
}

function updatePos(x, z) {
  brush.update();
  max = maxValue(octaves);
  emptiness = 255 * (1.0 - density);
  range = 255 + emptiness;
  xOff = x;
  zOff = z;
}

function mod(val, range) {
  let mod = val / range;
  return 1 - (mod * mod);
}

function opacity(val, bound, range) {
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

function register(controls) {
  controls.appendChild(slider('scale', 1, 96, 64, 1, function(val) {
    frequency = 1 /val;
  }));
  controls.appendChild(slider('octaves', 1, 6, 4, 1, function(val) {
    octaves = val;
  }));
  controls.appendChild(slider('radius', 8, 58, 45, 1, function(val) {
    brush.radius = val;
  }));
  controls.appendChild(slider('feather', 0, 100, 50, 100, function(val) {
    brush.feather = val;
  }));
  controls.appendChild(slider('density', 0, 100, 85, 100, function(val) {
    density = val;
  }));
  controls.appendChild(slider('detail', 10, 50, 15, 10, function(val) {
    detail = val;
  }));
  controls.appendChild(slider('height', 4, 48, 20, 1, function(val) {
    height = val;
  }));
  controls.appendChild(slider('y-offset', 0, 16, 3, 1, function(val) {
    offset = val;
  }));
  controls.appendChild(slider('speed', -10, 10, 1, 10, function(val) {
    increment = val;
  }));
}
