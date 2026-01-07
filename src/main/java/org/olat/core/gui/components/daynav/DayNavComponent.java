/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.daynav;

import java.time.DayOfWeek;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: Jan 6, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class DayNavComponent extends FormBaseComponentImpl {
	
	private static final ComponentRenderer RENDERER = new DayNavRenderer();

	static final String CMD_TODAY = "today";
	static final String CMD_PREV_WEEK = "prev.week";
	static final String CMD_NEXT_WEEK = "next.week";
	static final String CMD_SELECT_DAY_INDEX = "select_day";
	
	private final DayNavElementImpl element;
	private Date startDate;
	private int selectedDateIndex;
	
	DayNavComponent(String name) {
		super(name);
		this.element = null;
		
		init();
	}
	
	DayNavComponent(DayNavElementImpl element) {
		super(element.getFormItemId(), element.getName());
		this.element = element;
		
		init();
	}

	private void init() {
		setDomReplacementWrapperRequired(false);
		
		this.startDate = new Date();
		this.selectedDateIndex = -1;
	}
	
	@Override
	public DayNavElement getFormItem() {
		return element;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);
		if (CMD_TODAY.equals(cmd)) {
			doToday(ureq);
			return;
		} else if (CMD_PREV_WEEK.equals(cmd)) {
			doPrevWeek(ureq);
			return;
		} else if (CMD_NEXT_WEEK.equals(cmd)) {
			doNextWeek(ureq);
			return;
		}
		
		String dayIndexParam = ureq.getParameter(CMD_SELECT_DAY_INDEX);
		if (StringHelper.containsNonWhitespace(dayIndexParam)) {
			doChangeDay(ureq, dayIndexParam);
			return;
		}
	}

	private void doToday(UserRequest ureq) {
		startDate = DateUtils.addDays(new Date(), 1);
		doPrevWeek(ureq);
		setSelectedDate(new Date());
	}

	private void doPrevWeek(UserRequest ureq) {
		moveTo(ureq, DateUtils.getPreviousDay(startDate, DayOfWeek.MONDAY));
	}

	private void doNextWeek(UserRequest ureq) {
		moveTo(ureq, DateUtils.getNextDay(startDate, DayOfWeek.MONDAY));
	}

	private void moveTo(UserRequest ureq, Date startDate) {
		this.startDate = startDate;
		selectedDateIndex = 0;
		setDirty(true);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private void doChangeDay(UserRequest ureq, String dayIndexParam) {
		if (StringHelper.isLong(dayIndexParam)) {
			int dayIndex = Integer.valueOf(dayIndexParam).intValue();
			if (dayIndex >= 0 && dayIndex <= 6) {
				selectedDateIndex = dayIndex;
				setDirty(true);
			}
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
		setDirty(true);
	}

	int getSelectedDateIndex() {
		return selectedDateIndex;
	}

	public Date getSelectedDate() {
		if (selectedDateIndex >= 0) {
			return DateUtils.addDays(startDate, selectedDateIndex);
		}
		return null;
	}

	public void setSelectedDate(Date selectedDate) {
		selectedDateIndex = -1;
		if (startDate == null || selectedDate == null) {
			return;
		}
		
		long countDays = DateUtils.countDays(startDate, selectedDate);
		if (countDays >= 0 && selectedDateIndex <= 6) {
			selectedDateIndex = (int)countDays;
		}
		
		setDirty(true);
	}

}
