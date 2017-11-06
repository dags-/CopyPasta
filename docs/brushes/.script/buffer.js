class Buffer {
  constructor(plan, section) {
    this.plan = plan;
    this.section = section;
    this.planCenterY = plan.height / 2;
    this.sectionCenterY = section.height / 2;
  }

  width() {
    return this.plan.width + 1;
  }

  height() {
    return this.plan.height;
  }

  setPlan(x, z, r, g, b, a) {
    if (Buffer.checkBounds(this.plan, x, z)) {
      Buffer.setRGBA(this.plan, x, z, r, g, b, a);
    }
  }

  setSection(x, y, r, g, b, a) {
    if (Buffer.checkBounds(this.section, x, y)) {
      Buffer.setRGBA(this.section, x, y, r, g, b, a);
    }
  }

  apply(canvas, context) {
    let left = (canvas.width - this.plan.width) / 2
    context.putImageData(this.plan, left, 0);
    context.putImageData(this.section, left, this.plan.height);
  }

  setBackground(r, g, b) {
    Buffer.fill(this.plan, r, g, b);
    Buffer.fill(this.section, r, g, b);
  }

  drawSectionLine(r, g, b, a) {
    Buffer.drawLine(this.plan, 0, this.planCenterY, this.plan.width - 1, this.planCenterY, r, g, b, a);
  }

  drawSeparator(r, g, b, a) {
    Buffer.drawLine(this.plan, 0, this.plan.height - 1, this.plan.width - 1, this.plan.height - 1, r, g, b, a);
    Buffer.drawLine(this.section, 0, 0, this.section.width - 1, 0, r, g, b, 255);
  }

  static checkBounds(image, x, y) {
    return x >= 0 && x < image.width && y >= 0 && y < image.height;
  }

  static setRGB(image, x, y, r, g, b, a) {
    let i = (y * image.width + x) * 4;
    image.data[i] = r;
    image.data[i + 1] = g;
    image.data[i + 2] = b;
    image.data[i + 3] = 255;
  }

  static setRGBA(image, x, y, r, g, b, a) {
    let i = (y * image.width + x) * 4;
    let sr = image.data[i];
    let sg = image.data[i + 1];
    let sb = image.data[i + 2];
    let dst = a / 255;
    let src = 1 - dst;
    image.data[i] = (sr * src) + (r * dst);
    image.data[i + 1] = (sg * src) + (g * dst);
    image.data[i + 2] = (sb * src) + (b * dst);
  }

  static fill(image, r, g, b) {
    for (let dz = 0; dz < image.height; dz++) {
      for (let dx = 0; dx < image.width; dx++) {
        Buffer.setRGB(image, dx, dz, r, g, b, 255);
      }
    }
  }

  static drawLine(image, x0, y0, x1, y1, r, g, b, a) {
    for (let x = x0; x <= x1; x++) {
      for (let y = y0; y <= y1; y++) {
        Buffer.setRGBA(image, x, y, r, g, b, a);
      }
    }
  }
}
