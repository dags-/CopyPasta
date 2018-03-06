package me.dags.copy.brush.option.value;

import me.dags.config.Node;
import me.dags.copy.brush.option.Option;

/**
 * @author dags <dags@dags.me>
 */
public class Flip implements Node.Value<Flip> {

    public static final Flip DEFAULT = new Flip();
    public static final Option<Flip> OPTION = Option.of("flip", DEFAULT);

    private boolean x = false;
    private boolean y = false;
    private boolean z = false;
    private boolean auto = true;
    private boolean random = false;

    public boolean flipX() {
        return x;
    }

    public boolean flipY() {
        return y;
    }

    public boolean flipZ() {
        return z;
    }

    public boolean auto() {
        return auto;
    }

    public boolean random() {
        return random;
    }

    public void setX(boolean x) {
        this.x = x;
    }

    public void setY(boolean y) {
        this.y = y;
    }

    public void setZ(boolean z) {
        this.z = z;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public void setRandom(boolean random) {
        this.random = random;
    }

    @Override
    public void toNode(Node node) {
        node.set("x", x);
        node.set("y", y);
        node.set("z", z);
        node.set("auto", auto);
        node.set("random", random);
    }

    @Override
    public Flip fromNode(Node node) {
        Flip flip = new Flip();
        flip.setX(node.get("x", false));
        flip.setY(node.get("y", false));
        flip.setZ(node.get("z", false));
        flip.setAuto(node.get("auto", false));
        flip.setRandom(node.get("random", false));
        return flip;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        append(sb, x, "x");
        append(sb, y, "y");
        append(sb, z, "z");
        append(sb, auto, "auto");
        append(sb, random, "random");
        append(sb, sb.length() == 0, "none");
        return sb.toString();
    }

    private static void append(StringBuilder sb, boolean predicate, String next) {
        if (!predicate) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(",");
        }
        sb.append(next);
    }
}
