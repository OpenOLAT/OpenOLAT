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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingManager;
import org.olat.modules.contacttracing.ContactTracingModule;
import org.olat.modules.contacttracing.ContactTracingRegistration;
import org.olat.modules.contacttracing.ContactTracingSearchParams;
import org.olat.user.UserPropertiesConfig;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    ContactTracingManager contactTracingManager;
    @Autowired
    private UserPropertiesConfig userPropertiesConfig;

    public ContactTracingReportController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        setTranslator(userPropertiesConfig.getTranslator(getTranslator()));
        setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator()));
        setTranslator(Util.createPackageTranslator(ContactTracingModule.class, getLocale(), getTranslator()));

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
        ContactTracingStepContextWrapper contextWrapper = new ContactTracingStepContextWrapper();

        // Create first step and finish callback
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
        String dateLog = "Date" +"\t\t\t" + date;
        String issuedByLog = "Issued by" + "\t\t" + name + " - " + userID;
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

    private class FinishedCallback implements StepRunnerCallback {
        @Override
        public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
            ContactTracingStepContextWrapper contextWrapper = (ContactTracingStepContextWrapper) runContext.get("data");
            Map<ContactTracingLocation, List<ContactTracingRegistration>> locationEntryMap = contextWrapper.getLocations()
                    .stream().collect(Collectors.toMap(location -> location, location -> new ArrayList<>()));

            // If no locations provided, don't go on
            if (locationEntryMap.size() == 0) {
                return StepsMainRunController.DONE_UNCHANGED;
            } else {
                ContactTracingSearchParams searchParams = contextWrapper.getSearchParams();

                for (ContactTracingLocation location : locationEntryMap.keySet()) {
                    searchParams.setLocation(location);
                    locationEntryMap.put(location, contactTracingManager.getRegistrations(searchParams));
                }
            }

            // Create excel sheet
            String label = "Contact_tracing_report_" + Formatter.formatDateFilesystemSave(new Date()) + ".xlsx";
            List<String> sheetNames = new ArrayList<String>();
            for (ContactTracingLocation location : locationEntryMap.keySet()) {
				if (StringHelper.containsNonWhitespace(location.getBuilding())) {
					sheetNames.add(location.getReference());
				} else {
					sheetNames.add(translate("contact.tracing.location") + " " + (sheetNames.size() + 1));
				}
			}
            
            OpenXMLWorkbookResource spreadSheet = new OpenXMLWorkbookResource(label) {
                @Override
                protected void generate(OutputStream out) {
                    try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, locationEntryMap.keySet().size(), sheetNames)) {
                        // Generate a new sheet for every location
                        for (ContactTracingLocation location : locationEntryMap.keySet()) {
                            OpenXMLWorksheet sheet = workbook.nextWorksheet();

                            createLocationHeader(sheet, workbook);
                            createLocationData(location, sheet);

                            createRegistrationsHeader(sheet, workbook);
                            createRegistrationsData(locationEntryMap.get(location), sheet, workbook);
                        }
                    } catch (IOException e) {
                        log.error("", e);
                    }
                }
            };

            // Download the resource
            ureq.getDispatchResult().setResultingMediaResource(spreadSheet);

            // Fire event
            return StepsMainRunController.DONE_MODIFIED;
        }
    }

    private static class CancelCallback implements StepRunnerCallback {
        @Override
        public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
            return Step.NOSTEP;
        }
    }

    protected void createLocationHeader(OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
        Row headerRow = sheet.newRow();

        List<String> columns = new ArrayList<>();
        columns.add(translate("contact.tracing.cols.reference"));
        columns.add(translate("contact.tracing.cols.title"));
        columns.add(translate("contact.tracing.cols.building"));
        columns.add(translate("contact.tracing.cols.room"));
        columns.add(translate("contact.tracing.cols.sector"));
        columns.add(translate("contact.tracing.cols.table"));
        columns.add(translate("contact.tracing.cols.qr.id"));

        for (String headerValue : columns) {
            headerRow.addCell(columns.indexOf(headerValue), headerValue, workbook.getStyles().getHeaderStyle());
        }
    }

    protected void createLocationData(ContactTracingLocation location, OpenXMLWorksheet sheet) {
        // Create new row
        Row dataRow = sheet.newRow();

        // Fill row
        int i = 0;
        dataRow.addCell(i++, location.getReference());
        dataRow.addCell(i++, location.getTitle());
        dataRow.addCell(i++, location.getBuilding());
        dataRow.addCell(i++, location.getRoom());
        dataRow.addCell(i++, location.getSector());
        dataRow.addCell(i++, location.getTable());
        dataRow.addCell(i, location.getQrId());

        // Append two rows to separate from content
        sheet.newRow();
        sheet.newRow();
    }

    protected void createRegistrationsHeader(OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
        Row headerRow = sheet.newRow();

        List<String> columns = new ArrayList<>();
        columns.add(translate("contact.tracing.start.time"));
        columns.add(translate("contact.tracing.end.time"));
        columns.add(translate("contact.tracing.cols.seat.number"));
        columns.add(translate(userPropertiesConfig.getPropertyHandler(UserConstants.NICKNAME).i18nFormElementLabelKey()));
        columns.add(translate(userPropertiesConfig.getPropertyHandler(UserConstants.FIRSTNAME).i18nFormElementLabelKey()));
        columns.add(translate(userPropertiesConfig.getPropertyHandler(UserConstants.LASTNAME).i18nFormElementLabelKey()));
        columns.add(translate(userPropertiesConfig.getPropertyHandler(UserConstants.STREET).i18nFormElementLabelKey()));
        columns.add(translate(userPropertiesConfig.getPropertyHandler(UserConstants.EXTENDEDADDRESS).i18nFormElementLabelKey()));
        columns.add(translate(userPropertiesConfig.getPropertyHandler(UserConstants.ZIPCODE).i18nFormElementLabelKey()));
        columns.add(translate(userPropertiesConfig.getPropertyHandler(UserConstants.CITY).i18nFormElementLabelKey()));
        columns.add(translate(userPropertiesConfig.getPropertyHandler(UserConstants.EMAIL).i18nFormElementLabelKey()));
        columns.add(translate(userPropertiesConfig.getPropertyHandler(UserConstants.INSTITUTIONALEMAIL).i18nFormElementLabelKey()));
        columns.add(translate(userPropertiesConfig.getPropertyHandler("genericEmailProperty1").i18nFormElementLabelKey()));
        columns.add(translate(userPropertiesConfig.getPropertyHandler(UserConstants.TELMOBILE).i18nFormElementLabelKey()));
        columns.add(translate(userPropertiesConfig.getPropertyHandler(UserConstants.TELPRIVATE).i18nFormElementLabelKey()));
        columns.add(translate(userPropertiesConfig.getPropertyHandler(UserConstants.TELOFFICE).i18nFormElementLabelKey()));

        for (String headerValue : columns) {
            headerRow.addCell(columns.indexOf(headerValue), headerValue, workbook.getStyles().getHeaderStyle());
        }
    }

    protected void createRegistrationsData(List<ContactTracingRegistration> registrations, OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
        for (ContactTracingRegistration registration : registrations) {
            // Create new row
            Row dataRow = sheet.newRow();

            // Fill row
            int i = 0;
            dataRow.addCell(i++, registration.getStartDate(), workbook.getStyles().getDateTimeStyle());
            dataRow.addCell(i++, registration.getEndDate(), workbook.getStyles().getDateTimeStyle());
            dataRow.addCell(i++, registration.getSeatNumber());
            dataRow.addCell(i++, registration.getNickName());
            dataRow.addCell(i++, registration.getFirstName());
            dataRow.addCell(i++, registration.getLastName());
            dataRow.addCell(i++, registration.getStreet());
            dataRow.addCell(i++, registration.getExtraAddressLine());
            dataRow.addCell(i++, registration.getZipCode());
            dataRow.addCell(i++, registration.getCity());
            dataRow.addCell(i++, registration.getEmail());
            dataRow.addCell(i++, registration.getInstitutionalEmail());
            dataRow.addCell(i++, registration.getGenericEmail());
            dataRow.addCell(i++, registration.getMobilePhone());
            dataRow.addCell(i++, registration.getPrivatePhone());
            dataRow.addCell(i, registration.getOfficePhone());

        }
    }
}
