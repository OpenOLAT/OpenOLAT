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
package org.olat.ims.qti21.ui.statistics;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.statistic.StatisticResourceNode;
import org.olat.course.statistic.StatisticResourceResult;
import org.olat.ims.qti.editor.tree.ItemNode;
import org.olat.ims.qti.editor.tree.SectionNode;
import org.olat.ims.qti.statistics.QTIType;
import org.olat.ims.qti.statistics.model.StatisticAssessment;
import org.olat.ims.qti21.QTI21StatisticsManager;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * 
 * Initial date: 15.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21StatisticResourceResult implements StatisticResourceResult {
	
	private StatisticAssessment statisticAssessment;
	private QTI21StatisticSearchParams searchParams;
	
	private final RepositoryEntry testEntry;
	private final RepositoryEntry courseEntry;
	private final QTICourseNode courseNode;
	
	private final QTI21StatisticsManager qtiStatisticsManager;

	public QTI21StatisticResourceResult(RepositoryEntry testEntry, RepositoryEntry courseEntry,
			QTICourseNode courseNode, QTI21StatisticSearchParams searchParams) {
		
		this.courseNode = courseNode;
		this.testEntry = testEntry;
		this.courseEntry = courseEntry;
		this.searchParams = searchParams;

		qtiStatisticsManager = CoreSpringFactory.getImpl(QTI21StatisticsManager.class);
	}
	
	public QTIType getType() {
		return QTIType.qtiworks;
	}

	public RepositoryEntry getCourseEntry() {
		return courseEntry;
	}

	public QTICourseNode getTestCourseNode() {
		return courseNode;
	}
	
	public RepositoryEntry getTestEntry() {
		return testEntry;
	}
	
	public QTI21StatisticSearchParams getSearchParams() {
		return searchParams;
	}
	
	public StatisticAssessment getQTIStatisticAssessment() {
		if(statisticAssessment == null) {
			statisticAssessment = qtiStatisticsManager.getAssessmentStatistics(searchParams);
		}
		return statisticAssessment;
	}

	@Override
	public TreeModel getSubTreeModel() {
		GenericTreeModel subTreeModel = new GenericTreeModel();
		StatisticResourceNode rootTreeNode = new StatisticResourceNode(courseNode, this);
		subTreeModel.setRootNode(rootTreeNode);
		return subTreeModel;
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, TreeNode selectedNode) {
		return getController(ureq, wControl, stackPanel, selectedNode, false);
	}
	
	public Controller getController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			TreeNode selectedNode, boolean printMode) {	
		if(selectedNode instanceof StatisticResourceNode) {
			return createAssessmentController(ureq, wControl, stackPanel, printMode);
		} else if(selectedNode instanceof SectionNode) {
			return createAssessmentController(ureq, wControl, stackPanel, printMode);	
		} else if(selectedNode instanceof ItemNode) {
			
		}
		return null;
	}
	
	private Controller createAssessmentController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			boolean printMode) {
		Controller ctrl = new QTI21AssessmentTestStatisticsController(ureq, wControl, this, printMode);
		CourseNodeConfiguration cnConfig = CourseNodeFactory.getInstance()
				.getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType());
		String iconCssClass = cnConfig.getIconCSSClass();
		return TitledWrapperHelper.getWrapper(ureq, wControl, ctrl, courseNode, iconCssClass);
	}
}
