package deepclone.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class DeepCopyUtils {

    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T original) {
        try {
            return (T) deepCopyInternal(original, new IdentityHashMap<>());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to deep copy object", e);
        }
    }

    private static Object deepCopyInternal(Object original, Map<Object, Object> visited)
            throws IllegalAccessException, InstantiationException {

        if (original == null) {
            return null;
        }

        if (original instanceof String || original instanceof Number || original instanceof Boolean || original instanceof Character) {
            return original;
        }

        if (visited.containsKey(original)) {
            return visited.get(original);
        }

        Class<?> clazz = original.getClass();

        if (clazz.isArray()) {
            int length = Array.getLength(original);
            Object copy = Array.newInstance(clazz.getComponentType(), length);
            visited.put(original, copy);
            for (int i = 0; i < length; i++) {
                Array.set(copy, i, deepCopyInternal(Array.get(original, i), visited));
            }
            return copy;
        }

        if (original instanceof Collection<?> originalCollection) {
            Collection<Object> copyCollection = originalCollection instanceof List ? new ArrayList<>() : new HashSet<>();
            visited.put(original, copyCollection);
            for (Object item : originalCollection) {
                copyCollection.add(deepCopyInternal(item, visited));
            }
            return copyCollection;
        }

        if (original instanceof Map<?, ?> originalMap) {
            Map<Object, Object> copyMap = new HashMap<>();
            visited.put(original, copyMap);
            for (Map.Entry<?, ?> entry : originalMap.entrySet()) {
                Object keyCopy = deepCopyInternal(entry.getKey(), visited);
                Object valueCopy = deepCopyInternal(entry.getValue(), visited);
                copyMap.put(keyCopy, valueCopy);
            }
            return copyMap;
        }

        Object copy = instantiateClass(clazz);
        visited.put(original, copy);

        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object fieldValue = field.get(original);
                Object fieldValueCopy = deepCopyInternal(fieldValue, visited);
                field.set(copy, fieldValueCopy);
            }
            clazz = clazz.getSuperclass();
        }

        return copy;
    }

    private static Object instantiateClass(Class<?> clazz) throws InstantiationException {
        try {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Constructor<?> constructor = null;
            for (Constructor<?> cons : constructors) {
                if (cons.getParameterCount() == 0) {
                    constructor = cons;
                    break;
                }
            }
            if (constructor == null) {
                sun.misc.Unsafe unsafe = getUnsafeInstance();
                return unsafe.allocateInstance(clazz);
            } else {
                constructor.setAccessible(true);
                return constructor.newInstance();
            }
        } catch (Exception e) {
            throw new InstantiationException("No suitable constructor found for " + clazz.getName());
        }
    }

    private static sun.misc.Unsafe getUnsafeInstance() throws Exception {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (sun.misc.Unsafe) f.get(null);
    }
}