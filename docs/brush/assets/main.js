let canvas;
let context;
let buffer;
let command;

let xIncrement = 0;
let zIncrement = 0;
let xOffset = Math.random() * 1024;
let zOffset = Math.random() * 1024;

function init() {
  console.log('initializing!');

  // init canvas & buffer
  canvas = document.getElementById('canvas');
  context = canvas.getContext('2d');
  buffer = createBuffer();

  // add sliders etc
  let controls = document.getElementById('controls');
  let registrar = (child) => controls.appendChild(child);

  // add option controls
  controls.appendChild(createTitle('Options:'));
  registerOptions(registrar);

  // add animation controls
  registrar(createTitle('Animation:'));
  registrar(createSlider('x-speed', -10, 10, -1, 10, (val) => xIncrement = val));
  registrar(createSlider('z-speed', -10, 10, 2, 10, (val) => zIncrement = val));

  // add output text area
  command = createOuputBox();
  registrar(createTitle('Command:'));
  registrar(command);
  writeOutput(command);

  // begin update loop
  setInterval(update, 20);

  // begin render loop
  render();
}

function update() {
  xOffset += xIncrement;
  zOffset += zIncrement;
}

function render() {
  // background
  buffer.fill(100, 180, 235);
  // position & draw clouds
  setPos(xOffset, zOffset);
  draw(buffer);
  // draw line on plan where section is taken
  buffer.drawSectionLine(220, 0, 180, 64);
  // draw space between plan and section views
  buffer.drawSeparator(255, 255, 255, 255);
  // draw the buffer to the canvas
  buffer.apply(canvas, context);

  if (xIncrement !== 0 || zIncrement !== 0) {
    // request next frame
    window.requestAnimationFrame(render);
  }
}

function createBuffer() {
  let image = context.createImageData(canvas.width + 1, canvas.height);
  return new Buffer(image, canvas.width - 1);
}

function createTitle(title) {
  let heading = document.createElement('h3');
  heading.innerText = title;
  return heading;
}

function createOuputBox() {
  let box = document.createElement('textArea');
  box.setAttribute('readonly', 'readonly');
  box.onclick = () => box.select();
  return box;
}

function createSlider(id, min, max, val, fact, callback) {
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
    let rerender = xIncrement === 0 && zIncrement === 0;
    let val = this.value / fact;
    if (fact > 1) {
        val = val.toFixed(2);
    }
    label.innerText = id + ":" + val;
    callback(val);
    writeOutput(command);
    if (rerender) {
        render();
    }
  }

  control.appendChild(slider);
  control.appendChild(label);
  callback(val / fact);

  return control;
}
