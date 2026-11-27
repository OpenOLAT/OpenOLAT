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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.util.DateUtils;

/**
 * The parameters of a report run. A plain value object without UI state,
 * serialized with the report task.
 *
 * Initial date: 25 Nov 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ReportParameters implements Serializable {

	private static final long serialVersionUID = 2316862981045735310L;

	private ReportTimeRange timeRange;
	private Date from;
	private Date to;
	private Long timePeriodKey;
	private Date changedSince;
	private Date referenceDate;

	/**
	 * @param definitions The parameter definitions of a report template
	 * @return The parameters with the default values of the definitions
	 */
	public static ReportParameters defaults(List<ReportParameterDefinition> definitions) {
		ReportParameters params = new ReportParameters();
		for(ReportParameterDefinition definition:definitions) {
			if(definition.name() == ReportParameterName.timeRange) {
				params.setTimeRange(definition.defaultTimeRange());
			}
		}
		return params;
	}

	public ReportTimeRange getTimeRange() {
		return timeRange;
	}

	public void setTimeRange(ReportTimeRange timeRange) {
		this.timeRange = timeRange;
	}

	/**
	 * @return The first day of a custom range
	 */
	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	/**
	 * @return The last day (inclusive) of a custom range
	 */
	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}

	/**
	 * @return The key of the time period (repository entry lifecycle)
	 */
	public Long getTimePeriodKey() {
		return timePeriodKey;
	}

	public void setTimePeriodKey(Long timePeriodKey) {
		this.timePeriodKey = timePeriodKey;
	}

	/**
	 * @return The day from which on (inclusive) changes are reported
	 */
	public Date getChangedSince() {
		return changedSince;
	}

	public void setChangedSince(Date changedSince) {
		this.changedSince = changedSince;
	}

	/**
	 * @return The time the report was started, the relative ranges and the fiscal
	 * 		years are resolved from it. Null for the tasks started before this field
	 * 		existed, they use the time of the generation.
	 */
	public Date getReferenceDate() {
		return referenceDate;
	}

	public void setReferenceDate(Date referenceDate) {
		this.referenceDate = referenceDate;
	}

	/**
	 * Validates the parameters against the definitions of a report template.
	 *
	 * @param definitions The parameter definitions of the template
	 * @param now The current date
	 * @return The list of invalid parameters, empty if all are valid
	 */
	public List<ReportParameterName> validate(List<ReportParameterDefinition> definitions, Date now) {
		List<ReportParameterName> invalid = new ArrayList<>(2);

		boolean timeRangeSupported = isSupported(definitions, ReportParameterName.timeRange);
		if(timeRangeSupported) {
			if(timeRange == null
					|| (timeRange == ReportTimeRange.custom && (from == null || to == null || to.before(from)))
					|| (timeRange == ReportTimeRange.timePeriod && timePeriodKey == null)) {
				invalid.add(ReportParameterName.timeRange);
			}
		} else if(timeRange != null && timeRange != ReportTimeRange.all) {
			invalid.add(ReportParameterName.timeRange);
		}

		if(changedSince != null) {
			Date startOfTomorrow = DateUtils.toDate(DateUtils.toLocalDate(now).plusDays(1));
			if(!isSupported(definitions, ReportParameterName.changedSince) || !changedSince.before(startOfTomorrow)) {
				invalid.add(ReportParameterName.changedSince);
			}
		}
		return invalid;
	}

	private static boolean isSupported(List<ReportParameterDefinition> definitions, ReportParameterName name) {
		return definitions.stream().anyMatch(def -> def.name() == name);
	}
}
