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

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.breadcrumb.CrumbFormBasicController;
import org.olat.core.util.i18n.I18nItem;

/**
 * <h3>Description:</h3> This controller displays the given i18nItems as a list,
 * package by package. The controller offers a button to edit the whole list
 * from start or jumb directly to the start of a package <h3>Events thrown by
 * this controller:</h3>
 * <ul>
 * <li>I18nItemChangedEvent when an item has been changed by this or the item
 * edit controller</li>
 * </ul>
 * <p>
 * Initial Date: 10.09.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

class TranslationToolI18nItemListCrumbController extends CrumbFormBasicController {
	private Locale referenceLocale;
	private FormLink allTranslateButtonTop, allTranslateButtonBottom;
	private List<I18nItem> i18nItems;
	// true when the overlay files are edited and not the language files itself
	private boolean customizingMode = false;

	/**
	 * Constructor
	 * 
	 * @param ureq
	 * @param control
	 * @param i18nItems List of i18n items that should be displayed. List can be
	 *          empty but must NOT be NULL.
	 * @param referenceLocale The locale used as reference
	 */
	protected TranslationToolI18nItemListCrumbController(UserRequest ureq, WindowControl control, List<I18nItem> i18nItems,
			Locale referenceLocale, boolean customizingMode) {
		super(ureq, control, "translationToolI18nItemList");
		this.customizingMode = customizingMode;
		this.referenceLocale = referenceLocale;
		this.i18nItems = i18nItems;
		initForm(ureq);
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
	// nothing to do
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
		// Add the translate all button
		allTranslateButtonTop = new FormLinkImpl("allTranslateButtonTop", "allTranslateButtonTop", "generic.translateAllButton", Link.BUTTON);
		formLayout.add(allTranslateButtonTop);
		allTranslateButtonBottom = new FormLinkImpl("allTranslateButtonBottom", "allTranslateButtonBottom", "generic.translateAllButton",
				Link.BUTTON);
		formLayout.add(allTranslateButtonBottom);
		// Add a translate button for each package
		String currentBundleName = null;
		int bundlesCount = 0;
		for (I18nItem item : i18nItems) {
			if (!item.getBundleName().equals(currentBundleName)) {
				currentBundleName = item.getBundleName();
				String linkName = "translateBundle_" + currentBundleName;
				String label = (customizingMode ? "generic.customize.translateButton" : "generic.translateButton");
				FormLink bundleTranslateButton = new FormLinkImpl(linkName, linkName, label, Link.BUTTON_SMALL);
				bundleTranslateButton.setUserObject(item); // use first item of bundle
				formLayout.add(bundleTranslateButton);
				bundlesCount++;
			}
		}
		// Add all the items to velocity
		this.flc.contextPut("i18nItems", i18nItems);
		this.flc.contextPut("bundlesCount", bundlesCount);
		this.flc.contextPut("keysCount", i18nItems.size());
		// Override text labels for customizing mode
		if (customizingMode) {
			allTranslateButtonTop.setI18nKey("generic.customize.translateAllButton");
			allTranslateButtonBottom.setI18nKey("generic.customize.translateAllButton");
				}
		this.flc.contextPut("customizingMode", Boolean.valueOf(customizingMode));
		this.flc.contextPut("customizingPrefix", (customizingMode ? "customize." : ""));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest
	 * , org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof I18nItemChangedEvent) {
			// forward to parent
			fireEvent(ureq, event);
			return;
		}
		// forward all other events for form
		super.event(ureq, source, event);
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
		if (source == allTranslateButtonTop || source == allTranslateButtonBottom) {
			TranslationToolI18nItemEditCrumbController i18nItemEditCrumbCtr = new TranslationToolI18nItemEditCrumbController(ureq,
					getWindowControl(), i18nItems, referenceLocale, customizingMode);
			activateAndListenToChildCrumbController(i18nItemEditCrumbCtr);

		} else if (source instanceof FormLink) {
			FormLink link = (FormLink) source;
			TranslationToolI18nItemEditCrumbController i18nItemEditCrumbCtr = new TranslationToolI18nItemEditCrumbController(ureq,
					getWindowControl(), i18nItems, referenceLocale, customizingMode);
			activateAndListenToChildCrumbController(i18nItemEditCrumbCtr);
			// Set item from link as to be activated item
			I18nItem firstBundleItem = (I18nItem) link.getUserObject();
			i18nItemEditCrumbCtr.initialzeI18nitemAsCurrentItem(ureq, firstBundleItem);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.olat.core.gui.control.generic.breadcrumb.CrumbController#
	 * getCrumbLinkHooverText()
	 */
	public String getCrumbLinkHooverText() {
		return translate("list.crumb.hoover");
	}

	@Override
	public String getCrumbLinkText() {
		return translate("list.crumb.link");
	}

}
