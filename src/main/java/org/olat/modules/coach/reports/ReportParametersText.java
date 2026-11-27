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

/**
 * The texts of the parameters dialog which depend on the report template,
 * for example "Booking date" or "Issued date". The template translates them
 * with its own bundle.
 *
 * Initial date: 27 Nov 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public enum ReportParametersText {

	/**
	 * The title of the dialog.
	 */
	title("title"),
	/**
	 * The label of the time range, for example "Booking date".
	 */
	timeRange("time.range"),
	/**
	 * The description of the card "All time".
	 */
	allDescription("all.desc"),
	/**
	 * The description of the card "Specific date range".
	 */
	specificDescription("specific.desc"),
	/**
	 * The description of the card "Relative to today".
	 */
	relativeDescription("relative.desc"),
	/**
	 * The hint below "Only changes since" (only templates which support it).
	 */
	changedSinceHint("changed.since.hint");

	private final String i18nSuffix;

	private ReportParametersText(String i18nSuffix) {
		this.i18nSuffix = i18nSuffix;
	}

	public String i18nSuffix() {
		return i18nSuffix;
	}
}
