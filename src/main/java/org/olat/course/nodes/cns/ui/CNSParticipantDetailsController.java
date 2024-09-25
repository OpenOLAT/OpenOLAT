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
package org.olat.course.nodes.cns.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.Util;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.learningpath.LearningPathStatus;
import org.olat.course.learningpath.ui.LearningPathListController;
import org.olat.course.learningpath.ui.LearningPathStatusCellRenderer;
import org.olat.course.learningpath.ui.ObligationEditController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.cns.ui.CNSParticipantDetailsDataModel.CNSParticipantDetailsCols;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.assessment.ui.component.EvaluationLearningProgressCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserInfoProfile;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserInfoProfileController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSParticipantDetailsController extends FormBasicController {
	
	private static final String CMD_OBLIGATION = "obligation";

	private FlexiTableElement tableEl;
	private CNSParticipantDetailsDataModel dataModel;
	
	private UserInfoProfileController profileCtrl;
	private CloseableCalloutWindowController ccwc;
	private Controller obligationEditCtrl;

	private final UserCourseEnvironmentImpl coachedCourseEnv;
	private final UserInfoProfileConfig profileConfig;
	private final UserInfoProfile profile;
	private final RepositoryEntry courseEntry;
	private final List<CourseNode> childNodes;
	private final List<AssessmentEntry> selectedEntries;
	private final String suffix;
	private int counter = 0;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	public CNSParticipantDetailsController(UserRequest ureq, WindowControl wControl, Form mainForm, Identity coachedIdentity,
			UserInfoProfileConfig profileConfig, UserInfoProfile profile, CourseEnvironment courseEnvironment,
			RepositoryEntry courseEntry, List<CourseNode> childNodes, List<AssessmentEntry> selectedEntries) {
		super(ureq, wControl, LAYOUT_CUSTOM, "participant_details", mainForm);
		setTranslator(Util.createPackageTranslator(LearningPathListController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
		this.profileConfig = profileConfig;
		this.profile = profile;
		this.courseEntry = courseEntry;
		this.childNodes = childNodes;
		this.selectedEntries = selectedEntries;
		suffix = "_" + coachedIdentity.getKey();
		
		IdentityEnvironment identityEnv = new IdentityEnvironment();
		identityEnv.setIdentity(coachedIdentity);
		coachedCourseEnv = new UserCourseEnvironmentImpl(identityEnv, courseEnvironment);
		// Viewed identity must act as participant only, to get the right assigned / excluded course nodes.
		coachedCourseEnv.setUserRoles(false, false, true);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		profileCtrl = new UserInfoProfileController(ureq, getWindowControl(), profileConfig, profile);
		listenTo(profileCtrl);
		String profileName = "o_cns_detailu_" + profile.getIdentityKey();
		flc.put(profileName, profileCtrl.getInitialComponent());
		flc.contextPut("profileName", profileName);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		IndentedNodeRenderer intendedNodeRenderer = new IndentedNodeRenderer();
		intendedNodeRenderer.setIndentationEnabled(false);
		FlexiCellRenderer nodeRenderer = new TreeNodeFlexiCellRenderer(intendedNodeRenderer);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CNSParticipantDetailsCols.courseNode, nodeRenderer));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CNSParticipantDetailsCols.learningProgress,
				new EvaluationLearningProgressCellRenderer(getLocale(), true, true)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CNSParticipantDetailsCols.status,  new LearningPathStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CNSParticipantDetailsCols.obligation));
		
		dataModel = new CNSParticipantDetailsDataModel(columnsModel);
		String tableName = "o_cns_detailt_" + profile.getIdentityKey();
		flc.contextPut("tableName", tableName);
		tableEl = uifactory.addTableElement(getWindowControl(), tableName, dataModel, 250, false, getTranslator(), formLayout);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
	}
	
	private void loadModel() {
		List<CNSParticipantDetailsRow> rows = new ArrayList<>(childNodes.size());
		
		Map<String, AssessmentEntry> nodeIdentToEntry = selectedEntries.stream()
				.collect(Collectors.toMap(AssessmentEntry::getSubIdent, Function.identity()));
		for (CourseNode courseNode : childNodes) {
			CNSParticipantDetailsRow row = new CNSParticipantDetailsRow(courseNode);
			
			AssessmentEntry assessmentEntry = nodeIdentToEntry.get(courseNode.getIdent());
			if (assessmentEntry != null) {
				AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
				AssessmentEvaluation evaluation = AssessmentEvaluation.toAssessmentEvaluation(assessmentEntry, assessmentConfig);
				row.setEvaluation(evaluation);
				
				LearningPathStatus learningPathStatus = LearningPathStatus.of(evaluation);
				row.setLearningPathStatus(learningPathStatus);
			}
			
			forgeObligation(row);
			
			rows.add(row);
		}
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void forgeObligation(CNSParticipantDetailsRow row) {
		StringBuilder sb = new StringBuilder();
		
		if (row.getEvaluation() != null) {
			ObligationOverridable obligation = row.getEvaluation().getObligation();
			if (AssessmentObligation.mandatory == obligation.getCurrent()) {
				sb.append(translate("config.obligation.mandatory"));
			} else if (AssessmentObligation.optional == obligation.getCurrent()) {
				sb.append(translate("config.obligation.optional"));
			} else if (AssessmentObligation.evaluated == obligation.getCurrent()) {
				sb.append(translate("config.obligation.evaluated"));
			} else if (AssessmentObligation.excluded == obligation.getCurrent()) {
				sb.append(translate("config.obligation.excluded"));
			}
		} else {
			sb.append(translate("config.obligation.excluded"));
		}

		FormLink formLink = uifactory.addFormLink("o_obli_" + (counter++) + suffix, CMD_OBLIGATION + suffix,
				sb.toString(), null, null, Link.NONTRANSLATED);
		formLink.setUserObject(row.getCourseNode());
		row.setObligationFormItem(formLink);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink link) {
			if (link.getCmd().startsWith(CMD_OBLIGATION) && link.getCmd().endsWith(suffix) && link.getUserObject() instanceof CourseNode node) {
				doEditObligation(ureq, node, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == obligationEditCtrl) {
			if (event == Event.DONE_EVENT) {
				coachedCourseEnv.getScoreAccounting().evaluateAll(true);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			ccwc.deactivate();
			cleanUp();
		} else if (source == ccwc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(obligationEditCtrl);
		removeAsListenerAndDispose(ccwc);
		obligationEditCtrl = null;
		ccwc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doEditObligation(UserRequest ureq, CourseNode courseNode, FormLink link) {
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(obligationEditCtrl);
		
		obligationEditCtrl = new ObligationEditController(ureq, getWindowControl(), courseEntry, courseNode,
				coachedCourseEnv, !coachedCourseEnv.isCourseReadOnly());
		listenTo(obligationEditCtrl);
		
		CalloutSettings settings = new CalloutSettings();
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), obligationEditCtrl.getInitialComponent(),
				link.getFormDispatchId(), "", true, "", settings);
		listenTo(ccwc);
		ccwc.activate();
	}

}
