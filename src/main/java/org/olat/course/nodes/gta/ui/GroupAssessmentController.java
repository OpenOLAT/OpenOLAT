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
package org.olat.course.nodes.gta.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.ui.GroupAssessmentModel.Cols;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupAssessmentController extends FormBasicController {

	private static final String[] userVisibilityKeys = new String[]{ "visible", "hidden" };
	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };
	
	private FlexiTableElement table;
	private GroupAssessmentModel model;
	private FormLink saveAndDoneButton;
	private TextElement groupScoreEl, groupCommentEl;
	private SingleSelection userVisibilityEl;
	private MultipleSelectionElement groupPassedEl, applyToAllEl;
	
	private EditCommentController editCommentCtrl;
	private CloseableCalloutWindowController commentCalloutCtrl;
	
	private final List<UserPropertyHandler> userPropertyHandlers;

	private Float cutValue;
	private final boolean withScore, withPassed, withComment;
	private final GTACourseNode gtaNode;
	private final RepositoryEntry courseEntry;
	private final BusinessGroup assessedGroup;
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	private final List<Long> duplicateMemberKeys;
	
	public GroupAssessmentController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, GTACourseNode courseNode, BusinessGroup assessedGroup) {
		super(ureq, wControl, "assessment_per_group");
		this.gtaNode = courseNode;
		this.courseEntry = courseEntry;
		this.assessedGroup = assessedGroup;

		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		withScore = Mode.none != assessmentConfig.getScoreMode();
		withPassed = Mode.none != assessmentConfig.getPassedMode();
		if(withPassed) {
			cutValue = assessmentConfig.getCutValue();
		}
		withComment = assessmentConfig.hasComment();
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(GTACoachedGroupGradingController.USER_PROPS_ID, isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		List<IdentityRef> duplicates = gtaManager.getDuplicatedMemberships(courseNode);
		duplicateMemberKeys = new ArrayList<>(duplicates.size());
		for(IdentityRef duplicate:duplicates) {
			duplicateMemberKeys.add(duplicate.getKey());
		}
		
		initForm(ureq);
		ModelInfos modelInfos = loadModel();
		updateGUI(modelInfos);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FormLayoutContainer groupGradingCont = FormLayoutContainer.createDefaultFormLayout("groupGrading", getTranslator());
		groupGradingCont.setRootForm(mainForm);
		formLayout.add(groupGradingCont);
		
		applyToAllEl = uifactory.addCheckboxesHorizontal("applytoall", "group.apply.toall", groupGradingCont, onKeys, onValues);
		applyToAllEl.addActionListener(FormEvent.ONCHANGE);
		applyToAllEl.setElementCssClass("o_sel_course_gta_apply_to_all");
		
		if(withPassed && cutValue == null) {
			groupPassedEl = uifactory.addCheckboxesHorizontal("checkgroup", "group.passed", groupGradingCont, onKeys, onValues);
			groupPassedEl.setElementCssClass("o_sel_course_gta_group_passed");
		}
		
		if(withScore) {
			String pointVal = "";
			groupScoreEl = uifactory.addTextElement("pointgroup", "group.score", 5, pointVal, groupGradingCont);
			groupScoreEl.setElementCssClass("o_sel_course_gta_group_score");
		}
		
		if(withComment) {
			String comment = "";
			groupCommentEl = uifactory.addTextAreaElement("usercomment", "group.comment", 2500, 5, 40, true, false, comment, groupGradingCont);
			groupCommentEl.setElementCssClass("o_sel_course_gta_group_comment");
		}
		
		if(withPassed || withScore || withComment) {
			String[] userVisibilityValues = new String[]{ translate("user.visibility.visible"), translate("user.visibility.hidden") };
			userVisibilityEl = uifactory.addRadiosHorizontal("user.visibility", "user.visibility", groupGradingCont, userVisibilityKeys, userVisibilityValues);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = GTACoachedGroupGradingController.USER_PROPS_OFFSET + i++;
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(GTACoachedGroupGradingController.USER_PROPS_ID , userPropertyHandler);
			if(visible) {
				FlexiColumnModel col;
				if(UserConstants.FIRSTNAME.equals(propName)
						|| UserConstants.LASTNAME.equals(propName)) {
					col = new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
							colIndex, userPropertyHandler.getName(), true, propName,
							new StaticFlexiCellRenderer(userPropertyHandler.getName(), new TextFlexiCellRenderer()));
				} else {
					col = new DefaultFlexiColumnModel(true, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, propName);
				}
				columnsModel.addFlexiColumnModel(col);
			}
		}
		
		if(withPassed && cutValue == null) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.passedEl.i18nKey(), Cols.passedEl.ordinal()));
		}

		if(withScore) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.scoreEl.i18nKey(), Cols.scoreEl.ordinal()));
		}
		
		if(withComment) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.commentEl.i18nKey(), Cols.commentEl.ordinal()));
		}

		model = new GroupAssessmentModel(gtaNode, userPropertyHandlers, getLocale(), columnsModel);
		table = uifactory.addTableElement(getWindowControl(), "group-list", model, getTranslator(), formLayout);
		table.setCustomizeColumns(true);
		table.setEditMode(true);
		table.setAndLoadPersistedPreferences(ureq, "gtagroup-assessment-v2");

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		saveAndDoneButton = uifactory.addFormLink("save.done", buttonsCont, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void updateGUI(ModelInfos modelInfos) {
		if(modelInfos.isSame()) {
			applyToAllEl.select(onKeys[0], true);
			table.setVisible(false);
			
			if(groupPassedEl != null) {
				groupPassedEl.setVisible(true);
				Boolean passed = modelInfos.getPassed();
				groupPassedEl.select(onKeys[0], passed != null && passed.booleanValue());
			}
			if(groupScoreEl != null) {
				groupScoreEl.setVisible(true);
				Float score = modelInfos.getScore();
				if(score != null) {
					String scoreVal = AssessmentHelper.getRoundedScore(score);
					groupScoreEl.setValue(scoreVal);
				} else {
					groupScoreEl.setValue("");
				}
			}
			if(groupCommentEl != null) {
				groupCommentEl.setVisible(true);
				String comment = modelInfos.getComment();
				if(comment != null) {
					groupCommentEl.setValue(comment);
				}
			}
			
			if(userVisibilityEl != null) {
				userVisibilityEl.setVisible(true);
				if(modelInfos.getUserVisible() == null || modelInfos.getUserVisible().booleanValue()) {
					userVisibilityEl.select(userVisibilityKeys[0], true);
				} else {
					userVisibilityEl.select(userVisibilityKeys[1], true);
				}
			}
		} else {
			applyToAllEl.select(onKeys[0], false);
			table.setVisible(true);
			if(groupPassedEl != null) {
				groupPassedEl.setVisible(false);
			}
			if(groupScoreEl != null) {
				groupScoreEl.setVisible(false);
			}
			if(groupCommentEl != null) {
				groupCommentEl.setVisible(false);
			}
		}
		
		if(StringHelper.containsNonWhitespace(modelInfos.getDuplicates())) {
			String warning = translate("error.duplicate.memberships", new String[]{ gtaNode.getShortTitle(), modelInfos.getDuplicates()});
			flc.contextPut("duplicateWarning", warning);
		} else {
			flc.contextRemove("duplicateWarning");
		}
	}
	
	/**
	 * 
	 * @return True if all results are the same
	 */
	private ModelInfos loadModel() {
		//load participants, load datas
		ICourse course = CourseFactory.loadCourse(courseEntry);
		List<Identity> identities = businessGroupService.getMembers(assessedGroup, GroupRoles.participant.name());
		
		Map<Identity, AssessmentEntry> identityToEntryMap = new HashMap<>();
		List<AssessmentEntry> entries = course.getCourseEnvironment()
				.getAssessmentManager().getAssessmentEntries(assessedGroup, gtaNode);
		for(AssessmentEntry entry:entries) {
			identityToEntryMap.put(entry.getIdentity(), entry);
		}
		
		int count = 0;
		boolean same = true;
		StringBuilder duplicateWarning = new StringBuilder();
		Float scoreRef = null;
		Boolean passedRef = null;
		String commentRef = null;
		Boolean userVisibleRef = null;
		
		List<AssessmentRow> rows = new ArrayList<>(identities.size());
		for(Identity identity:identities) {
			AssessmentEntry entry = identityToEntryMap.get(identity);
			
			ScoreEvaluation scoreEval = null;
			if(withScore || withPassed) {
				CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
				scoreEval = courseAssessmentService.toAssessmentEvaluation(entry, gtaNode);
				if (scoreEval == null) {
					scoreEval = ScoreEvaluation.EMPTY_EVALUATION;
				}
			}
			
			String comment = null;
			if(withComment && entry != null) {
				comment = entry.getComment();
			}

			boolean duplicate = duplicateMemberKeys.contains(identity.getKey());
			if(duplicate) {
				if(duplicateWarning.length() > 0) duplicateWarning.append(", ");
				duplicateWarning.append(StringHelper.escapeHtml(userManager.getUserDisplayName(identity)));
			}

			AssessmentRow row = new AssessmentRow(identity, duplicate);
			rows.add(row);
			
			if(withScore) {
				Float score = scoreEval.getScore();
				String pointVal = AssessmentHelper.getRoundedScore(score);
				TextElement pointEl = uifactory.addTextElement("point" + count, null, 5, pointVal, flc);
				pointEl.setDisplaySize(5);
				row.setScoreEl(pointEl);
				if(count == 0) {
					scoreRef = score;
				} else if(!same(scoreRef, score)) {
					same = false;
				}
			}
			
			if(withPassed && cutValue == null) {
				Boolean passed = scoreEval.getPassed();
				MultipleSelectionElement passedEl = uifactory.addCheckboxesHorizontal("check" + count, null, flc, onKeys, onValues);
				if(passed != null && passed.booleanValue()) {
					passedEl.select(onKeys[0], passed.booleanValue());
				}
				row.setPassedEl(passedEl);
				if(count == 0) {
					passedRef = passed;
				} else if(!same(passedRef, passed)) {
					same = false;
				}
			}
			
			if(withComment) {
				FormLink commentLink = uifactory.addFormLink("comment-" + CodeHelper.getRAMUniqueID(), "comment", "comment", null, flc, Link.LINK);
				if(StringHelper.containsNonWhitespace(comment)) {
					commentLink.setIconLeftCSS("o_icon o_icon_comments");
				} else {
					commentLink.setIconLeftCSS("o_icon o_icon_comments_none");
				}
				commentLink.setUserObject(row);
				row.setComment(comment);
				row.setCommentEditLink(commentLink);

				if(count == 0) {
					commentRef = comment;
				} else if(!same(commentRef, comment)) {
					same = false;
				}
			}
			
			if(withScore || withPassed) {
				Boolean userVisible = scoreEval.getUserVisible();
				if(userVisible == null) {
					userVisible = Boolean.TRUE;
				}
				
				if(count == 0) {
					userVisibleRef = userVisible;
				} else if(!same(userVisibleRef, userVisible)) {
					same = false;
				}
			}
			
			count++;
		}
		
		model.setObjects(rows);
		table.reset();
		
		return new ModelInfos(same, scoreRef, passedRef, commentRef, userVisibleRef, duplicateWarning.toString());
	}
	
	private boolean same(Object reference, Object value) {
		boolean same = true;
		if((reference == null && value != null)
				|| (reference != null && value == null)
				|| (value != null && reference != null && !value.equals(reference))) {
			same = false;
		}
		return same;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(commentCalloutCtrl == source) {
			cleanUp();
		} else if(editCommentCtrl == source) {
			if(event == Event.DONE_EVENT) {
				table.reset();
			}
			commentCalloutCtrl.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(commentCalloutCtrl);
		removeAsListenerAndDispose(editCommentCtrl);
		commentCalloutCtrl = null;
		editCommentCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(applyToAllEl == source) {
			boolean allGroup = applyToAllEl.isAtLeastSelected(1);
			table.setVisible(!allGroup);
			if(groupPassedEl != null) {
				groupPassedEl.setVisible(allGroup);
			}
			if(groupScoreEl != null) {
				groupScoreEl.setVisible(allGroup);
			}
			if(groupCommentEl != null) {
				groupCommentEl.setVisible(allGroup);
			}
		} else if(source == saveAndDoneButton) {
			if(validateFormLogic(ureq)) {
				applyChanges(true);
				fireEvent(ureq, Event.CLOSE_EVENT);
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("comment".equals(link.getCmd())) {
				AssessmentRow row = (AssessmentRow)link.getUserObject();
				doEditComment(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		if(withScore) {
			if(applyToAllEl.isAtLeastSelected(1)) {
				allOk &= validateScore(groupScoreEl);
			} else {
				List<AssessmentRow> rows = model.getObjects();	
				for(AssessmentRow row:rows) {
					allOk &= validateScore(row.getScoreEl());
				}
			}
		}
	
		return allOk & super.validateFormLogic(ureq);
	}
	
	private boolean validateScore(TextElement scoreEl) {
		boolean allOk = true;
		
		scoreEl.clearError();
		String value = scoreEl.getValue();
		if(StringHelper.containsNonWhitespace(value)) {
			try {
				float score = Float.parseFloat(value);
				if(score < 0.0f) {
					scoreEl.setErrorKey("error.score.format", null);
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				scoreEl.setErrorKey("error.score.format", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		applyChanges(false);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void applyChanges(boolean setAsDone) {
		List<AssessmentRow> rows = model.getObjects();
		boolean userVisible = userVisibilityEl.isOneSelected() && userVisibilityEl.isSelected(0);
		if(applyToAllEl.isAtLeastSelected(1)) {
			applyChangesForTheWholeGroup(rows, setAsDone, userVisible);
		} else {
			applyChangesForEveryMemberGroup(rows, setAsDone, userVisible);
		}
	}
	
	private void applyChangesForEveryMemberGroup(List<AssessmentRow> rows, boolean setAsDone, boolean userVisible) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		
		for(AssessmentRow row:rows) {
			UserCourseEnvironment userCourseEnv = row.getUserCourseEnvironment(course);
			
			Float score = null;
			if(withScore) {
				String value = row.getScoreEl().getValue();
				if(StringHelper.containsNonWhitespace(value)) {
					score = Float.parseFloat(value);
				}
			}
			
			Boolean passed = null;
			if(withPassed) {
				if(cutValue == null) {
					passed = row.getPassedEl().isSelected(0);
				} else if(score != null) {
					passed = (score.floatValue() >= cutValue.floatValue()) ? Boolean.TRUE	: Boolean.FALSE;
				}
			}
			
			ScoreEvaluation newScoreEval;
			if(setAsDone) {
				newScoreEval = new ScoreEvaluation(score, passed, AssessmentEntryStatus.done, userVisible, null, null,null, null);
			} else {
				newScoreEval = new ScoreEvaluation(score, passed, null, userVisible, null, null, null, null);
			}
			courseAssessmentService.updateScoreEvaluation(gtaNode, newScoreEval, userCourseEnv, getIdentity(), false, Role.coach);
			
			if(withComment) {
				String comment = row.getComment();
				if(StringHelper.containsNonWhitespace(comment)) {
					courseAssessmentService.updatedUserComment(gtaNode, comment, userCourseEnv, getIdentity());
				}
			}
		}
	}
	
	private void applyChangesForTheWholeGroup(List<AssessmentRow> rows, boolean setAsDone, boolean userVisible) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		
		Float score = null;
		if(withScore) {
			String scoreValue = groupScoreEl.getValue();
			if(StringHelper.containsNonWhitespace(scoreValue)) {
				score = Float.parseFloat(scoreValue);
			}
		}
		
		Boolean passed = null;
		if(withPassed) {
			if(cutValue == null) {
				passed = groupPassedEl.isSelected(0);
			} else if(score != null) {
				passed = (score.floatValue() >= cutValue.floatValue()) ? Boolean.TRUE	: Boolean.FALSE;
			}
		}
		
		for(AssessmentRow row:rows) {
			UserCourseEnvironment userCourseEnv = row.getUserCourseEnvironment(course);
			ScoreEvaluation newScoreEval;
			if(setAsDone) {
				newScoreEval = new ScoreEvaluation(score, passed, AssessmentEntryStatus.done, userVisible, null, null, null, null);
			} else {
				newScoreEval = new ScoreEvaluation(score, passed, null, userVisible, null, null, null, null);
			}
			courseAssessmentService.updateScoreEvaluation(gtaNode, newScoreEval, userCourseEnv, getIdentity(), false, Role.coach);
		}

		if(withComment) {
			String comment = groupCommentEl.getValue();
			if(comment != null) {
				for(AssessmentRow row:rows) {
					UserCourseEnvironment userCourseEnv = row.getUserCourseEnvironment(course);
					courseAssessmentService.updatedUserComment(gtaNode, comment, userCourseEnv, getIdentity());
				}
			}
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doEditComment(UserRequest ureq, AssessmentRow row) {
		removeAsListenerAndDispose(commentCalloutCtrl);
		
		OLATResource courseOres = courseEntry.getOlatResource();
		editCommentCtrl = new EditCommentController(ureq, getWindowControl(), courseOres, gtaNode, row);
		listenTo(editCommentCtrl);
		commentCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				editCommentCtrl.getInitialComponent(), row.getCommentEditLink().getFormDispatchId(),
				"", true, "");
		listenTo(commentCalloutCtrl);
		commentCalloutCtrl.activate();
	}
	
	public static class ModelInfos {
		
		private final String duplicates;
		private final boolean same;
		private final Float score;
		private final Boolean passed;
		private final String comment;
		private final Boolean userVisible;
		
		public ModelInfos(boolean same, Float score, Boolean passed, String comment, Boolean userVisible, String duplicates) {
			this.same = same;
			this.score = score;
			this.passed = passed;
			this.comment = comment;
			this.userVisible = userVisible;
			this.duplicates = duplicates;
		}

		public boolean isSame() {
			return same;
		}

		public Float getScore() {
			return score;
		}

		public Boolean getPassed() {
			return passed;
		}
		
		public String getComment() {
			return comment;
		}

		public Boolean getUserVisible() {
			return userVisible;
		}

		public String getDuplicates() {
			return duplicates;
		}
	}
}
