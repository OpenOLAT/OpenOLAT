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
package org.olat.repository.ui.author.copy.wizard.general;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.BasicStepCollection;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.course.member.CourseBusinessGroupListController;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.group.ui.main.AbstractBusinessGroupListController;
import org.olat.group.ui.main.BGAccessControlledCellRenderer;
import org.olat.group.ui.main.BGTableItem;
import org.olat.group.ui.main.BusinessGroupListFlexiTableModel;
import org.olat.group.ui.main.BusinessGroupListFlexiTableModel.Cols;
import org.olat.group.ui.main.BusinessGroupNameCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.repository.ui.author.copy.wizard.CopyCourseSteps;
import org.olat.repository.ui.author.copy.wizard.CopyCourseStepsStep;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 21.04.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class GroupStep extends BasicStep {

	public static Step create(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		if (steps.isEditGroups()) {
			return new GroupStep(ureq, stepCollection, steps);
		} else {
			return CourseOverviewStep.create(ureq, steps);
		}
	}
	
	public GroupStep(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		super(ureq);
		
		setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
		setI18nTitleAndDescr("steps.groups.title", null);
		
		if (stepCollection == null) {
			stepCollection = new BasicStepCollection();
			stepCollection.setTitle(getTranslator(), "members.management");
		}
		setStepCollection(stepCollection);
		
		setNextStep(CourseOverviewStep.create(ureq, steps));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, !nextStep().equals(NOSTEP), nextStep().equals(NOSTEP));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new GroupStepController(ureq, windowControl, form, stepsRunContext);
	}
	
	private class GroupStepController extends StepFormBasicController {

		private final String REMOVE_GROUP = "remove_group";
		
		private CopyCourseContext context;
		
		private SingleSelection copyGroupsModeEl;
		
		private FlexiTableElement tableEl;
		private BusinessGroupListFlexiTableModel groupTableModel;
		
		@Autowired
		private BusinessGroupService businessGroupService;
		
		public GroupStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
			
			setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
			setTranslator(Util.createPackageTranslator(CourseBusinessGroupListController.class, getLocale(), getTranslator()));
			setTranslator(Util.createPackageTranslator(AbstractBusinessGroupListController.class, ureq.getLocale(), getTranslator()));
			
			context = (CopyCourseContext) runContext.get(CopyCourseContext.CONTEXT_KEY);
			
			initForm(ureq);
			loadData();
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			
			copyGroupsModeEl.clearError();
			
			if (!copyGroupsModeEl.isOneSelected()) {
				allOk &= false;
				copyGroupsModeEl.setErrorKey("error.select", null);
			}
			
			return allOk;
		}

		@Override
		protected void formOK(UserRequest ureq) {
			context.setCustomGroupCopyType(context.getCopyType(copyGroupsModeEl.getSelectedKey()));
			context.setGroups(groupTableModel.getObjects());
			
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == tableEl) {
				String cmd = event.getCommand();
				if(event instanceof SelectionEvent) {
					SelectionEvent se = (SelectionEvent)event;
					if(se.getIndex() >= 0 && se.getIndex() < groupTableModel.getRowCount()) {
						if(REMOVE_GROUP.equals(cmd)) {
							BusinessGroupRef item = groupTableModel.getObject(se.getIndex());
							Long businessGroupKey = item.getKey();
							groupTableModel.removeBusinessGroup(businessGroupKey);
							tableEl.reset();
						}
					}
				}
			} else if (source == copyGroupsModeEl) {
				if (copyGroupsModeEl.getSelectedKey().equals(CopyType.ignore.name())) {
					tableEl.setVisible(false);
				} else {
					tableEl.setVisible(true);
				}
			}
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			SelectionValue copy = new SelectionValue(CopyType.copy.name(), translate("options.copy"));
			SelectionValue reference = new SelectionValue(CopyType.reference.name(), translate("options.reference"));
			SelectionValue ignore = new SelectionValue(CopyType.ignore.name(), translate("options.ignore"));
			
			SelectionValues copyGroupModes = new SelectionValues(copy, reference, ignore);
			
			copyGroupsModeEl = uifactory.addRadiosHorizontal("groups", formLayout, copyGroupModes.keys(), copyGroupModes.values());
			copyGroupsModeEl.addActionListener(FormEvent.ONCHANGE);
			copyGroupsModeEl.setAllowNoSelection(false);
			
			// Group table
			FlexiTableColumnModel columnsModel 	= FlexiTableDataModelFactory.createFlexiTableColumnModel();
			
			FlexiColumnModel nameCol 			= new DefaultFlexiColumnModel(Cols.name, new BusinessGroupNameCellRenderer());
			FlexiColumnModel idCol 				= new DefaultFlexiColumnModel(false, Cols.key);
			FlexiColumnModel descriptionCol 	= new DefaultFlexiColumnModel(false, Cols.description.i18nHeaderKey(), Cols.description.ordinal(), false, null, FlexiColumnModel.ALIGNMENT_LEFT, new TextFlexiCellRenderer(EscapeMode.antisamy));
			FlexiColumnModel tutorsCol 			= new DefaultFlexiColumnModel(true, Cols.tutorsCount);
			FlexiColumnModel participantsCol 	= new DefaultFlexiColumnModel(true, Cols.participantsCount);
			FlexiColumnModel freePlacesCol 		= new DefaultFlexiColumnModel(true, Cols.freePlaces.i18nHeaderKey(), Cols.freePlaces.ordinal(), true, Cols.freePlaces.name(), FlexiColumnModel.ALIGNMENT_LEFT, new TextFlexiCellRenderer(EscapeMode.none));
			FlexiColumnModel waitingListCol 	= new DefaultFlexiColumnModel(true, Cols.waitingListCount);
			FlexiColumnModel accessCol 			= new DefaultFlexiColumnModel(true, Cols.accessTypes.i18nHeaderKey(), Cols.accessTypes.ordinal(), true, Cols.accessTypes.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGAccessControlledCellRenderer());
			FlexiColumnModel removeCol 			= new DefaultFlexiColumnModel("table.header.remove", Cols.unlink.ordinal(), REMOVE_GROUP, new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.remove"), REMOVE_GROUP), null));
			
			columnsModel.addFlexiColumnModel(idCol, nameCol, descriptionCol, tutorsCol, participantsCol, freePlacesCol, waitingListCol, accessCol, removeCol);
			
			groupTableModel = new BusinessGroupListFlexiTableModel(columnsModel, getLocale());
			
			FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
			sortOptions.setFromColumnModel(true);

			tableEl = uifactory.addTableElement(getWindowControl(), "groups.list", groupTableModel, 20, false, getTranslator(), formLayout);
			tableEl.setSortSettings(sortOptions);
		}
		
		private void loadData() {
			if (context.getCustomGroupCopyType() != null) {
				copyGroupsModeEl.select(context.getCustomGroupCopyType().name(), true);
			} else {
				copyGroupsModeEl.select(CopyType.reference.name(), true);
			}
			
			if (context.getGroups() != null) {
				groupTableModel.setObjects(context.getGroups());
			} else {
				groupTableModel.setObjects(loadGroups(context.getSourceRepositoryEntry()));
			}
			
			tableEl.reset();
		}
		
		protected List<BGTableItem> loadGroups(RepositoryEntry repositoryEntry) {
			BusinessGroupQueryParams params = new BusinessGroupQueryParams();
			params.setTechnicalTypes(List.of(BusinessGroup.BUSINESS_TYPE));
			params.setRepositoryEntry(repositoryEntry);
			
			List<StatisticsBusinessGroupRow> rows = businessGroupService.findBusinessGroupsFromRepositoryEntry(params, getIdentity(), params.getRepositoryEntry());
			List<BGTableItem> items = new ArrayList<>(rows.size());
			for(StatisticsBusinessGroupRow row:rows) {
				BGTableItem item = new BGTableItem(row, null, false, false);
				item.setNumOfOwners(row.getNumOfCoaches());
				item.setNumOfParticipants(row.getNumOfParticipants());
				item.setNumWaiting(row.getNumWaiting());
				item.setNumOfPendings(row.getNumPending());
				items.add(item);
			}
			return items;
		}
		
	}
}
