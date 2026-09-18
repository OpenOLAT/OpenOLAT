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
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.factsheet.Fact;
import org.olat.core.gui.components.factsheet.FactSheet;
import org.olat.core.gui.components.factsheet.FactSheetFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.util.ComponentList;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailerResult;
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
import org.olat.course.run.leave.ConfirmLeaveController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.LeavingStatusList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
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
	private static final String CMD_LEAVE = "leave";

	private Link leaveLink;
	private CloseableModalController cmc;
	private ConfirmLeaveController leaveDialogBox;

	private final RepositoryEntry entry;
	private final DetailsHeaderConfig config;
	private final boolean closeTabOnLeave;
	private boolean hasContent;

	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private RepositoryManager repositoryManager;

	public InfoPageMyCourseController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			boolean isMember, boolean guestOnly, boolean closeTabOnLeave, DetailsHeaderConfig config) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(),
				Util.createPackageTranslator(GradeUIFactory.class, ureq.getLocale())));
		this.entry = entry;
		this.config = config;
		this.closeTabOnLeave = closeTabOnLeave;

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
		hasContent = !facts.isEmpty() || config.isLeaveAvailable();
		if (!hasContent) {
			putInitialPanel(new Panel("empty"));
			return;
		}

		FactSheet factSheet = FactSheetFactory.createFactSheet("factSheet", null);
		factSheet.setTitle(translate("details.my.course"));
		factSheet.setFacts(facts);
		putInitialPanel(factSheet);
		if (config.isLeaveAvailable()) {
			String typeName = translate(entry.getOlatResource().getResourceableTypeName());
			leaveLink = LinkFactory.createCustomLink(CMD_LEAVE, CMD_LEAVE, translate("sign.out.type", typeName),
					Link.BUTTON | Link.NONTRANSLATED, null, this);
			leaveLink.setElementCssClass("o_sign_out btn-danger " + FactSheet.CSS_FOOTER_LINK_FULL_WIDTH);
			leaveLink.setIconLeftCSS("o_icon o_icon_sign_out");
			leaveLink.setGhost(true);
			leaveLink.setEnabled(config.isLeaveEnabled());
			factSheet.setFooterLinks(List.of(leaveLink));
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == leaveLink) {
			doConfirmLeave(ureq);
		} else if (source instanceof Link link && CMD_GROUP.equals(link.getCommand())) {
			doOpenGroup(ureq, (Long) link.getUserObject());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (leaveDialogBox == source) {
			if (event.equals(Event.DONE_EVENT)) {
				doLeave(ureq);
				fireEvent(ureq, new LeavingEvent(entry));
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(leaveDialogBox);
		removeAsListenerAndDispose(cmc);
		leaveDialogBox = null;
		cmc = null;
	}

	private void doConfirmLeave(UserRequest ureq) {
		if (guardModalController(leaveDialogBox)) return;

		String title = translate("sign.out.type", translate(entry.getOlatResource().getResourceableTypeName()));
		leaveDialogBox = new ConfirmLeaveController(ureq, getWindowControl(), entry);
		listenTo(leaveDialogBox);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), leaveDialogBox.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doLeave(UserRequest ureq) {
		MailerResult result = new MailerResult();
		MailPackage reMailing = new MailPackage(result, getWindowControl().getBusinessControl().getAsString(), true);
		LeavingStatusList status = new LeavingStatusList();
		repositoryManager.leave(getIdentity(), entry, status, reMailing);
		businessGroupService.leave(getIdentity(), entry, status, reMailing);
		DBFactory.getInstance().commit();

		if (status.isWarningManagedGroup() || status.isWarningManagedCourse()) {
			showWarning("sign.out.warning.managed");
		} else if (status.isWarningGroupWithMultipleResources()) {
			showWarning("sign.out.warning.mutiple.resources");
		} else {
			showInfo("sign.out.success", new String[]{ StringHelper.escapeHtml(entry.getDisplayname()) });
			if (closeTabOnLeave) {
				getWindowControl().getWindowBackOffice().getWindow().getDTabs().closeDTab(ureq, entry.getOlatResource(), null);
			}
		}
	}

	private void doOpenGroup(UserRequest ureq, Long groupKey) {
		String businessPath = "[BusinessGroup:" + groupKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

}
