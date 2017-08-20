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
        apply(apply);

        Operation test = this.test.pollFirst();
        test(test);

        Operation calculate;
        synchronized (lock) {
            calculate = this.calculate.pollFirst();
        }
        compute(calculate);
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
                return;
            }

            try {
                operation.calculate();
                test.addLast(operation);
            } catch (Throwable t) {
                t.printStackTrace();
                operation.dispose();
            }
        }
    }

    private void test(Operation operation) {
        if (operation != null) {
            if (operation.isCancelled()) {
                operation.dispose();
                return;
            }

            try {
                operation.test();
                apply.addLast(operation);
            } catch (Throwable t) {
                t.printStackTrace();
                operation.dispose();
            }
        }
    }

    private void apply(Operation operation) {
        if (operation != null) {
            if (operation.isCancelled()) {
                operation.dispose();
                return;
            }

            try {
                operation.apply();
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                operation.dispose();
            }
        }
    }
}
