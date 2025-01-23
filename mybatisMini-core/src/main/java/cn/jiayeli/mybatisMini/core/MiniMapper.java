package cn.jiayeli.mybatisMini.core;

import java.io.Serializable;

@FunctionalInterface
public interface MiniMapper<M, T> extends Serializable {
    T apply(M m) throws Exception;
}