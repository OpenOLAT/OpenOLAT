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
package org.olat.course.nodes.practice.manager;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.practice.PlayMode;
import org.olat.course.nodes.practice.PracticeAssessmentItemGlobalRef;
import org.olat.course.nodes.practice.PracticeResource;
import org.olat.course.nodes.practice.PracticeService;
import org.olat.course.nodes.practice.manager.LevelMixHelper.MixLevel;
import org.olat.course.nodes.practice.model.PracticeItem;
import org.olat.course.nodes.practice.model.PracticeResourceInfos;
import org.olat.course.nodes.practice.model.RankedIdentity;
import org.olat.course.nodes.practice.model.SearchPracticeItemParameters;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.AssessmentItemSessionDAO;
import org.olat.ims.qti21.manager.AssessmentTestSessionDAO;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.pool.QTI21MetadataConverter;
import org.olat.ims.qti21.ui.editor.metadata.ManifestMetadataItemized;
import org.olat.imscp.xml.manifest.ResourceType;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.manager.QEducationalContextDAO;
import org.olat.modules.qpool.manager.QItemQueriesDAO;
import org.olat.modules.qpool.manager.QItemTypeDAO;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Initial date: 5 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PracticeServiceImpl implements PracticeService {
	
	private static final Logger log = Tracing.createLoggerFor(PracticeServiceImpl.class);

	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private QItemTypeDAO itemTypeDao;
	@Autowired
	private QPoolService qPoolService;
	@Autowired
	private QItemQueriesDAO itemQueriesDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private PracticeResourceDAO practiceResourceDao;
	@Autowired
	private QEducationalContextDAO educationalContextDao;
	@Autowired
	private AssessmentTestSessionDAO assessmentTestSessionDao;
	@Autowired
	private AssessmentItemSessionDAO assessmentItemSessionDao;
	@Autowired
	private PracticeQuestionItemQueries practiceQuestionItemQueries;
	@Autowired
	private PracticeAssessmentTestSessionDAO practiceAssessmentTestSessionDao;
	@Autowired
	private PracticeAssessmentItemGlobalRefDAO practiceAssessmentItemGlobalRefDao;

	@Override
	public PracticeResource createResource(RepositoryEntry courseEntry, String subIdent, RepositoryEntry testEntry) {
		return practiceResourceDao.createResource(courseEntry, subIdent, testEntry, null, null, null);
	}

	@Override
	public PracticeResource createResource(RepositoryEntry courseEntry, String subIdent, Pool pool) {
		return practiceResourceDao.createResource(courseEntry, subIdent, null, pool, null, null);
	}

	@Override
	public PracticeResource createResource(RepositoryEntry courseEntry, String subIdent, QuestionItemCollection collection) {
		return practiceResourceDao.createResource(courseEntry, subIdent, null, null, collection, null);
	}

	@Override
	public PracticeResource createResource(RepositoryEntry courseEntry, String subIdent, OLATResource sharedResource) {
		return practiceResourceDao.createResource(courseEntry, subIdent, null, null, null, sharedResource);
	}

	@Override
	public void deleteResource(PracticeResource resource) {
		practiceResourceDao.deleteResource(resource);
	}

	@Override
	public List<PracticeResource> getResources(RepositoryEntry courseEntry, String subIdent) {
		return practiceResourceDao.getResources(courseEntry, subIdent);
	}
	
	@Override
	public List<PracticeResourceInfos> getResourcesInfos(IdentityRef identity, RepositoryEntry courseEntry, String subIdent) {
		List<PracticeResource> resources = getResources(courseEntry, subIdent);
		List<PracticeResourceInfos> infos = new ArrayList<>(resources.size());
		for(PracticeResource resource:resources) {
			if(resource.getTestEntry() != null) {
				int numOfRefs = countItemsOfRepositoryEntry(resource.getTestEntry());
				infos.add(new PracticeResourceInfos(resource, numOfRefs));
			} else if(resource.getItemCollection() != null) {
				SearchQuestionItemParams params = new SearchQuestionItemParams();
				params.setCollection(resource.getItemCollection());
				int numOfItems	= itemQueriesDao.countItems(params);
				infos.add(new PracticeResourceInfos(resource, numOfItems));
			} else if(resource.getPool() != null) {
				SearchQuestionItemParams params = new SearchQuestionItemParams();
				params.setPoolKey(resource.getPool().getKey());
				int numOfItems	= itemQueriesDao.countItems(params);
				infos.add(new PracticeResourceInfos(resource, numOfItems));
			} else if(resource.getResourceShare() != null) {
				SearchQuestionItemParams params = new SearchQuestionItemParams();
				params.setResource(resource.getResourceShare());
				int numOfItems	= itemQueriesDao.countItems(params);
				infos.add(new PracticeResourceInfos(resource, numOfItems));
			}
		}
		return infos;
	}

	@Override
	public List<PracticeItem> generateItems(List<PracticeResource> resources, SearchPracticeItemParameters searchParams,
			int numOfItems, Locale locale) {
		
		List<Pool> pools = new ArrayList<>();
		List<OLATResource> shares = new ArrayList<>();
		List<QuestionItemCollection> collections = new ArrayList<>();
		QTI21PracticeMetadataConverter metadataConverter = null;

		List<PracticeItem> proposedItems = new ArrayList<>();
		for(PracticeResource resource:resources) {
			if(resource.getTestEntry() != null) {
				if(metadataConverter == null) {
					List<TaxonomyLevel> levelKeys = searchParams.getDescendantsLevels();
					if(levelKeys == null) {
						levelKeys = qPoolService.getTaxonomyLevels();
					}
					metadataConverter = new QTI21PracticeMetadataConverter(levelKeys, itemTypeDao, educationalContextDao);
				}
				List<PracticeItem> resourceItems = loadItemsOfRepositoryEntry(resource.getTestEntry(), locale, searchParams, metadataConverter);
				proposedItems.addAll(resourceItems);
			} else if(resource.getItemCollection() != null) {
				collections.add(resource.getItemCollection());
			} else if(resource.getPool() != null) {
				pools.add(resource.getPool());
			} else if(resource.getResourceShare() != null) {
				shares.add(resource.getResourceShare());
			}
		}
		
		if(!proposedItems.isEmpty()) {
			if(searchParams.getPlayMode() == PlayMode.newQuestions) {
				proposedItems = filterNewQuestionsPlayMode(proposedItems, searchParams.getIdentity());
			} else if(searchParams.getPlayMode() == PlayMode.incorrectQuestions) {
				proposedItems = filterIncorrectQuestionsPlayMode(proposedItems, searchParams.getIdentity());
			}
		}
		
		log.debug("{} questions from tests", proposedItems.size());
		
		if(!collections.isEmpty() || !pools.isEmpty() || !shares.isEmpty()) {
			List<QuestionItem> items = practiceQuestionItemQueries.searchItems(searchParams, collections, pools, shares,
					searchParams.getIdentity());
			log.debug("{} questions from QPool", items.size());
			
			for(QuestionItem item:items) {
				proposedItems.add(new PracticeItem(item));
			}
		}
		
		if(searchParams.getPlayMode() == PlayMode.freeShuffle) {
			long completedSessions = practiceAssessmentTestSessionDao.countCompletedTestSessions(searchParams.getCourseEntry(),
					searchParams.getCourseEntry(), searchParams.getSubIdent(), searchParams.getIdentity());
			List<PracticeAssessmentItemGlobalRef> globalRefs = getPracticeAssessmentItemGlobalRefs(proposedItems, searchParams.getIdentity());
			proposedItems = filterFreeShufflePlayMode((int)completedSessions + 1, proposedItems, numOfItems, true, globalRefs);
		}
		
		proposedItems = trimItems(proposedItems, numOfItems);
		Collections.shuffle(proposedItems);
		return proposedItems;
	}
		
	protected List<PracticeItem> filterFreeShufflePlayMode(int currentSession, List<PracticeItem> practiceItems,
			int maxQuestions, boolean date, List<PracticeAssessmentItemGlobalRef> globalRefs) {

		Map<String,PracticeAssessmentItemGlobalRef> identifierRefs = globalRefs.stream()
				.collect(Collectors.toMap(PracticeAssessmentItemGlobalRef::getIdentifier, ref -> ref, (u, v) -> u));
		
		List<PracticeItem> leveledItems = new ArrayList<>(maxQuestions);
		
		int additionalQuestions = 0;
		List<MixLevel> levelMix = LevelMixHelper.mix(currentSession);
		for(int i=levelMix.size(); i-->0; ) {
			MixLevel ml = levelMix.get(i);
			if(ml.isEmpty()) {
				continue;
			}
			
			int partOfQuestions = ml.numOfQuestions(maxQuestions);
			int maxResults = additionalQuestions + partOfQuestions; 
			int daysBeforeRepetition = 0;
			if(date) {
				daysBeforeRepetition = ml.getNumOfDaysBeforeRepetition();
			}
			
			List<PracticeItem>  items = filterLevel(ml.getLevel(), daysBeforeRepetition, practiceItems, maxResults, identifierRefs);
			if(items.size() < maxResults) {
				additionalQuestions = maxResults - items.size();
			}
			
			log.debug("{} questions of level {}, requested {}", items.size(),  ml.getLevel(), maxResults);
			leveledItems.addAll(items);
		}
		
		if(leveledItems.size() < maxQuestions) {
			fillRandomly(leveledItems, practiceItems, maxQuestions - leveledItems.size());
		}
		
		// Check because of rounding errors
		leveledItems = trimItems(leveledItems, maxQuestions);
		return leveledItems;
	}
	
	private void fillRandomly(List<PracticeItem> items, List<PracticeItem> allPracticeItems, int numOfQuestionsToAdd) {
		Set<String> identifiers = items.stream()
				.filter(item -> item.getIdentifier() != null)
				.map(PracticeItem::getIdentifier)
				.collect(Collectors.toSet());
		
		List<PracticeItem> shuffledAllPracticeItems = new ArrayList<>(allPracticeItems);
		Collections.shuffle(shuffledAllPracticeItems);
		for(int i=0; i<allPracticeItems.size() && numOfQuestionsToAdd >= 0; i++) {
			PracticeItem item = allPracticeItems.get(i);
			if(identifiers.contains(item.getIdentifier())) {
				continue;
			}
			
			items.add(item);
			numOfQuestionsToAdd--;
		}
	}
	
	private List<PracticeItem> filterLevel(int level, int daysBeforeRepetition,
			List<PracticeItem> practiceItems, int maxQuestions,
			Map<String,PracticeAssessmentItemGlobalRef> identifierRefs) {
		List<PracticeItem> items = practiceItems.stream()
			.filter(item -> item.getIdentifier() != null)
			.filter(item -> {
				PracticeAssessmentItemGlobalRef ref = identifierRefs.get(item.getIdentifier());
				return (level == 0 && ref == null) || (ref != null && ref.getLevel() == level);
			})
			.collect(Collectors.toList());
		
		Collections.shuffle(items);
		
		// Prefer incorrect answers
		if(items.size() > maxQuestions) {
			items = filterPreferredQuestions(daysBeforeRepetition, items, maxQuestions,  identifierRefs);	
		}
		return items;
	}
	
	/**
	 * Prefer a mix of new questions and incorrect questions.
	 * 
	 * @param practiceItems The list of available questions
	 * @param maxQuestions The max. number of questions to return (can be less)
	 * @param identifierRefs The global references of this identity
	 * @return A list of items
	 */
	private List<PracticeItem> filterPreferredQuestions(int daysBeforeRepetition, List<PracticeItem> practiceItems,
			int maxQuestions, Map<String,PracticeAssessmentItemGlobalRef> identifierRefs) {
		List<PracticeItem> incorrectQuestions = new ArrayList<>();
		List<PracticeItem> incorrectQuestionsToRepeat = new ArrayList<>();
		
		List<PracticeItem> newQuestions = new ArrayList<>();
		
		List<PracticeItem> otherQuestions = new ArrayList<>();
		List<PracticeItem> otherQuestionsToRepeat = new ArrayList<>();
		
		Date currentDate = new Date();

		for(PracticeItem practiceItem:practiceItems) {
			PracticeAssessmentItemGlobalRef ref = identifierRefs.get(practiceItem.getIdentifier());
			if(ref == null) {
				newQuestions.add(practiceItem);
			} else if(ref.getLastAttemptsPassed() != null && !ref.getLastAttemptsPassed().booleanValue()) {
				if(questionToRepeat(daysBeforeRepetition, ref, currentDate)) {
					incorrectQuestionsToRepeat.add(practiceItem);
				} else {
					incorrectQuestions.add(practiceItem);
				}
			} else {
				if(questionToRepeat(daysBeforeRepetition, ref, currentDate)) {
					otherQuestionsToRepeat.add(practiceItem);
				} else {
					otherQuestions.add(practiceItem);
				}
			}	
		}
		
		List<PracticeItem> preferredQuestions = new ArrayList<>(maxQuestions);
		preferredQuestions.addAll(incorrectQuestionsToRepeat);
		preferredQuestions.addAll(newQuestions);
		
		if(preferredQuestions.size() > maxQuestions) {
			preferredQuestions = trimItems(preferredQuestions, maxQuestions);
		} else if(preferredQuestions.size() < maxQuestions) {
			int additionalQuestions = maxQuestions - preferredQuestions.size();
			if(!otherQuestionsToRepeat.isEmpty()) {
				List<PracticeItem> questions = trimItems(otherQuestionsToRepeat, additionalQuestions);
				preferredQuestions.addAll(questions);
			}
			
			additionalQuestions = maxQuestions - preferredQuestions.size();
			if(additionalQuestions > 0 && !incorrectQuestions.isEmpty()) {
				List<PracticeItem> questions = trimItems(incorrectQuestions, additionalQuestions);
				preferredQuestions.addAll(questions);
			}
			
			additionalQuestions = maxQuestions - preferredQuestions.size();
			if(additionalQuestions > 0 && !otherQuestions.isEmpty()) {
				List<PracticeItem> questions = trimItems(otherQuestions, additionalQuestions);
				preferredQuestions.addAll(questions);
			}
		}
		return preferredQuestions;
	}
	
	private List<PracticeItem> trimItems(List<PracticeItem> items, int maxQuestions) {
		if(maxQuestions > 0 && items.size() > maxQuestions) {
			Collections.shuffle(items);
			items = items.subList(0, maxQuestions);
		}
		return items;
	}
	
	private boolean questionToRepeat(int daysBeforeRepetition, PracticeAssessmentItemGlobalRef ref, Date today) {
		if(ref.getLastAttempts() == null) {
			return daysBeforeRepetition == 0;
		}
		long numOfDays = CalendarUtils.numOfDays(today, ref.getLastAttempts());
		return daysBeforeRepetition < numOfDays;
	}
	
	private List<PracticeItem> filterNewQuestionsPlayMode(List<PracticeItem> practiceItems, IdentityRef identity) {
		List<PracticeAssessmentItemGlobalRef> globalRefs = getPracticeAssessmentItemGlobalRefs(practiceItems, identity);
		Set<String> identifierRefs = globalRefs.stream()
				.map(PracticeAssessmentItemGlobalRef::getIdentifier)
				.collect(Collectors.toSet());
		
		practiceItems = practiceItems.stream()
			.filter(item -> item.getIdentifier() == null || !identifierRefs.contains(item.getIdentifier()))
			.collect(Collectors.toList());

		return practiceItems;
	}
	
	private List<PracticeItem> filterIncorrectQuestionsPlayMode(List<PracticeItem> practiceItems, IdentityRef identity) {
		List<PracticeAssessmentItemGlobalRef> globalRefs = getPracticeAssessmentItemGlobalRefs(practiceItems, identity);
		Set<String> notPassedRefs = globalRefs.stream()
				.filter(ref -> ref.getLastAttemptsPassed() != null && !ref.getLastAttemptsPassed().booleanValue())
				.map(PracticeAssessmentItemGlobalRef::getIdentifier)
				.collect(Collectors.toSet());

		practiceItems = practiceItems.stream()
			.filter(item -> item.getIdentifier() != null && notPassedRefs.contains(item.getIdentifier()))
			.collect(Collectors.toList());
		
		return practiceItems;
		
	}
	
	private int countItemsOfRepositoryEntry(RepositoryEntry testEntry) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File unzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		ResolvedAssessmentTest resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false, false);
		return resolvedAssessmentTest.getAssessmentItemRefs().size();
	}
	
	private List<PracticeItem> loadItemsOfRepositoryEntry(RepositoryEntry testEntry, Locale locale,
			SearchPracticeItemParameters searchParams, QTI21MetadataConverter metadataConverter) {
		final String language = locale.getLanguage();
		
		FileResourceManager frm = FileResourceManager.getInstance();
		File unzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		ManifestBuilder manifestBuilder = ManifestBuilder.read(new File(unzippedDirRoot, "imsmanifest.xml"));

		ResolvedAssessmentTest resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false, false);
		List<AssessmentItemRef> assessmentItemRefs = resolvedAssessmentTest.getAssessmentItemRefs();
		
		List<PracticeItem> items = new ArrayList<>(assessmentItemRefs.size());
		List<String> identifiers = new ArrayList<>(assessmentItemRefs.size());
		for(AssessmentItemRef ref:assessmentItemRefs) {
			URI systemId = resolvedAssessmentTest.getSystemIdByItemRefMap().get(ref);
			
			File itemFile = new File(systemId);
			String relativePathToManifest = unzippedDirRoot.toPath().relativize(itemFile.toPath()).toString();
			ResourceType resourceType = manifestBuilder.getResourceTypeByHref(relativePathToManifest);
			ManifestMetadataBuilder metadataBuilder = manifestBuilder.getMetadataBuilder(resourceType, true);
			
			QuestionItem item = new ManifestMetadataItemized(metadataBuilder, language, metadataConverter);
			if(SearchPracticeItemParametersHelper.accept(item, searchParams)) {
				String identifier = item.getIdentifier();
				if(!StringHelper.containsNonWhitespace(identifier)) {
					identifier = testEntry.getKey() + "-" + ref.getIdentifier().toString();
				}
				identifiers.add(identifier);
				
				
				String displayName = item.getTitle();
				if(displayName == null) {
					ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(ref);
					if(resolvedAssessmentItem != null) {
						AssessmentItem assessmentItem = resolvedAssessmentItem.getItemLookup().extractIfSuccessful();
						if(assessmentItem != null) {
							displayName = assessmentItem.getTitle();
						}
					}
				}

				items.add(new PracticeItem(identifier, displayName, ref, item, testEntry));
			}
		}
		
		return items;
	}

	@Override
	public List<AssessmentTestSession> getSeries(IdentityRef identity, RepositoryEntry courseEntry, String subIdent) {
		List<AssessmentTestSession> sessions = assessmentTestSessionDao.getTestSessions(courseEntry, courseEntry, subIdent, identity);
		return sessions.stream()
				.filter(session -> !session.isCancelled() && !session.isExploded()
						&& (session.getFinishTime() != null || session.getTerminationTime() != null))
				.collect(Collectors.toList());
	}
	
	@Override
	public void resetSeries(IdentityRef identity, RepositoryEntry courseEntry, String subIdent) {
		List<AssessmentTestSession> sessions = assessmentTestSessionDao.getTestSessions(courseEntry, courseEntry, subIdent, identity);
		for(AssessmentTestSession session:sessions) {
			qtiService.deleteAssessmentTestSession(session);
		}
	}

	@Override
	public long countCompletedSeries(IdentityRef identity, RepositoryEntry courseEntry, String subIdent) {
		return practiceAssessmentTestSessionDao.countCompletedTestSessions(courseEntry, courseEntry, subIdent, identity);
	}

	@Override
	public List<AssessmentTestSession> getTerminatedSeries(IdentityRef identity, RepositoryEntry courseEntry, String subIdent, Date from, Date to) {
		return assessmentTestSessionDao.getValidTestSessions(identity, courseEntry, subIdent, from, to);
	}

	@Override
	public double getProcentCorrectness(AssessmentTestSession testSession) {
		return assessmentItemSessionDao.getProcentCorrectAtFirstAttempt(testSession);
	}

	@Override
	public List<PracticeAssessmentItemGlobalRef> getPracticeAssessmentItemGlobalRefs(List<PracticeItem> items, IdentityRef identity) {
		List<String> identifiers = items.stream()
				.map(PracticeItem::getIdentifier)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		return practiceAssessmentItemGlobalRefDao.getAssessmentItemGlobalRefByUuids(identity, identifiers);
	}

	@Override
	public PracticeAssessmentItemGlobalRef updateAssessmentItemGlobalRef(Identity identity, String identifier, boolean firstAttempt,
			boolean correct) {
		PracticeAssessmentItemGlobalRef ref = practiceAssessmentItemGlobalRefDao.getAssessmentItemGlobalRefByUuid(identity, identifier);
		if(ref == null) {
			ref = practiceAssessmentItemGlobalRefDao.createAssessmentItemGlobalRefDAO(identity, identifier);
		}
		
		ref.setLastAttempts(new Date());
		ref.setLastAttemptsPassed(Boolean.FALSE);
		if(firstAttempt && correct) {
			ref.setLevel(ref.getLevel() + 1);
			ref.setLastAttemptsPassed(Boolean.TRUE);
		} else if(!correct && ref.getLevel() > 0) {
			ref.setLevel(ref.getLevel() - 1);
		}
		
		ref.setAttempts(ref.getAttempts() + 1);
		if(correct) {
			ref.setCorrectAnswers(ref.getCorrectAnswers() + 1);
		} else {
			ref.setIncorrectAnswers(ref.getIncorrectAnswers() + 1);
		}
		
		return practiceAssessmentItemGlobalRefDao.updateAssessmentItemGlobalRef(ref);
	}

	@Override
	public List<TaxonomyLevel> getTaxonomyWithDescendants(List<Long> keys) {
		List<TaxonomyLevel> exactLevels = taxonomyLevelDao.loadLevelsByKeys(keys);
		Set<TaxonomyLevel> withDescendants = new HashSet<>(exactLevels);
		for(TaxonomyLevel exactLevel:exactLevels) {
			List<TaxonomyLevel> descendants = taxonomyLevelDao.getDescendants(exactLevel, exactLevel.getTaxonomy());
			withDescendants.addAll(descendants);
		}
		return new ArrayList<>(withDescendants);
	}
	
	@Override
	public List<RankedIdentity> getRankList(Identity identity, RepositoryEntry courseEntry, String subIdent, int numOfEntries) {
		List<AssessmentEntry> entries = practiceAssessmentTestSessionDao.loadAssessmentEntries(courseEntry, subIdent);
		
		int index = -1;
		for(int i=0; i<entries.size(); i++) {
			if(identity.equals(entries.get(i).getIdentity())) {
				index = i;
			}
		}
		
		index = index - (numOfEntries / 2);
		if(index < 0) {
			index = 0;
		}
		int stopIndex = index + numOfEntries;
		List<RankedIdentity> rankList = new ArrayList<>();
		for(int i=index; i<stopIndex && i<entries.size(); i++) {
			Identity user = entries.get(i).getIdentity();
			boolean me = user != null && user.equals(identity);
			rankList.add(new RankedIdentity(user, i + 1, me));
		}
		return rankList;
	}
}
