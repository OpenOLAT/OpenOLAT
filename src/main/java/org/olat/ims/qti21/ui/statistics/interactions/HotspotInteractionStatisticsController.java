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
package org.olat.ims.qti21.ui.statistics.interactions;

import static org.olat.ims.qti21.model.xml.QtiNodesExtractor.extractIdentifiersFromCorrectResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.ims.qti21.QTI21StatisticsManager;
import org.olat.ims.qti21.manager.CorrectResponsesUtil;
import org.olat.ims.qti21.model.statistics.HotspotChoiceStatistics;
import org.olat.ims.qti21.model.statistics.StatisticsItem;
import org.olat.ims.qti21.ui.statistics.QTI21AssessmentItemStatisticsController;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticResourceResult;
import org.olat.ims.qti21.ui.statistics.SeriesFactory;
import org.olat.ims.qti21.ui.statistics.interactions.ResponseInfos.ExplanationType;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.content.xhtml.object.Object;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Shape;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.graphic.HotspotChoice;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;

/**
 * 
 * Initial date: 04.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HotspotInteractionStatisticsController extends BasicController {
	
	private final HotspotInteraction interaction;
	private final AssessmentItem assessmentItem;
	private final QTI21StatisticResourceResult resourceResult;
	
	private String backgroundMapperUri;
	
	@Autowired
	private QTI21StatisticsManager qtiStatisticsManager;
	
	public HotspotInteractionStatisticsController(UserRequest ureq, WindowControl wControl,
			AssessmentItemRef itemRef, AssessmentItem assessmentItem, HotspotInteraction interaction,
			StatisticsItem itemStats, QTI21StatisticResourceResult resourceResult) {
		super(ureq, wControl, Util.createPackageTranslator(QTI21AssessmentItemStatisticsController.class, ureq.getLocale()));
		this.interaction = interaction;
		this.assessmentItem = assessmentItem;
		this.resourceResult = resourceResult;
		
		File itemFile = resourceResult.getAssessmentItemFile(itemRef);
		backgroundMapperUri = registerMapper(ureq, new BackgroundMapper(itemFile));
		
		VelocityContainer mainVC = createVelocityContainer("statistics_interaction");
		List<HotspotChoiceStatistics> statisticResponses = qtiStatisticsManager
				.getHotspotInteractionStatistics(itemRef.getIdentifier().toString(), assessmentItem, interaction, resourceResult.getSearchParams());
		Series series;
		if(isMultipleChoice()) {
			series = getMultipleChoice(itemStats, statisticResponses);
		} else {
			series = getSingleChoice(statisticResponses);
		}
		
		HotspotBubbles bubbles = buildBubbleChart(statisticResponses);
		
		VelocityContainer mapVc = createVelocityContainer("hotspot_item");
		mainVC.put("questionMap", mapVc);
		mapVc.contextPut("mapperUri", backgroundMapperUri);
		mapVc.contextPut("bubbles", bubbles);
		Object object = interaction.getObject();
		if(object != null) {
			mapVc.contextPut("filename", object.getData());
			if(object.getHeight() != null) {
				mapVc.contextPut("height", object.getHeight());
			}
			if(object.getWidth() != null) {
				mapVc.contextPut("width", object.getWidth());
			}
		}
		
		VelocityContainer vc = createVelocityContainer("hbar_item");
		vc.contextPut("series", series);
		mainVC.put("questionChart", vc);
		mainVC.contextPut("series", series);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		
	}
	
	private boolean isMultipleChoice() {
		ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
		if(responseDeclaration != null && responseDeclaration.getCorrectResponse() != null) {
			CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
			if(correctResponse.getCardinality().isOneOf(Cardinality.MULTIPLE)) {
				return true;
			}
		}
		return false;
	}
	
	private List<Identifier> getCorrectResponses() {
		List<Identifier> correctAnswers = new ArrayList<>();
		ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
		if(responseDeclaration != null && responseDeclaration.getCorrectResponse() != null) {
			extractIdentifiersFromCorrectResponse(responseDeclaration.getCorrectResponse(), correctAnswers);
		}
		return correctAnswers;
	}
	
	private HotspotBubbles buildBubbleChart(List<HotspotChoiceStatistics> statisticResponses) {
		List<HotspotBubble> bubbles = new ArrayList<>(statisticResponses.size());
		
		int count = 0;
		for (HotspotChoiceStatistics statisticResponse:statisticResponses) {
			HotspotChoice choice = statisticResponse.getChoice();
			bubbles.add(new HotspotBubble(Integer.toString(++count), choice.getShape(), choice.getCoords(), statisticResponse.getCount()));
		}
		return new HotspotBubbles(bubbles);
	}
	
	private Series getSingleChoice(List<HotspotChoiceStatistics> statisticResponses) {

		int numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();
		List<Identifier> correctAnswers = getCorrectResponses();
		
		int i = 0;
		long numOfResults = 0;
		BarSeries d1 = new BarSeries();
		List<ResponseInfos> responseInfos = new ArrayList<>();
		for (HotspotChoiceStatistics statisticResponse:statisticResponses) {
			HotspotChoice choice = statisticResponse.getChoice();
			String text = getAnswerText(choice);
			double ans_count = statisticResponse.getCount();
			numOfResults += statisticResponse.getCount();
			boolean correct = correctAnswers.contains(choice.getIdentifier());

			Float points;
			Double mappedValue = CorrectResponsesUtil.getMappedValue(assessmentItem, interaction, choice);
			if(mappedValue != null) {
				points = mappedValue.floatValue();
			} else {
				points = correct ? 1.0f : 0.0f;
			}
			String cssColor = correct ? "bar_green" : "bar_red";
	
			String label = Integer.toString(++i);
			d1.add(ans_count, label, cssColor);

			responseInfos.add(new ResponseInfos(label, text, null, null, points, correct, false, ExplanationType.standard));
		}
		
		if(numOfResults != numOfParticipants) {
			long notAnswered = numOfParticipants - numOfResults;
			if(notAnswered > 0) {
				String label = Integer.toString(++i);
				String text = translate("user.not.answer");
				responseInfos.add(new ResponseInfos(label, text, null, null, null, false, false, ExplanationType.standard));
				d1.add(notAnswered, label, "bar_grey");
			}
		}

		List<BarSeries> serieList = Collections.singletonList(d1);
		Series series = new Series(serieList, responseInfos, numOfParticipants, false);
		series.setChartType(SeriesFactory.BAR_CORRECT);
		series.setItemCss("o_qti_scitem");
		return series;
	}
	
	private Series getMultipleChoice(StatisticsItem itemStats, List<HotspotChoiceStatistics> statisticResponses) {

		BarSeries d1 = new BarSeries("bar_green", "green", translate("answer.correct"));
		BarSeries d2 = new BarSeries("bar_red", "red", translate("answer.false"));
		BarSeries d3 = new BarSeries("bar_grey", "grey", translate("answer.noanswer"));
		
		int numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();
		int notAnswered = numOfParticipants - (itemStats == null ? 0 : itemStats.getNumOfResults());
		List<Identifier> correctAnswers = getCorrectResponses();
		
		int i = 0;
		List<ResponseInfos> responseInfos = new ArrayList<>();
		for(HotspotChoiceStatistics statisticResponse:statisticResponses) {
			HotspotChoice choice = statisticResponse.getChoice();
			String text = getAnswerText(choice);
			boolean correct = correctAnswers.contains(choice.getIdentifier());
			double answersPerAnswerOption = statisticResponse.getCount();

			double rightA;
			double wrongA;
			
			if (correct) {
				rightA = answersPerAnswerOption;
				wrongA = numOfParticipants - notAnswered - answersPerAnswerOption;
			} else {
				//minus negative points are not answered right?
				rightA = numOfParticipants - notAnswered - answersPerAnswerOption ;
				wrongA = answersPerAnswerOption;
			}
			
			String label = Integer.toString(++i);
			d1.add(rightA, label);
			d2.add(wrongA, label);
			d3.add(notAnswered, label);
			
			Float pointsObj = correct ? 1.0f : 0.0f;
			responseInfos.add(new ResponseInfos(label, text, null, null, pointsObj, correct, false, ExplanationType.standard));
		}

		List<BarSeries> serieList = new ArrayList<>(3);
		serieList.add(d1);
		serieList.add(d2);
		serieList.add(d3);
		
		Series series = new Series(serieList, responseInfos, numOfParticipants, true);
		series.setChartType(SeriesFactory.BAR_CORRECT_WRONG_NOT);
		series.setItemCss("o_qti_scitem");
		return series;
	}
	
	private String getAnswerText(HotspotChoice choice) {
		String text = choice.getLabel();
		if(!StringHelper.containsNonWhitespace(text)) {
			text = choice.getLabel();
		}
		if(!StringHelper.containsNonWhitespace(text)) {
			text = choice.getIdentifier().toString();
		}
		return text;
	}
	
	public static class HotspotBubbles {
		private final List<HotspotBubble> bubbles;
		
		public HotspotBubbles(List<HotspotBubble> bubbles) {
			this.bubbles = bubbles;
		}

		public List<HotspotBubble> getBubbles() {
			return bubbles;
		}
		
		public String getData() {
			StringBuilder data = new StringBuilder();
			data.append("[");
			for(HotspotBubble bubble:bubbles) {
				if(data.length() > 1) data.append(",");
				
				data.append("['").append(bubble.getLabel()).append("','")
				    .append(bubble.getShape().name()).append("',[");
				for(int i=0; i<bubble.getCoords().size(); i++) {
					if(i > 0) data.append(",");
					data.append(bubble.getCoords().get(i).intValue());
				}   
				data.append("],").append(bubble.getNumOfCorrect())
				    .append("]");
			}
			data.append("]");
			return data.toString();
		}
	}
	
	public static class HotspotBubble {
		
		private final String label;
		private final Shape shape;
		private final List<Integer> coords;
		private final long numOfCorrect;
		
		public HotspotBubble(String label, Shape shape, List<Integer> coords, long numOfCorrect) {
			this.label = label;
			this.coords = coords;
			this.shape = shape;
			this.numOfCorrect = numOfCorrect;
		}

		public String getLabel() {
			return label;
		}

		public Shape getShape() {
			return shape;
		}

		public List<Integer> getCoords() {
			return coords;
		}

		public long getNumOfCorrect() {
			return numOfCorrect;
		}
	}
	
	private static class BackgroundMapper implements Mapper {
		
		private final File itemFile;
		
		public BackgroundMapper(File itemFile) {
			this.itemFile = itemFile;
		}

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(StringHelper.containsNonWhitespace(relPath)) {
				if(relPath.startsWith("/")) {
					relPath = relPath.substring(1);
				}
				
				File backgroundFile = new File(itemFile.getParentFile(), relPath);
				return new VFSMediaResource(new LocalFileImpl(backgroundFile));
			}
			return new NotFoundMediaResource();
		}
	}
}
