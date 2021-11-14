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
package org.olat.course.nodes.cl.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.cl.CheckboxManager;
import org.olat.course.nodes.cl.model.AssessmentBatch;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.cl.model.DBCheck;
import org.olat.course.nodes.cl.model.DBCheckbox;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedIdentityCheckListController extends FormBasicController {

	private static final String[] onKeys = new String[]{ "on" };

	private final boolean cancel;
	private final boolean withScore;
	private final boolean saveAndClose;
	private final ModuleConfiguration config;
	private final CheckListCourseNode courseNode;
	private final UserCourseEnvironment coachCourseEnv;
	private final UserCourseEnvironment assessedUserCourseEnv;
	private final OLATResourceable courseOres;
	private final Identity assessedIdentity;
	private final CheckboxList checkboxList;
	private List<CheckboxWrapper> wrappers;
	private FormLink saveAndCloseLink;

	@Autowired
	private CheckboxManager checkboxManager;
	
	public AssessedIdentityCheckListController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity,
			OLATResourceable courseOres, UserCourseEnvironment coachCourseEnv,
			UserCourseEnvironment assessedUserCourseEnv, CheckListCourseNode courseNode, boolean saveAndClose,
			boolean cancel) {
		super(ureq, wControl);

		this.cancel = cancel;
		this.courseNode = courseNode;
		this.courseOres = courseOres;
		this.saveAndClose = saveAndClose;
		this.coachCourseEnv = coachCourseEnv;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		config = courseNode.getModuleConfiguration();
		Boolean hasScore = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		withScore = (hasScore == null || hasScore.booleanValue());	

		this.assessedIdentity = assessedIdentity;
		CheckboxList configCheckboxList = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		if(configCheckboxList == null) {
			checkboxList = new CheckboxList();
			checkboxList.setList(Collections.<Checkbox>emptyList());
		} else {
			checkboxList = configCheckboxList;
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
		
			List<DBCheck> checks = checkboxManager.loadCheck(assessedIdentity, courseOres, courseNode.getIdent());
			Map<String, DBCheck> uuidToCheckMap = new HashMap<>();
			for(DBCheck check:checks) {
				uuidToCheckMap.put(check.getCheckbox().getCheckboxId(), check);
			}
			
			List<Checkbox> list = checkboxList.getList();
			wrappers = new ArrayList<>(list.size());
			for(Checkbox checkbox:list) {
				DBCheck check = uuidToCheckMap.get(checkbox.getCheckboxId());
				boolean readOnly = false;
				CheckboxWrapper wrapper = forgeCheckboxWrapper(checkbox, check, readOnly, formLayout);
				wrappers.add(wrapper);
			}
			layoutCont.contextPut("checkboxList", wrappers);
		}
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonCont);
		FormSubmit saveButton = uifactory.addFormSubmitButton("save", "save", buttonCont);
		saveButton.setEnabled(checkboxList.getNumOfCheckbox() > 0);
		saveButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		saveAndCloseLink = uifactory.addFormLink("save.close", buttonCont, Link.BUTTON);
		saveAndCloseLink.setEnabled(checkboxList.getNumOfCheckbox() > 0);
		saveAndCloseLink.setVisible(saveAndClose && !coachCourseEnv.isCourseReadOnly());
		if(cancel) {
			uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
		}
	}
	
	private CheckboxWrapper forgeCheckboxWrapper(Checkbox checkbox, DBCheck check, boolean readOnly, FormItemContainer formLayout) {
		String[] values = new String[]{ translate(checkbox.getLabel().i18nKey()) };
		
		String boxId = "box_" + checkbox.getCheckboxId();
		MultipleSelectionElement boxEl = uifactory
				.addCheckboxesHorizontal(boxId, null, formLayout, onKeys, values);
		boxEl.setEnabled(!readOnly && !coachCourseEnv.isCourseReadOnly());
		boxEl.setLabel(StringHelper.escapeHtml(checkbox.getTitle()), null, false);
		boxEl.showLabel(true);
		boxEl.addActionListener(FormEvent.ONCHANGE);
		
		TextElement pointEl = null;
		if(withScore && checkbox.getPoints() != null) {
			String pointId = "point_" + checkbox.getCheckboxId();
			String points;
			if(check != null && check.getChecked() != null && check.getChecked().booleanValue()) {
				points = AssessmentHelper.getRoundedScore(check.getScore());
			} else {
				points = null;
			}
			pointEl = uifactory.addTextElement(pointId, null, 16, points, formLayout);
			pointEl.setDisplaySize(5);
			pointEl.setEnabled(!coachCourseEnv.isCourseReadOnly());
			
			Float maxScore = checkbox.getPoints();
			if(maxScore != null) {
				String maxValue = AssessmentHelper.getRoundedScore(maxScore);
				pointEl.setExampleKey("checklist.point.example", new String[]{ "0", maxValue});
			}
			// hide when not yet checked
			if(check != null) {
				pointEl.setVisible(check.getChecked());
			} else {
				pointEl.setVisible(false);
			}
		}
		
		CheckboxWrapper wrapper = new CheckboxWrapper(checkbox, check, boxEl, pointEl);
		boxEl.setUserObject(wrapper);
		if(check != null && check.getChecked() != null && check.getChecked().booleanValue()) {
			boxEl.select(onKeys[0], true);
			wrapper.setDbCheckbox(check.getCheckbox());
		}
		return wrapper;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof MultipleSelectionElement) {
			MultipleSelectionElement boxEl = (MultipleSelectionElement)source;
			CheckboxWrapper wrapper = (CheckboxWrapper)boxEl.getUserObject();
			doUpdateCheck(wrapper, boxEl.isAtLeastSelected(1));
		} else if(saveAndCloseLink == source) {
			if(validateFormLogic(ureq)) {
				doSave();
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		for(CheckboxWrapper wrapper:wrappers) {
			TextElement pointEl = wrapper.getPointEl();
			if(pointEl != null) {
				pointEl.clearError();
				String val = pointEl.getValue();
				if(StringHelper.containsNonWhitespace(val)) {
					try {
						Float max = wrapper.getCheckbox().getPoints();
						float maxScore = max == null ? 0f : max.floatValue();
						float score = Float.parseFloat(val);
						if(score < 0f || score > maxScore) {
							pointEl.setErrorKey("form.error.scoreOutOfRange", null);
							allOk &= false;
						}

					} catch (NumberFormatException e) {
						pointEl.setErrorKey("form.error.wrongFloat", null);
						allOk &= false;
					}
				}
			}
		}
		return allOk;
	}

	private void doSave() {
		List<AssessmentBatch> batchElements = new ArrayList<>();
		for(CheckboxWrapper wrapper:wrappers) {
			Float editedPoint = null;
			if(wrapper.getPointEl() != null) {
				String val = wrapper.getPointEl().getValue();
				if(StringHelper.containsNonWhitespace(val)) {
					try {
						editedPoint = Float.valueOf(val);
					} catch (NumberFormatException e) {
						editedPoint = null;
					}	
				}
			}
			
			boolean editedValue = wrapper.getCheckboxEl().isAtLeastSelected(1);
			
			Float currentPoint = null;
			boolean currentValue = false;
			if(wrapper.getCheck() != null) {
				currentPoint = wrapper.getCheck().getScore();
				Boolean checkObj = wrapper.getCheck().getChecked();
				if(checkObj != null && checkObj.booleanValue()) {
					currentValue = checkObj.booleanValue();
				}
			}
			
			if((editedValue != currentValue)
					|| ((currentPoint == null && editedPoint != null)
					|| (currentPoint != null &&  editedPoint == null)
					|| (currentPoint != null && !currentPoint.equals(editedPoint)))) {
				
				String boxId = wrapper.getCheckbox().getCheckboxId();
				batchElements.add(new AssessmentBatch(assessedIdentity.getKey(), boxId, editedPoint, editedValue));
			}
		}
		checkboxManager.check(courseOres, courseNode.getIdent(), batchElements);
		
		courseNode.updateScoreEvaluation(getIdentity(), assessedUserCourseEnv, assessedIdentity, Role.coach);
	}
	
	private void doUpdateCheck(CheckboxWrapper wrapper, boolean check) {
		if(wrapper.getPointEl() == null) return;//nothing to do

		if(check) {
			if(!StringHelper.containsNonWhitespace(wrapper.getPointEl().getValue())) {
				Checkbox checkbox = wrapper.getCheckbox();
				Float points = checkbox.getPoints();
				if(points != null) {
					String val = AssessmentHelper.getRoundedScore(points);
					wrapper.getPointEl().setValue(val);
				}
			}
		} else if(wrapper.getPointEl() != null) {
			wrapper.getPointEl().setValue("");
		}
		// hide when not yet checked
		wrapper.getPointEl().setVisible(check);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	public static class CheckboxWrapper {
		
		private final TextElement pointEl;
		private final MultipleSelectionElement checkboxEl;
		private final Checkbox checkbox;
		private DBCheck check;
		private DBCheckbox dbCheckbox;
		
		public CheckboxWrapper(Checkbox checkbox, DBCheck check, MultipleSelectionElement checkboxEl, TextElement pointEl) {
			this.checkboxEl = checkboxEl;
			this.check = check;
			this.pointEl = pointEl;
			this.checkbox = checkbox;
		}

		public Checkbox getCheckbox() {
			return checkbox;
		}
		
		public DBCheck getCheck() {
			return check;
		}

		public void setCheck(DBCheck check) {
			this.check = check;
		}

		/**
		 * This value is lazy loaded and can be null!
		 * @return
		 */
		public DBCheckbox getDbCheckbox() {
			return dbCheckbox;
		}

		public void setDbCheckbox(DBCheckbox dbCheckbox) {
			this.dbCheckbox = dbCheckbox;
		}

		public String getTitle2() {
			return checkbox.getTitle();
		}
		
		public boolean isPointsAvailable() {
			return checkbox.getPoints() != null;
		}
		
		public TextElement getPointEl() {
			return pointEl;
		}

		public MultipleSelectionElement getCheckboxEl() {
			return checkboxEl;
		}
		
		public String getCheckboxElName() {
			return checkboxEl.getName();
		}
	}
}