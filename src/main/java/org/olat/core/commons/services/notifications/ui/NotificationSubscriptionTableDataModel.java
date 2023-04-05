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
package org.olat.core.commons.services.notifications.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * Description:<br>
 * The subscription table data model displays the users notification
 * subscriptions
 * <p>
 * Initial Date: 22.12.2009 <br>
 *
 * @author gnaegi
 */
class NotificationSubscriptionTableDataModel extends DefaultFlexiTableDataModel<NotificationSubscriptionRow>
		implements SortableFlexiTableDataModel<NotificationSubscriptionRow> {

	private static final NotificationSubscriptionCols[] COLS = NotificationSubscriptionCols.values();
	private final Locale locale;

	NotificationSubscriptionTableDataModel(FlexiTableColumnModel tableColumnModel, Locale locale) {
		super(tableColumnModel);
		this.locale = locale;
	}


	@Override
	public Object getValueAt(int row, int col) {
		return getValueAt(getObject(row), col);
	}

	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<NotificationSubscriptionRow> rows = new NotificationSubscriptionSortableDelegate(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(NotificationSubscriptionRow row, int col) {
		switch (COLS[col]) {
			case key -> {
				return row.getKey();
			}
			case subType -> {
				return row.getSubType();
			}
			case learningResource -> {
				return row.getLearningResource();
			}
			case subRes -> {
				return row.getSubRes();
			}
			case addDesc -> {
				return row.getAddDesc();
			}
			case statusToggle -> {
				return row.getStatusToggle();
			}
			case creationDate -> {
				return row.getCreationDate();
			}
			case lastEmail -> {
				return row.getLastEmail();
			}
			case deleteLink -> {
				return row.getDeleteLink();
			}
			default -> {
				return "ERROR";
			}
		}
	}
}
