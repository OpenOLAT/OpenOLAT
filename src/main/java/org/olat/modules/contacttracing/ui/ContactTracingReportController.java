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

import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * Initial date: 12.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingReportController extends FormBasicController {

    private static final Logger log = Tracing.createLoggerFor(ContactTracingReportController.class);

    private StaticTextElement generatedByEl;
    private StaticTextElement dateEl;
    private TextElement authorizedByEl;
    private TextElement reasonEl;

    private StepsMainRunController generateReportStepController;

    public ContactTracingReportController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);

        initForm(ureq);
        loadData(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        // Add description
        setFormDescription("contact.tracing.report.description");

        // Add form elements
        generatedByEl = uifactory.addStaticTextElement("contact.tracing.report.generated.by", null, formLayout);
        dateEl = uifactory.addStaticTextElement("contact.tracing.report.date", null, formLayout);
        authorizedByEl = uifactory.addTextElement("contact.tracing.report.authorized.by", 255, null, formLayout);
        authorizedByEl.setNotEmptyCheck("contact.tracing.required");
        authorizedByEl.setMandatory(true);
        reasonEl = uifactory.addTextElement("contact.tracing.report.reason", 500, null, formLayout);
        reasonEl.setNotEmptyCheck("contact.tracing.required");
        reasonEl.setMandatory(true);

        uifactory.addFormSubmitButton("contact.tracing.report.start", formLayout);
    }

    private void loadData(UserRequest ureq) {
        String generatedBy = new StringBuilder()
                .append(ureq.getUserSession().getIdentity().getUser().getFirstName())
                .append(" ")
                .append(ureq.getUserSession().getIdentity().getUser().getLastName())
                .append(" - ")
                .append(ureq.getUserSession().getIdentity().getUser().getEmail())
                .toString();
        generatedByEl.setValue(generatedBy);

        Date date = new Date();
        String currentDate = StringHelper.formatLocaleDateTime(date.getTime(), getLocale());
        dateEl.setValue(currentDate);
    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
       if(source == generateReportStepController) {
            if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
                // Close the dialog
                getWindowControl().pop();

                // Remove steps controller
                removeAsListenerAndDispose(generateReportStepController);
                generateReportStepController = null;

                // Reload form
                loadData(ureq);

                // Reset entries
                authorizedByEl.setValue(null);
                reasonEl.setValue(null);
            }
        }
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        // To refresh the date
        loadData(ureq);

        boolean allOk = super.validateFormLogic(ureq);

        allOk &= validateFormItem(authorizedByEl);
        allOk &= validateFormItem(reasonEl);

        return allOk;
    }

    @Override
    protected void formOK(UserRequest ureq) {
        openReportGeneratorSteps(ureq);
        logReportAccess(ureq);
    }

    private void openReportGeneratorSteps(UserRequest ureq) {
        // Create context wrapper (used to transfer data from step to step)
        ContactTracingReportGeneratorContextWrapper contextWrapper = new ContactTracingReportGeneratorContextWrapper();

        // Create first step
        Step searchStep = new ContactTracingReportGeneratorStep1(ureq, contextWrapper);
        FinishedCallback finish = new FinishedCallback();
        CancelCallback cancel = new CancelCallback();
        // Create step controller
        generateReportStepController = new StepsMainRunController(ureq, getWindowControl(), searchStep, finish, cancel, translate("contact.tracing.report.generator.title"), null);
        listenTo(generateReportStepController);
        getWindowControl().pushAsModalDialog(generateReportStepController.getInitialComponent());
    }

    private void logReportAccess(UserRequest ureq) {
        String userID = ureq.getUserSession().getIdentity().getUser().getKey().toString();
        String name = generatedByEl.getValue();
        String date = dateEl.getValue();
        String authorizedBy = authorizedByEl.getValue();
        String reason = reasonEl.getValue();

        String startLog = "Contact tracing report has been generated:";
        String dateLog = "Date" +"\t\t\t\t" + date;
        String issuedByLog = "Issued by" + "\t\t\t" + name + " - " + userID;
        String authorizedByLog = "Authorized by" + "\t\t" + authorizedBy;
        String reasonLog = "Reason / Details" + "\t" + reason;

        log.info(Tracing.M_AUDIT, startLog);
        log.info(Tracing.M_AUDIT, dateLog);
        log.info(Tracing.M_AUDIT, issuedByLog);
        log.info(Tracing.M_AUDIT, authorizedByLog);
        log.info(Tracing.M_AUDIT, reasonLog);
    }

    @Override
    protected void doDispose() {

    }

    private static class FinishedCallback implements StepRunnerCallback {
        @Override
        public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {

            return StepsMainRunController.DONE_MODIFIED;
        }
    }

    private static class CancelCallback implements StepRunnerCallback {
        @Override
        public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {

            return Step.NOSTEP;
        }
    }
}
