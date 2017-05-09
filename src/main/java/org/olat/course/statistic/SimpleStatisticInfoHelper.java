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
package org.olat.course.statistic;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * Simple helper class which knows about since when statistics are available.
 * <p>
 * NOTE: THIS CLASS NEEDS TO BE REFACTORED IN 6.4 INTO STATISTICUPDATEMANAGER.
 *       IN 6.3 THERE ARE MULTIPLE SPRING BEAN SCOPES - THOSE FROM
 *       olat_extensions.xml ARE NOT ACCESSIBLE FROM BEANS WITHIN
 *       olatdefaultconfig.xml.
 * <P>
 * Initial Date:  22.02.2010 <br>
 * @author Stefan
 */
public class SimpleStatisticInfoHelper {

	/** the logging object used in this class **/
	private static final OLog log = Tracing.createLoggerFor(SimpleStatisticInfoHelper.class);

	/** a map with all sql statements for the supported dbvendors **/
	private final Map<String,String> sql;

	/** the calculated creationdate **/
	private Long creationDate;

	private DB dbInstance;

	public SimpleStatisticInfoHelper(DB dbInstance, Map<String, String> sql) {
		this.dbInstance = dbInstance;
		this.sql = sql;
	}
	
	/**
	 * Computes the creationdate_ if it's not already computed and returns it
	 * @return the creationdate
	 */
	public Date getFirstLoggingTableCreationDate() {
		if (creationDate == null) {
			try {
				synchronized(this) {
					if(creationDate == null) {
						List<?> creationDates = dbInstance.getCurrentEntityManager()
								.createNativeQuery(sql.get(dbInstance.getDbVendor()))
								.getResultList();
						creationDate = creationDates == null || creationDates.isEmpty() ? null : ((Number)creationDates.get(0)).longValue();
					}
				}
			} catch (Exception e) {
				log.error("", e);
			} finally {
				dbInstance.commitAndCloseSession();
			}
		}
		return creationDate == null ? null : new Date(creationDate);
	}
}
