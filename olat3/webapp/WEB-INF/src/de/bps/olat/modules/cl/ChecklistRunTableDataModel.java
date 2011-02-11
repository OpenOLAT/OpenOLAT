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

import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * Model for checklist run table.
 * 
 * <P>
 * Initial Date:  11.08.2009 <br>
 * @author bja <bja@bps-system.de>
 */
public class ChecklistRunTableDataModel extends DefaultTableDataModel {

	private static final int COLUMN_COUNT = 3;
	private Translator translator;

	public ChecklistRunTableDataModel(List<Checkpoint> checkpoints, Translator translator) {
		super(checkpoints);
		this.translator = translator;
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Checkpoint checkpoint = (Checkpoint) objects.get(row);

		switch (col) {
			case 0:
				// title
				return checkpoint.getTitle();
			case 1:
				// description
				return checkpoint.getDescription();
			case 2:
				// mode
				return CheckpointMode.getLocalizedMode(checkpoint.getMode(), translator);
			default:
				return "error";
		}
	}

}
