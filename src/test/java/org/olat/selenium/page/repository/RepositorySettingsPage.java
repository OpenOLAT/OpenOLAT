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
package org.olat.selenium.page.repository;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 6 Nov 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositorySettingsPage {
	
	protected final WebDriver browser;
	
	public RepositorySettingsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public RepositoryEditDescriptionPage assertOnInfos() {
		By infosBy = By.cssSelector("fieldset.o_sel_edit_repositoryentry");
		OOGraphene.waitElement(infosBy, browser);
		OOGraphene.waitTinymce(browser);
		return new RepositoryEditDescriptionPage(browser);
	}
	
	public RepositoryMetadataPage metadata() {
		By accessSegmentBy = By.cssSelector("ul.o_tools_segments a.o_sel_metadata");
		OOGraphene.waitElement(accessSegmentBy, browser);
		browser.findElement(accessSegmentBy).click();
		
		By accessConfigurationBy = By.cssSelector("fieldset.o_sel_repo_metadata");
		OOGraphene.waitElement(accessConfigurationBy, browser);
		return new RepositoryMetadataPage(browser);
	}
	
	public RepositoryExecutionSettingsPage execution() {
		By executionSegmentBy = By.cssSelector("ul.o_tools_segments a.o_sel_execution");
		OOGraphene.waitElement(executionSegmentBy, browser);
		browser.findElement(executionSegmentBy).click();
		
		By executionBy = By.cssSelector("div.o_sel_repo_execution");
		OOGraphene.waitElement(executionBy, browser);
		
		return new RepositoryExecutionSettingsPage(browser);
	}
	
	public RepositoryAccessPage accessConfiguration() {
		By accessSegmentBy = By.cssSelector("ul.o_tools_segments a.o_sel_access");
		OOGraphene.waitElement(accessSegmentBy, browser);
		browser.findElement(accessSegmentBy).click();
		
		By accessConfigurationBy = By.cssSelector("fieldset.o_sel_repo_access_configuration");
		OOGraphene.waitElement(accessConfigurationBy, browser);
		return new RepositoryAccessPage(browser);
	}
	
	public RepositoryCertificateSettingsPage certificates() {
		By accessSegmentBy = By.cssSelector("ul.o_tools_segments a.o_sel_assessment");
		OOGraphene.waitElement(accessSegmentBy, browser);
		browser.findElement(accessSegmentBy).click();
		
		By certificatesConfigurationBy = By.cssSelector("fieldset.o_sel_certificate_settings");
		OOGraphene.waitElement(certificatesConfigurationBy, browser);
		return new RepositoryCertificateSettingsPage(browser);
	}
	
	public void back() {
		OOGraphene.clickBreadcrumbBack(browser);
	}
}
