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
package org.olat.modules.quality;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface QualityDataCollection extends QualityDataCollectionLight, CreateInfo, ModifiedInfo {
	
	public void setStatus(QualityDataCollectionStatus status);
	
	public void setTitle(String title);
	
	public void setStart(Date start);
	
	public void setDeadline(Date deadline);
	
	public String getTopicCustom();
	
	public void setTopicCustom(String topic);
	
	public QualityDataCollectionTopicType getTopicType();
	
	public void setTopicType(QualityDataCollectionTopicType type);
	
	public Identity getTopicIdentity();
	
	public void setTopicIdentity(Identity identity);
	
	public Organisation getTopicOrganisation();
	
	public void setTopicOrganisation(Organisation organisation);
	
	public Curriculum getTopicCurriculum();
	
	public void setTopicCurriculum(Curriculum curriculum);
	
	public CurriculumElement getTopicCurriculumElement();
	
	public void setTopicCurriculumElement(CurriculumElement curriculumElement);
	
	public RepositoryEntry getTopicRepositoryEntry();
	
	public void setTopicRepositoryEntry(RepositoryEntry entry);

	public QualityGenerator getGenerator();
	
	public Long getGeneratorProviderKey();
}
