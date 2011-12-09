/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.core.gui.components.table;

import org.olat.core.gui.media.ExcelMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.filter.FilterFactory;

/**
 * Description:<br>
 * The class exports the table as a file with comma separated values fields.
 * 
 * <P>
 * Initial Date:  Nov 18, 2010 <br>
 * @author patrick
 */
class DefaultCsvTableExporter implements TableExporter {

	/**
	 * @see org.olat.core.gui.components.table.TableExporter#export(org.olat.core.gui.components.table.Table)
	 */
	@Override
	public MediaResource export(Table table) {
		Translator translator = table.getTranslator();
		int cdcnt = table.getColumnCount();
		int rcnt = table.getRowCount();
		StringBuilder sb = new StringBuilder();
		
		createHeader(table, translator, cdcnt, sb);
		sb.append('\n');
		
		createData(table, cdcnt, rcnt, sb);
		String res = sb.toString();

		// FIXME:FG:6.2:OLAT-3736  a use a charsetprovider interface which can deliver the selected charset of a user
		//String charset = UserManager.getInstance().getUserCharset(ureq.getIdentity());
		// As a temporary workaround use the default charset
		String charset = WebappHelper.getDefaultCharset();
		return new ExcelMediaResource(res, charset);
	}
	
	private void createHeader(final Table table, final Translator translator, final int cdcnt, final StringBuilder sb) {
		for (int c = 0; c < cdcnt; c++) {
			ColumnDescriptor cd = table.getColumnDescriptor(c);
			if (cd instanceof StaticColumnDescriptor) {
				// ignore static column descriptors - of no value in excel download!
				continue;
			}
			String headerKey = cd.getHeaderKey();
			String headerVal = cd.translateHeaderKey() ? translator.translate(headerKey) : headerKey;
			sb.append('\t').append(headerVal);
		}
	}

	private void createData(final Table table, final int cdcnt, final int rcnt, final StringBuilder sb) {
		for (int r = 0; r < rcnt; r++) {
			for (int c = 0; c < cdcnt; c++) {
				ColumnDescriptor cd = table.getColumnDescriptor(c);
				if (cd instanceof StaticColumnDescriptor) {
					// ignore static column descriptors - of no value in excel download!
					continue;
				}
				StringOutput so = new StringOutput();
				cd.renderValue(so, r, null);
				String cellValue = so.toString();
				cellValue = StringHelper.stripLineBreaks(cellValue);
				cellValue = FilterFactory.getHtmlTagsFilter().filter(cellValue);
				sb.append('\t').append(cellValue);
			}
			sb.append('\n');
		}
	}

}
