package cn.jiayeli;

import cn.jiayeli.mybatisMini.util.MiniMapper;
import cn.jiayeli.mybatisMini.util.MybatisMini;
import cn.jiayeli.mybatisMini.util.dao.ConfigMapper;
import cn.jiayeli.mybatisMini.util.model.ConfigModel;
import com.mysql.cj.util.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.lang.reflect.AnnotatedType;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class MybatisMiniTestCase {
    @Test
    public void appTestCase() {

        List<ConfigModel> configModelList = MybatisMini.queryList(ConfigMapper::queryList);
        System.out.println(Arrays.toString(configModelList.toArray()));
        assert !configModelList.isEmpty();

        try (SqlSession sqlSession = MybatisMini.getSqlSession()) {
            ConfigMapper mapper = sqlSession.getMapper(ConfigMapper.class);
            assert mapper != null;
            ConfigModel username = mapper.queryConfigByKey("username");
            assert username != null;
            System.out.println(username);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ConfigModel username = MybatisMini.<ConfigMapper, ConfigModel>queryObject(configMapper -> configMapper.queryConfigByKey("username"));
        System.out.println(username);
        assert username != null;

    }

    @Test
    public void withArgsTestCase() {
        ConfigModel username = MybatisMini.<ConfigMapper, ConfigModel>queryObject(configMapper -> configMapper.queryConfigByKey("username"));
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
    public void rft() throws ClassNotFoundException {
        String clazzName  = "cn.jiayeli.mybatisMini.util.dao.ConfigMapper";
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