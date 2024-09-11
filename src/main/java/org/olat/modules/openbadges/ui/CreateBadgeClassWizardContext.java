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
import java.util.Set;

import org.json.JSONObject;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.core.CourseElement;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesFactory;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.criteria.BadgeCondition;
import org.olat.modules.openbadges.criteria.BadgeCriteria;
import org.olat.modules.openbadges.criteria.BadgeCriteriaXStream;
import org.olat.modules.openbadges.criteria.CourseElementPassedCondition;
import org.olat.modules.openbadges.criteria.CourseElementScoreCondition;
import org.olat.modules.openbadges.criteria.CoursePassedCondition;
import org.olat.modules.openbadges.criteria.CourseScoreCondition;
import org.olat.modules.openbadges.criteria.LearningPathProgressCondition;
import org.olat.modules.openbadges.criteria.Symbol;
import org.olat.modules.openbadges.model.BadgeClassImpl;
import org.olat.modules.openbadges.v2.Constants;
import org.olat.modules.openbadges.v2.Profile;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * Initial date: 2023-06-19<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CreateBadgeClassWizardContext {

	public final static Long OWN_BADGE_KEY = -1L;

	private final RepositoryEntry entry;
	private final CourseNode courseNode;
	private final RepositoryEntrySecurity reSecurity;

	public boolean showStartingPointStep(Identity author) {
		if (isGlobalBadge()) {
			return false;
		}

		OpenBadgesManager openBadgesManager = CoreSpringFactory.getImpl(OpenBadgesManager.class);
		return openBadgesManager.hasCourseBadgeClasses(author);
	}

	public boolean showRecipientsStep() {
		if (isGlobalBadge()) {
			if (!badgeCriteria.isAwardAutomatically()) {
				return false;
			}
			if (!badgeCriteria.hasGlobalBadgeConditions()) {
				return false;
			}
			return true;
		}
		CourseEnvironment courseEnv = courseEnvironment();
		if (courseEnv == null) {
			return false;
		}
		List<Identity> identities = ScoreAccountingHelper.loadParticipants(courseEnv);
		return !identities.isEmpty();
	}

	public boolean isLearningPath() {
		CourseEnvironment courseEnv = courseEnvironment();
		if (courseEnv == null) {
			return false;
		}
		NodeAccessType nodeAccessType  = courseEnv.getCourseConfig().getNodeAccessType();
		return LearningPathNodeAccessProvider.TYPE.equals(nodeAccessType.getType());
	}

	public List<CourseNode> assessableCourseNodes() {
		if (courseResourcableId == null) {
			return new ArrayList<>();
		}

		ICourse course = CourseFactory.loadCourse(courseResourcableId);
		CourseEditorTreeModel editorModel = course.getEditorTreeModel();

		return AssessmentHelper.getAssessableNodes(entry, editorModel, null);
	}

	public boolean isGlobalBadge() {
		return !isCourseBadge();
	}

	private CourseEnvironment courseEnvironment() {
		if (isGlobalBadge()) {
			return null;
		}
		if (entry == null) {
			return null;
		}
		if (entry.getEntryStatus() != RepositoryEntryStatusEnum.published && entry.getEntryStatus() != RepositoryEntryStatusEnum.preparation) {
			return null;
		}
		ICourse course = CourseFactory.loadCourse(courseResourcableId);
		return course.getCourseEnvironment();
	}

	public boolean selectedTemplateIsSvg() {
		if (OWN_BADGE_KEY.equals(selectedTemplateKey)) {
			return false;
		}
		return selectedTemplateImage != null && OpenBadgesFactory.isSvgFileName(selectedTemplateImage);
	}

	public boolean selectedTemplateIsPng() {
		if (OWN_BADGE_KEY.equals(selectedTemplateKey)) {
			return false;
		}
		return selectedTemplateImage != null && OpenBadgesFactory.isPngFileName(selectedTemplateImage);
	}

	public boolean ownFileIsSvg() {
		if (!OWN_BADGE_KEY.equals(selectedTemplateKey)) {
			return false;
		}
		return temporaryBadgeImageFile != null && OpenBadgesFactory.isSvgFileName(temporaryBadgeImageFile.getPath());
	}

	public boolean ownFileIsPng() {
		if (!OWN_BADGE_KEY.equals(selectedTemplateKey)) {
			return false;
		}
		return temporaryBadgeImageFile != null && OpenBadgesFactory.isPngFileName(temporaryBadgeImageFile.getPath());
	}

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
	private final Mode mode;
	private File temporaryBadgeImageFile;
	private String targetBadgeImageFileName;
	private List<Identity> earners;
	private boolean startFromScratch = false;
	private Long sourceBadgeClassKey;

	public CreateBadgeClassWizardContext(RepositoryEntry entry, CourseNode courseNode,
										 RepositoryEntrySecurity reSecurity, Translator translator) {
		this.entry = entry;
		this.courseNode = courseNode;
		this.reSecurity = reSecurity;
		mode = Mode.create;
		ICourse course = entry != null ? CourseFactory.loadCourse(entry) : null;
		courseResourcableId = course != null ? course.getResourceableId() : null;
		BadgeClassImpl badgeClassImpl = new BadgeClassImpl();
		badgeClassImpl.setUuid(OpenBadgesFactory.createIdentifier());
		badgeClassImpl.setStatus(BadgeClass.BadgeClassStatus.preparation);
		badgeClassImpl.setSalt(OpenBadgesFactory.createSalt(badgeClassImpl));
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
		badgeClassImpl.setVersionWithScan("1.0");
		badgeClassImpl.setLanguage("en");
		badgeClassImpl.setValidityEnabled(false);
		badgeClassImpl.setEntry(entry);
		backgroundColorId = "gold";
		title = translator.translate("var.title.default");
		initCriteria();
		issuer = new Profile(badgeClassImpl);
		badgeClass = badgeClassImpl;
	}

	public CreateBadgeClassWizardContext(BadgeClass badgeClass, RepositoryEntrySecurity reSecurity) {
		mode = Mode.edit;
		ICourse course = badgeClass.getEntry() != null ? CourseFactory.loadCourse(badgeClass.getEntry()) : null;
		this.entry = badgeClass.getEntry();
		this.courseNode = null;
		this.reSecurity = reSecurity;
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
		addDefaultRule();
	}

	private void addDefaultRule() {
		if (courseNode != null) {
			CourseElement courseElement = BadgeCondition.loadCourseElement(entry, courseNode.getIdent());
			if (courseElement != null && courseElement.isAssesseable()) {
				if (!AssessmentConfig.Mode.none.equals(courseElement.getPassedMode())) {
					badgeCriteria.setAwardAutomatically(true);
					badgeCriteria.getConditions().add(new CourseElementPassedCondition(this.courseNode.getIdent()));
					return;
				}
				if (!AssessmentConfig.Mode.none.equals(courseElement.getScoreMode())) {
					badgeCriteria.setAwardAutomatically(true);
					badgeCriteria.getConditions().add(new CourseElementScoreCondition(this.courseNode.getIdent(),
							Symbol.greaterThan, 1));
					return;
				}
			}
		}
		if (entry != null) {
			CourseNode rootNode = CourseFactory.loadCourse(entry).getRunStructure().getRootNode();
			CourseElement courseElement = BadgeCondition.loadCourseElement(entry, rootNode.getIdent());
			if (!AssessmentConfig.Mode.none.equals(courseElement.getPassedMode())) {
				badgeCriteria.setAwardAutomatically(true);
				badgeCriteria.getConditions().add(new CoursePassedCondition());
				return;
			}
			if (!AssessmentConfig.Mode.none.equals(courseElement.getScoreMode())) {
				badgeCriteria.setAwardAutomatically(true);
				badgeCriteria.getConditions().add(new CourseScoreCondition(Symbol.greaterThan, 1));
				return;
			}

			if (isLearningPath()) {
				badgeCriteria.setAwardAutomatically(true);
				badgeCriteria.getConditions().add(new LearningPathProgressCondition(Symbol.greaterThan, 50));
			}
		}
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

	public File getTemporaryBadgeImageFile() {
		return temporaryBadgeImageFile;
	}

	public void setTemporaryBadgeImageFile(File temporaryBadgeImageFile) {
		this.temporaryBadgeImageFile = temporaryBadgeImageFile;
	}

	public String getTargetBadgeImageFileName() {
		return targetBadgeImageFileName;
	}

	public void setTargetBadgeImageFileName(String targetBadgeImageFileName) {
		this.targetBadgeImageFileName = targetBadgeImageFileName;
	}

	public List<Identity> getEarners() {
		return earners;
	}

	public void setEarners(List<Identity> earners) {
		this.earners = earners;
	}

	public boolean isCourseBadge() {
		return entry != null;
	}

	public RepositoryEntrySecurity getReSecurity() {
		return reSecurity;
	}

	public boolean isStartFromScratch() {
		return startFromScratch;
	}

	public void setStartFromScratch(boolean startFromScratch) {
		this.startFromScratch = startFromScratch;
	}

	public Long getSourceBadgeClassKey() {
		return sourceBadgeClassKey;
	}

	public void setSourceBadgeClassKey(Long sourceBadgeClassKey) {
		this.sourceBadgeClassKey = sourceBadgeClassKey;
	}

	public void copyFromExistingBadge(Translator translator) {
		if (sourceBadgeClassKey == null) {
			return;
		}

		OpenBadgesManager openBadgesManager = CoreSpringFactory.getImpl(OpenBadgesManager.class);
		File targetImageFile = openBadgesManager.copyBadgeClassWithTemporaryImage(sourceBadgeClassKey, badgeClass, translator);
		temporaryBadgeImageFile = targetImageFile;
		targetBadgeImageFileName = targetImageFile.getName();
		selectedTemplateKey = OWN_BADGE_KEY;
		selectedTemplateImage = null;
		badgeCriteria = BadgeCriteriaXStream.fromXml(badgeClass.getCriteria());
	}

	public void startFromScratch() {
		temporaryBadgeImageFile = null;
		targetBadgeImageFileName = null;
		selectedTemplateKey = null;
		selectedTemplateImage = null;
		initCriteria();
	}

	public String getBadgeName(String defaultName) {
		if (courseNode != null) {
			if (StringHelper.containsNonWhitespace(courseNode.getShortTitle())) {
				return courseNode.getShortTitle();
			}
			if (StringHelper.containsNonWhitespace(courseNode.getLongTitle())) {
				return courseNode.getLongTitle();
			}
		}
		return defaultName;
	}
}
