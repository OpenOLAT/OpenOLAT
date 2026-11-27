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
package org.olat.modules.coach.reports;

import java.util.Date;
import java.util.Locale;

/**
 * Initial date: 2025-01-29<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public abstract class TimeBoundReportConfiguration extends AbstractReportConfiguration {

	/**
	 * Resolves the time range of the parameters, with the current time as reference.
	 * 
	 * @param params The parameters of the report
	 * @return The time range, never null
	 */
	@Override
	protected ReportDateRange resolveRange(ReportParameters params) {
		// The locale is irrelevant, only the dates are used
		return new ReportParametersFormatter(Locale.ENGLISH).resolve(params, params.getReferenceDate() == null ? new Date() : params.getReferenceDate());
	}
}
