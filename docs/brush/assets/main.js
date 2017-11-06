const canvas = document.getElementById('preview');
const context = canvas.getContext('2d');
const buffer = createBuffer();
const frameRate = 1000 / 30;

let xOffset = Math.random() * 2048;
let zOffset = Math.random() * 2048;
let increment = 0;
let time = 0;
let animate = true;

function init() {
  console.log('initializing!');
  let controls = document.getElementById('controls');
  register(controls);
  setInterval(loop, 20);
  draw();
}

function loop() {
  zOffset += increment;

  let date = new Date();
  if (date.getTime() - time > frameRate) {
    time = date.getTime();
    draw();
  }
}

function draw() {
  updatePos(xOffset, zOffset);
  buffer.setBackground(100, 180, 235);
  brush.iterate(buffer, buffer.width(), buffer.height());
  buffer.drawSectionLine(220, 0, 180, 64);
  buffer.drawSeparator(255, 255, 255, 255);
  buffer.apply(canvas, context);
}

function createBuffer() {
  let planHeight = Math.round(canvas.width / 2) * 2;
  let planWidth = planHeight + 1;
  let sectionHeight = canvas.height - planHeight;
  let plan = context.createImageData(planWidth, planHeight);
  let section = context.createImageData(planWidth, sectionHeight);
  return new Buffer(plan, section);
}

function slider(id, min, max, val, fact, callback) {
  let control = document.createElement('div');
  control.classList.add('control');

  let slider = document.createElement('input');
  let label = document.createElement('label');
  label.innerText = id + ":" + (val / fact);

  slider.setAttribute('id', id);
  slider.setAttribute('type', 'range');
  slider.setAttribute('min', min);
  slider.setAttribute('max', max);
  slider.setAttribute('value', val);
  slider.oninput = function() {
    let val = this.value / fact;
    label.innerText = id + ":" + val;
    callback(val);
    if (increment === 0) {
      draw();
    }
  }
  control.appendChild(slider);
  control.appendChild(label);
  callback(val / fact);

  return control;
}
