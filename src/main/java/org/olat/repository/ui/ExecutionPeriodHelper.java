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
package org.olat.repository.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.util.DateUtils;

/**
 *
 * Initial date: 2026-09-24<br>
 * @author uhensler, https://www.frentix.com
 *
 */
public class ExecutionPeriodHelper {

	public static String getDatesType(Date start, Date end) {
		if (start == null && end == null) {
			return "none";
		}
		return start != null && end != null && DateUtils.isSameDay(start, end) ? "oneday" : "private";
	}

	public static void updateVisibility(String type, DateChooser privateDatesEl, SingleSelection publicDatesEl) {
		boolean oneDay = "oneday".equals(type);
		if (publicDatesEl != null) {
			publicDatesEl.setVisible("public".equals(type));
		}
		privateDatesEl.setVisible("private".equals(type) || oneDay);
		privateDatesEl.setSecondDate(!oneDay);
		privateDatesEl.setLabel(oneDay ? "cif.date" : "cif.private.dates", null);
	}

	public static class ExecutionPeriodCache {

		private Date cachedStartDate;
		private Date cachedEndDate;

		public void restoreOrCache(DateChooser privateDatesEl, String type) {
			if ("private".equals(type) || "oneday".equals(type)) {
				if (privateDatesEl.getDate() == null && cachedStartDate != null) {
					privateDatesEl.setDate(cachedStartDate);
				}
				if (privateDatesEl.getSecondDate() == null && cachedEndDate != null) {
					privateDatesEl.setSecondDate(cachedEndDate);
				}
			} else {
				if (privateDatesEl.getDate() != null) {
					cachedStartDate = privateDatesEl.getDate();
				}
				if (privateDatesEl.getSecondDate() != null) {
					cachedEndDate = privateDatesEl.getSecondDate();
				}
			}
		}
	}
}
