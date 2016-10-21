/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.admin.user.groups;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupMembership;

/**
 * @author gnaegi
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
class BusinessGroupTableModelWithType extends DefaultTableDataModel<GroupOverviewRow> {
	private final int columnCount;
	private final Translator trans;

	/**
	 * @param owned list of business groups
	 */
	public BusinessGroupTableModelWithType(Translator trans, int columnCount) {
		super(new ArrayList<GroupOverviewRow>());
		this.trans = trans;
		this.columnCount = columnCount;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return columnCount;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		GroupOverviewRow wrapped = objects.get(row);
		switch (Cols.values()[col]) {
			case name:
				return wrapped;
			case allowLeave: {
				Boolean allowed = wrapped.getAllowLeave();
				if(allowed != null && allowed.booleanValue()) {
					//check managed groups
					if(BusinessGroupManagedFlag.isManaged(wrapped.getManagedFlags(), BusinessGroupManagedFlag.membersmanagement)) {
						return Boolean.FALSE;
					}
				}
				return allowed;
			}
			case role:
				return wrapped.getMembership();
			case firstTime: {
				BusinessGroupMembership membership = wrapped.getMembership();
				return membership == null ? null : membership.getCreationDate();
			}
			case lastTime: {
				BusinessGroupMembership membership = wrapped.getMembership();
				return membership == null ? null : membership.getLastModified();
			}
			case key:
				return wrapped.getKey().toString();
			default:
				return "ERROR";
		}
	}
	
	@Override
	public Object createCopyWithEmptyList() {
		return new BusinessGroupTableModelWithType(trans, columnCount);
	}

	/**
	 * @param owned
	 */
	public void setEntries(List<GroupOverviewRow> owned) {
		setObjects(owned);
	}
	
	public enum Cols {
		name("table.header.bgname"),
		key("table.header.key"),
		firstTime("table.header.firstTime"),
		lastTime("table.header.lastTime"),
		role("table.header.role"),
		allowLeave("table.header.leave");
		
		private final String i18n;
		
		private Cols(String i18n) {
			this.i18n = i18n;
		}
		
		public String i18n() {
			return i18n;
		}
	}
}