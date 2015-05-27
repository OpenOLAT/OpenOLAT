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
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.ui.GroupAssessmentModel.Cols;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
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
	
	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };
	
	private FlexiTableElement table;
	private GroupAssessmentModel model;
	private TextElement groupScoreEl;
	private MultipleSelectionElement groupPassedEl, applyToAllEl;
	
	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;

	private Float cutValue;
	private final boolean withScore, withPassed;
	private final GTACourseNode gtaNode;
	private final CourseEnvironment courseEnv;
	private final BusinessGroup assessedGroup;
	private final AssessmentManager assessmentManager;
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	private final List<Long> duplicateMemberKeys;
	
	public GroupAssessmentController(UserRequest ureq, WindowControl wControl,
			CourseEnvironment courseEnv, GTACourseNode courseNode, BusinessGroup assessedGroup) {
		super(ureq, wControl, "assessment_per_group");
		this.gtaNode = courseNode;
		this.courseEnv = courseEnv;
		this.assessedGroup = assessedGroup;

		withScore = courseNode.hasScoreConfigured();
		withPassed = courseNode.hasPassedConfigured();
		if(withPassed) {
			cutValue = courseNode.getCutValueConfiguration();
		}
		
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(GTACoachedGroupGradingController.USER_PROPS_ID, isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		assessmentManager = courseEnv.getAssessmentManager();
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
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.username.i18nKey(), Cols.username.ordinal()));
		}
		
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
					col = new StaticFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
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

		model = new GroupAssessmentModel(gtaNode, userPropertyHandlers, getLocale(), columnsModel);
		table = uifactory.addTableElement(getWindowControl(), "group-list", model, getTranslator(), formLayout);
		table.setCustomizeColumns(true);
		table.setEditMode(true);
		table.setAndLoadPersistedPreferences(ureq, "gtagroup-assessment");

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
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
		} else {
			applyToAllEl.select(onKeys[0], false);
			table.setVisible(true);
			if(groupPassedEl != null) {
				groupPassedEl.setVisible(false);
			}
			if(groupScoreEl != null) {
				groupScoreEl.setVisible(false);
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
		ICourse course = CourseFactory.loadCourse(courseEnv.getCourseResourceableId());
		List<Identity> identities = businessGroupService.getMembers(assessedGroup, GroupRoles.participant.name());
		assessmentManager.preloadCache(identities);
		
		int count = 0;
		boolean same = true;
		StringBuilder duplicateWarning = new StringBuilder();
		Float scoreRef = null;
		Boolean passedRef = null;
		
		List<AssessmentRow> rows = new ArrayList<>(identities.size());
		for(Identity identity:identities) {
			UserCourseEnvironment userCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(identity, course);
			ScoreEvaluation scoreEval = userCourseEnv.getScoreAccounting().evalCourseNode(gtaNode);
			if (scoreEval == null) {
				scoreEval = new ScoreEvaluation(null, null);
			}
			
			boolean duplicate = duplicateMemberKeys.contains(identity.getKey());
			if(duplicate) {
				if(duplicateWarning.length() > 0) duplicateWarning.append(", ");
				duplicateWarning.append(StringHelper.escapeHtml(userManager.getUserDisplayName(identity)));
			}

			AssessmentRow row = new AssessmentRow(userCourseEnv, duplicate);
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
			count++;
		}
		
		model.setObjects(rows);
		table.reset();
		
		return new ModelInfos(same, scoreRef, passedRef, duplicateWarning.toString());
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
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		if(withScore) {
			List<AssessmentRow> rows = model.getObjects();	
			for(AssessmentRow row:rows) {
				TextElement scoreEl = row.getScoreEl();
				String value = scoreEl.getValue();
				if(StringHelper.containsNonWhitespace(value)) {
					try {
						float score = Float.parseFloat(value);
						if(score < 0.0f) {
							//not acceptable
						}
					} catch (NumberFormatException e) {
						allOk = false;
					}
				}
			}
		}
	
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {

		List<AssessmentRow> rows = model.getObjects();	
		if(applyToAllEl.isAtLeastSelected(1)) {
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
				UserCourseEnvironment userCourseEnv = row.getUserCourseEnvironment();
				ScoreEvaluation newScoreEval = new ScoreEvaluation(score, passed);
				gtaNode.updateUserScoreEvaluation(newScoreEval, userCourseEnv, getIdentity(), false);
			}
		} else {
			for(AssessmentRow row:rows) {
				UserCourseEnvironment userCourseEnv = row.getUserCourseEnvironment();
				
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
				
				ScoreEvaluation newScoreEval = new ScoreEvaluation(score, passed);
				gtaNode.updateUserScoreEvaluation(newScoreEval, userCourseEnv, getIdentity(), false);
			}
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public static class ModelInfos {
		
		private final String duplicates;
		private final boolean same;
		private final Float score;
		private final Boolean passed;
		
		public ModelInfos(boolean same, Float score, Boolean passed, String duplicates) {
			this.same = same;
			this.score = score;
			this.passed = passed;
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

		public String getDuplicates() {
			return duplicates;
		}
	}
}
