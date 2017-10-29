package me.dags.copy.operation;

/**
 * @author dags <dags@dags.me>
 */
public interface Operation {

    Phase calculate(int limit);

    Phase test(int limit);

    Phase apply(int limit);

    void dispose(Phase phase);

    enum Phase {
        CALCULATE,
        TEST,
        APPLY,
        DISPOSE,
        ERROR,
        CANCELLED,
        ;
    }
}
