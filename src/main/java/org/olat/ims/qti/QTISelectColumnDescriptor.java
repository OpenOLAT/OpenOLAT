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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.ims.qti;

import java.util.Locale;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  11 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class QTISelectColumnDescriptor extends DefaultColumnDescriptor {
	
	private boolean readOnly;
	private final Translator translator;
	
	public QTISelectColumnDescriptor(String headerKey, int dataColumn, boolean readOnly, Locale locale, Translator translator) {
		super(headerKey, dataColumn, null, locale);
		this.readOnly = readOnly;
		this.translator = translator;
	}

	@Override
	public String getAction(int row) {
		int sortedRow = table.getSortedRow(row);
		Object state = getTable().getTableDataModel().getValueAt(sortedRow, getDataColumn());

		if(state instanceof Boolean && !((Boolean)state).booleanValue()) {
			return readOnly ? null : "ret";
		}
		return "sel";
	}

	@Override
	public void renderValue(StringOutput sb, int row, Renderer renderer) {
		int sortedRow = table.getSortedRow(row);
		Object state = getTable().getTableDataModel().getValueAt(sortedRow, getDataColumn());
		if((state instanceof Boolean && ((Boolean)state).booleanValue())) {
			sb.append(translator.translate("select"));
		} else if(!readOnly) {
			sb.append(translator.translate("retrievetest"));
		}
	}
}
