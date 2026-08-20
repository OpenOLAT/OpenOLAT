/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.factsheet.Fact;
import org.olat.core.gui.components.factsheet.FactSheet;
import org.olat.core.gui.components.factsheet.FactSheetFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.util.ComponentList;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 19 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class InfoPageMyCourseController extends BasicController {

	private static final String CMD_GROUP = "group";

	private boolean hasContent;

	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private BusinessGroupService businessGroupService;

	public InfoPageMyCourseController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			boolean isMember, boolean guestOnly) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(),
				Util.createPackageTranslator(GradeUIFactory.class, ureq.getLocale())));

		List<Fact> facts = new ArrayList<>();
		if (!guestOnly && "CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
			addCourseFacts(facts, entry);
		}
		if (isMember) {
			addGroupsFact(facts, entry);
		}
		init(facts);
	}

	private void addCourseFacts(List<Fact> facts, RepositoryEntry entry) {
		ICourse course;
		try {
			course = CourseFactory.loadCourse(entry);
		} catch (CorruptedCourseException e) {
			return;
		}
		if (course == null) {
			return;
		}

		boolean learningPath = !ConditionNodeAccessProvider.TYPE.equals(course.getCourseConfig().getNodeAccessType().getType());
		boolean efficiencyStatementEnabled = course.getCourseConfig().isEfficiencyStatementEnabled();
		if (!learningPath && !efficiencyStatementEnabled) {
			return;
		}

		UserCourseEnvironment userCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(getIdentity(), course);
		CourseNode rootNode = userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(entry, rootNode);
		AssessmentEvaluation assessmentEvaluation = courseAssessmentService.getAssessmentEvaluation(rootNode, userCourseEnv);

		if (learningPath && assessmentEvaluation.getCompletion() != null) {
			addProgressFact(facts, assessmentEvaluation);
		}
		if (efficiencyStatementEnabled) {
			boolean hasScore = Mode.none != assessmentConfig.getScoreMode();
			boolean hasPassed = Mode.none != assessmentConfig.getPassedMode();
			if (hasPassed) {
				addStatusFact(facts, assessmentEvaluation);
			}
			if (hasScore) {
				addScoreFact(facts, assessmentEvaluation);
			}
			if (gradeModule.isEnabled()) {
				addGradeFact(facts, assessmentEvaluation);
			}
		}
	}

	private void addProgressFact(List<Fact> facts, AssessmentEvaluation assessmentEvaluation) {
		int completion = Math.round(assessmentEvaluation.getCompletion().floatValue() * 100);
		String progress = completion + "%";
		facts.add(FactSheetFactory.createFact("o_icon_progress", translate("details.progress"), progress));
	}

	private void addStatusFact(List<Fact> facts, AssessmentEvaluation assessmentEvaluation) {
		Boolean passed = assessmentEvaluation.getPassed();
		if (passed == null) {
			return;
		}
		String status = passed.booleanValue() ? translate("passed.true") : translate("passed.false");
		String iconCss = passed.booleanValue() ? "o_icon_passed" : "o_icon_failed";
		facts.add(FactSheetFactory.createFact(iconCss, translate("details.label.status"), status));
	}

	private void addScoreFact(List<Fact> facts, AssessmentEvaluation assessmentEvaluation) {
		if (assessmentEvaluation.getScore() == null) {
			return;
		}
		String score = AssessmentHelper.getRoundedScore(assessmentEvaluation.getScore());
		if (StringHelper.containsNonWhitespace(score)) {
			facts.add(FactSheetFactory.createFact("o_icon_score", translate("details.label.score"), score));
		}
	}

	private void addGradeFact(List<Fact> facts, AssessmentEvaluation assessmentEvaluation) {
		if (!StringHelper.containsNonWhitespace(assessmentEvaluation.getGrade())) {
			return;
		}
		String label = GradeUIFactory.translateGradeSystemLabel(getTranslator(), assessmentEvaluation.getGradeSystemIdent());
		String grade = GradeUIFactory.translatePerformanceClass(getTranslator(),
				assessmentEvaluation.getPerformanceClassIdent(), assessmentEvaluation.getGrade(), assessmentEvaluation.getGradeSystemIdent());
		facts.add(FactSheetFactory.createFact("o_icon_grade", label, grade));
	}

	private void addGroupsFact(List<Fact> facts, RepositoryEntry entry) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(getIdentity(), true, true);
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, entry, 0, -1);
		if (groups.isEmpty()) {
			return;
		}

		List<Component> groupLinks = new ArrayList<>(groups.size());
		for (BusinessGroup group : groups) {
			groupLinks.add(groupLink(group));
		}
		Component value = groupLinks.size() == 1 ? groupLinks.get(0) : new ComponentList("groups", groupLinks);
		facts.add(FactSheetFactory.createFact("o_icon_group", translate("cif.groups"), value));
	}

	private Link groupLink(BusinessGroup group) {
		String id = "grp_" + group.getKey();
		String title = StringHelper.escapeHtml(group.getName());
		Link link = LinkFactory.createCustomLink(id, CMD_GROUP, title, Link.LINK | Link.NONTRANSLATED, null, this);
		link.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
		link.setUserObject(group.getKey());
		return link;
	}

	public boolean hasContent() {
		return hasContent;
	}

	private void init(List<Fact> facts) {
		hasContent = !facts.isEmpty();
		if (!hasContent) {
			putInitialPanel(new Panel("empty"));
		} else {
			FactSheet factSheet = FactSheetFactory.createFactSheet("factSheet", null);
			factSheet.setTitle(translate("details.my.course"));
			factSheet.setFacts(facts);
			putInitialPanel(factSheet);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link && CMD_GROUP.equals(link.getCommand())) {
			doOpenGroup(ureq, (Long) link.getUserObject());
		}
	}

	private void doOpenGroup(UserRequest ureq, Long groupKey) {
		String businessPath = "[BusinessGroup:" + groupKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

}
