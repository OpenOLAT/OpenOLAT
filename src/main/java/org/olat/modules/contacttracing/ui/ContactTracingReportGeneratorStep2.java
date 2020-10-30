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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
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
 * Initial date: 15.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingReportGeneratorStep2 extends BasicStep {

    private PrevNextFinishConfig prevNextFinishConfig;
    private ContactTracingStepContextWrapper contextWrapper;

    public ContactTracingReportGeneratorStep2(UserRequest ureq, ContactTracingStepContextWrapper contextWrapper) {
        super(ureq);
        setI18nTitleAndDescr("contact.tracing.report.generator.step.2.title", "contact.tracing.report.generator.step.2.description");
        setNextStep(Step.NOSTEP);

        this.prevNextFinishConfig = new PrevNextFinishConfig(true, false, true);
        this.contextWrapper = contextWrapper;
    }

    @Override
    public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
        return prevNextFinishConfig;
    }

    @Override
    public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
        stepsRunContext.put("data", contextWrapper);
        return new ContactTracingReportGeneratorStep2Controller(ureq, windowControl, form, stepsRunContext);
    }
    
    
    
    private class ContactTracingReportGeneratorStep2Controller extends StepFormBasicController {

        private final ContactTracingStepContextWrapper contextWrapper;

        private FlexiTableElement tableEl;
        private ContactTracingLocationTableModel tableModel;

        @Autowired
        private ContactTracingManager contactTracingManager;

        public ContactTracingReportGeneratorStep2Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
            super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);

            contextWrapper = (ContactTracingStepContextWrapper) getFromRunContext("data");

            initForm(ureq);

        }

        @Override
        protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
            // Columns
            DefaultFlexiColumnModel referenceColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.reference);
            DefaultFlexiColumnModel titleColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.title);
            DefaultFlexiColumnModel buildingColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.building);
            DefaultFlexiColumnModel roomColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.room);
            DefaultFlexiColumnModel sectorColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.sector);
            DefaultFlexiColumnModel tableColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.table);
            DefaultFlexiColumnModel seatNumberColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.seatNumber);
            DefaultFlexiColumnModel guestColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.guest);
            DefaultFlexiColumnModel registrationsColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.registrations);
            
            // Yes No Cell Renderer
            YesNoCellRenderer yesNoRenderer = new YesNoCellRenderer(getTranslator());
            seatNumberColumn.setCellRenderer(yesNoRenderer);
            guestColumn.setCellRenderer(yesNoRenderer);

            // Columns model
            FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
            columnModel.addFlexiColumnModel(referenceColumn);
            columnModel.addFlexiColumnModel(titleColumn);
            columnModel.addFlexiColumnModel(buildingColumn);
            columnModel.addFlexiColumnModel(roomColumn);
            columnModel.addFlexiColumnModel(sectorColumn);
            columnModel.addFlexiColumnModel(tableColumn);
            columnModel.addFlexiColumnModel(seatNumberColumn);
            columnModel.addFlexiColumnModel(guestColumn);
            columnModel.addFlexiColumnModel(registrationsColumn);

            // Table model
            tableModel = new ContactTracingLocationTableModel(columnModel, contactTracingManager.getLocationsWithRegistrations(contextWrapper.getSearchParams()), getLocale());

            // Table element
            tableEl = uifactory.addTableElement(getWindowControl(), "locationsTable", tableModel, 25, false, getTranslator(), formLayout);
            tableEl.setPageSize(25);
            tableEl.setMultiSelect(true);
            tableEl.setSelectAllEnable(true);
            tableEl.setShowAllRowsEnabled(true);
            tableEl.setCustomizeColumns(false);
            tableEl.setEmtpyTableMessageKey("contact.tracing.location.table.empty");
            tableEl.setAndLoadPersistedPreferences(ureq, ContactTracingReportGeneratorStep2Controller.class.getCanonicalName());
        }

        @Override
        protected boolean validateFormLogic(UserRequest ureq) {
            boolean allOk = super.validateFormLogic(ureq);

            if (tableModel.getObjects() != null && tableModel.getObjects().size() > 0 && tableEl.getMultiSelectedIndex().size() == 0) {
                allOk = false;
                showWarning("contact.tracing.report.empty.table");
            }

            return allOk;
        }

        @Override
        protected void formOK(UserRequest ureq) {
            // Get selected locations
            List<ContactTracingLocation> locations = new ArrayList<>();
            tableEl.getMultiSelectedIndex().forEach(index -> locations.add(tableModel.getObject(index)));

            // Save them in context wrapper
            contextWrapper.setLocations(locations);

            // Fire event
            fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
        }

        @Override
        protected void doDispose() {

        }
    }
}
