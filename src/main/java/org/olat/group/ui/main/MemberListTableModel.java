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
package org.olat.group.ui.main;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.instantMessaging.model.Presence;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MemberListTableModel extends DefaultFlexiTableDataModel<MemberRow> implements SortableFlexiTableDataModel<MemberRow> {
	
	private final boolean onlineStatusEnabled;
	
	public MemberListTableModel(FlexiTableColumnModel columnModel, boolean onlineStatusEnabled) {
		super(columnModel);
		this.onlineStatusEnabled = onlineStatusEnabled;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<MemberRow> views = new MemberListTableSort(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		MemberRow member = getObject(row);
		return getValueAt(member, col);
	}

	@Override
	public Object getValueAt(MemberRow row, int col) {
		if(col >= 0 && col < Cols.values().length) {
			switch(Cols.values()[col]) {
				case username: return row.getView().getIdentityName();
				case firstTime: return row.getFirstTime();
				case lastTime: return row.getLastTime();
				case role: return row.getMembership();
				case groups: return row;
				case online: {
					FormLink chatLink = row.getChatLink();
					if(chatLink != null) {
						String onlineStatus = row.getOnlineStatus();
						if ("me".equals(onlineStatus)) {
							//no icon
						} else if (!onlineStatusEnabled) {
							// don't show the users status when not configured, only an icon to start a chat/message
							chatLink.setIconLeftCSS("o_icon o_icon_status_chat");
						}
						// standard case: available or unavailable (offline or dnd)
						else if(Presence.available.name().equals(onlineStatus)) {
							chatLink.setIconLeftCSS("o_icon o_icon_status_available");
						} else if(Presence.dnd.name().equals(onlineStatus)) {
							chatLink.setIconLeftCSS("o_icon o_icon_status_dnd");
						} else {
							chatLink.setIconLeftCSS("o_icon o_icon_status_unavailable");
						}
						if(chatLink.getComponent() != null) {
							chatLink.getComponent().setDirty(false);
						}
					}
					return chatLink;
				}
				case tools: return row.getToolsLink();
				default: return "ERROR";
			}
		}
		
		int propPos = col - AbstractMemberListController.USER_PROPS_OFFSET;
		return row.getView().getIdentityProp(propPos);
	}

	@Override
	public MemberListTableModel createCopyWithEmptyList() {
		return new MemberListTableModel(getTableColumnModel(), onlineStatusEnabled);
	}

	public enum Cols implements FlexiSortableColumnDef {
		username("table.header.login"),
		firstTime("table.header.firstTime"),
		lastTime("table.header.lastTime"),
		role("table.header.role"),
		groups("table.header.groups"),
		online("table.header.online"),
		tools("tools");
		
		private final String i18n;
		
		private Cols(String i18n) {
			this.i18n = i18n;
		}
		
		public String i18n() {
			return i18n;
		}

		@Override
		public String i18nHeaderKey() {
			return i18n;
		}

		@Override
		public boolean sortable() {
			return true;
		}
		@Override
		public String sortKey() {
			return i18n;
		}
	}
}
