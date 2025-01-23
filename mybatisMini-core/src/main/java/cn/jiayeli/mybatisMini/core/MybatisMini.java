package cn.jiayeli.mybatisMini.core;

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
import java.util.Objects;

/**
 * MybatisMini encapsulates MyBatis SqlSession and adds utility methods for query operations.
 */
@Slf4j
public class MybatisMini {

    private static final String MYBATIS_CONFIG_PATH = "mybatis/mybatisConfig.xml";
    private static SqlSessionFactory sqlSessionFactory;

    static {
        initializeSqlSessionFactory();
    }

    private static void initializeSqlSessionFactory() {
        try (InputStream inputStream = Resources.getResourceAsStream(MYBATIS_CONFIG_PATH)) {
            if (inputStream == null) {
                throw new MybatisMiniException("Failed to load mybatis config file: " + MYBATIS_CONFIG_PATH);
            }
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new MybatisMiniException("Error initializing MyBatis SqlSessionFactory", e);
        }
    }

    /**
     * Returns a new MyBatis SqlSession instance (non-auto commit mode).
     *
     * @return SqlSession object
     */
    public static SqlSession getSqlSession() {
        if (sqlSessionFactory == null) {
            throw new MybatisMiniException("SqlSessionFactory is not initialized");
        }
        return sqlSessionFactory.openSession();
    }

    /**
     * Executes a query method that returns a list.
     *
     * @param function a lambda function representing the mapper method reference
     * @param <T>      the target list element type
     * @param <M>      the mapper type
     * @return the query result as a list
     */
    public static <M, T> List<T> queryList(MiniMapper<M, List<T>> function) {
        Objects.requireNonNull(function, "Mapper function cannot be null");

        try (SqlSession sqlSession = getSqlSession()) {
            Class<M> mapperClass = resolveMapperClass(function);
            M mapper = sqlSession.getMapper(mapperClass);
            return function.apply(mapper);
        } catch (Exception e) {
            log.error("Error while querying list from MyBatis mapper", e);
            throw new MybatisMiniException("Failed to query list", e);
        }
    }

    /**
     * Executes a query method that returns a single object.
     *
     * @param function a lambda function representing the mapper method reference
     * @param <T>      the target object type
     * @param <M>      the mapper type
     * @return the query result as an object
     */
    public static <M, T> T queryObject(MiniMapper<M, T> function) {
        Objects.requireNonNull(function, "Mapper function cannot be null");

        try (SqlSession sqlSession = getSqlSession()) {
            Class<M> mapperClass = resolveMapperClass(function);
            M mapper = sqlSession.getMapper(mapperClass);
            return function.apply(mapper);
        } catch (Exception e) {
            log.error("Error while querying object from MyBatis mapper", e);
            throw new MybatisMiniException("Failed to query object", e);
        }
    }

    /**
     * Resolves the mapper class from a MiniMapper lambda function.
     *
     * @param lambda the mapper method reference as a lambda
     * @param <T>    the mapper type
     * @return the class of the mapper
     * @throws MybatisMiniException if the lambda does not represent a valid mapper method
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> resolveMapperClass(MiniMapper<T, ?> lambda) {
        try {
            Method writeReplace = lambda.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) writeReplace.invoke(lambda);

            String className = ReflectUtils.getMybatisMiniMapperClass(serializedLambda);
            return (Class<T>) Class.forName(className);
        } catch (Exception e) {
            log.error("Failed to resolve mapper class for lambda: {}", lambda, e);
            throw new MybatisMiniException("Error resolving Mapper class from lambda", e);
        }
    }

    /**
     * Custom exception for MyBatis Mini errors.
     */
    public static class MybatisMiniException extends RuntimeException {
        public MybatisMiniException(String message) {
            super(message);
        }

        public MybatisMiniException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}