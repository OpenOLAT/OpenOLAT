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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.Assert;

import org.dom4j.Document;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XMLParser;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.QTIResultSet;
import org.olat.ims.qti.export.helper.ItemWithResponseLid;
import org.olat.ims.qti.export.helper.ItemWithResponseStr;
import org.olat.ims.qti.export.helper.QTIItemObject;
import org.olat.ims.qti.statistics.QTIStatisticSearchParams;
import org.olat.ims.qti.statistics.QTIStatisticsManager;
import org.olat.ims.qti.statistics.model.StatisticsItem;
import org.olat.ims.qti.statistics.model.StatisticAssessment;
import org.olat.ims.resources.IMSEntityResolver;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
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
	private static OLog log = Tracing.createLoggerFor(QTIStatisticsManagerLargeTest.class);

	private static boolean isInitialized = false;
	private static Long olatResource ;
	private static String olatResourceDetail;
	private static Long repositoryRef;
	private static List<QTIItemObject> itemObjects;
	private static int numberOfParticipants = 1000;
	private static int numberOfQuestions = 4;
	private static Map<String,Double> averageScorePerQuestion = new HashMap<String,Double>();
	private static List<Float> averageRightAnswersInPercent = new ArrayList<Float>();
	private static Map<String,Long> identToANumOfRightAnswers = new HashMap<>();
	private static List<Long> allDurations = new ArrayList<Long>();
	private static List<Double> scorePerParticipant = new ArrayList<Double>();
	private static List<Double> scorePerParticipantInTenths = new ArrayList<Double>();

	private static int questionNumber = 1;
	private static double maxScore = 7.0d;
	private static float midpointScore = 0.0f;
	private static long midpointDuration = 0;
	private static int numberOfTestFailed = 0;
	private static int numberOfTestPassed = 0;
	private static float durationQ3 = 0l;
	private static float scoreQ1 = 0;
	private static float scoreQ2 = 0;
	private static int wrongAnswersQ2 = 0;
	private static List<String> fibAnswers = new ArrayList<String>();

	@Autowired
	private DB dbInstance;
	@Autowired
	private QTIStatisticsManager qtim;

	@Before
	public void setUp() throws Exception {
		if(isInitialized) return;
		
		RepositoryEntry re = createRepository();
		olatResource = re.getOlatResource().getResourceableId();
		repositoryRef = re.getKey();
		olatResourceDetail = UUID.randomUUID().toString().replace("-", "");

		getItemObjectList();

		long tempDuration = 0;
		double scoreQuestion1 = 0.0d, scoreQuestion2 = 0.0d, scoreQuestion3 = 0.0d, scoreQuestion4 = 0.0d;
		long maxScoreQ1 = 0, maxScoreQ2 = 0, maxScoreQ3 = 0, maxScoreQ4 = 0;
		
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
			tempDuration = (long) (Math.random() * 100000) + 1l;
			midpointDuration += tempDuration;
			midpointScore = midpointScore + setScore;
			scorePerParticipant.add(new Double(setScore));
			assertNotNull(allDurations);
			allDurations.add(tempDuration);

			test.setOlatResource(olatResource);
			test.setOlatResourceDetail(olatResourceDetail);
			test.setRepositoryRef(repositoryRef);
			test.setScore(setScore);
			test.setDuration(tempDuration);
			test.setIdentity(JunitTestHelper.createAndPersistIdentityAsUser("test" + i));
			test.setAssessmentID(111L);
			test.setLastModified(new Date(200, 8, 23, (int) (Math.random() * 1000 % 60), (int) (Math.random() * 1000 % 60), (int) (Math.random() * 1000 % 60)));
			dbInstance.saveObject(test);
			// insert values into result
			for (int j = 0; j < numberOfQuestions; j++) {
				QTIResult testres = new QTIResult();
				testres.setResultSet(test);
				tempDuration = (long)(Math.random() * 10000) + 1;
				testres.setDuration(tempDuration);
				testres.setIp("127.0.0.1");
				testres.setAnswer("asdf");

				if (questionNumber == 1) {
					testres.setItemIdent("QTIEDIT:SCQ:1000007885");
					if (i % 4 == 0) {
						testres.setAnswer("1000007887[[1000007890]]");
					}
					if (i % 4 == 1) {
						testres.setAnswer("1000007887[[1000009418]]");
					}
					if (i % 4 == 2) {
						testres.setAnswer("1000007887[[1000009464]]");
					}
					if (i % 4 == 3) {
						testres.setAnswer("1000007887[[1000007890]]");
					}
					float score = (float)Math.ceil(Math.random());
					scoreQ1 += score;
					testres.setScore(score);
					scoreQuestion1 += score;
					if (score == 1.0f) {
						maxScoreQ1++;
					}
				} else if (questionNumber == 2) {
					testres.setItemIdent("QTIEDIT:MCQ:1000009738");
					float score = (float)Math.ceil(Math.random() * 3);
					scoreQ2 += score;
					testres.setScore(score);
					scoreQuestion2 += score;
					if (score < 3.0f) {
						wrongAnswersQ2++;
					} else {
						maxScoreQ2++;
					}
				} else if (questionNumber == 3) {
					testres.setItemIdent("QTIEDIT:KPRIM:1000010376");
					durationQ3 += tempDuration;
					float score = (float)Math.ceil(Math.random() * 2);
					testres.setScore(score);
					scoreQuestion3 += score;
					if (score == 2.0f) {
						maxScoreQ3++;
					}
				} else if (questionNumber == 4) {
					testres.setItemIdent("QTIEDIT:FIB:1000010792");
					if (i % 4 == 0) {
						testres.setAnswer("Huagagaagaga");
						fibAnswers.add("Huagagaagaga");
					}
					if (i % 4 == 1) {
						testres.setAnswer("Yikes");
						fibAnswers.add("Yikes");
					}
					if (i % 4 == 2 || i % 4 == 3) {
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
				questionNumber++;
				if (questionNumber == 5) {
					questionNumber = 1;
				}
			}
			
			dbInstance.commitAndCloseSession();
		}
		
		dbInstance.commitAndCloseSession();
		
		durationQ3 = (durationQ3 / numberOfParticipants);
		
		scoreQ1 = scoreQ1 / numberOfParticipants;
		scoreQ2 = scoreQ2 / numberOfParticipants;
		midpointScore = midpointScore / numberOfParticipants;
		midpointDuration = midpointDuration / numberOfParticipants;
		averageScorePerQuestion.put("QTIEDIT:SCQ:1000007885", scoreQuestion1 / numberOfParticipants);
		averageScorePerQuestion.put("QTIEDIT:MCQ:1000009738", scoreQuestion2 / numberOfParticipants);
		averageScorePerQuestion.put("QTIEDIT:KPRIM:1000010376", scoreQuestion3 / numberOfParticipants);
		averageScorePerQuestion.put("QTIEDIT:FIB:1000010792", scoreQuestion4 / numberOfParticipants);
		averageRightAnswersInPercent.add((float)maxScoreQ1 / numberOfParticipants);
		averageRightAnswersInPercent.add((float)maxScoreQ2 / numberOfParticipants);
		averageRightAnswersInPercent.add((float)maxScoreQ3 / numberOfParticipants);
		averageRightAnswersInPercent.add((float)maxScoreQ4 / numberOfParticipants);
		identToANumOfRightAnswers.put("QTIEDIT:SCQ:1000007885", maxScoreQ1);
		identToANumOfRightAnswers.put("QTIEDIT:MCQ:1000009738", maxScoreQ2);
		identToANumOfRightAnswers.put("QTIEDIT:KPRIM:1000010376", maxScoreQ3);
		identToANumOfRightAnswers.put("QTIEDIT:FIB:1000010792", maxScoreQ4);

		// sort allDurations List asc
		Collections.sort(allDurations);
		Collections.sort(scorePerParticipant);

		double counter = 0.0d;
		double oneTenth = maxScore / 10;
		double limit = oneTenth;
		double score;
		for (int i = 0; i < scorePerParticipant.size(); i++) {
			score = scorePerParticipant.get(i).doubleValue();
			if (score <= limit) counter++;
			else {
				scorePerParticipantInTenths.add(counter);
				counter = 0;
				limit += oneTenth;
				i --;
			}
		}
		scorePerParticipantInTenths.add(counter);
		isInitialized = true;
	}

	@Test
	public void testStatistics() {
		long start = System.currentTimeMillis();

		QTIStatisticSearchParams searchParams = new QTIStatisticSearchParams(olatResource, olatResourceDetail);
		StatisticAssessment stats = qtim.getAssessmentStatistics(searchParams);
		log.info("Statistics of resource takes (ms): " + (System.currentTimeMillis() - start));
		
		Assert.assertNotNull(stats);
		Assert.assertEquals(midpointScore, stats.getAverage(), 0.01);
		Assert.assertEquals(numberOfParticipants, stats.getNumOfParticipants());
		Assert.assertEquals(numberOfTestFailed, stats.getNumOfFailed());
		Assert.assertEquals(numberOfTestPassed, stats.getNumOfPassed());

		double range = (scorePerParticipant.get(scorePerParticipant.size() - 1).doubleValue() - scorePerParticipant.get(0).doubleValue());
		Assert.assertEquals(range, stats.getRange(), 0.00001);
		Assert.assertTrue(stats.getStandardDeviation() > 0);
		Assert.assertEquals(midpointDuration, stats.getAverageDuration());
	}
	/*
	@Test
	public void testAverageScorePerItem() {
		List<StatisticItem> calculatedList = qtim.getStatisticPerItem(olatResource, olatResourceDetail);
		for (StatisticItem entry : averageScorePerQuestion) {
			Double average = calculatedList.get(entry.getItemIdent());
			assertEquals(entry.getValue().doubleValue(), entry.getAverageScore(), 0.01);
		}
	}*/
	
	/*
	@Test
	public void testAverageRightAnswersPerQuestionInPercent() {
		Assert.assertTrue(averageRightAnswersInPercent.size() > 0);
		
		Map<String,Long> calculatedList_v2 = qtim.getNumOfRightAnswersForAllQuestions(itemObjects, olatResource, olatResourceDetail);
		for (Map.Entry<String, Long> result:calculatedList_v2.entrySet()) {
			String itemIdent = result.getKey();
			Long numOfRightResponses = result.getValue();
			assertEquals(identToANumOfRightAnswers.get(itemIdent), numOfRightResponses);
		}
	}*/
	
	@Test
	public void testItemStatistics_singleChoice_0() {
		QTIItemObject itemObject = itemObjects.get(0);
		double maxValue = Double.parseDouble(itemObject.getItemMaxValue());

		QTIStatisticSearchParams searchParams = new QTIStatisticSearchParams(olatResource, olatResourceDetail);
		StatisticsItem stats  = qtim.getItemStatistics(itemObject.getItemIdent(), maxValue, searchParams);

		assertEquals(scoreQ1, stats.getAverageScore(), 0.1);
	}
	
	@Test
	public void testItemStatistics_multipleChoice_1() {
		QTIItemObject itemObject = itemObjects.get(1);
		double maxValue = Double.parseDouble(itemObject.getItemMaxValue());

		QTIStatisticSearchParams searchParams = new QTIStatisticSearchParams(olatResource, olatResourceDetail);
		StatisticsItem stats  = qtim.getItemStatistics(itemObject.getItemIdent(), maxValue, searchParams);

		double difficulty = scoreQ2 / maxValue;
		assertEquals(difficulty, stats.getDifficulty(), 0.1);
		assertEquals(scoreQ2, stats.getAverageScore(), 0.1);
		assertEquals(wrongAnswersQ2, stats.getNumOfIncorrectAnswers());
		assertEquals(numberOfParticipants - wrongAnswersQ2, stats.getNumOfCorrectAnswers());
	}
	
	@Test
	public void testItemStatistics_kprim_2() {
		QTIItemObject itemObject = itemObjects.get(2);
		double maxValue = Double.parseDouble(itemObject.getItemMaxValue());

		QTIStatisticSearchParams searchParams = new QTIStatisticSearchParams(olatResource, olatResourceDetail);
		StatisticsItem stats  = qtim.getItemStatistics(itemObject.getItemIdent(), maxValue, searchParams);
		float durationQ3InSec = durationQ3 / 1000;
		assertEquals(durationQ3InSec, stats.getAverageDuration(), 1.0f);
	}
/*
	@Test
	public void testAnswerOptions() {
		int questionNbr = 4;
		List<String> calculatedList = QTIHelper.getAnswerOptions(itemObjects.get(questionNbr-1));
		for (int i = 0; i < calculatedList.size(); i++) {
			assertTrue(itemObjects.get(questionNbr - 1).getResponseLabelMaterials().get(i).equals(calculatedList.get(i)));
		}
	}*/

	@Test
	public void testAnswerTexts() {
		QTIStatisticSearchParams searchParams = new QTIStatisticSearchParams(olatResource, olatResourceDetail);
		List<String> calculatedList  = qtim.getAnswers("QTIEDIT:FIB:1000010792", searchParams);
		Collections.sort(calculatedList);
		Collections.sort(fibAnswers);
		
		for (int i = 0; i < calculatedList.size(); i++) {
			assertEquals(fibAnswers.get(i), calculatedList.get(i));
		}
	}

	private void getItemObjectList() {
		InputStream in = QTIStatisticsManagerLargeTest.class.getResourceAsStream("qti.xml");
		XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
		Document doc = xmlParser.parse(in, false);
		Element root = doc.getRootElement();
		List items = root.selectNodes("//item");
		itemObjects = new ArrayList<QTIItemObject>();
		for (Iterator iter = items.iterator(); iter.hasNext();) {
			Element el_item = (Element) iter.next();
			if (el_item.selectNodes(".//response_lid").size() > 0) {
				itemObjects.add(new ItemWithResponseLid(el_item));
			} else if (el_item.selectNodes(".//response_str").size() > 0) {
				itemObjects.add(new ItemWithResponseStr(el_item));
			}
		}
	}
	
	private RepositoryEntry createRepository() {
		OLATResourceManager rm = OLATResourceManager.getInstance();
		// create course and persist as OLATResourceImpl
		
		OLATResource r =  rm.createOLATResourceInstance("QTIStatisticsTest");
		DBFactory.getInstance().saveObject(r);
		DBFactory.getInstance().intermediateCommit();

		RepositoryEntry d = RepositoryManager.getInstance().createRepositoryEntryInstance("Stéphane Rossé", "QTIStatisticsTest", "Repo entry");
		d.setOlatResource(r);
		d.setDisplayname("QTIStatisticsTest");
		DBFactory.getInstance().saveObject(d);
		DBFactory.getInstance().intermediateCommit();
		return d;
	}
}
