package me.dags.copy.operation;

import me.dags.copy.CopyPasta;
import me.dags.copy.PlayerManager;
import me.dags.copy.operation.applier.Applier;
import me.dags.copy.operation.calculator.Calculator;
import me.dags.copy.operation.tester.Tester;
import me.dags.copy.operation.visitor.Visitor2D;
import me.dags.copy.operation.visitor.Visitor3D;
import me.dags.copy.util.fmt;
import org.spongepowered.api.Sponge;

import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class PlaceOperation implements Operation {

    private final UUID owner;
    private final Calculator calculator;
    private final Tester tester;
    private final Applier applier;
    private final Visitor2D visitor2D;
    private final Visitor3D visitor3D;

    private PlaceOperation(UUID owner, Calculator calculator, Tester tester, Applier applier, Visitor2D visitor2D, Visitor3D visitor3D) {
        this.owner = owner;
        this.calculator = calculator;
        this.tester = tester;
        this.applier = applier;
        this.visitor2D = visitor2D;
        this.visitor3D = visitor3D;
    }

    public PlaceOperation(UUID owner, Calculator calculator, Tester tester, Applier applier, Visitor2D visitor2D) {
        this(owner, calculator, tester, applier, visitor2D, null);
    }

    public PlaceOperation(UUID owner, Calculator calculator, Tester tester, Applier applier, Visitor3D visitor3D) {
        this(owner, calculator, tester, applier, null, visitor3D);
    }

    @Override
    public Phase calculate(int limit) {
        if (visitor2D != null) {
            return calculator.iterate(limit, visitor2D);
        }
        if (visitor3D != null) {
            return calculator.iterate(limit, visitor3D);
        }
        return Phase.ERROR;
    }

    @Override
    public Phase test(int limit) {
        return tester.test(limit);
    }

    @Override
    public Phase apply(int limit) {
        return applier.apply(limit);
    }

    @Override
    public void dispose(Phase phase) {
        Sponge.getServer().getPlayer(owner).ifPresent(player -> {
            if (phase == Phase.ERROR) {
                fmt.error("Error occurred during operation").tell(CopyPasta.NOTICE_TYPE, player);
            }
            if (phase == Phase.CANCELLED) {
                fmt.error("Operation cancelled by plugin").tell(CopyPasta.NOTICE_TYPE, player);
            }
            if (phase == Phase.DISPOSE) {
                fmt.stress("Operation complete").tell(CopyPasta.NOTICE_TYPE, player);
            }
        });

        PlayerManager.getInstance().get(owner).ifPresent(data -> data.setOperating(false));
    }
}
