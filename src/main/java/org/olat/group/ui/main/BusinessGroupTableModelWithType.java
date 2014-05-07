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

package org.olat.group.ui.main;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupMembership;

/**
 * @author gnaegi
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupTableModelWithType extends DefaultTableDataModel<BGTableItem> {
	private final int columnCount;
	private final Translator trans;

	/**
	 * @param owned list of business groups
	 */
	public BusinessGroupTableModelWithType(Translator trans, int columnCount) {
		super(new ArrayList<BGTableItem>());
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
		BGTableItem wrapped = objects.get(row);
		switch (Cols.values()[col]) {
			case name:
				return wrapped.getBusinessGroup();
			case description:
				String description = wrapped.getBusinessGroupDescription();
				description = FilterFactory.getHtmlTagsFilter().filter(description);
				description = Formatter.truncate(description, 256);
				return description;
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
			case allowDelete: {
				Boolean allowed =  wrapped.getAllowDelete();
				if(allowed != null && allowed.booleanValue()) {
					//check managed groups
					if(BusinessGroupManagedFlag.isManaged(wrapped.getManagedFlags(), BusinessGroupManagedFlag.delete)) {
						return Boolean.FALSE;
					}
				}
				return allowed;
			}
			case resources:
				return wrapped;
			//fxdiff VCRP-1,2: access control of resources
			case accessControl:
				return new Boolean(wrapped.isAccessControl());
			case accessControlLaunch:
				if(wrapped.isAccessControl()) {
					if(wrapped.getMembership() != null) {
						return trans.translate("select");
					}
					return trans.translate("table.access");
				}
				return null;
			case accessTypes:
				return wrapped.getAccessTypes();
			case mark:
				return new Boolean(wrapped.isMarked());
			case lastUsage:
				return wrapped.getBusinessGroupLastUsage();
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
				return wrapped.getBusinessGroupKey().toString();
			case freePlaces: {
				Integer maxParticipants = wrapped.getMaxParticipants();
				if(maxParticipants != null && maxParticipants.intValue() >= 0) {
					long free = maxParticipants - (wrapped.getNumOfParticipants() + wrapped.getNumOfPendings());
					return new GroupNumber(free);
				}
				return GroupNumber.INFINITE;
			}
			case participantsCount: {
				long count = wrapped.getNumOfParticipants() + wrapped.getNumOfPendings();
				return count < 0 ? GroupNumber.ZERO : new GroupNumber(count);
			}
			case tutorsCount: {
				long count = wrapped.getNumOfOwners();
				return count < 0 ? GroupNumber.ZERO : new GroupNumber(count);
			}
			case waitingListCount: {
				if(wrapped.isWaitingListEnabled()) {
					long count = wrapped.getNumWaiting();
					return count < 0 ? GroupNumber.ZERO : new GroupNumber(count);
				}
				return GroupNumber.NONE;
			}
			case wrapper:
				return wrapped;
			case externalId:
				return wrapped.getBusinessGroupExternalId();
			default:
				return "ERROR";
		}
	}
	
	@Override
	//fxdiff VCRP-1,2: access control of resources
	public Object createCopyWithEmptyList() {
		return new BusinessGroupTableModelWithType(trans, columnCount);
	}
	
	public boolean filterEditableGroupKeys(UserRequest ureq, List<Long> groupKeys) {
		if(ureq.getUserSession().getRoles().isOLATAdmin() || ureq.getUserSession().getRoles().isGroupManager()) {
			return false;
		}
		
		int countBefore = groupKeys.size();
		
		for(BGTableItem item:getObjects()) {
			Long groupKey = item.getBusinessGroupKey();
			if(groupKeys.contains(groupKey)) {
				BusinessGroupMembership membership = item.getMembership();
				if(membership == null || !membership.isOwner()) {
					groupKeys.remove(groupKey);
				}
			}
		}
		
		return groupKeys.size() != countBefore;
	}

	/**
	 * @param owned
	 */
	public void setEntries(List<BGTableItem> owned) {
		setObjects(owned);
	}
	
	public void removeBusinessGroup(Long bgKey) {
		if(bgKey == null) return;
		
		for(int i=objects.size(); i-->0; ) {
			BGTableItem wrapped = objects.get(i);
			if(bgKey.equals(wrapped.getBusinessGroupKey())) {
				objects.remove(i);
				return;
			}
		}
	}
	
	public enum Cols {
		name("table.header.bgname"),
		description("table.header.description"),
		groupType(""),
		allowLeave("table.header.leave"),
		allowDelete("table.header.delete"),
		resources("table.header.resources"),
		accessControl(""),
		accessControlLaunch("table.header.ac"),
		accessTypes("table.header.ac.method"),
		mark("table.header.mark"),
		lastUsage("table.header.lastUsage"),
		role("table.header.role"),
		firstTime("table.header.firstTime"),
		lastTime("table.header.lastTime"),
		key("table.header.key"),
		freePlaces("table.header.freePlaces"),
		participantsCount("table.header.participantsCount"),
		tutorsCount("table.header.tutorsCount"),
		waitingListCount("table.header.waitingListCount"),
		wrapper(""),
		card("table.header.businesscard"),
		externalId("table.header.externalid");
		
		private final String i18n;
		
		private Cols(String i18n) {
			this.i18n = i18n;
		}
		
		public String i18n() {
			return i18n;
		}
	}
}