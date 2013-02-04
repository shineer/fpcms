package com.fpcms.common.webcrawler.htmlparser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.fpcms.common.util.JsoupSelectorUtil;
import com.fpcms.common.util.JsoupSelectorUtil.JsoupElementParentsSizeComparator;
import com.fpcms.common.util.KeywordUtil;
import com.fpcms.common.util.NetUtil;
import com.fpcms.common.webcrawler.htmlparser.HtmlPage.Anchor;

public class SinglePageCrawler {
	
	private static Logger logger = LoggerFactory.getLogger(SinglePageCrawler.class);
	
	private String[] urlList;
	private String[] acceptUrlRegexList;
	private String[] excludeUriRegexList;
	private String sourceLang; //TODO 自动识别语言
	private String[] mainContentSelector;
	private int minContentLength = 300;
	
	private HtmlPageCrawler htmlPageCrawler = new HtmlPageCrawler() {
		public boolean shoudVisitPage(Anchor a) {
			return true;
		}
		public void visit(HtmlPage page) {
		}
	};
	
	public SinglePageCrawler() {
	}
	
	public SinglePageCrawler(String... url) {
		super();
		setUrlList(url);
	}
	
	public void setHtmlPageCrawler(HtmlPageCrawler htmlPageCrawler) {
		Assert.notNull(htmlPageCrawler,"htmlPageCrawler must be not null");
		this.htmlPageCrawler = htmlPageCrawler;
	}

	public void setSourceLang(String sourceLang) {
		this.sourceLang = sourceLang;
	}

	public void setAcceptUrlRegexList(String... acceptUrlRegex) {
		this.acceptUrlRegexList = acceptUrlRegex;
	}
	
	public void setExcludeUriRegexList(String... excludeUriRegexList) {
		this.excludeUriRegexList = excludeUriRegexList;
	}

	public void setUrlList(String... url) {
		this.urlList = url;
	}

	public void setMainContentSelector(String... mainContentSelector) {
		this.mainContentSelector = mainContentSelector;
	}
	
	public void setMinContentLength(int minContentLength) {
		this.minContentLength = minContentLength;
	}

	public void execute() {
		for(String url : urlList) {
			try {
				crlawUrl(url);
			}catch(Exception e) {
				logger.error("error_on_crlaw_url:"+url,e);
			}
		}
	}

	public List<HtmlPage> crlawUrl(String url) {
		List<Anchor> shoudVisitAnchorList = getShoudVisitAnchorList(url);
		return visitAnchorList(shoudVisitAnchorList);
	}

	List<HtmlPage> visitAnchorList(List<Anchor> shoudVisitAnchorList) {
		List<HtmlPage> visitedPage = new ArrayList<HtmlPage>();
		for(Anchor a : shoudVisitAnchorList) {
			try {
				HtmlPage page = extractArticleByJsoup(a);
				htmlPageCrawler.visit(page);
				visitedPage.add(page);
			}catch(Exception e) {
				logger.warn("extractArticleByJsoup error",e);
			}
		}
		return visitedPage;
	}

	public List<Anchor> getShoudVisitAnchorList(String url) {
		String content = NetUtil.httpGet(url);
		Document doc = Jsoup.parse(content);
		List<Anchor> shoudVisitAnchorList = getShoudVisitAnchorList(url, doc);
		return shoudVisitAnchorList;
	}
	
	private List<Anchor> getShoudVisitAnchorList(String url, Document doc) {
		Elements elements = doc.getElementsByTag("a");
		
		List<Anchor> shoudVisitAnchorList = new ArrayList<Anchor>();
		for(Element anchor : elements) {
			String href = anchor.attr("href");
			String text = StringUtils.trim(anchor.text());
			String title = anchor.attr("title");
			Anchor a = new Anchor();
			
			String fullHref = Anchor.toFullUrl(url,href);
			a.setHref(fullHref);
			a.setText(text);
			a.setTitle(title);
			
			if(isAcceptUrl(a.getHref()) && htmlPageCrawler.shoudVisitPage(a)) {
				shoudVisitAnchorList.add(a);
			}else {
				logger.info("ignore_by_not_accept_url:{}",a.getHref());
			}
		}
		return shoudVisitAnchorList;
	}

