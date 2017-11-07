package me.dags.copy.brush.option;

/**
 * @author dags <dags@dags.me>
 */
public interface OptionHolder {

    Options getOptions();

    default <T> T getOption(Option<T> option) {
        return getOptions().get(option);
    }

    default <T> T mustOption(Option<T> option) {
        return getOptions().must(option);
    }

    default void setOption(Option option, Object value) {
        getOptions().set(option, value);
    }
}
