package me.dags.copy.clipboard;

import me.dags.copy.block.Facing;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Random;

/**
 * @author dags <dags@dags.me>
 */
public class ClipboardOptions {

    private static final Random RANDOM = new Random();

    private Facing clipboardFacingH = Facing.north;
    private Facing clipboardFacingV = Facing.north;
    private Facing playerHorizontalFacing = Facing.north;
    private Facing playerVerticalFacing = Facing.up;
    private boolean pasteAir = false;
    private boolean autoRotate = true;
    private boolean autoFlip = true;
    private boolean flipX = false;
    private boolean flipY = false;
    private boolean flipZ = false;
    private boolean randomRotate = false;
    private boolean randomFlipH = false;
    private boolean randomFlipV = false;
    private StateMapper stateMapper = StateMapper.EMPTY;

    public boolean pasteAir() {
        return pasteAir;
    }

    public boolean autoRotate() {
        return autoRotate;
    }

    public boolean autoFlip() {
        return autoFlip;
    }

    public boolean flipX() {
        return flipX;
    }

    public boolean flipY() {
        return flipY;
    }

    public boolean flipZ() {
        return flipZ;
    }

    public boolean randomRotate() {
        return randomRotate;
    }

    public boolean randomFlipH() {
        return randomFlipH;
    }

    public boolean randomFlipV() {
        return randomFlipV;
    }

    public void setStateMapper(StateMapper stateMapper) {
        if (stateMapper == null) {
            stateMapper = StateMapper.EMPTY;
        }
        this.stateMapper = stateMapper;
    }

    public void setPasteAir(boolean pasteAir) {
        this.pasteAir = pasteAir;
    }

    public void setAutoFlip(boolean autoFlip) {
        this.autoFlip = autoFlip;
    }

    public void setAutoRotate(boolean autoRotate) {
        this.autoRotate = autoRotate;
    }

    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }

    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }

    public void setFlipZ(boolean flipZ) {
        this.flipZ = flipZ;
    }

    public void setRandomRotate(boolean randomRotate) {
        this.randomRotate = randomRotate;
    }

    public void setRandomFlipH(boolean randomFlipH) {
        this.randomFlipH = randomFlipH;
    }

    public void setRandomFlipV(boolean randomFlipV) {
        this.randomFlipV = randomFlipV;
    }

    public void setClipboardFacingH(Facing horizontal, Facing vertical) {
        this.clipboardFacingH = horizontal;
        this.clipboardFacingV = vertical;
    }

    public void setPlayerFacing(Player player) {
        this.playerHorizontalFacing = Facing.horizontalFacing(player);
        this.playerVerticalFacing = Facing.verticalFacing(player);
    }

    public VolumeMapper createMapper() {
        int angle = 0;
        boolean flipX = this.flipX;
        boolean flipY = this.flipY;
        boolean flipZ = this.flipZ;

        if (autoRotate) {
            int from = clipboardFacingH.getAngle();
            int to = playerHorizontalFacing.getAngle();
            angle = Facing.clampAngle(to - from);
        }

        if (randomRotate) {
            int turns = RANDOM.nextInt(4);
            angle = turns * 90;
        }

        if (autoFlip && playerVerticalFacing != Facing.horizontal && clipboardFacingV != Facing.horizontal) {
            flipY = playerVerticalFacing != clipboardFacingV;
        }

        if (randomFlipH) {
            flipX = RANDOM.nextBoolean();
            flipZ = RANDOM.nextBoolean();
        }

        if (randomFlipV) {
            flipY = RANDOM.nextBoolean();
        }

        return new VolumeMapper(angle, flipX, flipY, flipZ, stateMapper);
    }
}
