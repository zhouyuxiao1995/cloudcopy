package org.jeecg.modules.flowable.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;
import org.jeecg.modules.flowable.entity.ProcessDefinitionModel;
import org.jeecg.modules.flowable.service.IProdefService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Resource
    private HistoryService historyService;

    @Resource
    private TaskService taskService;

    @Autowired
    ProcessEngineConfiguration processEngineConfiguration;

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

    /* *//**
     * 根据流程实例Id,获取实时流程图片
     *
     * @param processInstanceId
     * @param outputStream
     *//*
    @Override
    public void generateDiagram(String processInstanceId, OutputStream outputStream) throws IOException {
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        //流程走完的不显示图
        if (pi == null) {
            return;
        }
        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        //使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
        String InstanceId = task.getProcessInstanceId();
        List<Execution> executions = runtimeService
                .createExecutionQuery()
                .processInstanceId(InstanceId)
                .list();

        //得到正在执行的Activity的Id
        List activityIds = new ArrayList<>();
        List flows = new ArrayList<>();
        for (Execution exe : executions) {
            List ids = runtimeService.getActiveActivityIds(exe.getId());
            activityIds.addAll(ids);
        }

        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration engconf = processEngineConfiguration.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, "宋体","宋体","宋体",null,1.0,false);
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            while ((legth = in.read(buf)) != -1) {
                outputStream.write(buf, 0, legth);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }

    }*/

    /**
     * 根据流程实例Id,获取实时流程图片
     *
     * @param processInstanceId
     * @param outputStream
     */
    public void generateDiagram(String processInstanceId, OutputStream outputStream) {
        try {
            if (org.springframework.util.StringUtils.isEmpty(processInstanceId)) {
                logger.error("processInstanceId is null");
                return;
            }
            // 获取历史流程实例
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            // 获取流程中已经执行的节点，按照执行先后顺序排序
            List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId)
                    .orderByHistoricActivityInstanceId().asc().list();
            // 高亮已经执行流程节点ID集合
            List<String> highLightedActivitiIds = new ArrayList<>();
            for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
                highLightedActivitiIds.add(historicActivityInstance.getActivityId());
            }

            List<HistoricProcessInstance> historicFinishedProcessInstances = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).finished()
                    .list();
            ProcessDiagramGenerator processDiagramGenerator = null;
            // 如果还没完成，流程图高亮颜色为绿色，如果已经完成为红色
            if (!CollectionUtils.isEmpty(historicFinishedProcessInstances)) {
                // 如果不为空，说明已经完成
                processDiagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
            } else {
                //TODO 生成颜色有问题，没有生成绿色
                processDiagramGenerator = new DefaultProcessDiagramGenerator();
            }

            BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());
            // 高亮流程已发生流转的线id集合
            List<String> highLightedFlowIds = getHighLightedFlows(bpmnModel, historicActivityInstances);

            // 使用默认配置获得流程图表生成器，并生成追踪图片字符流
            InputStream imageStream = processDiagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivitiIds, highLightedFlowIds, "宋体", "微软雅黑", "黑体", null, 2.0, false);

            // 输出图片内容
            byte[] b = new byte[1024];
            int len;
            while ((len = imageStream.read(b, 0, 1024)) != -1) {
                outputStream.write(b, 0, len);
            }
        } catch (Exception e) {
            logger.error("processInstanceId" + processInstanceId + "生成流程图失败，原因：" + e.getMessage(), e);
        }

    }

    /**
     * 获取已经流转的线
     *
     * @param bpmnModel
     * @param historicActivityInstances
     * @return
     */
    private static List<String> getHighLightedFlows(BpmnModel bpmnModel, List<HistoricActivityInstance> historicActivityInstances) {
        // 高亮流程已发生流转的线id集合
        List<String> highLightedFlowIds = new ArrayList<>();
        // 全部活动节点
        List<FlowNode> historicActivityNodes = new ArrayList<>();
        // 已完成的历史活动节点
        List<HistoricActivityInstance> finishedActivityInstances = new ArrayList<>();

        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
            if (!historicActivityInstance.getActivityId().startsWith("sid-")) {
                FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstance.getActivityId(), true);
                historicActivityNodes.add(flowNode);
                if (historicActivityInstance.getEndTime() != null) {
                    finishedActivityInstances.add(historicActivityInstance);
                }
            }

        }

        FlowNode currentFlowNode = null;
        FlowNode targetFlowNode = null;
        // 遍历已完成的活动实例，从每个实例的outgoingFlows中找到已执行的
        for (HistoricActivityInstance currentActivityInstance : finishedActivityInstances) {
            // 获得当前活动对应的节点信息及outgoingFlows信息
            currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currentActivityInstance.getActivityId(), true);
            List<SequenceFlow> sequenceFlows = currentFlowNode.getOutgoingFlows();

            /**
             * 遍历outgoingFlows并找到已已流转的 满足如下条件认为已已流转： 1.当前节点是并行网关或兼容网关，则通过outgoingFlows能够在历史活动中找到的全部节点均为已流转 2.当前节点是以上两种类型之外的，通过outgoingFlows查找到的时间最早的流转节点视为有效流转
             */
            if ("parallelGateway".equals(currentActivityInstance.getActivityType()) || "inclusiveGateway".equals(currentActivityInstance.getActivityType())) {
                // 遍历历史活动节点，找到匹配流程目标节点的
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    targetFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getTargetRef(), true);
                    if (historicActivityNodes.contains(targetFlowNode)) {
                        highLightedFlowIds.add(targetFlowNode.getId());
                    }
                }
            } else {
                List<Map<String, Object>> tempMapList = new ArrayList<>();
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
                        if (historicActivityInstance.getActivityId().equals(sequenceFlow.getTargetRef())) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("highLightedFlowId", sequenceFlow.getId());
                            map.put("highLightedFlowStartTime", historicActivityInstance.getStartTime().getTime());
                            tempMapList.add(map);
                        }
                    }
                }

                if (!CollectionUtils.isEmpty(tempMapList)) {
                    // 遍历匹配的集合，取得开始时间最早的一个
                    long earliestStamp = 0L;
                    String highLightedFlowId = null;
                    for (Map<String, Object> map : tempMapList) {
                        long highLightedFlowStartTime = Long.valueOf(map.get("highLightedFlowStartTime").toString());
                        if (earliestStamp == 0 || earliestStamp >= highLightedFlowStartTime) {
                            highLightedFlowId = map.get("highLightedFlowId").toString();
                            earliestStamp = highLightedFlowStartTime;
                        }
                    }

                    highLightedFlowIds.add(highLightedFlowId);
                }

            }

        }
        return highLightedFlowIds;
    }
}
