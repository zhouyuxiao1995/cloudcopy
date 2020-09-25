package org.jeecg.modules.flowable.controller;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.repository.Deployment;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.User;
import org.flowable.idm.engine.impl.persistence.entity.UserEntityImpl;
import org.flowable.ui.common.model.GroupRepresentation;
import org.flowable.ui.common.model.ResultListDataRepresentation;
import org.flowable.ui.common.model.UserRepresentation;
import org.flowable.ui.common.security.DefaultPrivileges;
import org.flowable.ui.common.security.SecurityUtils;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.flowable.entity.ProcessDefinitionModel;
import org.jeecg.modules.flowable.service.IProdefService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 周余潇
 * @version 1.0
 * @date 2020/8/7 9:59
 * @explain
 **/

@RestController
@RequestMapping("/flowable")
public class FlowableController {
    private static final Logger logger = LoggerFactory.getLogger(FlowableController.class);
    @Resource
    protected IdentityService idmIdentityService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ModelService modelService;

    @Autowired
    private IProdefService prodefService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProcessEngine processEngine;

    /**
     * 获取默认的管理员信息
     *
     * @return
     */
    @RequestMapping(value = "/rest/account", method = RequestMethod.GET, produces = "application/json")
    public UserRepresentation getAccount() {
        User user = new UserEntityImpl();
        user.setId("admin");
        SecurityUtils.assumeUser(user);
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId("admin");
        userRepresentation.setFirstName("admin");
        List<String> privileges = new ArrayList<>();
        List<String> pris = new ArrayList<>();
        pris.add(DefaultPrivileges.ACCESS_MODELER);
        pris.add(DefaultPrivileges.ACCESS_IDM);
        pris.add(DefaultPrivileges.ACCESS_ADMIN);
        pris.add(DefaultPrivileges.ACCESS_TASK);
        pris.add(DefaultPrivileges.ACCESS_REST_API);
        userRepresentation.setPrivileges(privileges);
        return userRepresentation;
    }

    /**
     * 用户组查询
     *
     * @param filter
     * @return
     */
    @RequestMapping(value = "/rest/editor-groups", method = RequestMethod.GET)
    public ResultListDataRepresentation getGroups(@RequestParam(required = false, value = "filter") String filter) {
        if (StringUtils.isNotBlank(filter)) {
            filter = filter.trim();
            String sql = "select * from act_id_group where NAME_ like #{name}";
            filter = "%" + filter + "%";
            List<Group> groups = idmIdentityService.createNativeGroupQuery().sql(sql).parameter("name", filter).listPage(0, 10);
            List<GroupRepresentation> result = new ArrayList<>();
            for (Group group : groups) {
                result.add(new GroupRepresentation(group));
            }
            return new ResultListDataRepresentation(result);
        }
        return null;
    }

    /**
     * 用户查询
     *
     * @param filter
     * @return
     */
    @RequestMapping(value = "/rest/editor-users", method = RequestMethod.GET)
    public ResultListDataRepresentation getUsers(@RequestParam(value = "filter", required = false) String filter) {
        if (StringUtils.isNotBlank(filter)) {
            filter = filter.trim();
            String sql = "select * from act_id_user where ID_ like #{id} or LAST_ like #{name}";
            filter = "%" + filter + "%";
            List<User> matchingUsers = idmIdentityService.createNativeUserQuery().sql(sql).parameter("id", filter).parameter("name", filter).listPage(0, 10);
            List<UserRepresentation> userRepresentations = new ArrayList<>(matchingUsers.size());
            for (User user : matchingUsers) {
                userRepresentations.add(new UserRepresentation(user));
            }
            return new ResultListDataRepresentation(userRepresentations);
        }
        return null;
    }

    @GetMapping("deploy")
    public void deployProcess(String modelId) {
        logger.info("流程部署入参modelId：{}", modelId);
        // 通过act_de_model中存放的Modeler内容来部署
        Model modelData = modelService.getModel(modelId);
        if (modelData == null)
            logger.error("模型数据为空，请先设计流程并成功保存，再进行发布。");
        ObjectNode modelNode = null;
        // 获取模型
        byte[] bytes = modelService.getBpmnXML(modelData);
        if (null == bytes) {
            logger.error("模型数据为空，请先设计流程并成功保存，再进行发布。");
        }
        BpmnModel model = modelService.getBpmnModel(modelData);
        if (model.getProcesses().size() == 0) {
            logger.error("error", "数据模型不符要求，请至少设计一条主线流程。");
        }
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addBytes(processName, bpmnBytes)
                .deploy();
    }

    @GetMapping("/load/process-definition")
    public List<ProcessDefinitionModel> getProcessDefinition(String id, String name) {
        return prodefService.getProcessDefinition(id, name);
    }

    @PostMapping(value = "/start/process-instance")
    public Result<?> addExpense(@RequestBody JSONObject jsonObject) {
        List<ProcessDefinitionModel> processDefinitionModelList = jsonObject.getJSONArray("processDefinitionModelList") == null ? new ArrayList<>() : jsonObject.getJSONArray("processDefinitionModelList").toJavaList(ProcessDefinitionModel.class);
        prodefService.startProcessDefinition(processDefinitionModelList);
        return Result.ok("流程实例启动成功!");
    }

    @GetMapping(value = "/processDiagram")
    public void genProcessDiagram(HttpServletResponse httpServletResponse, @RequestParam(value="processId") String processId) throws Exception {
        prodefService.generateDiagram(processId,httpServletResponse.getOutputStream());
    }

}
