/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.ims.qti.statistics.manager;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XMLParser;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.QTIResultSet;
import org.olat.ims.qti.export.helper.ItemWithResponseLid;
import org.olat.ims.qti.export.helper.ItemWithResponseStr;
import org.olat.ims.qti.export.helper.QTIItemObject;
import org.olat.ims.qti.statistics.QTIStatisticSearchParams;
import org.olat.ims.qti.statistics.QTIStatisticsManager;
import org.olat.ims.qti.statistics.model.StatisticAssessment;
import org.olat.ims.qti.statistics.model.StatisticsItem;
import org.olat.ims.resources.IMSEntityResolver;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Test QTIStatisticsManager and QTIItemStatisticsManager 
 * 
 * <P>
 * Initial Date: 02.08.2011 <br>
 * 
 * @author mkuendig, srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class QTIStatisticsManagerLargeTest extends OlatTestCase {
	private static final Logger log = Tracing.createLoggerFor(QTIStatisticsManagerLargeTest.class);

	private static boolean isInitialized = false;
	private static Long olatResource ;
	private static String olatResourceDetail;
	private static Long repositoryRef;
	private static List<QTIItemObject> itemObjects;
	private static int numberOfParticipants = 1000;
	private static int numberOfQuestions = 4;
	private static Map<String,Double> averageScorePerQuestion = new HashMap<>();
	private static List<Float> averageRightAnswersInPercent = new ArrayList<>();
	private static Map<String,Float> identToANumOfRightAnswers = new HashMap<>();
	private static List<Long> allDurations = new ArrayList<>();
	private static List<Float> scorePerParticipant = new ArrayList<>();

	private static double maxScore = 7.0d;
	private static float averageScore = 0.0f;
	private static long averageDuration = 0;
	private static int numberOfTestFailed = 0;
	private static int numberOfTestPassed = 0;
	private static float durationQ3 = 0.0f;
	private static float scoreQ1 = 0.0f;
	private static float scoreQ2 = 0.0f;
	private static int rightAnswersQ2 = 0;
	private static int wrongAnswersQ2 = 0;
	private static List<String> fibAnswers = new ArrayList<>();

	@Autowired
	private DB dbInstance;
	@Autowired
	private QTIStatisticsManager qtim;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;

	@Before
	public void setUp() throws Exception {
		if(isInitialized) return;
		
		RepositoryEntry re = createRepository();
		olatResource = re.getOlatResource().getResourceableId();
		repositoryRef = re.getKey();
		olatResourceDetail = UUID.randomUUID().toString().replace("-", "");

		getItemObjectList();

		double scoreQuestion1 = 0.0d, scoreQuestion2 = 0.0d, scoreQuestion3 = 0.0d, scoreQuestion4 = 0.0d;
		float maxScoreQ1 = 0, maxScoreQ2 = 0, maxScoreQ3 = 0, maxScoreQ4 = 0;
		
		// insert value into resultset
		for (int i = 0; i < numberOfParticipants; i++) {
			QTIResultSet test = new QTIResultSet();
			float setScore = (float) Math.ceil(Math.random() * maxScore);
			if (setScore >= 4.0d) {
				numberOfTestPassed++;
				test.setIsPassed(true);
			} else {
				numberOfTestFailed++;
				test.setIsPassed(false);
			}
			long tempTestDuration = Math.round((Math.random() * 100000) + 1.0);
			averageDuration += tempTestDuration;
			averageScore += setScore;
			scorePerParticipant.add(setScore);
			assertNotNull(allDurations);
			allDurations.add(tempTestDuration);

			test.setOlatResource(olatResource);
			test.setOlatResourceDetail(olatResourceDetail);
			test.setRepositoryRef(repositoryRef);
			test.setScore(setScore);
			test.setDuration(tempTestDuration);
			test.setIdentity(JunitTestHelper.createAndPersistIdentityAsUser("test" + i));
			test.setAssessmentID(111L);
			Calendar cal = Calendar.getInstance();
			cal.set(2013, 8, 23, (int) (Math.random() * 1000 % 60), (int) (Math.random() * 1000 % 60), (int) (Math.random() * 1000 % 60));
			test.setLastModified(cal.getTime());
			dbInstance.saveObject(test);
			// insert values into result
			for (int j = 0; j < numberOfQuestions; j++) {
				QTIResult testres = new QTIResult();
				testres.setResultSet(test);
				long tempDuration = Math.round((Math.random() * 10000) + 1.0);
				testres.setDuration(tempDuration);
				testres.setIp("127.0.0.1");
				testres.setAnswer("asdf");

				if (j == 0) {
					testres.setItemIdent("QTIEDIT:SCQ:1000007885");
					if (i % 4 == 0) {
						testres.setAnswer("1000007887[[1000007890]]");
					} else if (i % 4 == 1) {
						testres.setAnswer("1000007887[[1000009418]]");
					} else if (i % 4 == 2) {
						testres.setAnswer("1000007887[[1000009464]]");
					} else if (i % 4 == 3) {
						testres.setAnswer("1000007887[[1000007890]]");
					}
					float score = (float)Math.ceil(Math.random());
					scoreQ1 += score;
					testres.setScore(score);
					scoreQuestion1 += score;
					if (score == 1.0f) {
						maxScoreQ1++;
					}
				} else if (j == 1) {
					testres.setItemIdent("QTIEDIT:MCQ:1000009738");
					float score = (float)Math.ceil(Math.random() * 3);
					scoreQ2 += score;
					testres.setScore(score);
					scoreQuestion2 += score;
					if (score < 3.0f) {
						wrongAnswersQ2++;
					} else {
						rightAnswersQ2++;
						maxScoreQ2++;
					}
				} else if (j == 2) {
					testres.setItemIdent("QTIEDIT:KPRIM:1000010376");
					durationQ3 += tempDuration;
					float score = (float)Math.ceil(Math.random() * 2);
					testres.setScore(score);
					scoreQuestion3 += score;
					if (score == 2.0f) {
						maxScoreQ3++;
					}
				} else if (j == 3) {
					testres.setItemIdent("QTIEDIT:FIB:1000010792");
					if (i % 4 == 0) {
						testres.setAnswer("Huagagaagaga");
						fibAnswers.add("Huagagaagaga");
					} else if (i % 4 == 1) {
						testres.setAnswer("Yikes");
						fibAnswers.add("Yikes");
					} else if (i % 4 == 2 || i % 4 == 3) {
						testres.setAnswer("Muragarara");
						fibAnswers.add("Muragarara");
					}
					float score = (float)Math.ceil(Math.random());
					testres.setScore(score);
					scoreQuestion4 += score;
					if (score == 1.0f) {
						maxScoreQ4++;
					}
				}
				testres.setLastModified(new Date());
				testres.setTstamp(new Date());
				dbInstance.saveObject(testres);
			}
			dbInstance.commitAndCloseSession();
		}
		dbInstance.commitAndCloseSession();
		
		durationQ3 = (durationQ3 / numberOfParticipants);
		
		scoreQ1 = scoreQ1 / numberOfParticipants;
		scoreQ2 = scoreQ2 / numberOfParticipants;
		averageScore = averageScore / numberOfParticipants;
		averageDuration = averageDuration / numberOfParticipants;
		averageScorePerQuestion.put("QTIEDIT:SCQ:1000007885", scoreQuestion1 / numberOfParticipants);
		averageScorePerQuestion.put("QTIEDIT:MCQ:1000009738", scoreQuestion2 / numberOfParticipants);
		averageScorePerQuestion.put("QTIEDIT:KPRIM:1000010376", scoreQuestion3 / numberOfParticipants);
		averageScorePerQuestion.put("QTIEDIT:FIB:1000010792", scoreQuestion4 / numberOfParticipants);
		averageRightAnswersInPercent.add(maxScoreQ1 / numberOfParticipants);
		averageRightAnswersInPercent.add(maxScoreQ2 / numberOfParticipants);
		averageRightAnswersInPercent.add(maxScoreQ3 / numberOfParticipants);
		averageRightAnswersInPercent.add(maxScoreQ4 / numberOfParticipants);
		identToANumOfRightAnswers.put("QTIEDIT:SCQ:1000007885", maxScoreQ1);
		identToANumOfRightAnswers.put("QTIEDIT:MCQ:1000009738", maxScoreQ2);
		identToANumOfRightAnswers.put("QTIEDIT:KPRIM:1000010376", maxScoreQ3);
		identToANumOfRightAnswers.put("QTIEDIT:FIB:1000010792", maxScoreQ4);

		// sort allDurations List asc
		Collections.sort(allDurations);
		Collections.sort(scorePerParticipant);

		isInitialized = true;
	}

	@Test
	public void testStatistics() {
		long start = System.currentTimeMillis();

		QTIStatisticSearchParams searchParams = new QTIStatisticSearchParams(olatResource, olatResourceDetail);
		StatisticAssessment stats = qtim.getAssessmentStatistics(searchParams);
		log.info("Statistics of resource takes (ms): " + (System.currentTimeMillis() - start));
		
		Assert.assertNotNull(stats);
		Assert.assertEquals(averageScore, stats.getAverage(), 0.01);
		Assert.assertEquals(numberOfParticipants, stats.getNumOfParticipants());
		Assert.assertEquals(numberOfTestFailed, stats.getNumOfFailed());
		Assert.assertEquals(numberOfTestPassed, stats.getNumOfPassed());

		double maxScore = scorePerParticipant.get(scorePerParticipant.size() - 1).doubleValue();
		double minScore = scorePerParticipant.get(0).doubleValue();
		double range = maxScore - minScore;
		Assert.assertEquals(maxScore, stats.getMaxScore(), 0.1);
		Assert.assertEquals(minScore, stats.getMinScore(), 0.1);
		Assert.assertEquals(range, stats.getRange(), 0.1);

		Assert.assertEquals(averageDuration, stats.getAverageDuration(), 2);
		Assert.assertTrue(stats.getStandardDeviation() > 0);
		Assert.assertTrue(stats.getMedian() > 0);
		Assert.assertNotNull(stats.getDurations());
		Assert.assertNotNull(stats.getScores());
		Assert.assertNotNull(stats.getMode());
		Assert.assertFalse(stats.getMode().isEmpty());
	}
	
	@Test
	public void testItemStatistics_singleChoice_0() {
		QTIItemObject itemObject = itemObjects.get(0);
		double maxValue = Double.parseDouble(itemObject.getItemMaxValue());

		QTIStatisticSearchParams searchParams = new QTIStatisticSearchParams(olatResource, olatResourceDetail);
		StatisticsItem stats  = qtim.getItemStatistics(itemObject.getItemIdent(), maxValue, searchParams);
		Assert.assertEquals(scoreQ1, stats.getAverageScore(), 0.1);
	}
	
	@Test
	public void testItemStatistics_multipleChoice_1() {
		QTIItemObject itemObject = itemObjects.get(1);
		double maxValue = Double.parseDouble(itemObject.getItemMaxValue());

		QTIStatisticSearchParams searchParams = new QTIStatisticSearchParams(olatResource, olatResourceDetail);
		StatisticsItem stats  = qtim.getItemStatistics(itemObject.getItemIdent(), maxValue, searchParams);

		double difficulty = rightAnswersQ2 / (double)numberOfParticipants;
		Assert.assertEquals(difficulty, stats.getDifficulty(), 0.1);
		Assert.assertEquals(scoreQ2, stats.getAverageScore(), 0.1);
		Assert.assertEquals(wrongAnswersQ2, stats.getNumOfIncorrectAnswers());
		Assert.assertEquals(numberOfParticipants - wrongAnswersQ2, stats.getNumOfCorrectAnswers());
	}
	
	@Test
	public void testItemStatistics_kprim_2() {
		QTIItemObject itemObject = itemObjects.get(2);
		double maxValue = Double.parseDouble(itemObject.getItemMaxValue());

		QTIStatisticSearchParams searchParams = new QTIStatisticSearchParams(olatResource, olatResourceDetail);
		StatisticsItem stats  = qtim.getItemStatistics(itemObject.getItemIdent(), maxValue, searchParams);
		float durationQ3InSec = durationQ3;
		Assert.assertEquals(durationQ3InSec, stats.getAverageDuration(), 1.0f);
	}

	@Test
	public void testAnswerTexts() {
		QTIStatisticSearchParams searchParams = new QTIStatisticSearchParams(olatResource, olatResourceDetail);
		List<String> answers  = qtim.getAnswers("QTIEDIT:FIB:1000010792", searchParams);
		Assert.assertTrue(answers.containsAll(fibAnswers));
		Assert.assertTrue(fibAnswers.containsAll(answers));
	}

	@SuppressWarnings("rawtypes")
	private void getItemObjectList() throws IOException {
		try(InputStream in = QTIStatisticsManagerLargeTest.class.getResourceAsStream("qti.xml")) {
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			Document doc = xmlParser.parse(in, false);
			Element root = doc.getRootElement();
			List items = root.selectNodes("//item");
			itemObjects = new ArrayList<>();
			for (Iterator iter = items.iterator(); iter.hasNext();) {
				Element el_item = (Element) iter.next();
				if (el_item.selectNodes(".//response_lid").size() > 0) {
					itemObjects.add(new ItemWithResponseLid(el_item));
				} else if (el_item.selectNodes(".//response_str").size() > 0) {
					itemObjects.add(new ItemWithResponseStr(el_item));
				}
			}
		} catch(IOException e) {
			log.error("", e);
			throw e;
		}
	}
	
	private RepositoryEntry createRepository() {
		OLATResourceManager rm = OLATResourceManager.getInstance();
		// create course and persist as OLATResourceImpl
		
		OLATResource r =  rm.createOLATResourceInstance("QTIStatisticsTest");
		dbInstance.saveObject(r);
		dbInstance.intermediateCommit();

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry d = repositoryService.create(null, "Kanu Unchou", "QTIStatisticsTest", "QTIStatisticsTest", "Repo entry",
				r, RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.saveObject(d);
		dbInstance.intermediateCommit();
		return d;
	}
}
