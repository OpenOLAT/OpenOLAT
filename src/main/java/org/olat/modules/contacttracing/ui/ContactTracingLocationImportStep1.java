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
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.ExcelMediaResource;
import org.olat.core.gui.translator.TranslatorHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingManager;
import org.olat.modules.contacttracing.model.ContactTracingLocationImpl;
import org.olat.modules.contacttracing.ui.ContactTracingStepContextWrapper.QrIdGenerationMode;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Oct 28, 2020<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingLocationImportStep1 extends BasicStep {
	
	private PrevNextFinishConfig prevNextFinishConfig;
    private ContactTracingStepContextWrapper contextWrapper;

    public ContactTracingLocationImportStep1(UserRequest ureq, ContactTracingStepContextWrapper contextWrapper) {
        super(ureq);
        setI18nTitleAndDescr("contact.tracing.locations.import.step.1.title", null);
        setNextStep(new ContactTracingLocationImportStep2(ureq, contextWrapper));

        this.prevNextFinishConfig = new PrevNextFinishConfig(false, true, false);
        this.contextWrapper = contextWrapper;
    }

    @Override
    public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
        return prevNextFinishConfig;
    }

    @Override
    public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext stepsRunContext, Form form) {
        stepsRunContext.put("data", contextWrapper);
        return new ContactTracingLocationImportStep1Controller(ureq, wControl, form, stepsRunContext);
    }
    
    
    
    private class ContactTracingLocationImportStep1Controller extends StepFormBasicController {
    	
    	private final String[] qrIdGenerationKeys = new String[] {"contact.tracing.location.edit.generate.numeric.identifier", "contact.tracing.location.edit.generate.human.readable.identifier"};
    	private final String[] qrIdGenerationValues;
    	
    	private FormLink templateDownloadLink;    
    	private SingleSelection qrIdGenerationPreferenceEl;
    	private TextAreaElement excelTextElement;
    	
    	@Autowired
    	private ContactTracingManager contactTracingManager;

		public ContactTracingLocationImportStep1Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT_2_10, null);

			qrIdGenerationValues = TranslatorHelper.translateAll(getTranslator(), qrIdGenerationKeys);
            contextWrapper = (ContactTracingStepContextWrapper) getFromRunContext("data");

            initForm(ureq);
		}
		
		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormDescription("contact.tracing.locations.import.instructions", new String[] {"on", "off", "on", "off"});
			
			// Add template download link
			templateDownloadLink = uifactory.addFormLink("templateDownloadLink", "contact.tracing.locations.import.template.link", "contact.tracing.locations.import.template.link.label", formLayout, Link.LINK);
			templateDownloadLink.setIconLeftCSS("o_icon o_filetype_xls o_icon-lg");
			
			// Add selection of qr id generation
			qrIdGenerationPreferenceEl = uifactory.addDropdownSingleselect("qrIdGeneration", "contact.tracing.cols.qr.id", formLayout, qrIdGenerationKeys, qrIdGenerationValues);
			
			// Add instruction sample
			String page = Util.getPackageVelocityRoot(getClass()) + "/contact_tracing_import_instructions.html";
			FormLayoutContainer instructions = FormLayoutContainer.createCustomFormLayout("instructions", getTranslator(), page);
			instructions.setLabel("contact.tracing.locations.import.instructions.label", null);
			formLayout.add(instructions);
			
			
			// Add input element for locations
			excelTextElement = uifactory.addTextAreaElement("importElement", "contact.tracing.locations", -1, 10, -1, true, true, true, null, formLayout);
			excelTextElement.setNotEmptyCheck("contact.tracing.locations.import.mandatory");
			excelTextElement.setMandatory(true);
			excelTextElement.setElementCssClass("o_textarea_line_numbers");
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == templateDownloadLink) {
				ureq.getDispatchResult().setResultingMediaResource(createExcelTemplate(ureq));
			}
		}
		
		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			
			if (qrIdGenerationPreferenceEl.getSelectedKey().equals(qrIdGenerationKeys[0])) {
				contextWrapper.setQrIdGenerationMode(QrIdGenerationMode.numeric);
			} else if (qrIdGenerationPreferenceEl.getSelectedKey().equals(qrIdGenerationKeys[1])) {
				contextWrapper.setQrIdGenerationMode(QrIdGenerationMode.humanReadable);
			}
			
			allOk &= validateExcelInput(excelTextElement);
			
			return allOk;
		}
		
		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);			
		}

		@Override
		protected void doDispose() {
			// Nothing to dispose here
		}
		
		private boolean validateExcelInput(TextAreaElement inputEl) {
			boolean allOk = validateFormItem(inputEl);
			
			if (allOk == false || !StringHelper.containsNonWhitespace(inputEl.getValue())) {
				return false;
			}
			
			String[] lines = inputEl.getValue().split("\r?\n");
			List<ContactTracingLocation> locationList = new ArrayList<>();
			
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				
				if (!StringHelper.containsNonWhitespace(line)) {
					continue;
				}
				
				String delimiter = "\t";
				String[] trueArray = new String[] {"1", "on"}; 
				// Use comma as fallback delimiter, e.g. for better testing
				if (line.indexOf(delimiter) == -1) {
					delimiter = ",";
				}
				
				String[] columns = line.split(delimiter);
				
				if (columns.length != 10) {
					allOk = false;
					inputEl.setErrorKey("contact.tracing.locations.import.excel.error", new String[] {String.valueOf(i + 1)});
					break;
				}
				
				ContactTracingLocation location = new ContactTracingLocationImpl();
				location.setReference(StringHelper.xssScan(columns[0]));
				location.setTitle(StringHelper.xssScan(columns[1]));
				location.setBuilding(StringHelper.xssScan(columns[2]));
				location.setRoom(StringHelper.xssScan(columns[3]));
				location.setSector(StringHelper.xssScan(columns[4]));
				location.setTable(StringHelper.xssScan(columns[5]));
				location.setSeatNumberEnabled(Arrays.stream(trueArray).anyMatch(columns[6]::contains));
				location.setAccessibleByGuests(Arrays.stream(trueArray).anyMatch(columns[7]::contains));
				location.setQrId(StringHelper.xssScan(columns[8]));
				location.setQrText(StringHelper.xssScan(columns[9]));
				
				// If QR ID is empty generate one
				if (!StringHelper.containsNonWhitespace(location.getQrId())) {
					switch(contextWrapper.getQrIdGenerationMode()) {
						case humanReadable:
							generateHumanReadableID(locationList, location);
							break;
						case numeric:
						default:
							generateNumericQrID(locationList, location);
							break;
					}
				}
				
				locationList.add(location);
			}
			
			contextWrapper.setLocations(locationList);
			
			return allOk;
		}
		
		private void generateNumericQrID(List<ContactTracingLocation> newLocations, ContactTracingLocation location) {
	        Random generator = new Random();
	        String qrId;

	        do {
	            qrId = String.valueOf(generator.nextInt(80000) + 10000);
	        } while (qrIdExists(newLocations, qrId));
	        
	        location.setQrId(qrId);
	    }

	    private void generateHumanReadableID(List<ContactTracingLocation> newLocations, ContactTracingLocation location) {
	        // Try with reference
	    	String qrId = transformStringToIdentifier(location.getReference());
	    	
	        if (qrIdExists(newLocations, qrId)) {
	            StringBuilder qrIdBuilder = new StringBuilder();

	            // Try with building-room-sector-table
	            qrIdBuilder.append(transformStringToIdentifier(location.getBuilding()))
	                    .append(location.getBuilding().length() > 0 ? "-" : "")
	                    .append(transformStringToIdentifier(location.getRoom()))
	                    .append(location.getRoom().length() > 0 ? "-" : "")
	                    .append(transformStringToIdentifier(location.getSector()))
	                    .append(location.getSector().length() > 0 ? "-" : "")
	                    .append(transformStringToIdentifier(location.getTable()));

	            // Replace last _ if existing
	            qrId = qrIdBuilder.toString();
	            if (qrId.endsWith("-")) {
	                qrId = qrId.substring(0, qrId.length() - 1);
	            }

	            if (qrIdExists(newLocations, qrId)) {
	                // Try with title
	            	qrId = transformStringToIdentifier(location.getTitle());
	                if (qrIdExists(newLocations, qrId)) {
	                    generateNumericQrID(newLocations, location);
	                    return;
	                }
	            }
	        }
	        
	        location.setQrId(qrId);
	    }
	    
	    private boolean qrIdExists(List<ContactTracingLocation> newLocations, String qrId) {
	    	if (contactTracingManager.qrIdExists(qrId)) {
	    		return true;
	    	}
	    	
	    	for (ContactTracingLocation location : newLocations) {
				if (location.getQrId().equals(qrId)) {
					return true;
				}
			}
	    	
	    	return false;
	    }
	    
	    private String transformStringToIdentifier(String identifier) {
	        if (!StringHelper.containsNonWhitespace(identifier)) {
	            return "";
	        }

	        identifier = StringHelper.transformDisplayNameToFileSystemName(identifier);
	        if (identifier.equals("_")) {
	            return "";
	        }

	        return identifier;
	    }
		
		/**
		 * Creates a template to import contact tracing locations
		 * 
		 * @param ureq
		 * @return A ExcelMediaRessource template file
		 */
		private ExcelMediaResource createExcelTemplate(UserRequest ureq) {
			String charset = UserManager.getInstance().getUserCharset(ureq.getIdentity());
			StringBuilder headerRow = new StringBuilder();
			StringBuilder dataRow = new StringBuilder();
			
			// Add columns
			// Reference
			headerRow.append(translate("contact.tracing.cols.reference")).append("\t");
			dataRow.append("fx C1").append("\t");
			
			// Title
			headerRow.append(translate("contact.tracing.cols.title")).append("\t");
			dataRow.append("Small conference room").append("\t");
			
			// Building
			headerRow.append(translate("contact.tracing.cols.building")).append("\t");
			dataRow.append("frentix Office").append("\t");
			
			// Room
			headerRow.append(translate("contact.tracing.cols.room")).append("\t");
			dataRow.append("Conf 1").append("\t");
			
			// Sector
			headerRow.append(translate("contact.tracing.cols.sector")).append("\t");
			dataRow.append("A").append("\t");
			
			// Table
			headerRow.append(translate("contact.tracing.cols.table")).append("\t");
			dataRow.append("4").append("\t");
			
			// Seat number
			headerRow.append(translate("contact.tracing.cols.seat.number")).append("\t");
			dataRow.append("off").append("\t");
			
			// Guests allowed
			headerRow.append(translate("contact.tracing.cols.guest")).append("\t");
			dataRow.append("on").append("\t");
			
			// QR ID
			headerRow.append(translate("contact.tracing.cols.qr.id")).append("\t");
			dataRow.append("").append("\t");
			
			// QR Text
			headerRow.append(translate("contact.tracing.cols.qr.text"));
			dataRow.append("<p style='font-size: 18px; font-weight: bold;'>Please disinfect the table after using this room!</p>");
			
			// Concatenate header and data row
			StringBuilder writeToFile = new StringBuilder();
			writeToFile.append(headerRow).append("\n").append(dataRow);
			
			ExcelMediaResource emr = new ExcelMediaResource(writeToFile.toString(), charset);
			emr.setFilename("ContactTracingImportTemplate");
			return emr;
		}
    }
}
