package org.jeecg.modules.flowable.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.flowable.entity.ProcessDefinitionModel;

import java.util.List;

/**
 * @author 周余潇
 * @version 1.0
 * @date 2020/9/27 15:11
 * @explain
 **/
public interface ProdefMapper  extends BaseMapper {

    @Select("select a.id,a.name,model_key ` key`,ifnull(b.version_,0) version from act_de_model a LEFT JOIN (SELECT f.* from act_re_procdef f INNER  JOIN (SELECT KEY_,MAX(VERSION_)VERSION_ FROM act_re_procdef GROUP BY KEY_ )t on f.KEY_ = t.KEY_ AND f.VERSION_ = t.VERSION_ )b on a.model_key=b.key_")
    List<ProcessDefinitionModel> getProcessDeploymentList();
}
