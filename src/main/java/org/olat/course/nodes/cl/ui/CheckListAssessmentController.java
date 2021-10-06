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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.transform.TransformerException;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CheckListCourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.cl.CheckboxManager;
import org.olat.course.nodes.cl.model.AssessedIdentity;
import org.olat.course.nodes.cl.model.AssessmentBatch;
import org.olat.course.nodes.cl.model.AssessmentData;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.cl.model.DBCheck;
import org.olat.course.nodes.cl.ui.CheckListAssessmentDataModel.Cols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * This is the coach view.
 * 
 * Initial date: 07.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListAssessmentController extends FormBasicController implements ControllerEventListener {
	
	protected static final String USER_PROPS_ID = CheckListAssessmentController.class.getCanonicalName();
	static final String CURRICULUM_EL_PREFIX = "curriculumelement-";
	static final String BUSINESS_GROUP_PREFIX = "businessgroup-";

	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };
	
	private final Float maxScore;
	private final Date dueDate;
	private final boolean withScore;
	private final CheckboxList checkboxList;
	private final Boolean closeAfterDueDate;
	private final OLATResourceable courseOres;
	private final CheckListCourseNode courseNode;
	private final ModuleConfiguration config;
	private final UserCourseEnvironment coachCourseEnv;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final boolean learningPath;

	private FormSubmit saveButton;
	private FormCancel cancelButton;
	private FormLink pdfExportButton, checkedPdfExportButton, editButton, boxAssessmentButton;
	private CheckListAssessmentDataModel model;
	private FlexiTableElement table;
	
	private CloseableModalController cmc;
	private AssessedIdentityOverviewController editCtrl;
	private CheckboxAssessmentController boxAssessmentCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CheckboxManager checkboxManager;
	@Autowired
	private AssessmentService assessmentService;
	
	/**
	 * Use this constructor to launch the checklist.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param courseNode
	 */
	public CheckListAssessmentController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment coachCourseEnv,
			OLATResourceable courseOres, CheckListCourseNode courseNode) {
		super(ureq, wControl, "assessment_list");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));

		this.courseOres = courseOres;
		this.courseNode = courseNode;
		this.coachCourseEnv = coachCourseEnv;
		this.learningPath = LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(coachCourseEnv).getType());
		config = courseNode.getModuleConfiguration();
		CheckboxList configCheckboxList = (CheckboxList)config.get(CheckListCourseNode.CONFIG_KEY_CHECKBOX);
		if(configCheckboxList == null) {
			checkboxList = new CheckboxList();
			checkboxList.setList(Collections.<Checkbox>emptyList());
		} else {
			checkboxList = configCheckboxList;
		}
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		closeAfterDueDate = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_CLOSE_AFTER_DUE_DATE);
		if(closeAfterDueDate != null && closeAfterDueDate.booleanValue()) {
			dueDate = (Date)config.get(CheckListCourseNode.CONFIG_KEY_DUE_DATE);
		} else {
			dueDate = null;
		}
		
		Boolean hasScore = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		withScore = (hasScore == null || hasScore.booleanValue());	
		
		maxScore = (Float)config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX);

		initForm(ureq);
		reloadTable();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("coach.desc");
		setFormContextHelp("Assessment#_checklist_manage");

		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			if(dueDate != null) {
				layoutCont.contextPut("dueDate", dueDate);
			}
		}

		FlexiTableSortOptions options = new FlexiTableSortOptions();
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = CheckListAssessmentDataModel.USER_PROPS_OFFSET + i++;
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);
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
				if(options.getDefaultOrderBy() == null) {
					options.setDefaultOrderBy(new SortKey(propName, true));
				}
			}
		}
		
		int numOfCheckbox = checkboxList.getNumOfCheckbox();
		List<Checkbox> boxList = checkboxList.getList();
		int j = 0;
		for(Checkbox box:boxList) {
			int colIndex = CheckListAssessmentDataModel.CHECKBOX_OFFSET + j++;
			String colName = "checkbox_" + colIndex;
			DefaultFlexiColumnModel column = new DefaultFlexiColumnModel(true, colName, colIndex, true, colName);
			column.setHeaderLabel(StringHelper.escapeHtml(box.getTitle()));
			columnsModel.addFlexiColumnModel(column);
		}

		if(withScore) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.totalPoints.i18nKey(), Cols.totalPoints.ordinal(), true, "points"));
		}
		if(coachCourseEnv.isCourseReadOnly()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.view.checkbox", translate("table.header.view.checkbox"), "view"));
		} else {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.edit.checkbox", translate("table.header.edit.checkbox"), "edit"));
		}

		model = new CheckListAssessmentDataModel(checkboxList, new ArrayList<>(), columnsModel, getLocale());
		table = uifactory.addTableElement(getWindowControl(), "checkbox-list", model, getTranslator(), formLayout);
		table.setExportEnabled(true);
		table.setCustomizeColumns(true);
		initFilters();
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		table.setSortSettings(sortOptions);
		table.setAndLoadPersistedPreferences(ureq, "checklist-assessment-v2-" + courseNode.getIdent());
		
		pdfExportButton = uifactory.addFormLink("pdf.export", formLayout, Link.BUTTON);
		pdfExportButton.setEnabled(numOfCheckbox > 0);
		checkedPdfExportButton = uifactory.addFormLink("pdf.export.checked", formLayout, Link.BUTTON);
		checkedPdfExportButton.setEnabled(numOfCheckbox > 0);
		
		editButton = uifactory.addFormLink("edit", formLayout, Link.BUTTON);
		editButton.setEnabled(numOfCheckbox > 0);
		editButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		saveButton = uifactory.addFormSubmitButton("save", formLayout);
		saveButton.getComponent().setSpanAsDomReplaceable(true);
		saveButton.setVisible(false);
		cancelButton = uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		cancelButton.setVisible(false);
		boxAssessmentButton = uifactory.addFormLink("box.assessment", formLayout, Link.BUTTON);
		boxAssessmentButton.setEnabled(numOfCheckbox > 0);
		boxAssessmentButton.setVisible(!coachCourseEnv.isCourseReadOnly());
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
		if (learningPath) {
			SelectionValues obligationValues = new SelectionValues();
			obligationValues.add(SelectionValues.entry(AssessmentObligation.mandatory.name(), translate("filter.mandatory")));
			obligationValues.add(SelectionValues.entry(AssessmentObligation.optional.name(), translate("filter.optional")));
			obligationValues.add(SelectionValues.entry(AssessmentObligation.excluded.name(), translate("filter.excluded")));
			FlexiTableMultiSelectionFilter obligationFilter = new FlexiTableMultiSelectionFilter(translate("filter.obligation"),
					AssessedIdentityListState.FILTER_OBLIGATION, obligationValues, true);
			obligationFilter.setValues(List.of(AssessmentObligation.mandatory.name(), AssessmentObligation.optional.name()));
			filters.add(obligationFilter);
		}
		
		SelectionValues groupValues = new SelectionValues();
		
		List<BusinessGroup> coachedGroups = coachCourseEnv.isAdmin()
				? coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getAllBusinessGroups()
				: coachCourseEnv.getCoachedGroups();
		if(coachedGroups != null) {
			for(BusinessGroup coachedGroup:coachedGroups) {
				groupValues.add(new SelectionValue(BUSINESS_GROUP_PREFIX + coachedGroup.getKey(), coachedGroup.getName(),
						null, "o_icon o_icon_curriculum_element", null, true));
			}
		}
		
		List<CurriculumElement> coachedElements = coachCourseEnv.isAdmin()
				? coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getAllCurriculumElements()
				: coachCourseEnv.getCoachedCurriculumElements();
		if(!coachedElements.isEmpty()) {
			for(CurriculumElement coachedElement: coachedElements) {
				groupValues.add(new SelectionValue(CURRICULUM_EL_PREFIX + coachedElement.getKey(), coachedElement.getDisplayName(),
						null, "o_icon o_icon_curriculum_element", null, true));
			}
		}
		
		if(!groupValues.isEmpty()) {
			FlexiTableExtendedFilter filter = new FlexiTableMultiSelectionFilter(translate("filter.groups"), "groups", groupValues, true);
			filters.add(filter);
		}
		
		if (!filters.isEmpty()) {
			table.setFilters(true, filters, false, true);
		}
	}
	
	private List<CheckListAssessmentRow> loadDatas() {
		CourseGroupManager cgm = coachCourseEnv.getCourseEnvironment().getCourseGroupManager();
		
		List<Checkbox> checkboxColl = checkboxList.getList();
		int numOfCheckbox = checkboxList.getNumOfCheckbox();
		RepositoryEntry courseEntry = cgm.getCourseEntry();

		List<AssessmentData> dataList = checkboxManager
				.getAssessmentDatas(courseOres, courseNode.getIdent(), cgm.getCourseEntry(), getIdentity(), coachCourseEnv.isAdmin());
		List<CheckListAssessmentRow> boxList = getAssessmentDataViews(dataList, checkboxColl);
		Map<Long,CheckListAssessmentRow> identityToView = boxList.stream()
				.collect(Collectors.toMap(CheckListAssessmentRow::getIdentityKey, row -> row, (row1, row2) -> row1));
		
		Map<Long, AssessmentObligation> identityKeyToObligation = learningPath
				? assessmentService.loadAssessmentEntriesBySubIdent(cgm.getCourseEntry(), courseNode.getIdent()).stream()
						.collect(Collectors.toMap(ae -> ae.getIdentity().getKey(), this::extractObligation))
				: Collections.emptyMap();
		
		List<AssessedIdentity> identityList = checkboxManager
				.getAssessedIdentities(courseEntry, getIdentity(), coachCourseEnv.isAdmin());
		for(AssessedIdentity identity:identityList) {
			CheckListAssessmentRow row = identityToView.computeIfAbsent(identity.getIdentity().getKey(), id -> {
				Boolean[] checked = new Boolean[numOfCheckbox];
				Float[] scores = new Float[numOfCheckbox];
				return new CheckListAssessmentRow(identity.getIdentity(), checked, scores, null, userPropertyHandlers, getLocale());
			});
			
			List<Long> curriculumElementKeys = identity.getCurriculumElmentKeys();
			if(!curriculumElementKeys.isEmpty()) {
				row.setCurriculumElementKeys(curriculumElementKeys);
			}
			List<Long> businessGroupKeys = identity.getBusinessGroupKeys();
			if(!businessGroupKeys.isEmpty()) {
				row.setGroupKeys(businessGroupKeys);
			}
			
			row.setAssessmentObligation(identityKeyToObligation.get(identity.getIdentity().getKey()));
		}
		
		List<CheckListAssessmentRow> views = new ArrayList<>();
		views.addAll(identityToView.values());
		return views;
	}
	
	private AssessmentObligation extractObligation(AssessmentEntry assessmentEntry) {
		return assessmentEntry != null && assessmentEntry.getObligation() != null
				? assessmentEntry.getObligation().getCurrent()
				: null;
	}
	
	private List<CheckListAssessmentRow> getAssessmentDataViews(List<AssessmentData> datas, List<Checkbox> checkbox) {
		List<CheckListAssessmentRow> dataViews = new ArrayList<>();
		
		int numOfcheckbox = checkbox.size();
		Map<String,Integer> indexed = new HashMap<>();
		for(int i=numOfcheckbox; i-->0; ) {
			indexed.put(checkbox.get(i).getCheckboxId(), Integer.valueOf(i));
		}
		
		for(AssessmentData data:datas) {
			Float[] scores = new Float[numOfcheckbox];
			Boolean[] checkBool = new Boolean[numOfcheckbox];
			float totalPoints = 0.0f;
			for(DBCheck check:data.getChecks()) {
				Float score = check.getScore();
				if(score != null) {
					totalPoints += score.floatValue();
					
				}
				
				if(check.getChecked() == null) continue;
				
				check.getCheckbox();
				
				Integer index = indexed.get(check.getCheckbox().getCheckboxId());
				if(index != null) {
					int i = index.intValue();
					if(i >= 0 && i<numOfcheckbox) {
						scores[i] = score;
						checkBool[i] = check.getChecked();
					}
				}
			}
			
			if(maxScore != null && maxScore.floatValue() > 0f && totalPoints > maxScore.floatValue()) {
				totalPoints = maxScore.floatValue();
			}
			CheckListAssessmentRow row = new CheckListAssessmentRow(data.getIdentity(), checkBool, scores, totalPoints, userPropertyHandlers, getLocale());
			dataViews.add(row);
		}
		return dataViews;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		saveButton.setVisible(false);
		cancelButton.setVisible(false);
		editButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		doSave();
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		saveButton.setVisible(false);
		cancelButton.setVisible(false);
		editButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		doDisableEditingMode();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(table == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("edit".equals(cmd) || "view".equals(cmd)) {
					CheckListAssessmentRow row = model.getObject(se.getIndex());
					doOpenEdit(ureq, row);
				} else if(UserConstants.FIRSTNAME.equals(cmd) || UserConstants.LASTNAME.equals(cmd)) {
					CheckListAssessmentRow row = model.getObject(se.getIndex());
					doOpenIdentity(ureq, row);
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				model.filter(table.getFilters());
				table.reset(false, false, true);
			}
		} else if(pdfExportButton == source) {
			doExportPDF(ureq);
		} else if(checkedPdfExportButton == source) {
			doCheckedExportPDF(ureq);
		} else if(editButton == source) {
			saveButton.setVisible(true);
			cancelButton.setVisible(true);
			editButton.setVisible(false);
			doEdit();
		} else if(boxAssessmentButton == source) {
			doOpenBoxAssessment(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		if(!(fiSrc instanceof MultipleSelectionElement)) {
			super.propagateDirtinessToContainer(fiSrc, event);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editCtrl == source) {
			if(event == Event.DONE_EVENT) {
				reloadTable();
			}
			if(event == Event.DONE_EVENT || Event.CANCELLED_EVENT == event) {
				cmc.deactivate();
				cleanUp();
			}
		} else if(boxAssessmentCtrl == source) {
			cmc.deactivate();
			cleanUp();
			
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				reloadTable();
			}
		} else if(cmc == source) {
			if(editCtrl != null && editCtrl.isChanges()) {
				reloadTable();
			}
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void reloadTable() {
		dbInstance.commit();//make sure all changes are on the database
		model.setObjects(loadDatas());
		model.filter(table.getFilters());
		table.reloadData();
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(boxAssessmentCtrl);
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		boxAssessmentCtrl = null;
		editCtrl = null;
		cmc = null;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void doOpenBoxAssessment(UserRequest ureq) {
		if(guardModalController(boxAssessmentCtrl)) return;

		List<CheckListAssessmentRow> rows = model.getObjects();
		boxAssessmentCtrl = new CheckboxAssessmentController(ureq, getWindowControl(), checkboxList, rows,
				courseOres, courseNode);
		listenTo(boxAssessmentCtrl);

		String title = translate("box.assessment");
		Component content = boxAssessmentCtrl.getInitialComponent();
		cmc = new CloseableModalController(getWindowControl(), translate("close"), content, true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEdit() {
		boolean edit = table.isEditMode();
		if(edit) {
			doDisableEditingMode();
		} else {
			List<CheckListAssessmentRow> rows = model.getBackedUpRows();
			int numOfCheckbox = checkboxList.getNumOfCheckbox();
			
			for(CheckListAssessmentRow row:rows) {
				Boolean[] checked = row.getChecked();
				MultipleSelectionElement[] checkedEls = new MultipleSelectionElement[numOfCheckbox];
				for(int i=0; i<numOfCheckbox; i++) {
					String checkName = "c" + i + "-" + row.getIdentityKey();
					checkedEls[i] = uifactory.addCheckboxesHorizontal(checkName, null, flc, onKeys, onValues);
					checkedEls[i].setAjaxOnly(true);
					checkedEls[i].setDomReplacementWrapperRequired(false);
					if(checked != null && i<checked.length && checked[i] != null && checked[i].booleanValue()) {
						checkedEls[i].select(onKeys[0], true);
					}
				}
				row.setCheckedEl(checkedEls);
			}
			table.setEditMode(true);
		}
	}
	
	private void doSave() {
		int numOfCheckbox = checkboxList.getNumOfCheckbox();
		List<CheckListAssessmentRow> rows = model.getBackedUpRows();//save all rows
		List<AssessmentBatch> batchElements = new ArrayList<>(rows.size());
		Set<Long> assessedIdentityToUpdate = new HashSet<>();
		for(CheckListAssessmentRow row:rows) {
			Boolean[] checked = row.getChecked();
			Boolean[] editedChecked = new Boolean[numOfCheckbox];
			MultipleSelectionElement[] checkedEls = row.getCheckedEl();
			if(checkedEls != null) {
				for(int i=0; i<numOfCheckbox; i++) {
					MultipleSelectionElement checkEl = checkedEls[i];
					boolean editedValue = checkEl.isAtLeastSelected(1);
					editedChecked[i] = Boolean.valueOf(editedValue);
					
					boolean currentValue;
					if(checked != null && checked.length > 0 && i<checked.length && checked[i] != null) {
						currentValue = checked[i].booleanValue();
					} else {
						currentValue = false;
					}
					
					if(editedValue != currentValue) {
						Checkbox checkbox = checkboxList.getList().get(i);
						String checkboxId = checkbox.getCheckboxId();
						Float score = editedValue ? checkbox.getPoints() : Float.valueOf(0f);
						batchElements.add(new AssessmentBatch(row.getIdentityKey(), checkboxId, score, editedValue));
						assessedIdentityToUpdate.add(row.getIdentityKey());
					}

					flc.remove(checkEl);
				}
			}
			row.setCheckedEl(null);
			row.setChecked(editedChecked);
		}
		doDisableEditingMode();
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
		
		reloadTable();
	}
	
	private void doDisableEditingMode() {
		table.setEditMode(false);
		List<CheckListAssessmentRow> rows = model.getBackedUpRows();
		for(CheckListAssessmentRow row:rows) {
			MultipleSelectionElement[] checkedEls = row.getCheckedEl();
			if(checkedEls != null) {
				for(MultipleSelectionElement checkEl:checkedEls) {
					flc.remove(checkEl);
				}
			}
			row.setCheckedEl(null);
		}
	}
	
	private void doExportPDF(UserRequest ureq) {
		try {
			ICourse course = CourseFactory.loadCourse(courseOres);
			
			String name = courseNode.getShortTitle();
			CheckboxPDFExport pdfExport = new CheckboxPDFExport(name, getTranslator(), userPropertyHandlers);
			pdfExport.setAuthor(userManager.getUserDisplayName(getIdentity()));
			pdfExport.setCourseNodeTitle(courseNode.getShortTitle());
			pdfExport.setCourseTitle(course.getCourseTitle());
			pdfExport.setCourseNodeTitle(courseNode.getShortTitle());
			pdfExport.setGroupName(getGroupNames());
			pdfExport.create(checkboxList, model.getObjects());
			ureq.getDispatchResult().setResultingMediaResource(pdfExport);
		} catch (IOException | TransformerException e) {
			logError("", e);
		}
	}
	
	private String getGroupNames() {
		List<FlexiTableFilter> filters = table.getFilters();
		if (filters != null && !filters.isEmpty()) {
			FlexiTableFilter groupsFilter = FlexiTableFilter.getFilter(filters, "groups");
			if(groupsFilter != null) {
				FlexiTableMultiSelectionFilter filter = (FlexiTableMultiSelectionFilter)groupsFilter;
				List<String> filterValues = filter.getValues();
				if(filterValues != null) {
					List<String> groupNames = new ArrayList<>(filterValues.size());
					for(String filterValue:filterValues) {
						SelectionValue groupValue = filter.getSelectionValues().get(filterValue);
						if (groupValue != null) {
							groupNames.add(groupValue.getValue());
						}
					}
					if (!groupNames.isEmpty()) {
						return groupNames.stream().collect(Collectors.joining(", "));
					}
				}
			}
		}
		return null;
	}
	
	private void doCheckedExportPDF(UserRequest ureq) {
		try {
			ICourse course = CourseFactory.loadCourse(courseOres);
			
			String name = courseNode.getShortTitle();
			CheckedPDFExport pdfExport = new CheckedPDFExport(name, getTranslator(), withScore, userPropertyHandlers);
			pdfExport.setAuthor(userManager.getUserDisplayName(getIdentity()));
			pdfExport.setCourseNodeTitle(courseNode.getShortTitle());
			pdfExport.setCourseTitle(course.getCourseTitle());
			pdfExport.create(checkboxList, model.getObjects());
			ureq.getDispatchResult().setResultingMediaResource(pdfExport);
		} catch (IOException | TransformerException e) {
			logError("", e);
		}
	}
	
	private void doOpenIdentity(UserRequest ureq, CheckListAssessmentRow row) {
		String businessPath = "[Identity:" + row.getIdentityKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenEdit(UserRequest ureq, CheckListAssessmentRow row) {
		if(guardModalController(editCtrl)) return;
		
		ICourse course = CourseFactory.loadCourse(courseOres);
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
		editCtrl = new AssessedIdentityOverviewController(ureq, getWindowControl(), assessedIdentity,
				courseOres, coachCourseEnv, assessedUserCourseEnv, courseNode);
		listenTo(editCtrl);

		String title = courseNode.getShortTitle();
		Component content = editCtrl.getInitialComponent();
		cmc = new CloseableModalController(getWindowControl(), translate("close"), content, true, title);
		listenTo(cmc);
		cmc.activate();
	}
}