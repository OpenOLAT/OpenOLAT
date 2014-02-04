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
package org.olat.core.commons.services.notifications.ui;

import org.olat.ControllerFactory;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * The subscription table data model displays the users notification
 * subscriptions
 * <P>
 * Initial Date: 22.12.2009 <br>
 * 
 * @author gnaegi
 */
class NotificationSubscriptionTableDataModel extends DefaultTableDataModel<Subscriber> {
	Translator trans;

	NotificationSubscriptionTableDataModel(Translator translator) {
		super(null); // set at a later stage
		this.trans = translator;
		this.setLocale(trans.getLocale());
	}

	/**
	 * Add the column descriptors to the given table controller that matches with
	 * this data model
	 * 
	 * @param subscriptionsTableCtr
	 */
	void addTableColumns(TableController subscriptionsTableCtr) {
		subscriptionsTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("overview.column.type", 0, "launch", getLocale()));
		subscriptionsTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("overview.column.resname", 1, null, getLocale()));
		subscriptionsTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("overview.column.subidentifier", 2, null, getLocale()));
		subscriptionsTableCtr.addColumnDescriptor(new StaticColumnDescriptor("del", "overview.column.action", trans
				.translate("overview.column.action.cellvalue")));
	}

	/**
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return 4;
	}

	/**
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getValueAt(int,
	 *      int)
	 */
	@Override
	public Object getValueAt(int row, int col) {
		Subscriber sub = getObject(row);
		Publisher pub = sub.getPublisher();
		
		switch (col) {
			case 0:
				String innerType = pub.getType();
				String typeName = ControllerFactory.translateResourceableTypeName(innerType, getLocale());
				return typeName;
			case 1:
				String containerType = pub.getResName();
				String containerTypeTrans = ControllerFactory.translateResourceableTypeName(containerType, getLocale());
				return containerTypeTrans;
			case 2:
				NotificationsHandler handler = NotificationsManager.getInstance().getNotificationsHandler(pub);
				if(handler == null){
					return "";
				}
				String title = handler.createTitleInfo(sub, getLocale());
				if(title == null) {
					return "";
				}
				return title;
			default:
				return "ERROR";
		}
	}
}
