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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;

import org.olat.util.FunctionalHomeSiteUtil.EPortfolioAction;
import org.olat.util.FunctionalHomeSiteUtil.HomeSiteAction;
import org.olat.util.FunctionalUtil.OlatSite;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class FunctionalEportfolioUtil {
	
	public final static String HOME_PORTAL_EDIT_LINK_CSS = "o_sel_add_artfeact";
	public final static String ADD_TEXT_ARTEFACT_CSS = "o_sel_add_text_artfeact";
	public final static String UPLOAD_FILE_ARTEFACT_CSS = "o_sel_add_upload_artfeact";
	public final static String CREATE_LEARNING_JOURNAL_CSS = "o_sel_add_liveblog_artfeact";
	
	public final static String CREATE_FOLDER_CSS = "b_ep_add_artefact";
	
	private String homePortalEditLinkCss;
	private String addTextArtefactCss;
	private String uploadFileArtefactCss;
	private String createLearningJournalCss;
	
	private String createFolderCss;
	
	private FunctionalUtil functionalUtil;
	private FunctionalHomeSiteUtil functionalHomeSiteUtil;
	
	public FunctionalEportfolioUtil(FunctionalUtil functionalUtil, FunctionalHomeSiteUtil functionalHomeSiteUtil){
		this.functionalUtil = functionalUtil;
		this.functionalHomeSiteUtil = functionalHomeSiteUtil;
		
		setHomePortalEditLinkCss(HOME_PORTAL_EDIT_LINK_CSS);
		setAddTextArtefactCss(ADD_TEXT_ARTEFACT_CSS);
		setUploadFileArtefactCss(UPLOAD_FILE_ARTEFACT_CSS);
		setCreateLearningJournalCss(CREATE_LEARNING_JOURNAL_CSS);
		
		setCreateFolderCss(CREATE_FOLDER_CSS);
	}

	/**
	 * @param binderPath
	 * @return
	 * 
	 * Creates an xpath expression of a slash separated path to
	 * select a tree item.
	 */
	public String createSelectorOfBinderPath(String binderPath){
		if(binderPath == null)
			return(null);
		
		if(!binderPath.startsWith("/")){
			binderPath = "/" + binderPath;
		}
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=");
		
		int prevSeparator = 0;
		int currentSeparator = -1;
		
		while(prevSeparator != binderPath.length() && (currentSeparator = binderPath.indexOf('/', prevSeparator + 1)) != -1){
			selectorBuffer.append("//ul//li//a//span[text()=")
			.append(binderPath.substring(prevSeparator + 1, currentSeparator))
			.append("]");
			
			
			prevSeparator = currentSeparator;
		}
		
		if(prevSeparator > 0){
			selectorBuffer.append("/..");
				
			return(selectorBuffer.toString());
		}else{
			return(null);
		}
	}
	
	/**
	 * @param browser
	 * @param binderPath
	 * @return the xpath selector
	 * 
	 * Create a binder of given path.
	 */
	public String createBinderPath(Selenium browser, String binderPath){
		if(binderPath == null)
			return(null);
		
		if(!binderPath.startsWith("/")){
			binderPath = "/" + binderPath;
		}
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=");
		
		int prevSeparator = 0;
		int currentSeparator = -1;
		
		while(prevSeparator != binderPath.length() && (currentSeparator = binderPath.indexOf('/', prevSeparator + 1)) != -1){
			String current = binderPath.substring(prevSeparator + 1, currentSeparator);
			
			selectorBuffer.append("//ul//li//a//span[text()=")
			.append(current)
			.append("]");
			
			
			
			
			
			
			prevSeparator = currentSeparator;
		}
		
		if(prevSeparator > 0){
			selectorBuffer.append("/..");
				
			return(selectorBuffer.toString());
		}else{
			return(null);
		}
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
		
		browser.waitForPageToLoad(functionalUtil.getWaitLimit());
		
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
	private boolean fillInTitleAndDescription(Selenium browser, String title, String description){
		StringBuffer locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//form//div[contains(@class, '")
		.append(functionalUtil.getWizardCss())
		.append("')]//input[@type='text']");
		
		browser.type(locatorBuffer.toString(), title);
		
		functionalUtil.typeMCE(browser, description);

		functionalUtil.clickWizardNext(browser);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param tags
	 * @return
	 * 
	 * Fills in the open wizard's tags.
	 */
	private boolean fillInTags(Selenium browser, String tags){
		StringBuffer locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//form//div[contains(@class, '")
		.append(functionalUtil.getWizardCss())
		.append("')]//input[@type='text']");
		
		browser.type(locatorBuffer.toString(), tags);
		
		functionalUtil.clickWizardNext(browser);
		
		return(true);
	}

	/**
	 * @param browser
	 * @param content
	 * @param title
	 * @param description
	 * @param tags
	 * @param binderPath
	 * @return
	 * 
	 * Add a text artefact to a e-portfolio.
	 */
	public boolean addTextArtefact(Selenium browser, String content, String title, String description, String tags, String binderPath){
		if(!functionalUtil.openSite(browser, OlatSite.HOME))
			return(false);
		
		if(!functionalHomeSiteUtil.openActionByMenuTree(browser, EPortfolioAction.MY_ARTIFACTS))
			return(false);
		
		/* open wizard */
		openEditLink(browser);
		
		StringBuffer locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//a[contains(@class, '")
		.append(getAddTextArtefactCss())
		.append("')]");
		
		browser.click(locatorBuffer.toString());
		
		/* fill in wizard - content */
		functionalUtil.typeMCE(browser, description);
		
		functionalUtil.clickWizardNext(browser);
		
		/* fill in wizard - title & description */
		fillInTitleAndDescription(browser, title, description);
		
		/* fill in wizard - tags */
		fillInTags(browser, tags);
		
		/* fill in wizard - select binder path */
		browser.click(createSelectorOfBinderPath(binderPath));
		
		/* click finish */
		functionalUtil.clickWizardFinish(browser);
		
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
	public boolean uploadFileArtefact(Selenium browser, URI file, String title, String description, String tags, String binderPath) throws MalformedURLException{
		if(!functionalUtil.openSite(browser, OlatSite.HOME))
			return(false);
		
		if(!functionalHomeSiteUtil.openActionByMenuTree(browser, EPortfolioAction.MY_ARTIFACTS))
			return(false);
		
		/* open wizard */
		openEditLink(browser);
		
		StringBuffer locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//a[contains(@class, '")
		.append(getUploadFileArtefactCss())
		.append("')]");
		
		browser.click(locatorBuffer.toString());
		
		/* fill in wizard - file */
		locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//form//div[contains(@class, '")
		.append(functionalUtil.getWizardCss())
		.append("')]//input[@type='file']");
		
		browser.attachFile(locatorBuffer.toString(), file.toURL().toString());
		
		functionalUtil.clickWizardNext(browser);
		
		/* fill in wizard - title & description */
		fillInTitleAndDescription(browser, title, description);
		
		/* fill in wizard - tags */
		fillInTags(browser, tags);
		
		/* fill in wizard - select binder path */
		browser.click(createSelectorOfBinderPath(binderPath));
		
		/* click finish */
		functionalUtil.clickWizardFinish(browser);
		
		return(true);
	}
	
	/**
	 * @param browser
	 * @param title
	 * @param description
	 * @param tags
	 * @param binderPath
	 * @return
	 * 
	 * Create a learnig journal for a e-portfolio.
	 */
	public boolean createLearningJournal(Selenium browser, String title, String description, String tags, String binderPath){
		return(createLearningJournal(browser, title, description, tags, binderPath, true));
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
	public boolean createLearningJournal(Selenium browser, String title, String description, String tags, String binderPath, boolean create){
		if(!functionalUtil.openSite(browser, OlatSite.HOME))
			return(false);
		
		if(!functionalHomeSiteUtil.openActionByMenuTree(browser, EPortfolioAction.MY_ARTIFACTS))
			return(false);
		
		/* open wizard */
		openEditLink(browser);
		
		StringBuffer locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("xpath=//a[contains(@class, '")
		.append(getCreateLearningJournalCss())
		.append("')]");
		
		browser.click(locatorBuffer.toString());
		
		browser.waitForPageToLoad(functionalUtil.getWaitLimit());
		
		/* fill in wizard - title & description */
		fillInTitleAndDescription(browser, title, description);
		
		/* fill in wizard - tags */
		fillInTags(browser, tags);
		
		/* fill in wizard - select binder path */
		String selector = createSelectorOfBinderPath(binderPath);
		
		if(selector == null){
			selector = createBinderPath(browser, binderPath);
			
			browser.click(selector);
		}else{
			browser.click(selector);
		}
		
		/* click finish */
		functionalUtil.clickWizardFinish(browser);
		
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

	public String getCreateFolderCss() {
		return createFolderCss;
	}

	public void setCreateFolderCss(String createFolderCss) {
		this.createFolderCss = createFolderCss;
	}
}
