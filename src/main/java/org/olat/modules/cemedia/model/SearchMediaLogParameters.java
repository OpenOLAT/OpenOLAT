package org.olat.modules.cemedia.model;

import java.util.List;

import org.olat.core.util.DateRange;
import org.olat.modules.cemedia.MediaLog;

/**
 * 
 * Initial date: 26 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchMediaLogParameters {
	
	private DateRange dateRange;
	private List<Long> identityKeys;
	private List<MediaLog.Action> actions;
	private String tempIdentifier;

	public DateRange getDateRange() {
		return dateRange;
	}

	public void setDateRange(DateRange dateRange) {
		this.dateRange = dateRange;
	}

	public List<Long> getIdentityKeys() {
		return identityKeys;
	}

	public void setIdentityKeys(List<Long> identityKeys) {
		this.identityKeys = identityKeys;
	}

	public List<MediaLog.Action> getActions() {
		return actions;
	}

	public void setActions(List<MediaLog.Action> actions) {
		this.actions = actions;
	}

	public String getTempIdentifier() {
		return tempIdentifier;
	}

	public void setTempIdentifier(String tempIdentifier) {
		this.tempIdentifier = tempIdentifier;
	}
}
