package sponge.configuration.TypeSerializer;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.math.vector.Vector3i;

import java.lang.reflect.Type;
import java.util.Arrays;

final class Vector3iSerializer implements TypeSerializer<Vector3i> {
    static final Vector3iSerializer INSTANCE = new Vector3iSerializer();

    private static final Vector3i vector = new Vector3i(0, 0, 0);

    private ConfigurationNode nonVirtualNode(final ConfigurationNode source, final Object... path) throws SerializationException {
        if (!source.hasChild(path)) {
            throw new SerializationException("Required field " + Arrays.toString(path) + " was not present in node");
        }
        return source.node(path);
    }

    @Override
    public Vector3i deserialize(final Type type, final ConfigurationNode source) throws SerializationException {
        final ConfigurationNode x = nonVirtualNode(source, "x");
        final ConfigurationNode y = nonVirtualNode(source, "y");
        final ConfigurationNode z = nonVirtualNode(source, "z");

        return Vector3i.from(x.getInt(), y.getInt(), z.getInt());
    }

    @Override
    public void serialize(final Type type, final Vector3i vec, final ConfigurationNode target) throws SerializationException {
        if (vec == null) {
            target.raw(null);
            return;
        }

        target.node("x").set(vec.x());
        target.node("y").set(vec.y());
        target.node("z").set(vec.z());
    }
}
