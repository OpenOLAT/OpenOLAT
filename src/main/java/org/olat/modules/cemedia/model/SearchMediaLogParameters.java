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
