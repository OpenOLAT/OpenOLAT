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
package org.olat.course.nodes.practice;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.course.nodes.practice.model.PracticeItem;
import org.olat.course.nodes.practice.model.PracticeResourceInfos;
import org.olat.course.nodes.practice.model.RankedIdentity;
import org.olat.course.nodes.practice.model.SearchPracticeItemParameters;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 5 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface PracticeService {
	
	public PracticeResource createResource(RepositoryEntry courseEntry, String subIdent, RepositoryEntry testEntry);
	
	public PracticeResource createResource(RepositoryEntry courseEntry, String subIdent, Pool pool);
	
	public PracticeResource createResource(RepositoryEntry courseEntry, String subIdent, QuestionItemCollection collection);
	
	public PracticeResource createResource(RepositoryEntry courseEntry, String subIdent, OLATResource sharedResource);
	
	public void deleteResource(PracticeResource resource);
	
	
	
	public List<PracticeResource> getResources(RepositoryEntry courseEntry, String subIdent);
	
	public List<PracticeResourceInfos> getResourcesInfos(IdentityRef identity, RepositoryEntry courseEntry, String subIdent);
	
	
	public List<PracticeItem> generateItems(List<PracticeResource> resources,
			SearchPracticeItemParameters searchParams, int numOfItems, Locale locale);
	
	public List<PracticeAssessmentItemGlobalRef> getPracticeAssessmentItemGlobalRefs(List<PracticeItem> items, IdentityRef identity);
	
	
	public List<AssessmentTestSession> getTerminatedSeries(IdentityRef identity, RepositoryEntry courseEntry, String subIdent,
			Date from, Date to);
	
	public List<AssessmentTestSession> getSeries(IdentityRef identity, RepositoryEntry courseEntry, String subIdent);
	
	public void resetSeries(IdentityRef identity, RepositoryEntry courseEntry, String subIdent);
	
	public long countCompletedSeries(IdentityRef identity, RepositoryEntry courseEntry, String subIdent);
	
	public double getProcentCorrectness(AssessmentTestSession testSession);
	
	public PracticeAssessmentItemGlobalRef updateAssessmentItemGlobalRef(Identity identity, String identifier, boolean firstAttempt, boolean correct);
	
	
	public List<TaxonomyLevel> getTaxonomyWithDescendants(List<Long> keys);
	
	public List<RankedIdentity> getRankList(Identity identity, RepositoryEntry courseEntry, String subIdent, int numOfEntries);


}
