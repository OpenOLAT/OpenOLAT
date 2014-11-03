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

package org.olat.group.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableDataModelWithMarkableRows;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;

/**
 * Description:<BR>
 * Extended business gropu table model with max size and current number of
 * participants as additional rows
 * <P>
 * Initial Date: Sep 9, 2004
 * 
 * @author gnaegi
 */
public class BusinessGroupTableModelWithMaxSize extends DefaultTableDataModel<BusinessGroup> implements TableDataModelWithMarkableRows<BusinessGroup>  {
	private static final OLog log = Tracing.createLoggerFor(BusinessGroupTableModelWithMaxSize.class);
	
	private static final int COLUMN_COUNT = 7;
	private List<Integer> members;
	private Translator trans;
	private Identity identity;
	private boolean cancelEnrollEnabled;
	private BusinessGroupService businessGroupService;

	/**
	 * @param groups List of business groups
	 * @param members List containing the number of participants for each group.
	 *          The index of the list corresponds with the index of the group list
	 * @param trans
	 */
	public BusinessGroupTableModelWithMaxSize(List<BusinessGroup> groups, List<Integer> members, Translator trans, Identity identity, boolean cancelEnrollEnabled) {
		super(groups);
		this.members = members;
		this.trans = trans;
		this.identity = identity;
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		this.cancelEnrollEnabled = cancelEnrollEnabled;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		BusinessGroup businessGroup = objects.get(row);
		Integer numbParts = members.get(row);
		Integer max = businessGroup.getMaxParticipants();
		switch (col) {
			case 0:
				return businessGroup.getName();
			case 1:
				String description = businessGroup.getDescription();
				description = FilterFactory.getHtmlTagsFilter().filter(description);
				description = Formatter.truncate(description, 256);
				return description;
			case 2:
				// Belegt/Plätze
				if (max == null) { 
					// no limit => return only members
					return numbParts; 
				}
				// return format 2/10
				StringBuilder buf = new StringBuilder();
				buf.append(numbParts);
				buf.append(trans.translate("grouplist.table.partipiciant.delimiter"));
				buf.append(businessGroup.getMaxParticipants());
				if(numbParts>businessGroup.getMaxParticipants()) {
				  log.info("Group overflow detected for the group: " + businessGroup + ", participants: " + numbParts + " maxParticipamts: " + businessGroup.getMaxParticipants());
				}
				return buf.toString();
			case 3:
				// Waiting-list
				if (businessGroup.getWaitingListEnabled().booleanValue()) {
					// Waitinglist is enabled => show current size
					int intValue = businessGroupService.countMembers(businessGroup, GroupRoles.waiting.name());
					return new Integer(intValue);
				}
				return trans.translate("grouplist.table.noWaitingList");
			case 4:
				// Status
				if (businessGroupService.hasRoles(identity, businessGroup, GroupRoles.participant.name())) {
					return trans.translate("grouplist.table.state.onPartipiciantList"); 
				} else if (businessGroupService.hasRoles(identity, businessGroup, GroupRoles.waiting.name())) {
					int pos = businessGroupService.getPositionInWaitingListFor(identity,businessGroup);
					String[] onWaitingListArgs = new String[] { Integer.toString(pos) };
					return trans.translate("grouplist.table.state.onWaitingList",onWaitingListArgs); 
				} else if (max != null && !businessGroup.getWaitingListEnabled().booleanValue() && (numbParts.intValue() >= max.intValue()) ) {
					return trans.translate("grouplist.table.state.enroll.full"); 
				}	else if (max != null && businessGroup.getWaitingListEnabled().booleanValue() && (numbParts.intValue() >= max.intValue()) ) {
					return trans.translate("grouplist.table.state.WaitingList");
				}
				return trans.translate("grouplist.table.state.notEnrolled");
			case 5:
				// Action enroll
				if (isEnrolledInAnyGroup(identity)) {
					// Allready enrolled => does not show action-link 'enroll'
					return Boolean.FALSE;
				}
				if (max != null && !businessGroup.getWaitingListEnabled().booleanValue() && (numbParts.intValue() >= max.intValue()) ) {
					// group is full => => does not show action-link 'enroll'
					return Boolean.FALSE;
				}
				return Boolean.TRUE;
			case 6:
				// Action cancel enrollment
				if (isEnrolledIn(businessGroup, identity)) {
          // check if user is on waiting-list
					if (businessGroupService.hasRoles(identity, businessGroup, GroupRoles.waiting.name())) {
            // user is on waitinglist => show allways action cancelEnrollment for waitinglist 
 					  return Boolean.TRUE;
					}
          // user is not on waitinglist => show action cancelEnrollment only if enabled 
					if (cancelEnrollEnabled) {
						return Boolean.TRUE;
					}
				}
				return Boolean.FALSE;
			default:
				return "ERROR";
		}
	}

	@Override
	public Object createCopyWithEmptyList() {
		return new BusinessGroupTableModelWithMaxSize(new ArrayList<BusinessGroup>(), members, trans, identity, cancelEnrollEnabled);
	}

	/**
	 * @param owned
	 */
	public void setEntries(List<BusinessGroup> owned) {
		this.objects = owned;
	}

	/**
	 * @param row
	 * @return the business group at the given row
	 */
	public BusinessGroup getBusinessGroupAt(int row) {
		return objects.get(row);
	}
	
	/**
	 * Check if an identity is in certain security-group.
	 * @param businessGroup
	 * @param ident
	 * @return true: Found identity in PartipiciantGroup or WaitingGroup.
	 */
	private boolean isEnrolledIn(BusinessGroup businessGroup, Identity ident) {
		if (businessGroupService.hasRoles(ident, businessGroup, GroupRoles.participant.name())
				|| businessGroupService.hasRoles(ident, businessGroup, GroupRoles.waiting.name())) {
			return true;
		} 
		return false;
	}
	
	/**
	 * Check if an identity is in any security-group.
	 * @param ident
	 * @return true: Found identity in any security-group of this table model.
	 */		
	private boolean isEnrolledInAnyGroup(Identity ident) {
		// loop over all business-groups
		for (BusinessGroup businessGroup:objects) {
			if (isEnrolledIn(businessGroup, ident) ) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getRowCssClass(int rowId) {
		BusinessGroup businessGroup = objects.get(rowId);
		boolean isEnrolled = isEnrolledIn(businessGroup, identity);
		return (isEnrolled ? "o_row_selected" : "");
	}

 
}