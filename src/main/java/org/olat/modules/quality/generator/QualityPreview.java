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
package org.olat.modules.quality.generator;

import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 1 Dec 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface QualityPreview {
	
	public String getIdentifier();
	
	public String getTitle();

	public Date getCreationDate();

	public Date getStart();

	public Date getDeadline();
	
	public QualityDataCollectionTopicType getTopicType();
	
	public String getTopicCustom();
	
	public Identity getTopicIdentity();
	
	public Organisation getTopicOrganisation();
	
	public Curriculum getTopicCurriculum();
	
	public CurriculumElement getTopicCurriculumElement();
	
	public RepositoryEntry getTopicRepositoryEntry();
	
	public QualityGenerator getGenerator();
	
	public Long getGeneratorProviderKey();
	
	public RepositoryEntry getFormEntry();
	
	public Long getDataCollectionKey();
	
	public QualityPreviewStatus getStatus();
	
	public List<Organisation> getOrganisations();
	
	public Long getNumParticipants();
	
	public boolean isRestrictedEdit();

}
