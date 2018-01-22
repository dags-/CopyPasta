class Circle {
  constructor() {
    this.radius = 32;
    this.radius2 = 0;
    this.feather = 0.1;
    this.featherRadius = 0;
    this.featherRange = 0;
  }

  iterate(performer) {
    this.radius2 = this.radius * this.radius;
    this.featherRadius = this.radius2 * (1 - this.feather);
    this.featherRange = this.radius2 - this.featherRadius;

    for (let dz = 0; dz < this.radius; dz++) {
      for (let dx = 0; dx < this.radius; dx++) {
        let dist2 = (dx * dx) + (dz * dz);
        if (dist2 > this.radius2) {
          continue;
        }
        performer(buffer, dx, dz, dist2);
        if (dx !== 0) {
          performer(buffer, -dx, dz, dist2);
        }
        if (dz !== 0) {
          performer(buffer, dx, -dz, dist2);
        }
        if (dx !== 0 && dz !== 0) {
          performer(buffer, -dx, -dz, dist2);
        }
      }
    }
  }
}
