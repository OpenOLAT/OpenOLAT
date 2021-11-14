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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.BarChartComponent;
import org.olat.core.gui.components.chart.BarSeries;
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
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
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
	
	private static class Graph {
		private List<Integer> values;
		private List<String> labelList;
		private String chartIntroStr;
		private int numElements = 0;
		
		public List<Integer> getValues() {
			return values;
		}
		
		public List<String> getLabels() {
			return labelList;
		}
	}
	
	/** the logging object used in this class **/
	private static final Logger log_ = Tracing.createLoggerFor(StatisticDisplayController.class);

	private static final String CLICK_NODE_ACTION = "clicknodeaction";
	
	public static final String CLICK_TOTAL_ACTION = "clicktotalaction";

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

	private TableController tableCtr;
	private TableController tableController;

	private VelocityContainer statisticVc;

	private Translator headerTranslator;
	
	public StatisticDisplayController(UserRequest ureq, WindowControl windowControl, ICourse course, IStatisticManager statisticManager) {
		super(ureq, windowControl);

		addLoggingResourceable(LoggingResourceable.wrap(course));
		addLoggingResourceable(LoggingResourceable.wrapNonOlatResource(StringResourceableType.statisticManager, "", statisticManager.getClass().getSimpleName()));
		
		if (course==null) {
			throw new IllegalArgumentException("Course must not be null");
		}
		this.course = course;
		this.statisticManager = statisticManager;
		this.headerTranslator = Util.createPackageTranslator(statisticManager.getClass(), ureq.getLocale());

		// statistic.html is under org.olat.course.statistic - no matter who subclasses BaseStatisticDisplayController
		setVelocityRoot(Util.getPackageVelocityRoot(StatisticDisplayController.class));
		setTranslator(Util.createPackageTranslator(statisticManager.getClass(), ureq.getLocale(), Util.createPackageTranslator(StatisticDisplayController.class, ureq.getLocale())));
		
		putInitialPanel(createInitialComponent(ureq));
	}
	
	protected Component createInitialComponent(UserRequest ureq) {
		statisticVc = createVelocityContainer("statistic");
		recreateTableController(ureq);
		return statisticVc;
	}

	protected void recreateTableController(UserRequest ureq) {
		StatisticResult result = recalculateStatisticResult(ureq);
		tableCtr = createTableController(ureq, result);
		statisticVc.put("statisticResult", tableCtr.getInitialComponent());
		statisticVc.contextPut("hasChart", Boolean.FALSE);

		Graph graph = calculateNodeGraph(result.getRowCount()-1);
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
		
		IndentedStatisticNodeRenderer indentedNodeRenderer = new IndentedStatisticNodeRenderer(Util.createPackageTranslator(statisticManager.getClass(), ureq.getLocale()));
		indentedNodeRenderer.setSimpleRenderingOnExport(true);
		CustomRenderColumnDescriptor nodeCD = new CustomRenderColumnDescriptor("stat.table.header.node", 0, 
				CLICK_NODE_ACTION, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, indentedNodeRenderer) {
			@Override
			public int compareTo(int rowa, int rowb) {
				// order by original row order
				return Integer.valueOf(rowa).compareTo(rowb);
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
		
		CustomRenderColumnDescriptor columnDescriptor = new CustomRenderColumnDescriptor("stat.table.header.total",
				column, StatisticDisplayController.CLICK_TOTAL_ACTION + column, ureq.getLocale(),
				ColumnDescriptor.ALIGNMENT_RIGHT, new TotalColumnRenderer()) {
			@Override
			public String getAction(int row) {
				if (row == table.getTableDataModel().getRowCount() - 1) {
					return super.getAction(row);
				}
				return null;
			}

		};
		columnDescriptor.setHeaderAlignment(ColumnDescriptor.ALIGNMENT_RIGHT);
		tableController.addColumnDescriptor(columnDescriptor);
		
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
		if (source==tableCtr && event instanceof TableEvent) {
			TableEvent tableEvent = (TableEvent)event;
			if (CLICK_NODE_ACTION.equals(tableEvent.getActionId())) {
				int rowId = tableEvent.getRowId();
				Graph graph = calculateNodeGraph(rowId);
				generateCharts(graph);
			} else if (tableEvent.getActionId().startsWith(CLICK_TOTAL_ACTION)) {
				try{
					int columnId = Integer.parseInt(tableEvent.getActionId().substring(CLICK_TOTAL_ACTION.length()));
					Graph graph = calculateTotalGraph(columnId);
					generateCharts(graph);
				} catch(NumberFormatException e) {
					log_.warn("event: Could not convert event into columnId for rendering graph: "+tableEvent.getActionId());
				}
			}
		}
	}
	
	private Graph calculateNodeGraph(int rowId) {
		
		Object o = tableCtr.getTableDataModel().getValueAt(rowId, 0);
		String selectionInfo = "";
		if (o instanceof Map) {
			Map map = (Map)o;
			CourseNode node = (CourseNode) map.get(StatisticResult.KEY_NODE);
			ThreadLocalUserActivityLogger.log(StatisticLoggingAction.VIEW_NODE_STATISTIC, getClass(), 
					LoggingResourceable.wrap(node),
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.statisticType, "", STATISTIC_TYPE_VIEW_NODE_STATISTIC));
			String shortTitle = StringHelper.escapeHtml((String) map.get(AssessmentHelper.KEY_TITLE_SHORT));
			selectionInfo = getTranslator().translate("statistic.chart.selectioninfo.node", new String[] { shortTitle });
		} else {
			ThreadLocalUserActivityLogger.log(StatisticLoggingAction.VIEW_TOTAL_OF_NODES_STATISTIC, getClass(), 
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.statisticType, "", STATISTIC_TYPE_VIEW_TOTAL_OF_NODES_STATISTIC));
			selectionInfo = getTranslator().translate("statistic.chart.selectioninfo.total");
		}
		String chartIntroStr = headerTranslator.translate("statistic.chart.intro", new String[] { selectionInfo });
		
		StringBuilder chd = new StringBuilder(4096);
		List<Integer> values = new ArrayList<>();

		int max = 10;
		int columnCnt = tableCtr.getTableDataModel().getColumnCount();
		List<String> labelList = new LinkedList<>();
		for(int column=1/*we ignore the node itself*/; column<columnCnt-1/*we ignore the total*/; column++) {
			Object cellValue = tableCtr.getTableDataModel().getValueAt(rowId, column);
			Integer v = 0;
			if (cellValue instanceof Integer) {
				v = (Integer)cellValue;
			}
			max = Math.max(max, v);
			if (chd.length()!=0) {
				chd.append(",");
			}
			chd.append(v);
			values.add(v);
			
			ColumnDescriptor cd = tableCtr.getColumnDescriptor(column);
			String headerKey = cd.getHeaderKey();
			if (cd.translateHeaderKey()) {
				headerKey = headerTranslator.translate(headerKey);
			}
			labelList.add(headerKey);
		}
		Graph result = new Graph();
		result.values = values;
		result.labelList = labelList;
		result.chartIntroStr = chartIntroStr;
		result.numElements = columnCnt-2;
		return result;
	}

	private Graph calculateTotalGraph(int columnId) {
		ColumnDescriptor cd = tableCtr.getColumnDescriptor(columnId);
		String headerKey = cd.getHeaderKey();
		if (cd.translateHeaderKey()) {
			headerKey = headerTranslator.translate(headerKey);
		}
		String selectionInfo = headerKey;
		String chartIntroStr;
		if (columnId==tableCtr.getTableDataModel().getColumnCount()-1) {
			ThreadLocalUserActivityLogger.log(StatisticLoggingAction.VIEW_TOTAL_TOTAL_STATISTIC, getClass(), 
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.statisticType, "", STATISTIC_TYPE_VIEW_TOTAL_TOTAL_STATISTIC));
			chartIntroStr = headerTranslator.translate("statistic.chart.pernode.total.intro");
		} else {
			ThreadLocalUserActivityLogger.log(StatisticLoggingAction.VIEW_TOTAL_BY_VALUE_STATISTIC, getClass(), 
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.statisticType, "", STATISTIC_TYPE_VIEW_TOTAL_BY_VALUE_STATISTIC),
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.statisticColumn, "", selectionInfo));
			chartIntroStr = headerTranslator.translate("statistic.chart.pernode.intro", new String[] { selectionInfo });
		}
		
		StringBuilder chd = new StringBuilder(4096);

		int max = 10;
		
		List<String> labelList = new LinkedList<>();
		for(int row=0; row<tableCtr.getTableDataModel().getRowCount()-1; row++) {
			Object cellValue = tableCtr.getTableDataModel().getValueAt(row, columnId);
			Integer v = 0;
			if (cellValue instanceof Integer) {
				v = (Integer)cellValue;
			}
			max = Math.max(max, v);
			if (chd.length()!=0) {
				chd.append(",");
			}
			chd.append(v);
			
			Map m = (Map)tableCtr.getTableDataModel().getValueAt(row, 0);
			headerKey = "n/a";
			if (m!=null) {
				headerKey = (String) m.get(AssessmentHelper.KEY_TITLE_SHORT);
			}
			
			labelList.add(headerKey);
		}
		Graph result = new Graph();
		result.labelList = labelList;
		result.chartIntroStr = chartIntroStr;
		result.numElements = tableCtr.getTableDataModel().getRowCount()-1;
		return result;
	}
	private void generateCharts(Graph graph) {
		statisticVc.contextPut("hasChart", Boolean.FALSE);
		statisticVc.contextPut("hasChartError", Boolean.FALSE);
		if (graph==null || graph.numElements==0) {
			Component ic = getInitialComponent();
			if (ic!=null) {
				ic.setDirty(true);
			}
			return;
		}
		try{
			statisticVc.contextPut("chartAlt", getTranslator().translate("chart.alt"));
			statisticVc.contextPut("chartIntro", graph.chartIntroStr);
			statisticVc.contextPut("hasChart", Boolean.TRUE);
			statisticVc.contextPut("hasChartError", Boolean.FALSE);
			
			BarChartComponent chartCmp = new BarChartComponent("stats");
			List<String> labels = graph.getLabels();
			List<Integer> values = graph.getValues();
			BarSeries serie = new BarSeries();
			for(int i=0; i<labels.size(); i++) {
				double value = values.get(i).doubleValue();
				String category = labels.get(i);
				serie.add(value, category);
			}
			chartCmp.addSeries(serie);
			statisticVc.put("chart", chartCmp);
			
		} catch(RuntimeException re) {
			log_.warn("generateCharts: RuntimeException during chart generation: "+re, re);
		}
		Component ic = getInitialComponent();
		if (ic!=null) {
			ic.setDirty(true);
		}
	}
}