package cn.jiayeli.mybatisMini.core;

import com.mysql.cj.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for reflecting and analyzing classes, methods, and interfaces.
 * Provides type analysis like extracting generic type parameters, finding implemented interfaces, etc.
 *
 * @author kuro@jiayeli.com
 */
@Slf4j
public class ReflectUtils {

    private ReflectUtils() {
        // Utility class should not be instantiated
    }

    /**
     * Extracts the parameter types of a method from its signature and finds the first parameter
     * that matches a MiniMapper implementation.
     *
     * @param methodSignature Method signature string in JNI format (e.g., `(Lpath/to/Class;)LReturnType;`).
     * @return The canonical name of the parameter class that corresponds to MiniMapper, or `null` if not found.
     * @throws ClassNotFoundException if a parameter type cannot be resolved to a class.
     */
    public static String findMiniMapperParameterType(String methodSignature) throws ClassNotFoundException {
        if (StringUtils.isNullOrEmpty(methodSignature)) {
            log.error("Method signature is empty or null.");
            throw new IllegalArgumentException("Method signature cannot be null or empty");
        }

        // Extract parameter part (e.g., "(Lcn/jiayeli/dao/ConfigMapper;)")
        int start = methodSignature.indexOf('(');
        int end = methodSignature.indexOf(')');
        if (start < 0 || end < 0 || start >= end) {
            log.error("Invalid method signature format: {}", methodSignature);
            throw new IllegalArgumentException("Invalid method signature format");
        }

        String parametersPart = methodSignature.substring(start + 1, end);
        log.debug("Parameters part extracted from signature: {}", parametersPart);

        // Process each parameter type
        String[] parameterTypes = parametersPart.split(";");
        for (String paramType : parameterTypes) {
            if (paramType.startsWith("L")) {
                String className = paramType.substring(1).replace('/', '.');
                Class<?> clazz = Class.forName(className);

                if (implementsInterface(clazz, MiniMapper.class)) {
                    log.debug("Parameter implements MiniMapper: {}", className);
                    return className;
                }
            }
        }
        log.warn("No parameter implements MiniMapper in method signature: {}", methodSignature);
        return null;
    }

    /**
     * Extracts the return type of a method from its signature.
     *
     * @param methodSignature Method signature string in JNI format.
     * @return The return type as a `Class<?>` object.
     * @throws ClassNotFoundException if the return type cannot be resolved to a class.
     */
    public static Class<?> extractReturnType(String methodSignature) throws ClassNotFoundException {
        if (StringUtils.isNullOrEmpty(methodSignature)) {
            log.error("Method signature is empty or null.");
            throw new IllegalArgumentException("Method signature cannot be null or empty");
        }

        int end = methodSignature.indexOf(')');
        if (end < 0 || end >= methodSignature.length() - 1) {
            log.error("Invalid method signature format: {}", methodSignature);
            throw new IllegalArgumentException("Invalid method signature format");
        }

        String returnTypePart = methodSignature.substring(end + 1);
        if (returnTypePart.startsWith("L")) {
            String className = returnTypePart.substring(1, returnTypePart.length() - 1).replace('/', '.');
            return Class.forName(className);
        } else if ("V".equals(returnTypePart)) {
            return void.class;
        }

        log.error("Unsupported return type in method signature: {}", methodSignature);
        throw new IllegalArgumentException("Unsupported return type: " + returnTypePart);
    }

    /**
     * Finds the MyBatis Mini mapper class from a SerializedLambda.
     *
     * @param serializedLambda Serialized lambda instance containing metadata about the lambda.
     * @return The fully qualified class name of the mapper class.
     * @throws ClassNotFoundException if the mapper class cannot be resolved.
     */
    public static String getMybatisMiniMapperClass(SerializedLambda serializedLambda) throws ClassNotFoundException {
        Objects.requireNonNull(serializedLambda, "SerializedLambda cannot be null");

        String implClassName = serializedLambda.getImplClass().replace("/", ".");
        Class<?> clazz = Class.forName(implClassName);

        // Check implemented interfaces for MiniMapper
        for (AnnotatedType annotatedType : clazz.getAnnotatedInterfaces()) {
            if (implementsInterface(annotatedType.getType(), MiniMapper.class)) {
                log.debug("Found MyBatis MiniMapper in class: {}", implClassName);
                return implClassName;
            }
        }

        // Fallback: Infer from method signature
        String implMethodSignature = serializedLambda.getImplMethodSignature();
        String mapperClassName = findMiniMapperParameterType(implMethodSignature);
        if (mapperClassName == null) {
            log.error("Unable to identify MiniMapper implementation for SerializedLambda: {}", serializedLambda);
            throw new IllegalStateException("Cannot find MiniMapper class");
        }
        log.debug("Mapper class determined from method signature: {}", mapperClassName);
        return mapperClassName;
    }

    /**
     * Retrieves generic type information implemented by a class.
     *
     * @param clazz The class to inspect.
     * @return A map containing raw types and type argument names.
     */
    public static Map<String, List<String>> getGenericTypeInformation(Class<?> clazz) {
        Objects.requireNonNull(clazz, "Class cannot be null");
        Map<String, List<String>> typeInfo = new HashMap<>();

        List<String> rawTypes = new ArrayList<>();
        List<String> typeArguments = new ArrayList<>();

        for (Type genericInterface : clazz.getGenericInterfaces()) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;

                rawTypes.add(parameterizedType.getRawType().getTypeName());
                typeArguments.addAll(Arrays.stream(parameterizedType.getActualTypeArguments())
                        .map(Type::getTypeName)
                        .collect(Collectors.toList()));
            } else {
                rawTypes.add(genericInterface.getTypeName());
            }
        }

        typeInfo.put("rawTypes", rawTypes);
        typeInfo.put("typeArguments", typeArguments);
        log.debug("Generic type information for {}: {}", clazz.getName(), typeInfo);
        return typeInfo;
    }

    /**
     * Checks if a class or interface type implements a specified interface.
     *
     * @param type      The type being checked.
     * @param interfaceClass The interface to check against.
     * @return True if the type implements the interface, false otherwise.
     */
    private static boolean implementsInterface(Type type, Class<?> interfaceClass) {
        if (type instanceof Class) {
            return interfaceClass.isAssignableFrom((Class<?>) type);
        } else if (type instanceof ParameterizedType) {
            return interfaceClass.isAssignableFrom((Class<?>) ((ParameterizedType) type).getRawType());
        }
        return false;
    }
}