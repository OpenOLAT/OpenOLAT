/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.util;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.olat.util.FunctionalHomeSiteUtil.EPortfolioAction;
import org.olat.util.FunctionalUtil.OlatSite;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalEPortfolioUtil {
	public final static String EP_ARTEFACTS_BUSINESS_PATH = "EPArtefacts";
	public final static String EP_MAPS_BUSINESS_PATH = "EPMaps";
	
	public final static String EPORTFOLIO_CSS = "b_eportfolio";
	public final static String EPORTFOLIO_MAP_CSS = "b_eportfolio_map";
	public final static String EPORTFOLIO_PAGE_CSS = "b_eportfolio_page";
	public final static String EPORTFOLIO_STRUCTURE_CSS = "b_eportfolio_structure";
	public final static String EPORTFOLIO_ARTEFACT_CSS = "b_artefact";
	public final static String EPORTFOLIO_ARTEFACT_DETAILS_CSS = "o_sel_artefact_details";
	
	public final static String HOME_PORTAL_EDIT_LINK_CSS = "o_sel_add_artfeact";
	public final static String ADD_TEXT_ARTEFACT_CSS = "o_sel_add_text_artfeact";
	public final static String UPLOAD_FILE_ARTEFACT_CSS = "o_sel_add_upload_artfeact";
	public final static String CREATE_LEARNING_JOURNAL_CSS = "o_sel_add_liveblog_artfeact";
	
	public final static String ADD_BINDER_BOX_ID = "o_addMapBox";
	public final static String CREATE_BINDER_CSS = "o_sel_create_map";
	public final static String CREATE_DEFAULT_BINDER_CSS = "o_sel_create_default_map";
	public final static String CREATE_TEMPLATE_BINDER_CSS = "o_sel_create_template_map";
	public final static String OPEN_BINDER_ICON_CSS = "b_open_icon";
	
	public final static String EDIT_LINK_CSS = "b_eportfolio_edit_link";
	
	public final static String EPORTFOLIO_TABLE_OF_CONTENTS_CSS = "b_portfolio_toc";
	public final static String EPORTFOLIO_TOC_LEVEL1_CSS = "level1";
	public final static String EPORTFOLIO_TOC_LEVEL2_CSS = "level2";
	
	public final static String ADD_LINK_CSS = "b_eportfolio_add_link";
	public final static String PAGE_TABS_CSS = "b_pagination";
	
	public final static String PAGE_ICON_CSS = "b_eportfolio_link";
	public final static String STRUCT_ICON_CSS = "b_ep_struct_icon";
	
	public final static String ARTEFACT_CSS = "b_artefact";
	public final static String TAG_ICON_CSS = "b_tag_icon";

	
	public enum ArtefactDisplay {
		TABLE,
		THUMBNAILS,
	}
	
	private String eportfolioCss;
	private String eportfolioMapCss;
	private String eportfolioPageCss;
	private String eportfolioStructureCss;
	private String eportfolioArtefactCss;
	private String eportfolioArtefactDetailsCss;
	
	private String homePortalEditLinkCss;
	private String addTextArtefactCss;
	private String uploadFileArtefactCss;
	private String createLearningJournalCss;
	
	private String addBinderBoxId;
	private String createBinderCss;
	private String createDefaultBinderCss;
	private String createTemplateBinderCss;
	private String openBinderCss;
	
	private String editLinkCss;
	
	private String eportfolioTableOfContentsCss;
	private String eportfolioTOCLevel1Css;
	private String eportfolioTOCLevel2Css;
	
	private String addLinkCss;
	private String pageTabsCss;
	
	private String pageIconCss;
	private String structIconCss;
	
	private String artefactCss;
	private String tagIconCss;
	
	private FunctionalUtil functionalUtil;
	private FunctionalHomeSiteUtil functionalHomeSiteUtil;
	
	public FunctionalEPortfolioUtil(FunctionalUtil functionalUtil, FunctionalHomeSiteUtil functionalHomeSiteUtil){
		this.functionalUtil = functionalUtil;
		this.functionalHomeSiteUtil = functionalHomeSiteUtil;
		
		setEPortfolioCss(EPORTFOLIO_CSS);
		setEPortfolioMapCss(EPORTFOLIO_MAP_CSS);
		setEPortfolioPageCss(EPORTFOLIO_PAGE_CSS);
		setEPortfolioStructureCss(EPORTFOLIO_STRUCTURE_CSS);
		setEPortfolioArtefactCss(EPORTFOLIO_ARTEFACT_CSS);
		setEPortfolioArtefactDetailsCss(EPORTFOLIO_ARTEFACT_DETAILS_CSS);
		
		setHomePortalEditLinkCss(HOME_PORTAL_EDIT_LINK_CSS);
		setAddTextArtefactCss(ADD_TEXT_ARTEFACT_CSS);
		setUploadFileArtefactCss(UPLOAD_FILE_ARTEFACT_CSS);
		setCreateLearningJournalCss(CREATE_LEARNING_JOURNAL_CSS);
		
		setAddBinderBoxId(ADD_BINDER_BOX_ID);
		setCreateBinderCss(CREATE_BINDER_CSS);
		setCreateDefaultBinderCss(CREATE_DEFAULT_BINDER_CSS);
		setCreateTemplateBinderCss(CREATE_TEMPLATE_BINDER_CSS);
		setOpenBinderCss(OPEN_BINDER_ICON_CSS);
		
		setEditLinkCss(EDIT_LINK_CSS);
		
		setEPortfolioTableOfContentsCss(EPORTFOLIO_TABLE_OF_CONTENTS_CSS);
		setEPortfolioTOCLevel1Css(EPORTFOLIO_TOC_LEVEL1_CSS);
		setEPortfolioTOCLevel2Css(EPORTFOLIO_TOC_LEVEL2_CSS);
		
		setAddLinkCss(ADD_LINK_CSS);
		setPageTabsCss(PAGE_TABS_CSS);
		
		setPageIconCss(PAGE_ICON_CSS);
		setStructIconCss(STRUCT_ICON_CSS);
		
		setArtefactCss(ARTEFACT_CSS);
		setTagIconCss(TAG_ICON_CSS);
	}

	/**
	 * @param browser
	 * @param key
	 * @param artefactTitle
	 * @return
	 * 
	 * Opens the artefact details popup.
	 */
	public boolean openArtefactDetails(Selenium browser, Long key, String artefactTitle){
		String epArtefactsBusinessPath = functionalUtil.getDeploymentUrl().toString() + "/url/HomeSite/" + key + "/" + EP_ARTEFACTS_BUSINESS_PATH;
		
//		do{
//			browser.open(epArtefactsBusinessPath);
//		}while();
		
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * @param browser
	 * @return
	 * 
	 * Clicks within the artefact details popup the link to it's content.
	 */
	public boolean clickArtefactContent(Selenium browser){
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * @param browser
	 * @param key
	 * @param binderTitle
	 * @return
	 * 
	 * Opens the specified binder.
	 */
	public boolean openBinder(Selenium browser, Long key, String binderTitle){
		String epMapsBusinessPath = functionalUtil.getDeploymentUrl().toString() + "/url/HomeSite/" + key + "/" + EP_MAPS_BUSINESS_PATH;
		
		//TODO:JK: implement me
		
		return(false);
	}
	
	/**
	 * @param binder
	 * @param page
	 * @param structure
	 * @return
	 * 
	 * Creates an xpath selector of specified binder, page and structure.
	 */
	public String createSelector(String binder, String page, String structure){
		if(binder == null || binder.isEmpty() || page == null || page.isEmpty())
			return(null);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=");
		selectorBuffer.append("//ul//li//a//span[text()='")
		.append(binder)
		.append("']/../../..//ul//li//a//span[text()='")
		.append(page)
		.append("']");
		
		if(structure != null && !structure.isEmpty()){
			selectorBuffer.append("/../../..//ul//li//a//span[text()='")
			.append(structure)
			.append("']");
		}
		
		selectorBuffer.append("/..");
		
		return(selectorBuffer.toString());
	}
	
	/**
	 * @param browser
	 * @param binder
	 * @param page
	 * @param structure
	 * @return true on success
	 * 
	 * Creates the specified elements (binder, page, structure) 
	 */
	public boolean createElements(Selenium browser, String binder, String page, String structure){
		if(!binderExists(browser, binder)){
			createDefaultBinder(browser, binder, null);
			
			createPage(browser, binder, page, ArtefactDisplay.THUMBNAILS, null);
			
			createStructure(browser, binder, page, structure, null);
		}else{
			if(!pageExists(browser, binder, page)){
				createPage(browser, binder, page, ArtefactDisplay.THUMBNAILS, null);
				
				createStructure(browser, binder, page, structure, null);
			}else{
				if(!structureExists(browser, binder, page, structure) && structure != null){
					createStructure(browser, binder, page, structure, null);
				}
			}
		}
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param title
	 * @return true if binder containing title exists else false.
	 * 
	 * Checks if binder containing title exists.
	 */
	public boolean binderExists(Selenium browser, String title){
		if(!functionalUtil.openSite(browser, OlatSite.HOME))
			return(false);
		
		if(!functionalHomeSiteUtil.openActionByMenuTree(browser, EPortfolioAction.MY_BINDERS))
			return(false);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//h4[text()='")
		.append(title)
		.append("']/..//a[contains(@class, '")
		.append(getOpenBinderCss())
		.append("')]");
		
		if(browser.isElementPresent(selectorBuffer.toString())){
			return(true);
		}else{
			return(false);
		}
	}
	
	/**
	 * @param browser
	 * @param title
	 * @return true on success
	 * 
	 * Opens a specified binder.
	 */
	public boolean openBinder(Selenium browser, String title){
		if(!functionalUtil.openSite(browser, OlatSite.HOME))
			return(false);
		
		if(!functionalHomeSiteUtil.openActionByMenuTree(browser, EPortfolioAction.MY_BINDERS, false))
			return(false);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//h4[text()='")
		.append(title)
		.append("']/..//a[contains(@class, '")
		.append(getOpenBinderCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @return true on success
	 * 
	 * Opens the editor of open binder.
	 */
	public boolean openEditor(Selenium browser){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getEditLinkCss())
		.append("')]//a");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param title
	 * @param description
	 * @return true on success
	 * 
	 * Creates a binder.
	 */
	public boolean createDefaultBinder(Selenium browser, String title, String description){
		if(!functionalUtil.openSite(browser, OlatSite.HOME))
			return(false);
		
		if(!functionalHomeSiteUtil.openActionByMenuTree(browser, EPortfolioAction.MY_BINDERS))
			return(false);
		
		/* open add binder dialog */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(getCreateBinderCss())
		.append("')]");
		
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("//a[contains(@class, '")
		.append(getCreateDefaultBinderCss())
		.append("')]");

		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
	
		/* fill in dialog - title */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[@id='")
		.append(getAddBinderBoxId())
		.append("']//form//input[@type='text']");
		
		browser.type(selectorBuffer.toString(), title);
		
		/* fill in dialog - description */
		functionalUtil.typeMCE(browser, description);
		
		
		/* fill in dialog - save */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[@id='")
		.append(getAddBinderBoxId())
		.append("']//form//button[last()]");
		
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param binder
	 * @param title
	 * @return
	 * 
	 * Checks if the specified page exists.
	 */
	public boolean pageExists(Selenium browser, String binder, String title){
		if(!openBinder(browser, binder))
			return(false);
			
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getEPortfolioTableOfContentsCss())
		.append("')]//ul//li[contains(@class, '")
		.append(getEPortfolioTOCLevel1Css())
		.append("')]//a//span[contains(text(), '")
		.append(title)
		.append("')]");
		
		if(browser.isElementPresent(selectorBuffer.toString())){
			return(true);
		}else{
			return(false);
		}
	}
	
	/**
	 * @param browser
	 * @param binder
	 * @param title
	 * @return
	 * 
	 * Opens the specified page.
	 */
	public boolean openPage(Selenium browser, String binder, String title){
		if(!openBinder(browser, binder))
			return(false);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getEPortfolioTableOfContentsCss())
		.append("')]//ul//li[contains(@class, '")
		.append(getEPortfolioTOCLevel1Css())
		.append("')]//a//span[contains(text(), '")
		.append(title)
		.append("')]/..");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param binder
	 * @param title
	 * @param display
	 * @param description
	 * @return true on success
	 * 
	 * Create a page in the specified binder.
	 */
	public boolean createPage(Selenium browser, String binder, String title, ArtefactDisplay display, String description){
		return(createPage(browser, binder, title, display, description, true));
	}
	
	public boolean createPage(Selenium browser, String binder, String title, ArtefactDisplay display, String description, boolean reopenBinder){
		if(reopenBinder){
			if(!openBinder(browser, binder))
				return(false);
		}
		
		/* click add */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(getAddLinkCss())
		.append("')]");
		
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		/* fill in wizard - title */
		if(title != null){
			selectorBuffer = new StringBuffer();

			selectorBuffer.append("xpath=//div[contains(@class, '")
			.append(getEPortfolioMapCss())
			.append("')]//form//input[@type='text']");

			browser.type(selectorBuffer.toString(), title);
		}
		
		/* fill in wizard - display */
		if(display != null){
			selectorBuffer = new StringBuffer();

			selectorBuffer.append("xpath=(//div[contains(@class, '")
			.append(getEPortfolioMapCss())
			.append("')]//form//input[@type='radio'])[")
			.append(display.ordinal() + 1)
			.append("]");

			browser.click(selectorBuffer.toString());
		}
		
		/* fill in wizard - description */
		functionalUtil.typeMCE(browser, description);
		
		/* fill in wizard - save */
		selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getEPortfolioMapCss())
		.append("')]//form//button[last()]");
		
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	public boolean renamePage(Selenium browser, String binder, String oldName, String newName){
		String selector = createSelector(binder, oldName, null);
		
		functionalUtil.waitForPageToLoadElement(browser, selector);
		browser.click(selector);
		
		/* rename */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getEPortfolioMapCss())
		.append("')]//form//input[@type='text']");
		
		browser.type(selectorBuffer.toString(), newName);
		
		/* save */
		selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=(//div[contains(@class, '")
		.append(getEPortfolioMapCss())
		.append("')]//form//button)[last()]");
		
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param binder
	 * @param page
	 * @param title
	 * @return true if structural element exists otherwise false
	 * 
	 * Checks if structural element exists.
	 */
	public boolean structureExists(Selenium browser, String binder, String page, String title){
		if(!openBinder(browser, binder))
			return(false);
		
		StringBuffer selectorBuffer = new StringBuffer();

		selectorBuffer.append("//div[contains(@class, '")
		.append(getEPortfolioTableOfContentsCss())
		.append("')]//ul//li");
		
		VelocityContext context = new VelocityContext();

		context.put("tocSelector", selectorBuffer.toString());
		context.put("level1", getEPortfolioTOCLevel1Css());
		context.put("level2", getEPortfolioTOCLevel2Css());
		context.put("page", page);
		context.put("structure", title);

		VelocityEngine engine = null;

		engine = new VelocityEngine();

		StringWriter sw = new StringWriter();

		try {
			engine.evaluate(context, sw, "eportfolioTOCStructurePosition", FunctionalEPortfolioUtil.class.getResourceAsStream("EPortfolioTOCStructurePosition.vm"));

			Integer i = new Integer(browser.getEval(sw.toString()));

			if(i.intValue() != -1){
				return(true);
			}else{
				return(false);
			}

		} catch (ParseErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MethodInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return(false);
	}
	
	/**
	 * @param browser
	 * @param binder
	 * @param page
	 * @param title
	 * @param description
	 * @return true on success
	 * 
	 * Creates the specified structural element.
	 */
	public boolean createStructure(Selenium browser, String binder, String page,
			String title, String description){
		if(!openBinder(browser, binder))
			return(false);
		
		/* open editor */
		openEditor(browser);
		
		/* select page and click create structure */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append("x-tree-node-anchor")
		.append("')]/span[text()='")
		.append(page)
		.append("']/..");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(getAddLinkCss())
		.append("') and contains(@class, '")
		.append(getStructIconCss())
		.append("')]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		/* fill in wizard - title */
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getEPortfolioMapCss())
		.append("')]//form//input[@type='text']");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.type(selectorBuffer.toString(), title);
				
		/* fill in wizard - description */
		functionalUtil.typeMCE(browser, description);
		
		/* fill in wizard - save */
		selectorBuffer = new StringBuffer();

		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getEPortfolioMapCss())
		.append("')]//form//button[last()]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoadElement(browser, createSelector(binder, page, title));
		
		return(true);
	}
	
	public boolean artefactExists(Selenium browser, String binder, String page, String structure, String artefact){
		if(!openPage(browser, binder, page)){
			return(false);
		}
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getEPortfolioMapCss())
		.append("')]");
		
		if(structure != null){
			selectorBuffer.append("//div[contains(@class, '")
			.append(getEPortfolioStructureCss())
			.append("')]//h5[text()='")
			.append(structure)
			.append("']/..");
		}
		
		selectorBuffer.append("//div[contains(@class, '")
		.append(getEPortfolioArtefactCss())
		.append("')]//h4[text()='")
		.append(artefact)
		.append("']");
		
		if(browser.isElementPresent(selectorBuffer.toString())){
			return(true);
		}else{
			return(false);
		}
	}
	
	public boolean openArtefact(Selenium browser, String binder, String page, String structure, String artefact){
		if(!openPage(browser, binder, page))
			return(false);
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(getEPortfolioMapCss())
		.append("')]");
		
		if(structure != null){
			selectorBuffer.append("//div[contains(@class, '")
			.append(getEPortfolioStructureCss())
			.append("')]//h5[text()='")
			.append(structure)
			.append("']/..");
		}
		
		selectorBuffer.append("//div[contains(@class, '")
		.append(getEPortfolioArtefactCss())
		.append("')]//h4[text()='")
		.append(artefact)
		.append("']/..//a[contains(@class, '")
		.append(getEPortfolioArtefactDetailsCss())
		.append("')]");

		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @return
	 * 
	 * Clicks the edit link.
	 */
	private boolean openEditLink(Selenium browser){
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(getHomePortalEditLinkCss())
		.append("')]");
		
		browser.click(selectorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param title
	 * @param description
	 * @return true on success
	 * 
	 * Fills in the open wizard's title and description fields. 
	 */
	protected boolean fillInTitleAndDescription(Selenium browser, String title, String description){
		StringBuffer locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//form//div[contains(@class, '")
		.append(functionalUtil.getWizardCss())
		.append("')]//input[@type='text']");
		
		functionalUtil.waitForPageToLoadElement(browser, locatorBuffer.toString());
		
		browser.type(locatorBuffer.toString(), title);
		
		functionalUtil.typeMCE(browser, description);
		
		functionalUtil.clickWizardNext(browser);
		
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param tags
	 * @return
	 * 
	 * Fills in the open wizard's tags.
	 */
	protected boolean fillInTags(Selenium browser, String[] tags){
		int i = 1;
		
		for(String tag: tags){
			StringBuffer locatorBuffer = new StringBuffer();
			
			locatorBuffer.append("xpath=(//form//div[contains(@class, '")
			.append(functionalUtil.getWizardCss())
			.append("')]//input[@type='text'])[" + i + "]");
			
			functionalUtil.waitForPageToLoadElement(browser, locatorBuffer.toString());
			
			browser.focus(locatorBuffer.toString());
			browser.type(locatorBuffer.toString(), tag);
			
			browser.focus(locatorBuffer.toString());
			browser.fireEvent(locatorBuffer.toString(), "changed");
			
			browser.keyDown(locatorBuffer.toString(), ",");
			browser.keyPress(locatorBuffer.toString(), ",");
			browser.keyUp(locatorBuffer.toString(), ",");
			browser.fireEvent(locatorBuffer.toString(), "changed");
			browser.fireEvent(locatorBuffer.toString(), "blur");
			
			i++;
		}
		
		functionalUtil.clickWizardNext(browser);
		
		return(true);
	}

	/**
	 * @param browser
	 * @param content
	 * @param title
	 * @param description
	 * @param tags
	 * @return
	 * 
	 * Add a text artefact to a e-portfolio.
	 */
	public boolean addTextArtefact(Selenium browser, String binder, String page, String structure,
			String content, String title, String description, String[] tags){
		/* create binder, page or structure if necessary */
		if(!createElements(browser, binder, page, structure))
			return(false);
		
		/* navigate to the right place */
		if(!functionalUtil.openSite(browser, OlatSite.HOME))
			return(false);
		
		if(!functionalHomeSiteUtil.openActionByMenuTree(browser, EPortfolioAction.MY_ARTIFACTS))
			return(false);
		
		/* open wizard */
		openEditLink(browser);
		functionalUtil.waitForPageToLoad(browser);
		
		StringBuffer locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//a[contains(@class, '")
		.append(getAddTextArtefactCss())
		.append("')]");
		
		browser.click(locatorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		
		/* fill in wizard - content */
		functionalUtil.typeMCE(browser, content);
		
		functionalUtil.clickWizardNext(browser);
		
		functionalUtil.waitForPageToLoad(browser);
		
		/* fill in wizard - title & description */
		fillInTitleAndDescription(browser, title, description);
		
		/* fill in wizard - tags */
		fillInTags(browser, tags);
		
		/* fill in wizard - select destination */
		String selector = createSelector(binder, page, structure);
		
		functionalUtil.waitForPageToLoadElement(browser, selector);
		
		browser.click(selector);
		
		/* click finish */
		functionalUtil.clickWizardFinish(browser);

		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param file
	 * @param title
	 * @param description
	 * @param tags
	 * @param binderPath
	 * @return
	 * @throws MalformedURLException
	 * 
	 * Upload a file artefact to a e-portfolio.
	 */
	public boolean uploadFileArtefact(Selenium browser, String binder, String page, String structure,
			URI file, String title, String description, String[] tags) throws MalformedURLException{
		if(!createElements(browser, binder, page, structure))
			return(false);
		
		if(!functionalUtil.openSite(browser, OlatSite.HOME))
			return(false);
		
		if(!functionalHomeSiteUtil.openActionByMenuTree(browser, EPortfolioAction.MY_ARTIFACTS))
			return(false);
		
		/* open wizard */
		openEditLink(browser);
		functionalUtil.waitForPageToLoad(browser);
		
		StringBuffer locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//a[contains(@class, '")
		.append(getUploadFileArtefactCss())
		.append("')]");
		
		browser.click(locatorBuffer.toString());
		functionalUtil.waitForPageToLoad(browser);
		
		/* fill in wizard - file */
		locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//form//div[contains(@class, '")
		.append(functionalUtil.getWizardCss())
		.append("')]//input[@type='file']");
		
		browser.focus(locatorBuffer.toString());
		browser.type(locatorBuffer.toString(), file.toURL().getPath());
		//browser.attachFile(locatorBuffer.toString(), file.toURL().toString());
		
		//TODO:JK: find a solution for IE
		/* IE may don't like the following script */
		//browser.runScript("$(\"form ." + functionalUtil.getWizardCss() + " input[type='file']\").trigger(\"change\")");

//		functionalUtil.waitForPageToLoad(browser);
		
		functionalUtil.clickWizardNext(browser);
		
		functionalUtil.waitForPageToLoad(browser);
		
		/* fill in wizard - title & description */
		fillInTitleAndDescription(browser, title, description);
		
		/* fill in wizard - tags */
		fillInTags(browser, tags);
		
		/* fill in wizard - select binder path */
		String selector = createSelector(binder, page, structure);
		
		functionalUtil.waitForPageToLoadElement(browser, selector);
		
		browser.click(selector);
		
		/* click finish */
		functionalUtil.clickWizardFinish(browser);
		
		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param title
	 * @param description
	 * @param tags
	 * @param binderPath
	 * @param create
	 * @return
	 * 
	 * Create a learnig journal for a e-portfolio.
	 */
	public boolean createLearningJournal(Selenium browser, String binder, String page, String structure,
			String title, String description, String[] tags){
		if(!createElements(browser, binder, page, structure))
			return(false);
		
		if(!functionalUtil.openSite(browser, OlatSite.HOME))
			return(false);
		
		if(!functionalHomeSiteUtil.openActionByMenuTree(browser, EPortfolioAction.MY_ARTIFACTS))
			return(false);
		
		/* open wizard */
		openEditLink(browser);
		functionalUtil.waitForPageToLoad(browser);
		
		StringBuffer locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//a[contains(@class, '")
		.append(getCreateLearningJournalCss())
		.append("')]");
		
		browser.click(locatorBuffer.toString());
		
		functionalUtil.waitForPageToLoad(browser);
		
		/* fill in wizard - title & description */
		fillInTitleAndDescription(browser, title, description);
		
		/* fill in wizard - tags */
		fillInTags(browser, tags);
		
		/* fill in wizard - select binder path */
		String selector = createSelector(binder, page, structure);
		
		functionalUtil.waitForPageToLoadElement(browser, selector);
		
		browser.click(selector);
		
		/* click finish */
		functionalUtil.clickWizardFinish(browser);

		functionalUtil.waitForPageToLoad(browser);
		
		return(true);
	}

	public FunctionalUtil getFunctionalUtil() {
		return functionalUtil;
	}

	public void setFunctionalUtil(FunctionalUtil functionalUtil) {
		this.functionalUtil = functionalUtil;
	}

	public FunctionalHomeSiteUtil getFunctionalHomeSiteUtil() {
		return functionalHomeSiteUtil;
	}

	public void setFunctionalHomeSiteUtil(
			FunctionalHomeSiteUtil functionalHomeSiteUtil) {
		this.functionalHomeSiteUtil = functionalHomeSiteUtil;
	}
	
	public String getEPortfolioCss() {
		return eportfolioCss;
	}

	public void setEPortfolioCss(String eportfolioCss) {
		this.eportfolioCss = eportfolioCss;
	}

	public String getEPortfolioMapCss() {
		return eportfolioMapCss;
	}
	
	public void setEPortfolioMapCss(String eportfolioMapCss) {
		this.eportfolioMapCss = eportfolioMapCss;
	}	

	public String getEPortfolioPageCss() {
		return eportfolioPageCss;
	}

	public void setEPortfolioPageCss(String eportfolioPageCss) {
		this.eportfolioPageCss = eportfolioPageCss;
	}

	public String getEPortfolioStructureCss() {
		return eportfolioStructureCss;
	}

	public void setEPortfolioStructureCss(String eportfolioStructureCss) {
		this.eportfolioStructureCss = eportfolioStructureCss;
	}

	public String getEPortfolioArtefactCss() {
		return eportfolioArtefactCss;
	}

	public void setEPortfolioArtefactCss(String eportfolioArtefactCss) {
		this.eportfolioArtefactCss = eportfolioArtefactCss;
	}

	public String getEPortfolioArtefactDetailsCss() {
		return eportfolioArtefactDetailsCss;
	}

	public void setEPortfolioArtefactDetailsCss(String eportfolioArtefactDetailsCss) {
		this.eportfolioArtefactDetailsCss = eportfolioArtefactDetailsCss;
	}

	public String getHomePortalEditLinkCss() {
		return homePortalEditLinkCss;
	}

	public void setHomePortalEditLinkCss(String homePortalEditLinkCss) {
		this.homePortalEditLinkCss = homePortalEditLinkCss;
	}
	
	public String getAddTextArtefactCss() {
		return addTextArtefactCss;
	}

	public void setAddTextArtefactCss(String addTextArtefactCss) {
		this.addTextArtefactCss = addTextArtefactCss;
	}

	public String getUploadFileArtefactCss() {
		return uploadFileArtefactCss;
	}

	public void setUploadFileArtefactCss(String uploadFileArtefactCss) {
		this.uploadFileArtefactCss = uploadFileArtefactCss;
	}

	public String getCreateLearningJournalCss() {
		return createLearningJournalCss;
	}

	public void setCreateLearningJournalCss(String createLearningJournalCss) {
		this.createLearningJournalCss = createLearningJournalCss;
	}

	public String getAddBinderBoxId() {
		return addBinderBoxId;
	}

	public void setAddBinderBoxId(String addBinderBoxId) {
		this.addBinderBoxId = addBinderBoxId;
	}

	public String getCreateBinderCss() {
		return createBinderCss;
	}

	public void setCreateBinderCss(String createBinderCss) {
		this.createBinderCss = createBinderCss;
	}

	public String getCreateDefaultBinderCss() {
		return createDefaultBinderCss;
	}

	public void setCreateDefaultBinderCss(String createDefaultBinderCss) {
		this.createDefaultBinderCss = createDefaultBinderCss;
	}

	public String getCreateTemplateBinderCss() {
		return createTemplateBinderCss;
	}

	public void setCreateTemplateBinderCss(String createTemplateBinderCss) {
		this.createTemplateBinderCss = createTemplateBinderCss;
	}

	public String getOpenBinderCss() {
		return openBinderCss;
	}

	public void setOpenBinderCss(String openBinderCss) {
		this.openBinderCss = openBinderCss;
	}

	public String getEditLinkCss() {
		return editLinkCss;
	}


	public void setEditLinkCss(String editLinkCss) {
		this.editLinkCss = editLinkCss;
	}


	public String getEPortfolioTableOfContentsCss() {
		return eportfolioTableOfContentsCss;
	}

	public void setEPortfolioTableOfContentsCss(String eportfolioTableOfContentsCss) {
		this.eportfolioTableOfContentsCss = eportfolioTableOfContentsCss;
	}

	public String getEPortfolioTOCLevel1Css() {
		return eportfolioTOCLevel1Css;
	}

	public void setEPortfolioTOCLevel1Css(String eportfolioTOCLevel1Css) {
		this.eportfolioTOCLevel1Css = eportfolioTOCLevel1Css;
	}

	public String getEPortfolioTOCLevel2Css() {
		return eportfolioTOCLevel2Css;
	}

	public void setEPortfolioTOCLevel2Css(String eportfolioTOCLevel2Css) {
		this.eportfolioTOCLevel2Css = eportfolioTOCLevel2Css;
	}

	public String getAddLinkCss() {
		return addLinkCss;
	}

	public void setAddLinkCss(String addLinkCss) {
		this.addLinkCss = addLinkCss;
	}

	public String getPageTabsCss() {
		return pageTabsCss;
	}

	public void setPageTabsCss(String pageTabsCss) {
		this.pageTabsCss = pageTabsCss;
	}


	public String getPageIconCss() {
		return pageIconCss;
	}


	public void setPageIconCss(String pageIconCss) {
		this.pageIconCss = pageIconCss;
	}


	public String getStructIconCss() {
		return structIconCss;
	}


	public void setStructIconCss(String structIconCss) {
		this.structIconCss = structIconCss;
	}

	public String getArtefactCss() {
		return artefactCss;
	}

	public void setArtefactCss(String artefactCss) {
		this.artefactCss = artefactCss;
	}

	public String getTagIconCss() {
		return tagIconCss;
	}

	public void setTagIconCss(String tagIconCss) {
		this.tagIconCss = tagIconCss;
	}
}
