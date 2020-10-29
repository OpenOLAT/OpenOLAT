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
package org.olat.modules.contacttracing.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.editor.overview.YesNoCellRenderer;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingManager;
import org.olat.modules.contacttracing.ui.ContactTracingLocationTableModel.ContactTracingLocationCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Oct 28, 2020<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingLocationImportStep2 extends BasicStep {
	
	private PrevNextFinishConfig prevNextFinishConfig;
    private ContactTracingStepContextWrapper contextWrapper;

    public ContactTracingLocationImportStep2(UserRequest ureq, ContactTracingStepContextWrapper contextWrapper) {
        super(ureq);
        setI18nTitleAndDescr("contact.tracing.locations.import.step.2.title", null);
        setNextStep(Step.NOSTEP);

        this.prevNextFinishConfig = new PrevNextFinishConfig(true, false, true);
        this.contextWrapper = contextWrapper;
    }

    @Override
    public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
        return prevNextFinishConfig;
    }

    @Override
    public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext stepsRunContext, Form form) {
        stepsRunContext.put("data", contextWrapper);
        return new ContactTracingLocationImportStep2Controller(ureq, wControl, form, stepsRunContext);
    }
    
    
    
    private class ContactTracingLocationImportStep2Controller extends StepFormBasicController {
    	
    	private FlexiTableElement tableEl;
    	private ContactTracingLocationTableModel tableModel;
    	
    	@Autowired
        private ContactTracingManager contactTracingManager;

		public ContactTracingLocationImportStep2Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);

            contextWrapper = (ContactTracingStepContextWrapper) getFromRunContext("data");

            initForm(ureq);
            loadData();
		}
		
		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			// Columns
	        DefaultFlexiColumnModel referenceColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.reference);
	        DefaultFlexiColumnModel titleColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.title);
	        DefaultFlexiColumnModel roomColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.room);
	        DefaultFlexiColumnModel buildingColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.building);
	        DefaultFlexiColumnModel qrIdColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.qrId);
	        
	        // Already existing column
	        ContactTracingWarningCellRenderer warningCellRenderer = new ContactTracingWarningCellRenderer();
	        DefaultFlexiColumnModel alreadyExistingColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.alreadyExisting);
	        alreadyExistingColumn.setIconHeader(ContactTracingLocationCols.alreadyExisting.iconHeader());
	        alreadyExistingColumn.setCellRenderer(warningCellRenderer);

	        // Url column
	        DefaultFlexiColumnModel urlColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.url);
	        urlColumn.setDefaultVisible(false);

	        // QR text column
	        DefaultFlexiColumnModel qrTextColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.qrText);
	        TextFlexiCellRenderer textFlexiCellRenderer = new TextFlexiCellRenderer(EscapeMode.antisamy);
	        qrTextColumn.setDefaultVisible(false);
	        qrTextColumn.setCellRenderer(textFlexiCellRenderer);
	        
	        // Sector column
	        DefaultFlexiColumnModel sectorColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.sector);
	        sectorColumn.setDefaultVisible(false);
	        
	        // Table column
	        DefaultFlexiColumnModel tableColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.table);
	        tableColumn.setDefaultVisible(false);
	        
	        // Seat number column
	        DefaultFlexiColumnModel seatNumberColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.seatNumber);
	        seatNumberColumn.setCellRenderer(new YesNoCellRenderer(getTranslator()));

	        // Guest column
	        DefaultFlexiColumnModel guestColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.guest);
	        guestColumn.setCellRenderer(new YesNoCellRenderer(getTranslator()));

	        // Columns model
	        FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
	        columnModel.addFlexiColumnModel(alreadyExistingColumn);
	        columnModel.addFlexiColumnModel(referenceColumn);
	        columnModel.addFlexiColumnModel(titleColumn);
	        columnModel.addFlexiColumnModel(buildingColumn);
	        columnModel.addFlexiColumnModel(roomColumn);
	        columnModel.addFlexiColumnModel(sectorColumn);
	        columnModel.addFlexiColumnModel(tableColumn);
	        columnModel.addFlexiColumnModel(seatNumberColumn);
	        columnModel.addFlexiColumnModel(qrIdColumn);
	        columnModel.addFlexiColumnModel(urlColumn);
	        columnModel.addFlexiColumnModel(qrTextColumn);
	        columnModel.addFlexiColumnModel(guestColumn);

	        // Table model
	        tableModel = new ContactTracingLocationTableModel(columnModel, contextWrapper.getLocations(), getLocale());

	        // Table element
	        tableEl = uifactory.addTableElement(getWindowControl(), "locationsTable", tableModel, getTranslator(), formLayout);
	        tableEl.setPageSize(25);
	        tableEl.setExportEnabled(false);
	        tableEl.setSearchEnabled(true);
	        tableEl.setMultiSelect(true);
	        tableEl.setSelectAllEnable(true);
	        tableEl.setShowAllRowsEnabled(true);
	        tableEl.setEmptyTableSettings("contact.tracing.location.table.empty", false);
	        tableEl.setAndLoadPersistedPreferences(ureq, ContactTracingLocationImportStep2.class.getCanonicalName());
		}
		
		private void loadData() {
			Map<ContactTracingLocation, Boolean> alreadyExistingMap = new HashMap<>();
			contextWrapper.getLocations().forEach(location -> alreadyExistingMap.put(location, contactTracingManager.qrIdExists(location.getQrId())));
			
			tableModel.setAlreadyExistingMap(alreadyExistingMap);
			tableEl.reset();
			
			Set<Integer> newLocationIndices = new HashSet<>();
			List<ContactTracingLocation> tableElements = tableModel.getObjects();
			
			for (ContactTracingLocation location : tableElements) {
				if (!alreadyExistingMap.get(location)) {
					newLocationIndices.add(tableElements.indexOf(location));
				}
			}
			
			tableEl.setMultiSelectedIndex(newLocationIndices);
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == tableEl) {
				if (event instanceof FlexiTableSearchEvent) {
		            String searchString = ((FlexiTableSearchEvent) event).getSearch();
		            tableModel.filter(searchString, null);
		            tableEl.reset();
		        }
			}
		}
		
		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			
			if (tableModel.getRowCount() != 0 && tableEl.getMultiSelectedIndex().isEmpty()) {
				allOk = false;
				showWarning("contact.tracing.import.selection.warning");
			}
			
			return allOk;
		}
		
		@Override
		protected void formOK(UserRequest ureq) {
			// Get selected locations and put them into context
			List<ContactTracingLocation> selectedLocationList = new ArrayList<>();
			List<ContactTracingLocation> locationList = tableModel.getObjects();
			
			tableEl.getMultiSelectedIndex().forEach(index -> selectedLocationList.add(locationList.get(index)));
			
			contextWrapper.setLocations(selectedLocationList);
			
			// Fire event
            fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void doDispose() {
			// TODO Auto-generated method stub
			
		}
    }
}