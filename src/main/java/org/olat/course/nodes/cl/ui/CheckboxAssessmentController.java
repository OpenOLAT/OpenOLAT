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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.DB;
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
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.cl.CheckboxManager;
import org.olat.course.nodes.cl.model.AssessmentBatch;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.cl.ui.CheckboxAssessmentDataModel.Cols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckboxAssessmentController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[]{ "" };
	
	private FlexiTableElement table;
	private SingleSelection checkboxEl;
	private FormLink selectAllBoxButton;
	private CheckboxAssessmentDataModel model;
	private List<CheckListAssessmentRow> initialRows;
	
	private final CheckboxList checkboxList;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CheckboxManager checkboxManager;
	@Autowired
	private BaseSecurityModule securityModule;

	private final boolean withScore;
	private int currentCheckboxIndex = 0;
	private final OLATResourceable courseOres;
	private final CheckListCourseNode courseNode;
	
	public CheckboxAssessmentController(UserRequest ureq, WindowControl wControl, CheckboxList checkboxList,
			List<CheckListAssessmentRow> initialRows, OLATResourceable courseOres, CheckListCourseNode courseNode) {
		super(ureq, wControl, "assessment_per_box");
		this.courseNode = courseNode;
		this.courseOres = courseOres;
		this.initialRows = initialRows;
		this.checkboxList = checkboxList;

		ModuleConfiguration config = courseNode.getModuleConfiguration();
		Boolean hasScore = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		withScore = (hasScore == null || hasScore.booleanValue());	

		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(CheckListAssessmentController.USER_PROPS_ID, isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("assessment.checkbox.description");

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = CheckListAssessmentDataModel.USER_PROPS_OFFSET + i++;
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(CheckListAssessmentController.USER_PROPS_ID , userPropertyHandler);
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

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.check.i18nKey(), Cols.check.ordinal(),
				true, Cols.check.name()));
		if(withScore) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.points.i18nKey(), Cols.points.ordinal(),
					true, Cols.points.name()));
		}
		int numOfCheckbox = checkboxList.getList().size();
		String[] keys = new String[numOfCheckbox];
		String[] values = new String[numOfCheckbox];
		List<Checkbox> checkbox = checkboxList.getList();
		for(int j=0; j<numOfCheckbox; j++) {
			keys[j] = checkbox.get(j).getCheckboxId();
			values[j] = checkbox.get(j).getTitle();	
		}
		
		FormLayoutContainer selectCont = FormLayoutContainer.createDefaultFormLayout("checkbox_sel_cont", getTranslator());
		formLayout.add(selectCont);
		
		checkboxEl = uifactory.addDropdownSingleselect("checkbox", "select.checkbox", selectCont, keys, values, null);
		checkboxEl.addActionListener(FormEvent.ONCHANGE);
		checkboxEl.select(keys[0], true);
		
		Checkbox box = checkboxList.getList().get(currentCheckboxIndex);
		boolean hasPoints = box.getPoints() != null && box.getPoints().floatValue() > 0f;
		
		List<CheckboxAssessmentRow> boxRows = new ArrayList<>(initialRows.size());
		for(CheckListAssessmentRow initialRow: initialRows) {
			Boolean[] checked = new Boolean[numOfCheckbox];
			if(initialRow.getChecked() != null) {
				System.arraycopy(initialRow.getChecked(), 0, checked, 0, initialRow.getChecked().length);
			}
			Float[] scores = new Float[numOfCheckbox];
			if(initialRow.getScores() != null) {
				System.arraycopy(initialRow.getScores(), 0, scores, 0, initialRow.getScores().length);
			}
			
			CheckboxAssessmentRow row = new CheckboxAssessmentRow(initialRow, checked, scores);
	
			String name = "box_" + boxRows.size() + "_";
			String pointVal = "";
			if(scores != null && scores.length > currentCheckboxIndex
					&& scores[currentCheckboxIndex] != null) {
				pointVal = AssessmentHelper.getRoundedScore(scores[currentCheckboxIndex]);
			}
			TextElement pointEl = uifactory.addTextElement(name + "point", null, 5, pointVal, formLayout);
			pointEl.setDisplaySize(5);
			
			MultipleSelectionElement checkEl = uifactory.addCheckboxesHorizontal(name + "check", null, formLayout, onKeys, onValues);
			checkEl.setDomReplacementWrapperRequired(false);
			checkEl.addActionListener(FormEvent.ONCHANGE);
			checkEl.setUserObject(row);
			if(checked != null && checked.length > currentCheckboxIndex
					&& checked[currentCheckboxIndex] != null && checked[currentCheckboxIndex].booleanValue()) {
				checkEl.select(onKeys[0], true);
			}
			pointEl.setVisible(hasPoints);

			row.setCheckedEl(checkEl);
			row.setPointEl(pointEl);
			boxRows.add(row);
		}

		model = new CheckboxAssessmentDataModel(boxRows, columnsModel, getLocale());
		table = uifactory.addTableElement(getWindowControl(), "checkbox-list", model, getTranslator(), formLayout);
		table.setCustomizeColumns(true);
		table.setEditMode(true);
		table.setAndLoadPersistedPreferences(ureq, "checkbox-assessment-v2");

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		selectAllBoxButton = uifactory.addFormLink("selectall", buttonsCont, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(checkboxEl == source) {
			int nextCheckboxIndex = checkboxEl.getSelected();
			saveCurrentSelectCheckbox();
			
			Checkbox box = checkboxList.getList().get(nextCheckboxIndex);
			boolean hasPoints = box.getPoints() != null && box.getPoints().floatValue() > 0f;
			
			List<CheckboxAssessmentRow> rows = model.getObjects();
			for(CheckboxAssessmentRow row:rows) {
				Boolean[] checkedArr = row.getChecked();
				if(checkedArr[nextCheckboxIndex] != null && checkedArr[nextCheckboxIndex].booleanValue()) {
					row.getCheckedEl().select(onKeys[0], true);
				} else {
					row.getCheckedEl().select(onKeys[0], false);
				}
				
				Float[] scores = row.getScores();
				if(scores[nextCheckboxIndex] != null && scores[nextCheckboxIndex] != null) {
					row.getPointEl().setValue(AssessmentHelper.getRoundedScore(scores[nextCheckboxIndex]));
				} else {
					row.getPointEl().setValue("");
				}
				row.getPointEl().setVisible(hasPoints);
			}
			currentCheckboxIndex = nextCheckboxIndex;
		} else if(source instanceof MultipleSelectionElement) {
			MultipleSelectionElement checkEl = (MultipleSelectionElement)source;
			if(checkEl.getUserObject() instanceof CheckboxAssessmentRow) {
				CheckboxAssessmentRow row = (CheckboxAssessmentRow)checkEl.getUserObject();
				if(row.getPointEl().isVisible()) {
					boolean checked = checkEl.isAtLeastSelected(1);
					if(checked) {
						int nextCheckboxIndex = checkboxEl.getSelected();
						Checkbox box = checkboxList.getList().get(nextCheckboxIndex);
						String pointVal = AssessmentHelper.getRoundedScore(box.getPoints());
						row.getPointEl().setValue(pointVal);
					} else {
						row.getPointEl().setValue("");
					}
				}
			}
		} else if(selectAllBoxButton == source) {
			doSelectAll();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void saveCurrentSelectCheckbox() {
		Float defaultScore = checkboxList.getList().get(currentCheckboxIndex).getPoints();
		List<CheckboxAssessmentRow> rows = model.getObjects();
		for(CheckboxAssessmentRow row:rows) {
			boolean checked = row.getCheckedEl().isAtLeastSelected(1);
			Boolean[] checkedArr = row.getChecked();
			if(checked) {
				checkedArr[currentCheckboxIndex] = Boolean.TRUE;
			} else {
				checkedArr[currentCheckboxIndex] = Boolean.FALSE;
			}
			
			Float points;
			if(checked) {
				points = getPointVal(row);
				if(points == null) {
					points = defaultScore;
				}
			} else {
				points = new Float(0f);
			}
			row.getScores()[currentCheckboxIndex] = points;
		}
	}
	
	private Float getPointVal(CheckboxAssessmentRow row) {
		String pointVal = row.getPointEl().getValue();
		try {
			return new Float(pointVal);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	private void doSelectAll() {
		List<CheckboxAssessmentRow> rows = model.getObjects();
		for(CheckboxAssessmentRow row:rows) {
			boolean checked = row.getCheckedEl().isAtLeastSelected(1);
			Boolean[] checkedArr = row.getChecked();
			if(!checked) {
				checkedArr[currentCheckboxIndex] = Boolean.TRUE;
				row.getCheckedEl().select(onKeys[0], true);
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		saveCurrentSelectCheckbox();
		
		int numOfCheckbox = checkboxList.getNumOfCheckbox();
		Map<Long, CheckListAssessmentRow> identityToInitialRow = new HashMap<>();
		for(CheckListAssessmentRow initialRow:initialRows) {
			identityToInitialRow.put(initialRow.getIdentityKey(), initialRow);
		}
		
		Set<Long> assessedIdentityToUpdate = new HashSet<>();
		List<CheckboxAssessmentRow> rows = model.getObjects();
		List<AssessmentBatch> batchElements = new ArrayList<>();
		for(CheckboxAssessmentRow row:rows) {
			CheckListAssessmentRow initialRow = identityToInitialRow.get(row.getIdentityKey());
			Boolean[] checked = initialRow.getChecked();
			Boolean[] editedChecked = row.getChecked();
			Float[] scores = initialRow.getScores();
			Float[] editedScores = row.getScores();
			
			for(int i=0; i<numOfCheckbox; i++) {
				Checkbox box = checkboxList.getList().get(i);
				
				boolean currentValue = getSecureValue(checked, i);
				boolean editedValue = getSecureValue(editedChecked, i);
				Float currentPoint = getSecureValue(scores, i);;
				Float editedPoint;
				if(box.getPoints() != null && box.getPoints().floatValue() > 0f) {
					editedPoint = getSecureValue(editedScores, i);
				} else {
					editedPoint = null;
				}

				if((editedValue != currentValue)
						|| ((currentPoint == null && editedPoint != null)
						|| (currentPoint != null &&  editedPoint == null)
						|| (currentPoint != null && !currentPoint.equals(editedPoint)))) {
					Checkbox checkbox = checkboxList.getList().get(i);
					String checkboxId = checkbox.getCheckboxId();
					batchElements.add(new AssessmentBatch(row.getIdentityKey(), checkboxId, editedPoint, editedValue));
					assessedIdentityToUpdate.add(row.getIdentityKey());
				}
			}
		}
		checkboxManager.check(courseOres, courseNode.getIdent(), batchElements);
		
		if(!assessedIdentityToUpdate.isEmpty()) {
			dbInstance.commit();
			ICourse course = CourseFactory.loadCourse(courseOres);
			
			List<Identity> assessedIdentities = securityManager.loadIdentityByKeys(assessedIdentityToUpdate);
			for(Identity assessedIdentity:assessedIdentities) {
				UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
				courseNode.updateScoreEvaluation(getIdentity(), assessedUserCourseEnv, assessedIdentity, Role.coach);
			}
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private boolean getSecureValue(Boolean[] checked, int i) {
		boolean value;
		if(checked != null && checked.length > 0 && i<checked.length && checked[i] != null) {
			value = checked[i].booleanValue();
		} else {
			value = false;
		}
		return value;
	}
	
	private Float getSecureValue(Float[] scores, int i) {
		Float value;
		if(scores != null && scores.length > 0 && i<scores.length && scores[i] != null) {
			value = scores[i];
		} else {
			value = null;
		}
		return value;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
