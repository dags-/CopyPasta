let brush = new Brush(perform);
let max = 0;
let octaves = 3;
let frequency = 0.01;
let terrXOff = 0;
let terrZOff = 0;

function perform(visitor, x, z, dist2) {
  let noise = getValue(x + terrXOff, 0, z + terrZOff, frequency, octaves) / max;
  let height = Math.round(noise * 255);

  visitor.setPlan(x, z, height, height, height, 255);

  if (z === visitor.planCenterY) {
    let floor = visitor.section.height;
    let scaleHeight = Math.round(noise * floor * 2);
    for (var y = scaleHeight; y > 0; y--) {
      let gray = getGray(y, floor);
      visitor.setSection(x, floor - y, 128, 128, 128, 255);
    }
  }
}

function getGray(value, range) {
  let steps = 8;
  let perc = value / range;
  let step = Math.round(steps * perc);
  return step * (range / steps);
}

function updatePos(x, z) {
  brush.update();
  terrXOff = x;
  terrZOff = z;
  max = maxValue(octaves);
}

function register(controls) {
  controls.appendChild(slider('scale', 1, 96, 32, 1, function(val) {
    frequency = 1 / val;
  }));
  controls.appendChild(slider('octaves', 1, 8, 3, 1, function(val) {
    octaves = val;
  }));
  controls.appendChild(slider('radius', 8, 48, 32, 1, function(val) {
    brush.radius = val;
  }));
  controls.appendChild(slider('feather', 0, 100, 50, 100, function(val) {
    brush.feather = val;
  }));
}
