/*
 * Copyright [duowan.com]
 * Web Site: http://www.duowan.com
 * Since 2005 - 2013
 */

package com.fpcms.service;

import com.duowan.common.util.page.Page;
import com.fpcms.model.BlogExternal;
import com.fpcms.query.BlogExternalQuery;


/**
 * [BlogExternal] 的业务操作
 * 
 * @author badqiu email:badqiu(a)gmail.com
 * @version 1.0
 * @since 1.0
 */
public interface BlogExternalService {

	/** 
	 * 创建BlogExternal
	 **/
	public BlogExternal create(BlogExternal blogExternal);
	
	/** 
	 * 更新BlogExternal
	 **/	
    public BlogExternal update(BlogExternal blogExternal);
    
	/** 
	 * 删除BlogExternal
	 **/
    public void removeById(String blogUrl, String username, String password);
    
	/** 
	 * 根据ID得到BlogExternal
	 **/    
    public BlogExternal getById(String blogUrl, String username, String password);
    
	/** 
	 * 分页查询: BlogExternal
	 **/      
	public Page<BlogExternal> findPage(BlogExternalQuery query);
	
    
}
