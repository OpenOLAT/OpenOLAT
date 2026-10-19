/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.scorm.assessment;

import java.util.Date;
import java.util.List;

/**
 * 
 * Initial date: 16 oct. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ScoSummaryRow {
	
	private final boolean lastRun;
	private final String scoId;
	private final Date date;
	private final Double score;
	private final String totalTime;
	private final List<CmiData> cmiData;
	
	public ScoSummaryRow(String scoId, boolean lastRun, Date date, Double score, String totalTime, List<CmiData> cmiData) {
		this.scoId = scoId;
		this.lastRun = lastRun;
		this.date = date;
		this.score = score;
		this.totalTime = totalTime;
		this.cmiData = cmiData;
	}

	public String getScoId() {
		return scoId;
	}
	
	public boolean isLastRun() {
		return lastRun;
	}

	public Date getDate() {
		return date;
	}
	
	public Double getScore() {
		return score;
	}
	
	public String getTotalTime() {
		return totalTime;
	}
	
	public List<CmiData> getCmiData() {
		return cmiData;
	}

}
