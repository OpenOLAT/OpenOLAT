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
import java.sql.Statement;

import org.olat.core.configuration.Destroyable;
import org.olat.core.logging.StartupException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Description:<br>
 * hsqld needs special sql command for shutting down...
 * 
 * <P>
 * Initial Date:  29.01.2010 <br>
 * @author guido
 */
public class HsqldbDriverManagerDataSourceWithShutdownHook extends DriverManagerDataSource implements Destroyable {
	
	/**
	 * @see org.olat.core.configuration.OLATModule#destroy()
	 */
	@Override
	public void destroy() {
			//no logger available any more at this time during shuthown
			System.out.println("shutting down hsqldb");
			Statement sql = null;
			try {
				Connection con = getConnection();
				sql = con.createStatement();
				sql.execute("SHUTDOWN");
				sql.close();
			} catch (Exception e) {
				System.out.println("Error while shutting down hsqldb:"+e.getMessage());
			}
	}
	
	/**
	 * [spring]
	 * @param clusterMode
	 */
	public void setOlatClustering(String clusterMode) {
		if (clusterMode.equals("Cluster")) {
			String msg = "****************************************\n  You cannot run OLAT in Cluster mode with the embedded hsqldb as it does not supports some features" +
					" which are needed for clustering. For Clustering you need a database like mysql with innodb tables or similar    \n************************************************";
			throw new StartupException(msg);
		}
	}


}
