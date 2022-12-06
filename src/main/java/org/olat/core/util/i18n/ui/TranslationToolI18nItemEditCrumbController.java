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

package org.olat.core.util.i18n.ui;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.breadcrumb.CrumbFormBasicController;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.prefs.Preferences;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <h3>Description:</h3> This controller can be used to edit one or more
 * I18nItems <h3>Events thrown by this controller:</h3>
 * <ul>
 * <li>I18nItemChangedEvent when an item has been changed</li>
 * </ul>
 * <p>
 * Initial Date: 10.09.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class TranslationToolI18nItemEditCrumbController extends CrumbFormBasicController {
	private Locale referenceLocale;
	private Locale compareLocale;
	private List<I18nItem> i18nItems;
	private int currentItemPosition = 0;
	private I18nItem currentItem;
	private String currentValue = null;
	private String currentAnnotation = null;

	private SingleSelection bundlesSelection;
	private SingleSelection keysSelection;
	private TextAreaElement referenceArea;
	private TextElement annotationArea;
	private TextElement targetArea;
	private TextAreaElement compareArea;
	private MultipleSelectionElement compareSwitch;
	private SingleSelection compareLangSelection;
	private FormLink previousLink, saveLink, saveNextLink, nextLink, annotationAddLink;
	private ProgressBar progressBarBundle;
	private ProgressBar progressBarKey;

	private static final String[] KEYS_ENABLED = new String[]{ "enabled" };
	// true when the overlay files are edited and not the language files itself
	private boolean customizingMode = false;
	
	@Autowired
	private I18nManager i18nMgr;
	@Autowired
	private I18nModule i18nModule;

	/**
	 * Constructor for the item edit controller. Use the
	 * initialzeI18nitemAsCurrentItem() to set the current item from the given
	 * i18nItems list to a specific value after constructing the object
	 * 
	 * @param ureq
	 * @param control
	 * @param i18nItems List of i18n items that should be displayed. List can be
	 *          empty but must NOT be NULL.
	 * @param referenceLocale The locale used as reference
	 * @param customizingMode true: edit overlay customization files and not
	 *          language files; false: edit language files
	 */
	public TranslationToolI18nItemEditCrumbController(UserRequest ureq, WindowControl control, List<I18nItem> i18nItems,
			Locale referenceLocale, boolean customizingMode) {
		super(ureq, control, "translationToolI18nItemEdit");
		this.customizingMode = customizingMode;
		this.i18nItems = i18nItems;
		if(referenceLocale == null) {
			Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
			List<String> referenceLangs = i18nModule.getTransToolReferenceLanguages();
			String referencePrefs = (String)guiPrefs.get(I18nModule.class, I18nModule.GUI_PREFS_PREFERRED_REFERENCE_LANG, referenceLangs.get(0));
			this.referenceLocale = i18nMgr.getLocaleOrNull(referencePrefs);
			if(this.referenceLocale == null) {
				this.referenceLocale = i18nMgr.getLocaleOrDefault("de");
			}
		} else {
			this.referenceLocale = referenceLocale;
		}
		if (i18nItems.size() > 0) {
			currentItem = i18nItems.get(currentItemPosition);
			initForm(ureq);
		} else {
			showError("edit.error.noitem");
			getInitialComponent().setVisible(false); // hide velocity page
		}
	}

	/**
	 * Initializes the view of the controller for the given i18n item. The item
	 * must exist in the i18nItems list that has been used when constructing this
	 * object
	 * 
	 * @param ureq
	 * @param toBeActivatedItem
	 */
	public void initialzeI18nitemAsCurrentItem(UserRequest ureq, I18nItem toBeActivatedItem) {
		// find item in list
		for (int i = 0; i < i18nItems.size(); i++) {
			I18nItem item = i18nItems.get(i);
			if (item.equals(toBeActivatedItem)) {
				currentItemPosition = i;
				currentItem = i18nItems.get(currentItemPosition);
				initOrUpdateCurrentItem(ureq);
				return;
			}
		}
		// ups, could not find item, log error and continue
		logError("Could not init view for item with key::" + toBeActivatedItem.getKey() + " in bundle::" + toBeActivatedItem.getBundleName()
				+ " in language::" + toBeActivatedItem.getLocale().toString(), null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm
	 * (org.olat.core.gui.components.form.flexible.FormItemContainer,
	 * org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		flc.contextPut("referenceLanguageKey", referenceLocale.toString());
		flc.contextPut("referenceLanguage", i18nMgr.getLanguageTranslated(referenceLocale.toString(), false));
		// Add bundles and keys selection
		List<String> bundlesList = new ArrayList<>();
		List<String> keysList = new ArrayList<>();
		for (I18nItem item : i18nItems) {
			if (!bundlesList.contains(item.getBundleName())) {
				bundlesList.add(item.getBundleName());
			}
			if (currentItem.getBundleName().equals(item.getBundleName())) {
				keysList.add(item.getKey());
			}
		}
		String[] bundlesListArray = ArrayHelper.toArray(bundlesList);
		String[] keysListArray = ArrayHelper.toArray(keysList);
		bundlesSelection = uifactory.addDropdownSingleselect("bundlesSelection", null, flc, bundlesListArray, bundlesListArray, null);
		bundlesSelection.setDomReplacementWrapperRequired(false);
		bundlesSelection.addActionListener(FormEvent.ONCHANGE);
		bundlesSelection.select(currentItem.getBundleName(), true);
		keysSelection = uifactory.addDropdownSingleselect("keysSelection", null, flc, keysListArray, keysListArray, null);
		keysSelection.setDomReplacementWrapperRequired(false);
		keysSelection.addActionListener(FormEvent.ONCHANGE);
		keysSelection.select(currentItem.getKey(), true);
		// Add reference box
		referenceArea = uifactory.addTextAreaElement("referenceArea", null, -1, -1, -1, false, false, null, flc);
		referenceArea.setEnabled(false); // read only
		// Add compare box
		Boolean compareEnabledPrefs = (Boolean) guiPrefs.get(I18nModule.class, I18nModule.GUI_PREFS_COMPARE_LANG_ENABLED, Boolean.FALSE);
		compareArea = uifactory.addTextAreaElement("compareArea", null, -1, -1, -1, false, false, null, flc);
		compareArea.setEnabled(false); // read only
		compareArea.setVisible(compareEnabledPrefs.booleanValue());
		
		String[] compareSwitchValues = new String[]{ translate("generic.enable") };
		compareSwitch = uifactory.addCheckboxesVertical("compareSwitch", null, flc, KEYS_ENABLED, compareSwitchValues, 1);
		compareSwitch.setDomReplacementWrapperRequired(false);
		compareSwitch.select(KEYS_ENABLED[0], compareEnabledPrefs.booleanValue());
		compareSwitch.addActionListener(FormEvent.ONCLICK);
		formLayout.add(compareSwitch);
		flc.contextPut("compareSwitchEnabled", compareEnabledPrefs);
		
		// Add compare language selection
		Set<String> availableLangKeys = i18nModule.getAvailableLanguageKeys();
		String[] comparelangKeys = ArrayHelper.toArray(availableLangKeys);
		String[] compareLangValues = new String[comparelangKeys.length];
		for (int i = 0; i < comparelangKeys.length; i++) {
			String key = comparelangKeys[i];
			String explLang = i18nMgr.getLanguageInEnglish(key, customizingMode);
			String all = explLang;
			if (explLang != null && !explLang.equals(key)) all += " (" + key + ")";
			compareLangValues[i] = all;
		}
		ArrayHelper.sort(comparelangKeys, compareLangValues, false, true, false);
		// Build css classes for comparison languages
		String comparePrefs = (String) guiPrefs.get(I18nModule.class, I18nModule.GUI_PREFS_PREFERRED_COMPARE_LANG, I18nModule
				.getDefaultLocale().toString());
		compareLocale = i18nMgr.getLocaleOrNull(comparePrefs);
		if (compareLocale == null) compareLocale = I18nModule.getDefaultLocale();
		compareLangSelection = uifactory.addDropdownSingleselect("compareLangSelection", null, flc, comparelangKeys, compareLangValues, null);
		compareLangSelection.setDomReplacementWrapperRequired(false);
		compareLangSelection.select(i18nModule.getLocaleKey(compareLocale), true);
		flc.contextPut("compareLanguageKey", i18nModule.getLocaleKey(compareLocale));
		compareLangSelection.addActionListener(FormEvent.ONCHANGE);
		compareLangSelection.setEnabled(compareEnabledPrefs.booleanValue());
		
		// Add target box
		flc.contextPut("targetLanguageKey", i18nModule.getLocaleKey(currentItem.getLocale()));
		flc.contextPut("targetLanguage", i18nMgr.getLanguageTranslated(i18nModule.getLocaleKey(currentItem.getLocale()), false));			
		targetArea = uifactory.addTextAreaElement("targetArea", null, -1, 5, -1, true, false, null, flc);
		// Add annotation box
		annotationArea = uifactory.addTextAreaElement("annotationArea", null, -1, 1, -1, true, false, null, flc);
		// Add progress bar
		// init with values
		progressBarBundle = new ProgressBar("progressBarBundle", 300, 1, bundlesList.size(), translate("generic.bundles"));
		progressBarBundle.setPercentagesEnabled(false);
		flc.put("progressBarBundle", progressBarBundle);
		progressBarKey = new ProgressBar("progressBarKey", 300, 1, keysList.size(), translate("generic.keys"));
		progressBarKey.setPercentagesEnabled(false);
		flc.put("progressBarKey", progressBarKey);
		// Add navigation buttons
		previousLink = uifactory.addFormLink("previousLink", "edit.button.previous", null, formLayout, Link.BUTTON);
		previousLink.setIconLeftCSS("o_icon o_icon_previous_page");
		saveLink = uifactory.addFormLink("saveLink", "edit.button.save", null, formLayout, Link.BUTTON);
		saveNextLink = uifactory.addFormLink("saveNextLink", "edit.button.saveNext", null, formLayout, Link.BUTTON);
		formLayout.add(saveNextLink);
		nextLink = uifactory.addFormLink("nextLink", "edit.button.next", null, formLayout, Link.BUTTON);
		nextLink.setIconRightCSS("o_icon o_icon_next_page");
		formLayout.add(nextLink);
		// init values from item
		initOrUpdateCurrentItem(ureq);
		//
		// Override text labels for customizing mode
		if (customizingMode) {
			// don't edit annotations in customizing mode
			annotationArea.setEnabled(false);
			// target lang flags and lang name		
			Locale origLocale = i18nModule.getAllLocales().get(i18nMgr.createOrigianlLocaleKeyForOverlay(currentItem.getLocale()));
			if(origLocale == null) {
				origLocale = currentItem.getLocale();
			}
			String localeKey = i18nModule.getLocaleKey(origLocale);
			flc.contextPut("targetLanguageKey", localeKey);			
			flc.contextPut("targetLanguage", i18nMgr.getLanguageTranslated(localeKey, true));		
		}
		flc.contextPut("customizingMode", Boolean.valueOf(customizingMode));
		flc.contextPut("customizingPrefix", (customizingMode ? "customize." : ""));

	}

	private void initOrUpdateCurrentItem(UserRequest ureq) {
		// Set keys (must call before setting new currentItemPosition bundle name!
		if (bundlesSelection.getSelectedKey().equals(currentItem.getBundleName())) {
			// still in same bundle, just select the currentItemPosition key
			keysSelection.select(currentItem.getKey(), true);
			// Update key progress bar
			progressBarKey.setActual(keysSelection.getSelected() + 1.0f);
		} else {
			// in new bundle, load new keys
			updateKeysSelectionAndProgress();
		}
		// Set reference value
		String refValue = i18nMgr.getLocalizedString(currentItem.getBundleName(), currentItem.getKey(), null, referenceLocale, false, false, false,
				false, 0);
		referenceArea.setValue(refValue);
		// Add target value
		String targetValue = i18nMgr.getLocalizedString(currentItem, null);
		targetArea.setValue(targetValue);
		// Add compare value
		updateCompareArea(ureq);
		// Add annotation
		currentAnnotation = i18nMgr.getAnnotation(currentItem);
		annotationArea.setValue(currentAnnotation);
		if (currentAnnotation == null) {
			annotationArea.setVisible(false);
			annotationAddLink = new FormLinkImpl("annotationAddLink", "annotationAddLink", "edit.button.add.annotation", Link.BUTTON_SMALL);
			this.flc.add(annotationAddLink);
		} else {
			annotationArea.setVisible(true);
			if (annotationAddLink != null) {
				this.flc.remove(annotationAddLink);
				annotationAddLink = null;
			}
		}
		// Push item to velocity
		this.flc.contextPut("i18nItem", currentItem);
		// Set all links
		this.flc.contextPut("hasPrevious", (currentItemPosition == 0 ? Boolean.FALSE : Boolean.TRUE));
		this.flc.contextPut("hasNext", (currentItemPosition + 1 == i18nItems.size() ? Boolean.FALSE : Boolean.TRUE));
	}

	private void updateCompareArea(UserRequest ureq) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (compareSwitch.isSelected(0)) {
			// Add target value
			String compareValue = i18nMgr.getLocalizedString(currentItem.getBundleName(), currentItem.getKey(), null, compareLocale, customizingMode, false,
					false, false, 0);
			compareArea.setValue(compareValue);
			// save settings as gui prefs
			guiPrefs.put(I18nModule.class, I18nModule.GUI_PREFS_PREFERRED_COMPARE_LANG, compareLocale.toString());
			guiPrefs.putAndSave(I18nModule.class, I18nModule.GUI_PREFS_COMPARE_LANG_ENABLED, Boolean.TRUE);
		} else {
			guiPrefs.putAndSave(I18nModule.class, I18nModule.GUI_PREFS_COMPARE_LANG_ENABLED, Boolean.FALSE);
		}
		// update GUI
		this.flc.contextPut("compareSwitchEnabled", Boolean.valueOf(compareSwitch.isSelected(0)));
		this.flc.contextPut("compareLanguageKey", compareLocale.toString());
	}

	private void updateKeysSelectionAndProgress() {
		List<String> keysList = new ArrayList<>();
		for (I18nItem item : i18nItems) {
			if (currentItem.getBundleName().equals(item.getBundleName())) {
				keysList.add(item.getKey());
			}
		}
		String[] keysListArray = ArrayHelper.toArray(keysList);
		keysSelection.setKeysAndValues(keysListArray, keysListArray, null);
		keysSelection.select(currentItem.getKey(), true);
		// Update key progress bar
		progressBarKey.setMax(keysListArray.length);
		progressBarKey.setActual(keysSelection.getSelected() + 1.0f);
		// Set currentItemPosition bundle
		bundlesSelection.select(currentItem.getBundleName(), true);
		// Update bundle progress bar
		progressBarBundle.setActual(bundlesSelection.getSelected() + 1.0f);
	}

	private void doPrevious(UserRequest ureq) {
		if(currentItemPosition > 0) {
			currentItemPosition--;
		}
		currentItem = i18nItems.get(currentItemPosition);
		initOrUpdateCurrentItem(ureq);
	}

	private void doNext(UserRequest ureq) {
		if(currentItemPosition+1 < i18nItems.size()) {
			currentItemPosition++;
		}
		currentItem = i18nItems.get(currentItemPosition);
		initOrUpdateCurrentItem(ureq);
	}

	private void doSaveCurrentItem(UserRequest ureq) {
		// update annotation if dirty
		String newAnnotation = annotationArea.getValue();
		if (!StringHelper.containsNonWhitespace(newAnnotation)) newAnnotation = null;
		// check if the same
		if (!StringUtils.equals(currentAnnotation, newAnnotation)) {
			currentAnnotation = newAnnotation;
			i18nMgr.setAnnotation(currentItem, currentAnnotation);
		}
		// update value if dirty
		String newValue = targetArea.getValue();
		if ((currentValue == null && newValue != null) || (newValue == null && currentValue != null) || !currentValue.equals(newValue)) {
			i18nMgr.saveOrUpdateI18nItem(currentItem, targetArea.getValue());
			currentValue = newValue;
			fireEvent(ureq, new I18nItemChangedEvent(currentItem));
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// no form ok events to catch
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		targetArea.clearError();
		try {
			String val = targetArea.getValue();
			// first resolve any gender */: or whatever thing before checking for {0} style input
			val = i18nMgr.applyGenderStrategy(currentItem.getLocale(), val);
			MessageFormat.format(val, "1", "2", "3");
		} catch (IllegalArgumentException e) {
			targetArea.setErrorKey("edit.error.invalid.item", new String[] { e.getLocalizedMessage() });
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == bundlesSelection) {
			for (int i = 0; i < i18nItems.size(); i++) {
				I18nItem item = i18nItems.get(i);
				if (item.getBundleName().equals(bundlesSelection.getSelectedKey())) {
					currentItemPosition = i;
					currentItem = item;
					break;
				}
			}
			// in new bundle, load new keys to prevent problem in update method
			updateKeysSelectionAndProgress();
			// update everything
			initOrUpdateCurrentItem(ureq);

		} else if (source == keysSelection) {
			for (int i = 0; i < i18nItems.size(); i++) {
				I18nItem item = i18nItems.get(i);
				if (item.getBundleName().equals(bundlesSelection.getSelectedKey()) && item.getKey().equals(keysSelection.getSelectedKey())) {
					currentItemPosition = i;
					currentItem = item;
					break;
				}
			}
			initOrUpdateCurrentItem(ureq);

		} else if (source == compareSwitch) {
			if (compareSwitch.isSelected(0)) {
				compareLangSelection.setEnabled(true);
				compareArea.setVisible(true);
				updateCompareArea(ureq);
			} else {
				compareLangSelection.setEnabled(false);
				compareArea.setVisible(false);
				updateCompareArea(ureq);
			}

		} else if (source == compareLangSelection) {
			String selectedLangKey = compareLangSelection.getSelectedKey();
			compareLocale = I18nManager.getInstance().getLocaleOrNull(selectedLangKey);
			updateCompareArea(ureq);

		} else if (source == previousLink) {
			// don't save
			doPrevious(ureq);
		} else if (source == saveLink) {
			if(validateFormLogic(ureq)) {
				// only save
				doSaveCurrentItem(ureq);
			}
		} else if (source == saveNextLink) {
			if(validateFormLogic(ureq)) {
				// first save
				doSaveCurrentItem(ureq);
				// second update
				doNext(ureq);
			}
		} else if (source == nextLink) {
			// don't save
			doNext(ureq);
		} else if (source == annotationAddLink) {
			annotationArea.setValue("");
			annotationArea.setVisible(true);
			if (annotationAddLink != null) {
				this.flc.remove(annotationAddLink);
				annotationAddLink = null;
			}
		}
	}

	@Override
	public String getCrumbLinkHooverText() {
		if (customizingMode) {
			return translate("edit.customize.crumb.hoover");			
		}
		return translate("edit.crumb.hoover");
	}

	@Override
	public String getCrumbLinkText() {
		return translate("edit.crumb.link");
	}

	@Override
	protected void doDispose() {
		i18nItems = null;
        super.doDispose();
	}

}