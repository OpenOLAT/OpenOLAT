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

package org.olat.ims.qti;


import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.ims.qti.process.Persister;

/**
 * Initial Date:  12.01.2005
 *
 * @author Mike Stock
 */
public class QTIResultTableModel implements TableDataModel<QTIResultSet> {

	private static final int COLUMN_COUNT = 3;
	private List<QTIResultSet> resultSets;
	private Persister persister;
	private boolean inTest;
	private final Translator translator;
	
	/**
	 * @param resultSets
	 */
	public QTIResultTableModel(List<QTIResultSet> resultSets, Translator translator) {
		this(resultSets, null, translator);
	}
	
	/**
	 * @param resultSets
	 */
	public QTIResultTableModel(List<QTIResultSet> resultSets, Persister persister, Translator translator) {
		this.resultSets = resultSets;
		this.persister = persister;
		this.inTest = (persister == null ? false : persister.exists());
		this.translator = translator;
	}
	
	public boolean updateStatus() {
		boolean newStatus = (persister == null ? false : persister.exists());
		if(newStatus == inTest) {
			return false;
		}
		inTest = newStatus;
		return true;
	}
	
	public boolean isTestRunning() {
		return (persister == null ? false : persister.exists());
	}
	
	
	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getRowCount()
	 */
	public int getRowCount() {
		return resultSets.size() + (inTest ? 1 : 0);
	}
	
	@Override
	public QTIResultSet getObject(int row) {
		return resultSets.get(row);
	}

	@Override
	public void setObjects(List<QTIResultSet> objects) {
		this.resultSets = objects;
	}

	@Override
	public QTIResultTableModel createCopyWithEmptyList() {
		return new QTIResultTableModel(new ArrayList<QTIResultSet>(), persister, translator);
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		if(inTest && (row + 1 == getRowCount())) {
			switch (col) {
				case 0: return persister.getLastModified();
				case 1: return "<span class='o_ochre'>" + translator.translate("notReleased") + "</span>";
				case 2: return "<span class='o_ochre'>" + translator.translate("open") + "</span>";
				case 3: return Boolean.FALSE;
				default: return "error";
			}	
		}
		QTIResultSet resultSet = getObject(row);
		switch (col) {
			case 0: return resultSet.getLastModified();
			case 1: {
				if (resultSet.getDuration() == null) {
					// fix for old testsets generated previously to duration introduction
					return "n/a";
				} else {
					return Formatter.formatDuration(resultSet.getDuration().longValue());
				}
			}
			case 2: return "" + AssessmentHelper.getRoundedScore(resultSet.getScore());
			case 3: return Boolean.TRUE;
			default: return "error";
		}	
	}

	/**
	 * @param rowId
	 * @return result set
	 */
	public QTIResultSet getResultSet(int rowId) {
		return resultSets.get(rowId);
	}
	
	public static class Wrapper {
		
		private QTIResultSet resultSet;
		
		public Wrapper(QTIResultSet resultSet) {
			this.resultSet = resultSet;
		}

		public QTIResultSet getResultSet() {
			return resultSet;
		}
	}
}
