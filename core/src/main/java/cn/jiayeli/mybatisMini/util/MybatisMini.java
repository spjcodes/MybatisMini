package cn.jiayeli.mybatisMini.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.List;



@Slf4j
public class MybatisMini {

    private static SqlSessionFactory sqlSessionFactory;

    static {
        try {
            // 读取 MyBatis 配置文件
            String resource = "mybatis/mybatisConfig.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("初始化 MyBatis 时发生错误！");
        }
    }

    /**
     * 获取 SqlSession
     *
     * @return SqlSession 对象
     */
    public static SqlSession getSqlSession() {
        return sqlSessionFactory.openSession();
    }

    /**
     * 查询多条记录 （传入函数式方法引用解析）
     *
     * @param function 方法引用， 形如 Mapper::方法名
     * @param <T>      返回类型
     * @param <M>      Mapper 类型
     * @return 查询结果
     */
    public static <M, T> List<T> queryList(MiniMapper<M, List<T>> function) {
        try (SqlSession sqlSession = getSqlSession()) {
            // 解析 Mapper 类型
            Class<M> mapperClass = getMyBatisMapperClass(function);
            // 获取 Mapper
            M mapper = sqlSession.getMapper(mapperClass);
            // 调用方法
            return function.apply(mapper);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询发生错误！");
        }
    }

    /**
     * 查询单条记录 （传入函数式方法引用解析）
     *
     * @param function 方法引用， 形如 Mapper::方法名
     * @param <T>      返回泛型类型
     * @param <M>      Mapper 类型
     * @return 查询结果
     */
    public static <M, T> T queryObject(MiniMapper<M, T> function, Object... args) {
        try (SqlSession sqlSession = getSqlSession()) {
            // 解析 Mapper 类型
            Class<M> mapperClass = getMyBatisMapperClass(function);
            // 获取 Mapper
            M mapper = sqlSession.getMapper(mapperClass);
            // 调用方法
            return function.apply(mapper);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询发生错误！");
        }
    }

    /**
     * 使用反射解析方法引用获取 Mapper 的 Class 信息
     *
     * @param lambda 方法引用 Lambda
     * @param <T>    Mapper 类型
     * @return Mapper 类
     * @throws Exception 解析异常
     */
    private static <T> Class<T> getMyBatisMapperClass(MiniMapper<T, ?> lambda) throws Exception {
        // 获取 SerializedLambda 实例
        Method[] declaredMethods = lambda.getClass().getDeclaredMethods();
        Method writeReplace = lambda.getClass().getDeclaredMethod("writeReplace");
        writeReplace.setAccessible(true);
        SerializedLambda serializedLambda = (SerializedLambda) writeReplace.invoke(lambda);

        String mybatisMiniMapperClass = ReflectUtils.getMybatisMiniMapperClass(serializedLambda);
        return (Class<T>) Class.forName(mybatisMiniMapperClass);
    }
}