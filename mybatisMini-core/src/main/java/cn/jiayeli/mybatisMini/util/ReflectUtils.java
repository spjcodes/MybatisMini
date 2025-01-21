package cn.jiayeli.mybatisMini.util;

import com.mysql.cj.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ReflectUtils {

    public static void _main(String[] args) throws ClassNotFoundException {
        // 给出的方法签名
        String implMethodSignature = "(Lcn/jiayeli/dao/ConfigMapper;)Lcn/jiayeli/model/ConfigModel;";

        // 解析并提取信息
        List<String> parameterClasses = getParameterTypes(implMethodSignature);
        Class<?> returnType = getReturnType(implMethodSignature);

        // 打印结果
        System.out.println("参数类型:");
        for (String paramClass : parameterClasses) {
            System.out.println(paramClass);
        }
        System.out.println("返回值类型:");
        System.out.println(returnType.getName());
    }

    /**
     * 获取方法签名中的参数类型对应的 Class 对象
     */
    public static List<String> getParameterTypes(String methodSignature) throws ClassNotFoundException {
        List<String> parameterClasses = new ArrayList<>();

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
//                    parameterClasses.add(Class.forName(className));
                    parameterClasses.add(className);
                }
            }
        }

        return parameterClasses;
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
        List<String> parameterTypes = ReflectUtils.getParameterTypes(implMethodSignature);
        log.debug("parameterTypes: {}", parameterTypes);
        return !parameterTypes.isEmpty() ? parameterTypes.get(0) : "";
    }
}
