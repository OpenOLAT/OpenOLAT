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
package org.olat.modules.coach.reports;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.LongFunction;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.coach.ui.manager.ReportParametersController;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * Resolves the parameters of a report to dates and describes them for the
 * people: the suffix of the title, the summary saved in the description of
 * the export and the time range of the metadata sheet. The texts never
 * contain names of persons.
 *
 * Initial date: 27 Nov 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ReportParametersFormatter {

	/**
	 * The length of the column e_description of the export metadata.
	 */
	public static final int MAX_DESCRIPTION_LENGTH = 4000;

	private final Translator translator;
	private final Formatter formatter;
	private final int fiscalYearStartDay;
	private final int fiscalYearStartMonth;
	private final LongFunction<RepositoryEntryLifecycle> timePeriodLoader;

	/**
	 * @param locale The locale of the texts
	 */
	public ReportParametersFormatter(Locale locale) {
		this(locale, CoreSpringFactory.getImpl(CurriculumModule.class),
				CoreSpringFactory.getImpl(RepositoryEntryLifecycleDAO.class));
	}

	private ReportParametersFormatter(Locale locale, CurriculumModule curriculumModule,
			RepositoryEntryLifecycleDAO lifecycleDao) {
		this(locale, curriculumModule.getReportsAccountingFiscalYearStartDay(),
				curriculumModule.getReportsAccountingFiscalYearStartMonth(), lifecycleDao::loadById);
	}

	/**
	 * @param locale The locale of the texts
	 * @param fiscalYearStartDay The day of the start of the fiscal year (1 - 31)
	 * @param fiscalYearStartMonth The month of the start of the fiscal year (1 - 12)
	 * @param timePeriodLoader Loads a time period by its primary key
	 */
	public ReportParametersFormatter(Locale locale, int fiscalYearStartDay, int fiscalYearStartMonth,
			LongFunction<RepositoryEntryLifecycle> timePeriodLoader) {
		this.translator = Util.createPackageTranslator(ReportParametersController.class, locale);
		this.formatter = Formatter.getInstance(locale);
		this.fiscalYearStartDay = fiscalYearStartDay;
		this.fiscalYearStartMonth = fiscalYearStartMonth;
		this.timePeriodLoader = timePeriodLoader;
	}

	/**
	 * Resolves the time range of the parameters.
	 *
	 * @param params The parameters, null means "All time"
	 * @param reference The reference time, typically the start of the report
	 * @return The range, never null
	 * @throws ReportGenerationException If the time period of the parameters doesn't exist anymore
	 */
	public ReportDateRange resolve(ReportParameters params, Date reference) {
		ReportTimeRange timeRange = params == null ? null : params.getTimeRange();
		if (timeRange == null) {
			return ReportDateRange.ALL;
		}

		Date from = params.getFrom();
		Date to = params.getTo();
		if (timeRange == ReportTimeRange.timePeriod) {
			RepositoryEntryLifecycle timePeriod = loadTimePeriod(params);
			if (timePeriod == null) {
				throw new ReportGenerationException("Time period not found: " + params.getTimePeriodKey());
			}
			from = timePeriod.getValidFrom();
			to = timePeriod.getValidTo();
		}
		return timeRange.resolve(reference, from, to, fiscalYearStartDay, fiscalYearStartMonth);
	}

	/**
	 * The title of a generated report, for example "Bookings - Last 4 weeks" or
	 * "Bookings - 01.01.2026 to 31.03.2026, changed since 01.02.2026".
	 *
	 * @param name The name of the report template
	 * @param params The parameters
	 * @param reference The reference time, typically the start of the report
	 * @return The title
	 */
	public String formatTitle(String name, ReportParameters params, Date reference) {
		ReportTimeRange timeRange = getTimeRange(params);

		String suffix;
		if (timeRange == ReportTimeRange.custom) {
			suffix = formatRange(resolve(params, reference), false);
		} else if (timeRange == ReportTimeRange.timePeriod) {
			suffix = formatTimePeriodName(params, reference);
		} else {
			suffix = formatTimeRangeLabel(timeRange);
		}

		if (params != null && params.getChangedSince() != null) {
			suffix = translator.translate("report.title.changed.since", suffix, formatter.formatDate(params.getChangedSince()));
		}
		return translator.translate("report.title.params", name, suffix);
	}

	/**
	 * The summary of the parameters, truncated to the length of the description
	 * of the export metadata.
	 *
	 * @see #formatSummary(ReportConfiguration, ReportParameters, Date)
	 */
	public String formatDescription(ReportConfiguration template, ReportParameters params, Date reference) {
		return Formatter.truncate(formatSummary(template, params, reference), MAX_DESCRIPTION_LENGTH);
	}

	/**
	 * The summary of the parameters, one line per parameter, for example
	 * "Booking date: Current year (01.01.2026 to 31.12.2026)". The presets
	 * show their resolved dates.
	 *
	 * @param template The report template, gives the label of the time range
	 * @param params The parameters
	 * @param reference The reference time, typically the start of the report
	 * @return The summary, the lines are separated by a line break
	 */
	public String formatSummary(ReportConfiguration template, ReportParameters params, Date reference) {
		ReportTimeRange timeRange = getTimeRange(params);
		Locale locale = translator.getLocale();

		String value;
		if (timeRange == ReportTimeRange.all) {
			value = formatTimeRangeLabel(timeRange);
		} else {
			String dates = formatRange(resolve(params, reference), false);
			if (timeRange == ReportTimeRange.custom) {
				value = dates;
			} else if (timeRange == ReportTimeRange.timePeriod) {
				String name = formatTimePeriodName(params, reference);
				value = name.equals(dates) ? dates : name + " (" + dates + ")";
			} else {
				value = formatTimeRangeLabel(timeRange) + " (" + dates + ")";
			}
		}

		List<String> lines = new ArrayList<>(2);
		lines.add(template.getParametersText(ReportParametersText.timeRange, locale) + ": " + value);
		if (params != null && params.getChangedSince() != null) {
			lines.add(translator.translate("report.params.changed.since") + ": " + formatter.formatDate(params.getChangedSince()));
		}
		return String.join("\n", lines);
	}

	/**
	 * The time range as text, "All time" or the first and the last day.
	 *
	 * @param range The resolved range
	 * @param dash true to separate the days with an en dash, false with the word "to"
	 * @return The text
	 */
	public String formatRange(ReportDateRange range, boolean dash) {
		if (range == null || (range.from() == null && range.toExclusive() == null)) {
			return formatTimeRangeLabel(ReportTimeRange.all);
		}

		String from = range.from() == null ? "" : formatter.formatDate(range.from());
		String to = range.toExclusive() == null ? "" : formatter.formatDate(lastDay(range.toExclusive()));
		if (dash) {
			return (from + " – " + to).trim();
		}
		return translator.translate("report.title.range", from, to).trim();
	}

	/**
	 * @param toExclusive The exclusive end of a range
	 * @return The last day of the range: the day before if the end is midnight,
	 * 		else the day of the end (relative ranges end now)
	 */
	private static Date lastDay(Date toExclusive) {
		if (toExclusive.equals(DateUtils.getStartOfDay(toExclusive))) {
			return DateUtils.addDays(toExclusive, -1);
		}
		return toExclusive;
	}

	private String formatTimeRangeLabel(ReportTimeRange timeRange) {
		return translator.translate("report.params.time.range." + timeRange.name());
	}

	private String formatTimePeriodName(ReportParameters params, Date reference) {
		RepositoryEntryLifecycle timePeriod = loadTimePeriod(params);
		if (timePeriod != null && StringHelper.containsNonWhitespace(timePeriod.getLabel())) {
			return timePeriod.getLabel();
		}
		return formatRange(resolve(params, reference), false);
	}

	private RepositoryEntryLifecycle loadTimePeriod(ReportParameters params) {
		return params.getTimePeriodKey() == null ? null : timePeriodLoader.apply(params.getTimePeriodKey().longValue());
	}

	private static ReportTimeRange getTimeRange(ReportParameters params) {
		return params == null || params.getTimeRange() == null ? ReportTimeRange.all : params.getTimeRange();
	}
}
