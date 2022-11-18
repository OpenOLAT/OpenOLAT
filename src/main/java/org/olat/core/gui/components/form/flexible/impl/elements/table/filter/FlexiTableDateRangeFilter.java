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
package org.olat.core.gui.components.form.flexible.impl.elements.table.filter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 18 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FlexiTableDateRangeFilter extends FlexiTableFilter implements FlexiTableExtendedFilter {
	
	private static final String DIVIDER = ";";
	private static final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	private final boolean timeEnabled;
	private final String startLabel;
	private final String endLabel;
	private final Locale locale;
	private DateRange filterDateRange;
	
	public FlexiTableDateRangeFilter(String label, String filter, boolean defaultVisible, boolean timeEnabled,
			String startLabel, String endLabel, Locale locale) {
		super(label, filter);
		this.timeEnabled = timeEnabled;
		this.startLabel = startLabel;
		this.endLabel = endLabel;
		this.locale = locale;
		setDefaultVisible(defaultVisible);
	}
	
	public DateRange getDateRange() {
		return filterDateRange;
	}

	@Override
	public String getValue() {
		return toString(filterDateRange);
	}

	@Override
	public void setValue(Object val) {
		this.filterDateRange = toDateRange(val);
	}

	@Override
	public List<String> getValues() {
		String value = toString(filterDateRange);
		if(StringHelper.containsNonWhitespace(value)) {
			return List.of(value);
		}
		return List.of();
	}
	
	@Override
	public List<String> getHumanReadableValues() {
		if (filterDateRange != null) {
			List<String> values = new ArrayList<>(2);
			if (filterDateRange.getStart() != null) {
				values.add(startLabel + ": " + formatDate(filterDateRange.getStart()));
			}
			if (filterDateRange.getEnd() != null) {
				values.add(endLabel + ": " + formatDate(filterDateRange.getEnd()));
			}
			if (!values.isEmpty()) {
				return values;
			}
		}
		return List.of();
	}

	@Override
	public void reset() {
		filterDateRange = null;
	}
	
	@Override
	public String getDecoratedLabel(boolean withHtml) {
		return getDecoratedLabel(filterDateRange, withHtml);
	}

	@Override
	public String getDecoratedLabel(Object objectValue, boolean withHtml) {
		StringBuilder label = new StringBuilder(getLabel());
		DateRange dateRange = toDateRange(objectValue);
		if (isSelected(dateRange)) {
			label.append(": ");
			if (withHtml) {
				label.append("<small>");
			}
			
			if (dateRange.getStart() != null) {
				label.append(startLabel).append(": ").append(formatDate(dateRange.getStart()));
			}
			if (dateRange.getEnd() != null) {
				if (dateRange.getStart() != null) {
					label.append(", ");
				}
				label.append(endLabel).append(": ").append(formatDate(dateRange.getEnd()));
			}
			
			if(withHtml) {
				label.append("</small>");
			}
		}
		return label.toString();
	}
	
	private String formatDate(Date date) {
		return timeEnabled
				? Formatter.getInstance(locale).formatDateAndTime(date)
				: Formatter.getInstance(locale).formatDate(date);
	}

	@Override
	public boolean isSelected() {
		return isSelected(filterDateRange);
	}
	
	private static boolean isSelected(DateRange dateRange) {
		return dateRange != null && (dateRange.getStart() != null || dateRange.getEnd() != null);
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl, Translator translator) {
		return new FlexiFilterDateRangeController(ureq, wControl, this, timeEnabled, startLabel, endLabel, filterDateRange);
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl, Translator translator, Object preselectedValue) {
		DateRange dateRange = toDateRange(preselectedValue);
		return new FlexiFilterDateRangeController(ureq, wControl, this, timeEnabled, startLabel, endLabel, dateRange);
	}

	private DateRange toDateRange(Object object) {
		DateRange dateRange = null;
		if (object instanceof DateRange) {
			dateRange = (DateRange)object;
		}
		if (object instanceof String) {
			dateRange = toDateRange((String)object);
		}
		return dateRange;
	}
	
	private DateRange toDateRange(String value) {
		DateRange dateRange = null;
		
		if (StringHelper.containsNonWhitespace(value) && value.indexOf(DIVIDER) > -1) {
			dateRange = new DateRange();
			String[] values = value.split(DIVIDER);
			String startValue = values[0];
			if (StringHelper.containsNonWhitespace(startValue)) {
				try {
					dateRange.setStart(dateTimeFormat.parse(startValue));
				} catch (ParseException e) {
					//
				}
			}
			if (values.length > 1) {
				String endValue = values[1];
				if (StringHelper.containsNonWhitespace(endValue)) {
					try {
						dateRange.setEnd(dateTimeFormat.parse(endValue));
					} catch (ParseException e) {
						//
					}
				}
			}
		}
		
		return dateRange;
	}
	
	public static String toString(DateRange dateRange) {
		if (!isSelected(dateRange)) {
			return null;
		}
		
		String value = "";
		if (dateRange.getStart() != null) {
			value += dateTimeFormat.format(dateRange.getStart());
		}
		value += DIVIDER;
		if (dateRange.getEnd() != null) {
			value += dateTimeFormat.format(dateRange.getEnd());
		}
		
		return value;
	}
	
	public static final class DateRange {
		
		private Date start;
		private Date end;
		
		public Date getStart() {
			return start;
		}
		
		public void setStart(Date start) {
			this.start = start;
		}
		
		public Date getEnd() {
			return end;
		}
		
		public void setEnd(Date end) {
			this.end = end;
		}
		
	}
}
