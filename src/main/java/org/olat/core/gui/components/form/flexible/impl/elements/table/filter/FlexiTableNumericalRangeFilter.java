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

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 24 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class FlexiTableNumericalRangeFilter extends FlexiTableFilter implements FlexiTableExtendedFilter {
	
	private static final String DIVIDER = ";";
	
	private static final DecimalFormat numericalFormat = new DecimalFormat("#0.###", new DecimalFormatSymbols(Locale.ENGLISH));
	
	private final String startLabel;
	private final String endLabel;
	private NumericalRange filterNumericalRange;
	
	public FlexiTableNumericalRangeFilter(String label, String filter, boolean defaultVisible,
			String startLabel, String endLabel) {
		super(label, filter);
		this.startLabel = startLabel;
		this.endLabel = endLabel;
		setDefaultVisible(defaultVisible);
	}
	
	public NumericalRange getNumericalRange() {
		return filterNumericalRange;
	}

	@Override
	public String getValue() {
		return toString(filterNumericalRange);
	}

	@Override
	public void setValue(Object val) {
		this.filterNumericalRange = toNumericalRange(val);
	}

	@Override
	public List<String> getValues() {
		String value = toString(filterNumericalRange);
		if(StringHelper.containsNonWhitespace(value)) {
			return List.of(value);
		}
		return List.of();
	}
	
	@Override
	public List<String> getHumanReadableValues() {
		if (filterNumericalRange != null) {
			List<String> values = new ArrayList<>(2);
			if (filterNumericalRange.getStart() != null) {
				values.add(startLabel + ": " + formatNumerical(filterNumericalRange.getStart()));
			}
			if (filterNumericalRange.getEnd() != null) {
				values.add(endLabel + ": " + formatNumerical(filterNumericalRange.getEnd()));
			}
			if (!values.isEmpty()) {
				return values;
			}
		}
		return List.of();
	}

	@Override
	public void reset() {
		filterNumericalRange = null;
	}
	
	@Override
	public String getDecoratedLabel(boolean withHtml) {
		return getDecoratedLabel(filterNumericalRange, withHtml);
	}

	@Override
	public String getDecoratedLabel(Object objectValue, boolean withHtml) {
		StringBuilder label = new StringBuilder(getLabel());
		NumericalRange range = toNumericalRange(objectValue);
		if (isSelected(range)) {
			label.append(": ");
			if (withHtml) {
				label.append("<small>");
			}
			
			if (range.getStart() != null) {
				label.append(startLabel).append(": ").append(formatNumerical(range.getStart()));
			}
			if (range.getEnd() != null) {
				if (range.getStart() != null) {
					label.append(", ");
				}
				label.append(endLabel).append(": ").append(formatNumerical(range.getEnd()));
			}
			
			if(withHtml) {
				label.append("</small>");
			}
		}
		return label.toString();
	}
	
	public static String formatNumerical(Double number) {
		synchronized(numericalFormat) {
			return numericalFormat.format(number);
		}
	}

	@Override
	public boolean isSelected() {
		return isSelected(filterNumericalRange);
	}
	
	private static boolean isSelected(NumericalRange range) {
		return range != null && (range.getStart() != null || range.getEnd() != null);
	}
	
	@Override
	public FlexiFilterExtendedController getController(UserRequest ureq, WindowControl wControl, Form form, Translator translator, Object preselectedValue) {
		NumericalRange range = preselectedValue != null? toNumericalRange(preselectedValue): filterNumericalRange;
		return new FlexiFilterNumericalRangeController(ureq, wControl, form, this, startLabel, endLabel, range);
	}

	private NumericalRange toNumericalRange(Object object) {
		NumericalRange range = null;
		if (object instanceof NumericalRange nRange) {
			range = nRange;
		}
		if (object instanceof String string) {
			range = toDateRange(string);
		}
		return range;
	}
	
	private NumericalRange toDateRange(String value) {
		NumericalRange range = null;
		
		if (StringHelper.containsNonWhitespace(value) && value.indexOf(DIVIDER) > -1) {
			range = new NumericalRange();
			String[] values = value.split(DIVIDER);
			String startValue = values[0];
			if (StringHelper.containsNonWhitespace(startValue)) {
				try {
					range.setStart(Double.valueOf(startValue));
				} catch (Exception e) {
					//
				}
			}
			if (values.length > 1) {
				String endValue = values[1];
				if (StringHelper.containsNonWhitespace(endValue)) {
					try {
						range.setEnd(Double.valueOf(endValue));
					} catch (Exception e) {
						//
					}
				}
			}
		}
		
		return range;
	}
	
	public static String toString(NumericalRange range) {
		if (!isSelected(range)) {
			return null;
		}
		
		String value = "";
		if (range.getStart() != null) {
			value += range.getStart();
		}
		value += DIVIDER;
		if (range.getEnd() != null) {
			value += range.getEnd();
		}
		
		return value;
	}
	
	public static final class NumericalRange implements Serializable {
		
		private static final long serialVersionUID = 6193865138346459711L;
		
		private Double start;
		private Double end;
		
		public Double getStart() {
			return start;
		}
		
		public void setStart(Double start) {
			this.start = start;
		}
		
		public Double getEnd() {
			return end;
		}
		
		public void setEnd(Double end) {
			this.end = end;
		}
	}
}
