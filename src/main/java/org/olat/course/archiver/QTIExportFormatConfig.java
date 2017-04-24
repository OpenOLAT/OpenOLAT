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
package org.olat.course.archiver;

import org.olat.ims.qti.export.QTIExportItemFormatConfig;

/**
 * data container to persist result export config
 * 
 * Initial Date: 18.04.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class QTIExportFormatConfig implements QTIExportItemFormatConfig {
	
	private boolean responseCols;
	private boolean positionsOfResponsesCol;
	private boolean pointCol;
	private boolean timeCols;

	public QTIExportFormatConfig(boolean resCols, boolean posOfResCol, boolean pointCol, boolean timeCols){
		this.responseCols = resCols;
		this.positionsOfResponsesCol = posOfResCol;
		this.pointCol = pointCol;
		this.timeCols = timeCols;
	}

	public boolean hasResponseCols() {
		return responseCols;
	}

	public boolean hasPositionsOfResponsesCol() {
		return positionsOfResponsesCol;
	}

	public boolean hasPointCol() {
		return pointCol;
	}

	public boolean hasTimeCols() {
		return timeCols;
	}
	
	public void setPointCol(boolean pointColConfigured) {
		this.pointCol = pointColConfigured;
	}

	public void setPositionsOfResponsesCol(boolean positionsOfResponsesColConfigured) {
		this.positionsOfResponsesCol = positionsOfResponsesColConfigured;
	}

	public void setResponseCols(boolean responseColsConfigured) {
		this.responseCols = responseColsConfigured;
	}

	public void setTimeCols(boolean timeColsConfigured) {
		this.timeCols = timeColsConfigured;
	}

}
