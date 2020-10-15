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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.editor.overview.YesNoCellRenderer;
import org.olat.modules.contacttracing.ContactTracingManager;
import org.olat.modules.contacttracing.ui.ContactTracingLocationTableModel.ContactTracingLocationCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 15.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingReportGeneratorStep2Controller extends StepFormBasicController {

    private final ContactTracingReportGeneratorContextWrapper contextWrapper;

    private FlexiTableElement tableEl;
    private ContactTracingLocationTableModel tableModel;

    @Autowired
    private ContactTracingManager contactTracingManager;

    public ContactTracingReportGeneratorStep2Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
        super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);

        contextWrapper = (ContactTracingReportGeneratorContextWrapper) getFromRunContext("data");

        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        // Columns
        DefaultFlexiColumnModel referenceColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.reference);
        DefaultFlexiColumnModel titleColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.title);
        DefaultFlexiColumnModel roomColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.room);
        DefaultFlexiColumnModel buildingColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.building);
        DefaultFlexiColumnModel registrationsColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.registrations);

        // Columns model
        FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
        columnModel.addFlexiColumnModel(referenceColumn);
        columnModel.addFlexiColumnModel(titleColumn);
        columnModel.addFlexiColumnModel(roomColumn);
        columnModel.addFlexiColumnModel(buildingColumn);
        columnModel.addFlexiColumnModel(registrationsColumn);

        // Table model
        tableModel = new ContactTracingLocationTableModel(columnModel, contactTracingManager.getLocations(), contactTracingManager);
        tableModel.filter(contextWrapper.getLocationSearch(), null);

        // Table element
        tableEl = uifactory.addTableElement(getWindowControl(), "locationsTable", tableModel, getTranslator(), formLayout);
        tableEl.setPageSize(20);
        tableEl.setMultiSelect(true);
        tableEl.setSelectAllEnable(true);
        tableEl.setShowAllRowsEnabled(true);
        tableEl.setCustomizeColumns(false);
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean allOk =  super.validateFormLogic(ureq);


        return allOk;
    }

    @Override
    protected void formOK(UserRequest ureq) {
        fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
    }

    @Override
    protected void doDispose() {

    }
}
