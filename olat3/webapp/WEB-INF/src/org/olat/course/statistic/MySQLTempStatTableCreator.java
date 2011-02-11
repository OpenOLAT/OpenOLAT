package org.olat.course.statistic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.olat.core.logging.OLog;
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
	private static final OLog log_ = Tracing.createLoggerFor(MySQLTempStatTableCreator.class);

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
							"businesspath varchar(2048) not null," +
							"userproperty2 varchar(255)," +							// homeOrg
							"userproperty4 varchar(255)," +							// orgType
							"userproperty10 varchar(255)," +						// studyBranch3
							"userproperty3 varchar(255)" +							// studyLevel
					");");
			
			log_.info("updateStatistic: inserting logging actions from "+from+" until "+until);
			
			// same month optimization
			String oLoggingTable = "o_loggingtable";
			Calendar lastUpdatedCalendar = Calendar.getInstance();
			lastUpdatedCalendar.setTime(from);
			Calendar nowUpdatedCalendar = Calendar.getInstance();
			nowUpdatedCalendar.setTime(until);
			
			if (lastUpdatedCalendar.get(Calendar.MONTH)==nowUpdatedCalendar.get(Calendar.MONTH)) {
				// that means we are in the same month, so use the current month's o_loggingtable
				// e.g. o_loggingtable_201002
				String monthStr = String.valueOf(lastUpdatedCalendar.get(Calendar.MONTH)+1);
				if (monthStr.length()==1) {
					monthStr = "0"+monthStr;
				}
				String sameMonthTable = "o_loggingtable_"+String.valueOf(lastUpdatedCalendar.get(Calendar.YEAR))+monthStr;
				List tables = jdbcTemplate_.queryForList("show tables like '"+sameMonthTable+"'");
				if (tables!=null && tables.size()==1) {
					log_.info("updateStatistic: using "+sameMonthTable+" instead of "+oLoggingTable);
					oLoggingTable = sameMonthTable;
				} else {
					log_.info("updateStatistic: using "+oLoggingTable+" ("+sameMonthTable+" didn't exist)");
				}
			} else {
				log_.info("updateStatistic: using "+oLoggingTable+" since from and to months are not the same");
			}
			
			jdbcTemplate_.execute(
					"insert into o_stat_temptable (creationdate,businesspath,userproperty2,userproperty4,userproperty10,userproperty3) " +
						"select " +
							"creationdate,businesspath,userproperty2,userproperty4,userproperty10,userproperty3 " +
						"from " + 
						oLoggingTable + 
						" where " +
							"actionverb='launch' and actionobject='node' and creationdate>from_unixtime('"+(from.getTime()/1000)+"') and creationdate<=from_unixtime('"+(until.getTime()/1000)+"');");

			long numLoggingActions = jdbcTemplate_.queryForLong("select count(*) from o_stat_temptable;");
			log_.info("updateStatistic: insert done. number of logging actions: "+numLoggingActions);
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
