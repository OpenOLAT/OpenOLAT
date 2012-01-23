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
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.GUIInterna;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.ValidationStatusImpl;

/**
 * Description:<br>
 * TODO: patrickb Class Description for JSDateChooser
 * <P>
 * Initial Date: 19.01.2007 <br>
 * 
 * @author patrickb
 */
public class JSDateChooser extends TextElementImpl implements DateChooser{

	/**
	 * the java script date chooser
	 */
	private JSDateChooserComponent jscomponent;
	/**
	 * the textelement receiving the date
	 */
	private TextElementComponent txtcomponent;

	private Locale locale;
	private String customDateFormat = null;
	private DateFormat df;
	private boolean dateChooserTimeEnabled;
	private String dateChooserDateFormat;
	private String forValidDateErrorKey;
	private boolean checkForValidDate;

	public JSDateChooser(String name, String predefinedValue) {
		this(null, name, predefinedValue);
	}
	
	/**
	 * @param id A fix identifier for state-less behavior, must be unique or null
	 */
	public JSDateChooser(String id, String name, String predefinedValue) {
		super(id, name, predefinedValue);
		jscomponent = new JSDateChooserComponent(this);
		txtcomponent = (TextElementComponent) super.getFormItemComponent();
	}

	public void setDisplaySize(int dispSize){
		displaySize = dispSize;
	}
	
	protected Component getFormItemComponent() {
		return jscomponent;
	}

	TextElementComponent getTextElementComponent() {
		return txtcomponent;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.AbstractTextElement#validate(java.util.List)
	 */
	@Override
	public void validate(List validationResults) {
		// checks of the textelement
		super.validate(validationResults);
		/*
		 * postcondition: .......................................................
		 * hasError -> TRUE if error found, do not check further, errormsg is set
		 * hasError -> FALSE clearError() was called, check valid date
		 */
		if(hasError){
			return;
		}
		// check valid date
		if (checkForValidDate && !checkValidDate()) {
			validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
			return;
		}
	}

	
	
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.FormItemImpl#showError(boolean)
	 */
	@Override
	public void showError(boolean show) {
		super.showError(show);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.AbstractTextElement#rootFormAvailable()
	 */
	@Override
	protected void rootFormAvailable() {
		super.rootFormAvailable();
		//locale is available!
		locale = getTranslator().getLocale();
		
		if (GUIInterna.isLoadPerformanceMode()) {
			getRootForm().getReplayableDispatchID(txtcomponent);
		}
	}

	private boolean checkValidDate() {
		String val = getValue();
		if (val != null && val.length() == 0 && !isMandatory()) return true; 
		if (val == null || getDate() == null) {
			//must be set
			setErrorKey(forValidDateErrorKey, null);
			return false;
		}else{
			return true;
		}
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.DateChooser#getDate()
	 */
	public Date getDate() {
		Date d = null;
		try {
			d = getDateFormat().parse(getValue());
		} catch (ParseException e) {
			// return null
		}
		return d;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.DateChooser#setDate(java.util.Date)
	 */
	public void setDate(Date date) {
		if (date == null) {
			setValue("");
		} else {
			setValue(formatDate(date));
		}
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.DateChooser#isDateChooserTimeEnabled()
	 */
	public boolean isDateChooserTimeEnabled() {
		return dateChooserTimeEnabled;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.DateChooser#setDateChooserTimeEnabled(boolean)
	 */
	public void setDateChooserTimeEnabled(boolean dateChooserTimeEnabled) {
		this.dateChooserTimeEnabled = dateChooserTimeEnabled;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.DateChooser#getDateChooserDateFormat()
	 */
	public String getDateChooserDateFormat() {
		return dateChooserDateFormat;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.DateChooser#setDateChooserDateFormat(java.lang.String)
	 */
	public void setDateChooserDateFormat(String dateChooserDateFormat) {
		this.dateChooserDateFormat = dateChooserDateFormat;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.DateChooser#setCustomDateFormat(java.lang.String)
	 */
	public void setCustomDateFormat(String customDateFormat){
		this.customDateFormat = customDateFormat;
	}
	
	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.DateChooser#setValidDateCheck(java.lang.String)
	 */
	public void setValidDateCheck(String errorKey) {
		checkForValidDate = true;
		forValidDateErrorKey = errorKey;
	}

	@Override
	public void setVisible(boolean isVisible){
		super.setVisible(isVisible);
		txtcomponent.setVisible(isVisible);
	}
	
	@Override
	public void setEnabled(boolean isEnabled){
		super.setEnabled(isEnabled);
		txtcomponent.setEnabled(isEnabled);
	}
	
	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.DateChooser#getExampleDateString()
	 */
	public String getExampleDateString(){
		return formatDate(new Date(System.currentTimeMillis()));
	}
	
	private String formatDate(Date date) {
		String da = getDateFormat().format(date);
		return da;
	}

	private DateFormat getDateFormat() {
		if (df == null) {
			if (customDateFormat == null) {
				if(isDateChooserTimeEnabled()) {
					df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
					df.setLenient(false);
				} else {
					df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
					df.setLenient(false);
				}
			} else {
				df = new SimpleDateFormat(customDateFormat);
				df.setLenient(false);
			}
		}
		return df;
	}

}
