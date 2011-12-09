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
* <p>
*/ 

package org.olat.core.gui.components.form;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.formelements.CheckBoxElement;
import org.olat.core.gui.formelements.DateElement;
import org.olat.core.gui.formelements.FormElement;
import org.olat.core.gui.formelements.IntegerElement;
import org.olat.core.gui.formelements.MultipleSelectionElement;
import org.olat.core.gui.formelements.PasswordElement;
import org.olat.core.gui.formelements.RadioButtonGroupElement;
import org.olat.core.gui.formelements.SingleSelectionElement;
import org.olat.core.gui.formelements.TextAreaElement;
import org.olat.core.gui.formelements.TextElement;
import org.olat.core.gui.formelements.VisibilityDependsOnSelectionRule;
import org.olat.core.gui.formelements.WikiMarkupTextAreaElement;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.i18n.I18nModule;

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost
 */

public abstract class Form extends Component {

	/**
	 * Comment for <code>CANCEL_IDENTIFICATION</code>
	 */
	public static final String CANCEL_IDENTIFICATION = "olat_foca";
	/**
	 * Comment for <code>SUBMIT_IDENTIFICATION</code>
	 */
	public static final String SUBMIT_IDENTIFICATION = "olat_fosm";
	/**
	 * Comment for <code>ELEM_BUTTON_COMMAND_ID</code>
	 */
	// don't change, is used also in functins.js
	// and choice renderer..
	public static final String ELEM_BUTTON_COMMAND_ID = "olatcmd_";

	/**
	 * Comment for <code>EVENT_VALIDATION_OK</code>
	 */
	public static final Event EVNT_VALIDATION_OK = new Event("validation ok");
	/**
	 * Comment for <code>EVENT_VALIDATION_NOK</code>
	 */
	public static final Event EVNT_VALIDATION_NOK = new Event("validation nok");
	/**
	 * Comment for <code>EVENT_FORM_CANCELLED</code>
	 */
	public static final Event EVNT_FORM_CANCELLED = new Event("form_cancelled");

	private static final ComponentRenderer RENDERER = new FormRenderer();

	private List submitKeysi18n = new ArrayList();
	private List submitKeysIdentifiers = new ArrayList();
	private String selectedSubmitKey = null;
	private String cancelKeyi18n = null;

	private boolean displayOnly = false;

	// we like to keep the order for rendering -> LinkedHashMap instead of Hashmap
	private Map elements = new LinkedHashMap(10);
	private List visibilityDependsOnSelectionRules = new ArrayList();
	private boolean valid = false;
	boolean markCancel;
	boolean markCommand;
	String markLookupCommand;
	boolean markSubmit;

	/**
	 * A Form must have a valid name and a valid translator. Create the translator
	 * outside the Form e.g. in a controller or manager.
	 * 
	 * @param name, translator
	 */
	public Form(String name, Translator translator) {
		super(name, translator);
	}

	/**
	 * @deprecated construct a new form if needed
	 */
	public void removeFormElements() {
		valid = false;
		submitKeysi18n.clear();
		submitKeysIdentifiers.clear();
		cancelKeyi18n = null;
		displayOnly = false;
		elements = new LinkedHashMap(10);
	}

	/**
	 * adds a formelement
	 * 
	 * @param name should not contain anything critical to HTML (no ".", no
	 *          umlaut, etc.)
	 * @param formElement
	 */
	public void addFormElement(String name, FormElement formElement) {
		formElement.setName(name);
		elements.put(name, formElement);
	}

	/**
	 * @param name
	 * @return The generic form element
	 */
	public FormElement getFormElement(String name) {
		return (FormElement) elements.get(name);
	}

	/**
	 * @param name
	 * @return A form element of type text
	 */
	public TextElement getTextElement(String name) {
		return (TextElement) getFormElement(name);
	}

	/**
	 * @param name
	 * @return A form element of type integer
	 */
	public IntegerElement getIntegerElement(String name) {
		return (IntegerElement) getFormElement(name);
	}

	/**
	 * @param name
	 * @return A form element of type date
	 */
	public DateElement getDateElement(String name) {
		return (DateElement) getFormElement(name);
	}

	/**
	 * @param name
	 * @return A form element of type check-box
	 */
	public CheckBoxElement getCheckBoxElement(String name) {
		return (CheckBoxElement) getFormElement(name);
	}

	/**
	 * @param name
	 * @return A form element of type single selection
	 */
	public SingleSelectionElement getSingleSelectionElement(String name) {
		return (SingleSelectionElement) getFormElement(name);
	}

	/**
	 * @param name
	 * @return A form element of type radio button
	 */
	public RadioButtonGroupElement getRadioButtonElement(String name) {
		return (RadioButtonGroupElement) getFormElement(name);
	}

