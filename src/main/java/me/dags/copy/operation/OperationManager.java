package me.dags.copy.operation;

import java.util.LinkedList;

/**
 * @author dags <dags@dags.me>
 */
public class OperationManager implements Runnable {

    private final int preLimit;
    private final int changeLimit;
    private final Object lock = new Object();
    private final LinkedList<Operation> calculate = new LinkedList<>();
    private final LinkedList<Operation> test = new LinkedList<>();
    private final LinkedList<Operation> apply = new LinkedList<>();

    private boolean finishing = false;

    public OperationManager(int pre, int change) {
        this.preLimit = pre;
        this.changeLimit = change;
    }

    public void reset() {
        finishing = false;
    }

    public void queueOperation(Operation operation) {
        if (finishing) {
            return;
        }

        synchronized (lock) {
            this.calculate.addLast(operation);
        }
    }

    @Override
    public void run() {
        // drain queues in reverse order so that operations are spread across ticks
        Operation apply = this.apply.pollFirst();
        apply(apply, preLimit);

        Operation test = this.test.pollFirst();
        test(test, preLimit);

        Operation calculate;
        synchronized (lock) {
            calculate = this.calculate.pollFirst();
        }

        compute(calculate, changeLimit);
    }

    public void finish() {
        if (finishing) {
            return;
        }

        synchronized (lock) {
            finishing = true;

            while (!calculate.isEmpty()) {
                Operation operation = calculate.pollFirst();
                compute(operation, Integer.MAX_VALUE);
            }

            while (!test.isEmpty()) {
                Operation operation = test.pollFirst();
                test(operation, Integer.MAX_VALUE);
            }

            while (!apply.isEmpty()) {
                Operation operation = apply.pollFirst();
                apply(operation, Integer.MAX_VALUE);
            }
        }
    }

    private void compute(Operation operation, int limit) {
        if (operation != null) {
            try {
                Operation.Phase result = operation.calculate(limit);
                queue(operation, result);
            } catch (Throwable t) {
                t.printStackTrace();
                operation.dispose(Operation.Phase.ERROR);
            }
        }
    }

    private void test(Operation operation, int limit) {
        if (operation != null) {
            try {
                Operation.Phase result = operation.test(limit);
                queue(operation, result);
            } catch (Throwable t) {
                t.printStackTrace();
                operation.dispose(Operation.Phase.ERROR);
            }
        }
    }

    private void apply(Operation operation, int limit) {
        if (operation != null) {
            try {
                Operation.Phase result = operation.apply(limit);
                queue(operation, result);
            } catch (Throwable t) {
                t.printStackTrace();
                operation.dispose(Operation.Phase.ERROR);
            }
        }
    }

    private void queue(Operation operation, Operation.Phase phase) {
        switch (phase) {
            case CALCULATE:
                calculate.add(operation);
                return;
            case TEST:
                test.add(operation);
                return;
            case APPLY:
                apply.add(operation);
                return;
            default:
                operation.dispose(phase);
        }
    }
}
