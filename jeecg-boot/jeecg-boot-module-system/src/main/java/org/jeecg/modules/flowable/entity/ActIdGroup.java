package org.jeecg.modules.flowable.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 周余潇
 * @version 1.0
 * @date 2020/8/10 15:03
 * @explain
 **/
@Data
@TableName("act_id_group")
public class ActIdGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id_;
    private int rev_;
    private String name_;
    private String type_;

}
