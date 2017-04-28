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

/**
 * data container to persist result export config
 * 
 * Initial Date: 18.04.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class ExportFormat {
	
	private boolean responseCols;
	private boolean positionsOfResponsesCol;
	private boolean pointCol;
	private boolean timeCols;
	private boolean commentCol;

	public ExportFormat(boolean responseCols, boolean posOfResCol, boolean pointCol, boolean timeCols, boolean commentCol){
		this.responseCols = responseCols;
		this.positionsOfResponsesCol = posOfResCol;
		this.pointCol = pointCol;
		this.timeCols = timeCols;
		this.commentCol = commentCol;
	}

	public boolean isResponseCols() {
		return responseCols;
	}
	
	public void setResponseCols(boolean responseColsConfigured) {
		this.responseCols = responseColsConfigured;
	}

	public boolean isPositionsOfResponsesCol() {
		return positionsOfResponsesCol;
	}
	
	public void setPositionsOfResponsesCol(boolean positionsOfResponsesColConfigured) {
		this.positionsOfResponsesCol = positionsOfResponsesColConfigured;
	}

	public boolean isPointCol() {
		return pointCol;
	}
	
	public void setPointCol(boolean pointColConfigured) {
		this.pointCol = pointColConfigured;
	}

	public boolean isTimeCols() {
		return timeCols;
	}
	
	public void setTimeCols(boolean timeColsConfigured) {
		this.timeCols = timeColsConfigured;
	}

	public boolean isCommentCol() {
		return commentCol;
	}

	public void setCommentCol(boolean commentCol) {
		this.commentCol = commentCol;
	}
}
