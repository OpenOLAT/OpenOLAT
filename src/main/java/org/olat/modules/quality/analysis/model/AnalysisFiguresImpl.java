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
package org.olat.modules.quality.analysis.model;

import org.olat.modules.quality.analysis.AnlaysisFigures;

/**
 * 
 * Initial date: 05.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisFiguresImpl implements AnlaysisFigures {

	private final Long dataCollectionCount;
	private final Long participationCount;
	private final Long publicPparticipationCount;
	
	public AnalysisFiguresImpl(Long dataCollectionCount, Long totalCount, Long publicCount, Long publicDoneCount) {
		this.dataCollectionCount = zeroIfNull(dataCollectionCount);
		this.participationCount = zeroIfNull(totalCount) - zeroIfNull(publicCount);
		this.publicPparticipationCount = zeroIfNull(publicDoneCount);
	}
	
	private Long zeroIfNull(Long value) {
		return value != null? value: Long.valueOf(0);
	}

	@Override
	public Long getDataCollectionCount() {
		return dataCollectionCount;
	}

	@Override
	public Long getParticipationCount() {
		return participationCount;
	}

	@Override
	public Long getPublicParticipationCount() {
		return publicPparticipationCount;
	}

}
