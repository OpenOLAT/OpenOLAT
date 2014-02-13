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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
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
	private TextElement referenceArea;
	private TextElement annotationArea;
	private TextElement targetArea;
	private TextElement compareArea;
	private MultipleSelectionElement compareSwitch;
	private SingleSelection compareLangSelection;
	private FormLink previousLink, saveLink, saveNextLink, nextLink, annotationAddLink;
	private ProgressBar progressBarBundle, progressBarKey;

	private static final String KEYS_ENABLED = "enabled";
	private static final String KEYS_EMPTY = "";
	// true when the overlay files are edited and not the language files itself
	private boolean customizingMode = false;
	
	private final I18nManager i18nMgr;

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
		i18nMgr = I18nManager.getInstance();
		this.customizingMode = customizingMode;
		this.i18nItems = i18nItems;
		if(referenceLocale == null) {
			Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
			List<String> referenceLangs = I18nModule.getTransToolReferenceLanguages();
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
			I18nItem item = (I18nItem) i18nItems.get(i);
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
		FormUIFactory formFactory = FormUIFactory.getInstance();
		I18nManager i18nMgr = I18nManager.getInstance();
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		flc.contextPut("referenceLanguageKey", referenceLocale.toString());
		flc.contextPut("referenceLanguage", i18nMgr.getLanguageTranslated(referenceLocale.toString(), false));
		// Add bundles and keys selection
		List<String> bundlesList = new ArrayList<String>();
		List<String> keysList = new ArrayList<String>();
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
		bundlesSelection = formFactory.addDropdownSingleselect("bundlesSelection", this.flc, bundlesListArray, bundlesListArray, null);
		bundlesSelection.addActionListener(FormEvent.ONCHANGE);
		bundlesSelection.select(currentItem.getBundleName(), true);
		keysSelection = formFactory.addDropdownSingleselect("keysSelection", this.flc, keysListArray, keysListArray, null);
		keysSelection.addActionListener(FormEvent.ONCHANGE);
		keysSelection.select(currentItem.getKey(), true);
		// Add reference box
		referenceArea = formFactory.addTextAreaElement("referenceArea", "edit.referenceArea", -1, 1, -1, true, null, this.flc);
		referenceArea.setEnabled(false); // read only
		// Add compare box
		Boolean compareEnabledPrefs = (Boolean) guiPrefs.get(I18nModule.class, I18nModule.GUI_PREFS_COMPARE_LANG_ENABLED, Boolean.FALSE);
		compareArea = formFactory.addTextAreaElement("compareArea", "edit.compareArea", -1, 1, -1, true, null, this.flc);
		compareArea.setEnabled(false); // read only
		compareArea.setVisible(compareEnabledPrefs.booleanValue());
		compareSwitch = formFactory.addCheckboxesHorizontal("compareSwitch", null, this.flc, new String[] { KEYS_ENABLED },
				new String[] { KEYS_EMPTY }, null);//i18nLabel set to null -> disabled label
		compareSwitch.select(KEYS_ENABLED, compareEnabledPrefs.booleanValue());
		compareSwitch.addActionListener(FormEvent.ONCLICK);
		formLayout.add(compareSwitch);
		this.flc.contextPut("compareSwitchEnabled", compareEnabledPrefs);
		// Add compare language selection
		Set<String> availableLangKeys = I18nModule.getAvailableLanguageKeys();
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
		String[] compareLangCssClasses = i18nMgr.createLanguageFlagsCssClasses(comparelangKeys, "b_with_small_icon_left");
		String comparePrefs = (String) guiPrefs.get(I18nModule.class, I18nModule.GUI_PREFS_PREFERRED_COMPARE_LANG, I18nModule
				.getDefaultLocale().toString());
		compareLocale = i18nMgr.getLocaleOrNull(comparePrefs);
		if (compareLocale == null) compareLocale = I18nModule.getDefaultLocale();
		compareLangSelection = formFactory.addDropdownSingleselect("compareLangSelection", this.flc, comparelangKeys, compareLangValues,
				compareLangCssClasses);
		compareLangSelection.select(i18nMgr.getLocaleKey(compareLocale), true);
		this.flc.contextPut("compareLanguageKey", i18nMgr.getLocaleKey(compareLocale));
		compareLangSelection.addActionListener(FormEvent.ONCHANGE);
		compareLangSelection.setEnabled(compareEnabledPrefs.booleanValue());
		// Add target box
		this.flc.contextPut("targetLanguageKey", i18nMgr.getLocaleKey(currentItem.getLocale()));
		this.flc.contextPut("targetLanguage", i18nMgr.getLanguageTranslated(i18nMgr.getLocaleKey(currentItem.getLocale()), false));			
		targetArea = formFactory.addTextAreaElement("targetArea", "edit.targetArea", -1, 1, -1, true, null, this.flc);
		// Add annotation box
		annotationArea = formFactory.addTextAreaElement("annotationArea", "edit.annotationArea", -1, 1, -1, true, null, this.flc);
		// Add progress bar
		// init with values
		progressBarBundle = new ProgressBar("progressBarBundle", 200, 1, bundlesList.size(), translate("generic.bundles"));
		progressBarBundle.setPercentagesEnabled(false);
		this.flc.put("progressBarBundle", progressBarBundle);
		progressBarKey = new ProgressBar("progressBarKey", 200, 1, keysList.size(), translate("generic.keys"));
		progressBarKey.setPercentagesEnabled(false);
		this.flc.put("progressBarKey", progressBarKey);
		// Add navigation buttons
		previousLink = new FormLinkImpl("previousLink", "previousLink", "edit.button.previous", Link.BUTTON);
		formLayout.add(previousLink);
		saveLink = new FormLinkImpl("saveLink", "saveLink", "edit.button.save", Link.BUTTON);
		formLayout.add(saveLink);
		saveNextLink = new FormLinkImpl("saveNextLink", "saveNextLink", "edit.button.saveNext", Link.BUTTON);
		formLayout.add(saveNextLink);
		nextLink = new FormLinkImpl("nextLink", "nextLink", "edit.button.next", Link.BUTTON);
		formLayout.add(nextLink);
		// init values from item
		initOrUpdateCurrentItem(ureq);
		//
		// Override text labels for customizing mode
		if (customizingMode) {
			// don't edit annotations in customizing mode
			annotationArea.setEnabled(false);
			// target lang flags and lang name		
			Locale origLocale = I18nModule.getAllLocales().get(i18nMgr.createOrigianlLocaleKeyForOverlay(currentItem.getLocale()));
			if(origLocale == null) {
				origLocale = currentItem.getLocale();
			}
			String localeKey = i18nMgr.getLocaleKey(origLocale);
			flc.contextPut("targetLanguageKey", localeKey);			
			flc.contextPut("targetLanguage", i18nMgr.getLanguageTranslated(localeKey, true));		
		}
		flc.contextPut("customizingMode", Boolean.valueOf(customizingMode));
		flc.contextPut("customizingPrefix", (customizingMode ? "customize." : ""));

	}

	private void initOrUpdateCurrentItem(UserRequest ureq) {
		I18nManager i18nMgr = I18nManager.getInstance();
		// Set keys (must call before setting new currentItemPosition bundle name!
		if (bundlesSelection.getSelectedKey().equals(currentItem.getBundleName())) {
			// still in same bundle, just select the currentItemPosition key
			keysSelection.select(currentItem.getKey(), true);
			// Update key progress bar
			progressBarKey.setActual(keysSelection.getSelected() + 1);
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
			I18nManager i18nMgr = I18nManager.getInstance();
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
		List<String> keysList = new ArrayList<String>();
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
		progressBarKey.setActual(keysSelection.getSelected()+1);
		// Set currentItemPosition bundle
		bundlesSelection.select(currentItem.getBundleName(), true);
		// Update bundle progress bar
		progressBarBundle.setActual(bundlesSelection.getSelected()+1);
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
		I18nManager i18nMgr = I18nManager.getInstance();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK
	 * (org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
	// no form ok events to catch
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.olat.core.gui.components.form.flexible.impl.FormBasicController#
	 * formInnerEvent(org.olat.core.gui.UserRequest,
	 * org.olat.core.gui.components.form.flexible.FormItem,
	 * org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
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
			// only save
			doSaveCurrentItem(ureq);
		} else if (source == saveNextLink) {
			// first save
			doSaveCurrentItem(ureq);
			// second update
			doNext(ureq);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.olat.core.gui.control.generic.breadcrumb.CrumbController#
	 * getCrumbLinkHooverText()
	 */
	public String getCrumbLinkHooverText() {
		if (customizingMode) {
			return translate("edit.customize.crumb.hoover");			
		}
		return translate("edit.crumb.hoover");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.olat.core.gui.control.generic.breadcrumb.CrumbController#getCrumbLinkText
	 * ()
	 */
	public String getCrumbLinkText() {
		return translate("edit.crumb.link");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose
	 * ()
	 */
	@Override
	protected void doDispose() {
		i18nItems = null;
	}

}