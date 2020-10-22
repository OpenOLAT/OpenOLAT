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

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.modules.contacttracing.ContactTracingSearchParams;

/**
 * Initial date: 15.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingReportGeneratorStep1Controller extends StepFormBasicController {

    private TextElement locationFullTextSearchEl;
    private TextElement referenceEl;
    private TextElement titleEl;
    private TextElement buildingEl;
    private TextElement roomEl;
    private TextElement sectorEl;
    private TextElement tableEl;
    private DateChooser startDateEl;
    private DateChooser endDateEl;

    ContactTracingSearchParams searchParams;

    private final ContactTracingReportGeneratorContextWrapper contextWrapper;

    public ContactTracingReportGeneratorStep1Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
        super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);

        contextWrapper = (ContactTracingReportGeneratorContextWrapper) getFromRunContext("data");
        searchParams = new ContactTracingSearchParams();

        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        locationFullTextSearchEl = uifactory.addTextElement("contact.tracing.report.generator.search.location", -1, null, formLayout);
        referenceEl = uifactory.addTextElement("contact.tracing.cols.reference", -1, null, formLayout);
        titleEl = uifactory.addTextElement("contact.tracing.cols.title", -1, null, formLayout);
        buildingEl = uifactory.addTextElement("contact.tracing.cols.building", -1, null, formLayout);
        roomEl = uifactory.addTextElement("contact.tracing.cols.room", -1, null, formLayout);
        sectorEl = uifactory.addTextElement("contact.tracing.cols.sector", -1, null, formLayout);
        tableEl = uifactory.addTextElement("contact.tracing.cols.table", -1, null, formLayout);

        // Calendar to generate dates
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        startDateEl = uifactory.addDateChooser("contact.tracing.report.generator.search.date.start", calendar.getTime(), formLayout);
        startDateEl.setMandatory(true);
        startDateEl.setNotEmptyCheck("contact.tracing.required");
        startDateEl.setDateChooserTimeEnabled(true);

        // Set end date
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);

        endDateEl = uifactory.addDateChooser("contact.tracing.report.generator.search.date.end", calendar.getTime(), formLayout);
        endDateEl.setDateChooserTimeEnabled(true);
        endDateEl.setDefaultTimeAtEndOfDay(true);
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean allOk =  super.validateFormLogic(ureq);

        allOk &= validateFormItem(locationFullTextSearchEl);
        allOk &= validateFormItem(referenceEl);
        allOk &= validateFormItem(titleEl);
        allOk &= validateFormItem(buildingEl);
        allOk &= validateFormItem(roomEl);
        allOk &= validateFormItem(sectorEl);
        allOk &= validateFormItem(tableEl);
        allOk &= validateFormItem(startDateEl);
        allOk &= validateFormItem(endDateEl);

        allOk &= containsFormAnyData();

        searchParams.setFullTextSearch(getValue(locationFullTextSearchEl));
        searchParams.setReference(getValue(referenceEl));
        searchParams.setTitle(getValue(titleEl));
        searchParams.setBuilding(getValue(buildingEl));
        searchParams.setRoom(getValue(roomEl));
        searchParams.setSector(getValue(sectorEl));
        searchParams.setTable(getValue(tableEl));
        searchParams.setStartDate(startDateEl.getDate());
        searchParams.setEndDate(endDateEl.getDate());

        return allOk;
    }

    private boolean containsFormAnyData() {
        boolean containsData =
                getValue(locationFullTextSearchEl) != null ||
                getValue(referenceEl) != null ||
                getValue(titleEl) != null ||
                getValue(buildingEl) != null ||
                getValue(roomEl) != null ||
                getValue(sectorEl) != null ||
                getValue(tableEl) != null;

        if (!containsData) {
            showWarning("contact.tracing.report.generator.empty.form.warning");
        }

        return containsData;
    }

    private String getValue(FormItem formItem) {
        if (formItem instanceof TextElement) {
            if (StringHelper.containsNonWhitespace(((TextElement) formItem).getValue())) {
                return ((TextElement) formItem).getValue();
            }
        }

        return null;
    }

    @Override
    protected void formOK(UserRequest ureq) {
        contextWrapper.setSearchParams(searchParams);

        fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
    }

    @Override
    protected void doDispose() {
        // Nothing to dispose here
    }
}
