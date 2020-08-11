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
@TableName("act_id_user")
public class ActIdUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id_;
    private int rev_;
    private String first_;
    private String last_;
    private String displayName_;
    private String email_;
    private String pictureId_;
    private String tenantId_;

}
