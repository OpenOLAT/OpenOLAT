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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.statistic;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Base class for Statistic Display Controllers - subclass this 
 * to show a simple table with the result of a statistic query.
 * <p>
 * Initial Date: 10.02.2010 <br>
 * 
 * @author eglis
 */
public class StatisticDisplayController extends BasicController {
	
	class Graph {
		private int max = 0;
		public String chd;
		public List<String> labelList;
		public String chartIntroStr;
		private int numElements = 0;
		
		String getLabelsFormatted(int maxLength, double maxWidth) {
			final int MIN_LENGTH = 8;
			StringBuffer sb = new StringBuffer();
			long labelsToIgnore = 0;
			for (Iterator<String> it = labelList.iterator(); it.hasNext();) {
				String aLabel = it.next();
				sb.append("|");
				if (maxLength==-1) {
					sb.append(aLabel);
				} else {
					if (maxLength<MIN_LENGTH) {
						if (labelsToIgnore>0) {
							// then we don't issue a label here
							labelsToIgnore--;
							continue;
						}
						// then issue a label with length MIN_LENGTH
						sb.append(aLabel.length()>MIN_LENGTH ? (aLabel.substring(0, Math.max(1,MIN_LENGTH-2))+"..") : aLabel);
						
						labelsToIgnore = Math.round(MIN_LENGTH * 6/maxWidth) - 1;
					} else {
						sb.append(aLabel.length()>maxLength ? (aLabel.substring(0, Math.max(1,maxLength-2))+"..") : aLabel);
					}
				}
			}
			try {
				return URLEncoder.encode(sb.toString(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return URLEncoder.encode(sb.toString());
			}
		}

		public int getLengthOfLastLabel() {
			if (labelList!=null && labelList.size()>0) {
				return labelList.get(labelList.size()-1).length();
			} else {
				// return some minimal meaningful length for the empty label
				return 10;
			}
		}
	}
	
	/** the logging object used in this class **/
	private static final OLog log_ = Tracing.createLoggerFor(StatisticDisplayController.class);

	private final static String CLICK_NODE_ACTION = "clicknodeaction";
	
	public final static String CLICK_TOTAL_ACTION = "clicktotalaction";

	/** a possible value of statisticType in the user activity logging **/
	private static final String STATISTIC_TYPE_VIEW_NODE_STATISTIC = "VIEW_NODE_STATISTIC";

	/** a possible value of statisticType in the user activity logging **/
	private static final String STATISTIC_TYPE_VIEW_TOTAL_OF_NODES_STATISTIC = "VIEW_TOTAL_OF_NODES_STATISTIC";

	/** a possible value of statisticType in the user activity logging **/
	private static final String STATISTIC_TYPE_VIEW_TOTAL_BY_VALUE_STATISTIC = "VIEW_TOTAL_BY_VALUE_STATISTIC";

	/** a possible value of statisticType in the user activity logging **/
	private static final String STATISTIC_TYPE_VIEW_TOTAL_TOTAL_STATISTIC = "VIEW_TOTAL_TOTAL_STATISTIC";

	private final ICourse course;
	private final IStatisticManager statisticManager;

	private TableController tableCtr_;
	private TableController tableController;

	private VelocityContainer statisticVc_;

	private Translator headerTranslator_;
	
	
	public StatisticDisplayController(UserRequest ureq, WindowControl windowControl, ICourse course, IStatisticManager statisticManager) {
		super(ureq, windowControl);

		addLoggingResourceable(LoggingResourceable.wrap(course));
		addLoggingResourceable(LoggingResourceable.wrapNonOlatResource(StringResourceableType.statisticManager, "", statisticManager.getClass().getSimpleName()));
		
		if (course==null) {
			throw new IllegalArgumentException("Course must not be null");
		}
		if (statisticManager==null) {
			throw new IllegalArgumentException("statisticManager must not be null");
		}
		this.course = course;
		this.statisticManager = statisticManager;
		this.headerTranslator_ = Util.createPackageTranslator(statisticManager.getClass(), ureq.getLocale());

		// statistic.html is under org.olat.course.statistic - no matter who subclasses BaseStatisticDisplayController
		setVelocityRoot(Util.getPackageVelocityRoot(StatisticDisplayController.class));
		setTranslator(Util.createPackageTranslator(statisticManager.getClass(), ureq.getLocale(), Util.createPackageTranslator(StatisticDisplayController.class, ureq.getLocale())));
		
		putInitialPanel(createInitialComponent(ureq));
	}
	
	protected Component createInitialComponent(UserRequest ureq) {
		statisticVc_ = this.createVelocityContainer("statistic");
		statisticVc_.contextPut("statsSince", getStatsSinceStr(ureq));
		Package pkg = getStatisticManager().getClass().getPackage();
		String fullPkgName = pkg.getName();
		String pkgName = fullPkgName.substring(fullPkgName.lastIndexOf(".")+1);
		statisticVc_.contextPut("package", fullPkgName);
		statisticVc_.contextPut("packageHtml", "statistic_"+pkgName+".html");
		recreateTableController(ureq);
		
		return statisticVc_;
	}

	protected void recreateTableController(UserRequest ureq) {
		StatisticResult result = recalculateStatisticResult(ureq);
		tableCtr_ = createTableController(ureq, result);
		statisticVc_.put("statisticResult", tableCtr_.getInitialComponent());
		statisticVc_.contextPut("hasChart", Boolean.FALSE);

		Graph graph = calculateNodeGraph(ureq, result.getRowCount()-1);
		generateCharts(graph);
	}

	protected StatisticResult recalculateStatisticResult(UserRequest ureq) {
		return statisticManager.generateStatisticResult(ureq, course, getCourseRepositoryEntryKey());
	}
	
	private TableController createTableController(UserRequest ureq, StatisticResult result) {
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setDisplayTableHeader(true);
		tableConfig.setDisplayRowCount(true);
		tableConfig.setPageingEnabled(true);
		tableConfig.setDownloadOffered(true);
		tableConfig.setSortingEnabled(true);
		
		removeAsListenerAndDispose(tableController);
		tableController = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(tableController);
		
		//		tableCtr.addColumnDescriptor(statisticManager.createColumnDescriptor(ureq, 0, null));
		IndentedStatisticNodeRenderer indentedNodeRenderer = new IndentedStatisticNodeRenderer(Util.createPackageTranslator(statisticManager.getClass(), ureq.getLocale()));
		indentedNodeRenderer.setSimpleRenderingOnExport(true);
		CustomRenderColumnDescriptor nodeCD = new CustomRenderColumnDescriptor("stat.table.header.node", 0, 
				CLICK_NODE_ACTION, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, indentedNodeRenderer) {
			@Override
			public int compareTo(int rowa, int rowb) {
				// order by original row order
				return new Integer(rowa).compareTo(rowb);
			}
		};
		tableController.addColumnDescriptor(nodeCD);

		int column = 1;
		List<String> headers = result.getHeaders();
		for (Iterator<String> it = headers.iterator(); it.hasNext();) {
			final String aHeader = it.next();
			final int aColumnId = column++;
			tableController.addColumnDescriptor(statisticManager.createColumnDescriptor(ureq, aColumnId, aHeader));
		}
		
		tableController.addColumnDescriptor(new CustomRenderColumnDescriptor("stat.table.header.total", column, 
				StatisticDisplayController.CLICK_TOTAL_ACTION+column, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT, new TotalColumnRenderer()) {
			@Override
			public String getAction(int row) {
				if (row==table.getTableDataModel().getRowCount()-1) {
					return super.getAction(row);
				} else {
					return null;
				}
			}
			
		});
		
		tableController.setTableDataModel(result);
		
		return tableController;
	}
	
	/**
	 * Returns the ICourse which this controller is showing statistics for
	 * @return the ICourse which this controller is showing statistics for
	 */
	protected ICourse getCourse() {
		return course;
	}

	/**
	 * Returns the IStatisticManager associated to this controller via spring.
	 * @return the IStatisticManager associated to this controller via spring
	 */
	protected IStatisticManager getStatisticManager() {
		return statisticManager;
	}
	
	protected long getCourseRepositoryEntryKey() {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.class, course.getResourceableId());
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(ores, false);
		long resid = 0;
		if (re != null) {
			resid = re.getKey();
		}
		return resid;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to be done here yet
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source==tableCtr_ && event instanceof TableEvent) {
			TableEvent tableEvent = (TableEvent)event;
			if (CLICK_NODE_ACTION.equals(tableEvent.getActionId())) {

				int rowId = tableEvent.getRowId();
				Graph graph = calculateNodeGraph(ureq, rowId);
				generateCharts(graph);
			} else if (tableEvent.getActionId().startsWith(CLICK_TOTAL_ACTION)) {
				
				try{
					int columnId = Integer.parseInt(tableEvent.getActionId().substring(CLICK_TOTAL_ACTION.length()));
					Graph graph = calculateTotalGraph(ureq, columnId);
					generateCharts(graph);
				} catch(NumberFormatException e) {
					log_.warn("event: Could not convert event into columnId for rendering graph: "+tableEvent.getActionId());
					return;
				}
				
			}
		}
	}
	
