package org.olat.course.statistic;

/**
 * Simple o_property setter and getter class to manage logging versions.
 * <p>
 * The idea is to set a tuple <version,startingTimeMillis> in the o_property
 * table once a breaking change in the logging system happens, rendering
 * data in the o_loggingversion prior to startingTimeMillis unusable for
 * statistics. Or at least, older data needs to be treated the old way and
 * newer data a new way.
 * <p>
 * This is to prevent having to add a version column to the o_loggingtable
 * for the sole purpose of this infrequent version change.
 * <P>
 * Initial Date:  04.03.2010 <br>
 * @author Stefan
 */
public interface LoggingVersionManager {

	/**
	 * Insert a property flagging all logging actions starting now with the given version.
	 * <p>
	 * The meaning of version is not yet defined - no one uses this feature yet.
	 * Still, we add this property to be upwards compatible.
	 * @param version the version to be set in the property, starting at 1 counting upwards
	 */
	public void setLoggingVersionStartingNow(int version);
	
	/**
	 * Insert a property flagging all logging actions starting at the given
	 * startingTimeMillis with the given version.
	 * <p>
	 * The meaning of version is not yet defined - no one uses this feature yet.
	 * Still, we add this property to be upwards compatible.
	 * @param version the version to be set in the property, starting at 1 counting upwards
	 * @param startingTimeMillis the time at which the given version should start
	 */
	public void setLoggingVersion(int version, long startingTimeMillis);
	
	/**
	 * Returns the starting time at which the given logging version was introduced
	 * to this system.
	 * <p>
	 * The meaning of version is not yet defined - no one uses this feature yet
	 * @param version the version for which the starting time should be looked up
	 * @return the starting time at which the given version starts - or -1 if that
	 * version has not been set in the LoggingVersionManager yet.
	 */
	public long getStartingTimeForVersion(int version);
	
}
