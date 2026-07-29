/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.ims.qti21.model.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.xml.interactions.MatchAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;
import uk.ac.ed.ph.jqtiplus.value.DirectedPairValue;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.Value;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * Tests of the score calculation of the matchInteraction with the negative
 * point system (partial scoring). All matrices in this test have 4 rows and
 * 3 columns, one correct answer per row and a max. score of 4.0.
 * <p>
 * With single choice, only one answer per row can be selected, therefore at
 * most 4 wrong answers are possible. With multiple choice, all 12 - 4 = 8
 * associations which are not correct can be selected.
 *
 * Initial date: 29 Jul 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class MatchAssessmentItemBuilderTest {

	private static final Logger log = Tracing.createLoggerFor(MatchAssessmentItemBuilderTest.class);
	
	
	// -- ScoreEvaluation: allCorrectAnswers
	
	/**
	 * All 4 rows answered correctly: the max. score is reached.
	 */
	@Test
	public void singleChoiceAllCorrectAnswersAllCorrect() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				true, true, 4, 3, ScoreEvaluation.allCorrectAnswers, 0.0d, 1.0d);
		File itemFile = serialize(itemBuilder);

		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 0), pair(itemBuilder, 1, 1),
				pair(itemBuilder, 2, 2), pair(itemBuilder, 3, 0));
		Assert.assertEquals(new FloatValue(1.0d), score);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}
	
	/**
	 * All 4 rows answered correctly: the max. score is reached.
	 */
	@Test
	public void singleChoiceAllCorrectAnswersNotAllCorrect() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				true, true, 4, 3, ScoreEvaluation.allCorrectAnswers, 0.0d, 1.0d);
		File itemFile = serialize(itemBuilder);

		// 4 * 4.0 / 4 - 0 * 4.0 / 4 = 4.0
		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 0), pair(itemBuilder, 1, 1),
				pair(itemBuilder, 2, 2), pair(itemBuilder, 3, 2));
		Assert.assertEquals(new FloatValue(0.0d), score);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}
	

	// -- ScoreEvaluation: perAnswer
	
	/**
	 * All 4 rows answered correctly: the max. score is reached.
	 */
	@Test
	public void singleChoiceScorePerAnswersAllCorrect() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				true, true, 4, 3, ScoreEvaluation.perAnswer, 0.0d, 4.0d);
		File itemFile = serialize(itemBuilder);

		// 4 * 4.0 / 4 - 0 * 4.0 / 4 = 4.0
		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 0), pair(itemBuilder, 1, 1),
				pair(itemBuilder, 2, 2), pair(itemBuilder, 3, 0));
		Assert.assertEquals(new FloatValue(4.0d), score);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}
	
	/**
	 * All 4 rows answered correctly: the max. score is reached.
	 */
	@Test
	public void singleChoiceScorePerAnswersNotAllCorrect() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				true, true, 4, 3, ScoreEvaluation.perAnswer, 0.0d, 4.0d);
		File itemFile = serialize(itemBuilder);

		// 4 * 4.0 / 4 - 0 * 4.0 / 4 = 4.0
		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 0), pair(itemBuilder, 1, 1),
				pair(itemBuilder, 2, 2), pair(itemBuilder, 3, 2));
		Assert.assertEquals(new FloatValue(3.0d), score);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}

	// -- ScoreEvaluation: negativePointSystem
	
	/**
	 * All 4 rows answered correctly: the max. score is reached.
	 */
	@Test
	public void singleChoiceNegativePointSystemAllCorrect() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				false, true, 4, 3, ScoreEvaluation.negativePointSystem, 0.0d, 4.0d);
		File itemFile = serialize(itemBuilder);

		// 4 * 4.0 / 4 - 0 * 4.0 / 4 = 4.0
		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 0), pair(itemBuilder, 1, 1),
				pair(itemBuilder, 2, 2), pair(itemBuilder, 3, 0));
		Assert.assertEquals(new FloatValue(4.0d), score);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}

	/**
	 * 3 rows correct, 1 row wrong. The wrong answers are weighted with the number
	 * of rows: 3 * 4.0 / 4 - 1 * 4.0 / 4 = 2.0
	 */
	@Test
	public void singleChoiceNegativePointSystemThreeCorrectOneWrong() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				false, true, 4, 3, ScoreEvaluation.negativePointSystem, 0.0d, 4.0d);
		File itemFile = serialize(itemBuilder);

		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 0), pair(itemBuilder, 1, 1),
				pair(itemBuilder, 2, 2), pair(itemBuilder, 3, 1));
		Assert.assertEquals(new FloatValue(2.0d), score);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}

	/**
	 * 2 rows correct, 2 rows wrong: 2 * 4.0 / 4 - 2 * 4.0 / 4 = 0.0
	 * <p>
	 * Before the fix, the wrong answers were weighted with rows * columns - correct
	 * answers = 8, which returned 2 * 4.0 / 4 - 2 * 4.0 / 8 = 1.0 (25% of the max.
	 * score) although only 4 wrong answers can be selected at all.
	 */
	@Test
	public void singleChoiceNegativePointSystemTwoCorrectTwoWrong() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				false, true, 4, 3, ScoreEvaluation.negativePointSystem, 0.0d, 4.0d);
		File itemFile = serialize(itemBuilder);

		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 0), pair(itemBuilder, 1, 1),
				pair(itemBuilder, 2, 0), pair(itemBuilder, 3, 1));
		Assert.assertEquals(new FloatValue(0.0d), score);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}

	/**
	 * 2 rows correct, 2 rows not answered: 2 * 4.0 / 4 - 0 * 4.0 / 4 = 2.0
	 */
	@Test
	public void singleChoiceNegativePointSystemTwoCorrectTwoUnanswered() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				false, true, 4, 3, ScoreEvaluation.negativePointSystem, 0.0d, 4.0d);
		File itemFile = serialize(itemBuilder);

		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 0), pair(itemBuilder, 1, 1));
		Assert.assertEquals(new FloatValue(2.0d), score);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}

	/**
	 * All 4 rows wrong: 0 * 4.0 / 4 - 4 * 4.0 / 4 = -4.0, limited by the min. score 0.0
	 */
	@Test
	public void singleChoiceNegativePointSystemAllWrong() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				false, true, 4, 3, ScoreEvaluation.negativePointSystem, 0.0d, 4.0d);
		File itemFile = serialize(itemBuilder);

		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 1), pair(itemBuilder, 1, 0),
				pair(itemBuilder, 2, 0), pair(itemBuilder, 3, 1));
		Assert.assertEquals(new FloatValue(0.0d), score);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}

	/**
	 * Nothing answered at all: no correct and no wrong answer, the score stays 0.0
	 */
	@Test
	public void singleChoiceNegativePointSystemNotAnswered() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				false, true, 4, 3, ScoreEvaluation.negativePointSystem, 0.0d, 4.0d);
		File itemFile = serialize(itemBuilder);

		ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, new HashMap<>());
		Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
		Assert.assertEquals(new FloatValue(0.0d), score);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}

	/**
	 * Drag and drop uses the same builder as the matrix. Single choice with
	 * 2 correct and 2 wrong answers: 2 * 4.0 / 4 - 2 * 4.0 / 4 = 0.0
	 */
	@Test
	public void dragAndDropSingleChoiceNegativePointSystemTwoCorrectTwoWrong() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_DRAG_AND_DROP,
				false, true, 4, 3, ScoreEvaluation.negativePointSystem, 0.0d, 4.0d);
		File itemFile = serialize(itemBuilder);

		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 0), pair(itemBuilder, 1, 1),
				pair(itemBuilder, 2, 0), pair(itemBuilder, 3, 1));
		Assert.assertEquals(new FloatValue(0.0d), score);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}

	/**
	 * Multiple choice is not changed by the fix. All 4 correct answers selected
	 * and nothing else: 4 * 4.0 / 4 - 0 * 4.0 / 8 = 4.0
	 */
	@Test
	public void multipleChoiceNegativePointSystemAllCorrect() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				true, true, 4, 3, ScoreEvaluation.negativePointSystem, 0.0d, 4.0d);
		File itemFile = serialize(itemBuilder);

		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 0), pair(itemBuilder, 1, 1),
				pair(itemBuilder, 2, 2), pair(itemBuilder, 3, 0));
		Assert.assertEquals(new FloatValue(4.0d), score);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}

	/**
	 * Multiple choice is not changed by the fix. All 4 correct answers plus one
	 * wrong one, the wrong answers are weighted with 12 - 4 = 8:
	 * 4 * 4.0 / 4 - 1 * 4.0 / 8 = 3.5
	 */
	@Test
	public void multipleChoiceNegativePointSystemAllCorrectAndOneWrong() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				true, true, 4, 3, ScoreEvaluation.negativePointSystem, 0.0d, 4.0d);
		File itemFile = serialize(itemBuilder);

		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 0), pair(itemBuilder, 1, 1),
				pair(itemBuilder, 2, 2), pair(itemBuilder, 3, 0), pair(itemBuilder, 0, 1));
		Assert.assertEquals(new FloatValue(3.5d), score);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}

	/**
	 * Multiple choice is not changed by the fix. 2 correct and 2 wrong answers:
	 * 2 * 4.0 / 4 - 2 * 4.0 / 8 = 1.0
	 */
	@Test
	public void multipleChoiceNegativePointSystemTwoCorrectTwoWrong() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				true, true, 4, 3, ScoreEvaluation.negativePointSystem, 0.0d, 4.0d);
		File itemFile = serialize(itemBuilder);

		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 0), pair(itemBuilder, 1, 1),
				pair(itemBuilder, 2, 0), pair(itemBuilder, 3, 1));
		Assert.assertEquals(new FloatValue(1.0d), score);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}
	
	/**
	 * Multiple choice is not changed by the fix. 2 correct and 1 wrong answer:
	 * 2 * 3.0 / 3 - 2 * 3.0 / 6 = 1.0
	 */
	@Test
	public void singleChoiceNegativePointSystemTwoCorrectOneWrong() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				false, true, 3, 3, ScoreEvaluation.negativePointSystem, 0.0d, 3.0d);
		File itemFile = serialize(itemBuilder);

		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 0),
				pair(itemBuilder, 1, 1),
				pair(itemBuilder, 2, 1));
		Assert.assertEquals(new FloatValue(1.0d), score);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}

	/**
	 * Safeguard: single choice without any correct answer. The positive part of the
	 * formula would divide by zero. The wrong answers are subtracted from 0.0:
	 * 0.0 - 2 * 4.0 / 4 = -2.0 (the min. score is set to -4.0 to see the value)
	 */
	@Test
	public void singleChoiceNegativePointSystemWithoutCorrectAnswers() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				false, false, 4, 3, ScoreEvaluation.negativePointSystem, -4.0d, 4.0d);
		File itemFile = serialize(itemBuilder);

		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 0), pair(itemBuilder, 1, 1));
		Assert.assertTrue(score instanceof FloatValue);
		Assert.assertEquals(-2.0d, ((FloatValue)score).doubleValue(), 0.0001d);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}

	/**
	 * Safeguard: multiple choice without any correct answer. The wrong answers are
	 * weighted with 12: 0.0 - 2 * 4.0 / 12 = -0.6666
	 */
	@Test
	public void multipleChoiceNegativePointSystemWithoutCorrectAnswers() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				true, false, 4, 3, ScoreEvaluation.negativePointSystem, -4.0d, 4.0d);
		File itemFile = serialize(itemBuilder);

		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 0), pair(itemBuilder, 1, 1));
		Assert.assertTrue(score instanceof FloatValue);
		Assert.assertEquals(-0.6666d, ((FloatValue)score).doubleValue(), 0.0001d);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}

	/**
	 * The generated assessmentItem must be read back as a single choice matrix
	 * with the negative point system.
	 */
	@Test
	public void extractSingleChoiceNegativePointSystem() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				false, true, 4, 3, ScoreEvaluation.negativePointSystem, 0.0d, 4.0d);
		File itemFile = serialize(itemBuilder);

		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		AssessmentItem assessmentItem = loadAssessmentItem(itemFile);
		MatchAssessmentItemBuilder reloadedBuilder = new MatchAssessmentItemBuilder(assessmentItem, qtiSerializer);

		Assert.assertEquals(ScoreEvaluation.negativePointSystem, reloadedBuilder.getScoreEvaluationMode());
		Assert.assertFalse(reloadedBuilder.isMultipleChoice());
		Assert.assertEquals(4, reloadedBuilder.getSourceChoices().size());
		Assert.assertEquals(3, reloadedBuilder.getTargetChoices().size());
		Assert.assertEquals(4.0, reloadedBuilder.getMaxScoreBuilder().getScore().doubleValue(), 0.0001d);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}
	
	/**
	 * The degenerated assessmentItem without correct nor incorrect answers must return zero as score.
	 */
	@Test
	public void extractMultipleChoiceDegenerated() throws IOException {
		MatchAssessmentItemBuilder itemBuilder = createMatrix(QTI21Constants.CSS_MATCH_MATRIX,
				false, true, 0, 0, ScoreEvaluation.negativePointSystem, 0.0d, 4.0d);
		File itemFile = serialize(itemBuilder);

		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		AssessmentItem assessmentItem = loadAssessmentItem(itemFile);
		MatchAssessmentItemBuilder reloadedBuilder = new MatchAssessmentItemBuilder(assessmentItem, qtiSerializer);

		Assert.assertEquals(ScoreEvaluation.negativePointSystem, reloadedBuilder.getScoreEvaluationMode());
		Assert.assertFalse(reloadedBuilder.isMultipleChoice());
		Assert.assertEquals(0, reloadedBuilder.getSourceChoices().size());
		Assert.assertEquals(0, reloadedBuilder.getTargetChoices().size());
		Assert.assertEquals(4.0, reloadedBuilder.getMaxScoreBuilder().getScore().doubleValue(), 0.0001d);
		
		Value score = run(itemFile, itemBuilder);
		Assert.assertTrue(score instanceof FloatValue);
		Assert.assertEquals(0.0d, ((FloatValue)score).doubleValue(), 0.0001d);

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}
	
	/**
	 * Regression test for issue https://track.frentix.com/issue/OO-9548
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void extractMultipleChoice12Options8CorrectAllCorrectAnswers() throws IOException, URISyntaxException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		
		URL itemUrl = AssessmentItemBuilderTest.class.getResource("resources/openolat/match-12options-8correct.xml");
		File itemFile = new File(itemUrl.toURI());
		AssessmentItem assessmentItem = loadAssessmentItem(itemFile);
		MatchAssessmentItemBuilder itemBuilder = new MatchAssessmentItemBuilder(assessmentItem, qtiSerializer);

		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 0), pair(itemBuilder, 0, 1),
				pair(itemBuilder, 1, 2),
				pair(itemBuilder, 2, 0), pair(itemBuilder, 2, 2),
				pair(itemBuilder, 3, 0), pair(itemBuilder, 3, 1), pair(itemBuilder, 3, 2));
		Assert.assertTrue(score instanceof FloatValue);
		Assert.assertEquals(12.0d, ((FloatValue)score).doubleValue(), 0.0001d);
	}
	
	/**
	 * Regression test for issue https://track.frentix.com/issue/OO-9548
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void extractMultipleChoice12Options8Correct2IncorrectAnswers() throws IOException, URISyntaxException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		
		URL itemUrl = AssessmentItemBuilderTest.class.getResource("resources/openolat/match-12options-8correct.xml");
		File itemFile = new File(itemUrl.toURI());
		AssessmentItem assessmentItem = loadAssessmentItem(itemFile);
		MatchAssessmentItemBuilder itemBuilder = new MatchAssessmentItemBuilder(assessmentItem, qtiSerializer);

		Value score = run(itemFile, itemBuilder, pair(itemBuilder, 0, 0), pair(itemBuilder, 0, 1),
				pair(itemBuilder, 1, 2),
				pair(itemBuilder, 2, 0), pair(itemBuilder, 2, 1),
				pair(itemBuilder, 3, 0), pair(itemBuilder, 3, 1), pair(itemBuilder, 3, 2));
		Assert.assertTrue(score instanceof FloatValue);
		Assert.assertEquals(7.5d, ((FloatValue)score).doubleValue(), 0.0001d);
	}

	/**
	 * A matrix scored with the negative point system.
	 *
	 * @param cssClass The matrix or the drag and drop variant
	 * @param multipleChoice true for multiple choice, false for single choice
	 * @param withCorrectAnswers true to define one correct answer per row
	 * @param numOfRows The number of rows
	 * @param numOfColumns The number of columns
	 * @param minScore The min. score of the item
	 * @param maxScore The max. score of the item
	 * @return The builder of the assessment item
	 */
	private MatchAssessmentItemBuilder createMatrix(String cssClass, boolean multipleChoice,
			boolean withCorrectAnswers, int numOfRows, int numOfColumns, ScoreEvaluation scoreEvaluation, double minScore, double maxScore) {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		MatchAssessmentItemBuilder itemBuilder = new MatchAssessmentItemBuilder("Matrix", cssClass, qtiSerializer);
		itemBuilder.setQuestion("<p>Select one answer per row:</p>");
		
		List<SimpleAssociableChoice> sourceChoices = itemBuilder.getSourceChoices();
		sourceChoices.clear();
		for(int i=0; i<numOfRows; i++) {
			sourceChoices.add(AssessmentItemFactory
					.createSimpleAssociableChoice("Row " + (i + 1), itemBuilder.getSourceMatchSet()));
		}

		List<SimpleAssociableChoice> targetChoices = itemBuilder.getTargetChoices();
		targetChoices.clear();
		for(int i=0; i<numOfColumns; i++) {
			targetChoices.add(AssessmentItemFactory
					.createSimpleAssociableChoice("Column " + (i + 1), itemBuilder.getTargetMatchSet()));
		}

		itemBuilder.clearAssociations();
		if(withCorrectAnswers) {
			for(int i=0; i<numOfRows; i++) {
				itemBuilder.addAssociation(sourceChoices.get(i).getIdentifier(),
						targetChoices.get(i % numOfColumns).getIdentifier());
				
				if(scoreEvaluation == ScoreEvaluation.perAnswer) {
					DirectedPairValue pair = new DirectedPairValue(sourceChoices.get(i).getIdentifier(),
							targetChoices.get(i % numOfColumns).getIdentifier());
					itemBuilder.addScore(pair, Double.valueOf(1.0));
				}
			}
		}

		itemBuilder.setMultipleChoice(multipleChoice);
		itemBuilder.setScoreEvaluationMode(scoreEvaluation);
		itemBuilder.setMinScore(minScore);
		itemBuilder.setMaxScore(maxScore);
		itemBuilder.build();
		return itemBuilder;
	}

	private File serialize(MatchAssessmentItemBuilder itemBuilder) throws IOException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		File itemFile = new File(WebappHelper.getTmpDir(), "matchAssessmentItem" + UUID.randomUUID() + ".xml");
		try(FileOutputStream out = new FileOutputStream(itemFile)) {
			qtiSerializer.serializeJqtiObject(itemBuilder.getAssessmentItem(), out);
		} catch(Exception e) {
			log.error("", e);
			Assert.fail("Cannot serialize the assessment item");
		}
		return itemFile;
	}

	/**
	 * @param row The index of the row (source choice)
	 * @param column The index of the column (target choice)
	 * @return The directed pair as submitted by the candidate
	 */
	private String pair(MatchAssessmentItemBuilder itemBuilder, int row, int column) {
		return itemBuilder.getSourceChoices().get(row).getIdentifier().toString()
				+ " " + itemBuilder.getTargetChoices().get(column).getIdentifier().toString();
	}

	private Value run(File itemFile, MatchAssessmentItemBuilder itemBuilder, String... pairs) {
		Map<Identifier, ResponseData> responseMap = new HashMap<>();
		responseMap.put(itemBuilder.getMatchInteraction().getResponseIdentifier(), new StringResponseData(pairs));
		ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
		return itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
	}

	private AssessmentItem loadAssessmentItem(File itemFile) {
		QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(itemFile.toPath());
		AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
		ResolvedAssessmentItem item = assessmentObjectXmlLoader.loadAndResolveAssessmentItem(itemFile.toURI());
		return item.getItemLookup().getRootNodeHolder().getRootNode();
	}
}
