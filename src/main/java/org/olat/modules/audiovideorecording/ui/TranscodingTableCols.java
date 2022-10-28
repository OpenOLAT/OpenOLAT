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
package org.olat.modules.audiovideorecording.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;

/**
 * Initial date: 2022-10-25<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public enum TranscodingTableCols implements FlexiSortableColumnDef {
	id("transcoding.table.header.id", true),
	fileName("transcoding.table.header.fileName", true),
	fileSize("transcoding.table.header.fileSize", true),
	creationDate("transcoding.table.header.creationDate", true),
	status("transcoding.table.header.status", true),
	action("transcoding.table.header.action", false);

	private final String i18nKey;
	private final boolean sortable;

	TranscodingTableCols(String i18nKey, boolean sortable) {
		this.i18nKey = i18nKey;
		this.sortable = sortable;
	}


	@Override
	public String i18nHeaderKey() {
		return i18nKey;
	}

	@Override
	public boolean sortable() {
		return sortable;
	}

	@Override
	public String sortKey() {
		return name();
	}
}
