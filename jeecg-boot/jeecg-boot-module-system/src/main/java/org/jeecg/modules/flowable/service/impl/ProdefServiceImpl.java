package org.jeecg.modules.flowable.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.jeecg.modules.flowable.controller.FlowableController;
import org.jeecg.modules.flowable.entity.ProcessDefinitionModel;
import org.jeecg.modules.flowable.service.IProdefService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 周余潇
 * @version 1.0
 * @date 2020/8/11 9:57
 * @explain
 **/
@Service
public class ProdefServiceImpl implements IProdefService {

    private static final Logger logger = LoggerFactory.getLogger(ProdefServiceImpl.class);

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Override
    public List<ProcessDefinitionModel> getProcessDefinition(String id, String name) {
        String sql = "SELECT f.* from act_re_procdef f INNER  JOIN (\n" +
                "SELECT KEY_,MAX(VERSION_)VERSION_ FROM act_re_procdef GROUP BY KEY_ )t\n" +
                "on f.KEY_ = t.KEY_ AND f.VERSION_ = t.VERSION_  where 1=1";
        if (StringUtils.isNotEmpty(id))
            sql += " and f.id_ like '%" + id + "%'";
        if (StringUtils.isNotEmpty(name))
            sql += " and f.name_ like '%" + name + "%'";
        List<ProcessDefinition> processDefinitions = repositoryService.createNativeProcessDefinitionQuery().sql(sql).list();
        List<ProcessDefinitionModel> processDefinitionModelList = new ArrayList<>();
        processDefinitions.forEach(processDefinition -> {
            ProcessDefinitionModel processDefinitionModel = new ProcessDefinitionModel();
            BeanUtils.copyProperties(processDefinition, processDefinitionModel);
            processDefinitionModelList.add(processDefinitionModel);

        });
        return processDefinitionModelList;
    }

    @Override
    public void startProcessDefinition(List<ProcessDefinitionModel> processDefinitionModelList) {
        processDefinitionModelList.forEach(processDefinitionModel -> {
            //启动流程
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionModel.getKey());
            logger.info("提交成功.流程Id为：" + processInstance.getId());
        });
    }
}