	/**
	 * @param name
	 * @return A form element of type multiple selection
	 */
	public MultipleSelectionElement getMultipleSelectionElement(String name) {
		return (MultipleSelectionElement) getFormElement(name);
	}

	/**
	 * @param name
	 * @return A form element of type text area
	 */
	public TextAreaElement getTextAreaElement(String name) {
		return (TextAreaElement) getFormElement(name);
	}

	/**
	 * @param name
	 * @return A form element of type wiki markup
	 */
	public WikiMarkupTextAreaElement getWikiMarkupTextAreaElement(String name) {
		return (WikiMarkupTextAreaElement) getFormElement(name);
	}

	/**
	 * @param name
	 * @return A form element of type password
	 */
	public PasswordElement getPasswordElement(String name) {
		return (PasswordElement) getFormElement(name);
	}

	/**
	 * @return integer: the form element counter
	 */
	public int getElementCount() {
		return elements.size();
	}

	/**
	 * gives an iterator over the names of the contained formelements
	 * 
	 * @return Iterator
	 */
	public Iterator getNameIterator() {
		return elements.keySet().iterator();
	}

	/**
	 * disables the form input on all formelements, but keeps the submit/cancel
	 * buttons. convenience method, useful for e.g. after a form has been
	 * successfully submitted.
	 * 
	 * @param readOnly
	 */
	public void setAllFormElements(boolean readOnly) {
		Iterator it_formelemnames = getNameIterator();
		while (it_formelemnames.hasNext()) {
			String name = (String) it_formelemnames.next();
			FormElement fe = getFormElement(name);
			fe.setReadOnly(readOnly);
		}
	}

	/**
	 * @see org.olat.core.gui.components.Component#dispatchRequest(org.olat.core.gui.UserRequest)
	 */
	protected void doDispatchRequest(UserRequest ureq) {
		// since we are a >form<, this must be a submit or a cancel
		// check for cancel first
		
		
		String cancel = ureq.getParameter(Form.CANCEL_IDENTIFICATION);
		String lookupCommand = ureq.getParameter(ELEM_BUTTON_COMMAND_ID);

		// test for recorder
		

		if (cancel != null) {
			setDirty(true);
			fireEvent(ureq, EVNT_FORM_CANCELLED);
		} else if (lookupCommand != null) {
			fireEvent(ureq, new Event(lookupCommand));
		} else { // assume form submit
			// cannot rely on this since a form can also be submitted by hitting
			// return, in which case
			// the submit button itself is not submitted
			// if (ureq.getParameter(Form.SUBMIT_IDENTIFICATION) != null) {

			// standard behavior: set all values, validate, and fire Event
			Map recMap = null;
			Iterator it_formelemnames = getNameIterator();
			while (it_formelemnames.hasNext()) {
				String name = (String) it_formelemnames.next();
				String[] values = ureq.getHttpReq().getParameterValues(name);
				FormElement fe = getFormElement(name);
				fe.clearError(); // clear any previous errors
				if (!fe.isReadOnly()) {
					fe.setValues(values);
				}
			}

			// get selected submit key
			Set parameters = ureq.getParameterSet();
			String parameterKey = null;
			boolean foundASubmit = false;
			for (Iterator iter_parameters = parameters.iterator(); iter_parameters.hasNext();) {
				parameterKey = (String) iter_parameters.next();
				if (parameterKey.startsWith(SUBMIT_IDENTIFICATION)) {
					foundASubmit = true;
					break;
				}
			}
			
			if (foundASubmit) {
				String sPosition = parameterKey.substring(SUBMIT_IDENTIFICATION.length() + 1);
				int iPosition = Integer.parseInt(sPosition);
				selectedSubmitKey = (String) submitKeysIdentifiers.get(iPosition);
			} else {
				// assume the first submitkey (when we hit enter in a form (with internet explorer)
				selectedSubmitKey = (String) submitKeysIdentifiers.get(0);
			}
			setDirty(true);

			if (validate()) {
				valid = true;
				fireEvent(ureq, EVNT_VALIDATION_OK);
			} else {
				valid = false;
				fireEvent(ureq, EVNT_VALIDATION_NOK);
			}
		}
	}

	/**
	 * @return true: form validates, false: form contains invalid data
	 */
	public abstract boolean validate();

	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		// include needed css and js libs

		JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
		jsa.addRequiredCSSFile(Form.class, "css/jscalendar.css", false);
		jsa.addRequiredJsFile(Form.class, "js/jscalendar/calendar.js");
		jsa.addRequiredJsFile(Form.class, "js/jscalendar/olatcalendartranslator.js");
		jsa.addRequiredJsFile(Form.class, "js/jscalendar/calendar-setup.js");
		jsa.addRequiredJsFile(Form.class, "js/form.js");

	}

	/**
	 * Returns the submitKeys.
	 * 
	 * @return List of i18n keys for submit buttons
	 */
	List getSubmitKeysi18n() {
		return submitKeysi18n;
	}

	/**
	 * Sets the submitKey.
	 * 
	 * @param submitKeyi18n The submitKey to set
	 * @deprecated since 22.11.2005 replaced by
	 *             <code>addSubmitKey(String submitKeyi18n, String identifier)</code>.
	 */
	public void setSubmitKey(String submitKeyi18n) {
		addSubmitKey(submitKeyi18n, "");
	}

	/**
	 * Sets the submitKey.
	 * 
	 * @param submitKeyi18n The submitKey to set
	 * @deprecated since 19.07.2006 replaced by
	 *             <code>addSubmitKey(String submitKeyi18n, String identifier)</code>.
	 */
	public void addSubmitKey(String submitKeyi18n) {
		addSubmitKey(submitKeyi18n, "");
	}

	/**
	 * Adds a button with i18n key identified by identifier.
	 * 
	 * @param submitKeyi18n
	 * @param identifier
	 */
	public void addSubmitKey(String submitKeyi18n, String identifier) {
		submitKeysi18n.add(submitKeyi18n);
		submitKeysIdentifiers.add(identifier);
	}

	/**
	 * Returns the validated.
	 * 
	 * @return boolean
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Sets the default cancel button. behaves like
	 * <code>setCancelKey("cancel")</code>
	 */
	public void setCancelButton() {
		setCancelKey("cancel");
	}

	/**
	 * Removes the default cancel button. This is only needed if you want to
	 * toggle the visibility of the button. behaves like
	 * <code>setCancelKey(null)</code>
	 */
	public void removeCancelButton() {
		setCancelKey(null);
	}

	/**
	 * Sets the cancelParamName.
	 * 
	 * @param cancelKey
	 */
	public void setCancelKey(String cancelKeyi18n) {
		this.cancelKeyi18n = cancelKeyi18n;
	}

	/**
	 * Returns the cancelKey.
	 * 
	 * @return String
	 */
	String getCancelKeyi18n() {
		return cancelKeyi18n;
	}

	/**
	 * Returns the displayOnly.
	 * 
	 * @return boolean
	 */
	public boolean isDisplayOnly() {
		return displayOnly;
	}

	/**
	 * Sets the displayOnly.
	 * 
	 * @param displayOnly The displayOnly to set
	 */
	public void setDisplayOnly(boolean displayOnly) {
		this.displayOnly = displayOnly;
		setAllFormElements(displayOnly);
	}

	/**
	 * Check if this form contains mandatory fields
	 * 
	 * @return True if form has at least one mandatory field, false otherwhise
	 */
	public boolean hasMandatoryFields() {
		Iterator entries = elements.values().iterator();
		while (entries.hasNext()) {
			if (((FormElement) entries.next()).isMandatory()) return true;
		}
		return false;
	}

	/**
	 * Check if this form contains mandatory fields
	 * 
	 * @return True if form has at least one mandatory field, false otherwhise
	 */
	public boolean hasWikiMarkupFields() {
		Iterator entries = elements.values().iterator();
		while (entries.hasNext()) {
			if (entries.next() instanceof WikiMarkupTextAreaElement) return true;
		}
		return false;
	}

	/**
	 * Set a visibility-of-element-depends-on-a-selection-element rule to the form
	 * 
	 * @param rule
	 */
	public void addVisibilityDependsOnSelectionRule(VisibilityDependsOnSelectionRule rule) {
		visibilityDependsOnSelectionRules.add(rule);
	}

	/**
	 * Get a list of all dependecy rules
	 * 
	 * @return List of VisibilityDependsOnSelectionRule objects
	 */
	public List getVisibilityDependsOnSelectionRules() {
		return visibilityDependsOnSelectionRules;
	}

	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}


	/**
	 * @return Returns the selectedSubmitKey.
	 */
	public String getSelectedSubmitKey() {
		return selectedSubmitKey;
	}
	
	/**
	 * Use this to translate inside a form.
	 * 
	 * @param key
	 * @return
	 */
	protected String translate(String key) {
		return getTranslator().translate(key);
	}

	/**
	 * Use this to translate inside a form.
	 * 
	 * @param key
	 * @param args
	 * @return
	 */
	protected String translate(String key, String[] args) {
		return getTranslator().translate(key, args);
	}

	/**
	 * @return The current locale of the form translator.
	 */
	protected Locale getLocale() {
		if (getTranslator() == null) {
			return I18nModule.getDefaultLocale();
		} else {
			return getTranslator().getLocale();
		}
	}

}