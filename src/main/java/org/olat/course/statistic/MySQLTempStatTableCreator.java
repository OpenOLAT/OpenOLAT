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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * MySQL specific class which creates a temporary table with the
 * result of 'o_loggingtable where actionverb=launch and actionobject=node'.
 * <P>
 * Initial Date:  16.02.2010 <br>
 * @author Stefan
 */
public class MySQLTempStatTableCreator implements IStatisticUpdater {

	/** the logging object used in this class **/
	private static final Logger log_ = Tracing.createLoggerFor(MySQLTempStatTableCreator.class);

	/** the jdbcTemplate is used to allow access to other than the default database and 
	 * allow raw sql code
	 */
	private JdbcTemplate jdbcTemplate_;
	
	/** set via spring **/
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		jdbcTemplate_ = jdbcTemplate;
		DataSource dataSource = jdbcTemplate==null ? null : jdbcTemplate.getDataSource();
		Connection connection = null;
		try{
			if (dataSource!=null) {
				connection = dataSource.getConnection();
			}
		} catch(SQLException e) {
			log_.warn("setJdbcTemplate: SQLException while trying to get connection for logging", e);
		}
		log_.info("setJdbcTemplate: jdbcTemplate="+jdbcTemplate+", dataSource="+dataSource+", connection="+connection);
	}
	
	@Override
	public void updateStatistic(boolean fullRecalculation, Date from, Date until, StatisticUpdateManager statisticUpdateManager) {
		// create temp table
		final long startTime = System.currentTimeMillis();
		try{
			log_.info("updateStatistic: dropping o_stat_temptable if still existing");
			jdbcTemplate_.execute("drop table if exists o_stat_temptable;");
			
			log_.info("updateStatistic: creating o_stat_temptable");
			jdbcTemplate_.execute(
					"create table o_stat_temptable (" +
						"creationdate datetime not null," +
						"businesspath varchar(2048) not null" +
					");");
			
			log_.info("updateStatistic: inserting logging actions from "+from+" until "+until);
			
			// same month optimization
			Calendar lastUpdatedCalendar = Calendar.getInstance();
			lastUpdatedCalendar.setTime(from);
			Calendar nowUpdatedCalendar = Calendar.getInstance();
			nowUpdatedCalendar.setTime(until);
			
			long fromSeconds = from.getTime() / 1000l;
			long untilSeconds = until.getTime() / 1000l;

			long numLoggingActions = jdbcTemplate_.update(
					"insert into o_stat_temptable (creationdate,businesspath) " +
						"select creationdate,businesspath" +
						" from o_loggingtable" + 
						" where actionverb='launch' and actionobject='node' and creationdate>from_unixtime('"+ fromSeconds +"') and creationdate<=from_unixtime('"+ untilSeconds +"');");
			
			log_.info("updateStatistic: insert done. number of logging actions: " + numLoggingActions);
		} catch(RuntimeException e) {
			log_.warn("updateStatistic: ran into a RuntimeException: "+e, e);
		} catch(Error er) {
			log_.warn("updateStatistic: ran into an Error: "+er, er);
		} finally {
			final long diff = System.currentTimeMillis() - startTime;
			log_.info("updateStatistic: END. duration="+diff);
		}
	}

}
