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
package org.olat.course.nodes.iq;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 
 * Initial date: 20 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExtraInfos implements Comparable<ExtraInfos> {

	private final Integer extraTimeInSeconds;
	private Integer compensationExtraTimeInSeconds;
	private final Date start;
	private final BigDecimal maxScore;
	
	public ExtraInfos() {
		this(null, null, null);
	}
	
	public ExtraInfos(Integer extraTimeInSeconds, Date start, BigDecimal maxScore) {
		this.extraTimeInSeconds = extraTimeInSeconds;
		this.start = start;
		this.maxScore = maxScore;
	}

	public Integer getExtraTimeInSeconds() {
		return extraTimeInSeconds;
	}
	
	public Integer getCompensationExtraTimeInSeconds() {
		return compensationExtraTimeInSeconds;
	}
	
	public void setCompensationExtraTimeInSeconds(Integer compensationExtraTimeInSeconds) {
		this.compensationExtraTimeInSeconds = compensationExtraTimeInSeconds;
	}

	public Date getStart() {
		return start;
	}
	
	public BigDecimal getMaxScore() {
		return maxScore;
	}

	@Override
	public int compareTo(ExtraInfos o) {
		if(o == null) return -1;
		if(extraTimeInSeconds == null && o.extraTimeInSeconds == null) return 0;
		if(extraTimeInSeconds != null && o.extraTimeInSeconds == null) return -1;
		if(extraTimeInSeconds == null && o.extraTimeInSeconds != null) return 1;
		return extraTimeInSeconds.compareTo(o.extraTimeInSeconds);
	}
}
