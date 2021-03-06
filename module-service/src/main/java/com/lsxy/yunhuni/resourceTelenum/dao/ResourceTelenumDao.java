package com.lsxy.yunhuni.resourceTelenum.dao;

import com.lsxy.framework.api.base.BaseDaoInterface;
import com.lsxy.yunhuni.api.resourceTelenum.model.ResourceTelenum;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 全局号码DAO
 * Created by zhangxb on 2016/7/1.
 */
public interface ResourceTelenumDao  extends BaseDaoInterface<ResourceTelenum, Serializable> {

    /**
     * 根据号码获取资源
     * @param telNumber
     * @return
     */
    ResourceTelenum findByTelNumber(String telNumber);

    /**
     * 清除过期的号码
     * @param expireTime 过期时间
     */
    @Modifying
    @Query(value = "UPDATE db_lsxy_bi_yunhuni.tb_oc_resource_telenum num SET num.status=0 , num.tenant_id=NULL,num.app_id=NULL,num.subaccount_id=NULL WHERE num.id IN " +
            "(SELECT rent.res_id FROM db_lsxy_bi_yunhuni.tb_bi_resources_rent rent WHERE rent.deleted=0 AND rent.rent_expire<:expireTime AND rent.res_type=1 AND rent.rent_status IN (1,2))",nativeQuery = true)
    void cleanExpireResourceTelnum(@Param("expireTime") Date expireTime);


    /**
     * 根据状态获取总数
     * @return
     */
    @Query(value = " SELECT COUNT(1) FROM " +
            " (SELECT * FROM db_lsxy_bi_yunhuni.tb_oc_resource_telenum num WHERE num.deleted=0 AND num.status = 0 AND num.usable=1 AND tel_number <> :testNum) a" +
            " INNER JOIN " +
            " (SELECT DISTINCT ttl.tel_number FROM db_lsxy_bi_yunhuni.tb_oc_telnum_to_linegateway ttl WHERE ttl.line_id  IN (:lineIds) AND (ttl.is_dialing=1 OR ttl.is_through=1 ) AND ttl.deleted = 0) b" +
            " ON a.tel_number = b.tel_number" , nativeQuery = true)
    Long countFreeNumber(@Param("testNum") String testNum, @Param("lineIds") List<String> lineIds);

    /**
     * 获取1个空闲的号码
     * @return
     */
    @Query(value = " SELECT * FROM " +
            " (SELECT * FROM db_lsxy_bi_yunhuni.tb_oc_resource_telenum num WHERE num.deleted=0 AND num.status = 0 AND num.usable=1 AND tel_number <> :testNum) a" +
            " INNER JOIN " +
            " (SELECT DISTINCT ttl.tel_number FROM db_lsxy_bi_yunhuni.tb_oc_telnum_to_linegateway ttl WHERE ttl.line_id  IN (:lineIds) AND (ttl.is_dialing=1 OR ttl.is_through=1 ) AND ttl.deleted = 0) b" +
            " ON a.tel_number = b.tel_number limit :random,1" , nativeQuery = true)
    ResourceTelenum findOneFreeNumber(@Param("testNum") String testNum,@Param("lineIds") List<String> lineIds, @Param("random") long random);

    /**
     * 根据呼叫URI查找号码资源
     * @param callUri
     * @return
     */
    ResourceTelenum findByCallUri(String callUri);

    /**
     *
     * @param ids
     * @return
     */
    List<ResourceTelenum> findByIdIn(Collection<String> ids);

    @Query(value = "SELECT * FROM db_lsxy_bi_yunhuni.tb_oc_resource_telenum num WHERE num.tenant_id=:tenantId AND num.tel_number IN (:froms) AND (num.subaccount_id =:subAccountId OR (num.app_id = :appId AND num.subaccount_id IS NULL) OR num.app_id IS NULL)  " +
            " AND num.usable='1' AND (num.is_dialing = '1' OR  num.is_through = '1') AND num.area_id = :areaId  AND num.deleted = 0",nativeQuery = true)
    List<ResourceTelenum> findCallingTelnumByTenantIdAndAppIdAndTelnumAndSubaccountId(@Param("tenantId") String tenantId, @Param("froms") List<String> froms, @Param("appId") String appId, @Param("areaId") String areaId,@Param("subAccountId")String subAccountId);

