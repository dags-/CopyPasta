package me.dags.copy.operation;

import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerManager;
import me.dags.copy.operation.phase.Apply;
import me.dags.copy.operation.phase.Calculate;
import me.dags.copy.operation.phase.Test;
import me.dags.copy.util.fmt;
import org.spongepowered.api.Sponge;

import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class PlaceOperation implements Operation {

    private final Calculate calculate;
    private final Test test;
    private final Apply apply;
    private final UUID owner;

    public PlaceOperation(UUID owner, Calculate calculate, Test test, Apply apply) {
        this.calculate = calculate;
        this.test = test;
        this.apply = apply;
        this.owner = owner;
    }

    @Override
    public Phase calculate(int limit) {
        return calculate.calculate(limit);
    }

    @Override
    public Phase test(int limit) {
        return test.test(limit);
    }

    @Override
    public Phase apply(int limit) {
        return apply.apply(limit);
    }

    @Override
    public void dispose(Phase phase) {
        Sponge.getServer().getPlayer(owner).ifPresent(player -> {
            if (phase == Operation.Phase.ERROR) {
                fmt.error("Error occurred during operation").tell(CopyPasta.NOTICE_TYPE, player);
            }
            if (phase == Operation.Phase.CANCELLED) {
                fmt.error("Operation cancelled by plugin").tell(CopyPasta.NOTICE_TYPE, player);
            }
            if (phase == Operation.Phase.DISPOSE) {
                fmt.stress("Operation complete").tell(CopyPasta.NOTICE_TYPE, player);
            }
        });

        PlayerManager.getInstance().get(owner).ifPresent(data -> data.setOperating(false));
    }
}
