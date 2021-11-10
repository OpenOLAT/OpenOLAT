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
package org.olat.course.highscore.ui;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.course.ICourse;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.DueDateService;
import org.olat.course.duedate.ui.DueDateConfigFormItem;
import org.olat.course.duedate.ui.DueDateConfigFormatter;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

public class HighScoreEditController extends FormBasicController {
	
	private static final String[] ON_KEYS = new String[]{ "on" };
	private static final String[] yesOrNoKeys = new String[] { "highscore.all", "highscore.bestonly" };
	
	/** configuration: boolean has a podium */
	public static final String CONFIG_KEY_HIGHSCORE= "allowHighscore";	
	/** configuration: boolean has a position */
	public static final String CONFIG_KEY_POSITION= "position";
	/** configuration: boolean has a podium */
	public static final String CONFIG_KEY_PODIUM = "podium";
	/** configuration: boolean has a histogram */
	public static final String CONFIG_KEY_HISTOGRAM = "histogram";
	/** configuration: boolean has a listing */
	public static final String CONFIG_KEY_LISTING = "listing";
	/** configuration: boolean bestonly or all */
	public static final String CONFIG_KEY_BESTONLY = "bestOnly";
	/** configuration: integer size of table */
	public static final String CONFIG_KEY_NUMUSER = "numUser";
	/** configuration: boolean anonymize */
	public static final String CONFIG_KEY_ANONYMIZE = "anonymize";
	/** configuration: boolean runtime */
	public static final String CONFIG_KEY_RUNTIME = "runTime";
	/** configuration: Date Start */
	private static final String CONFIG_KEY_RELATIVE_DATES = "highscore.rel.date";
	public static final String CONFIG_KEY_DATESTART = "dateStarting";
	private static final String CONFIG_KEY_DATESTART_RELATIVE = "dateStartingRel";
	private static final String CONFIG_KEY_DATESTART_RELATIVE_TO = "dateStartingRelTo";
	
	private MultipleSelectionElement relativeDatesEl;
	private DueDateConfigFormItem dateStart;
	private SingleSelection bestOnlyEl;
	
	private MultipleSelectionElement allowHighScore;
	private MultipleSelectionElement showPosition;
	private MultipleSelectionElement showPodium;
	private MultipleSelectionElement showHistogram;
	private MultipleSelectionElement showListing;
	private MultipleSelectionElement displayAnonymous;

	private IntegerElement numTableRows;
	private ModuleConfiguration config;
	private final List<String> relativeToDates;
	
	@Autowired
	private DueDateService dueDateService;
	
