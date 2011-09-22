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
package de.bps.course.nodes.ll;

import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;

/**
 * Description:<br>
 * Data model for editing link lists.
 *
 * <P>
 * Initial Date: 17.11.2008 <br>
 *
 * @author Marcel Karras (toka@freebits.de)
 */
public class LLEditTableDataModel extends DefaultTableDataModel {

	private static int COLUMN_COUNT = 3;

	public LLEditTableDataModel(final List<LLModel> objects) {
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
		final LLModel model = ((LLModel) objects.get(row));
		switch (col) {
			case 0:
				return model.getTarget();
			case 1:
				return model.getDescription();
			case 2:
				return model.getComment();
			default:
				return "ERR";
		}
	}
}
