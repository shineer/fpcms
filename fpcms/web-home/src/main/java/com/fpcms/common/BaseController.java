package com.fpcms.common;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.duowan.common.util.page.Page;
import com.duowan.common.util.page.PageQuery;
import com.fpcms.common.util.SpringMVCUtils;
import com.fpcms.common.util.URLUtil;

public class BaseController {
	protected static Logger logger = LoggerFactory.getLogger(BaseController.class); 
	public static String UPDATE_SUCCESS = "UPDATE_SUCCESS";
	public static String DELETE_SUCCESS = "DELETE_SUCCESS";
	public static String CREATED_SUCCESS = "CREATED_SUCCESS";
	
	protected ModelMap toModelMap(Page page, PageQuery query) {
		return SpringMVCUtils.toModelMap(page, query);
	}
	
	protected String getSite() {
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		String url = request.getRequestURL().toString();
		return URLUtil.getHostSite(url);
	}

	
}
