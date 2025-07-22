package me.paperxiang.stormeye.utils;
import java.nio.ByteBuffer;
import java.util.UUID;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
public final class PersistentDataTypes {
    public static final PersistentDataType<byte[], UUID> UUID = new UUIDPersistentDataType();
    private PersistentDataTypes() {}
    private static final class UUIDPersistentDataType implements PersistentDataType<byte[], UUID> {
        @Override
        public @NotNull Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }
        @Override
        public @NotNull Class<UUID> getComplexType() {
            return UUID.class;
        }
        @Override
        public byte @NotNull [] toPrimitive(UUID complex, @NotNull PersistentDataAdapterContext context) {
            final ByteBuffer bytes = ByteBuffer.allocate(Long.BYTES * 2);
            bytes.putLong(complex.getMostSignificantBits());
            bytes.putLong(complex.getLeastSignificantBits());
            return bytes.array();
        }
        @Override
        public @NotNull UUID fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
            final ByteBuffer bytes = ByteBuffer.wrap(primitive);
            return new UUID(bytes.getLong(), bytes.getLong());
        }
    }
}
