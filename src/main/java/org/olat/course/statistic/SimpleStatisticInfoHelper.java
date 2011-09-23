package org.olat.course.statistic;

import java.util.Date;
import java.util.Map;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.jdbc.core.JdbcTemplate;

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
	private static final OLog log_ = Tracing.createLoggerFor(SimpleStatisticInfoHelper.class);

	/** the jdbcTemplate is used to allow access to other than the default database and 
	 * allow raw sql code
	 */
	private final JdbcTemplate jdbcTemplate_;

	/** a map with all sql statements for the supported dbvendors **/
	private final Map<String,String> sql_;

	/** the dbvendor configured **/
	private final String dbVendor_;

	/** the calculated creationdate **/
	private long creationdate_;

	/** whether or not creationdate has been computed **/
	private boolean initialized_ = false;
	
	/**
	 *@TODO
	 *@FIXME
	 * harsh violation of spring idea - yet we run with this until spring is refactored for 6.4 
	 */
	private static volatile SimpleStatisticInfoHelper SINGLETON_;
	
	/** spring **/
	public SimpleStatisticInfoHelper(JdbcTemplate jdbcTemplate, Map<String, String> sql, String dbVendor) {
		jdbcTemplate_ = jdbcTemplate;
		sql_ = sql;
		dbVendor_ = dbVendor;
		SINGLETON_ = this;
	}
	
	/**
	 * Computes the creationdate_ if it's not already computed and returns it
	 * @return the creationdate
	 */
	private synchronized Date doGetFirstLoggingTableCreationDate() {
		if (!initialized_) {
			creationdate_ = jdbcTemplate_.queryForLong(sql_.get(dbVendor_));
			initialized_ = true;
		}
		if (creationdate_!=-1) {
			return new Date(creationdate_*1000);
		}
		// otherwise we have a misconfiguration
		log_.error("getFirstLoggingTableCreationDate: misconfiguration! SimpleStatisticInfoHelper is not configured!");
		return null;
	}
		
	/**
	 * @deprecated refactor SimpleStatisticInfoHelper in 6.4 !!!
	 */
	public static Date getFirstLoggingTableCreationDate() {
		if (SINGLETON_==null) {
			log_.error("getFirstLoggingTableCreationDate: misconfiguration! SimpleStatisticInfoHelper is not configured!");
			return null;
		} else {
			return SINGLETON_.doGetFirstLoggingTableCreationDate();
		}
	}
}
