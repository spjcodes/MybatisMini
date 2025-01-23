package cn.jiayeli;

import cn.jiayeli.mybatisMini.core.MiniMapper;
import cn.jiayeli.mybatisMini.core.MybatisMini;
import cn.jiayeli.mybatisMini.example.dao.ConfigMapper;
import cn.jiayeli.mybatisMini.example.model.ConfigModel;
import com.mysql.cj.util.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static cn.jiayeli.mybatisMini.core.ReflectUtils.getGenericTypeInformation;

public class MybatisMiniTestCase {

    @Test
    public void getGenericTypeInformationTestCase() throws ClassNotFoundException {
        Map<String, List<String>> types = getGenericTypeInformation(Class.forName("cn.jiayeli.mybatisMini.example.dao.ConfigMapper"));
        System.out.println(types);
    }
    @Test
    public void test() throws ClassNotFoundException {
            // 获取 ConfigMapper 的泛型父接口
//            Type[] genericInterfaces = ConfigMapper.class.getGenericInterfaces();
            Type[] genericInterfaces = Class.forName("cn.jiayeli.mybatisMini.example.dao.ConfigMapper").getGenericInterfaces();

            for (Type genericInterface : genericInterfaces) {
                // 我们只关心它是 ParameterizedType 类型的，因为它带有泛型信息
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericInterface;

                    // 获取此接口的原始类型
                    Type rawType = parameterizedType.getRawType();
                    System.out.println("继承的接口原始类型：" + rawType); // MiniMapper.class

                    // 获取此接口的泛型参数
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    System.out.println("接口的泛型参数：");
                    for (Type typeArg : typeArguments) {
                        System.out.println("  " + typeArg.getTypeName());
                    }
                }
            }
    }


    @Test
    public void appTestCase() {

        List<ConfigModel> configModelList = MybatisMini.queryList(ConfigMapper::queryList);
        System.out.println(Arrays.toString(configModelList.toArray()));
        assert !configModelList.isEmpty();

        String usernameKey = "username";
        try (SqlSession sqlSession = MybatisMini.getSqlSession()) {
            ConfigMapper mapper = sqlSession.getMapper(ConfigMapper.class);
            assert mapper != null;
            ConfigModel username = mapper.queryConfigByKey(usernameKey);
            assert username != null;
            System.out.println(username);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ConfigModel username =
                MybatisMini.<ConfigMapper, ConfigModel>queryObject(
                        configMapper -> configMapper.queryConfigByKey(usernameKey)
                );
        System.out.println(username);
        assert username != null;

    }

    @Test
    public void withArgsTestCase() throws ClassNotFoundException {
        String userName  = "username";
        ConfigModel username = MybatisMini.<ConfigMapper, ConfigModel>queryObject(configMapper -> configMapper.queryConfigByKey(userName));
        System.out.println(username);
    }

    @Test
    public void appTestCase2() {
        ff f = new ff();
        Object o = execFunc(f, "1");
        Object o1 = execFunc(args -> {
            System.out.println("args: " + args);
            return args;
        }, "1", 1, 2, 3);
        System.out.println(o1);
    }

    public Object execFunc(Function<Object, Object> f, String p1, Object... args) {
        System.out.println(p1);
        return f.apply(args);
    }


    @Test
    public void classTypeMatchTestCase() throws ClassNotFoundException {
        String clazzName  = "cn.jiayeli.mybatisMini.example.dao.ConfigMapper";
        String annotatedInterfaces = getMybatisMiniMapperClass(clazzName);
        System.out.println(annotatedInterfaces);
    }

    private String getMybatisMiniMapperClass(String clazzName) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(clazzName);
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
                return clazzName;
            }
        }
        return null;
    }


}

class ff implements Function<Object, Object> {

    @Override
    public Object apply(Object o) {
        return o;
    }

    @Override
    public <V> Function<V, Object> compose(Function<? super V, ?> before) {
        return Function.super.compose(before);
    }

    @Override
    public <V> Function<Object, V> andThen(Function<? super Object, ? extends V> after) {
        return Function.super.andThen(after);
    }
}