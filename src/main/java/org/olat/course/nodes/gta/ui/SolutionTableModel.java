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
package org.olat.course.nodes.gta.ui;

import org.apache.commons.lang3.tuple.Pair;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.model.Solution;

/**
 * 
 * Initial date: 25.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SolutionTableModel extends DefaultFlexiTableDataModel<SolutionRow> {
	
	public SolutionTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		SolutionRow solutionRow = getObject(row);
		Solution solution = solutionRow.getSolution();
		switch(SolCols.values()[col]) {
			case title: return solutionRow.getSolution().getTitle();
			case file: return solutionRow.getDownloadLink() == null
					? solution.getFilename() : solutionRow.getDownloadLink();
			case author: return solutionRow.getAuthor();
			case mode: return solution.isInTranscoding() ?
					GTAManager.BUSY_VALUE : Pair.of(solutionRow.getMode(), solution.getFilename());
			default: return "ERROR";
		}
	}

	public enum SolCols {
		title("task.title"),
		file("task.file"),
		author("table.header.author"),
		mode("table.header.view");
		
		private final String i18nKey;
	
		private SolCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
