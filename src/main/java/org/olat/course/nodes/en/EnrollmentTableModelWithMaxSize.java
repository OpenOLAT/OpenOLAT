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

package org.olat.course.nodes.en;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableDataModelWithMarkableRows;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;

/**
 * Description:<BR>
 * Extended business gropu table model with max size and current number of
 * participants as additional rows
 * <P>
 * Initial Date: Sep 9, 2004
 * 
 * @author gnaegi
 */
public class EnrollmentTableModelWithMaxSize extends DefaultTableDataModel<EnrollmentRow> implements TableDataModelWithMarkableRows<EnrollmentRow>  {
	private static final Logger log = Tracing.createLoggerFor(EnrollmentTableModelWithMaxSize.class);
	
	private static final int COLUMN_COUNT = 7;

	private final Translator trans;
	private final Identity identity;
	private final boolean cancelEnrollEnabled;
	private final int maxEnrolCount;

	/**
	 * @param groups List of business groups
	 * @param members List containing the number of participants for each group.
	 *          The index of the list corresponds with the index of the group list
	 * @param trans
	 */
	public EnrollmentTableModelWithMaxSize(List<EnrollmentRow> groups, Translator trans, Identity identity,
			boolean cancelEnrollEnabled, int maxEnrolCount) {
		super(groups);
		this.trans = trans;
		this.identity = identity;
		this.cancelEnrollEnabled = cancelEnrollEnabled;
		this.maxEnrolCount = maxEnrolCount;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int col) {
		EnrollmentRow enrollmentRow = objects.get(row);
		int numOfParticipants = enrollmentRow.getNumOfParticipants() + enrollmentRow.getNumOfReservations();
		int max = enrollmentRow.getMaxParticipants();
		switch (col) {
			case 0: return enrollmentRow.getSortKey();
			case 1: return enrollmentRow.getName();
			case 2: return enrollmentRow.getDescription();
			case 3:
				// Belegt/Plätze
				if (max < 0) { 
					// no limit => return only members
					return numOfParticipants; 
				}
				// return format 2/10
				StringBuilder buf = new StringBuilder();
				buf.append(numOfParticipants)
				   .append(trans.translate("grouplist.table.partipiciant.delimiter"))
				   .append(max);
				if(numOfParticipants > max) {
					log.info("Group overflow detected for the group: " + enrollmentRow.getKey() + "[name=" + enrollmentRow.getName() + "], participants: " + numOfParticipants + " maxParticipamts: " + enrollmentRow.getMaxParticipants());
				}
				return buf.toString();
			case 4:
				// Waiting-list
				if (enrollmentRow.isWaitingListEnabled()) {
					// Waitinglist is enabled => show current size
					return Integer.valueOf(enrollmentRow.getNumInWaitingList());
				}
				return trans.translate("grouplist.table.noWaitingList");
			case 5:
				// Status
				if (enrollmentRow.isParticipant()) {
					return trans.translate("grouplist.table.state.onPartipiciantList"); 
				} else if (enrollmentRow.isWaiting()) {
					int pos = enrollmentRow.getPositionInWaitingList();
					String[] onWaitingListArgs = new String[] { Integer.toString(pos) };
					return trans.translate("grouplist.table.state.onWaitingList",onWaitingListArgs); 
				} else if (max  >= 0 && !enrollmentRow.isWaitingListEnabled() && numOfParticipants >= max) {
					return trans.translate("grouplist.table.state.enroll.full"); 
				}	else if (max  >= 0 && enrollmentRow.isWaitingListEnabled() && numOfParticipants >= max) {
					return trans.translate("grouplist.table.state.WaitingList");
				}
				return trans.translate("grouplist.table.state.notEnrolled");
			case 6:
				// Action enroll
				if (getEnrolCount() >= maxEnrolCount || isEnrolledIn(enrollmentRow)) {
					// Already too much enrollments or already enrolled in the bg of the row => does not show action-link 'enroll'
					return Boolean.FALSE;
				}
				if (max >= 0 && !enrollmentRow.isWaitingListEnabled() && numOfParticipants >= max) {
					// group is full => => does not show action-link 'enroll'
					return Boolean.FALSE;
				}
				return Boolean.TRUE;
			case 7:
				// Action cancel enrollment
				if (isEnrolledIn(enrollmentRow)) {
          // check if user is on waiting-list
					if (enrollmentRow.isWaiting()) {
            // user is on waitinglist => show allways action cancelEnrollment for waitinglist 
 					  return Boolean.TRUE;
					}
          // user is not on waitinglist => show action cancelEnrollment only if enabled 
					if (cancelEnrollEnabled) {
						return Boolean.TRUE;
					}
				}
				return Boolean.FALSE;
			default: return "ERROR";
		}
	}

	@Override
	public Object createCopyWithEmptyList() {
		return new EnrollmentTableModelWithMaxSize(new ArrayList<EnrollmentRow>(), trans, identity, cancelEnrollEnabled, maxEnrolCount);
	}

	/**
	 * @param owned
	 */
	public void setEntries(List<EnrollmentRow> owned) {
		this.objects = owned;
	}

	/**
	 * @param row
	 * @return the business group at the given row
	 */
	public EnrollmentRow getRowAt(int row) {
		return objects.get(row);
	}
	
	/**
	 * Check if an identity is in certain security-group.
	 * @param businessGroup
	 * @param ident
	 * @return true: Found identity in PartipiciantGroup or WaitingGroup.
	 */
	private boolean isEnrolledIn(EnrollmentRow enrollmentRow) {
		return enrollmentRow.isWaiting() || enrollmentRow.isParticipant();
	}
	
	/**
	 * Check if an identity is in any security-group.
	 * @param ident
	 * @return amount of business groups the identity is enrolled in
	 */		
	private int getEnrolCount() {
		int enrolCount=0;
		// loop over all business-groups
		for (EnrollmentRow enrollmentRow:objects) {
			if (isEnrolledIn(enrollmentRow) ) {
				enrolCount++;
				// optimize, enough is enough
				if (maxEnrolCount == enrolCount) {
					return enrolCount;
				}
			}
		}
		return enrolCount;
	}

	@Override
	public String getRowCssClass(int rowId) {
		EnrollmentRow enrollmentRow = objects.get(rowId);
		return isEnrolledIn(enrollmentRow) ? "o_row_selected" : "";
	}
	
	public Stats getStats() {
		Stats stats = new Stats();
		for(int i=getRowCount(); i-->0; ) {
			EnrollmentRow row = getObject(i);
			if(row.isWaitingListEnabled() && row.isWaiting()) {
				stats.getWaitingGroupNames().add(row.getName());
			}
			if(row.isParticipant()) {
				stats.getParticipantingGroupNames().add(row.getName());
			}
			if(row.isWaitingListEnabled()) {
				stats.setSomeGroupWaitingListEnabled(true);
			}
		}
		return stats;
	}
	
	public static class Stats {
		
		private boolean someGroupWaitingListEnabled = false;
		private final List<String> participantingGroupNames = new ArrayList<>(5);
		private final List<String> waitingGroupNames = new ArrayList<>(5);

		public boolean isSomeGroupWaitingListEnabled() {
			return someGroupWaitingListEnabled;
		}

		public void setSomeGroupWaitingListEnabled(boolean someGroupWaitingListEnabled) {
			this.someGroupWaitingListEnabled = someGroupWaitingListEnabled;
		}

		public List<String> getParticipantingGroupNames() {
			return participantingGroupNames;
		}

		public List<String> getWaitingGroupNames() {
			return waitingGroupNames;
		}
	}
}