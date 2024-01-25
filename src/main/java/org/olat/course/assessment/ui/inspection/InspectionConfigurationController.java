/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.inspection;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.course.assessment.ui.inspection.elements.ResultsDisplayCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InspectionConfigurationController extends StepFormBasicController {

	private FormToggle accessCodeEl;
	private StaticTextElement durationEl;
	private DateChooser inspectionPeriodEl;
	private SingleSelection configurationEl;
	private StaticTextElement configurationInfosEl;
	
	private final CreateInspectionContext context;
	private final List<AssessmentInspectionConfiguration> configurations;

	@Autowired
	private AssessmentInspectionService inspectionService;
	
	public InspectionConfigurationController(UserRequest ureq, WindowControl wControl,
			CreateInspectionContext context, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		
		
		this.context = context;
		configurations = inspectionService.getInspectionConfigurations(context.getCourseEntry());
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		SelectionValues configurationKV = new SelectionValues();
		for(AssessmentInspectionConfiguration configuration:configurations) {
			configurationKV.add(SelectionValues.entry(configuration.getKey().toString(), configuration.getName()));
		}
		
		configurationEl = uifactory.addDropdownSingleselect("configuration", "inspection.configuration", formLayout,
				configurationKV.keys(), configurationKV.values());
		configurationEl.addActionListener(FormEvent.ONCHANGE);
		configurationEl.setMandatory(true);
		if(context.getEditedInspection() != null && configurationKV.containsKey(context.getEditedInspection().getConfiguration().getKey().toString()) ) {
			configurationEl.select(context.getEditedInspection().getConfiguration().getKey().toString(), true);
		} else if(!configurations.isEmpty()) {
			AssessmentInspectionConfiguration firstConfiguration = configurations.get(0);
			configurationEl.select(firstConfiguration.getKey().toString(), true);
		}
		
		durationEl = uifactory.addStaticTextElement("configuration.duration", "configuration.duration", "", formLayout);
		
		inspectionPeriodEl = uifactory.addDateChooser("configuration.inspection.period", "configuration.inspection.period", null, formLayout);
		inspectionPeriodEl.setSecondDate(true);
		inspectionPeriodEl.setDateChooserTimeEnabled(true);
		inspectionPeriodEl.setSeparator("configuration.inspection.period.separator");
		inspectionPeriodEl.setMandatory(true);
		if(context.getEditedInspection() != null) {
			inspectionPeriodEl.setDate(context.getEditedInspection().getFromDate());
			inspectionPeriodEl.setSecondDate(context.getEditedInspection().getToDate());
		} else {
			setDefaultFromToDates(ureq.getRequestTimestamp());
		}
		
		accessCodeEl = uifactory.addToggleButton("configuration.access.code", "configuration.access.code", translate("on"), translate("off"), formLayout);
		if(context.getEditedInspection() != null && StringHelper.containsNonWhitespace(context.getEditedInspection().getAccessCode())) {
			accessCodeEl.toggleOn();
		}
	
		configurationInfosEl = uifactory.addStaticTextElement("configuration.infos", null, "", formLayout);
		configurationInfosEl.setDomWrapperElement(DomWrapperElement.div);
	}
	
	private void setDefaultFromToDates(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MINUTE, 0);  
		cal.set(Calendar.SECOND, 0);  
		cal.set(Calendar.MILLISECOND, 0);  
		cal.add(Calendar.HOUR_OF_DAY, 1);
		inspectionPeriodEl.setDate(cal.getTime());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		configurationEl.clearError();
		if(!configurationEl.isOneSelected()) {
			configurationEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		inspectionPeriodEl.clearError();
		if(inspectionPeriodEl.getDate() == null || inspectionPeriodEl.getSecondDate() == null) {
			inspectionPeriodEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(inspectionPeriodEl.getDate() != null && inspectionPeriodEl.getSecondDate() != null) {
			if(inspectionPeriodEl.getSecondDate().before(inspectionPeriodEl.getDate())) {
				inspectionPeriodEl.setErrorKey("error.from.to.date");
				allOk &= false;
			} else if(inspectionPeriodEl.getSecondDate().before(ureq.getRequestTimestamp())) {
				inspectionPeriodEl.setErrorKey("error.from.to.date.in.past");
				allOk &= false;
			}
		}
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(configurationEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateUI() {
		AssessmentInspectionConfiguration configuration = getSelectedConfiguration();
		if(configuration == null) {
			durationEl.setValue("");
			configurationInfosEl.setValue("");
		} else {
			int durationInMinutes = configuration.getDuration() / 60;
			durationEl.setValue(translate("duration.cell", Integer.toString(durationInMinutes)));
			configurationInfosEl.setValue(getConfigurationInfos(configuration));
			
			Date fromDate = inspectionPeriodEl.getDate();
			if(fromDate != null && context.getEditedInspection() == null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(fromDate);
				cal.add(Calendar.MINUTE, durationInMinutes);
				inspectionPeriodEl.setSecondDate(cal.getTime());
			}
		}
	}
	
	private String getConfigurationInfos(AssessmentInspectionConfiguration configuration) {
		int durationInMinutes = configuration.getDuration() / 60;
		StringBuilder sb = new StringBuilder(1000);
		sb.append("<div class='o_info_with_icon'><p>")
		  .append(translate("configuration.infos.title", StringHelper.escapeHtml(configuration.getName()))).append("</p><ul>")
		  .append("<li>").append(translate("configuration.infos.duration", Integer.toString(durationInMinutes))).append("</li>");
		
		List<String> options = configuration.getOverviewOptionsAsList();
		ResultsDisplayCellRenderer renderer = new ResultsDisplayCellRenderer(getTranslator());
		List<String> translatedOptions = new ArrayList<>(options.size());
		for(String option:options) {
			translatedOptions.add(renderer.translatedOption(option));
		}

		sb.append("<li>").append(translate("configuration.infos.options")).append(": ").append(String.join(" / ", translatedOptions)).append("</li>")
		  .append("<li>").append(translate("configuration.infos.ips")).append(": ").append(configuration.isRestrictAccessIps() ? translate("yes") : translate("no")).append("</li>")
		  .append("<li>").append(translate("configuration.infos.seb")).append(": ").append(configuration.isSafeExamBrowser() ? translate("yes") : translate("no")).append("</li>");  
		return sb.append("</ul></div>").toString();
	}
	
	private AssessmentInspectionConfiguration getSelectedConfiguration() {
		if(configurationEl.isOneSelected()) {
			String selectedKey = configurationEl.getSelectedKey();
			for(AssessmentInspectionConfiguration configuration:configurations) {
				if(configuration.getKey().toString().equals(selectedKey)) {
					return configuration;
				}
			}
		}
		return null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(configurationEl.isOneSelected()) {
			AssessmentInspectionConfiguration configuration = getSelectedConfiguration();
			context.setInspectionConfiguration(configuration);
		}
		
		context.setStartDate(inspectionPeriodEl.getDate());
		context.setEndDate(inspectionPeriodEl.getSecondDate());
		context.setAccessCode(accessCodeEl.isOn());
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
