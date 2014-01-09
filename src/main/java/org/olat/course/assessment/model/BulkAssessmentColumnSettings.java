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
package org.olat.course.assessment.model;

import java.io.Serializable;

/**
 * 
 * Initial date: 09.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulkAssessmentColumnSettings implements Serializable {

	private static final long serialVersionUID = -8726013806107035722L;
	
	private int usernameColumn;
	private int scoreColumn;
	private int passedColumn;
	private int commentColumn;
	
	public int getUsernameColumn() {
		return usernameColumn;
	}
	
	public void setUsernameColumn(int usernameColumn) {
		this.usernameColumn = usernameColumn;
	}
	
	public int getScoreColumn() {
		return scoreColumn;
	}
	
	public void setScoreColumn(int scoreColumn) {
		this.scoreColumn = scoreColumn;
	}
	
	public int getPassedColumn() {
		return passedColumn;
	}
	
	public void setPassedColumn(int passedColumn) {
		this.passedColumn = passedColumn;
	}
	
	public int getCommentColumn() {
		return commentColumn;
	}
	
	public void setCommentColumn(int commentColumn) {
		this.commentColumn = commentColumn;
	}
}
