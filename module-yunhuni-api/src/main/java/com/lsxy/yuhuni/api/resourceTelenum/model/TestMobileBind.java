package com.lsxy.yuhuni.api.resourceTelenum.model;

import com.lsxy.framework.api.base.IdEntity;
import com.lsxy.framework.api.tenant.model.Tenant;

import javax.persistence.*;

/**
 * 测试号码绑定
 * Created by zhangxb on 2016/7/2.
 */
@Entity
@Table(schema = "db_lsxy_bi_yunhuni",name="tb_bi_test_mobile_bind")
public class TestMobileBind extends IdEntity {
    private String number;//测试号码
    Tenant tenant;//所属租户

    @Column( name = "number")
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @ManyToOne
    @JoinColumn( name = "tenant_id")
    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }
}
