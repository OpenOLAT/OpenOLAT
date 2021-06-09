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

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
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
	private String separatorI18nKey;
	private boolean timeOnlyEnabled;
	private boolean dateChooserTimeEnabled;
	private boolean defaultTimeAtEndOfDay;
	private String forValidDateErrorKey;
	private boolean checkForValidDate;
	private boolean sameDay;
	private boolean secondDate;
	private boolean buttonsEnabled = true;
	private int minute;
	private int hour;
	private int secondMinute;
	private int secondHour;
	private String secondValue;
	private DateChooser defaultDateValue;
	private Date initialDate;
	
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
		}
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		super.evalFormRequest(ureq);
		if(!isEnabled()) return;
		
		try {
			String receiverId = component.getFormDispatchId();
			int requestHour = getRequestValue("o_dch_".concat(receiverId));
			if (requestHour > -1) {
				hour = requestHour;
			}
			int requestMinute = getRequestValue("o_dcm_".concat(receiverId));
			if (requestMinute > -1) {
				minute = requestMinute;
			}
			if(isSecondDate()) {
				String secondReceiverId = receiverId.concat("_snd");
				secondHour = getRequestValue("o_dch_".concat(secondReceiverId));
				secondMinute = getRequestValue("o_dcm_".concat(secondReceiverId));
				secondValue = getRootForm().getRequestParameter(secondReceiverId);
			}
		} catch (NumberFormatException e) {
			log.error("", e);
		}
	}
	
	public String getSecondValue() {
		return secondValue;
	}
	
	private int getRequestValue(String id) {
		String val = getRootForm().getRequestParameter(id);
		if (StringHelper.isLong(val)) {
			return Integer.parseInt(val);
		}
		return -1;
	}

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

	@Override
	public Date getDate() {
		return getDate(getValue(), hour, minute);
	}
	
	@Override
	public Date getSecondDate() {
		if(isSameDay()) {
			return getDate(getValue(), secondHour, secondMinute);
		}
		return getDate(getSecondValue(), secondHour, secondMinute);
	}
	
	private Date getDate(String val, int h, int m) {
		Date d = null;
		try {
			d = parseDate(val);
			if(d != null && (isTimeOnly() || isDateChooserTimeEnabled()) && (m >= 0 || h >= 0)) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(d);
				if(h >= 0) {
					cal.set(Calendar.HOUR_OF_DAY, h);
				}
				if(m >= 0) {
					cal.set(Calendar.MINUTE, m);
				}
				d = cal.getTime();
			}	
		} catch (ParseException e) {
			log.error("", e);
		}
		return d;
	}

	@Override
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
	
	@Override
	public void setSecondDate(Date date) {
		if (date == null) {
			secondValue = "";
			secondHour = secondMinute = 0;
		} else {
			secondValue = formatDate(date);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			secondHour = cal.get(Calendar.HOUR_OF_DAY);
			secondMinute = cal.get(Calendar.MINUTE);
		}
	}

	@Override
	public Date getInitialDate() {
		return initialDate;
	}
	
	@Override
	public void setInitialDate(Date initialDate) {
		this.initialDate = initialDate;
	}
	
	@Override
	public long getDateDifference() {
		Date date = getDate();
		
		if (initialDate != null && date != null) {
			return date.getTime() - initialDate.getTime();
		}
		
		return 0;
	}
	
	public String getSeparator() {
		return separatorI18nKey;
	}

	@Override
	public void setSeparator(String i18nKey) {
		separatorI18nKey = i18nKey;
	}

	@Override
	public boolean isTimeOnly() {
		return timeOnlyEnabled;
	}

	@Override
	public void setTimeOnly(boolean enable) {
		timeOnlyEnabled = enable;
	}

	@Override
	public boolean isDateChooserTimeEnabled() {
		return dateChooserTimeEnabled;
	}

	@Override
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

	public boolean isButtonsEnabled() {
		return buttonsEnabled;
	}

	@Override
	public void setButtonsEnabled(boolean buttonsEnabled) {
		this.buttonsEnabled = buttonsEnabled;
	}

	@Override
	public boolean isSecondDate() {
		return secondDate;
	}

	@Override
	public void setSecondDate(boolean enableSecondDate) {
		this.secondDate = enableSecondDate;
	}

	@Override
	public boolean isSameDay() {
		return sameDay;
	}

	@Override
	public void setSameDay(boolean sameDay) {
		this.sameDay = sameDay;
	}

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
	
	@Override
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
	
	@Override
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