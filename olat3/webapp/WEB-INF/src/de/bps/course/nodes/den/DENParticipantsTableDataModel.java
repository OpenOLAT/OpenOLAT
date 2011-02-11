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
package de.bps.course.nodes.den;

import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;

/**
 * Table data model for the view of participants enrolled in an event
 * @author skoeber
 */
class DENParticipantsTableDataModel extends DefaultTableDataModel {
	
	public static final String MAIL_ACTION = "denSendMail";
	public static final String REMOVE_ACTION = "denRemoveParticipant";
	
	private static final int COLUMN_COUNT = 2;

	public DENParticipantsTableDataModel(List objects) {
		super(objects);
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Identity identity = (Identity)objects.get(row);
		
		switch (col) {
		case 0:
			return identity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale());
		case 1:
			return identity.getUser().getProperty(UserConstants.LASTNAME, getLocale());
		case 2:
			return identity.getUser().getProperty(UserConstants.EMAIL, getLocale());
		case 3:
			return Boolean.TRUE;
		case 4:
			return Boolean.TRUE;

		default:
			return "error";
		}
	}
	
	public void setEntries(List<Identity> newEntries) {
		this.objects = newEntries;
	}
	
	public Identity getEntryAt(int row) {
		return (Identity)objects.get(row);
	}
	
}
