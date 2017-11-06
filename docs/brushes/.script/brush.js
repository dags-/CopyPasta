class Brush {
  constructor(performer) {
    this.radius = 32;
    this.radius2 = 0;
    this.feather = 0.1;
    this.featherRadius = 0;
    this.featherRange = 0;
    this.performer = performer;
  }

  update() {
    this.radius2 = this.radius * this.radius;
    this.featherRadius = this.radius2 * (1 - this.feather);
    this.featherRange = this.radius2 - this.featherRadius;
  }

  iterate(buffer, width, height) {
    let cx = buffer.planCenterY;
    let cz = buffer.planCenterY - 1;
    for (let dz = 0; dz < this.radius; dz++) {
      for (let dx = 0; dx < this.radius; dx++) {
        let dist2 = (dx * dx) + (dz * dz);
        if (dist2 > this.radius2) {
          continue;
        }
        this.performer(buffer, cx + dx, cz + dz, dist2);
        if (dx !== 0) {
          this.performer(buffer, cx - dx, cz + dz, dist2);
        }
        if (dz !== 0) {
          this.performer(buffer, cx + dx, cz - dz, dist2);
        }
        if (dx !== 0 && dz !== 0) {
          this.performer(buffer, cx - dx, cz - dz, dist2);
        }
      }
    }
  }
}
