package me.dags.copy.registry.option;

import ninja.leaping.configurate.ConfigurationNode;

/**
 * @author dags <dags@dags.me>
 */
public class Options {

    private final ConfigurationNode data;

    public Options(ConfigurationNode data) {
        this.data = data;
    }

    public ConfigurationNode getData() {
        return data;
    }
}
