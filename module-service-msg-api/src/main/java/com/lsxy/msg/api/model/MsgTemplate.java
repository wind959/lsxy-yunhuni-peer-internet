package com.lsxy.msg.api.model;

import com.lsxy.framework.api.base.IdEntity;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by liups on 2017/3/1.
 */
@Entity
@Where(clause = "deleted=0")
@Table(schema = "db_lsxy_bi_yunhuni", name = "tb_bi_msg_template")
public class MsgTemplate extends IdEntity {
    public static final int STATUS_PASS = 1;
    public static final int STATUS_WAIT = 0;
    public static final int STATUS_FAIL = -1;
    private String tempId;
    private String tenantId;
    private String appId;
    private String subaccountId;
    private String name;
    private String type;
    private String content;
    private Integer status;
    private String reason;
    private String remark;
    private String lastUserName;

    public MsgTemplate() {
    }

    public MsgTemplate(String tenantId, String appId, String name, String type, String content, String remark) {
        this(tenantId, appId, null,name, type, content, remark);
    }

    public MsgTemplate(String tenantId,String appId,String subaccountId,String name,String type,String content,String remark){
        this.status = STATUS_WAIT;
        this.tenantId = tenantId;
        this.appId = appId;
        this.subaccountId = subaccountId;
        this.name = name;
        this.type = type;
        this.content = content;
        this.remark = remark;
    }

    @Column(name = "temp_id")
    public String getTempId() {
        return tempId;
    }

    public void setTempId(String tempId) {
        this.tempId = tempId;
    }

    @Column(name = "tenant_id")
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Column(name = "app_id")
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Column(name = "subaccount_id")
    public String getSubaccountId() {
        return subaccountId;
    }

    public void setSubaccountId(String subaccountId) {
        this.subaccountId = subaccountId;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Column(name = "status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Column(name = "reason")
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Column(name = "remark")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Column(name = "last_user_name")
    public String getLastUserName() {
        return lastUserName;
    }

    public void setLastUserName(String lastUserName) {
        this.lastUserName = lastUserName;
    }
}
