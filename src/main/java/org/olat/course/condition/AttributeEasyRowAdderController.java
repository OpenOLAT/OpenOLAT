/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.course.condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.Level;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.LogModule;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.Util;
import org.olat.course.condition.operators.OperatorManager;
import org.olat.shibboleth.ShibbolethModule;
import org.olat.shibboleth.util.AttributeTranslator;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * A subform that implement the shibboleth easy mode config rows.
 * <P>
 * Initial Date: 23.10.2006 <br>
 *
 * @author Lars Eberle (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 * @author Florian Gnägi (<a href="http://www.frentix.com/">frentix GmbH</a>)
 */
public class AttributeEasyRowAdderController extends FormBasicController {

	private static final String EASYROWS = "easyrows";
	// identifyer prefixes
	private final static String PRE_ATTRIBUTE = "attribute_";
	private final static String PRE_OPERATOR = "operator_";
	private final static String PRE_VALUE_TEXT = "valuetxt_";
	private final static String PRE_VALUE_SELECTION = "valuesel_";
	private final String[] attrKeys;
	private final String preselectedAttribute;
	private final String preselectedAttributeValue;
	private final AttributeTranslator attributeTranslator;
	private final String[] operatorKeys;
	// the attribute name form elements
	private List<String> columnAttribute;
	// the operator form elements
	private List<String> columnOperator;
	// the value text input field form elements
	private List<String> columnValueText;
	// the alternative value selection drop down form elements
	private List<String> columnValueSelection;
	// add and remove row elements
	private List<String> columnAddRow;
	private List<String> columnRemoveRow;
	// global row increment counter
	private int rowCreationCounter = 0;
	//
	private boolean isinit = false;

	@Autowired
	private ShibbolethModule shibbolethModule;

	/**
	 * Constructor for a shibboleth attribute rule creator form.
	 *
	 * @param ureq
	 * @param wControl
	 * @param parentForm
	 */
	public AttributeEasyRowAdderController(final UserRequest ureq, final WindowControl wControl, final Form parentForm) {
		super(ureq, wControl, FormBasicController.LAYOUT_CUSTOM, EASYROWS, parentForm);
		// Set custom translator to use translations from shibb module as well
		setTranslator(Util.createPackageTranslator(ShibbolethModule.class, ureq.getLocale(), getTranslator()));
		attributeTranslator = shibbolethModule.getAttributeTranslator();
		attrKeys = getShibAttributes();
		preselectedAttribute = shibbolethModule.getPreselectedAttributeKey(shibbolethModule.getShibbolethAttributeName(UserConstants.INSTITUTIONALNAME));
		preselectedAttributeValue = ureq.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALNAME, getLocale());
		operatorKeys = OperatorManager.getRegisteredOperatorKeys(shibbolethModule.getOperatorKeys());
		this.init();
	}

	/**
	 * Constructor for a log attribute rule creator form.
	 *
	 * @param ureq
	 * @param wControl
	 * @param parentForm
	 */
	public AttributeEasyRowAdderController(final UserRequest ureq, final WindowControl wControl, final Form parentForm, final String[] attributes) {
		super(ureq, wControl, FormBasicController.LAYOUT_CUSTOM, EASYROWS, parentForm);
		if (attributes == null) { throw new IllegalArgumentException("attributes must not be null"); }
		attrKeys = attributes.clone();
		preselectedAttribute = UserConstants.INSTITUTIONALNAME;
		preselectedAttributeValue = ureq.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALNAME, getLocale());
		attributeTranslator = null;
		operatorKeys = OperatorManager.getRegisteredOperatorKeys(LogModule.getOperatorKeys());
		this.init();
	}

	/**
	 * Call this to cleanup everything
	 */
	public void cleanUp() {
		if (columnAttribute != null) {
			while (columnAttribute.size() > 0) {
				removeRowAt(0);
			}
		}
		isinit = false;
	}

	/**
	 * Call this to initialize the form
	 */
	public void init() {
		if (!isinit) {
			initForm(flc, this, null);
			isinit = true;
		}
	}

	@Override
	protected void doDispose() {
		// help GC
		columnAttribute = null;
		columnOperator = null;
		columnValueText = null;
		columnValueSelection = null;
		columnAddRow = null;
		columnRemoveRow = null;
        super.doDispose();
	}

	@Override
	protected void formInnerEvent(final UserRequest ureq, final FormItem source, final FormEvent event) {
		if (isinit) {
			final String compName = source.getName();
			if (columnAddRow.contains(compName)) {
				// add link clicked
				final int clickPos = ((Integer) source.getUserObject()).intValue();
				addRowAt(clickPos + 1);
			} else if (columnRemoveRow.contains(compName)) {
				// remove link clicked
				final int clickPos = ((Integer) source.getUserObject()).intValue();
				removeRowAt(clickPos);
			}
			if (compName.startsWith(PRE_ATTRIBUTE)) {
				// one of the attribute selection drop boxes has been clicked
				final SingleSelection s1 = (SingleSelection) source;
				String attr;
				if (s1.isOneSelected()) {
					attr = s1.getSelectedKey();
				} else {
					// Special case: two new rows, modify the attribute on the second one
					// without touching the first one -> nothing selected on the first row
					// In this case we use the first one which is the visible one.
					attr = s1.getKey(0);
				}
				// update the value form element depending on the selected attribute
				final int clickPos = ((Integer) s1.getUserObject()).intValue();
				updateValueElementForAttribute(attr, clickPos, null);
			}
		}
		// update whole container to reflect changes.
		this.flc.setDirty(true);
	}

	@Override
	protected void event(final UserRequest ureq, final Controller source, final Event event) {
		if (event instanceof FormEvent) {
			final FormEvent fe = (FormEvent) event;
			final FormItem sourceItem = fe.getFormItemSource();
			final String compName = sourceItem.getName();
			if (columnAddRow.contains(compName)) {
				// add link clicked
				final int clickPos = ((Integer) sourceItem.getUserObject()).intValue();
				addRowAt(clickPos + 1);
			} else if (columnRemoveRow.contains(compName)) {
				// remove link clicked
				final int clickPos = ((Integer) sourceItem.getUserObject()).intValue();
				removeRowAt(clickPos);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(final UserRequest ureq) {
		// nothing to do
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormBasicController#initFormElements(org.olat.core.gui.components.form.flexible.api.FormItemContainer, org.olat.core.gui.control.Controller,
	 *      org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(final FormItemContainer formLayout, final Controller listener, final UserRequest ureq) {
		//
		columnAttribute = new ArrayList<>();
		columnOperator = new ArrayList<>();
		columnValueText = new ArrayList<>();
		columnValueSelection = new ArrayList<>();
		columnAddRow = new ArrayList<>();
		columnRemoveRow = new ArrayList<>();
		// add a 0 row by default
		addRowAt(0);

		((FormLayoutContainer) formLayout).contextPut("columnAttribute", columnAttribute);
		((FormLayoutContainer) formLayout).contextPut("columnOperator", columnOperator);
		((FormLayoutContainer) formLayout).contextPut("columnValueText", columnValueText);
		((FormLayoutContainer) formLayout).contextPut("columnValueSelection", columnValueSelection);
		((FormLayoutContainer) formLayout).contextPut("columnAddRow", columnAddRow);
		((FormLayoutContainer) formLayout).contextPut("columnRemoveRow", columnRemoveRow);
	}

	/**
	 * Internal helper to get the current row count
	 *
	 * @return
	 */
	private int getRowCount() {
		if (columnAttribute != null) {
			return columnAttribute.size();
		} else {
			return 0;
		}
	}

	/**
	 * Method to get a list of extended conditions represented in this form
	 *
	 * @return
	 */
	public List<ExtendedCondition> getAttributeConditions() {
		final List<ExtendedCondition> le = new ArrayList<>();
		for (final Iterator<String> iterator = columnAttribute.iterator(); iterator.hasNext();) {
			final String aname = iterator.next();
			final String row = aname.replace(PRE_ATTRIBUTE, "");
			final SingleSelection attribute = (SingleSelection) flc.getFormComponent(PRE_ATTRIBUTE + row);
			final String condName = attribute.getSelectedKey();
			final SingleSelection operator = (SingleSelection) flc.getFormComponent(PRE_OPERATOR + row);
			final String condOperator = operator.getSelectedKey();
			String condValue = "";
			final SingleSelection valuessi = (SingleSelection) flc.getFormComponent(PRE_VALUE_SELECTION + row);
			if (valuessi.isVisible()) {
				if (valuessi.isOneSelected()) {
					condValue = valuessi.getSelectedKey();
				} else {
					// user did not actively select one, maybe because the first one was already the one he wanted. Use this one
					condValue = valuessi.getKey(0);
				}
			} else {
				final TextElement valuetei = (TextElement) flc.getFormComponent(PRE_VALUE_TEXT + row);
				condValue = valuetei.getValue();
			}
			le.add(new ExtendedCondition(condName, condOperator, condValue));
		}
		return le;
	}

	/**
	 * Method to initialize this form with the given extended conditions
	 *
	 * @param cond
	 */
	public void setAttributeConditions(final List<ExtendedCondition> cond) {
		if (getRowCount() > 1) { throw new AssertException("more than one row found, don't know what do do"); }
		if (!isinit) { throw new AssertException("must call init() before calling setAttributeConditions() !"); }
		// use default initialized rows when no conditions have to be set
		if (cond.size() == 0) { return; }
		// remove default row from init process to make process of generating the
		// existing configuration easier
		removeRowAt(0);
		for (final Iterator<ExtendedCondition> iterator = cond.iterator(); iterator.hasNext();) {
			final ExtendedCondition extendedCondition = iterator.next();
			final int row = getRowCount();
			// now count is always one more than the row position, thus the next position to add a row
			// is the same as the current row count
			addRowAt(row);
			// set value in attribute selection
			SingleSelection ssi = (SingleSelection) flc.getFormComponent(columnAttribute.get(row));
			ssi.select(extendedCondition.getAttribute(), true);
			// set value in operator selection
			ssi = (SingleSelection) flc.getFormComponent(columnOperator.get(row));
			ssi.select(extendedCondition.getOperator().getOperatorKey(), true);
			// set the selectable values for this attribute if available and set the
			// preselected / predefined value.
			final String attribute = extendedCondition.getAttribute();
			updateValueElementForAttribute(attribute, row, extendedCondition.getValue());
		}
	}

	/**
	 * Internal method to add a new row at the given position
	 *
	 * @param i
	 */
	private void addRowAt(final int rowPos) {
		// 1) Make room for the new row if the row is inserted between existing
		// rows. Increment the row id in the user object of the form elements and
		// move them in the form element arrays
		final Map<String,FormItem> formComponents = flc.getFormComponents();
		for (int move = rowPos + 1; move <= columnAttribute.size(); move++) {
			FormItem oldPos = formComponents.get(columnAttribute.get(move - 1));
			oldPos.setUserObject(Integer.valueOf(move));
			oldPos = formComponents.get(columnOperator.get(move - 1));
			oldPos.setUserObject(Integer.valueOf(move));
			oldPos = formComponents.get(columnValueText.get(move - 1));
			oldPos.setUserObject(Integer.valueOf(move));
			oldPos = formComponents.get(columnValueSelection.get(move - 1));
			oldPos.setUserObject(Integer.valueOf(move));
			oldPos = formComponents.get(columnAddRow.get(move - 1));
			oldPos.setUserObject(Integer.valueOf(move));
			oldPos = formComponents.get(columnRemoveRow.get(move - 1));
			oldPos.setUserObject(Integer.valueOf(move));
		}
		// 2) create the new row

		// get gui translated shib attributes - fallback is to use the key also as value
		final String[] guiTranslatedAttKeys = new String[attrKeys.length];
		for (int j = 0; j < attrKeys.length; j++) {
			final String key = attrKeys[j];
			// OLAT-5089: use translate(String key, String[] args, boolean fallBackToDefaultLocale) version
			// of Translator because that's the only one not
			String translated = getTranslator().translate(key, null, Level.OFF);
			if (translated.indexOf(Translator.NO_TRANSLATION_ERROR_PREFIX) == 0) {
				final Translator translator = UserManager.getInstance().getPropertyHandlerTranslator(getTranslator());
				final String prefix = "form.name.";
				// OLAT-5089: use translate(String key, String[] args, boolean fallBackToDefaultLocale) version
				// of Translator because that's the only one not
				translated = translator.translate(prefix + key, null, Level.OFF);
				if (translated.indexOf(Translator.NO_TRANSLATION_ERROR_PREFIX) == 0) {
					// could not translate this key, use key for non-translated values
					guiTranslatedAttKeys[j] = key;
				} else {
					guiTranslatedAttKeys[j] = translated;
				}
			} else {
				guiTranslatedAttKeys[j] = translated;
			}
		}
		// sort after the values
		ArrayHelper.sort(attrKeys, guiTranslatedAttKeys, false, true, true);
		// use this sorted keys-values
		final SingleSelection attribute = uifactory.addDropdownSingleselect(PRE_ATTRIBUTE + rowCreationCounter, null, flc, attrKeys, guiTranslatedAttKeys, null);
		attribute.setUserObject(Integer.valueOf(rowPos));
		attribute.addActionListener(FormEvent.ONCHANGE);
		columnAttribute.add(rowPos, attribute.getName());

		// 2b) LimitCheck selector
		final String[] values = OperatorManager.getRegisteredAndAlreadyTranslatedOperatorLabels(getLocale(), operatorKeys);
		final FormItem operator = uifactory.addDropdownSingleselect(PRE_OPERATOR + rowCreationCounter, null, flc, operatorKeys, values, null);
		operator.setUserObject(Integer.valueOf(rowPos));
		columnOperator.add(rowPos, operator.getName());

		// 2c) Attribute value - can be either a text input field or a selection
		// drop down box - create both and hide the selection box
		//
		final TextElement valuetxt = uifactory.addTextElement(PRE_VALUE_TEXT + rowCreationCounter, null, -1, null, flc);
		valuetxt.setDisplaySize(25);
		valuetxt.setNotEmptyCheck("form.easy.error.attribute");
		valuetxt.setUserObject(Integer.valueOf(rowPos));
		columnValueText.add(rowPos, valuetxt.getName());
		// now the selection box
		final FormItem iselect = uifactory.addDropdownSingleselect(PRE_VALUE_SELECTION + rowCreationCounter, null, flc, new String[0], new String[0], null);
		iselect.setUserObject(Integer.valueOf(rowPos));
		iselect.setVisible(false);
		columnValueSelection.add(rowPos, iselect.getName());
		// 3) Init values for this row, assume selection of attribute at position 0
		if (ArrayUtils.contains(attrKeys, preselectedAttribute)) {
			attribute.select(preselectedAttribute, true);
			updateValueElementForAttribute(attribute.getKey(ArrayUtils.indexOf(attrKeys, preselectedAttribute)), rowPos, preselectedAttributeValue);
		} else {
			updateValueElementForAttribute(attribute.getKey(0), rowPos, null);
		}
		// 4) Add the 'add' and 'remove' buttons
		final FormLinkImpl addL = new FormLinkImpl("add_" + rowCreationCounter, "add." + rowPos, "+", Link.BUTTON_SMALL + Link.NONTRANSLATED);
		addL.setUserObject(Integer.valueOf(rowPos));
		flc.add(addL);
		columnAddRow.add(rowPos, addL.getName());
		//
		final FormLinkImpl removeL = new FormLinkImpl("remove_" + rowCreationCounter, "remove." + rowPos, "-", Link.BUTTON_SMALL + Link.NONTRANSLATED);
		removeL.setUserObject(Integer.valueOf(rowPos));
		flc.add(removeL);
		columnRemoveRow.add(rowPos, removeL.getName());

		// new row created, increment counter for unique form element id's
		rowCreationCounter++;
	}

	/**
	 * Internal method to remove the row at the given position.
	 *
	 * @param clickPos The row to be removed
	 */
	private void removeRowAt(final int clickPos) {
		// 1) remove the form elements from the form container
		flc.remove(flc.getFormComponent(columnAttribute.get(clickPos)));
		columnAttribute.remove(clickPos);
		flc.remove(flc.getFormComponent(columnOperator.get(clickPos)));
		columnOperator.remove(clickPos);
		flc.remove(flc.getFormComponent(columnValueText.get(clickPos)));
		columnValueText.remove(clickPos);
		flc.remove(flc.getFormComponent(columnValueSelection.get(clickPos)));
		columnValueSelection.remove(clickPos);
		flc.remove(flc.getFormComponent(columnAddRow.get(clickPos)));
		columnAddRow.remove(clickPos);
		flc.remove(flc.getFormComponent(columnRemoveRow.get(clickPos)));
		columnRemoveRow.remove(clickPos);
		// 2) adjust all rows below the removed row. set the new row id in the user
		// object of the form element and move the element in the element arrays
		for (int move = clickPos; move < columnAttribute.size(); move++) {
			FormItem oldPos = flc.getFormComponent(columnAttribute.get(move));
			oldPos.setUserObject(Integer.valueOf(move));
			oldPos = flc.getFormComponent(columnOperator.get(move));
			oldPos.setUserObject(Integer.valueOf(move));
			oldPos = flc.getFormComponent(columnValueText.get(move));
			oldPos.setUserObject(Integer.valueOf(move));
			oldPos = flc.getFormComponent(columnValueSelection.get(move));
			oldPos.setUserObject(Integer.valueOf(move));
			oldPos = flc.getFormComponent(columnAddRow.get(move));
			oldPos.setUserObject(Integer.valueOf(move));
			oldPos = flc.getFormComponent(columnRemoveRow.get(move));
			oldPos.setUserObject(Integer.valueOf(move));
		}
	}

	/**
	 * Internal method to update a row's value element. This can be a text input box or a selection drop down depending on the shibboleth module configuration and the selected attribute. The method
	 * will set the given value as the users selected / inputed value
	 *
	 * @param attribute The attribute key. Must not be NULL
	 * @param row the row ID
	 * @param value The value that should be selected / used in the text input field. Can be NULL.
	 */
	private void updateValueElementForAttribute(final String attribute, final int row, final String value) {
		String[] selectableKeys = getSelectableKeys(attribute);

		// Get the value text input and selection drop down form elements for this
		// row. Don't use the element name since there we have the global row
		// counter in the name and _not_ the current row id!
		final SingleSelection iselect = (SingleSelection) flc.getFormComponent(columnValueSelection.get(row));
		final TextElement tei = (TextElement) flc.getFormComponent(columnValueText.get(row));

		if (selectableKeys.length > 0) {
			attributeAsSelectBox(attribute, value, selectableKeys, iselect, tei);
		} else {
			attributeAsTextField(value, iselect, tei);
		}
	}

	private String[] getSelectableKeys(final String attribute) {
		String[] selectableKeys;
		if (getAttributeTranslator() == null) {
			selectableKeys = new String[0];
		} else {
			selectableKeys = getAttributeTranslator().getSelectableValuesForAttribute(attribute);
		}
		if (selectableKeys == null) {
			selectableKeys = new String[0];
		}
		// POSTCONDITION:selectableKeys != null
		return selectableKeys;
	}

	private void attributeAsTextField(final String value, final SingleSelection iselect, final TextElement tei) {
		// update text element visibility and the value
		tei.setValue(value);
		tei.setVisible(true);
		// and hide selection box
		iselect.setVisible(false);
	}

	private void attributeAsSelectBox(final String attribute, final String value, final String[] selectableKeys, final SingleSelection iselect, final TextElement tei) {
		// to set value on selection drop down we first need to remove it and create it as new one with new keys
		// create an object array with key - value pairs in it
		final String[] guiTranslatedKeys = new String[selectableKeys.length];
		for (int i = 0; i < selectableKeys.length; i++) {
			final String key = selectableKeys[i];
			final String translated = getTranslator().translate(attribute + "." + key);
			if (translated.indexOf(Translator.NO_TRANSLATION_ERROR_PREFIX) == 0) {
				// could not translate this key, use key for non-translated values
				guiTranslatedKeys[i] = key;
			} else {
				guiTranslatedKeys[i] = translated;
			}
		}
		// sort the key-value-pairs by value
		ArrayHelper.sort(selectableKeys, guiTranslatedKeys, false, true, true);

		// update keys and values now
		iselect.setKeysAndValues(selectableKeys, guiTranslatedKeys, null);
		// set user value
		if (value != null) {
			// check if stored value exists, otherwise don't select anything
			if (Arrays.asList(selectableKeys).contains(value)) {
				iselect.select(value, true);
			} else {
				// ups, maybe this value has been removed from the list? maybe a programming error?
				logWarn("could not select value::" + value + " for shibboleth attribute::" + attribute + " in course easy mode, not found in selectable list", null);
			}
		}
		iselect.setVisible(true);
		// set alternative text input field as non visible
		tei.setVisible(false);
	}

	private AttributeTranslator getAttributeTranslator() {
		return attributeTranslator;
	}

	/**
	 * Internal helper to create a sting array that contains all shibboleth attributes that can be selected in the drop down
	 *
	 * @return String[] - will never return null
	 */
	private String[] getShibAttributes() {
		if (shibbolethModule.isEnableShibbolethCourseEasyConfig()) {
			final AttributeTranslator attTrans = getAttributeTranslator();
			Set<String> attributes = attTrans.getTranslateableAttributes();
			final String[] outNames = new String[attributes.size()];
			int i = 0;
			for (final String attribute : attributes) {
				outNames[i] = attTrans.translateAttribute(attribute);
				i++;
			}
			return outNames;
		}
		return new String[0];
	}

	/**
	 * Checks if this form produces an error
	 *
	 * @return true: has an error; false: is valid
	 */
	public boolean hasError() {
		for (final Iterator<String> iterator = columnValueText.iterator(); iterator.hasNext();) {
			final String name = iterator.next();
			final TextElement tei = (TextElement) flc.getFormComponent(name);
			if (tei.isVisible() && tei.getValue().trim().length() == 0) { return true; }
		}
		return false;
	}

	/**
	 * Get the form item that forms this subform
	 *
	 * @return
	 */
	public FormItem getFormItem() {
		return this.flc;
	}

}
