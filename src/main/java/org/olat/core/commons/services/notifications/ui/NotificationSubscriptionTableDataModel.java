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

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * Description:<br>
 * The subscription table data model displays the users notification
 * subscriptions
 * <p>
 * Initial Date: 22.12.2009 <br>
 *
 * @author gnaegi
 */
class NotificationSubscriptionTableDataModel extends DefaultFlexiTableDataModel<NotificationSubscriptionRow> {

	NotificationSubscriptionTableDataModel(FlexiTableColumnModel tableColumnModel) {
		super(tableColumnModel);
	}


	@Override
	public Object getValueAt(int row, int col) {
		NotificationSubscriptionRow call = getObject(row);

		switch (NotificationSubscriptionCols.values()[col]) {
			case key -> {
				return call.getKey();
			}
			case subType -> {
				return call.getSubType();
			}
			case courseGroup -> {
				return call.getCourseGroup();
			}
			case subRes -> {
				return call.getSubRes();
			}
			case addDesc -> {
				return call.getAddDesc();
			}
			case statusToggle -> {
				return call.getStatusToggle();
			}
			case creationDate -> {
				return call.getCreationDate();
			}
			case lastEmail -> {
				return call.getLastEmail();
			}
			case deleteLink -> {
				return call.getDeleteLink();
			}
			default -> {
				return "ERROR";
			}
		}
	}
}
