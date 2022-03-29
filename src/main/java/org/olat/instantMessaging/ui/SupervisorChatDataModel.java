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
package org.olat.instantMessaging.ui;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.instantMessaging.InstantMessage;
import org.olat.instantMessaging.InstantMessageTypeEnum;
import org.olat.instantMessaging.model.RosterChannelInfos;
import org.olat.instantMessaging.model.RosterChannelInfos.RosterStatus;
import org.olat.instantMessaging.ui.component.RosterEntryWithUnreadCellRenderer;

/**
 * 
 * Initial date: 21 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SupervisorChatDataModel extends DefaultFlexiTableDataModel<RosterRow>
implements SortableFlexiTableDataModel<RosterRow>, FlexiTableCssDelegate {
	
	private static final Logger log = Tracing.createLoggerFor(SupervisorChatDataModel.class);
	private static final SupervisedChatCols[] COLS = SupervisedChatCols.values();
	protected static final long FIVE_MINUTES_MS = 5 * 60 * 1000l;
	
	private Date now;
	private final Locale locale;
	private List<RosterRow> backups;
	private final IdentityRef identity;
	private final String videoMarker;
	
	private Set<Long> identityKeys;
	
	public SupervisorChatDataModel(FlexiTableColumnModel columnModel, IdentityRef identity, Translator translator) {
		super(columnModel);
		this.now = new Date();
		this.locale = translator.getLocale();
		this.identity = identity;
		this.videoMarker = "<span><i class='o_icon o_icon-fw o_livestream_icon'> </i> " + translator.translate("meeting.invitation") + "</span>";
	}
	
	@Override
	public void sort(SortKey orderBy) {
		try {
			List<RosterRow> views = new SupervisorChatTableModelSortDelegate(orderBy, this, locale)
					.sort();
			super.setObjects(views);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	public boolean isInRoster(Long identityKey) {
		return identityKeys != null && identityKeys.contains(identityKey);
	}
	
	public RosterRow getObjectByChannel(String channel) {
		return getObjects().stream()
				.filter(row -> channel.equals(row.getChannel()))
				.findFirst()
				.orElse(null);
	}

	public void filter(FlexiFiltersTab tab) {
		String id = tab == null ? "All" : tab.getId();
		List<RosterRow> filteredRows;
		if("Active".equals(id)) {
			filteredRows = backups.stream()
					.filter(r -> r.getRosterStatus() == RosterStatus.active)
					.collect(Collectors.toList());
		} else if("Completed".equals(id)) {
			filteredRows = backups.stream()
					.filter(r -> (r.getRosterStatus() == RosterStatus.completed || r.getRosterStatus() == RosterStatus.ended))
					.collect(Collectors.toList());
		} else if("Requested".equals(id)) {
			filteredRows = backups.stream()
					.filter(r -> r.getRosterStatus() == RosterStatus.request)
					.collect(Collectors.toList());
		} else if("My".equals(id)) {
			filteredRows = backups.stream()
					.filter(r -> r.inRoster(identity))
					.collect(Collectors.toList());
		} else {
			filteredRows = backups;
		}
		super.setObjects(filteredRows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		RosterRow sRow = getObject(row);
		return getValueAt(sRow, col);
	}

	@Override
	public Object getValueAt(RosterRow row, int col) {
		switch(COLS[col]) {
			case portrait: return row.getRoster();
			case online: return row.getOnlineStatus();
			case participant: return row.getRoster();
			case lastMessage: return getMessage(row);
			case supervisor: return RosterEntryWithUnreadCellRenderer.getName(row.getRoster(), true);
			case status: return row.getRosterStatus();
			case lastActivity: return row.getLastActivity();
			case join: return row.getJoinLink();
			case tools: return row.getToolLink();
			default: return "ERROR";
		}
	}
	
	private String getMessage(RosterRow row) {
		InstantMessage msg = row.getLastTextMessage();
		if(msg != null) {
			if(msg.getType() == InstantMessageTypeEnum.meeting) {
				return videoMarker;
			} else if(StringHelper.containsNonWhitespace(msg.getBody())) {
				return Formatter.truncate(msg.getBody(), 64, "\u2026");
			}
		}
		return null;
	}
	
	@Override
	public void setObjects(List<RosterRow> objects) {
		super.setObjects(objects);
		backups = objects;
		
		final Set<Long> keys = new HashSet<>();
		for(RosterRow row:objects) {
			row.getRoster().getEntries()
				.forEach(entry -> keys.add(entry.getIdentityKey()));
		}
		identityKeys = keys;
	}
	
	public Date getLastActivity() {
		Date lastActivity = null;
		
		List<RosterRow> currentRows = getObjects();
		for(RosterRow row:currentRows) {
			Date activity = row.getLastActivity();
			RosterStatus status = row.getRosterStatus();
			RosterChannelInfos roster = row.getRoster();
			if((status == RosterStatus.request || status == RosterStatus.active)
					&& activity != null && (lastActivity == null || activity.after(lastActivity))
					&& (!roster.hasActiveVipEntries() || (roster.inRoster(identity) && roster.hasUnreadMessages()))) {
				lastActivity = activity;
			}
		}
			
		return lastActivity;
	}
	
	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		now = new Date();
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		now = new Date();
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		RosterRow row = getObject(pos);
		RosterChannelInfos roster = row.getRoster();
		RosterStatus status = row.getRosterStatus();
		Date lastActivity = row.getLastActivity();
		if((status == RosterStatus.request || status == RosterStatus.active)
				&& (lastActivity != null && (now.getTime() - lastActivity.getTime()) > FIVE_MINUTES_MS)
				&& (!roster.hasActiveVipEntries() || (roster.inRoster(identity) && roster.hasUnreadMessages()))) {
			return "o_im_danger";
		}
		if(status == RosterStatus.request) {
			return "o_im_request";
		}
		if(status == RosterStatus.active && row.getRoster().hasUnreadMessages()) {
			return "o_im_unread";
		}
		return null;
	}

	public enum SupervisedChatCols implements FlexiSortableColumnDef {
		portrait("table.header.portrait"),
		online("table.header.status"),
		participant("table.header.participant"),
		lastMessage("table.header.last.message"),
		supervisor("table.header.supervisor"),
		lastActivity("table.header.last.activity"),
		status("table.header.status"),
		join("table.header.action"),
		tools("table.header.actions");

		private final String i18nKey;

		private SupervisedChatCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != portrait && this != join && this != tools;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
