package me.dags.copy.operation;

/**
 * @author dags <dags@dags.me>
 */
public interface Operation {

    boolean isCancelled();

    void calculate();

    void test();

    void apply();

    void dispose();
}