	HtmlPage extractArticleByJsoup(Anchor anchor) throws IOException {
		try {
			
			Connection conn = Jsoup.connect(anchor.getHref());
			conn.userAgent("Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)");
			conn.timeout(1000 * 6);
			Document doc = conn.get();
			logger.info("doc.baseUri:"+doc.baseUri() );
			
			String title = smartGetTitle(anchor,doc.title());
			String keywords = JsoupSelectorUtil.select(doc.head(),"[name=keywords]").attr("content");
			String description = JsoupSelectorUtil.select(doc.head(),"[name=description]").attr("content");
			String content = JsoupSelectorUtil.select(doc.body(),mainContentSelector).text();
			Element smartMainContent = smartGetMainContent(doc);
			
			HtmlPage page = new HtmlPage();
			page.setAnchor(anchor);
			page.setContent(StringUtils.defaultIfBlank(content,smartMainContent == null ? null : smartMainContent.text()));
			page.setDescription(description);
			page.setKeywords(keywords);
			page.setTitle(title);
			page.setSourceLang(sourceLang);
			
			
			//TODO 增加anchor.text 与 page.title的比较或者是替换
			logger.info("------------------- url:"+page.getAnchor().getHref()+" ---------------------------");
			logger.info("smartMainContent.text:" + (smartMainContent == null ? "NOT_FOUND" : smartMainContent.text()));
			logger.info("title:"+page.getTitle());
			logger.info("keywords:"+page.getKeywords());
			logger.info("description:"+page.getDescription());
			logger.info("content,size:"+ StringUtils.length(page.getContent()) +" "+page.getContent());
			logger.info("content.deepLevel:"+JsoupSelectorUtil.select(doc,mainContentSelector).parents().size());
			if(smartMainContent != null && StringUtils.isNotBlank(content)) {
				if(!smartMainContent.text().equals(page.getContent())) {
					logger.warn("-------------------error: smart max length text != selector["+StringUtils.join(mainContentSelector,",")+"] text----------------------");
				}
			}
			
			return page;
		}catch(Exception e) {
			throw new RuntimeException("error on extractArticleByJsoup anchor:"+anchor,e);
		}
	}

	private String smartGetTitle(Anchor anchor, String pageTitle) {
//		if(StringUtils.isNotBlank(anchor.getText()) && anchor.getText().length() >= 6) {
//			if(pageTitle.trim().startsWith(anchor.getText())) {
//				return anchor.getText();
//			}
//		}
		return extrectMainTitle(pageTitle);
	}

	private Element smartGetMainContent(Document doc) {
		List<Element> allDiv = JsoupSelectorUtil.selectList(doc,"div");
		Collections.sort(allDiv,new JsoupElementParentsSizeComparator());
		
		for(Element element : allDiv) {
			int conditionSymbolesCount = minContentLength / 50;
			int commonSymbolesCount = KeywordUtil.getCommonSymbolsCount(element.text());
			int divCount = element.getElementsByTag("div").size();
			int parentsSize = element.parents().size();
			
			/*
			 * TODO 增加判断如果出现空格数过多的文字也属于垃圾特征,如: 首页 产品列表 关于我们 
			 * TODO 包含垃圾子段的父亲,也是垃圾
			 * TODO 
			 */
			if(element.text().length() >= minContentLength  && parentsSize >= 4 && commonSymbolesCount > conditionSymbolesCount) {
				if(element.getElementsByTag("a").size() >= 5) {
					continue;
				}
				if(divCount >= 5) {
					continue;
				}
				
				logger.info("success_found_valid_content:"+element.tagName()+ " class:" + element.className() + " id:"+ element.id() + " anchor.count:"+element.getElementsByTag("a").size() + " parentsSize:" + parentsSize + " contentSize:"+element.text().length()+" commonSymbolesCount:"+commonSymbolesCount+" divCount:"+divCount);
				return element;
			}
		}
		return null;
	}

	private static char[] titleSeperator = {'_','-',':','|'};
	static String extrectMainTitle(String title) {
		title = title.trim();
		for(char c : titleSeperator) {
			int indexOf = title.indexOf(c);
			if(indexOf >= 0) {
				return title.substring(0,indexOf).trim();
			}
		}
		return title;
	}

	boolean isAcceptUrl(String href) {
		if(StringUtils.isBlank(href)) {
			return false;
		}
		
		try {
			new URL(href);
		} catch (MalformedURLException e) {
			return false;
		}
		
		if(acceptUrlRegexList == null) {
			return true;
		}
		
		if(excludeUriRegexList != null) {
			for(String exclude : excludeUriRegexList) {
				if(StringUtils.isNotBlank(exclude)) {
					if(href.matches(exclude)) {
						return false;
					}
				}
			}
		}
		
		if(acceptUrlRegexList != null) {
			for(String accept : acceptUrlRegexList) {
				if(StringUtils.isNotBlank(accept)) {
					if(href.matches(accept)) {
						return true;
					}
				}
			}
		}
		return false;
	}

}