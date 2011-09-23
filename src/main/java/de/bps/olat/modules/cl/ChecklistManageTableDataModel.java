/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.olat.modules.cl;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;

public class ChecklistManageTableDataModel extends DefaultTableDataModel {
	
	private static int COLUMN_COUNT;
	private static int ROW_COUNT;
	
	private Checklist checklist;
	private List<Identity> participants;
	private List entries;

	@SuppressWarnings("unchecked")
	public ChecklistManageTableDataModel(Checklist checklist, List<Identity> participants) {
		super(participants);
		this.checklist = checklist;
		this.participants = participants;
		
		COLUMN_COUNT = checklist.getCheckpoints().size() + 2;
		ROW_COUNT = participants.size();
		
		this.entries = new ArrayList(ROW_COUNT);
		for( Identity identity : participants ) {
			List row = new ArrayList(COLUMN_COUNT);
			// name
			row.add(
					identity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale()) + " " +
					identity.getUser().getProperty(UserConstants.LASTNAME, getLocale()));
			// checkpoints value
			for( Checkpoint checkpoint : this.checklist.getCheckpointsSorted(ChecklistUIFactory.comparatorTitleAsc) ) {
				row.add(checkpoint.getSelectionFor(identity));
			}
			// action
			row.add(true);
			// add to columns
			entries.add(row);
		}
	}

	public int getColumnCount() {
		// name, 1-n checkpoints, action
		return COLUMN_COUNT;
	}
	
	public int getRowCount() {
		return ROW_COUNT;
	}
	
	public Object getValueAt(int row, int col) {
		List entry = (List)entries.get(row);
		return entry.get(col);
	}
	
	public Identity getParticipantAt(int row) {
		return participants.get(row);
	}
}
