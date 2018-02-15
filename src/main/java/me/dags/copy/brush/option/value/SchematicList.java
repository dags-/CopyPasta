package me.dags.copy.brush.option.value;

import me.dags.config.Node;
import me.dags.copy.brush.option.Option;
import me.dags.copy.registry.schematic.SchematicEntry;
import me.dags.copy.util.WeightedList;

/**
 * @author dags <dags@dags.me>
 */
public class SchematicList extends WeightedList<SchematicEntry> implements Node.Value<SchematicList> {

    public static final Option<SchematicList> OPTION = Option.of("schematics", SchematicList.class, SchematicList::new);

    @Override
    public SchematicList fromNode(Node node) {
        SchematicList list = new SchematicList();
        node.iterate((k, n) -> {
            String path = k.toString();
            String repo = n.get("repo", "");
            double weight = n.get("weight", 1D);
            SchematicEntry.of(repo, path).ifPresent(e -> list.add(e, weight));
        });
        return list;
    }

    @Override
    public void toNode(Node node) {
        iterate((s, w) -> node.node(s.getPath()).set("weight", w).set("repo", s.getRepo()));
    }
}
