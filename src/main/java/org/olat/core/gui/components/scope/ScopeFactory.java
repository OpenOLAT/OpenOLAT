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
package org.olat.core.gui.components.scope;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 24 Nov 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ScopeFactory {
	
	public static ScopeSelection createScopeSelection(String name, VelocityContainer vc, ComponentEventListener listener,
			List<Scope> scopes) {
		ScopeSelection comp = new ScopeSelection(name);
		comp.setScopes(scopes);
		if (listener != null) {
			comp.addListener(listener);
		}
		if (vc != null) {
			vc.put(comp.getComponentName(), comp);
		}
		return comp;
	}
	
	public static Scope createScope(String identifier, String displayName, String hint) {
		return new ScopeImpl(identifier, displayName, hint);
	}
	
	public static DateScopeSelection createDateScopeSelection(WindowControl wControl, String name, VelocityContainer vc,
			ComponentEventListener listener, List<DateScope> dateScopes, Locale locale) {
		DateScopeSelection comp = new DateScopeSelection(wControl, name, locale);
		comp.setDateScopes(dateScopes);
		if (listener != null) {
			comp.addListener(listener);
		}
		if (vc != null) {
			vc.put(comp.getComponentName(), comp);
		}
		return comp;
	}
	
	public static DateScope createDateScope(String identifier, String displayName, String hint, DateRange dateRange) {
		return new DateScopeImpl(identifier, displayName, hint, dateRange);
	}
	
	public static String formatDateRange(Translator translator, Formatter formatter, DateRange dateRange) {
		return translator.translate("date.scope.range.hint", formatter.formatDate(dateRange.getFrom()), formatter.formatDate(dateRange.getTo()));
	}
	
	public static DateScopesBuilder dateScopesBuilder(Locale locale) {
		return new DateScopesBuilder(locale);
	}
	
	public static class DateScopesBuilder {
		
		private final Translator translator;
		private final Formatter formatter;
		private List<DateScope> dateScopes = new ArrayList<>(4);
		private int counter = 0;

		private DateScopesBuilder(Locale locale) {
			translator = Util.createPackageTranslator(ScopeFactory.class, locale);
			formatter = Formatter.getInstance(locale);
		}
		
		public DateScopesBuilder nextWeeks(int numOfWeeks) {
			DateRange dateRange = new DateRange(DateUtils.getStartOfDay(new Date()), DateUtils.getEndOfDay(DateUtils.addDays(new Date(), numOfWeeks * 7)));
			String displayName = numOfWeeks == 1
					? translator.translate("date.scope.next.week")
					: translator.translate("date.scope.next.weeks", String.valueOf(numOfWeeks));
			String hint = formatDateRange(dateRange);
			dateScopes.add(createDateScope("scope." + counter++, displayName, hint, dateRange));
			return this;
		}
		
		public DateScopesBuilder nextMonths(int numOfMonths) {
			DateRange dateRange = new DateRange(DateUtils.getStartOfDay(new Date()), DateUtils.getEndOfDay(DateUtils.addMonth(new Date(), numOfMonths)));
			String displayName = numOfMonths == 1
					? translator.translate("date.scope.next.month")
					: translator.translate("date.scope.next.months", String.valueOf(numOfMonths));
			String hint = formatDateRange(dateRange);
			dateScopes.add(createDateScope("scope." + counter++, displayName, hint, dateRange));
			return this;
		}
		
		public DateScopesBuilder toEndOfMonth() {
			LocalDate lastDayOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
			String nameOfMonth = Month.from(lastDayOfMonth).getDisplayName(TextStyle.FULL_STANDALONE, translator.getLocale());
			DateRange dateRange = new DateRange(DateUtils.getStartOfDay(new Date()), DateUtils.getEndOfDay(DateUtils.toDate(lastDayOfMonth)));
			String displayName = translator.translate("date.scope.to.end.month", nameOfMonth);
			String hint = formatDateRange(dateRange);
			dateScopes.add(createDateScope("scope." + counter++, displayName, hint, dateRange));
			return this;
		}
		
		public DateScopesBuilder toEndOfYear() {
			DateRange dateRange = new DateRange(DateUtils.getStartOfDay(new Date()), DateUtils.getEndOfYear(new Date()));
			String displayName = translator.translate("date.scope.to.end.year", String.valueOf(DateUtils.toLocalDate(dateRange.getTo()).getYear()));
			String hint = formatDateRange(dateRange);
			dateScopes.add(createDateScope("scope." + counter++, displayName, hint, dateRange));
			return this;
		}
		
		public DateScopesBuilder christmasToNewYear() {
			int year = LocalDate.now().getYear();
			if (LocalDate.now().getMonth() == Month.JANUARY && LocalDate.now().getDayOfMonth() <= 2) {
				year--;
			}
			DateRange dateRange = new DateRange(
					new GregorianCalendar(year, 11, 22, 0, 0, 0).getTime(),
					new GregorianCalendar(year + 1, 0, 2, 23, 59, 59).getTime());
			String displayName = translator.translate("date.scope.christmas.newyear");
			String hint = formatDateRange(dateRange);
			dateScopes.add(createDateScope("scope." + counter++, displayName, hint, dateRange));
			return this;
		}
		
		public DateScopesBuilder add(DateScope dateScope) {
			dateScopes.add(dateScope);
			return this;
		}
		
		private String formatDateRange(DateRange dateRange) {
			return ScopeFactory.formatDateRange(translator, formatter, dateRange);
		}
		
		public List<DateScope> build() {
			return List.copyOf(dateScopes);
		}
		
	}

}
