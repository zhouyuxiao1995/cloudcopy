package org.jeecg.modules.flowable.service;

import org.flowable.engine.repository.ProcessDefinition;
import org.jeecg.modules.flowable.entity.ProcessDefinitionModel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author 周余潇
 * @version 1.0
 * @date 2020/8/11 9:54
 * @explain
 **/
public interface IProdefService {
    /**
     * 根据id、name模糊查询流程定义列表
     * @param id
     * @param name
     * @return
     */
    List<ProcessDefinitionModel> getProcessDefinition(String id, String name);
    void startProcessDefinition(List<ProcessDefinitionModel> processDefinitionModelList);
    /**
     * 绘制流程图
     * @param instId
     * @param outputStream
     * @return
     */
    void generateDiagram(String instId, OutputStream outputStream) throws IOException;

    List<ProcessDefinitionModel> getProcessDeploymentList();
}
