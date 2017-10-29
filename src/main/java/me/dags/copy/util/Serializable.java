package me.dags.copy.util;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

/**
 * @author dags <dags@dags.me>
 */
public interface Serializable<T> {

    TypeToken<T> getToken();

    TypeSerializer<T> getSerializer();
}
