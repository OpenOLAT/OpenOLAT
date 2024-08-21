/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.quality.generator.model;

import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityPreview;
import org.olat.modules.quality.generator.QualityPreviewStatus;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 1 Dec 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityPreviewImpl implements QualityPreview {
	
	private String identifier;
	private String title;
	private Date creationDate;
	private Date start;
	private Date deadline;
	private boolean qualitativeFeedback;
	private QualityDataCollectionTopicType topicType;
	private String topicCustom;
	private Identity topicIdentity;
	private Organisation topicOrganisation;
	private Curriculum topicCurriculum;
	private CurriculumElement topicCurriculumElement;
	private RepositoryEntry topicRepositoryEntry;
	private QualityGenerator generator;
	private Long generatorProviderKey;
	private RepositoryEntry formEntry;
	private Long dataCollectionKey;
	private QualityPreviewStatus status;
	private List<Organisation> organisations;
	private Long numParticipants;
	private boolean restrictedEdit;
	
	@Override
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getStart() {
		return start;
	}
	
	public void setStart(Date start) {
		this.start = start;
	}
	
	@Override
	public Date getDeadline() {
		return deadline;
	}
	
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	
	public boolean isQualitativeFeedback() {
		return qualitativeFeedback;
	}
	
	public void setQualitativeFeedback(boolean qualitativeFeedback) {
		this.qualitativeFeedback = qualitativeFeedback;
	}
	
	@Override
	public QualityDataCollectionTopicType getTopicType() {
		return topicType;
	}
	
	public void setTopicType(QualityDataCollectionTopicType topicType) {
		this.topicType = topicType;
	}
	
	@Override
	public String getTopicCustom() {
		return topicCustom;
	}
	
	public void setTopicCustom(String topicCustom) {
		this.topicCustom = topicCustom;
	}
	
	@Override
	public Identity getTopicIdentity() {
		return topicIdentity;
	}
	
	public void setTopicIdentity(Identity topicIdentity) {
		this.topicIdentity = topicIdentity;
	}
	
	@Override
	public Organisation getTopicOrganisation() {
		return topicOrganisation;
	}
	
	public void setTopicOrganisation(Organisation topicOrganisation) {
		this.topicOrganisation = topicOrganisation;
	}
	
	@Override
	public Curriculum getTopicCurriculum() {
		return topicCurriculum;
	}
	
	public void setTopicCurriculum(Curriculum topicCurriculum) {
		this.topicCurriculum = topicCurriculum;
	}
	
	@Override
	public CurriculumElement getTopicCurriculumElement() {
		return topicCurriculumElement;
	}
	
	public void setTopicCurriculumElement(CurriculumElement topicCurriculumElement) {
		this.topicCurriculumElement = topicCurriculumElement;
	}
	
	@Override
	public RepositoryEntry getTopicRepositoryEntry() {
		return topicRepositoryEntry;
	}
	
	public void setTopicRepositoryEntry(RepositoryEntry topicRepositoryEntry) {
		this.topicRepositoryEntry = topicRepositoryEntry;
	}
	
	@Override
	public QualityGenerator getGenerator() {
		return generator;
	}
	
	public void setGenerator(QualityGenerator generator) {
		this.generator = generator;
	}
	
	@Override
	public Long getGeneratorProviderKey() {
		return generatorProviderKey;
	}
	
	public void setGeneratorProviderKey(Long generatorProviderKey) {
		this.generatorProviderKey = generatorProviderKey;
	}

	@Override
	public RepositoryEntry getFormEntry() {
		return formEntry;
	}

	public void setFormEntry(RepositoryEntry formEntry) {
		this.formEntry = formEntry;
	}

	@Override
	public Long getDataCollectionKey() {
		return dataCollectionKey;
	}

	public void setDataCollectionKey(Long dataCollectionKey) {
		this.dataCollectionKey = dataCollectionKey;
	}

	@Override
	public QualityPreviewStatus getStatus() {
		return status;
	}

	public void setStatus(QualityPreviewStatus status) {
		this.status = status;
	}

	@Override
	public List<Organisation> getOrganisations() {
		return organisations;
	}

	public void setOrganisations(List<Organisation> organisations) {
		this.organisations = organisations;
	}

	@Override
	public Long getNumParticipants() {
		return numParticipants;
	}

	public void setNumParticipants(Long numParticipants) {
		this.numParticipants = numParticipants;
	}

	@Override
	public boolean isRestrictedEdit() {
		return restrictedEdit;
	}

	public void setRestrictedEdit(boolean restrictedEdit) {
		this.restrictedEdit = restrictedEdit;
	}

	@Override
	public String toString() {
		return "QualityPreviewImpl [identifier=" + identifier + ", start=" + start + ", title=" + title + ", generator=" + generator + "]";
	}

}
