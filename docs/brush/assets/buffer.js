class Buffer {
  constructor(image, planHeight) {
    let buf = new ArrayBuffer(image.data.length);
    this.image = image;
    this.buf8 = new Uint8ClampedArray(buf);
    this.data = new Uint32Array(buf);
    this.planStart = 0;
    this.planEnd = planHeight;
    this.sectionStart = this.planEnd;
    this.sectionEnd = image.height;

    this.planOffsetX = (image.width / 2) - 1;
    this.planOffsetY = planHeight / 2;

    this.sectionOffsetX = image.width / 2;
    this.sectionOffsetY = planHeight + ((image.height - planHeight) / 2);
  }

  drawPlan(x, z, r, g, b, a) {
    x += this.planOffsetX;
    z += this.planOffsetY;
    if (this.checkBounds(x, z, 0, 0, this.image.width, this.planEnd)) {
      this.setRGBA(x, z, r, g, b, a);
    }
  }

  drawSection(x, y, r, g, b, a) {
    x += this.sectionOffsetX;
    y += this.sectionOffsetY;
    if (this.checkBounds(x, y, 0, this.sectionStart, this.image.width, this.sectionEnd)) {
      this.setRGBA(x, y, r, g, b, a);
    }
  }

  apply(canvas, context) {
    let left = (canvas.width - this.image.width) / 2
    this.image.data.set(this.buf8);
    context.putImageData(this.image, 0, 0);
  }

  drawSectionLine(r, g, b, a) {
    this.drawLine(0, this.planOffsetY, this.image.width - 1, this.planOffsetY, r, g, b, a);
  }

  drawSeparator(r, g, b, a) {
    this.drawLine(0, this.planEnd - 1, this.image.width - 1, this.planEnd + 1, r, g, b, a);
  }

  checkBounds(x, y, minX, minY, maxX, maxY) {
    return x >= minX && x < maxX && y >= minY && y < maxY;
  }

  setRGB(x, y, r, g, b) {
    let i = (y * this.image.width + x);
    this.data[i] = (255 << 24) | (b << 16) | (g << 8) | r;
  }

  setRGBA(x, y, r, g, b, a) {
    let i = (y * this.image.width + x);
    let c = this.data[i];

    let r0 = c & 0xFF;
    let g0 = (c >> 8) & 0xFF;
    let b0 = (c >> 16) & 0xFF;

    let dst = a / 255;
    let src = 1 - dst;

    r0 = (r0 * src) + (r * dst);
    g0 = (g0 * src) + (g * dst);
    b0 = (b0 * src) + (b * dst);

    this.data[i] = (255 << 24) | (b0 << 16) | (g0 << 8) | r0;
  }

  fill(r, g, b) {
    for (let i = 0; i < this.data.length; i++) {
      this.data[i] = (255 << 24) | (b << 16) | (g << 8) | r;
    }
  }

  drawLine(x0, y0, x1, y1, r, g, b, a) {
    for (let x = x0; x <= x1; x++) {
      for (let y = y0; y <= y1; y++) {
        this.setRGBA(x, y, r, g, b, a);
      }
    }
  }
}
