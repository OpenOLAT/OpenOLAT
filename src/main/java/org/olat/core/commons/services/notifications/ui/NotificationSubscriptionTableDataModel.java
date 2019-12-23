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

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
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
	
	private final NotificationsManager notificationsManager;

	NotificationSubscriptionTableDataModel(Translator translator) {
		super(null); // set at a later stage
		this.trans = translator;
		setLocale(trans.getLocale());
		notificationsManager = CoreSpringFactory.getImpl(NotificationsManager.class);
	}

	/**
	 * Add the column descriptors to the given table controller that matches with
	 * this data model
	 * 
	 * @param subscriptionsTableCtr
	 */
	void addTableColumns(TableController subscriptionsTableCtr, boolean admin) {
		subscriptionsTableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("overview.column.key", 0, "launch", getLocale()));
		subscriptionsTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("overview.column.type", 1, "launch", getLocale()));
		subscriptionsTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("overview.column.resname", 2, null, getLocale()));
		subscriptionsTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("overview.column.subidentifier", 3, null, getLocale()));
		subscriptionsTableCtr.addColumnDescriptor(admin, new DefaultColumnDescriptor("overview.column.creationDate", 4, null, getLocale()));
		subscriptionsTableCtr.addColumnDescriptor(admin, new DefaultColumnDescriptor("overview.column.lastEmail", 5, null, getLocale()));
		subscriptionsTableCtr.addColumnDescriptor(new StaticColumnDescriptor("del", "overview.column.action", trans
				.translate("overview.column.action.cellvalue")));
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Subscriber sub = getObject(row);
		Publisher pub = sub.getPublisher();
		
		switch (col) {
			case 0: return sub.getKey();
			case 1:
				String innerType = pub.getType();
				return NewControllerFactory.translateResourceableTypeName(innerType, getLocale());
			case 2:
				String containerType = pub.getResName();
				return NewControllerFactory.translateResourceableTypeName(containerType, getLocale());
			case 3:
				NotificationsHandler handler = notificationsManager.getNotificationsHandler(pub);
				if(handler == null){
					return "";
				}
				String title = handler.createTitleInfo(sub, getLocale());
				if(title == null) {
					return "";
				}
				return title;
			case 4: return sub.getCreationDate();
			case 5: return sub.getLatestEmailed();
			default:
				return "ERROR";
		}
	}
}
