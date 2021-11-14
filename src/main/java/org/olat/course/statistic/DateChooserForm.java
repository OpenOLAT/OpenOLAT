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

package org.olat.course.statistic;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;

/**
 * Initial Date:  03.12.2009 <br>
 * @author bja
 */
public class DateChooserForm extends FormBasicController {

	private DateChooser fromDate;
	private DateChooser toDate;
	private final long numDaysRange_;
	
	public DateChooserForm(UserRequest ureq, WindowControl wControl, long numDaysRange) {
		super(ureq, wControl);
		
		numDaysRange_ = numDaysRange;
		initForm(ureq);
	}
	
	public Date getFromDate() {
		if (fromDate!=null && fromDate.getDate() != null) {
			return fromDate.getDate();
		} else {
			return null;
		}
	}
	
	public Date getToDate() {
		if (toDate!=null && toDate.getDate() != null) {
			return toDate.getDate();
		} else {
			return null;
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		/*
		 * workaround: catch each inner event to set whole form dirty, in order to
		 * make the $f.hasError("group") having an effect in ajax mode. E.g.
		 * removing table tr's in the layouting velocity container.
		 */
		flc.setDirty(true);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean retVal = true;
		// datefields are valid
		// check complex rules involving checks over multiple elements
		Date fromDateVal = fromDate.getDate();
		Date toDateVal = toDate.getDate();
		if (!fromDate.hasError() && !toDate.hasError()) {
			// check valid dates
			// if both are set, check from < to
			if (fromDateVal != null && toDateVal != null) {
				/*
				 * bugfix http://bugs.olat.org/jira/browse/OLAT-813 valid dates and not
				 * empty, in easy mode we assume that Start and End date should
				 * implement the meaning of
				 * ----false---|S|-----|now|-TRUE---------|E|---false--->t .............
				 * Thus we check for Startdate < Enddate, error otherwise
				 */
				if (fromDateVal.after(toDateVal)) {				
					fromDate.setTranslator(Util.createPackageTranslator(org.olat.course.condition.Condition.class, ureq.getLocale(), fromDate.getTranslator()));
					fromDate.setErrorKey("form.easy.error.bdateafteredate", null);
					retVal = false;
				}
			} else {
				if (fromDateVal == null && !fromDate.isEmpty()) {
					//not a correct begin date
					fromDate.setTranslator(Util.createPackageTranslator(org.olat.course.condition.Condition.class, ureq.getLocale(), fromDate.getTranslator()));
					fromDate.setErrorKey("form.easy.error.bdate", null);
					retVal = false;
				}
				if (toDateVal == null && !toDate.isEmpty()) {
					toDate.setTranslator(Util.createPackageTranslator(org.olat.course.condition.Condition.class, ureq.getLocale(), toDate.getTranslator()));
					toDate.setErrorKey("form.easy.error.edate", null);
					retVal = false;
				}
			}
		}
		return retVal;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, final UserRequest ureq) {
		long defaultWeekRange = numDaysRange_ * 24 * 60 * 60 * 1000;
		
		// from date
		fromDate = uifactory.addDateChooser("datechooser.bdate",  new Date(new Date().getTime()-defaultWeekRange), formLayout);
		fromDate.setExampleKey("datechooser.example.bdate", null);
		fromDate.setDisplaySize(fromDate.getExampleDateString().length());
		// end date
		toDate = uifactory.addDateChooser("datechooser.edate", new Date(), formLayout);
		toDate.setExampleKey("datechooser.example.edate", null);
		toDate.setDisplaySize(toDate.getExampleDateString().length());
		
		uifactory.addFormSubmitButton( "datechooser.generate", formLayout);
	}
}