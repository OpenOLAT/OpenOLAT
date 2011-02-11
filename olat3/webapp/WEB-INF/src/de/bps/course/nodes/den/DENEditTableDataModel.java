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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;

public class DENEditTableDataModel extends DefaultTableDataModel {
	
	public static final String CHANGE_ACTION = "denDateChange";
	public static final String DELETE_ACTION = "denDateDelete";
	
	// begin, end, location, comment, num participants
	private static final int COLUMN_COUNT = 5;
	
	private Translator translator;
	private DENManager denManager;
	
	public DENEditTableDataModel(List objects, Translator translator) {
		super(objects);
		
		this.translator = translator;
		denManager = DENManager.getInstance();
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public Object getValueAt(int row, int col) {
		KalendarEvent event = (KalendarEvent)objects.get(row);
		
		switch (col) {
			case 0: return denManager.format(event.getSubject());
			case 1:
				//begin
				Formatter formatter = Formatter.getInstance(translator.getLocale());
				String formattedDate = formatter.formatDateAndTime(event.getBegin());
				return denManager.format(formattedDate);
			case 2:
				//duration
				Date begin = event.getBegin();
				Date end = event.getEnd();
				long milliSeconds = denManager.getDuration(begin, end);
				
				return denManager.formatDuration(milliSeconds, translator);
			case 3:
				//location
				return denManager.format(event.getLocation());
			case 4: return event.getComment();
			case 5: return event.getNumParticipants();
		default:	return "error";
		}
	}

	public KalendarEvent getDENEventObject(int row) {
		return (KalendarEvent)objects.get(row);
	}
	
	public void setEntry(int row, KalendarEvent event) {
		objects.remove(row);
		objects.add(row, event);
	}
	
	public KalendarEvent getEntryAt(int row) {
		return (KalendarEvent)objects.get(row);
	}
	
	public void removeEntries(BitSet choosenEntries) {
		Collection<Object> delList = new ArrayList<Object>();
		for (int i = 0; i < objects.size(); i++) {
			if(choosenEntries.get(i))
				delList.add(objects.get(i));
		}
		if(delList.size() > 0)
			objects.removeAll(delList);
	}
}
