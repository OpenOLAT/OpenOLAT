package org.olat.course.statistic;

import java.util.Date;

/**
 * Each statistic table has a corresponding IStatisticUpdater configured, which takes
 * care of regenerating the statistic table.
 * <p>
 * Note that regenerating the statistic table means first flushing the whole content
 * then generating it again.
 * <p>
 * We might later on come about more clever ways to update deltas only - but for now
 * this seems just fine.
 * <p>
 * NOTE: It is the duty of the IStatisticUpdater to register itself with the
 * StatisticUpdaterManager in order to be called by the cronjob or via admin console !!!
 * <P>
 * Initial Date:  12.02.2010 <br>
 * @author Stefan
 */
public interface IStatisticUpdater {

	/**
	 * Update the statistic table belonging to this implementor
	 * @param fullRecalculation when set to true the statisticupdater should do a complete recalc of all stats,
	 * rather than updating since the last call to updateStatistic
	 * @param from update the statistics starting at the given from date - note that this is never null
	 * irrespective of fullRecalculation true or false
	 * @param until update the statistics ending at the given until date - this is never null
	 * @param statisticUpdateManager the StatisticUpdateManager is passed to the IStatisticUpdater for
	 * callbacks and utility functions such as access to the lastUpdated property
	 */
	void updateStatistic(boolean fullRecalculation, Date from, Date until, StatisticUpdateManager statisticUpdateManager);

}
