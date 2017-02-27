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
/**
 * Initial Date:  10.08.2016 <br>
 * @author fkiefer
 */
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.JSDateChooser;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.ModuleConfiguration;

public class HighScoreEditController extends FormBasicController {
	
	private final static String[] yesOrNoKeys = new String[] { "highscore.all", "highscore.bestonly" };
	
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
	public static final String CONFIG_KEY_DATESTART = "dateStarting";
	
	private SingleSelection horizontalRadioButtons;
	
	private SelectionElement allowHighScore;
	private SelectionElement showPosition;
	private SelectionElement showPodium;
	private SelectionElement showHistogram;
	private SelectionElement showListing;
	private SelectionElement displayAnonymous;

	private IntegerElement numTableRows;
	private CourseNode msNode;	
	private ModuleConfiguration config;
	private JSDateChooser dateStart;
	
	public HighScoreEditController(UserRequest ureq, WindowControl wControl, CourseNode msNode) {
		super(ureq, wControl, FormBasicController.LAYOUT_DEFAULT);
		this.msNode = msNode;
		initForm(ureq);
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
		setFormContextHelp("ok");

		allowHighScore = uifactory.addCheckboxesHorizontal("highscore.show", formLayout, new String[] { "xx" },
				new String[] { null });
		allowHighScore.addActionListener(FormEvent.ONCLICK);
		allowHighScore.select("xx", true);

		dateStart = new JSDateChooser("startDate", getLocale());
		dateStart.setLabel("highscore.datestart", null);
		dateStart.setExampleKey("example.date", null);
		dateStart.setDateChooserTimeEnabled(true);
		dateStart.setValidDateCheck("valid.date");
		formLayout.add(dateStart);

		displayAnonymous = uifactory.addCheckboxesHorizontal("highscore.anonymize", formLayout, new String[] { "xx" },
				new String[] { null });
		
		uifactory.addSpacerElement("spacer", formLayout, false);

		showPosition = uifactory.addCheckboxesHorizontal("highscore.position", formLayout, new String[] { "xx" },
				new String[] { translate("option.show") });	
		showPosition.addActionListener(FormEvent.ONCLICK);
		showPodium = uifactory.addCheckboxesHorizontal("highscore.podium", formLayout, new String[] { "xx" },
				new String[] { translate("option.show") });
		showPodium.addActionListener(FormEvent.ONCLICK);
		showHistogram = uifactory.addCheckboxesHorizontal("highscore.histogram", formLayout, new String[] { "xx" },
				new String[] { translate("option.show") });
		showHistogram.addActionListener(FormEvent.ONCLICK);
		showListing = uifactory.addCheckboxesHorizontal("highscore.listing", formLayout, new String[] { "xx" },
				new String[] { translate("option.show") });
		showListing.addActionListener(FormEvent.ONCLICK);

		// Translate the keys to the yes and no option values
		final String[] yesOrNoOptions = new String[yesOrNoKeys.length];
		for (int i = 0; i < yesOrNoKeys.length; i++) {
			yesOrNoOptions[i] = translate(yesOrNoKeys[i]);
		}
		horizontalRadioButtons = uifactory.addRadiosHorizontal("highscore.void", formLayout, yesOrNoKeys,
				yesOrNoOptions);
		// A default value is needed for show/hide rules
		horizontalRadioButtons.select(yesOrNoKeys[1], true);
		horizontalRadioButtons.addActionListener(FormEvent.ONCLICK);//why
		numTableRows = uifactory.addIntegerElement("textField", "highscore.tablesize", 10, formLayout);
		numTableRows.setMandatory(true);
		numTableRows.setNotEmptyCheck("highscore.emptycheck");
		numTableRows.setMinValueCheck(1, "integerelement.toosmall");
		numTableRows.setMaxValueCheck(100000, "integerelement.toobig");
		numTableRows.setIntValueCheck("integerelement.noint");
		
		// Create submit and cancel buttons
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout",
				getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		setFromConfig();
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if (source == allowHighScore){
			activateForm(false);			
		} else if (source == showListing){
			activateListing();
		} else if (source == horizontalRadioButtons){
			activateTopUsers();
		}
		
		if (allowHighScore.isSelected(0) && (!showPosition.isSelected(0) && !showPodium.isSelected(0)
				&& !showListing.isSelected(0) && !showHistogram.isSelected(0))) {
			allowHighScore.setErrorKey("highscore.error.noselection", null);
		} else {
			allowHighScore.clearError();
		}
	}
	