	public HighScoreEditController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config, ICourse course) {
		super(ureq, wControl, FormBasicController.LAYOUT_DEFAULT);
		setTranslator(Util.createPackageTranslator(getTranslator(), DueDateConfigFormItem.class, getLocale()));
		this.config = config;
		this.relativeToDates = dueDateService.getCourseRelativeToDateTypes(course.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
		initForm(ureq);
	}
	
	public static DueDateConfig getStartDateConfig(ModuleConfiguration moduleConfig) {
		return moduleConfig.getBooleanSafe(HighScoreEditController.CONFIG_KEY_HIGHSCORE)
				? DueDateConfig.ofModuleConfiguration(moduleConfig, CONFIG_KEY_RELATIVE_DATES, CONFIG_KEY_DATESTART,
						CONFIG_KEY_DATESTART_RELATIVE, CONFIG_KEY_DATESTART_RELATIVE_TO)
				: DueDateConfig.noDueDateConfig();
	}
	
	public static void setStartDateConfig(ModuleConfiguration moduleConfig, DueDateConfig startDateConfig) {
		moduleConfig.setIntValue(CONFIG_KEY_DATESTART_RELATIVE, startDateConfig.getNumOfDays());
		moduleConfig.setStringValue(CONFIG_KEY_DATESTART_RELATIVE_TO, startDateConfig.getRelativeToType());
		moduleConfig.setDateValue(CONFIG_KEY_DATESTART, startDateConfig.getAbsoluteDate());
	}
	
	public void setFormInfoMessage(String i18nKey, Translator infoMessageTranslator) {
		Translator transWithFallback = Util.createPackageTranslator(HighScoreEditController.class, getLocale(), infoMessageTranslator);
		setTranslator(transWithFallback);
		setFormInfo(i18nKey);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("controller.title");
		setFormDescription("highscore.description");
		setFormContextHelp("Assessment#highscore");
		
		allowHighScore = uifactory.addCheckboxesHorizontal("highscore.show", formLayout, new String[] { "xx" },
				new String[] { null });
		allowHighScore.addActionListener(FormEvent.ONCLICK);
		boolean allowhighscore = config.getBooleanSafe(CONFIG_KEY_HIGHSCORE,false);
		if (allowhighscore) {
			allowHighScore.select("xx", allowhighscore);		
		}
		
		relativeDatesEl = uifactory.addCheckboxesHorizontal("relative.dates", "relative.dates", formLayout, ON_KEYS, new String[]{ "" });
		relativeDatesEl.addActionListener(FormEvent.ONCHANGE);
		boolean useRelativeDates = config.getBooleanSafe(CONFIG_KEY_RELATIVE_DATES);
		relativeDatesEl.select(ON_KEYS[0], useRelativeDates);
		
		SelectionValues relativeToDatesKV = new SelectionValues();
		DueDateConfigFormatter.create(getLocale()).addCourseRelativeToDateTypes(relativeToDatesKV, relativeToDates);
		dateStart = DueDateConfigFormItem.create("edit.participation.deadline", relativeToDatesKV,
				useRelativeDates, getStartDateConfig(config));
		dateStart.setLabel("highscore.datestart", null);
		formLayout.add(dateStart);

		displayAnonymous = uifactory.addCheckboxesHorizontal("highscore.anonymize", formLayout, new String[] { "xx" },
				new String[] { null });
		if (config.getBooleanSafe(CONFIG_KEY_ANONYMIZE,false)) {
			displayAnonymous.select("xx", true); 
		}
		
		uifactory.addSpacerElement("spacer", formLayout, false);

		showPosition = uifactory.addCheckboxesHorizontal("highscore.position", formLayout, new String[] { "xx" },
				new String[] { translate("option.show") });	
		showPosition.addActionListener(FormEvent.ONCLICK);
		boolean showposition = config.getBooleanSafe(CONFIG_KEY_POSITION,false);
		if (showposition) {
			showPosition.select("xx", true); 
		}
		

		showPodium = uifactory.addCheckboxesHorizontal("highscore.podium", formLayout, new String[] { "xx" },
				new String[] { translate("option.show") });
		showPodium.addActionListener(FormEvent.ONCLICK);
		if (config.getBooleanSafe(CONFIG_KEY_PODIUM,false)) {
			showPodium.select("xx", true); 
		}
		
		showHistogram = uifactory.addCheckboxesHorizontal("highscore.histogram", formLayout, new String[] { "xx" },
				new String[] { translate("option.show") });
		showHistogram.addActionListener(FormEvent.ONCLICK);
		if (config.getBooleanSafe(CONFIG_KEY_HISTOGRAM,false)) {
			showHistogram.select("xx", true); 
		}

		showListing = uifactory.addCheckboxesHorizontal("highscore.listing", formLayout, new String[] { "xx" },
				new String[] { translate("option.show") });
		showListing.addActionListener(FormEvent.ONCLICK);
		boolean listing = config.getBooleanSafe(CONFIG_KEY_LISTING,false);
		if (listing) {
			showListing.select("xx", true); 
		}

		// Translate the keys to the yes and no option values
		final String[] yesOrNoOptions = new String[yesOrNoKeys.length];
		for (int i = 0; i < yesOrNoKeys.length; i++) {
			yesOrNoOptions[i] = translate(yesOrNoKeys[i]);
		}
		bestOnlyEl = uifactory.addRadiosHorizontal("highscore.void", formLayout, yesOrNoKeys,
				yesOrNoOptions);
		int showAll = config.getBooleanEntry(CONFIG_KEY_BESTONLY) != null ? 
				(int) config.get(CONFIG_KEY_BESTONLY) : 1;
		bestOnlyEl.select(yesOrNoKeys[showAll], true);	
		bestOnlyEl.addActionListener(FormEvent.ONCLICK);
		bestOnlyEl.setVisible(listing);

		numTableRows = uifactory.addIntegerElement("textField", "highscore.tablesize", 10, formLayout);
		numTableRows.setMandatory(true);
		numTableRows.setNotEmptyCheck("highscore.emptycheck");
		numTableRows.setMinValueCheck(1, "integerelement.toosmall");
		numTableRows.setMaxValueCheck(100000, "integerelement.toobig");
		numTableRows.setIntValueCheck("integerelement.noint");
		if (showAll == 0 || !listing) {
			numTableRows.setVisible(false);
		}
		int numuser = config.getBooleanEntry(CONFIG_KEY_NUMUSER) != null ? 
				(int) config.get(CONFIG_KEY_NUMUSER) : 10;
		numTableRows.setIntValue(numuser);
		
		// Create submit and cancel buttons
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout",
				getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		activateForm(true);
	}
	
	private void updateStartUI() {
		boolean useRelativeDate = relativeDatesEl.isAtLeastSelected(1);
		dateStart.setRelative(useRelativeDate);
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if (source == allowHighScore){
			activateForm(false);			
		} else if (source == showListing){
			activateListing();
		} else if (source == bestOnlyEl){
			activateTopUsers();
		} else if(relativeDatesEl == source) {
			updateStartUI();
		}
		
		if (allowHighScore.isSelected(0) && (!showPosition.isSelected(0) && !showPodium.isSelected(0)
				&& !showListing.isSelected(0) && !showHistogram.isSelected(0))) {
			allowHighScore.setErrorKey("highscore.error.noselection", null);
		} else {
			allowHighScore.clearError();
		}
	}
		
	private void activateForm (boolean init){
		boolean formactive = allowHighScore.isSelected(0);
		MultipleSelectionElement[] checkboxes = {showPosition,showPodium,showHistogram,showListing,displayAnonymous};		
		for (int i = 0; i < checkboxes.length; i++) {
			checkboxes[i].setEnabled(formactive);
			if (!init) {
				if (formactive) {
					checkboxes[i].select("xx", true);
				} else {
					checkboxes[i].uncheckAll();
				}
			} 			
		}
		if (!init) {
			bestOnlyEl.setVisible(formactive);
			bestOnlyEl.select(yesOrNoKeys[1], true);
			numTableRows.setVisible(formactive);
		}
	}

	private void activateListing() {
		boolean listingactive = showListing.isSelected(0);
		bestOnlyEl.setVisible(listingactive);
		bestOnlyEl.select(yesOrNoKeys[1], true);
		numTableRows.setVisible(listingactive);
	}
	
	private void activateTopUsers() {
		int all = bestOnlyEl.getSelected();
		numTableRows.setVisible(all != 0);
	}
	

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOK = super.validateFormLogic(ureq);
		if (allowHighScore.isSelected(0)) {
			allOK &= showHistogram.isSelected(0) || showListing.isSelected(0) 
					|| showPodium.isSelected(0) || showPosition.isSelected(0);
		}
		
		dateStart.clearError();
		List<ValidationStatus> dateStartValidation = new ArrayList<>(1);
		dateStart.validate(dateStartValidation);
		if (!dateStartValidation.isEmpty()) {
			allOK &= false;
		}
		
		return allOK;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		config.set(CONFIG_KEY_HIGHSCORE, allowHighScore.isSelected(0));
		config.set(CONFIG_KEY_POSITION, showPosition.isSelected(0));
		config.set(CONFIG_KEY_PODIUM, showPodium.isSelected(0));
		config.set(CONFIG_KEY_HISTOGRAM, showHistogram.isSelected(0));
		config.set(CONFIG_KEY_LISTING, showListing.isSelected(0));
		config.set(CONFIG_KEY_ANONYMIZE, displayAnonymous.isSelected(0));
		config.setBooleanEntry(CONFIG_KEY_RELATIVE_DATES, relativeDatesEl.isAtLeastSelected(1));
		setStartDateConfig(config, dateStart.getDueDateConfig());
		if (showListing.isSelected(0)) {
			int bestOnly = bestOnlyEl.getSelected();
			config.set(CONFIG_KEY_BESTONLY, bestOnly);
			if(bestOnly == 1) {
				config.set(CONFIG_KEY_NUMUSER, numTableRows.getIntValue());
			} else {
				config.remove(CONFIG_KEY_NUMUSER);
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}
	
	

}
