package me.dags.copy.util;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;

/**
 * @author dags <dags@dags.me>
 */
public interface Serializable<T> {

    TypeToken<T> getToken();

    TypeSerializer<T> getSerializer();

    default void register(TypeSerializerCollection collection) {
        collection.registerType(getToken(), getSerializer());
    }
}