	private void setFromConfig() {
		config = msNode.getModuleConfiguration();
		boolean allowhighscore = config.getBooleanSafe(CONFIG_KEY_HIGHSCORE,false);
		allowHighScore.select("xx", allowhighscore);
		showPosition.select("xx", config.getBooleanSafe(CONFIG_KEY_POSITION,false));
		showPodium.select("xx", config.getBooleanSafe(CONFIG_KEY_PODIUM,false));
		showHistogram.select("xx", config.getBooleanSafe(CONFIG_KEY_HISTOGRAM,false));
		displayAnonymous.select("xx", config.getBooleanSafe(CONFIG_KEY_ANONYMIZE,false));
		Date start = config.getBooleanEntry(CONFIG_KEY_DATESTART) != null ? 
				(Date) config.get(CONFIG_KEY_DATESTART) : null;
		dateStart.setDate(start);
		boolean listing = config.getBooleanSafe(CONFIG_KEY_LISTING,false);
		showListing.select("xx", listing);
		int showAll = config.getBooleanEntry(CONFIG_KEY_BESTONLY) != null ? 
				(int) config.get(CONFIG_KEY_BESTONLY) : 0;
		horizontalRadioButtons.select(yesOrNoKeys[showAll], true);
		if (showAll == 0 || !listing) {
			numTableRows.setVisible(false);
		}
		horizontalRadioButtons.setVisible(listing);
		int numuser = config.getBooleanEntry(CONFIG_KEY_NUMUSER) != null ? 
				(int) config.get(CONFIG_KEY_NUMUSER) : 10;
		numTableRows.setIntValue(numuser);
		activateForm(true);
	}
	
	
	
	private void activateForm (boolean init){
		boolean formactive = allowHighScore.isSelected(0);
		SelectionElement[] checkboxes = {showPosition,showPodium,showHistogram,showListing,displayAnonymous};		
		for (int i = 0; i < checkboxes.length; i++) {
			checkboxes[i].setEnabled(formactive);
			if (!init) {
				checkboxes[i].select("xx", formactive);
			}
		}
		if (!init) {
			horizontalRadioButtons.setVisible(formactive);
			horizontalRadioButtons.select(yesOrNoKeys[1], true);
			numTableRows.setVisible(formactive);
			dateStart.setDate(null);
		}
	}

	private void activateListing() {
		boolean listingactive = showListing.isSelected(0);
		horizontalRadioButtons.setVisible(listingactive);
		horizontalRadioButtons.select(yesOrNoKeys[1], true);
		numTableRows.setVisible(listingactive);
	}
	
	private void activateTopUsers() {
		int all = horizontalRadioButtons.getSelected();
		numTableRows.setVisible(all != 0);
	}
	
	public void updateModuleConfiguration(ModuleConfiguration moduleConfiguration) {
		moduleConfiguration.set(CONFIG_KEY_HIGHSCORE, allowHighScore.isSelected(0));
		moduleConfiguration.set(CONFIG_KEY_POSITION, showPosition.isSelected(0));
		moduleConfiguration.set(CONFIG_KEY_PODIUM, showPodium.isSelected(0));
		moduleConfiguration.set(CONFIG_KEY_HISTOGRAM, showHistogram.isSelected(0));
		moduleConfiguration.set(CONFIG_KEY_LISTING, showListing.isSelected(0));
		moduleConfiguration.set(CONFIG_KEY_DATESTART, dateStart.getDate());
		moduleConfiguration.set(CONFIG_KEY_ANONYMIZE, displayAnonymous.isSelected(0));
		if (showListing.isSelected(0)) {
			moduleConfiguration.set(CONFIG_KEY_BESTONLY, horizontalRadioButtons.getSelected());
			moduleConfiguration.set(CONFIG_KEY_NUMUSER, numTableRows.getIntValue());
		}
	}

	

	@Override
	protected void formOK(UserRequest ureq) {
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
