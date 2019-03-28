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

package org.olat.upgrade;

import javax.sql.DataSource;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * Description:<br>
 * Interface for an OLAT upgrade. The upgrade will be executed during the 
 * OLAT startup process using the two do... methods
 * 
 * <P>
 * Initial Date:  15.08.2005 <br>
 * @author gnaegi
 */
public abstract class OLATUpgrade {
	
	static final String TASK_DP_UPGRADE = "Database update";
	private String alterDbFilename;
	OLog log = Tracing.createLoggerFor(this.getClass());

	/**
	 * @return String representing the unique version identifyer of this upgrade
	 */
	public abstract String getVersion();
	
	/**
	 * Method is executed after every module and every extension is initialized but 
	 * still in the init method of the servlet. Here you have full access to the database
	 * layer and you can savely access every module.
	 * @param upgradeManager
	 * @return true if anything has been upgraded, false if nothing has been 
	 * upgraded (e.g. since the upgrade is already installed). false does not indicate
	 * a failure! In case of failure, throw an exception!
	 */
	public abstract boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager);
	
	/**
	 * [used by spring] to inject alter db sql statements if available.
	 * If a file is present it get's automatically executed
	 * @param filename
	 */
	public void setAlterDbStatements(String filename) {
		this.alterDbFilename = filename;
	}
	
	/**
	 * @return the filename or null and update task information
	 */
	public String getAlterDbStatements() {
		return this.alterDbFilename;
	}
	
	/**
	 * Executes an SQL query in plain SQL by loading the appropriate driver.
	 * This should be done before the OLAT database module has been started.
	 * @param query
	 * @param dataSource
	 */
	public void executePlainSQLDBStatement(String query, DataSource dataSource) {
		JdbcTemplate template = new JdbcTemplate(dataSource);
		template.update(query);
	}

	
}
