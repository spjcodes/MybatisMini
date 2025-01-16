package cn.jiayeli.mybatisMini.util.dao;

import cn.jiayeli.mybatisMini.util.model.ConfigModel;
import cn.jiayeli.mybatisMini.util.MiniMapper;

import java.util.List;


public interface ConfigMapper extends MiniMapper<ConfigMapper, ConfigModel> {

//    @Select("select * from config")
    public List<ConfigModel> queryList();

    public ConfigModel queryConfigByKey(String config_key);
}
