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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.fileresource.DownloadeableMediaResource;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesFactory;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.criteria.BadgeCondition;
import org.olat.modules.openbadges.criteria.BadgeCriteria;
import org.olat.modules.openbadges.criteria.BadgeCriteriaXStream;
import org.olat.modules.openbadges.v2.Profile;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;

import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-06-08<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeAssertionPublicController extends FormBasicController {

	private final BadgeAssertion badgeAssertion;
	private final String mediaUrl;
	private final String downloadUrl;
	private final String fileName;

	@Autowired
	private OpenBadgesManager openBadgesManager;
	@Autowired
	private UserManager userManager;

	public BadgeAssertionPublicController(UserRequest ureq, WindowControl wControl, String uuid) {
		super(ureq, wControl, "assertion_web");

		mediaUrl = registerMapper(ureq, new BadgeAssertionMediaFileMapper());
		downloadUrl = registerMapper(ureq, new BadgeAssertionDownloadableMediaFileMapper());
		badgeAssertion = openBadgesManager.getBadgeAssertion(uuid);
		fileName = badgeAssertion.getDownloadFileName();

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("img", mediaUrl + "/" + badgeAssertion.getBakedImage());
		flc.contextPut("downloadUrl", downloadUrl + "/" + fileName);

		flc.contextPut("revokedBadge", badgeAssertion.getStatus() == BadgeAssertion.BadgeAssertionStatus.revoked);

		BadgeClass badgeClass = badgeAssertion.getBadgeClass();
		flc.contextPut("badgeClass", badgeClass);

		String recipientName = userManager.getUserDisplayName(badgeAssertion.getRecipient());
		flc.contextPut("recipientName", recipientName);

		Profile issuer = new Profile(new JSONObject(badgeClass.getIssuer()));
		flc.contextPut("issuer", issuer.getName());
		flc.contextPut("issuerUrl", issuer.getUrl());

		String issueDate = Formatter.getInstance(getLocale()).formatDateAndTime(badgeAssertion.getIssuedOn());
		flc.contextPut("issueDate", issueDate);

		BadgeCriteria badgeCriteria = BadgeCriteriaXStream.fromXml(badgeClass.getCriteria());

		String createdOn = Formatter.getInstance(getLocale()).formatDateAndTime(badgeClass.getCreationDate());
		uifactory.addStaticTextElement("form.createdOn", createdOn, formLayout);

		if (badgeClass.isValidityEnabled()) {
			String validityPeriod = badgeClass.getValidityTimelapse() + " " +
					translate("form.time." + badgeClass.getValidityTimelapseUnit().name());
			uifactory.addStaticTextElement("form.valid", validityPeriod, formLayout);
		}

		if (badgeClass.getLanguage() != null) {
			String languageDisplayName = Locale.forLanguageTag(badgeClass.getLanguage()).getDisplayName(getLocale());
			uifactory.addStaticTextElement("form.language", languageDisplayName, formLayout);
		}

		if (badgeAssertion.getAwardedBy() != null) {
			Identity awardedByIdentity = badgeAssertion.getAwardedBy();
			String awardedBy = userManager.getUserDisplayName(awardedByIdentity);
			uifactory.addStaticTextElement("form.awarded.by", awardedBy, formLayout);

			if (awardedByIdentity.getUser() != null && awardedByIdentity.getUser().getEmail() != null) {
				String awardedContact = awardedByIdentity.getUser().getEmail();
				uifactory.addStaticTextElement("form.contact", awardedContact, formLayout);
			}
		}

		uifactory.addStaticTextElement("form.version", badgeClass.getVersion(), formLayout);

		uifactory.addStaticTextElement("form.issued.on",
				Formatter.getInstance(getLocale()).formatDateAndTime(badgeAssertion.getIssuedOn()), formLayout);

		flc.contextPut("criteriaDescription", badgeCriteria.getDescription());

		flc.contextPut("showConditions", badgeCriteria.isAwardAutomatically());
		List<BadgeCondition> badgeConditions = badgeCriteria.getConditions();
		List<Condition> conditions = new ArrayList<>();
		for (int i = 0; i < badgeConditions.size(); i++) {
			BadgeCondition badgeCondition = badgeConditions.get(i);
			Condition condition = new Condition(badgeCondition, i == 0, getTranslator());
			conditions.add(condition);
		}
		flc.contextPut("conditions", conditions);

		if (!badgeCriteria.isAwardAutomatically()) {
			uifactory.addStaticTextElement("badge.issued.manually", null,
					translate("badge.issued.manually"), formLayout);
		}

		RepositoryEntry courseEntry = badgeClass.getEntry();
		if (courseEntry != null) {
			ICourse course = CourseFactory.loadCourse(courseEntry);
			uifactory.addStaticTextElement("form.course", course.getCourseTitle(), formLayout);
		}

		flc.contextPut("fileName", "badge_" + badgeAssertion.getBakedImage());

		flc.contextPut("publicLink", OpenBadgesFactory.createAssertionPublicUrl(badgeAssertion.getUuid()));
	}

	private File createTemporaryFile() {
		File temporaryFile = new File(WebappHelper.getTmpDir(), fileName);
		if (temporaryFile.exists()) {
			return temporaryFile;
		}

		VFSLeaf assertionLeaf = openBadgesManager.getBadgeAssertionVfsLeaf(badgeAssertion.getBakedImage());
		if (assertionLeaf != null && assertionLeaf instanceof LocalFileImpl localFile) {
			FileUtils.copyFileToFile(localFile.getBasefile(), temporaryFile, false);
			return temporaryFile;
		}
		return null;
	}

	@Override
	protected void doDispose() {
		File temporaryFile = new File(WebappHelper.getTmpDir(), fileName);
		if (temporaryFile.exists()) {
			temporaryFile.delete();
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private class BadgeAssertionMediaFileMapper implements Mapper {

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			VFSLeaf assertionLeaf = openBadgesManager.getBadgeAssertionVfsLeaf(relPath);
			if (assertionLeaf != null) {
				return new VFSMediaResource(assertionLeaf);
			}
			return new NotFoundMediaResource();
		}
	}

	private class BadgeAssertionDownloadableMediaFileMapper implements Mapper {

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			File temporaryFile = createTemporaryFile();
			if (temporaryFile != null) {
				return new DownloadeableMediaResource(temporaryFile);
			}
			return new NotFoundMediaResource();
		}
	}

	public record Condition(BadgeCondition badgeCondition, boolean first, Translator translator) {
		@Override
		public String toString() {
			return badgeCondition.toString(translator);
		}
	}
}
