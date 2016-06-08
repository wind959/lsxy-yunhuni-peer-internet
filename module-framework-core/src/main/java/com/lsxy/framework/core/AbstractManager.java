package com.lsxy.framework.core;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.lsxy.framework.core.persistence.BaseDaoInterface;
import com.lsxy.framework.core.persistence.IdEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lsxy.framework.core.utils.BeanUtils;
import com.lsxy.framework.core.utils.HqlUtil;
import com.lsxy.framework.core.utils.Page;

import org.springframework.transaction.annotation.Transactional;

/**
 * 抽象manager
 * @author tandy
 *
 * @param <T>
 */
@SuppressWarnings({"rawtypes"})
public abstract class AbstractManager<T> {
	private Log logger = LogFactory.getLog(AbstractManager.class);
	
	public abstract BaseDaoInterface<T,Serializable> getDao();
	
	
	@PersistenceContext  
	 private EntityManager em; 
	
	public EntityManager getEm() {
		return em;
	}

	/**
	 * 实体数量
	 * @return
	 */
	public long count(){
		Class<T> x = this.getEntityClass();
		String hql = "select count(*) from "+x.getName()+" obj where obj.deleted=false";
		Query query = em.createQuery(hql);
		Long count = (Long) query.getSingleResult();
		return count;
	}
	
	/**
	 * 保存
	 * @param entity
	 */
	public T save(T entity){
		T  obj = this.getDao().save(entity);
		return obj;
	}
//	
//	/**
//	 * 删除所有对象
//	 */
//	public void cleanAll(){
//		this.getDao().deleteAll();
//	}
//	
//	/**
//	 * 根据标识删除对象
//	 * @param id
//	 */
//	public void delete(Serializable id){
//		this.getDao().delete(id);
//	}
//	
//	/**
//	 * 删除对象
//	 * @param object
//	 */
//	public void delete(T object){
//		this.getDao().delete(object);
//	}
	
	/**
	 * 根据id逻辑删除实体对象
	 * @param id
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void logicDelete(Serializable id) throws IllegalAccessException, InvocationTargetException{
		T obj = this.getDao().findOne(id);
		this.logicDelete(obj);
	}
	
	/**
	 * 逻辑删除实体对象,将对象属性delete 设置为true
	 * @param obj
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void logicDelete(T obj) throws IllegalAccessException, InvocationTargetException{
		if(obj != null){
			BeanUtils.setProperty(obj, "deleted", true);
			this.save(obj);
//			this.getEm().flush();
//			this.getEm().detach(obj);
		}
	}
	
	/**
	 * 根据条件执行逻辑删除
	 * @param property
	 * @param value
	 */
	public void logicDeleteByCondition(String property,Object value){
		Class<T> x = this.getEntityClass();
		String hql = "update "+x.getName()+" as obj set obj.deleted=true";
		hql = HqlUtil.addCondition(hql, property,value,HqlUtil.LOGIC_AND,HqlUtil.TYPE_OBJECT);
		Query query = this.getEm().createQuery(hql);
		query.setParameter(property.replace(".", ""), value);
		logger.debug("-----hql:"+hql);
		logger.debug("-----hql:"+hql);
		int count = query.executeUpdate();
		logger.debug("result:" + count);
	}
	
	
	/**
	 * 自定义查询方法，不带分页,默认排除掉deleted数据
	 * @param jpql
	 * @param params
	 * @return
	 */
	public List findByCustomWithParams(String jpql,Object ... params){
		return this.findByCustomWithParams(jpql,true, params);
	}
	
	/**
	 * from LoginLog ll where ll.personId='' order by ll.dt desc
	 */
	public List findByCustomWithParams(String jpql,boolean excludeDeleted,Object ... params){
		if(excludeDeleted)
			jpql = HqlUtil.addCondition(jpql, "deleted", 0,HqlUtil.LOGIC_AND,HqlUtil.TYPE_NUMBER);
		Query query = this.em.createQuery(jpql);
		for (int i = 0; i < params.length; i++) {
			Object object = params[i];
			query.setParameter(i+1, object);
		}
		List loglist = query.getResultList();
		return loglist;
	}
	/**
	 * 自定义查询方法，带分页
	 * 默认排除deleted
	 */
	public Page findByCustom(String jpql,int pageNo,int pageSize){
		return this.findByCustom(jpql, true, pageNo, pageSize);
	}
	
