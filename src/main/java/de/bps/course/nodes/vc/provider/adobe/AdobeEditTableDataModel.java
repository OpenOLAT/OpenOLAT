// <OLATCE-103>
/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2010 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * 
 * All rights reserved.
 */
package de.bps.course.nodes.vc.provider.adobe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;

import de.bps.course.nodes.vc.MeetingDate;

/**
 * Description:<br>
 * Data model for editing dates lists - Virtual Classroom dates.
 * 
 * <P>
 * Initial Date: 14.07.2010 <br>
 * 
 * @author Jens Lindner (jlindne4@hs-mittweida.de)
 * @author skoeber
 */
public class AdobeEditTableDataModel extends DefaultTableDataModel {

	private static int COLUMN_COUNT = 4;

	public AdobeEditTableDataModel(final List<MeetingDate> objects) {
		super(objects);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/**
	 * {@inheritDoc}
	 */
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
				SimpleDateFormat sd = new SimpleDateFormat("dd.MM.yyyy HH:mm");
				return sd.format(model.getEnd());
			default:
				return "error";
		}
	}
}
// </OLATCE-103>