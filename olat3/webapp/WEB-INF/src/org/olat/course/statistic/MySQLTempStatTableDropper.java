package org.olat.course.statistic;

import java.util.Date;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * MySQL specific class which drops the temporary table with the
 * result of 'o_loggingtable where actionverb=launch and actionobject=node'.
 * <P>
 * Initial Date:  16.02.2010 <br>
 * @author Stefan
 */
public class MySQLTempStatTableDropper implements IStatisticUpdater {

	/** the logging object used in this class **/
	private static final OLog log_ = Tracing.createLoggerFor(MySQLTempStatTableDropper.class);

	/** the jdbcTemplate is used to allow access to other than the default database and 
	 * allow raw sql code
	 */
	private JdbcTemplate jdbcTemplate_;

	/** set via spring **/
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		jdbcTemplate_ = jdbcTemplate;
	}
	
	@Override
	public void updateStatistic(boolean fullRecalculation, Date from, Date until, StatisticUpdateManager statisticUpdateManager) {
		// note: fullRecalculation has no affect to the dropper
		
		// create temp table
		final long startTime = System.currentTimeMillis();
		try{
			jdbcTemplate_.execute("drop table o_stat_temptable;");

		} catch(RuntimeException e) {
			e.printStackTrace(System.out);
		} catch(Error er) {
			er.printStackTrace(System.out);
		} finally {
			final long diff = System.currentTimeMillis() - startTime;
			log_.info("updateStatistic: END. duration="+diff);
		}
	}

}
