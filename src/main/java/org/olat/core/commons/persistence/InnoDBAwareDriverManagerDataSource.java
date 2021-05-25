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
package org.olat.core.commons.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.olat.core.configuration.Initializable;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Description:<br>
 * Helper class that checks on startup if transactions are supported on mysql. Which means you run
 * your tables with the innoDB engine.
 * 
 * <P>
 * Initial Date:  28.05.2010 <br>
 * @author guido
 */
public class InnoDBAwareDriverManagerDataSource extends DriverManagerDataSource implements Initializable {
	private static final Logger log = Tracing.createLoggerFor(InnoDBAwareDriverManagerDataSource.class);
	private String dbVendor;
	
	/**
	 * [spring only]
	 */
	private InnoDBAwareDriverManagerDataSource () {
		//
	}

	@Override
	public void init() {
		//test makes only sense if mysql with innoDb tables are used
		if (!dbVendor.contains("mysql")) {
			return;
		}

		try(Connection connection = getConnection();
				Statement statement = connection.createStatement()) {
			log.info(Tracing.M_AUDIT, "Checking whether mysql tables support transactions based on innoDB tab...");
			statement.execute("show create table o_plock;");

			ResultSet result = statement.getResultSet();
			if(result.next()) {
				String createTableCommand = result.getString("Create Table");
				if (createTableCommand.contains("InnoDB")) {
					log.info(Tracing.M_AUDIT, "Your mysql tables look like they support transactions, fine!");
				} else {
					throw new StartupException("Your tables do not support transactions based on innoDB tables. Check your database server and enable innoDB engine! Your table currently runs: " + createTableCommand);
				}
			} else {
				throw new StartupException("Cannot retrieve table informations.");
			}
			result.close();
		} catch (SQLException e) {
			log.error("", e);
			if (e.getMessage().contains("doesn't exist")) {
				log.info(Tracing.M_AUDIT, "o_plock table does not yet exist, will check transaction support on next startup");
			} else {
				throw new StartupException("Could not execute db statement.", e);
			}
		}
	}
	
	/**
	 * [spring]
	 * @param dbVendor
	 */
	public void setDbVendor(String dbVendor) {
		this.dbVendor = dbVendor;
	
	}

}