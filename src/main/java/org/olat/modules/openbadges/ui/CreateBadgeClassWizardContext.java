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
package org.olat.modules.openbadges.ui;

import java.util.Set;

import org.olat.core.helpers.Settings;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.criteria.BadgeCriteria;
import org.olat.modules.openbadges.criteria.BadgeCriteriaXStream;
import org.olat.modules.openbadges.model.BadgeClassImpl;
import org.olat.modules.openbadges.v2.Constants;
import org.olat.modules.openbadges.v2.Profile;
import org.olat.repository.RepositoryEntry;

import org.json.JSONObject;

/**
 * Initial date: 2023-06-19<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadgeClassWizardContext {
	public enum Mode {
		create, edit
	}

	public static final String KEY = "createBadgeClassWizardContext";

	private final BadgeClass badgeClass;
	private final Long courseResourcableId;
	private Long selectedTemplateKey;
	private String selectedTemplateImage;
	private Set<String> templateVariables;
	private String backgroundColorId;
	private String title;
	private BadgeCriteria badgeCriteria;
	private Profile issuer;
	private Mode mode;

	public CreateBadgeClassWizardContext(RepositoryEntry entry) {
		mode = Mode.create;
		ICourse course = entry != null ? CourseFactory.loadCourse(entry) : null;
		courseResourcableId = course != null ? course.getResourceableId() : null;
		BadgeClassImpl badgeClassImpl = new BadgeClassImpl();
		badgeClassImpl.setUuid(OpenBadgesUIFactory.createIdentifier());
		badgeClassImpl.setStatus(BadgeClass.BadgeClassStatus.preparation);
		badgeClassImpl.setSalt("badgeClass" + Math.abs(badgeClassImpl.getUuid().hashCode()));
		Profile issuer = new Profile(new JSONObject());
		if (course != null) {
			String url = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + entry.getKey();
			issuer.setName(course.getCourseTitle());
			issuer.setUrl(url);
		} else {
			issuer.setName(Settings.getApplicationName());
			issuer.setUrl(Settings.getServerContextPathURI());
		}
		badgeClassImpl.setIssuer(issuer.asJsonObject(Constants.TYPE_VALUE_ISSUER).toString());
		badgeClassImpl.setVersion("1.0");
		badgeClassImpl.setLanguage("en");
		badgeClassImpl.setValidityEnabled(false);
		badgeClassImpl.setEntry(entry);
		backgroundColorId = "gold";
		title = course != null ? course.getCourseTitle() : Settings.getApplicationName();
		initCriteria();
		issuer = new Profile(badgeClassImpl);
		badgeClass = badgeClassImpl;
	}

	public CreateBadgeClassWizardContext(BadgeClass badgeClass) {
		mode = Mode.edit;
		ICourse course = badgeClass.getEntry() != null ? CourseFactory.loadCourse(badgeClass.getEntry()) : null;
		courseResourcableId = course != null ? course.getResourceableId() : null;
		backgroundColorId = null;
		title = null;
		badgeCriteria = BadgeCriteriaXStream.fromXml(badgeClass.getCriteria());
		this.badgeClass = badgeClass;
		issuer = new Profile(badgeClass);
	}

	private void initCriteria() {
		badgeCriteria = new BadgeCriteria();
		badgeCriteria.setAwardAutomatically(false);
	}

	public BadgeClass getBadgeClass() {
		return badgeClass;
	}

	public Long getSelectedTemplateKey() {
		return selectedTemplateKey;
	}

	public void setSelectedTemplateKey(Long selectedTemplateKey) {
		this.selectedTemplateKey = selectedTemplateKey;
	}

	public String getSelectedTemplateImage() {
		return selectedTemplateImage;
	}

	public void setSelectedTemplateImage(String selectedTemplateImage) {
		this.selectedTemplateImage = selectedTemplateImage;
	}

	public Long getCourseResourcableId() {
		return courseResourcableId;
	}

	public String getBackgroundColorId() {
		return backgroundColorId;
	}

	public void setBackgroundColorId(String backgroundColorId) {
		this.backgroundColorId = backgroundColorId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public BadgeCriteria getBadgeCriteria() {
		return badgeCriteria;
	}

	public boolean needsCustomization() {
		if (!StringHelper.containsNonWhitespace(selectedTemplateImage)) {
			return false;
		}
		String suffix = FileUtils.getFileSuffix(selectedTemplateImage);
		if (!suffix.equalsIgnoreCase("svg")) {
			return false;
		}

		if (getTemplateVariables() == null || getTemplateVariables().isEmpty()) {
			return false;
		}

		return true;
	}

	public Set<String> getTemplateVariables() {
		return templateVariables;
	}

	public void setTemplateVariables(Set<String> templateVariables) {
		this.templateVariables = templateVariables;
	}

	public Mode getMode() {
		return mode;
	}
}
