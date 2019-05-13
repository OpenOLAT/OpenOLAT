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

import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Only drop the temporary table.
 * 
 * 
 * Initial date: 8 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PostgreSQLTempStatTableDropper implements IStatisticUpdater {

	/** the logging object used in this class **/
	private static final Logger log = Tracing.createLoggerFor(PostgreSQLTempStatTableDropper.class);

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
		// note: fullRecalculation has no affect to the dropper
		
		// create temp table
		final long startTime = System.currentTimeMillis();
		try{
			jdbcTemplate.execute("drop table o_stat_temptable;");
		} catch(RuntimeException e) {
			log.error("", e);
		} catch(Error er) {
			log.error("", er);
		} finally {
			final long diff = System.currentTimeMillis() - startTime;
			log.info("updateStatistic: END. duration=" + diff);
		}
	}
}
