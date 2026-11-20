/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.member;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.ui.CurriculumHelper;

/**
 * 
 * Initial date: 19 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumMemberImplementationRow {
	
	private final Curriculum curriculum;
	private final CurriculumElement element;
	private final CurriculumElementType type;

	private final List<CurriculumRoles> allRoles = new ArrayList<>(2);
	private final List<CurriculumRoles> implementationRoles = new ArrayList<>(2);
	
	private FormLink structureLink;
	private FormLink calendarsLink;
	private ExternalLinkItem openLink;
	
	private final String curriculumUrl;
	private final String implementationUrl;
	
	public CurriculumMemberImplementationRow(CurriculumElement element) {
		this.element = element;
		this.curriculum = element.getCurriculum();
		this.type = element.getType();
		
		List<ContextEntry> cEntries = BusinessControlFactory.getInstance()
				.createCEListFromString(CurriculumHelper.getCurriculumBusinessPath(curriculum.getKey()));
		curriculumUrl = BusinessControlFactory.getInstance().getAsAuthURIString(cEntries, true);
		
		List<ContextEntry> iEntries = BusinessControlFactory.getInstance()
				.createCEListFromString(CurriculumHelper.getImplementationBusinessPath(element.getKey()));
		implementationUrl = BusinessControlFactory.getInstance().getAsAuthURIString(iEntries, true);
	}
	
	public CurriculumElement getCurriculumElement() {
		return element;
	}
	
	public Date getBeginDate() {
		return element.getBeginDate();
	}
	
	public Date getEndDate() {
		return element.getEndDate();
	}
	
	public CurriculumElementStatus getStatus() {
		return element.getElementStatus() == null
				? CurriculumElementStatus.preparation
				: element.getElementStatus();
	}
	
	public String getImplementationUrl() {
		return implementationUrl;
	}

	public Curriculum getCurriculum() {
		return curriculum;
	}
	
	public String getCurriculumBusinessPath() {
		return curriculumUrl;
	}

	public String getCurriculumElementTypeName() {
		return type == null ? null : type.getDisplayName();
	}
	
	public CurriculumElementType getCurriculumElementType() {
		return type;
	}
	
	public boolean isCalendarsEnabled() {
		boolean enabled = false;
		if(element != null) {
			if(element.getCalendars() == CurriculumCalendars.enabled) {
				enabled = true;
			} else if(element.getCalendars() == CurriculumCalendars.inherited && type != null) {
				enabled = type.getCalendars() == CurriculumCalendars.enabled;
			}
		}
		return enabled;
	}

	public List<CurriculumRoles> getAllRoles() {
		return allRoles;
	}
	
	public void addToAllRoles(CurriculumRoles role) {
		if(!allRoles.contains(role)) {
			allRoles.add(role);
		}
	}

	public List<CurriculumRoles> getImplementationRoles() {
		return implementationRoles;
	}
	
	public void addToImplementationRoles(CurriculumRoles role) {
		if(!implementationRoles.contains(role)) {
			implementationRoles.add(role);
		}
	}

	public FormLink getStructureLink() {
		return structureLink;
	}

	public void setStructureLink(FormLink structureLink) {
		this.structureLink = structureLink;
	}
	
	public FormLink getCalendarsLink() {
		return calendarsLink;
	}

	public void setCalendarsLink(FormLink calendarsLink) {
		this.calendarsLink = calendarsLink;
	}

	public ExternalLinkItem getOpenLink() {
		return openLink;
	}

	public void setOpenLink(ExternalLinkItem openLink) {
		this.openLink = openLink;
	}
}
