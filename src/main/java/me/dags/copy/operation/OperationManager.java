package me.dags.copy.operation;

import java.util.LinkedList;

/**
 * @author dags <dags@dags.me>
 */
public class OperationManager implements Runnable {

    private final Object lock = new Object();
    private final LinkedList<Operation> calculate = new LinkedList<>();
    private final LinkedList<Operation> test = new LinkedList<>();
    private final LinkedList<Operation> apply = new LinkedList<>();

    private boolean finishing = false;
    private int operationsPerTick = 3;

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
        for (int i = operationsPerTick; i > 0; i--) {
            Operation calculate;
            synchronized (lock) {
                calculate = this.calculate.pollFirst();
            }
            compute(calculate);

            Operation test = this.test.pollFirst();
            test(test);

            Operation apply = this.apply.pollFirst();
            apply(apply);
        }
    }

    public void finish() {
        if (finishing) {
            return;
        }

        synchronized (lock) {
            finishing = true;

            while (!calculate.isEmpty()) {
                Operation operation = calculate.pollFirst();
                compute(operation);
            }

            while (!test.isEmpty()) {
                Operation operation = test.pollFirst();
                test(operation);
            }

            while (!apply.isEmpty()) {
                Operation operation = apply.pollFirst();
                apply(operation);
            }
        }
    }

    private void compute(Operation operation) {
        if (operation != null) {
            if (operation.isCancelled()) {
                operation.dispose();
            } else {
                operation.calculate();
                test.addLast(operation);
            }
        }
    }

    private void test(Operation operation) {
        if (operation != null) {
            if (operation.isCancelled()) {
                operation.dispose();
            } else {
                operation.test();
                apply.addLast(operation);
            }
        }
    }

    private void apply(Operation operation) {
        if (operation != null) {
            if (!operation.isCancelled()) {
                operation.apply();
            }
            operation.dispose();
        }
    }
}
