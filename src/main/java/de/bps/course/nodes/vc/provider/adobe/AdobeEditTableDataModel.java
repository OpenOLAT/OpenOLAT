// <OLATCE-103>
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
package de.bps.course.nodes.vc.provider.adobe;

import java.text.SimpleDateFormat;
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
public class AdobeEditTableDataModel extends DefaultTableDataModel<MeetingDate> {

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
		final MeetingDate model = objects.get(row);
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