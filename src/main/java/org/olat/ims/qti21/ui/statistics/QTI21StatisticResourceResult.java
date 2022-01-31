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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.statistic.StatisticResourceNode;
import org.olat.course.statistic.StatisticResourceResult;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.QTI21StatisticsManager;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.ims.qti21.model.statistics.StatisticAssessment;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.repository.RepositoryEntry;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.SectionPart;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.provision.BadResourceException;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

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
	private ResolvedAssessmentTest resolvedAssessmentTest;
	private final QTI21StatisticsSecurityCallback secCallback;
	
	private final QTI21Service qtiService;
	private final QTI21StatisticsManager qtiStatisticsManager;
	
	private boolean withFilter = true;

	public QTI21StatisticResourceResult(RepositoryEntry testEntry, QTI21StatisticSearchParams searchParams,
			QTI21StatisticsSecurityCallback secCallback) {
		this(testEntry, null, null, searchParams, secCallback);
	}
	
	public QTI21StatisticResourceResult(RepositoryEntry testEntry, RepositoryEntry courseEntry,
			QTICourseNode courseNode, QTI21StatisticSearchParams searchParams,
			QTI21StatisticsSecurityCallback secCallback) {
		
		this.courseNode = courseNode;
		this.testEntry = testEntry;
		this.courseEntry = courseEntry;
		this.searchParams = searchParams;
		this.secCallback = secCallback;

		qtiService = CoreSpringFactory.getImpl(QTI21Service.class);
		qtiStatisticsManager = CoreSpringFactory.getImpl(QTI21StatisticsManager.class);
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
	
	public ResolvedAssessmentTest getResolvedAssessmentTest() {
		return resolvedAssessmentTest;
	}
	
	public List<AssessmentItem> getAssessmentItems() {
		List<AssessmentItemRef> itemRefs = resolvedAssessmentTest.getAssessmentItemRefs();
		List<AssessmentItem> items = new ArrayList<>(itemRefs.size());
		for(AssessmentItemRef itemRef:itemRefs) {
			AssessmentItem item = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef).getRootNodeLookup().extractIfSuccessful();
			items.add(item);
		}
		return items;
	}
	
	public QTI21StatisticSearchParams getSearchParams() {
		return searchParams;
	}
	
	public StatisticAssessment getQTIStatisticAssessment() {
		if(statisticAssessment == null) {
			Double cutValue = getCutValue();
			statisticAssessment = qtiStatisticsManager.getAssessmentStatistics(searchParams, cutValue);
		}
		return statisticAssessment;
	}
	
	public Double getCutValue() {
		AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
		return QtiNodesExtractor.extractCutValue(assessmentTest);
	}
	
	public File getAssessmentItemFile(AssessmentItemRef itemRef) {
		URI itemUri = resolvedAssessmentTest.getSystemIdByItemRefMap().get(itemRef);
		return new File(itemUri);
	}
	
	public File getUnzippedDirectory() {
		return FileResourceManager.getInstance().unzipFileResource(testEntry.getOlatResource());
	}
	
	public boolean canViewAnonymousUsers() {
		return secCallback.canViewAnonymousUsers();
	}
	
	public boolean canViewNonParticipantUsers() {
		return secCallback.canViewNonParticipantUsers();
	}
	
	public boolean isViewAnonymousUsers() {
		return searchParams.isViewAnonymUsers();
	}
	
	public void setViewAnonymousUsers(boolean view) {
		if(view != searchParams.isViewAnonymUsers()) {
			statisticAssessment = null;
		}
		searchParams.setViewAnonymUsers(view);
	}
	
	public boolean isViewNonParticipantUsers() {
		return searchParams.isViewAllUsers();
	}
	
	public void setViewNonPaticipantUsers(boolean view) {
		if(view != searchParams.isViewAllUsers()) {
			statisticAssessment = null;
		}
		searchParams.setViewAllUsers(view);
	}
	
	public boolean isWithFilter() {
		return withFilter;
	}

	public void setWithFilter(boolean withFilter) {
		this.withFilter = withFilter;
	}

	/**
	 * Return the tree model for a test learn resource.
	 * 
	 * @return
	 */
	public TreeModel getTreeModel() {
		GenericTreeModel treeModel = new GenericTreeModel();
		GenericTreeNode rootTreeNode = new GenericTreeNode();
		treeModel.setRootNode(rootTreeNode);
		
		FileResourceManager frm = FileResourceManager.getInstance();
		File unzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false, false);
		AssessmentTest test = resolvedAssessmentTest.getTestLookup().getRootNodeHolder().getRootNode();
		
		rootTreeNode.setTitle(test.getTitle());
		rootTreeNode.setUserObject(test);
		rootTreeNode.setIconCssClass("o_icon o_icon-lg o_qtiassessment_icon");
		buildRecursively(test, rootTreeNode);
		return treeModel;
	}

	/**
	 * Return the tree model for a course and a specific test.
	 * 
	 */
	@Override
	public TreeModel getSubTreeModel() {
		GenericTreeModel subTreeModel = new GenericTreeModel();
		StatisticResourceNode rootTreeNode = new StatisticResourceNode(courseNode, this);
		subTreeModel.setRootNode(rootTreeNode);
		
		FileResourceManager frm = FileResourceManager.getInstance();
		File unzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false, false);
		
		AssessmentTest test = resolvedAssessmentTest.getTestLookup().getRootNodeHolder().getRootNode();
		buildRecursively(test, rootTreeNode);
		return subTreeModel;
	}
	
	private void buildRecursively(AssessmentTest test, GenericTreeNode rootTreeNode) {
		//list all test parts
		List<TestPart> parts = test.getTestParts();
		if(parts.size() == 1) {
			TreeNode firstItem = null;
			List<AssessmentSection> sections = test.getTestParts().get(0).getAssessmentSections();
			for(AssessmentSection section:sections) {
				TreeNode itemNode = buildRecursively(section, rootTreeNode);
				if(firstItem == null) {
					firstItem = itemNode;
				}
			}
		} else {
			int counter = 0;
			for(TestPart part:parts) {
				buildRecursively(part, ++counter, rootTreeNode);
			}
		}
	}
	
	private void buildRecursively(TestPart part, int pos, TreeNode parentNode) {
		GenericTreeNode partNode = new GenericTreeNode();
		partNode.setTitle(pos + ". Test part");
		partNode.setIconCssClass("o_icon o_qtiassessment_icon");
		partNode.setUserObject(part);
		parentNode.addChild(partNode);

		TreeNode firstItem = null;
		List<AssessmentSection> sections = part.getAssessmentSections();
		for(AssessmentSection section:sections) {
			TreeNode itemNode = buildRecursively(section, partNode);
			if(firstItem == null) {
				firstItem = itemNode;
			}
		}
		partNode.setDelegate(firstItem);
	}
	
	private TreeNode buildRecursively(AssessmentSection section, TreeNode parentNode) {
		GenericTreeNode sectionNode = new GenericTreeNode();
		sectionNode.setTitle(section.getTitle());
		sectionNode.setIconCssClass("o_icon o_mi_qtisection");
		sectionNode.setUserObject(section);
		parentNode.addChild(sectionNode);
		
		TreeNode firstItem = null;
		for(SectionPart part: section.getSectionParts()) {
			TreeNode itemNode = null;
			if(part instanceof AssessmentItemRef) {
				itemNode = buildRecursively((AssessmentItemRef)part, sectionNode);
				
			} else if(part instanceof AssessmentSection) {
				itemNode = buildRecursively((AssessmentSection) part, sectionNode);
			}
			if(firstItem == null) {
				firstItem = itemNode;
			}
		}
		
		sectionNode.setDelegate(firstItem);
		return firstItem;
	}
	
	private TreeNode buildRecursively(AssessmentItemRef itemRef, TreeNode parentNode) {
		GenericTreeNode itemNode = new GenericTreeNode();
		
		ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
		BadResourceException ex = resolvedAssessmentItem.getItemLookup().getBadResourceException();
		if(ex == null) {
			AssessmentItem assessmentItem = resolvedAssessmentItem.getItemLookup().getRootNodeHolder().getRootNode();
			itemNode.setTitle(assessmentItem.getTitle());
			
			QTI21QuestionType type = QTI21QuestionType.getTypeRelax(assessmentItem);
			if(type != null) {
				itemNode.setIconCssClass("o_icon ".concat(type.getCssClass()));
			} else {
				itemNode.setIconCssClass("o_icon o_mi_qtiunkown");
			}
			itemNode.setUserObject(itemRef);
			parentNode.addChild(itemNode);
		}

		return itemNode;
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, TreeNode selectedNode) {
		return getController(ureq, wControl, stackPanel, selectedNode, false);
	}
	
	public Controller getController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			TreeNode selectedNode, boolean printMode) {	
		if(selectedNode instanceof StatisticResourceNode) {
			return createAssessmentController(ureq, wControl, stackPanel, printMode);
		} else {
			Object uobject = selectedNode.getUserObject();
			
			if(uobject instanceof AssessmentItemRef) {
				TreeNode parentNode = (TreeNode)selectedNode.getParent();
				String sectionTitle = parentNode.getTitle();
				return createAssessmentItemController(ureq, wControl,
						(AssessmentItemRef)uobject, sectionTitle, printMode);
			} else if(uobject instanceof AssessmentTest) {
				return createAssessmentController(ureq, wControl, stackPanel, printMode);
			}
		}
		return null;
	}
	
	private Controller createAssessmentController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, boolean printMode) {
		Controller ctrl = new QTI21AssessmentTestStatisticsController(ureq, wControl, stackPanel, this, withFilter, printMode, true);
		if(courseNode != null) {
			CourseNodeConfiguration cnConfig = CourseNodeFactory.getInstance()
					.getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType());
			String iconCssClass = cnConfig.getIconCSSClass();
			ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, ctrl, courseNode, iconCssClass);
		}
		return ctrl;
	}
	
	private Controller createAssessmentItemController(UserRequest ureq, WindowControl wControl,
			AssessmentItemRef assessmentItemRef, String sectionTitle, boolean printMode) {
		ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(assessmentItemRef);
		if(resolvedAssessmentItem == null || resolvedAssessmentItem.getItemLookup() == null) {
			Translator translator = Util.createPackageTranslator(AssessmentTestDisplayController.class, ureq.getLocale());
			String text = translator.translate("error.assessment.item.missing");
			Controller errorCtrl = MessageUIFactory.createErrorMessage(ureq, wControl, "", text);
			return TitledWrapperHelper.getWrapper(ureq, wControl, errorCtrl, courseNode, "o_icon_error");
		}
		
		Controller ctrl = new QTI21AssessmentItemStatisticsController(ureq, wControl,
				assessmentItemRef, resolvedAssessmentItem, sectionTitle, this, withFilter, printMode);
		String iconCssClass = "o_mi_qtisc";
		if(courseNode != null) {
			ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, ctrl, courseNode, iconCssClass);
		}
		return ctrl;
	}
}
