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

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.ValidationStatusImpl;

/**
 * <P>
 * Initial Date: 19.01.2007 <br>
 * 
 * @author patrickb
 */
public class JSDateChooser extends TextElementImpl implements DateChooser {

	private static final Logger log = Tracing.createLoggerFor(JSDateChooser.class);
	/**
	 * the java script date chooser
	 */
	private JSDateChooserComponent jscomponent;
	/**
	 * the textelement receiving the date
	 */
	private TextElementComponent dateComponent;

	private Locale locale;
	private boolean dateChooserTimeEnabled;
	private boolean defaultTimeAtEndOfDay;
	private String forValidDateErrorKey;
	private boolean checkForValidDate;
	private int minute;
	private int hour;
	private DateChooser defaultDateValue;
	
	public JSDateChooser(String name, Locale locale) {
		this(null, name, null, locale);
	}

	public JSDateChooser(String name, Date predefinedValue, Locale locale) {
		this(null, name, predefinedValue, locale);
	}
	
	/**
	 * @param id A fix identifier for state-less behavior, must be unique or null
	 */
	public JSDateChooser(String id, String name, Date predefinedValue, Locale locale) {
		super(id, name, "");
		this.locale = locale;
		setDate(predefinedValue);
		jscomponent = new JSDateChooserComponent(this);
		dateComponent = (TextElementComponent) super.getFormItemComponent();
	}

	@Override
	public String getForId() {
		return dateComponent.getFormDispatchId();
	}

	@Override
	public void setDomReplacementWrapperRequired(boolean required) {
		jscomponent.setDomReplacementWrapperRequired(required);
		super.setDomReplacementWrapperRequired(required);
	}

	@Override
	public void setDisplaySize(int dispSize){
		displaySize = dispSize;
	}

	@Override
	protected Component getFormItemComponent() {
		return jscomponent;
	}

	TextElementComponent getTextElementComponent() {
		return dateComponent;
	}

	@Override
	public DateChooser getDefaultValue() {
		return defaultDateValue;
	}

	@Override
	public void setDefaultValue(DateChooser dateChooser) {
		defaultDateValue = dateChooser;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.AbstractTextElement#validate(java.util.List)
	 */
	@Override
	public void validate(List<ValidationStatus> validationResults) {
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

	@Override
	public void evalFormRequest(UserRequest ureq) {
		super.evalFormRequest(ureq);
		
		try {
			String hourStr = getRootForm().getRequestParameter("o_dch_" + component.getFormDispatchId());
			if (hourStr != null && StringHelper.isLong(hourStr)) {
				hour = Integer.parseInt(hourStr);
			}	
			String minuteStr = getRootForm().getRequestParameter("o_dcm_" + component.getFormDispatchId());
			if (minuteStr != null && StringHelper.isLong(minuteStr)) {
				minute = Integer.parseInt(minuteStr);
			}
		} catch (NumberFormatException e) {
			log.error("", e);
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.AbstractTextElement#rootFormAvailable()
	 */
	@Override
	protected void rootFormAvailable() {
		super.rootFormAvailable();
		//locale is available!
		locale = getTranslator().getLocale();
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
	@Override
	public Date getDate() {
		Date d = null;
		try {
			d = parseDate(getValue());
			if(d != null && isDateChooserTimeEnabled() && (minute >= 0 || hour >= 0)) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(d);
				if(hour >= 0) {
					cal.set(Calendar.HOUR_OF_DAY, hour);
				}
				if(minute >= 0) {
					cal.set(Calendar.MINUTE, minute);
				}
				d = cal.getTime();
			}	
		} catch (ParseException e) {
			log.error("", e);
		}
		return d;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.DateChooser#setDate(java.util.Date)
	 */
	public void setDate(Date date) {
		if (date == null) {
			setValue("");
			hour = minute = 0;
		} else {
			setValue(formatDate(date));
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			hour = cal.get(Calendar.HOUR_OF_DAY);
			minute = cal.get(Calendar.MINUTE);
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

	public boolean isDefaultTimeAtEndOfDay() {
		return defaultTimeAtEndOfDay;
	}

	@Override
	public void setDefaultTimeAtEndOfDay(boolean defaultTimeAtEndOfDay) {
		this.defaultTimeAtEndOfDay = defaultTimeAtEndOfDay;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.DateChooser#getDateChooserDateFormat()
	 */
	public String getDateChooserDateFormat() {
		Calendar cal = Calendar.getInstance();
		cal.set( 1999, Calendar.MARCH, 1, 0, 0, 0 );
		String formattedDate = Formatter.getInstance(translator.getLocale()).formatDate(cal.getTime());
		formattedDate = formattedDate.replace("1999", "yy");
		formattedDate = formattedDate.replace("99", "yy");
		formattedDate = formattedDate.replace("03", "mm");
		formattedDate = formattedDate.replace("3", "mm");
		formattedDate = formattedDate.replace("01", "dd");
		formattedDate = formattedDate.replace("1", "dd");
		return formattedDate;
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
		dateComponent.setVisible(isVisible);
	}
	
	@Override
	public void setEnabled(boolean isEnabled){
		super.setEnabled(isEnabled);
		dateComponent.setEnabled(isEnabled);
	}
	
	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.form.flexible.impl.elements.DateChooser#getExampleDateString()
	 */
	public String getExampleDateString(){
		return formatDate(new Date(System.currentTimeMillis()));
	}
	
	private String formatDate(Date date) {
		if(date == null) {
			return null;
		}
		return Formatter.getInstance(locale).formatDate(date);
	}
	
	private Date parseDate(String val) throws ParseException {
		if(StringHelper.containsNonWhitespace(val)) {
			return Formatter.getInstance(locale).parseDate(val);
		}
		return null;
	}
}