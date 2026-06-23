package salon.common.infrastructure.persistence;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public final class DomainReflection {

    private DomainReflection() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T instantiate(Class<T> type, Class<?>[] paramTypes, Object[] args) {
        try {
            Constructor<?> ctor = type.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            return (T) ctor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot rehydrate " + type.getName(), e);
        }
    }

    public static void set(Object target, String fieldName, Object value) {
        try {
            Field field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot set field " + fieldName, e);
        }
    }

    public static Object get(Object target, String fieldName) {
        try {
            Field field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot read field " + fieldName, e);
        }
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
