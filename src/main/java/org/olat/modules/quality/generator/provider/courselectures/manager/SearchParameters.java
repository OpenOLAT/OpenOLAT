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

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 22.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SearchParameters {
	
	private Integer minTotalLectures;
	private Integer selectingLecture;
	private QualityGeneratorRef excludeGeneratorAndTopicIdentityRef;
	private QualityGeneratorRef excludeGeneratorAndTopicRepositoryRef;
	private IdentityRef teacherRef;
	private Collection<? extends RepositoryEntryRef> courseRefs;
	private Collection<? extends CurriculumElementRef> curriculumElementRefs;
	private Collection<? extends OrganisationRef> organsationRefs;
	private Date from;
	private Date to;

	public Integer getMinTotalLectures() {
		return minTotalLectures;
	}

	public void setMinTotalLectures(Integer minTotalLectures) {
		this.minTotalLectures = minTotalLectures;
	}

	public Integer getSelectingLecture() {
		return selectingLecture;
	}

	public void setSelectingLecture(Integer selectingLecture) {
		this.selectingLecture = selectingLecture;
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

	public Collection<? extends CurriculumElementRef> getCurriculumElementRefs() {
		if (curriculumElementRefs == null) {
			curriculumElementRefs = Collections.emptyList();
		}
		return curriculumElementRefs;
	}

	public void setCurriculumElementRefs(Collection<? extends CurriculumElementRef> curriculumElementRefs) {
		this.curriculumElementRefs = curriculumElementRefs;
	}

	public Collection<? extends OrganisationRef> getOrgansationRefs() {
		if (organsationRefs == null) {
			organsationRefs = Collections.emptyList();
		}
		return organsationRefs;
	}

	public void setOrgansationRefs(Collection<? extends OrganisationRef> organsationRefs) {
		this.organsationRefs = organsationRefs;
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


}
