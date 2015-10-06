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
package org.olat.ims.qti21.ui.editor;

import java.io.File;
import java.net.URI;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.VetoableCloseController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.util.Util;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoryEntryRuntimeController.ToolbarAware;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * Assessment test editor and composer.
 * 
 * 
 * Initial date: 22.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestComposerController extends MainLayoutBasicController implements VetoableCloseController, ToolbarAware {
	
	
	private final MenuTree menuTree;
	private final Link saveLink;
	private final TooledStackedPanel toolbar;
	private final VelocityContainer mainVC;

	private Controller currentEditorCtrl;
	private final LayoutMain3ColsController columnLayoutCtr;
	
	private final File unzippedDirRoot;
	private final RepositoryEntry testEntry;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	
	private final boolean restrictedEdit;
	
	@Autowired
	private QTI21Service qtiService;
	
	public AssessmentTestComposerController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar,
			RepositoryEntry testEntry) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentTestDisplayController.class, getLocale(), getTranslator()));
		
		this.toolbar = toolbar;
		this.testEntry = testEntry;
		restrictedEdit = false;
		
		// test structure
		menuTree = new MenuTree("atTree");
		menuTree.setExpandSelectedNode(false);
		menuTree.setDragEnabled(true);
		menuTree.setDropEnabled(true);
		menuTree.setDropSiblingEnabled(true);	
		menuTree.setDndAcceptJSMethod("treeAcceptDrop_notWithChildren");	
		menuTree.setElementCssClass("o_assessment_test_editor_menu");
		menuTree.addListener(this);

		FileResourceManager frm = FileResourceManager.getInstance();
		unzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentObject(unzippedDirRoot);
		menuTree.setTreeModel(new AssessmentTestEditorAndComposerTreeModel(resolvedAssessmentTest));
		
		//default buttons
		saveLink = LinkFactory.createToolLink("serialize", translate("serialize"), this, "o_icon_save");
		saveLink.setDomReplacementWrapperRequired(false);
		
		// main layout
		mainVC = createVelocityContainer("assessment_test_composer");
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, mainVC, "at" + testEntry.getKey());			
		columnLayoutCtr.addCssClassToMain("o_editor");
		listenTo(columnLayoutCtr);
		putInitialPanel(columnLayoutCtr.getInitialComponent());
		
		// init
		partEditorFactory(ureq, menuTree.getTreeModel().getRootNode());
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void initToolbar() {
		toolbar.addTool(saveLink, Align.left);
	}

	@Override
	public boolean requestForClose(UserRequest ureq) {
		return true;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof AssessmentTestEvent) {
			AssessmentTestEvent ate = (AssessmentTestEvent)event;
			if(ate == AssessmentTestEvent.ASSESSMENT_TEST_CHANGED_EVENT) {
				doSaveAssessmentTest();
			}
		} else if(event instanceof AssessmentSectionEvent) {
			AssessmentSectionEvent ase = (AssessmentSectionEvent)event;
			if(AssessmentSectionEvent.ASSESSMENT_SECTION_CHANGED.equals(ase.getCommand())) {
				doSaveAssessmentTest();
				doUpdate(ase.getSection());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(menuTree == source) {
			if (event instanceof TreeEvent) {
				TreeEvent te = (TreeEvent)event;
				String cmd = te.getCommand();
				if (MenuTree.COMMAND_TREENODE_CLICKED.equals(cmd)) {
					TreeNode selectedNode = menuTree.getTreeModel()
							.getNodeById(te.getNodeId());
					partEditorFactory(ureq, selectedNode);
				}
			}
		} else if(saveLink == source) {
			doSaveAssessmentTest();
		}
	}
	
	private void doSaveAssessmentTest() {
		URI testURI = resolvedAssessmentTest.getTestLookup().getSystemId();
		File testFile = new File(testURI);
		qtiService.updateAssesmentObject(testFile, resolvedAssessmentTest);
	}
	
	private void doUpdate(AssessmentSection section) {
		TreeNode node = menuTree.getTreeModel()
				.getNodeById(section.getIdentifier().toString());
		if(node instanceof GenericTreeNode) {
			GenericTreeNode sectionNode = (GenericTreeNode)node;
			if(!section.getTitle().equals(sectionNode.getTitle())) {
				sectionNode.setTitle(section.getTitle());
				menuTree.setDirty(true);
			}
		}
	}
	
	private void partEditorFactory(UserRequest ureq, TreeNode selectedNode) {
		//remove old one
		if(currentEditorCtrl != null) {
			mainVC.remove(currentEditorCtrl.getInitialComponent());
			removeAsListenerAndDispose(currentEditorCtrl);
			currentEditorCtrl = null;
		}

		//fill with the new
		mainVC.contextPut("cssClass", selectedNode.getIconCssClass());
		mainVC.contextPut("identifier", selectedNode.getIdent());
		mainVC.contextPut("title", selectedNode.getTitle());

		Object uobject = selectedNode.getUserObject();
		if(uobject instanceof AssessmentTest) {
			currentEditorCtrl = new AssessmentTestEditorController(ureq, getWindowControl(), (AssessmentTest)uobject);
		} else if(uobject instanceof TestPart) {
			currentEditorCtrl = new AssessmentTestPartEditorController(ureq, getWindowControl(), (TestPart)uobject, restrictedEdit);
		} else if(uobject instanceof AssessmentSection) {
			currentEditorCtrl = new AssessmentSectionEditorController(ureq, getWindowControl(), (AssessmentSection)uobject, restrictedEdit);
		} else if(uobject instanceof AssessmentItemRef) {
			AssessmentItemRef itemRef = (AssessmentItemRef)uobject;
			ResolvedAssessmentItem item = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
			currentEditorCtrl = new AssessmentItemEditorController(ureq, getWindowControl(), testEntry, item, itemRef, unzippedDirRoot);
		}
		
		if(currentEditorCtrl != null) {
			listenTo(currentEditorCtrl);
			mainVC.put("content", currentEditorCtrl.getInitialComponent());
		} else {
			mainVC.put("content", new Panel("empty"));
		}
	}
}