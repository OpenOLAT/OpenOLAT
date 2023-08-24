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
package org.olat.course.quota.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;

/**
 * Initial date: Jul 04, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public enum CourseQuotaUsageCols implements FlexiSortableColumnDef {
	resource("table.header.course.quota.resource"),
	type("table.header.course.quota.type"),
	external("table.header.course.quota.external"),
	numOfFiles("table.header.course.quota.num.files"),
	totalUsedSize("table.header.course.quota.size"),
	quota("table.header.course.quota.quota"),
	curUsed("table.header.course.quota.used"),
	editQuota("table.header.course.quota.edit.quota"),
	displayRss("table.header.course.quota.display.rss");

	private final String i18nKey;

	CourseQuotaUsageCols(String i18nKey) {
		this.i18nKey = i18nKey;
	}

	@Override
	public String i18nHeaderKey() {
		return i18nKey;
	}

	@Override
	public boolean sortable() {
		return false;
	}

	@Override
	public String sortKey() {
		return name();
	}
}
