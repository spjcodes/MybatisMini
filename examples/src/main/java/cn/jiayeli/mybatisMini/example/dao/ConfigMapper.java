package cn.jiayeli.mybatisMini.example.dao;

import cn.jiayeli.mybatisMini.example.model.ConfigModel;
import cn.jiayeli.mybatisMini.core.MiniMapper;

import java.util.List;


public interface ConfigMapper extends MiniMapper<ConfigMapper, ConfigModel> {

//    @Select("select * from config")
    public List<ConfigModel> queryList();

    public ConfigModel queryConfigByKey(String config_key);
}
