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
package org.olat.modules.selectus.model.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;

import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.MailLogInfos;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;

/**
 * 
 * Initial date: 22.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EmailVariables {
	
	private Position position;
	private boolean showDecisions;
	private boolean showAttachmentWarning;
	private Collection<String> categoriesToAdd;
	private List<ApplicationLight> rows;
	private List<MailLogInfos> mailLog;
	private List<UserRating> ratings;
	private List<IdentityRef> committee;
	private Date applicationStatusDate;
	private ApplicationStatus applicationStatus;
	private String applicationStatusComment;
	private Identity secretary;
	private Identity headOfCommittee;
	private List<ApplicationLight> selectedApps;
	
	private List<String> applicationsGroups;
	
	private String templateName;
	private final List<ApplicationMailTemplate> templates = new ArrayList<>();
	
	private final Locale locale;
	
	public EmailVariables(Locale locale) {
		this.locale = locale;
	}
	
	public Position getPosition() {
		return position;
	}
	
	public void setPosition(Position position) {
		this.position = position;
	}
	
	public boolean isShowDecisions() {
		return showDecisions;
	}

	public void setShowDecisions(boolean showDecisions) {
		this.showDecisions = showDecisions;
	}

	public boolean isShowAttachmentWarning() {
		boolean hasLetter = hasSelectedTemplateLetter();
		return showAttachmentWarning && hasLetter;
	}
	
	public boolean hasSelectedTemplateLetter() {
		List<ApplicationMailTemplate> namedTemplates = getTemplates(templateName);
		for(ApplicationMailTemplate namedTemplate:namedTemplates) {
			if(namedTemplate.getLetterTemplate() != null) {
				return true;
			}
		}
		return false;
	}

	public void setShowAttachmentWarning(boolean showAttachmentWarning) {
		this.showAttachmentWarning = showAttachmentWarning;
	}

	public List<ApplicationLight> getRows() {
		return rows;
	}
	
	public void setRows(List<ApplicationLight> rows) {
		this.rows = rows;
	}

	public List<MailLogInfos> getMailLog() {
		return mailLog;
	}

	public void setMailLog(List<MailLogInfos> mailLog) {
		this.mailLog = mailLog;
	}

	public Collection<String> getCategoriesToAdd() {
		return categoriesToAdd;
	}

	public void setCategoriesToAdd(Collection<String> categories) {
		this.categoriesToAdd = categories;
	}

	public Date getApplicationStatusDate() {
		return applicationStatusDate;
	}

	public void setApplicationStatusDate(Date applicationStatusDate) {
		this.applicationStatusDate = applicationStatusDate;
	}

	public ApplicationStatus getApplicationStatus() {
		return applicationStatus;
	}

	public void setApplicationStatus(ApplicationStatus applicationStatus) {
		this.applicationStatus = applicationStatus;
	}

	public String getApplicationStatusComment() {
		return applicationStatusComment;
	}

	public void setApplicationStatusComment(String applicationStatusComment) {
		this.applicationStatusComment = applicationStatusComment;
	}

	public List<UserRating> getRatings() {
		return ratings;
	}
	
	public void setRatings(List<UserRating> ratings) {
		this.ratings = ratings;
	}
	
	public List<IdentityRef> getCommittee() {
		return committee;
	}
	
	public void setCommittee(List<IdentityRef> committee) {
		this.committee = committee;
	}

	public Identity getSecretary() {
		return secretary;
	}

	public void setSecretary(Identity secretary) {
		this.secretary = secretary;
	}

	public Identity getHeadOfCommittee() {
		return headOfCommittee;
	}

	public void setHeadOfCommittee(Identity headOfCommittee) {
		this.headOfCommittee = headOfCommittee;
	}

	public List<ApplicationLight> getSelectedApps() {
		return selectedApps;
	}

	public void setSelectedApps(List<ApplicationLight> selectedApps) {
		this.selectedApps = selectedApps;
	}

	public List<String> getApplicationsGroups() {
		return applicationsGroups;
	}

	public void setApplicationsGroups(List<String> applicationsGroups) {
		this.applicationsGroups = applicationsGroups;
	}

	public String getTemplateName() {
		return templateName == null ? ApplicationMailTemplate.DEFAULT_TEMPLATE : templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String[] getTemplateNames() {
		List<String> names = new ArrayList<>();
		for(ApplicationMailTemplate template:templates) {
			if(!names.contains(template.getName())) {
				names.add(template.getName());
			}
		}
		return names.toArray(new String[names.size()]);
	}
	
	public SelectionValues getTemplatesKeyValues() {
		SelectionValues keyValues = new SelectionValues();
		Set<String> names = new HashSet<>();
		for(ApplicationMailTemplate template:templates) {
			if(names.contains(template.getName())) {
				continue;
			}
			String label = template.getLabel();
			if(template.getLetterTemplate() != null) {
				Translator translator = Util.createPackageTranslator(PositionController.class, locale);
				label = translator.translate("mailtemplateform.templates.w.letter", label);
			}
			keyValues.add(SelectionValues.entry(template.getName(), label));
			names.add(template.getName());
		}
		return keyValues;
	}

	public List<ApplicationMailTemplate> getTemplates() {
		return templates;
	}

	public ApplicationMailTemplate getTemplate(String name, Locale locale) {
		ApplicationMailTemplate localizedTemplate = null;
		for(ApplicationMailTemplate template:templates) {
			if(template.getLocale().equals(locale) && template.getName().equals(name)) {
				localizedTemplate = template;
			}
		}
		return localizedTemplate;
	}
	
	public List<ApplicationMailTemplate> getTemplates(String name) {
		List<ApplicationMailTemplate> namedTemplates = new ArrayList<>();
		for(ApplicationMailTemplate template:templates) {
			if(template.getName().equals(name)) {
				namedTemplates.add(template);
			}
		}
		return namedTemplates;
	}

	public void addTemplate(ApplicationMailTemplate template) {
		templates.add(template);
	}
}