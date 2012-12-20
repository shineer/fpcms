package com.fpcms.home.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fpcms.service.CmsChannelService;
import com.fpcms.service.CmsPropertyService;
@Controller
public class LayoutController {

	@Autowired(required=true)
	private CmsChannelService cmsChannelService;
	
	@Autowired(required=true)
	private CmsPropertyService cmsPropertyService;
	
	
	@RequestMapping
	public String layout(ModelMap model) throws Exception {
		model.putAll(cmsPropertyService.findAllGroup());
		
		model.put("nav", cmsChannelService.findChildsByChannelCode("nav"));
		model.put("area", cmsChannelService.findChildsByChannelCode("area"));
		model.put("category", cmsChannelService.findChildsByChannelCode("category"));
		return "/layout";
	}
	
}
