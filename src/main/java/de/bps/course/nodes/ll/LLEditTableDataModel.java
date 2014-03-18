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
public class LLEditTableDataModel extends DefaultTableDataModel<LLModel> {

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
		final LLModel model = objects.get(row);
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
