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
package org.olat.modules.quality.generator.provider.courselectures.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 22.08.2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SearchParameters {

	private Integer minTotalLectures;
	private Integer maxTotalLectures;
	private Integer selectingLecture;
	private boolean lastLectureBlock = false;
	private QualityGeneratorRef finishedDataCollectionForGeneratorAndTopicIdentityRef;
	private QualityGeneratorRef finishedDataCollectionForGeneratorAndTopicRepositoryRef;
	private QualityGeneratorRef excludeGeneratorAndTopicIdentityRef;
	private QualityGeneratorRef excludeGeneratorAndTopicRepositoryRef;
	private IdentityRef teacherRef;
	private Collection<? extends RepositoryEntryRef> courseRefs;
	private Collection<? extends CurriculumElementRef> whiteListRefs;
	private Collection<? extends CurriculumElementRef> blackListRefs;
	private List<? extends OrganisationRef> organisationRefs;
	private Date from;
	private Date to;
	private Collection<Long> excludedEducationalTypeKeys;

	public Integer getMinTotalLectures() {
		return minTotalLectures;
	}

	public void setMinTotalLectures(Integer minTotalLectures) {
		this.minTotalLectures = minTotalLectures;
	}

	public Integer getMaxTotalLectures() {
		return maxTotalLectures;
	}

	public void setMaxTotalLectures(Integer maxTotalLectures) {
		this.maxTotalLectures = maxTotalLectures;
	}

	public Integer getSelectingLecture() {
		return selectingLecture;
	}

	public void setSelectingLecture(Integer selectingLecture) {
		this.selectingLecture = selectingLecture;
	}

	public boolean isLastLectureBlock() {
		return lastLectureBlock;
	}

	public void setLastLectureBlock(boolean lastLectureBlock) {
		this.lastLectureBlock = lastLectureBlock;
	}

	public QualityGeneratorRef getFinishedDataCollectionForGeneratorAndTopicIdentityRef() {
		return finishedDataCollectionForGeneratorAndTopicIdentityRef;
	}

	public void setFinishedDataCollectionForGeneratorAndTopicIdentityRef(QualityGeneratorRef generatorRef) {
		this.finishedDataCollectionForGeneratorAndTopicIdentityRef = generatorRef;
	}

	public QualityGeneratorRef getFinishedDataCollectionForGeneratorAndTopicRepositoryRef() {
		return finishedDataCollectionForGeneratorAndTopicRepositoryRef;
	}

	public void setFinishedDataCollectionForGeneratorAndTopicRepositoryRef(QualityGeneratorRef generatorRef) {
		this.finishedDataCollectionForGeneratorAndTopicRepositoryRef = generatorRef;
	}

	public QualityGeneratorRef getExcludeGeneratorAndTopicIdentityRef() {
		return excludeGeneratorAndTopicIdentityRef;
	}

	public void setExcludeGeneratorAndTopicIdentityRef(QualityGeneratorRef excludeGeneratorRef) {
		this.excludeGeneratorAndTopicIdentityRef = excludeGeneratorRef;
	}

	public QualityGeneratorRef getExcludeGeneratorAndTopicRepositoryRef() {
		return excludeGeneratorAndTopicRepositoryRef;
	}

	public void setExcludeGeneratorAndTopicRepositoryRef(QualityGeneratorRef excludeGeneratorRef) {
		this.excludeGeneratorAndTopicRepositoryRef = excludeGeneratorRef;
	}

	public IdentityRef getTeacherRef() {
		return teacherRef;
	}

	public void setTeacherRef(IdentityRef teacherRef) {
		this.teacherRef = teacherRef;
	}

	public Collection<? extends RepositoryEntryRef> getCourseRefs() {
		if (courseRefs == null) {
			courseRefs = Collections.emptyList();
		}
		return courseRefs;
	}

	public void setCourseRefs(Collection<? extends RepositoryEntryRef> courseRefs) {
		this.courseRefs = courseRefs;
	}

	public Collection<? extends CurriculumElementRef> getWhiteListRefs() {
		if (whiteListRefs == null) {
			whiteListRefs = Collections.emptyList();
		}
		return whiteListRefs;
	}

	public void setWhiteListRefs(Collection<? extends CurriculumElementRef> whiteListRefs) {
		this.whiteListRefs = whiteListRefs;
	}

	public Collection<? extends CurriculumElementRef> getBlackListRefs() {
		if (blackListRefs == null) {
			blackListRefs = Collections.emptyList();
		}
		return blackListRefs;
	}

	public void setBlackListRefs(Collection<? extends CurriculumElementRef> blackListRefs) {
		this.blackListRefs = blackListRefs;
	}

	public List<? extends OrganisationRef> getOrganisationRefs() {
		if (organisationRefs == null) {
			organisationRefs = Collections.emptyList();
		}
		return organisationRefs;
	}

	public void setOrganisationRefs(List<? extends OrganisationRef> organisationRefs) {
		this.organisationRefs = organisationRefs;
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}
	
	public Collection<Long> getExcludedEducationalTypeKeys() {
		return excludedEducationalTypeKeys;
	}
	
	public void setExcludedEducationalTypeKeys(Collection<Long> excludedEducationalTypeKeys) {
		this.excludedEducationalTypeKeys = excludedEducationalTypeKeys;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SearchParameters [minTotalLectures=");
		builder.append(minTotalLectures);
		builder.append(", maxTotalLectures=");
		builder.append(maxTotalLectures);
		builder.append(", selectingLecture=");
		builder.append(selectingLecture);
		builder.append(", lastLectureBlock=");
		builder.append(lastLectureBlock);
		builder.append(", finishedDataCollectionForGeneratorAndTopicIdentityRef=");
		builder.append(finishedDataCollectionForGeneratorAndTopicIdentityRef);
		builder.append(", finishedDataCollectionForGeneratorAndTopicRepositoryRef=");
		builder.append(finishedDataCollectionForGeneratorAndTopicRepositoryRef);
		builder.append(", excludeGeneratorAndTopicIdentityRef=");
		builder.append(excludeGeneratorAndTopicIdentityRef);
		builder.append(", excludeGeneratorAndTopicRepositoryRef=");
		builder.append(excludeGeneratorAndTopicRepositoryRef);
		builder.append(", teacherRef=");
		builder.append(teacherRef);
		builder.append(", courseRefs=");
		builder.append(courseRefs);
		builder.append(", curriculumElementRefs={");
		if (whiteListRefs != null) {
			builder.append(whiteListRefs.stream()
					.map(CurriculumElementRef::getKey)
					.map(k -> k.toString())
					.collect(Collectors.joining(", ")));
		}
		builder.append("]");
		builder.append(", organisationRefs={");
		if (organisationRefs != null) {
			builder.append(organisationRefs.stream()
					.map(OrganisationRef::getKey)
					.map(k -> k.toString())
					.collect(Collectors.joining(", ")));
		}
		builder.append("]");
		builder.append(", from=");
		builder.append(from);
		builder.append(", to=");
		builder.append(to);
		builder.append("]");
		builder.append(", excludedEducationalTypeKeys=[");
		if (excludedEducationalTypeKeys != null) {
			builder.append(excludedEducationalTypeKeys.stream()
					.map(r -> r.toString())
					.collect(Collectors.joining(", ")));
		}
		builder.append("]");
		return builder.toString();
	}

}