    @Query(value = "SELECT * FROM db_lsxy_bi_yunhuni.tb_oc_resource_telenum num WHERE num.tenant_id=:tenantId AND num.tel_number IN (:froms) AND (num.app_id = :appId OR num.app_id IS NULL) AND num.subaccount_id IS NULL " +
            " AND num.usable='1' AND (num.is_dialing = '1' OR  num.is_through = '1') AND num.area_id = :areaId AND num.deleted = 0",nativeQuery = true)
    List<ResourceTelenum> findCallingTelnumByTenantIdAndAppIdAndTelnum(@Param("tenantId") String tenantId, @Param("froms") List<String> froms, @Param("appId") String appId, @Param("areaId") String areaId);

    @Query(value = " SELECT * FROM db_lsxy_bi_yunhuni.tb_oc_resource_telenum num WHERE num.tenant_id=:tenantId AND num.app_id=:appId AND num.subaccount_id =:subAccountId  " +
            " AND num.usable='1' AND (num.is_dialing = '1' OR  num.is_through = '1') AND num.area_id = :areaId AND num.deleted = 0 LIMIT 1",nativeQuery = true)
    ResourceTelenum findCallingTelnumByTenantIdAndAppIdAndSubaccountId(@Param("tenantId") String tenantId, @Param("appId") String appId, @Param("areaId") String areaId,@Param("subAccountId")String subAccountId);

    @Query(value = " SELECT * FROM db_lsxy_bi_yunhuni.tb_oc_resource_telenum num WHERE num.tenant_id=:tenantId AND num.app_id=:appId AND num.subaccount_id IS NULL " +
            " AND num.usable='1' AND (num.is_dialing = '1' OR  num.is_through = '1') AND num.area_id = :areaId AND num.deleted = 0 LIMIT 1",nativeQuery = true)
    ResourceTelenum findCallingTelnumByTenantIdAndAppId(@Param("tenantId") String tenantId, @Param("appId") String appId, @Param("areaId") String areaId);

    @Query(value = " SELECT * FROM db_lsxy_bi_yunhuni.tb_oc_resource_telenum num WHERE num.tenant_id=:tenantId AND num.app_id IS NULL " +
            " AND num.usable='1' AND (num.is_dialing = '1' OR  num.is_through = '1') AND num.area_id = :areaId AND num.deleted = 0 LIMIT 1",nativeQuery = true)
    ResourceTelenum findCallingTelnumByTenantIdAndAppIdIsNull(@Param("tenantId") String tenantId, @Param("areaId") String areaId);

    List<ResourceTelenum> findByTelNumberIn(Collection<String> telNumbers);

    ResourceTelenum findFirstByTelNumberOrCallUri(String num, String num1);

    ResourceTelenum findFirstByTenantIdAndAppIdAndUsableAndIsCalled(String tenantId, String appId, String usableTrue, String iscalledTrue);

    /**
     * 应用释放所有的号码
     * @param tenantId
     * @param appId
     */
    @Modifying
    @Query(value = "update db_lsxy_bi_yunhuni.tb_oc_resource_telenum num set num.app_id = null,num.subaccount_id = null,num.last_time = :date where num.tenant_id = :tenantId and num.app_id = :appId and num.deleted = 0 ",nativeQuery = true)
    void appUnbindAll(@Param("tenantId") String tenantId, @Param("appId") String appId,@Param("date") Date date);

    /**
     * 子账号释放所有号码
     * @param appId 应用Id
     * @param subaccountId 子账号Id
     * @param date 时间
     */
    @Modifying
    @Query(value = "update db_lsxy_bi_yunhuni.tb_oc_resource_telenum num set num.subaccount_id = null,num.last_time = :date where num.tenant_id = :tenantId and num.app_id = :appId and num.subaccount_id = :subaccountId and num.deleted = 0 ",nativeQuery = true)
    void subaccountUnbindAll(@Param("tenantId") String tenantId,@Param("appId") String appId,@Param("subaccountId") String subaccountId,@Param("date") Date date);
}
