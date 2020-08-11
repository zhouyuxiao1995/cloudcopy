package org.jeecg.modules.flowable.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 周余潇
 * @version 1.0
 * @date 2020/8/10 15:03
 * @explain
 **/
@Data
public class ProcessDefinitionModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 流程定义id
     */
    private String id;
    /**
     * 流程定义key
     */
    private String key;
    /**
     * 流程定义名称
     */
    private String name;
    /**
     * 流程定义版本
     */
    private int version;

}
