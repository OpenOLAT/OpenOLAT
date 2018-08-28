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
package org.olat.modules.quality.generator.provider.courselectures.ui;

import static org.olat.core.gui.translator.TranslatorHelper.translateAll;
import static org.olat.modules.quality.ui.QualityUIFactory.validateDouble;
import static org.olat.modules.quality.ui.QualityUIFactory.validateInteger;
import static org.olat.modules.quality.ui.QualityUIFactory.validateIsMandatory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.User;
import org.olat.modules.quality.generator.QualityGeneratorConfigs;
import org.olat.modules.quality.generator.QualityGeneratorSearchParams;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.QualityGeneratorView;
import org.olat.modules.quality.generator.TitleCreator;
import org.olat.modules.quality.generator.provider.courselectures.CourseLecturesFollowUpProvider;
import org.olat.modules.quality.generator.provider.courselectures.CourseLecturesProvider;
import org.olat.modules.quality.generator.provider.courselectures.LimitCheck;
import org.olat.modules.quality.generator.ui.ProviderConfigController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseLectureFollowUpProviderConfigController extends ProviderConfigController {
	
	private TextElement titleEl;
	private SingleSelection previousGeneratorEl;
	private SingleSelection gradeTotalCheckEl;
	private TextElement gradeTotalLimitEl;
	private SingleSelection gradeSingleCheckEl;
	private TextElement gradeSingleLimitEl;
	private TextElement durationEl;
	private TextElement minutesBeforeEndEl;
	private TextElement invitationDaysEl;
	private TextElement reminder1DaysEl;
	private TextElement reminder2DaysEl;
	
	private final QualityGeneratorConfigs configs;

	@Autowired
	private QualityGeneratorService generatorService;
	@Autowired
	private TitleCreator titleCreator;

	public CourseLectureFollowUpProviderConfigController(UserRequest ureq, WindowControl wControl, Form mainForm,
			QualityGeneratorConfigs configs) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, mainForm);
		this.configs = configs;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String title = configs.getValue(CourseLecturesFollowUpProvider.CONFIG_KEY_TITLE);
		titleEl = uifactory.addTextElement("config.title", 200, title, formLayout);
		String identifiers = titleCreator.getIdentifiers(Arrays.asList(RepositoryEntry.class, User.class)).stream()
				.collect(Collectors.joining(", "));
		titleEl.setHelpTextKey("config.title.help", new String[] {identifiers} );
		
		// Conditions
		QualityGeneratorSearchParams searchParams = new QualityGeneratorSearchParams();
		searchParams.setProviderType(CourseLecturesProvider.TYPE);
		List<QualityGeneratorView> generators = generatorService.loadGenerators(searchParams);
		String[] generatorKeys = generators.stream().map(QualityGeneratorView::getKey).map(String::valueOf).toArray(String[]::new);
		String[] generatorValues = generators.stream().map(QualityGeneratorView::getTitle).toArray(String[]::new);
		previousGeneratorEl = uifactory.addDropdownSingleselect("followup.config.previous", formLayout, generatorKeys, generatorValues);
		String previousKey = configs.getValue(CourseLecturesFollowUpProvider.CONFIG_KEY_PREVIOUS_GENERATOR_KEY);
		if (previousKey != null && Arrays.stream(generatorKeys).anyMatch(key -> key.equals(previousKey))) {
			previousGeneratorEl.select(previousKey, true);
		}
		
		String[] totalLimitCheckKeys = LimitCheck.getKeys();
		gradeTotalCheckEl = uifactory.addDropdownSingleselect("followup.config.total.check", formLayout,
				totalLimitCheckKeys, translateAll(getTranslator(), LimitCheck.getI18nKeys()));
		String totalLimitCheckKey = configs.getValue(CourseLecturesFollowUpProvider.CONFIG_KEY_GRADE_TOTAL_CHECK_KEY);
		if (totalLimitCheckKey != null && Arrays.stream(totalLimitCheckKeys).anyMatch(key -> key.equals(totalLimitCheckKey))) {
			gradeTotalCheckEl.select(totalLimitCheckKey, true);
		} else {
			gradeTotalCheckEl.select(totalLimitCheckKeys[0], true);
		}
		
		String totalLimit = configs.getValue(CourseLecturesFollowUpProvider.CONFIG_KEY_GRADE_TOTAL_LIMIT);
		gradeTotalLimitEl = uifactory.addTextElement("followup.config.total.limit", 5, totalLimit, formLayout);
		
		String[] singleLimitCheckKeys = LimitCheck.getKeys();
		gradeSingleCheckEl = uifactory.addDropdownSingleselect("followup.config.single.check", formLayout,
				singleLimitCheckKeys, translateAll(getTranslator(), LimitCheck.getI18nKeys()));
		String singleLimitCheckKey = configs.getValue(CourseLecturesFollowUpProvider.CONFIG_KEY_GRADE_TOTAL_CHECK_KEY);
		if (singleLimitCheckKey != null && Arrays.stream(singleLimitCheckKeys).anyMatch(key -> key.equals(singleLimitCheckKey))) {
			gradeSingleCheckEl.select(singleLimitCheckKey, true);
		} else {
			gradeSingleCheckEl.select(totalLimitCheckKeys[0], true);
		}
		
		String singleLimit = configs.getValue(CourseLecturesFollowUpProvider.CONFIG_KEY_GRADE_SINGLE_LIMIT);
		gradeSingleLimitEl = uifactory.addTextElement("followup.config.single.limit", 5, singleLimit, formLayout);
		
		String minutesBeforeEnd = configs.getValue(CourseLecturesFollowUpProvider.CONFIG_KEY_MINUTES_BEFORE_END);
		minutesBeforeEndEl = uifactory.addTextElement("config.minutes.before.end", 3, minutesBeforeEnd, formLayout);

		// duration
		String duration = configs.getValue(CourseLecturesFollowUpProvider.CONFIG_KEY_DURATION_DAYS);
		durationEl = uifactory.addTextElement("config.duration", 4, duration, formLayout);

		// reminders
		String invitationDays = configs.getValue(CourseLecturesFollowUpProvider.CONFIG_KEY_INVITATION_AFTER_DC_START_DAYS);
		invitationDaysEl = uifactory.addTextElement("config.invitation.days", 4, invitationDays, formLayout);

		String reminder1Days = configs.getValue(CourseLecturesFollowUpProvider.CONFIG_KEY_REMINDER1_AFTER_DC_DAYS);
		reminder1DaysEl = uifactory.addTextElement("config.reminder1.days", 4, reminder1Days, formLayout);

		String reminder2Days = configs.getValue(CourseLecturesFollowUpProvider.CONFIG_KEY_REMINDER2_AFTER_DC_DAYS);
		reminder2DaysEl = uifactory.addTextElement("config.reminder2.days", 4, reminder2Days, formLayout);
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		boolean enabled = !readOnly;
		titleEl.setEnabled(enabled);
		previousGeneratorEl.setEnabled(enabled);
		minutesBeforeEndEl.setEnabled(enabled);
		invitationDaysEl.setEnabled(enabled);
		reminder1DaysEl.setEnabled(enabled);
		reminder2DaysEl.setEnabled(enabled);
		durationEl.setEnabled(enabled);
		flc.setDirty(true);
	}

	@Override
	public boolean validateBeforeActivation(UserRequest ureq) {
		boolean allOk = true;
		
		allOk &= validateIsMandatory(titleEl);
		allOk &= validateIsMandatory(previousGeneratorEl);
		allOk &= validateDouble(gradeTotalLimitEl, 0, 12);
		allOk &= validateDouble(gradeSingleLimitEl, 0, 12);
		allOk &= validateIsMandatory(minutesBeforeEndEl) && validateInteger(minutesBeforeEndEl, 0, 1000);
		allOk &= validateIsMandatory(durationEl) && validateInteger(durationEl, 1, 10000);
		allOk &= validateInteger(invitationDaysEl, 0, 10000);
		allOk &= validateInteger(reminder1DaysEl, 1, 10000);
		allOk &= validateInteger(reminder2DaysEl, 1, 10000);
		
		return allOk;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		titleEl.clearError();
		gradeTotalLimitEl.clearError();
		gradeSingleLimitEl.clearError();
		minutesBeforeEndEl.clearError();
		durationEl.clearError();
		invitationDaysEl.clearError();
		reminder1DaysEl.clearError();
		reminder2DaysEl.clearError();
		
		return super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String title = titleEl.getValue();
		configs.setValue(CourseLecturesFollowUpProvider.CONFIG_KEY_TITLE, title);
		
		String previousKey = previousGeneratorEl.isOneSelected()? previousGeneratorEl.getSelectedKey(): null;
		configs.setValue(CourseLecturesFollowUpProvider.CONFIG_KEY_PREVIOUS_GENERATOR_KEY, previousKey);
		
		String totalLimitCheckKey = gradeTotalCheckEl.isOneSelected()? gradeTotalCheckEl.getSelectedKey(): null;
		configs.setValue(CourseLecturesFollowUpProvider.CONFIG_KEY_GRADE_TOTAL_CHECK_KEY, totalLimitCheckKey);
		
		String totalLimit = gradeTotalLimitEl.getValue();
		configs.setValue(CourseLecturesFollowUpProvider.CONFIG_KEY_GRADE_TOTAL_LIMIT, totalLimit);
		
		String singleLimitCheckKey = gradeSingleCheckEl.isOneSelected()? gradeSingleCheckEl.getSelectedKey(): null;
		configs.setValue(CourseLecturesFollowUpProvider.CONFIG_KEY_GRADE_SINGLE_CHECK_KEY, singleLimitCheckKey);
		
		String singleLimit = gradeSingleLimitEl.getValue();
		configs.setValue(CourseLecturesFollowUpProvider.CONFIG_KEY_GRADE_SINGLE_LIMIT, singleLimit);
		
		String minutesBeforeEnd = minutesBeforeEndEl.getValue();
		configs.setValue(CourseLecturesFollowUpProvider.CONFIG_KEY_MINUTES_BEFORE_END, minutesBeforeEnd);
		
		String duration = durationEl.getValue();
		configs.setValue(CourseLecturesFollowUpProvider.CONFIG_KEY_DURATION_DAYS, duration);
		
		String invitationDays = invitationDaysEl.getValue();
		configs.setValue(CourseLecturesFollowUpProvider.CONFIG_KEY_INVITATION_AFTER_DC_START_DAYS, invitationDays);
		
		String reminder1Days = reminder1DaysEl.getValue();
		configs.setValue(CourseLecturesFollowUpProvider.CONFIG_KEY_REMINDER1_AFTER_DC_DAYS, reminder1Days);

		String reminder2Days = reminder2DaysEl.getValue();
		configs.setValue(CourseLecturesFollowUpProvider.CONFIG_KEY_REMINDER2_AFTER_DC_DAYS, reminder2Days);
	}

	@Override
	protected void doDispose() {
		//
	}

}
