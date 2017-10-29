package me.dags.copy.operation.calculator;

import me.dags.copy.operation.Operation;
import me.dags.copy.operation.visitor.Visitor2D;
import me.dags.copy.operation.visitor.Visitor3D;

/**
 * @author dags <dags@dags.me>
 */
public interface Calculator {

    void reset();

    default Operation.Phase iterate(int limit, Visitor2D visitor) {
        return Operation.Phase.TEST;
    }

    default Operation.Phase iterate(int limit, Visitor3D visitor) {
        return Operation.Phase.TEST;
    }
}
