/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
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
public class ChecklistRunTableDataModel extends DefaultTableDataModel<Checkpoint> {

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
		Checkpoint checkpoint = getObject(row);

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
