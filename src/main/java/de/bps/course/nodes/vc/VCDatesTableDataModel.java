//<OLATCE-103>
/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2011 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * 
 * All rights reserved.
 */
package de.bps.course.nodes.vc;

import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;


/**
 * 
 * Description:<br>
 * Table model for run view of vc course node. Summarizes all planned dates for meetings.
 * 
 * <P>
 * Initial Date:  19.01.2011 <br>
 * @author skoeber
 */
public class VCDatesTableDataModel extends DefaultTableDataModel {
	
	//title, description, begin, end
	private static final int COLUMN_COUNT = 4;

	public VCDatesTableDataModel(List objects) {
		super(objects);
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public Object getValueAt(int row, int col) {
		final MeetingDate model = ((MeetingDate) objects.get(row));

		switch (col) {
			case 0:
				return model.getTitle();
			case 1:
				return model.getDescription();
			case 2:
				return model.getBegin();
			case 3:
				return model.getEnd();
			default:
				return "error";
		}
	}

	public MeetingDate getEntryAt(int row) {
		return (MeetingDate) objects.get(row);
	}

	public void setEntries(List newEntries) {
		this.objects = newEntries;
	}
}
//</OLATCE-103>