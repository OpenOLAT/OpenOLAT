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
package org.olat.modules.openbadges;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.openbadges.manager.BadgeClassDAO;
import org.olat.modules.openbadges.model.BadgeClassImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * Initial date: 2023-05-08<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface OpenBadgesManager {
	String VAR_TITLE = "$title";
	String VAR_BACKGROUND = "$background";

	//
	// Template
	//

	void createFactoryBadgeTemplates();

	BadgeTemplate createTemplate(String identifier, String name, File templateFile, String targetFileName,
								 String description, Collection<String> scopes, Identity savedBy);

	List<BadgeTemplate> getTemplates(BadgeTemplate.Scope scope);

	List<TemplateWithSize> getTemplatesWithSizes(BadgeTemplate.Scope scope);

	BadgeTemplate getTemplate(Long key);

	String getTemplateSvgPreviewImage(String templateImage);

	VFSLeaf getTemplateVfsLeaf(String templateImage);

	String getColorAsRgb(String colorId);

	Set<String> getTemplateSvgSubstitutionVariables(String image);

	String getTemplateSvgImageWithSubstitutions(Long templateKey, String backgroundColorId, String title);

	String getTemplateSvgImageWithSubstitutions(String templateImage, String backgroundColorId, String title);

	SelectionValues getAvailableLanguages(Locale displayLocale);

	void updateTemplate(BadgeTemplate badgeTemplate);

	void updateTemplate(BadgeTemplate badgeTemplate, File templateFile, String targetFileName, Identity savedBy);

	void deleteTemplate(BadgeTemplate badgeTemplate);


	//
	// Class
	//

	void createBadgeClass(BadgeClassImpl badgeClass);

	String createBadgeClassImageFromSvgTemplate(String uuid, Long templateKey, String backgroundColorId, String title, Identity savedBy);

	String createBadgeClassImageFromPngTemplate(String uuid, Long templateKey);

	String createBadgeClassImage(String uuid, File tempBadgeFileImage, String targetBadgeImageFileName, Identity savedBy);

	List<BadgeClass> getBadgeClasses(RepositoryEntry entry);

	List<BadgeClass> getBadgeClassesInCoOwnedCourseSet(RepositoryEntry entry);

	List<BadgeClassWithSizeAndCount> getBadgeClassesWithSizesAndCounts(RepositoryEntry entry);

	List<BadgeClassDAO.BadgeClassWithUseCount> getBadgeClassesWithUseCounts(RepositoryEntry entry);

	List<String> getBadgeClassNames(Collection<Long> badgeClassKeys);

	List<BadgeAssertion> getRuleEarnedBadgeAssertions(IdentityRef recipient, RepositoryEntryRef courseEntry, String courseNodeIdent);

	boolean conditionForCourseNodeExists(BadgeClass badgeClass, String courseNodeIdent);

	Long getNumberOfBadgeClasses(RepositoryEntryRef entry);

	BadgeClass getBadgeClass(String uuid);

	BadgeClass getBadgeClass(Long key);

	VFSLeaf getBadgeClassVfsLeaf(String classFile);

	BadgeClass updateBadgeClass(BadgeClass badgeClass);

	void removeCourseEntryFromCourseBadgeClasses(RepositoryEntry entry);

	void deleteBadgeClassAndAssertions(BadgeClass badgeClass);

	boolean hasCourseBadgeClasses(Identity author);

	List<BadgeClassDAO.BadgeClassWithUseCount> getCourseBadgeClassesWithUseCounts(Identity identity);

	List<BadgeClassWithSizeAndCount> getCourseBadgeClassesWithSizesAndCounts(Identity identity);

	void copyBadgeClass(Long sourceClassKey, Translator translator, Identity author);

	File copyBadgeClassWithTemporaryImage(Long sourceClassKey, BadgeClass targetClass, Translator translator);

	List<BadgeClassDAO.NameAndVersion> getBadgeClassNameVersionTuples(boolean excludeBadgeClass, BadgeClass badgeClass);

	//
	// Assertion
	//

	BadgeAssertion createBadgeAssertion(String uuid, BadgeClass badgeClass, Date issuedOn,
										Identity recipient, Identity savedBy);

	void handleCourseReset(RepositoryEntry courseEntry, boolean learningPath, Identity doer);

	void issueBadgesAutomatically(Identity recipient, Identity awardedBy, RepositoryEntry courseEntry,
								  boolean learningPath, List<AssessmentEntry> assessmentEntries);

	void issueBadgesAutomatically(RepositoryEntry courseEntry, boolean learningPath, Identity awardedBy);

	interface AssessmentEntriesHandler {
		void handleAssessmentEntries(Identity participant, List<AssessmentEntry> assessmentEntries);
	}

	void getParticipantsWithAssessmentEntries(RepositoryEntry courseEntry, Identity identity,
											  AssessmentToolSecurityCallback securityCallback,
											  AssessmentEntriesHandler assessmentEntriesHandler);

	List<ParticipantAndAssessmentEntries> associateParticipantsWithAssessmentEntries(List<AssessmentEntry> assessmentEntries);

	void issueBadge(BadgeClass badgeClass, List<Identity> recipients, Identity awardedBy);

	List<BadgeAssertion> getBadgeAssertions(BadgeClass badgeClass);

	List<BadgeAssertion> getBadgeAssertions(Identity recipient, RepositoryEntry courseEntry, boolean nullEntryMeansAll);

	List<BadgeAssertionWithSize> getBadgeAssertionsWithSizes(Identity identity, RepositoryEntry courseEntry,
															 boolean nullEntryMeansAll);

	boolean isBadgeAssertionExpired(BadgeAssertion badgeAssertion);

	BadgeAssertion getBadgeAssertion(String uuid);

	List<Identity> getBadgeAssertionIdentities(Collection<Long> badgeClassKeys);

	boolean hasBadgeAssertion(Identity recipient, String badgeClassUuid);

	boolean hasBadgeAssertion(Identity recipient, Long badgeClassKey);

	VFSLeaf getBadgeAssertionVfsLeaf(String relPath);

	boolean unrevokedBadgeAssertionsExist(BadgeClass badgeClass);

	void updateBadgeAssertion(BadgeAssertion badgeAssertion, Identity awardedBy);

	void revokeBadgeAssertion(Long key);

	void revokeBadgeAssertions(BadgeClass badgeClass);

	void deleteBadgeAssertion(BadgeAssertion badgeAssertion);

	String badgeAssertionAsLinkedInUrl(BadgeAssertion badgeAssertion);

	//
	// Category
	//

	List<TagInfo> getCategories(BadgeTemplate badgeTemplate, BadgeClass badgeClass);

	List<TagInfo> readBadgeCategoryTags();

	void updateCategories(BadgeTemplate badgeTemplate, BadgeClass badgeClass, List<String> displayNames);

	//
	// Entry Configuration
	//
	BadgeEntryConfiguration getConfiguration(RepositoryEntry entry);

	BadgeEntryConfiguration updateConfiguration(BadgeEntryConfiguration configuration);

	boolean isEnabled();

	boolean showBadgesEditTab(RepositoryEntry courseEntry, CourseNode courseNode);

	boolean showBadgesRunSegment(RepositoryEntry courseEntry, CourseNode courseNode, UserCourseEnvironment userCourseEnv);

	void deleteConfiguration(RepositoryEntryRef entry);

	void copyConfigurationAndBadgeClasses(RepositoryEntry sourceEntry, RepositoryEntry targetEntry, Identity author);

	void importConfiguration(RepositoryEntry targetEntry, BadgeEntryConfiguration configuration);

	void importBadgeClasses(RepositoryEntry targetEntry, BadgeClasses badgeClasses, File fImportBaseDirectory, Identity author);

	//
	// Organizations
	//
	List<BadgeOrganization> loadLinkedInOrganizations();

	BadgeOrganization loadLinkedInOrganization(Long key);

	BadgeOrganization loadLinkedInOrganization(String organizationId);

	boolean isBadgeOrganizationInUse(Long key);

	void addLinkedInOrganization(String organizationId, String organizationName);

	void updateBadgeOrganization(BadgeOrganization badgeOrganization);

	void deleteBadgeOrganization(BadgeOrganization badgeOrganization);

	//
	// Types
	//

	enum FileType {
		png,
		svg
	}

	record TemplateWithSize (BadgeTemplate template, Size size) {
		public Size fitIn(int width, int height) {
			double sourceAspectRatio = (double) size.getWidth() / (double) size.getHeight();
			double targetAspectRatio = (double) width / (double) height;
			if (sourceAspectRatio > targetAspectRatio) {
				return new Size(width, (int) Math.round(width / sourceAspectRatio), false);
			} else {
				return new Size((int) Math.round(height * sourceAspectRatio), height, false);
			}
		}
	}

	record BadgeClassWithSizeAndCount(BadgeClass badgeClass, Size size, Long count, Long revokedCount, Long resetCount) {
		public Size fitIn(int width, int height) {
			double sourceAspectRatio = (double) size.getWidth() / (double) size.getHeight();
			double targetAspectRatio = (double) width / (double) height;
			if (sourceAspectRatio > targetAspectRatio) {
				return new Size(width, (int) Math.round(width / sourceAspectRatio), false);
			} else {
				return new Size((int) Math.round(height * sourceAspectRatio), height, false);
			}
		}
	}

	record BadgeAssertionWithSize (BadgeAssertion badgeAssertion, Size size) {
		public Size fitIn(int width, int height) {
			double sourceAspectRatio = (double) size.getWidth() / (double) size.getHeight();
			double targetAspectRatio = (double) width / (double) height;
			if (sourceAspectRatio > targetAspectRatio) {
				return new Size(width, (int) Math.round(width / sourceAspectRatio), false);
			} else {
				return new Size((int) Math.round(height * sourceAspectRatio), height, false);
			}
		}
	}

	record ParticipantAndAssessmentEntries(Identity participant, List<AssessmentEntry> assessmentEntries) {}
}
