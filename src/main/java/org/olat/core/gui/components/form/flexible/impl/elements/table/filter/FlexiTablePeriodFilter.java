/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.form.flexible.impl.elements.table.filter;

import java.io.Serializable;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 28 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiTablePeriodFilter extends FlexiTableFilter implements FlexiTableExtendedFilter {

	private final Locale locale;
	private PeriodWithUnit value;
	private final String futureLabel;
	private final String pastLabel;
	
	public FlexiTablePeriodFilter(String label, String filter, boolean defaultVisible, Locale locale) {
		this(label, filter, null, null, defaultVisible, locale);
	}
	
	public FlexiTablePeriodFilter(String label, String filter, String futureLabel, String pastLabel,
			boolean defaultVisible, Locale locale) {
		super(label, filter);
		this.locale = locale;
		this.futureLabel = futureLabel;
		this.pastLabel = pastLabel;
		setDefaultVisible(defaultVisible);
	}
	
	public Period getPeriod() {
		return value == null ? null : value.period();
	}
	
	public PeriodWithUnit getPeriodWithUnit() {
		return value;
	}

	public String getFutureLabel() {
		return futureLabel;
	}

	public String getPastLabel() {
		return pastLabel;
	}

	@Override
	public String getValue() {
		return value == null ? null : Long.toString(value.period().get(ChronoUnit.DAYS));
	}

	@Override
	public void setValue(Object val) {
		if(val instanceof PeriodWithUnit period) {
			value = period;
		} else if(val instanceof Period period) {
			value = new PeriodWithUnit(period, false, period.getDays(), ChronoUnit.DAYS);
		} else if(val instanceof Number number) {
			int valAsInt = number.intValue();
			value = new PeriodWithUnit(Period.ofDays(valAsInt), valAsInt < 0, Math.abs(valAsInt), ChronoUnit.DAYS);
		} else if(val instanceof String string && StringHelper.isLong(string)) {
			int valAsInt = Integer.parseInt(string);
			value = new PeriodWithUnit(Period.ofDays(valAsInt), valAsInt < 0, Math.abs(valAsInt), ChronoUnit.DAYS);
		} else {
			value = null;
		}
	}

	@Override
	public List<String> getValues() {
		if(value != null) {
			return List.of(getValue());
		}
		return List.of();
	}
	
	@Override
	public List<String> getHumanReadableValues() {
		if (value != null) {
			String hv = getHumanReadableValue(value);
			if(hv != null) {
				return List.of(hv);
			}
		}
		return List.of();
	}

	@Override
	public void reset() {
		value = null;
	}
	
	@Override
	public String getDecoratedLabel(boolean withHtml) {
		return getDecoratedLabel(value, withHtml);
	}

	@Override
	public String getDecoratedLabel(Object objectValue, boolean withHtml) {
		StringBuilder label = new StringBuilder(getLabel());
		if(objectValue instanceof PeriodWithUnit pwu) {
			String hv = getHumanReadableValue(pwu);
			if(hv != null) {
				label.append(": ").append(hv);
			}
		}
		return label.toString();
	}
	
	private String getHumanReadableValue(PeriodWithUnit objectValue) {
		if (objectValue == null) return null;
		
		Translator translator = Util.createPackageTranslator(FlexiTableElementImpl.class, locale);
		StringBuilder sb = new StringBuilder();
		int valAsInt = value.value();
		String valAsString = Integer.toString(valAsInt);
		sb.append(valAsInt);
		
		String i18nKey;
		if(value.unit() == ChronoUnit.WEEKS) {
			i18nKey = valAsInt == 1 ? "period.week" : "period.weeks";
		} else if(value.unit() == ChronoUnit.MONTHS) {
			i18nKey = valAsInt == 1 ? "period.month" : "period.months";
		} else if(value.unit() == ChronoUnit.YEARS) {
			i18nKey = valAsInt == 1 ? "period.year" : "period.years";
		} else {
			i18nKey = valAsInt == 1 ? "period.day" : "period.days";
		}
		return translator.translate(i18nKey, valAsString);
	}

	@Override
	public boolean isSelected() {
		return value != null;
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl, Translator translator) {
		return new FlexiFilterPeriodController(ureq, wControl, this);
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl, Translator translator, Object preselectedValue) {
		return new FlexiFilterPeriodController(ureq, wControl, this);
	}
	
	public record PeriodWithUnit(Period period, boolean past, int value, ChronoUnit unit) implements Serializable {

		public Date toDateFromNow() {
			Calendar cal = Calendar.getInstance();
			int factor = past ? -1 : 1;
			switch(unit) {
				case DAYS:
					cal.add(Calendar.DATE, factor * value);
					break;
				case WEEKS:
					cal.add(Calendar.DATE, factor * value * 7);
					break;
				case MONTHS:
					cal.add(Calendar.MONTH, factor * value);
					break;
				case YEARS:
					cal.add(Calendar.YEAR, factor * value);
					break;
				default:
					cal.add(Calendar.DATE, factor * value);
					break;
			}
			return past ? CalendarUtils.startOfDay(cal.getTime()) : CalendarUtils.endOfDay(cal.getTime());
		}
	}
}
