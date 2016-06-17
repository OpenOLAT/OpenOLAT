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
package org.olat.modules.portfolio.model;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.link.Link;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.Section;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageRow {
	
	private final Page page;
	private final Section section;
	private final boolean firstPageOfSection;
	
	private Link openLink;
	private FormLink openFormLink;
	
	public PageRow(Page page, Section section, boolean firstPageOfSection) {
		this.page = page;
		this.section = section;
		this.firstPageOfSection = firstPageOfSection;
	}
	
	public Long getKey() {
		return page.getKey();
	}
	
	public Page getPage() {
		return page;
	}
	
	public String getTitle() {
		return page.getTitle();
	}
	
	public String getSummary() {
		return page.getSummary();
	}
	
	public Date getLastModified() {
		return page.getLastModified();
	}
	
	public String getSectionTitle() {
		return section.getTitle();
	}
	public boolean isSection () {
		if(section == null){
			return false;
		}
		return true;
		
	}
	
	public String getSectionLongTitle() {
		
		long pos = section.getPos();
		return (pos+1) + ". " + section.getTitle();
	}
	
	public Date getSectionBeginDate() {
		return section.getBeginDate();
	}
	
	public Date getSectionEndDate() {
		return section.getEndDate();
	}
	
	public String getSectionDescription() {
		return section.getDescription();
	}

	public boolean isFirstPageOfSection() {
		return firstPageOfSection;
	}
	
	public FormLink getOpenFormItem() {
		return openFormLink;
	}
	
	public String getOpenFormItemName() {
		return openFormLink == null ? null : openFormLink.getComponent().getComponentName();
	}

	public void setOpenFormLink(FormLink openFormLink) {
		this.openFormLink = openFormLink;
	}
	
	public String getOpenLinkName() {
		return openLink == null ? null : openLink.getComponentName();
	}
	
	public void setOpenLink(Link openLink) {
		this.openLink = openLink;
	}


}
