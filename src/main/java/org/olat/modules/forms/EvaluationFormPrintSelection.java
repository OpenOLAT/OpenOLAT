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
package org.olat.modules.forms;

/**
 * 
 * Initial date: 01.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EvaluationFormPrintSelection {

	private boolean overview;
	private boolean tables;
	private boolean diagrams;
	private boolean sessions;
	
	public boolean isOverview() {
		return overview;
	}
	public void setOverview(boolean overview) {
		this.overview = overview;
	}
	public boolean isTables() {
		return tables;
	}
	public void setTables(boolean tables) {
		this.tables = tables;
	}
	public boolean isDiagrams() {
		return diagrams;
	}
	public void setDiagrams(boolean diagrams) {
		this.diagrams = diagrams;
	}
	public boolean isSessions() {
		return sessions;
	}
	public void setSessions(boolean sessions) {
		this.sessions = sessions;
	}
	
	
}
