package org.olat.course.highscore.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

public class HighScoreEditController extends FormBasicController {
	
	private final static String[] yesOrNoKeys = new String[] { "highscore.all", "highscore.bestonly" };
	
	/** configuration: boolean has a podium */
	public static final String CONFIG_KEY_HIGHSCORE= "allowHighscore";
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
	
	private SingleSelection horizontalRadioButtons;
	private SelectionElement allowHighScore;
	private SelectionElement showPodium;
	private SelectionElement showHistogram;
	private SelectionElement showListing;
	private SelectionElement displayAnonymous;
	private TextElement numTableRows;
	private CourseNode msNode;	
	private ModuleConfiguration config;
	
	public HighScoreEditController(UserRequest ureq, WindowControl wControl, CourseNode msNode, UserCourseEnvironment euce) {
		super(ureq, wControl, FormBasicController.LAYOUT_DEFAULT);
		this.msNode = msNode;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		allowHighScore = uifactory.addCheckboxesHorizontal("highscore.show", formLayout, new String[] { "xx" },
				new String[] { null });
		allowHighScore.addActionListener(FormEvent.ONCLICK);
		allowHighScore.select("xx", true);

		uifactory.addSpacerElement("spacer", formLayout, false);

		showPodium = uifactory.addCheckboxesHorizontal("highscore.podium", formLayout, new String[] { "xx" },
				new String[] { null });
		showHistogram = uifactory.addCheckboxesHorizontal("highscore.histogram", formLayout, new String[] { "xx" },
				new String[] { null });
		showListing = uifactory.addCheckboxesHorizontal("highscore.listing", formLayout, new String[] { "xx" },
				new String[] { null });
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
		
		numTableRows = uifactory.addTextElement("textField", "highscore.tablesize", 4, "10", formLayout);
		numTableRows.setMandatory(true);
		numTableRows.setNotEmptyCheck("highscore.emptycheck");
		
		displayAnonymous = uifactory.addCheckboxesHorizontal("highscore.anonymize", formLayout, new String[] { "xx" }, new String[] { null });
		
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
	}
	
	private void setFromConfig() {
		config = msNode.getModuleConfiguration();
		boolean allowhighscore = config.getBooleanSafe(CONFIG_KEY_HIGHSCORE);
		allowHighScore.select("xx", allowhighscore);
		showPodium.select("xx", config.getBooleanSafe(CONFIG_KEY_PODIUM));
		showHistogram.select("xx", config.getBooleanSafe(CONFIG_KEY_HISTOGRAM));
		boolean listing = config.getBooleanSafe(CONFIG_KEY_LISTING);
		showListing.select("xx", listing);
		int showAll = (int) config.get(CONFIG_KEY_BESTONLY);
		horizontalRadioButtons.select(yesOrNoKeys[showAll], true);
		if (showAll == 0 || !listing) {
			numTableRows.setVisible(false);
		}
		displayAnonymous.setVisible(listing);
		horizontalRadioButtons.setVisible(listing);
		numTableRows.setValue(config.get(CONFIG_KEY_NUMUSER).toString());
		activateForm(true);
	}
	
	private void activateForm (boolean init){
		boolean formactive = allowHighScore.isSelected(0);
		SelectionElement[] checkboxes = {showPodium,showHistogram,showListing,displayAnonymous};		
		for (int i = 0; i < checkboxes.length; i++) {
			checkboxes[i].setEnabled(formactive);
			if (!init) {
				checkboxes[i].select("xx", i != 3 ? formactive : false);
			}
		}
		if (!init) {
			displayAnonymous.setVisible(formactive);
			horizontalRadioButtons.setVisible(formactive);
			horizontalRadioButtons.select(yesOrNoKeys[1], true);
			numTableRows.setVisible(formactive);
		}
	}

	private void activateListing() {
		boolean listingactive = showListing.isSelected(0);
		displayAnonymous.setVisible(listingactive);
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
		moduleConfiguration.set(CONFIG_KEY_PODIUM, showPodium.isSelected(0));
		moduleConfiguration.set(CONFIG_KEY_HISTOGRAM, showHistogram.isSelected(0));
		moduleConfiguration.set(CONFIG_KEY_LISTING, showListing.isSelected(0));
		if (showListing.isSelected(0)) {
			moduleConfiguration.set(CONFIG_KEY_ANONYMIZE, displayAnonymous.isSelected(0));
			moduleConfiguration.set(CONFIG_KEY_BESTONLY, horizontalRadioButtons.getSelected());
			moduleConfiguration.set(CONFIG_KEY_NUMUSER, convertToInteger(numTableRows.getValue()));
		}
	}

	public static int convertToInteger(String str) {
		if (str == null) {
			return 10;
		}
		int length = str.length();
		if (length == 0) {
			return 10;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return 10;
			}
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				return 10;
			}
		}
		return Integer.valueOf(str);
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
		// TODO Auto-generated method stub

	}
	
	

}
