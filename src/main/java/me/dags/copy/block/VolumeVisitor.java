package me.dags.copy.block;

/**
 * @author dags <dags@dags.me>
 */
public interface VolumeVisitor {

    void visit(int x, int y, int z);
}
