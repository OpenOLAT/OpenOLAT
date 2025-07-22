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
package org.olat.modules.curriculum;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.Group;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface CurriculumElement extends CurriculumElementShort, CreateInfo, ModifiedInfo {
	
	public String getIdentifier();
	
	public void setIdentifier(String identifier);
	
	public void setDisplayName(String displayName);
	
	public String getDescription();
	
	public void setDescription(String description);

	String getTeaser();

	void setTeaser(String teaser);
	
	String getAuthors();

	void setAuthors(String authors);

	String getMainLanguage();

	void setMainLanguage(String mainLanguage);

	String getLocation();

	void setLocation(String location);

	String getObjectives();
	
	void setObjectives(String objectives);

	String getRequirements();

	void setRequirements(String requirements);

	String getCredits();

	void setCredits(String credits);

	String getExpenditureOfWork();

	void setExpenditureOfWork(String expenditureOfWork);
	
	public String getExternalId();
	
	public void setExternalId(String externalId);

	public Date getBeginDate();
	
	public void setBeginDate(Date date);
	
	public Date getEndDate();
	
	public void setEndDate(Date date);

	Long getMinParticipants();

	void setMinParticipants(Long minParticipants);

	Long getMaxParticipants();

	void setMaxParticipants(Long maxParticipants);

	Set<TaughtBy> getTaughtBys();

	void setTaughtBys(Set<TaughtBy> taughtBys);

	/**
	 * @return true if calendars aggregation is available at this level of the curriculum
	 */
	public CurriculumCalendars getCalendars();

	public void setCalendars(CurriculumCalendars calendars);
	
	/**
	 * @return true if absence management aggregation is available at this level of the curriculum
	 */
	public CurriculumLectures getLectures();
	
	public void setLectures(CurriculumLectures lectures);
	
	/**
	 * @return true if learning progress aggregation is available at this level of the curriculum
	 */
	public CurriculumLearningProgress getLearningProgress();
	
	public void setLearningProgress(CurriculumLearningProgress learningProgress);

	boolean isShowOutline();

	void setShowOutline(boolean showOutline);

	boolean isShowLectures();

	void setShowLectures(boolean showLectures);
	
	boolean isShowCertificateBenefit();

	void setShowCertificateBenefit(boolean showCertificateBenefit);

	boolean isShowCreditPointsBenefit();

	void setShowCreditPointsBenefit(boolean showCreditPointsBenefit);
	
	public CurriculumElementStatus getElementStatus();
	
	
	public String getMaterializedPathKeys();
	
	public List<Long> getMaterializedPathKeysList();
	
	public void setManagedFlags(CurriculumElementManagedFlag[] flags);
	
	
	public Automation getAutoInstantiation();

	public void setAutoInstantiation(Automation instantiation);
	
	public Automation getAutoAccessForCoach();

	public void setAutoAccessForCoach(Automation accessForCoach);

	public Automation getAutoPublished();

	public void setAutoPublished(Automation published);

	public Automation getAutoClosed();

	public void setAutoClosed(Automation closed);
	
	public boolean hasAutomation();
	
	/**
	 * The method check the type and the parent.
	 * 
	 * @return true if the element is a root element (an implementation)
	 *  with no structure (single element) and a maximum of one course
	 */
	public boolean isSingleCourseImplementation();
	
	/**
	 * @return The position of an element as child of a parent element
	 */
	public Integer getPos();
	
	/**
	 * @return The position of a root element directly under the curriculum
	 */
	public Integer getPosCurriculum();
	
	public Curriculum getCurriculum();
	
	public CurriculumElement getParent();
	
	public String getNumberImpl();
	
	public CurriculumElementType getType();
	
	public void setType(CurriculumElementType type);
	
	public RepositoryEntryEducationalType getEducationalType();
	
	public void setEducationalType(RepositoryEntryEducationalType educationalType);
	
	public Group getGroup();
	
	public Set<CurriculumElementToTaxonomyLevel> getTaxonomyLevels();
	
	public OLATResource getResource();

}
