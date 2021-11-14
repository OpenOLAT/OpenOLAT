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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.den;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;

/**
 * Form for creation or edit of dates in the date enrollment
 * @author skoeber
 *
 */
public class DENDatesForm extends FormBasicController {
	
	public final static int CREATE_DATES_LAYOUT = 1;
	public final static int EDIT_SINGLE_DATE_LAYOUT = 2;
	public final static int EDIT_MULTIPLE_DATES_LAYOUT = 3;

	private FormSubmit submitBtn;
	private TextElement subjectTE, locationTE, commentTE, durationTE, pauseTE, retakeTE, participantsTE, moveTE;
	private DateChooser beginDateChooser;
	private boolean showBeginDateChooser = false;
	private boolean showDurationTE = false;
	private boolean showPauseTE = false;
	private boolean showRetakeTE = false;
	private boolean showMoveTE = false;
	private int layout;
	
	/**
	 * Constructor
	 * @param ureq
	 * @param wControl
	 * @param translator
	 * @param layout (CREATE_DATES_LAYOUT, EDIT_SINGLE_DATE_LAYOUT, EDIT_MULTIPLE_DATES_LAYOUT)
	 */
	public DENDatesForm(UserRequest ureq, WindowControl wControl, Translator translator, int layout) {
		super(ureq, wControl);
		this.setTranslator(translator);
		this.layout = layout;
		
		
		/*
		 * REVIEW:pb:2009-11-23:Hi Stefan, soweit ich gesehen habe, wird der int Layout nur verwendet um die context help im velocity zu switchen
		 * mein Vorschlag wäre, hier zur Konstruktorenzeit anstelle des int's die relevanten HELP Strings ins velocity zu schreiben und im velocity nur noch eine zeile
		 * mit contextHelp(${XYZ},${UVW}) haben.
		 */
		
		if(layout == CREATE_DATES_LAYOUT) {
			showBeginDateChooser = true;
			showDurationTE = true;
			showPauseTE = true;
			showRetakeTE = true;
		} else if(layout == EDIT_SINGLE_DATE_LAYOUT) {
			showBeginDateChooser = true;
			showDurationTE = true;
		} else if(layout == EDIT_MULTIPLE_DATES_LAYOUT) {
			showMoveTE = true;
		}
		setFormContextHelp("Other#_addentry");


		initForm(this.flc, this, ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {

		//determine if FormController is in CREATE_DATES_LAYOUT mode
		boolean createsDateLayout = layout == CREATE_DATES_LAYOUT;
		
		//create form elements
		subjectTE = uifactory.addTextElement("subject", "config.dates.subject", 25, "", formLayout);
		subjectTE.setDisplaySize(25);
		setupTextElementDependingOn(createsDateLayout, subjectTE);
		
		locationTE = uifactory.addTextElement("location", "config.dates.location", 25, "", formLayout);
		locationTE.setDisplaySize(25);
		setupTextElementDependingOn(createsDateLayout, locationTE);
		
		commentTE = uifactory.addTextElement("comment", "config.dates.comment", 100, "", formLayout);
		commentTE.setDisplaySize(25);
		commentTE.setRegexMatchCheck("[^\"\\{\\}]*", "form.error.format");
		commentTE.showError(false);
		
		//Label of duration is depending on EDIT_SINGLE_DAE_LAYOUT
		String durationTELabel = layout == EDIT_SINGLE_DATE_LAYOUT ? "config.dates.duration.single" : "config.dates.duration";
		durationTE = uifactory.addTextElement("duration", durationTELabel, 5, "00:00", formLayout);
		durationTE.setDisplaySize(5);
		durationTE.setRegexMatchCheck("\\d{1,2}:\\d\\d", "form.error.format");
		durationTE.setExampleKey("config.dates.duration.example", null);
		durationTE.setNotEmptyCheck("form.error.notempty");
		durationTE.setMandatory(true);
		durationTE.setEnabled(showDurationTE);
		durationTE.setVisible(showDurationTE);
		durationTE.showError(false);
		
		pauseTE = uifactory.addTextElement("pause", "config.dates.pause", 5, "00:00", formLayout);
		pauseTE.setDisplaySize(5);
		pauseTE.setRegexMatchCheck("\\d{1,2}:\\d\\d", "form.error.format");
		pauseTE.setExampleKey("config.dates.pause.example", null);
		pauseTE.setNotEmptyCheck("form.error.notempty");
		pauseTE.setMandatory(true);
		pauseTE.setEnabled(showPauseTE);
		pauseTE.setVisible(showPauseTE);
		
		retakeTE = uifactory.addTextElement("retake", "config.dates.retakes", 4, "1", formLayout);
		retakeTE.setDisplaySize(4);
		retakeTE.setRegexMatchCheck("^[1-9][0-9]*", "form.error.format");
		retakeTE.setMandatory(true);
		retakeTE.setNotEmptyCheck("form.error.notempty");
		retakeTE.setEnabled(showRetakeTE);
		retakeTE.setVisible(showRetakeTE);
		
		beginDateChooser = uifactory.addDateChooser("begin", "config.dates.begin", null, formLayout);
		beginDateChooser.setNotEmptyCheck("form.error.notempty");
		beginDateChooser.setValidDateCheck("form.error.date");
		beginDateChooser.setMandatory(true);
		beginDateChooser.setDisplaySize(20);
		beginDateChooser.setDateChooserTimeEnabled(true);
		beginDateChooser.setExampleKey("config.dates.begin.example", null);
		beginDateChooser.setEnabled(showBeginDateChooser);
		beginDateChooser.setVisible(showBeginDateChooser);
		beginDateChooser.setDate(new Date());
		beginDateChooser.showError(false);
		
		participantsTE = uifactory.addTextElement("participants", "config.dates.participants", 4, "", formLayout);
		participantsTE.setDisplaySize(4);
		if(layout == CREATE_DATES_LAYOUT) {
			participantsTE.setRegexMatchCheck("^[1-9][0-9]*", "form.error.format");
			participantsTE.setNotEmptyCheck("form.error.notempty");
			participantsTE.setMandatory(true);
		} else {
			participantsTE.setRegexMatchCheck("(^[1-9][0-9]*)?$", "form.error.format");
			participantsTE.setMandatory(false);
		}
		participantsTE.showError(false);
		
		moveTE = uifactory.addTextElement("move", "config.dates.move", 6, "+00:00", formLayout);
		moveTE.setDisplaySize(6);
		moveTE.setRegexMatchCheck("[-+]\\d\\d:\\d\\d", "form.error.format");
		moveTE.setNotEmptyCheck("form.error.notempty");
		moveTE.setExampleKey("config.dates.move.example", null);
		moveTE.setMandatory(true);
		moveTE.setEnabled(showMoveTE);
		moveTE.setVisible(showMoveTE);
		moveTE.showError(false);
		
		if(layout == EDIT_SINGLE_DATE_LAYOUT || layout == EDIT_MULTIPLE_DATES_LAYOUT)
			submitBtn = new FormSubmit("submitBtn", "config.dates.save");
		else
			submitBtn = new FormSubmit("submitBtn", "dates.table.edit.save");
		formLayout.add(submitBtn);
	}
	
	/* helper method
	 * set mandatory, validity checks depending on creates date layout.
	 */
	private void setupTextElementDependingOn(boolean createDatesLayout, TextElement textElement) {
		if(createDatesLayout) {
			textElement.setNotEmptyCheck("form.error.notempty");
			textElement.setRegexMatchCheck("[^\"\\{\\}]*", "form.error.format");
			textElement.setMandatory(true);
		} else {
			textElement.setMandatory(false);
		}
		textElement.showError(false);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean ok = true;
		
		if(layout == CREATE_DATES_LAYOUT || layout == EDIT_SINGLE_DATE_LAYOUT) {
			if(beginDateChooser.isEmpty() || beginDateChooser.getDate() == null || durationTE.isEmpty())
				ok = false;
		}
		
		if(layout == CREATE_DATES_LAYOUT) {
			if(retakeTE.isEmpty() || subjectTE.isEmpty() || locationTE.isEmpty() || participantsTE.isEmpty()) {
				ok = false;
			} else {
				try {
					int numRetakes = Integer.parseInt(retakeTE.getValue());
					if( numRetakes > 0 && pauseTE.isEmpty() )
						ok = false;
				} catch(NumberFormatException ex) {
					ok = false;
				}
			}
		}
		
		if(layout == EDIT_MULTIPLE_DATES_LAYOUT) {
			if(moveTE.isEmpty())
				ok = false;
		}
		
		return ok;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formNOK(UserRequest ureq) {
		// nothing to do
	}
	
	/**
	 * @return String with subject of this date
	 */
	public String getSubject() {
		return subjectTE.getValue();
	}
	
	/**
	 * @return Date with begin
	 */
	public Date getBeginDate() {
		return beginDateChooser.getDate();
	}
	
	/**
	 * @return String with duration in like "hh:mm"
	 */
	public String getDuration() {
		return durationTE.getValue();
	}
	
	/**
	 * @return String with pause in format "hh:mm"
	 */
	public String getPause() {
		return pauseTE.getValue();
	}
	
	/**
	 * @return int number of retakes
	 */
	public int getRetakes() {
		return Integer.parseInt(retakeTE.getValue());
	}
	
	/**
	 * @return int number of maximal participants for this date, 0 if empty
	 */
	public int getNumParts() {
		if(!participantsTE.isEmpty())
			return Integer.parseInt(participantsTE.getValue());
		return 0;
	}
	
	/**
	 * @return String location
	 */
	public String getLocation() {
		return locationTE.getValue();
	}
	
	/**
	 * @return String comment
	 */
	public String getComment() {
		return commentTE.getValue();
	}
	
	/**
	 * @return String gap for movement
	 */
	public String getMovementGap() {
		return moveTE.getValue();
	}
	
	/**
	 * set the subject
	 * @param value
	 */
	public void setSubject(String value) {
		subjectTE.setValue(value);
	}
	
	/**
	 * set the comment
	 * @param value
	 */
	public void setComment(String value) {
		commentTE.setValue(value);
	}
	
	/**
	 * set the location
	 * @param value
	 */
	public void setLocation(String value) {
		locationTE.setValue(value);
	}
	
	/**
	 * set number of participants
	 * @param value
	 */
	public void setNumParts(int value) {
		participantsTE.setValue(Integer.toString(value));
	}
	
	/**
	 * set duration (format hh:mm)
	 * @param value
	 */
	public void setDuration(String value) {
		durationTE.setValue(value);
	}
	
	/**
	 * set date
	 * @param date
	 */
	public void setFormDate(Date date) {
		beginDateChooser.setDate(date);
	}

}
