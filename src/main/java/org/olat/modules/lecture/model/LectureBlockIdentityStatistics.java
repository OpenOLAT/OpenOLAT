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
package org.olat.modules.lecture.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.modules.lecture.LectureRateWarning;

/**
 * 
 * 
 * Initial date: 16 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockIdentityStatistics extends LectureBlockStatistics {
	
	private final String identityName;
	private final String[] identityProps;
	
	private List<Long> absentLectureBlocks;
	private LectureRateWarning explicitWarning;
	
	public LectureBlockIdentityStatistics(Long identityKey, String identityName, String[] identityProps,
			Long lectureBlockKey, Long repoKey, String displayName, String externalRef, boolean calculateRate, double requiredRate,
			Date firstAdmission) {
		super(identityKey, lectureBlockKey, repoKey, displayName, externalRef, calculateRate, requiredRate, firstAdmission);
		this.identityName = identityName;
		this.identityProps = identityProps;
	}
	
	public String getIdentityName() {
		return identityName;
	}
	
	public String[] getIdentityProps() {
		return identityProps;
	}
	
	public LectureRateWarning getExplicitWarning() {
		return explicitWarning;
	}

	public void setExplicitWarning(LectureRateWarning explicitWarning) {
		this.explicitWarning = explicitWarning;
	}
	
	/**
	 * @return null if empty
	 */
	public List<Long> getAbsentLectureBlocks() {
		return absentLectureBlocks;
	}
	
	public void addAbsentLectureBlock(Long lectureBlockKey) {
		if(lectureBlockKey == null) return;
		
		if(absentLectureBlocks == null) {
			absentLectureBlocks = new ArrayList<>();
		}
		absentLectureBlocks.add(lectureBlockKey);
	}

	public String getIdentityProp(int pos) {
		if(identityProps != null && pos >= 0 && pos < identityProps.length) {
			return identityProps[pos];
		}
		return null;
	}
	
	public LectureBlockIdentityStatistics cloneForAggregation() {
		LectureBlockIdentityStatistics clone
			= new LectureBlockIdentityStatistics(getIdentityKey(), identityName, identityProps, null,null, null, null, false, 0.0d, null);
		clone.addTotalAbsentLectures(getTotalAbsentLectures());
		clone.addTotalAttendedLectures(getTotalAttendedLectures());
		clone.addTotalAuthorizedAbsentLectures(getTotalAuthorizedAbsentLectures());
		clone.addTotalDispensationLectures(getTotalDispensationLectures());
		clone.addTotalEffectiveLectures(getTotalEffectiveLectures());		
		clone.addTotalLectureBlocks(getTotalLectureBlocks());
		clone.addTotalPersonalPlannedLectures(getTotalPersonalPlannedLectures());
		if(absentLectureBlocks != null) {
			clone.absentLectureBlocks = new ArrayList<>(absentLectureBlocks);
		}
		return clone;
	}
	
	public LectureBlockIdentityStatistics cloneAll() {
		LectureBlockIdentityStatistics clone = new LectureBlockIdentityStatistics(getIdentityKey(),
				identityName, identityProps, getLectureBlockKey(), getRepoKey(), getDisplayName(), getExternalRef(), isCalculateRate(), getRequiredRate(), getFirstAdmission());
		clone.addTotalAbsentLectures(getTotalAbsentLectures());
		clone.addTotalAttendedLectures(getTotalAttendedLectures());
		clone.addTotalAuthorizedAbsentLectures(getTotalAuthorizedAbsentLectures());
		clone.addTotalDispensationLectures(getTotalDispensationLectures());
		clone.addTotalEffectiveLectures(getTotalEffectiveLectures());		
		clone.addTotalLectureBlocks(getTotalLectureBlocks());
		clone.addTotalPersonalPlannedLectures(getTotalPersonalPlannedLectures());
		if(absentLectureBlocks != null) {
			clone.absentLectureBlocks = new ArrayList<>(absentLectureBlocks);
		}
		return clone;
	}
	
	public void aggregate(LectureBlockIdentityStatistics statistics) {
		addTotalAbsentLectures(statistics.getTotalAbsentLectures());
		addTotalAttendedLectures(statistics.getTotalAttendedLectures());
		addTotalAuthorizedAbsentLectures(statistics.getTotalAuthorizedAbsentLectures());
		addTotalDispensationLectures(statistics.getTotalDispensationLectures());
		addTotalEffectiveLectures(statistics.getTotalEffectiveLectures());		
		addTotalLectureBlocks(statistics.getTotalLectureBlocks());
		addTotalPersonalPlannedLectures(statistics.getTotalPersonalPlannedLectures());
		if(statistics.getAbsentLectureBlocks() != null) {
			if(absentLectureBlocks == null) {
				absentLectureBlocks = new ArrayList<>(statistics.getAbsentLectureBlocks());
			} else {
				absentLectureBlocks.addAll(statistics.getAbsentLectureBlocks());
			}
		}
	}
}
