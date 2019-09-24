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

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Default implementation for IStatisticUpdater.
 * <p>
 * This implementation takes the following properties (via spring):
 * <ul>
 *  <li>jdbcTemplate: the JdbcTemplate to be used to access the
 *  o_loggingtable and to create the o_stat_* tables - note that this 
 *  might be different from the default database configured in OLAT
 *  when using a master-slave setup for example</li>
 *  <li>updateSQL: a list of (raw) sql statements which update
 *  the o_stat_* table (only the one to which this updater
 *  belongs!). The idea of these sql statements is to
 *  support incremental updates, i.e. to only update the difference,
 *  not having to delete the whole o_stat_* table away on each update
 *  (in order to improve speed)
 *  </li>
 *  <li>deleteSQL: a list of (raw) sql statements which delete
 *  the o_stat_* table (only the one to which this updater belongs!)
 *  </li>
 * </ul>
 * <P>
 * Initial Date:  12.02.2010 <br>
 * @author Stefan
 */
public class StatisticUpdater implements IStatisticUpdater {

	/** the logging object used in this class **/
	private static final Logger log_ = Tracing.createLoggerFor(StatisticUpdater.class);

	/** the jdbcTemplate is used to allow access to other than the default database and 
	 * allow raw sql code
	 */
	private JdbcTemplate jdbcTemplate_;

	/** holds the update SQL statements - set via spring **/
	private String[] updateSQL_;
	
	/** holds the delete SQL statements - set via spring **/
	private String[] deleteSQL_;

	/** name used to identify this StatisticUpdater for logging purpose **/
	private String loggingName_;

	/** set via spring **/
	public void setLoggingName(String loggingName) {
		loggingName_ = loggingName;
	}
	
	@Override
	public String toString() {
		return super.toString()+"["+loggingName_+"]";
	}
	
	/** set via spring **/
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		jdbcTemplate_ = jdbcTemplate;
	}
	
	/** set via spring **/
	public void setUpdateSQL(String[] updateSQL) {
		updateSQL_ = updateSQL;
	}
	
	/** set via spring **/
	public void setDeleteSQL(String[] deleteSQL) {
		deleteSQL_ = deleteSQL;
	}
	
	/**
	 * get the query which updates the whole table
	 * @return
	 */
	protected String[] getUpdateQueries() {
		return updateSQL_;
	}
	
	/**
	 * get the query which deletes the whole table
	 * @return
	 */
	protected String[] getDeleteQueries() {
		return deleteSQL_;
	}

	@Override
	@SuppressWarnings("squid:S2077")// SQL is defined in the configuration and cannot be changed
	public final void updateStatistic(boolean fullRecalculation, Date from, Date until, StatisticUpdateManager statisticUpdateManager) {
		log_.info("updateStatistic<"+loggingName_+">: START");
		final long startTime = System.currentTimeMillis();
		try{
			if (fullRecalculation) {
				String[] deleteQueries = getDeleteQueries();
				if (deleteQueries!=null) {
					for (int i = 0; i < deleteQueries.length; i++) {
						String aDeleteQuery = deleteQueries[i];
						if (aDeleteQuery!=null && aDeleteQuery.length()>0) {
							jdbcTemplate_.execute(aDeleteQuery);
						}
					}
				}
			}
			
			String[] updateQueries = getUpdateQueries();
			if (updateQueries!=null) {
				for (int i = 0; i < updateQueries.length; i++) {
					String anUpdateQuery = updateQueries[i];
					if (anUpdateQuery!=null && anUpdateQuery.length()>0) {
						jdbcTemplate_.execute(anUpdateQuery);
					}
				}
			}

		} catch(RuntimeException e) {
			log_.error("updateStatistic<"+loggingName_+">: RuntimeException while updating the statistics: "+e, e);
		} finally {
			final long diff = System.currentTimeMillis() - startTime;
			log_.info("updateStatistic<"+loggingName_+">: END. duration="+diff+" milliseconds");
		}
	}
}
