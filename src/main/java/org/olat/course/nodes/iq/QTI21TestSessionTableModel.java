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
package org.olat.course.nodes.iq;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.ims.qti21.AssessmentTestSession;

/**
 * 
 * Initial date: 21.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21TestSessionTableModel extends DefaultFlexiTableDataModel<AssessmentTestSession> {
	
	public QTI21TestSessionTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public DefaultFlexiTableDataModel<AssessmentTestSession> createCopyWithEmptyList() {
		return new QTI21TestSessionTableModel(getTableColumnModel());
	}

	@Override
	public Object getValueAt(int row, int col) {
		AssessmentTestSession session = getObject(row);
		switch(TSCols.values()[col]) {
			case lastModified: return session.getLastModified();
			case results: {
				Date terminated = session.getTerminationTime();
				return terminated == null ? Boolean.FALSE : Boolean.TRUE;
			}
			default: return "ERROR";
		}
	}
	
	public enum TSCols {
		lastModified("table.header.lastModified"),
		results("table.header.results");
		
		private final String i18nKey;
		
		private TSCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
