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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.criteria.BadgeCondition;
import org.olat.modules.openbadges.criteria.BadgeCriteria;
import org.olat.modules.openbadges.criteria.BadgeCriteriaXStream;
import org.olat.modules.openbadges.v2.Profile;
import org.olat.repository.RepositoryEntry;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-07-13<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeDetailsOverviewController extends FormBasicController {

	public static final Event SHOW_RECIPIENTS_EVENT = new Event("show-recipients");
	
	private final Long badgeClassKey;
	private String mediaUrl;
	private String name;
	private String version;
	private SingleSelection versionSelectionEl;
	private FormLink courseEl;
	private StaticTextElement validityPeriodEl;
	private StaticTextElement issuerEl;
	private StaticTextElement languageEl;
	private StaticTextElement versionEl;
	private FormLink recipientsEl;
	private StaticTextElement issuedManuallyEl;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	public BadgeDetailsOverviewController(UserRequest ureq, WindowControl wControl, Long badgeClassKey) {
		super(ureq, wControl, "badge_details_overview");
		this.badgeClassKey = badgeClassKey;

		registerMapper(ureq);

		initForm(ureq);
	}

	void registerMapper(UserRequest ureq) {
		mediaUrl = registerMapper(ureq, new BadgeClassMediaFileMapper());
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		BadgeClass badgeClass = openBadgesManager.getBadgeClassByKey(badgeClassKey);

		SelectionValues sv = new SelectionValues();
		if (badgeClass.hasPreviousVersion()) {
			openBadgesManager.getBadgeClassVersions(badgeClass.getRootId())
					.forEach(bc -> sv.add(SelectionValues.entry(bc.getUuid(), versionString(bc))));
			versionSelectionEl = uifactory.addDropdownSingleselect("versionSelection", formLayout, sv.keys(), sv.values());
			versionSelectionEl.addActionListener(FormEvent.ONCHANGE);
			versionSelectionEl.select(badgeClass.getUuid(), true);
		}

		courseEl = uifactory.addFormLink("form.course", "goToCourse", "", 
				translate("form.course"), formLayout, Link.NONTRANSLATED);
		uifactory.addStaticTextElement("form.createdOn",
				Formatter.getInstance(getLocale()).formatDateAndTime(badgeClass.getCreationDate()), formLayout);
		validityPeriodEl = uifactory.addStaticTextElement("form.valid", "", formLayout);
		issuerEl = uifactory.addStaticTextElement("class.issuer", "", formLayout);
		languageEl = uifactory.addStaticTextElement("form.language", "", formLayout);
		versionEl = uifactory.addStaticTextElement("form.version", "", formLayout);
		versionEl.setVisible(badgeClass.getPreviousVersion() != null);
		recipientsEl = uifactory.addFormLink("form.recipients", "goToRecipients", "", 
				translate("form.recipients"), formLayout, Link.NONTRANSLATED);
		issuedManuallyEl = uifactory.addStaticTextElement("badge.issued.manually", null,
				translate("badge.issued.manually"), formLayout);

		loadData(false);
	}

	private String versionString(BadgeClass badgeClass) {
		return OpenBadgesUIFactory.versionString(getTranslator(), badgeClass, true, true);
	}

	void loadData(boolean updateVersionDropdown) {
		BadgeClass badgeClass;
		if (versionSelectionEl != null && versionSelectionEl.isVisible() && versionSelectionEl.getSelectedKey() != null) {
			if (updateVersionDropdown) {
				String selectedVersion = versionSelectionEl.getSelectedKey();
				badgeClass = openBadgesManager.getBadgeClassByKey(badgeClassKey);
				SelectionValues sv = new SelectionValues();
				openBadgesManager.getBadgeClassVersions(badgeClass.getRootId())
						.forEach(bc -> sv.add(SelectionValues.entry(bc.getUuid(), versionString(bc))));
				versionSelectionEl.setKeysAndValues(sv.keys(), sv.values(), sv.cssClasses());
				versionSelectionEl.select(selectedVersion, true);
			} else {
				badgeClass = openBadgesManager.getBadgeClassByUuid(versionSelectionEl.getSelectedKey());
			}
		} else {
			badgeClass = openBadgesManager.getBadgeClassByKey(badgeClassKey);
		}
		Long nbRecipients = openBadgesManager.getNumberOfBadgeAssertions(badgeClass.getKey());

		name = badgeClass.getNameWithScan();
		version = badgeClass.getVersion();

		flc.contextPut("img", mediaUrl + "/" + badgeClass.getImage());
		flc.contextPut("imgAlt", translate("badge.image") + ": " + badgeClass.getNameWithScan());
		flc.contextPut("badgeClass", badgeClass);
		flc.contextPut("isCourseBadge", badgeClass.getEntry() != null);

		RepositoryEntry courseEntry = badgeClass.getEntry();
		if (courseEntry != null) {
			ICourse course = CourseFactory.loadCourse(courseEntry);
			courseEl.setI18nKey(StringHelper.xssScan(course.getCourseTitle()));
			courseEl.setVisible(true);
		} else {
			courseEl.setVisible(false);
		}

		if (badgeClass.isValidityEnabled()) {
			String validityPeriod = badgeClass.getValidityTimelapse() + " " +
					translate("form.time." + badgeClass.getValidityTimelapseUnit().name());
			validityPeriodEl.setValue(validityPeriod);
			validityPeriodEl.setVisible(true);
		} else {
			validityPeriodEl.setVisible(false);
		}

		Profile issuer = new Profile(new JSONObject(badgeClass.getIssuer()));
		issuerEl.setValue(issuer.getNameWithScan());

		if (badgeClass.getLanguage() != null) {
			String languageDisplayName = Locale.forLanguageTag(badgeClass.getLanguage()).getDisplayName(getLocale());
			languageEl.setValue(languageDisplayName);
			languageEl.setVisible(true);
		} else {
			languageEl.setVisible(false);
		}

		versionEl.setValue(badgeClass.getVersionWithScan());
		
		if (nbRecipients > 0) {
			recipientsEl.setI18nKey(Long.toString(nbRecipients));
			recipientsEl.setVisible(true);
		} else {
			recipientsEl.setVisible(false);
		}

		BadgeCriteria badgeCriteria = BadgeCriteriaXStream.fromXml(badgeClass.getCriteria());
		flc.contextPut("criteriaDescription", badgeCriteria.getDescriptionWithScan());
		flc.contextPut("showConditions", badgeCriteria.isAwardAutomatically());

		List<BadgeCondition> badgeConditions = badgeCriteria.getConditions();
		List<Condition> conditions = new ArrayList<>();
		for (int i = 0; i < badgeConditions.size(); i++) {
			BadgeCondition badgeCondition = badgeConditions.get(i);
			Condition condition = new Condition(badgeCondition, i == 0, getTranslator(), badgeClass.getEntry());
			conditions.add(condition);
		}
		flc.contextPut("conditions", conditions);

		issuedManuallyEl.setVisible(!badgeCriteria.isAwardAutomatically());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == courseEl) {
			fireEvent(ureq, FormEvent.BACK_EVENT);
		} else if (source == versionSelectionEl) {
			loadData(false);
		} else if (source == recipientsEl) {
			fireEvent(ureq, SHOW_RECIPIENTS_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private class BadgeClassMediaFileMapper implements Mapper {

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			VFSLeaf classFileLeaf = openBadgesManager.getBadgeClassVfsLeaf(relPath);
			if (classFileLeaf != null) {
				return new VFSMediaResource(classFileLeaf);
			}
			return new NotFoundMediaResource();
		}
	}

	public record Condition(BadgeCondition badgeCondition, boolean first, Translator translator, RepositoryEntry courseEntry) {
		@Override
		public String toString() {
			return badgeCondition.toString(translator, courseEntry);
		}
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}
}