	private Graph calculateNodeGraph(UserRequest ureq, int rowId) {
		
		Object o = tableCtr_.getTableDataModel().getValueAt(rowId, 0);
		String selectionInfo = "";
		if (o instanceof Map) {
			Map map = (Map)o;
			CourseNode node = (CourseNode) map.get(StatisticResult.KEY_NODE);
			ThreadLocalUserActivityLogger.log(StatisticLoggingAction.VIEW_NODE_STATISTIC, getClass(), 
					LoggingResourceable.wrap(node),
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.statisticType, "", STATISTIC_TYPE_VIEW_NODE_STATISTIC));
			selectionInfo = getTranslator().translate("statistic.chart.selectioninfo.node", new String[] { (String) map.get(AssessmentHelper.KEY_TITLE_SHORT) });
		} else {
			ThreadLocalUserActivityLogger.log(StatisticLoggingAction.VIEW_TOTAL_OF_NODES_STATISTIC, getClass(), 
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.statisticType, "", STATISTIC_TYPE_VIEW_TOTAL_OF_NODES_STATISTIC));
			selectionInfo = getTranslator().translate("statistic.chart.selectioninfo.total");
		}
		String chartIntroStr = headerTranslator_.translate("statistic.chart.intro", new String[] { selectionInfo, getStatsSinceStr(ureq) });
		
		StringBuffer chd = new StringBuffer();

		int max = 10;
		int columnCnt = tableCtr_.getTableDataModel().getColumnCount();
		List<String> labelList = new LinkedList<String>();
		for(int column=1/*we ignore the node itself*/; column<columnCnt-1/*we ignore the total*/; column++) {
			Object cellValue = tableCtr_.getTableDataModel().getValueAt(rowId, column);
			Integer v = 0;
			if (cellValue instanceof Integer) {
				v = (Integer)cellValue;
			}
			max = Math.max(max, v);
			if (chd.length()!=0) {
				chd.append(",");
			}
			chd.append(v);
			
			ColumnDescriptor cd = tableCtr_.getColumnDescriptor(column);
			String headerKey = cd.getHeaderKey();
			if (cd.translateHeaderKey()) {
				headerKey = headerTranslator_.translate(headerKey);
			}
			labelList.add(headerKey);
		}
		Graph result = new Graph();
		result.max = max;
		result.chd = chd.toString();
		result.labelList = labelList;
		result.chartIntroStr = chartIntroStr;
		result.numElements = columnCnt-2;
		return result;
	}

	private Graph calculateTotalGraph(UserRequest ureq, int columnId) {
		ColumnDescriptor cd = tableCtr_.getColumnDescriptor(columnId);
		String headerKey = cd.getHeaderKey();
		if (cd.translateHeaderKey()) {
			headerKey = headerTranslator_.translate(headerKey);
		}
		String selectionInfo = headerKey;
		String chartIntroStr;
		if (columnId==tableCtr_.getTableDataModel().getColumnCount()-1) {
			ThreadLocalUserActivityLogger.log(StatisticLoggingAction.VIEW_TOTAL_TOTAL_STATISTIC, getClass(), 
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.statisticType, "", STATISTIC_TYPE_VIEW_TOTAL_TOTAL_STATISTIC));
			chartIntroStr = headerTranslator_.translate("statistic.chart.pernode.total.intro", new String[] {getStatsSinceStr(ureq)});
		} else {
			ThreadLocalUserActivityLogger.log(StatisticLoggingAction.VIEW_TOTAL_BY_VALUE_STATISTIC, getClass(), 
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.statisticType, "", STATISTIC_TYPE_VIEW_TOTAL_BY_VALUE_STATISTIC),
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.statisticColumn, "", selectionInfo));
			chartIntroStr = headerTranslator_.translate("statistic.chart.pernode.intro", new String[] { selectionInfo });
		}
		
		StringBuffer chd = new StringBuffer();

		int max = 10;
		
		List<String> labelList = new LinkedList<String>();
		for(int row=0; row<tableCtr_.getTableDataModel().getRowCount()-1; row++) {
			Object cellValue = tableCtr_.getTableDataModel().getValueAt(row, columnId);
			Integer v = 0;
			if (cellValue instanceof Integer) {
				v = (Integer)cellValue;
			}
			max = Math.max(max, v);
			if (chd.length()!=0) {
				chd.append(",");
			}
			chd.append(v);
			
			Map m = (Map)tableCtr_.getTableDataModel().getValueAt(row, 0);
			headerKey = "n/a";
			if (m!=null) {
				headerKey = (String) m.get(AssessmentHelper.KEY_TITLE_SHORT);
			}
			
			labelList.add(headerKey);
		}
		Graph result = new Graph();
		result.max = max;
		result.chd = chd.toString();
		result.labelList = labelList;
		result.chartIntroStr = chartIntroStr;
		result.numElements = tableCtr_.getTableDataModel().getRowCount()-1;
		return result;
	}
	private void generateCharts(Graph graph) {
		statisticVc_.contextPut("hasChart", Boolean.FALSE);
		statisticVc_.contextPut("hasChartError", Boolean.FALSE);
		if (graph==null || graph.numElements==0) {
			Component ic = getInitialComponent();
			if (ic!=null) {
				ic.setDirty(true);
			}
			return;
		}
		try{
			statisticVc_.contextPut("chartAlt", getTranslator().translate("chart.alt"));
			statisticVc_.contextPut("chartIntro", graph.chartIntroStr);
			
			int lengthLastLabel = graph.getLengthOfLastLabel(); // if '|' does not occur, this will be length+1 which is okish
			
			int maxWidth = 1000;
			int idealBarWidth = 32;
			int idealBarSpace = 24;
			int widthPerCharacter = 6; // this is the width per character, roughly
			int additionalYAxisWidth = 9; // this is the width needed for the y axis and the dashes themselves
			int spareWidth = 5;
			int minimalSpaceBetweenLabels = 2;
			double maxLabelWidth = ((double)idealBarWidth+(double)idealBarSpace-(double)minimalSpaceBetweenLabels);
			int maxLabelChars = (int)Math.floor(maxLabelWidth/(double)widthPerCharacter);
			
			int yAxisWidthLeftMargin = String.valueOf(graph.max).length()*widthPerCharacter + additionalYAxisWidth;
			int yAxisWidthRightMargin = (maxLabelChars*widthPerCharacter)/2 + spareWidth;
			double idealWidthToSpaceRatio = (double)idealBarWidth/(double)idealBarSpace;
			
			String chartType = "bvs";
			String chartData = graph.chd;
			String chartDataScaling = "0,"+graph.max;
			String chartAxisRange = "1,0,"+graph.max;
			String chartColor = "879CB8"; 
			//olat dark blue r=91, g=117, b=154 => 5B,75,9A
			//olat light blue r=135, g=156, b=184 => 87,9C,B8
			//olat light grey-blue r=217, g=220, b=227 => D9,Dc,E3
			
			String chartXLabels = graph.getLabelsFormatted(maxLabelChars, maxLabelWidth);

			String chartBarWidth = String.valueOf(idealBarWidth);
			String chartSpaceBetweenBars = String.valueOf(idealBarSpace); 
			String chartSize = "1000x220";
			
			//calculate the max size using default values
			double n = graph.numElements;
			long idealWidth = yAxisWidthLeftMargin + Math.round((n - 0.5) * idealBarWidth) + Math.round((n-1) * idealBarSpace) + yAxisWidthRightMargin;
			if (idealWidth>maxWidth) {
				double drawingWidth = maxWidth - yAxisWidthLeftMargin - yAxisWidthRightMargin;
				// be:
				//   a: the width of a bar
				//   b: the space between bars
				//   f: the factor a/b -> f=a/b, a=f*b
				//   c: the max space available for all bars
				//   n: the number of bars
				// formula:
				//   c = (n-0.5)*a + (n-1)*b = n*f*b + (n-1)*b = ((n-0.5)*f + n - 1)*b
				double possibleBarSpace = drawingWidth/((n-0.5)*idealWidthToSpaceRatio + n - 1);
				int barSpace = Math.max(0, (int)Math.floor(possibleBarSpace));
				
				// calculate again with the actual barSpace
				// formula:
				//   a = (c - (n-1)*b)/(n-0.5)
				
				int barWidth = Math.max(1, (int)Math.floor((drawingWidth-(n-1)*((double)barSpace))/(n-0.5)));
				
				chartBarWidth = String.valueOf(barWidth);
				chartSpaceBetweenBars = String.valueOf(barSpace);
				maxLabelWidth = ((double)barWidth+(double)barSpace-(double)minimalSpaceBetweenLabels);
				maxLabelChars = (int)Math.floor(maxLabelWidth/(double)widthPerCharacter);
				chartXLabels = graph.getLabelsFormatted(maxLabelChars, maxLabelWidth);

				lengthLastLabel = Math.min(maxLabelChars, lengthLastLabel);
				yAxisWidthRightMargin = (lengthLastLabel*widthPerCharacter)/2 + spareWidth;
				long actualWidth = yAxisWidthLeftMargin + Math.round((n - 0.5) * barWidth) + Math.round((n-1) * barSpace) + yAxisWidthRightMargin;
				chartSize = actualWidth+"x220";
			} else {
				chartSize = idealWidth+"x220";
			}
			
			String url = "http://chart.apis.google.com/chart?" +
					"chs="+chartSize+
					"&chma=0,0,0,0"+
					"&cht="+chartType+
					"&chd=t:"+chartData+
					"&chds="+chartDataScaling+
					"&chxt=x,y" +
					"&chxl=0:"+chartXLabels+
					"&chco="+chartColor+
					"&chbh="+chartBarWidth+","+chartSpaceBetweenBars+
					"&chxr="+chartAxisRange;
			statisticVc_.contextPut("chartUrl", url);
			if (url.length()>2000) {
				// from http://code.google.com/apis/chart/faq.html#url_length
				// The maximum length of a URL is not determined by the Google Chart API, 
				// but rather by web browser and web server considerations.
				// The longest URL that Google accepts in a chart GET request is 2048 characters in length, 
				// after URL-encoding (e.g., | becomes %7C). For POST, this limit is 16K. 
				statisticVc_.contextPut("hasChartError", Boolean.TRUE);
				statisticVc_.contextPut("hasChart", Boolean.FALSE);
				statisticVc_.contextPut("chartError", getTranslator().translate("chart.error"));
			} else {
				statisticVc_.contextPut("hasChart", Boolean.TRUE);
				statisticVc_.contextPut("hasChartError", Boolean.FALSE);
			}
		} catch(RuntimeException re) {
			log_.warn("generateCharts: RuntimeException during chart generation: "+re, re);
		}
		Component ic = getInitialComponent();
		if (ic!=null) {
			ic.setDirty(true);
		}
	}

	protected String getStatsSinceStr(UserRequest ureq) {
		Date d = SimpleStatisticInfoHelper.getFirstLoggingTableCreationDate();
		if (d==null) {
			return "n/a";
		}
		Calendar c = Calendar.getInstance(ureq.getLocale());
		c.setTime(d);
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, ureq.getLocale());
		return df.format(c.getTime());
	}
	
	@Override
	protected void doDispose() {
	// TODO Auto-generated method stub

	}

}
