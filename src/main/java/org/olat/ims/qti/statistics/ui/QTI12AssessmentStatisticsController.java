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
package org.olat.ims.qti.statistics.ui;

import static org.olat.ims.qti.statistics.ui.StatisticFormatter.duration;
import static org.olat.ims.qti.statistics.ui.StatisticFormatter.format;
import static org.olat.ims.qti.statistics.ui.StatisticFormatter.getModeString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.chart.BarSeries.Stringuified;
import org.olat.core.gui.components.chart.StatisticsComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.CodeHelper;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.QTIDocument;
import org.olat.ims.qti.editor.beecom.objects.Section;
import org.olat.ims.qti.statistics.QTIStatisticResourceResult;
import org.olat.ims.qti.statistics.QTIStatisticsManager;
import org.olat.ims.qti.statistics.QTIStatisticsResource;
import org.olat.ims.qti.statistics.QTIType;
import org.olat.ims.qti.statistics.model.StatisticAssessment;
import org.olat.ims.qti.statistics.model.StatisticItem;
import org.olat.ims.qti.statistics.model.StatisticSurveyItem;
import org.olat.repository.RepositoryManager;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12AssessmentStatisticsController extends BasicController implements TooledController {

	private final QTIType type;
	private final Float maxScore;
	private final Float cutValue;
	private final String mediaBaseURL;
	private final Long courseResourceID;
	private final Long repoEntryId;
	
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackPanel;
	private final Link printLink, downloadRawLink;
	
	private final SeriesFactory seriesfactory;
	private final QTIStatisticResourceResult resourceResult;
	private final QTIStatisticsManager qtiStatisticsManager;

	public QTI12AssessmentStatisticsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			QTIStatisticResourceResult resourceResult, boolean printMode) {
		super(ureq, wControl);
		
		type = resourceResult.getType();
		this.stackPanel = stackPanel;
		this.resourceResult = resourceResult;
		mediaBaseURL = resourceResult.getMediaBaseURL();
		seriesfactory = new SeriesFactory(resourceResult, getTranslator());
		qtiStatisticsManager = CoreSpringFactory.getImpl(QTIStatisticsManager.class);
		courseResourceID = RepositoryManager.getInstance().lookupRepositoryEntryKey(resourceResult.getCourseOres(), false);
		repoEntryId = resourceResult.getQTIRepositoryEntry().getResourceableId();
		
		mainVC = createVelocityContainer("statistics_assessment");
		mainVC.put("loadd3js", new StatisticsComponent("d3loader"));
		
		mainVC.contextPut("printMode", new Boolean(printMode));
		if(stackPanel != null) {
			printLink = LinkFactory.createToolLink("print" + CodeHelper.getRAMUniqueID(), translate("print"), this);
			printLink.setIconLeftCSS("o_icon o_icon_print o_icon-lg");
			printLink.setPopup(new LinkPopupSettings(680, 500, "qti-stats"));
			
			downloadRawLink = LinkFactory.createToolLink("download" + CodeHelper.getRAMUniqueID(), translate("download.raw.data"), this);
		} else {
			printLink = null;
			downloadRawLink = LinkFactory.createLink("download.raw.data", mainVC, this);
			downloadRawLink.setCustomEnabledLinkCSS("o_content_download");
			mainVC.put("download", downloadRawLink);
		}
		downloadRawLink.setIconLeftCSS("o_icon o_icon_download o_icon-lg");

		//cut value
		QTICourseNode testNode = resourceResult.getTestCourseNode();
		
		StatisticAssessment stats = resourceResult.getQTIStatisticAssessment();
		
		boolean hasEssay = false;

		List<Item> items = new ArrayList<>();
		QTIDocument qtiDocument = resourceResult.getQTIDocument();
		for(Section section:qtiDocument.getAssessment().getSections()) {
			for(Item item:section.getItems()) {
				items.add(item);
				String ident = item.getIdent();
				if(ident != null && ident.startsWith("QTIEDIT:ESSAY")) {
					hasEssay = true;
				}
			}
		}
		
		if(hasEssay) {
			mainVC.contextPut("hasEssay", Boolean.TRUE);
		}

		cutValue = getCutValueSetting(testNode);
		maxScore = getMaxScoreSetting(testNode, items);

		initCourseNodeInformation(stats);
		initDurationHistogram(resourceResult.getQTIStatisticAssessment());
		if(QTIType.test.equals(type)) {
			initScoreHistogram(stats);
			initScoreStatisticPerItem(items, stats.getNumOfParticipants());
		} else {
			initItemsOverview(items);
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeTool(downloadRawLink);
			stackPanel.removeTool(printLink);
		}
	}

	@Override
	public void initTools() {
		if(stackPanel != null) {
			stackPanel.addTool(printLink, Align.right);
			stackPanel.addTool(downloadRawLink, Align.right);
		}
	}

	private Float getCutValueSetting(QTICourseNode testNode) {
		Float cutValueSetting;
		if(QTIType.test.equals(type)) {
			Object cutScoreObj = testNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_CUTVALUE);
			if (cutScoreObj instanceof Float) {
				cutValueSetting = (Float)cutScoreObj;
			} else {
				cutValueSetting = null;
			}
		} else {
			cutValueSetting = null;
		}
		return cutValueSetting;
	}
	
	private Float getMaxScoreSetting(QTICourseNode testNode, List<Item> items) {
		Float maxScoreSetting;
		if(QTIType.test.equals(type)) {
			Object maxScoreObj = testNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_MAXSCORE);
			if (maxScoreObj instanceof Float) {
				maxScoreSetting = (Float)maxScoreObj;
			} else {
				// try to calculate max
				float max = 0;
				for (Item item: items) {
					if(item.getQuestion() != null) {
						max += item.getQuestion().getMaxValue();
					}
				}
				maxScoreSetting = max > 0 ? max : null;
			}
		} else {
			maxScoreSetting = null;
		}
		return maxScoreSetting;
	}
	
	private void initCourseNodeInformation(StatisticAssessment stats) {
		mainVC.contextPut("numOfParticipants", stats.getNumOfParticipants());
		mainVC.contextPut("type", resourceResult.getType());
		mainVC.contextPut("courseId", courseResourceID);		
		mainVC.contextPut("testId", repoEntryId);		
		
		if(QTIType.test.equals(type)) {
			mainVC.contextPut("numOfPassed", stats.getNumOfPassed());
			mainVC.contextPut("numOfFailed", stats.getNumOfFailed());
	
			if (cutValue != null) {
				mainVC.contextPut("cutScore", format(cutValue));
			} else {
				mainVC.contextPut("cutScore", "-");
			}
	
			mainVC.contextPut("maxScore", format(maxScore));
			mainVC.contextPut("average", format(stats.getAverage()));
			mainVC.contextPut("range", format(stats.getRange()));
			mainVC.contextPut("standardDeviation", format(stats.getStandardDeviation()));
			mainVC.contextPut("mode", getModeString(stats.getMode()));
			mainVC.contextPut("median", format(stats.getMedian()));
		}
		
		String duration = duration(stats.getAverageDuration());
		mainVC.contextPut("averageDuration", duration);
	}
	
	private void initDurationHistogram(StatisticAssessment stats) {
		if(!BarSeries.hasNotNullDatas(stats.getDurations())) return;
		
		VelocityContainer durationHistogramVC = createVelocityContainer("histogram_duration");
		durationHistogramVC.contextPut("datas", BarSeries.datasToString(stats.getDurations()));
		mainVC.put("durationHistogram", durationHistogramVC);
	}
	
	private void initScoreStatisticPerItem(List<Item> items, double numOfParticipants) {
		BarSeries d1 = new BarSeries();
		BarSeries d2 = new BarSeries();
		List<StatisticItem> statisticItems = qtiStatisticsManager
				.getStatisticPerItem(items, resourceResult.getSearchParams(), numOfParticipants);
		
		int i = 0;
		List<ItemInfos> itemInfos = new ArrayList<>(items.size());
		for (StatisticItem statisticItem: statisticItems) {
			Item item = statisticItem.getItem();
			
			String label = Integer.toString(++i);
			String text = item.getTitle(); 
			d1.add(statisticItem.getAverageScore(), label);
			double numOfRightAnswers = statisticItem.getNumOfCorrectAnswers();
			double res = numOfRightAnswers;
			d2.add(res, label);
			
			itemInfos.add(new ItemInfos(label, text));
		}
		
		mainVC.contextPut("itemInfoList", itemInfos);

		VelocityContainer averageScorePeritemVC = createVelocityContainer("hbar_average_score_per_item");
		Stringuified data1 = BarSeries.getDatasAndColors(Collections.singletonList(d1), "bar_default");
		averageScorePeritemVC.contextPut("datas", data1);
		mainVC.put("averageScorePerItemChart", averageScorePeritemVC);
		
		VelocityContainer percentRightAnswersPerItemVC = createVelocityContainer("hbar_right_answer_per_item");
		Stringuified data2 = BarSeries.getDatasAndColors(Collections.singletonList(d2), "bar_green");
		percentRightAnswersPerItemVC.contextPut("datas", data2);
		percentRightAnswersPerItemVC.contextPut("numOfParticipants", Long.toString(Math.round(numOfParticipants)));
		mainVC.put("percentRightAnswersPerItemChart", percentRightAnswersPerItemVC);
	}

	private void initScoreHistogram(StatisticAssessment stats) {
		VelocityContainer scoreHistogramVC = createVelocityContainer("histogram_score");
		scoreHistogramVC.contextPut("datas", BarSeries.datasToString(stats.getScores()));
		scoreHistogramVC.contextPut("cutValue", cutValue);
		mainVC.put("scoreHistogram", scoreHistogramVC);
	}
	
	private void initItemsOverview(List<Item> items) {
		List<StatisticSurveyItem> surveyItems = qtiStatisticsManager
				.getStatisticAnswerOptions(resourceResult.getSearchParams(), items);

		int count = 0;
		List<String> overviewList = new ArrayList<>();
		for(StatisticSurveyItem surveyItem:surveyItems) {
			Item item = surveyItem.getItem();
			Series series = seriesfactory.getSeries(item, null);
			if(series != null) {//essay hasn't a series
				String name = "overview_" + count++;
				VelocityContainer vc = createVelocityContainer(name, "hbar_item_overview");
				vc.contextPut("series", series);
				vc.contextPut("question", item.getQuestion().getQuestion().renderAsHtml(mediaBaseURL));
				vc.contextPut("questionType", item.getQuestion().getType());
				vc.contextPut("title", item.getTitle());
				mainVC.put(vc.getDispatchID(), vc);
				overviewList.add(vc.getDispatchID());
			}
		}
		
		mainVC.contextPut("overviewList", overviewList);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(printLink == source) {
			printPages(ureq);
		} else if(downloadRawLink == source) {
			doDownloadRawData(ureq);
		}
	}
	
	private void doDownloadRawData(UserRequest ureq) {
		MediaResource resource = new QTIStatisticsResource(resourceResult, getLocale());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}

	private void printPages(UserRequest ureq) {
		ControllerCreator printControllerCreator = new ControllerCreator() {
			public Controller createController(UserRequest lureq, WindowControl lwControl) {
				return new QTI12PrintController(lureq, lwControl, resourceResult);
			}					
		};
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createPrintPopupLayout(printControllerCreator);
		openInNewBrowserWindow(ureq, layoutCtrlr);
	}
	
	public static class ItemInfos {
		
		private final String label;
		private final String text;
		
		public ItemInfos(String label, String text) {
			this.label = label;
			this.text = text;
		}

		public String getLabel() {
			return label;
		}

		public String getText() {
			return text;
		}
	}
}