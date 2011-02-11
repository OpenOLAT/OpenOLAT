package org.olat.course.statistic;

import java.util.List;

/**
 * Spring helper class to manage different SQL flavours
 * <P>
 * Initial Date:  01.03.2010 <br>
 * @author Stefan
 */
public class StatisticUpdateConfig {
	
	private List<IStatisticUpdater> updaters_;
	
	public StatisticUpdateConfig() {
		// nothing to be done here
	}
	
	public void setUpdaters(List<IStatisticUpdater> updaters) {
		updaters_ = updaters;
	}
	
	public List<IStatisticUpdater> getUpdaters() {
		return updaters_;
	}

}
