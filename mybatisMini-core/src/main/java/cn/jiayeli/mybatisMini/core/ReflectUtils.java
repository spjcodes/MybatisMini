package cn.jiayeli.mybatisMini.core;

import com.mysql.cj.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import cn.jiayeli.mybatisMini.core.MiniMapper;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class ReflectUtils {

    public static void _main(String[] args) throws ClassNotFoundException {
        // 给出的方法签名
        printGenericTypeInfo(Class.forName("cn.jiayeli.mybatisMini.core.dao.ConfigMapper"));
     /*   // 解析并提取信息
        List<String> parameterClasses = getParameterTypes(implMethodSignature);
        Class<?> returnType = getReturnType(implMethodSignature);

        // 打印结果
        System.out.println("参数类型:");
        for (String paramClass : parameterClasses) {
            System.out.println(paramClass);
        }
        System.out.println("返回值类型:");
        System.out.println(returnType.getName());*/
    }

    /**
     * 获取方法签名中的参数类型对应的 Class 对象
     */
    public static String  getParameterTypes(String methodSignature) throws ClassNotFoundException {

        // 提取参数部分 (形如 (Lcn/jiayeli/dao/ConfigMapper;) )
        int start = methodSignature.indexOf('(');
        int end = methodSignature.indexOf(')');
        String parametersPart = methodSignature.substring(start + 1, end);

        // 每个参数的类型以 `L<package/class>;` 格式表示，按此拆分
        if (!parametersPart.isEmpty()) {
            String[] parameterTypes = parametersPart.split(";");
            for (String paramType : parameterTypes) {
                if (paramType.startsWith("L")) {
                    // 转换为 Java 的类名格式（去除前缀 `L` 和结尾 `;`）
                    String className = paramType.substring(1).replace('/', '.');
                    Class<?> clazz = Class.forName(className);
                    if (printGenericTypeInfo(clazz).get("rowTypes").contains(MiniMapper.class.getCanonicalName())) {
                        return className;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 获取方法签名中的返回值类型对应的 Class 对象
     */
    public static Class<?> getReturnType(String methodSignature) throws ClassNotFoundException {
        // 提取返回值部分 (形如 Lcn/jiayeli/model/ConfigModel;)
        int end = methodSignature.indexOf(')');
        String returnTypePart = methodSignature.substring(end + 1);

        if (returnTypePart.startsWith("L")) {
            // 转换为 Java 的��名格式（去除前缀 `L` 和结尾 `;`）
            String className = returnTypePart.substring(1, returnTypePart.length() - 1).replace('/', '.');
            return Class.forName(className);
        } else if ("V".equals(returnTypePart)) {
            // 返回值为 void
            return void.class;
        }

        throw new IllegalArgumentException("Unsupported return type: " + returnTypePart);
    }

    public static String getMybatisMiniMapperClass(SerializedLambda serializedLambda) throws ClassNotFoundException {
        // 获取方法引用对应的类名
        String className = serializedLambda.getImplClass().replace("/", ".");
        Class<?> clazz = Class.forName(className);
        AnnotatedType[] annotatedInterfaces = clazz.getAnnotatedInterfaces();
        for (AnnotatedType annotatedInterface : annotatedInterfaces) {
            String atype = annotatedInterface.getType().getTypeName();
            String annotateName = "";
            if (!StringUtils.isNullOrEmpty(atype)) {
                annotateName = atype.replaceAll("<[^>]+>", "");
            }
            Class<MiniMapper> miniMapperClass = MiniMapper.class;
            String typeName = miniMapperClass.getTypeName();
            if (typeName.equals(annotateName)) {
                return className;
            }
        }
        String implMethodSignature = serializedLambda.getImplMethodSignature();
        log.debug("implMethodSignature: {}", implMethodSignature);
        String mapperClassName  = ReflectUtils.getParameterTypes(implMethodSignature);
        log.debug("mapperClassName: {}", mapperClassName);
        return mapperClassName;
    }

    /**
     * 获取指定类实现的泛型接口类型信息，包括原始类型和泛型参数信息。
     *
     * @param clazz 要解析的类或接口
     * @return
     */
    public static HashMap<String, List<String>> printGenericTypeInfo(Class<?> clazz) {
        List<String> rowTypes = new ArrayList<>();
        List<String> typeArgsTypes = new ArrayList<>();
        // 获取类实现的所有直接接口
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        if (genericInterfaces.length == 0) {
            System.out.println(clazz.getName() + " 没有实现任何接口。");
            log.debug("{} 没有实现任何接口。", clazz.getName());
            return null;
        }

        System.out.println(clazz.getName() + " 实现的接口和泛型信息：");
       log.debug("{} 实现的接口和泛型信息：", clazz.getName());

        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                // 获取原始类型
                Type rawType = parameterizedType.getRawType();
                log.debug("原始类型：{}", rawType);
                rowTypes.add(rawType.getTypeName());
                // 获取泛型参数
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                for (int i = 0; i < typeArguments.length; i++) {
                    log.debug("泛型参数[{}]：{}%n", i, typeArguments[i].getTypeName());
                    typeArgsTypes.add(typeArguments[i].getTypeName());
                }
            } else {
                // 如果不是 ParameterizedType，则只输出原始类型
                log.debug("原始类型（无泛型): {}", genericInterface);
                rowTypes.add(genericInterface.getTypeName());
            }
        }
        return new HashMap<String, List<String>>() {{
            put("rowTypes", rowTypes);
            put("argsTypes", typeArgsTypes);
        }};
    }

}
