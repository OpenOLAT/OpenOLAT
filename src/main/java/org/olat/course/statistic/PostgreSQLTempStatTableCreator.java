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
package org.olat.course.statistic;

import java.sql.Types;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Drop and create the temporary table, copy the needed data.
 * 
 * 
 * Initial date: 8 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PostgreSQLTempStatTableCreator implements IStatisticUpdater {

	/** the logging object used in this class **/
	private static final Logger log_ = Tracing.createLoggerFor(PostgreSQLTempStatTableCreator.class);

	/** the jdbcTemplate is used to allow access to other than the default database and 
	 * allow raw sql code
	 */
	private JdbcTemplate jdbcTemplate;
	
	/** set via spring **/
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	@Override
	public void updateStatistic(boolean fullRecalculation, Date from, Date until, StatisticUpdateManager statisticUpdateManager) {
		// create temp table
		final long startTime = System.currentTimeMillis();
		log_.info("updateStatistic: dropping o_stat_temptable if still existing");
		try {
			jdbcTemplate.execute("drop table if exists o_stat_temptable;");
		} catch (Exception e) {
			log_.error("Cannot drop the temporary table", e);
		}
		
		try{
			log_.info("updateStatistic: creating o_stat_temptable");
			jdbcTemplate.execute(
					"create table o_stat_temptable (" +
						"creationdate timestamp not null," +
						"businesspath varchar(2048) not null" +
					");");
			
			log_.info("updateStatistic: inserting logging actions from "+from+" until "+until);
			
			int numLoggingActions = jdbcTemplate.update(
					"insert into o_stat_temptable (creationdate,businesspath) " +
						"select creationdate,businesspath" +
						" from o_loggingtable" + 
						 " where actionverb='launch' and actionobject='node' and creationdate>? and creationdate<=?;",
						new Object[]{ from, until }, new int[]{ Types.TIMESTAMP, Types.TIMESTAMP});

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