	/**
	 * 获取最后一页数据
	 * @param jpql
	 * @param pageSize
	 * @return
	 */
	public Page findLastPageByCustom(String jpql,int pageSize){
		return this.findLastPageByCustom(jpql, true, pageSize);
	}
	/**
	 * from LoginLog ll where ll.personId='' order by ll.dt desc
	 */
	public Page findByCustom(String jpql,boolean excludeDeleted,int pageNo,int pageSize){
		logger.debug("findByCustom:"+jpql);
		if(excludeDeleted)
			jpql = HqlUtil.addCondition(jpql, "deleted", 0,HqlUtil.LOGIC_AND,HqlUtil.TYPE_NUMBER);
		pageNo--;
		String countJpql = " select count (*) " + HqlUtil.removeOrders(HqlUtil.removeSelect(jpql));
		Query query = this.em.createQuery(countJpql);
		Object obj = query.getSingleResult();
		long totalCount = (Long) obj;
		
		query = this.em.createQuery(jpql);
		query.setMaxResults(pageSize);
		query.setFirstResult(pageNo*pageSize);
		List list = query.getResultList();
		Page page = new Page((pageNo)*pageSize+1,totalCount,pageSize,list);
		return page;
	}

	/**
	 * from LoginLog ll where ll.personId='' order by ll.dt desc
	 */
	public Page findByCustom(String jpql,boolean excludeDeleted,int pageNo,int pageSize,Object ... params){
		logger.debug("findByCustom:"+jpql);
		if(excludeDeleted)
			jpql = HqlUtil.addCondition(jpql, "deleted", 0,HqlUtil.LOGIC_AND,HqlUtil.TYPE_NUMBER);
		pageNo--;
		String countJpql = " select count (*) " + HqlUtil.removeOrders(HqlUtil.removeSelect(jpql));
		Query query = this.em.createQuery(countJpql);
		Object obj = query.getSingleResult();
		long totalCount = (Long) obj;
		
		query = this.em.createQuery(jpql);
		query.setMaxResults(pageSize);
		query.setFirstResult(pageNo*pageSize);
		
		for (int i = 0; i < params.length; i++) {
			Object object = params[i];
			query.setParameter(i+1, object);
		}
		
		List list = query.getResultList();
		Page page = new Page((pageNo)*pageSize+1,totalCount,pageSize,list);
		return page;
	}
	/**
	 * 获取最后一页数据
	 * @param jpql
	 * @param pageSize
	 * @return
	 */
	public Page findLastPageByCustom(String jpql,boolean excludeDeleted,int pageSize){
		logger.debug("findByCustom:"+jpql);
		if(excludeDeleted)
			jpql = HqlUtil.addCondition(jpql, "deleted", 0,HqlUtil.LOGIC_AND,HqlUtil.TYPE_NUMBER);
		String countJpql = " select count (*) " + HqlUtil.removeOrders(HqlUtil.removeSelect(jpql));
		Query query = this.em.createQuery(countJpql);
		Object obj = query.getSingleResult();
		int totalCount =((Long) obj).intValue();
		int lastPageNo = totalCount/pageSize+(totalCount%pageSize == 0?0:1);
		query = this.em.createQuery(jpql);
		query.setMaxResults(pageSize);
		query.setFirstResult(lastPageNo*pageSize);
		List list = query.getResultList();
		Page page = new Page((lastPageNo)*pageSize+1,totalCount,pageSize,list);
		return page;
	}
	
	@SuppressWarnings("unchecked")
	protected Class<T> getEntityClass() {   
        Type genType = this.getClass().getGenericSuperclass();   
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();   
        return (Class<T>) params[0];
    }
	
	public T findById(Serializable id){
		return this.getDao().findOne(id);
	}

	/**
	 * 执行hql update and delete
	 * @param hql
	 * @param params
	 * @return
	 */
	public int exec(String hql, Object[] params) {
		Query query = em.createQuery(hql);
		for (int i = 0; i < params.length; i++) {
			Object object = params[i];
			query.setParameter(i+1, object);
		}
		return query.executeUpdate();
	}
	
	/**
	 * 查询以数字返回的结果，一般用在count查询中
	 * @param hql
	 * @param params
	 * @return
	 */
	public long findLong(String hql,Object ... params){
		Query query = this.getEm().createQuery(hql);
		for (int i = 0; i < params.length; i++) {
			Object object = params[i];
			query.setParameter(i+1, object);
		}
		Object o = query.getSingleResult();
		if(o instanceof Long){
			return (Long)o;
		}
		return 0;
	}



	@Transactional
	public void batchInsert(List list) {
		for (int i = 0; i < list.size(); i++) {
			em.persist(list.get(i));
			if (i % 30 == 0) {
				em.flush();
				em.clear();
			}
		}
	}

	@Transactional
	public void batchUpdate(List list) {
		for (int i = 0; i < list.size(); i++) {
			em.merge(list.get(i));
			if (i % 30 == 0) {
				em.flush();
				em.clear();
			}
		}
	}
	/**
	 * 批量逻辑删除
	 * @param list
	 */
	@Transactional
	public void batchDelete(List<IdEntity> list) {
		for (int i = 0; i < list.size(); i++) {
			IdEntity entity = list.get(i);
			entity.setDeleted(true);
			em.merge(entity);
			if (i % 30 == 0) {
				em.flush();
				em.clear();
			}
		}
	}
}