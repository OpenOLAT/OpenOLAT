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
import java.net.URISyntaxException;
import java.util.UUID;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
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
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentTestFactory;
import org.olat.ims.qti21.model.xml.ManifestPackage;
import org.olat.ims.qti21.model.xml.interactions.EssayAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.KPrimAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MultipleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.olat.ims.qti21.ui.editor.events.AssessmentSectionEvent;
import org.olat.ims.qti21.ui.editor.events.AssessmentTestEvent;
import org.olat.ims.qti21.ui.editor.events.AssessmentTestPartEvent;
import org.olat.imscp.xml.manifest.ManifestType;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoryEntryRuntimeController.ToolbarAware;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AbstractPart;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

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
	private final Dropdown addItemTools;
	private final Link newSectionLink, newSingleChoiceLink, newMultipleChoiceLink, newKPrimLink, newEssayLink;
	private final TooledStackedPanel toolbar;
	private final VelocityContainer mainVC;

	private Controller currentEditorCtrl;
	private final LayoutMain3ColsController columnLayoutCtr;
	
	private final File unzippedDirRoot;
	private final RepositoryEntry testEntry;
	private ManifestType manifest;
	private ResolvedAssessmentTest resolvedAssessmentTest;
	
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
		updateTreeModel();
		manifest = ManifestPackage.read(new File(unzippedDirRoot, "imsmanifest.xml"));
		
		//default buttons
		saveLink = LinkFactory.createToolLink("serialize", translate("serialize"), this, "o_icon_save");
		saveLink.setDomReplacementWrapperRequired(false);
		
		//add elements
		addItemTools = new Dropdown("editTools", "new.elements", false, getTranslator());
		addItemTools.setIconCSS("o_icon o_icon-fw o_icon_add");
		toolbar.addTool(addItemTools, Align.left);
		
		newSectionLink = LinkFactory.createToolLink("new.section", translate("new.section"), this, "o_mi_qtisection");
		newSectionLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newSectionLink);
		
		//items
		newSingleChoiceLink = LinkFactory.createToolLink("new.sc", translate("new.sc"), this, "o_mi_qtisc");
		newSingleChoiceLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newSingleChoiceLink);
		newMultipleChoiceLink = LinkFactory.createToolLink("new.mc", translate("new.mc"), this, "o_mi_qtimc");
		newMultipleChoiceLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newMultipleChoiceLink);
		newKPrimLink = LinkFactory.createToolLink("new.kprim", translate("new.kprim"), this, "o_mi_qtikprim");
		newKPrimLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newKPrimLink);
		newEssayLink = LinkFactory.createToolLink("new.essay", translate("new.essay"), this, "o_mi_qtiessay");
		newEssayLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newEssayLink);

		// main layout
		mainVC = createVelocityContainer("assessment_test_composer");
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, mainVC, "at" + testEntry.getKey());			
		columnLayoutCtr.addCssClassToMain("o_editor");
		listenTo(columnLayoutCtr);
		putInitialPanel(columnLayoutCtr.getInitialComponent());
		
		// init
		TreeNode selectedNode = doOpenFirstItem();
		if(selectedNode == null) {
			selectedNode = menuTree.getTreeModel().getRootNode();
		}
		partEditorFactory(ureq, selectedNode);
	}
	
	private void updateTreeModel() {
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot);
		menuTree.setTreeModel(new AssessmentTestEditorAndComposerTreeModel(resolvedAssessmentTest));
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void initToolbar() {
		toolbar.addTool(addItemTools, Align.left);
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
		} else if(event instanceof AssessmentTestPartEvent) {
			AssessmentTestPartEvent atpe = (AssessmentTestPartEvent)event;
			if(atpe == AssessmentTestPartEvent.ASSESSMENT_TEST_PART_CHANGED_EVENT) {
				doSaveAssessmentTest();
			}
		} else if(event instanceof AssessmentSectionEvent) {
			AssessmentSectionEvent ase = (AssessmentSectionEvent)event;
			if(AssessmentSectionEvent.ASSESSMENT_SECTION_CHANGED.equals(ase.getCommand())) {
				doSaveAssessmentTest();
				doUpdate(ase.getSection().getIdentifier(), ase.getSection().getTitle());
			}
		} else if(event instanceof AssessmentItemEvent) {
			AssessmentItemEvent aie = (AssessmentItemEvent)event;
			if(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED.equals(aie.getCommand())) {
				doUpdate(aie.getAssessmentItemRef().getIdentifier(), aie.getAssessmentItem().getTitle());
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
		} else if(newSectionLink == source) {
			doNewSection(ureq, menuTree.getSelectedNode());
		} else if(newSingleChoiceLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new SingleChoiceAssessmentItemBuilder(qtiService.qtiSerializer()));
		} else if(newMultipleChoiceLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new MultipleChoiceAssessmentItemBuilder(qtiService.qtiSerializer()));
		} else if(newKPrimLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new KPrimAssessmentItemBuilder(qtiService.qtiSerializer()));
		} else if(newEssayLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new EssayAssessmentItemBuilder(qtiService.qtiSerializer()));
		}
	}
	
	private TreeNode doOpenFirstItem() {
		TreeNode node = menuTree.getTreeModel().getRootNode();
		if(node.getChildCount() > 0) {
			return doOpenFirstItem((TreeNode)node.getChildAt(0));
		}
		return node;
	}
	
	private TreeNode doOpenFirstItem(TreeNode node) {
		if(node.getUserObject() instanceof AssessmentItemRef) {
			menuTree.setSelectedNode(node);
			menuTree.open(node);
			return node;
		}
		if(node.getChildCount() > 0) {
			return doOpenFirstItem((TreeNode)node.getChildAt(0));
		}
		return null;
	}
	
	private void doNewSection(UserRequest ureq, TreeNode selectedNode) {
		AbstractPart parentPart;
		TreeNode sectionNode = getNearestSection(selectedNode);
		if(sectionNode != null) {
			AssessmentSection section = (AssessmentSection)sectionNode.getUserObject();
			parentPart = section.getParent();
		} else if(selectedNode.getUserObject() instanceof TestPart) {
			parentPart = (TestPart)selectedNode.getUserObject();
		} else {
			showWarning("error.cannot.create.section");
			return;
		}

		AssessmentSection newSection;
		if(parentPart instanceof TestPart) {
			newSection = AssessmentTestFactory.createAssessmentSection((TestPart)parentPart);
		} else if(parentPart instanceof AssessmentSection) {
			newSection = AssessmentTestFactory.createAssessmentSection((AssessmentSection)parentPart);
		} else {
			showWarning("error.cannot.create.section");
			return;
		}
	
		//save the test
		URI testUri = resolvedAssessmentTest.getTestLookup().getSystemId();
		File testFile = new File(testUri);
		qtiService.updateAssesmentObject(testFile, resolvedAssessmentTest);

		//reload the test
		updateTreeModel();
		
		TreeNode newSectionNode = menuTree.getTreeModel().getNodeById(newSection.getIdentifier().toString());
		menuTree.setSelectedNode(newSectionNode);
		menuTree.open(newSectionNode);

		partEditorFactory(ureq, newSectionNode);
	}
	
	/**
	 * The method create a simple single choice, save the assessment item
	 * and append it to the test, update the manifest file.
	 * 
	 * @param ureq
	 * @param selectedNode
	 */
	private void doNewAssessmentItem(UserRequest ureq, TreeNode selectedNode, AssessmentItemBuilder itemBuilder) {
		try {
			TreeNode sectionNode = getNearestSection(selectedNode);
			AssessmentSection section = (AssessmentSection)sectionNode.getUserObject();
			
			AssessmentItemRef itemRef = new AssessmentItemRef(section);
			String itemId = "sc" + UUID.randomUUID();
			itemRef.setIdentifier(Identifier.parseString(itemId));
			File itemFile = new File(unzippedDirRoot, itemId + ".xml");
			itemRef.setHref(new URI(itemFile.getName()));
			section.getSectionParts().add(itemRef);
			
			AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();
			qtiService.persistAssessmentObject(itemFile, assessmentItem);
			
			URI testUri = resolvedAssessmentTest.getTestLookup().getSystemId();
			File testFile = new File(testUri);
			qtiService.updateAssesmentObject(testFile, resolvedAssessmentTest);

			ManifestPackage.appendAssessmentItem(itemFile.getName(), manifest);
			ManifestPackage.write(manifest, new File(unzippedDirRoot, "imsmanifest.xml"));
			
			updateTreeModel();
			
			TreeNode newItemNode = menuTree.getTreeModel().getNodeById(itemId);
			menuTree.setSelectedNode(newItemNode);
			menuTree.open(newItemNode);

			partEditorFactory(ureq, newItemNode);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	private TreeNode getNearestSection(TreeNode node) {
		if(node.getUserObject() instanceof AssessmentSection) {
			return node;
		}
		if(node.getUserObject() instanceof AssessmentItemRef) {
			return (TreeNode)node.getParent();
		}
		if(node.getUserObject() instanceof TestPart) {
			return (TreeNode)node.getChildAt(0);
		}
		return null;
	}
	
	private void doSaveAssessmentTest() {
		URI testURI = resolvedAssessmentTest.getTestLookup().getSystemId();
		File testFile = new File(testURI);
		qtiService.updateAssesmentObject(testFile, resolvedAssessmentTest);
	}
	
	private void doUpdate(Identifier identifier, String newTitle) {
		TreeNode node = menuTree.getTreeModel()
				.getNodeById(identifier.toString());
		if(node instanceof GenericTreeNode) {
			GenericTreeNode itemNode = (GenericTreeNode)node;
			if(!newTitle.equals(itemNode.getTitle())) {
				itemNode.setTitle(newTitle);
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
			if(item.getItemLookup().getBadResourceException() != null) {
				currentEditorCtrl = new BadResourceController(ureq, getWindowControl(),
						item.getItemLookup().getBadResourceException(), unzippedDirRoot, itemRef.getHref());
			} else {
				currentEditorCtrl = new AssessmentItemEditorController(ureq, getWindowControl(), testEntry,
						item, itemRef, unzippedDirRoot);
			}
		}
		
		if(currentEditorCtrl != null) {
			listenTo(currentEditorCtrl);
			mainVC.put("content", currentEditorCtrl.getInitialComponent());
		} else {
			mainVC.put("content", new Panel("empty"));
		}
	}
}