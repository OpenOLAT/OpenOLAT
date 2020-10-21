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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.modules.contacttracing.ContactTracingSearchParams;

/**
 * Initial date: 15.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingReportGeneratorStep1Controller extends StepFormBasicController {

    private TextElement locationSearchEl;
    private DateChooser startDateEl;
    private DateChooser endDateEl;

    private final ContactTracingReportGeneratorContextWrapper contextWrapper;

    public ContactTracingReportGeneratorStep1Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
        super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);

        contextWrapper = (ContactTracingReportGeneratorContextWrapper) getFromRunContext("data");

        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        locationSearchEl = uifactory.addTextElement("contact.tracing.report.generator.search.location", -1, null, formLayout);
        locationSearchEl.setMandatory(true);
        locationSearchEl.setNotEmptyCheck("contact.tracing.required");

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

        allOk &= validateFormItem(locationSearchEl);
        allOk &= validateFormItem(startDateEl);
        allOk &= validateFormItem(endDateEl);

        return allOk;
    }

    @Override
    protected void formOK(UserRequest ureq) {
        ContactTracingSearchParams searchParams = new ContactTracingSearchParams();

        searchParams.setFullTextSearch(locationSearchEl.getValue());
        searchParams.setStartDate(startDateEl.getDate());
        searchParams.setEndDate(endDateEl.getDate());

        contextWrapper.setSearchParams(searchParams);

        fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
    }

    @Override
    protected void doDispose() {

    }
}
