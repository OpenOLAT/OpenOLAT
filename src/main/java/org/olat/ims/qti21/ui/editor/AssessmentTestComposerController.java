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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentTestFactory;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.model.xml.interactions.EssayAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.KPrimAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MultipleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.pool.QTI21QPoolServiceProvider;
import org.olat.ims.qti21.questionimport.AssessmentItemAndMetadata;
import org.olat.ims.qti21.questionimport.AssessmentItemsPackage;
import org.olat.ims.qti21.questionimport.ImportOptions;
import org.olat.ims.qti21.questionimport.QImport_1_InputStep;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.olat.ims.qti21.ui.editor.events.AssessmentSectionEvent;
import org.olat.ims.qti21.ui.editor.events.AssessmentTestEvent;
import org.olat.ims.qti21.ui.editor.events.AssessmentTestPartEvent;
import org.olat.imscp.xml.manifest.ResourceType;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.ui.SelectItemController;
import org.olat.modules.qpool.ui.events.QItemViewEvent;
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
	private final Link newSectionLink, newSingleChoiceLink, newMultipleChoiceLink, newKPrimLink,
		newFIBLink, newEssayLink;
	private final Link importFromPoolLink, importFromTableLink;
	private final TooledStackedPanel toolbar;
	private final VelocityContainer mainVC;

	private Controller currentEditorCtrl;
	private CloseableModalController cmc;
	private SelectItemController selectQItemCtrl;
	private StepsMainRunController importTableWizard;
	private final LayoutMain3ColsController columnLayoutCtr;
	
	private final File unzippedDirRoot;
	private final RepositoryEntry testEntry;
	private ManifestBuilder manifestBuilder;
	private ResolvedAssessmentTest resolvedAssessmentTest;
	
	private final boolean survey = false;
	private final boolean restrictedEdit;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private QTI21QPoolServiceProvider qti21QPoolServiceProvider;
	
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
		manifestBuilder = ManifestBuilder.read(new File(unzippedDirRoot, "imsmanifest.xml"));
		
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
		
		newFIBLink = LinkFactory.createToolLink("new.fib", translate("new.fib"), this, "o_mi_qtifib");
		newFIBLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newFIBLink);
		newEssayLink = LinkFactory.createToolLink("new.essay", translate("new.essay"), this, "o_mi_qtiessay");
		newEssayLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newEssayLink);
		
		addItemTools.addComponent(new Dropdown.Spacer("sep-import"));
		//import
		importFromPoolLink = LinkFactory.createToolLink("import.pool", translate("tools.import.qpool"), this, "o_mi_qpool_import");
		importFromPoolLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(importFromPoolLink);
		importFromTableLink = LinkFactory.createToolLink("import.table", translate("tools.import.table"), this, "o_mi_table_import");
		importFromTableLink.setIconLeftCSS("o_icon o_icon_table o_icon-fw");
		importFromTableLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(importFromTableLink);

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
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, true);
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
				doSaveManifest();
			}
		} else if(event instanceof AssessmentItemEvent) {
			AssessmentItemEvent aie = (AssessmentItemEvent)event;
			if(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED.equals(aie.getCommand())) {
				doUpdate(aie.getAssessmentItemRef().getIdentifier(), aie.getAssessmentItem().getTitle());
				doSaveManifest();
			} else if(AssessmentItemEvent.ASSESSMENT_ITEM_METADATA_CHANGED.equals(aie.getCommand())) {
				doSaveManifest();
			}
		} else if(selectQItemCtrl == source) {
			cmc.deactivate();
			cleanUp();
			
			if(event instanceof QItemViewEvent) {
				QItemViewEvent e = (QItemViewEvent)event;
				List<QuestionItemView> items = e.getItemList();
				doInsert(ureq, items);
			}
		}  else if(importTableWizard == source) {
			AssessmentItemsPackage importPackage = (AssessmentItemsPackage)importTableWizard.getRunContext().get("importPackage");
			getWindowControl().pop();
			cleanUp();
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doInsert(ureq, importPackage);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(selectQItemCtrl);
		removeAsListenerAndDispose(cmc);
		selectQItemCtrl = null;
		cmc = null;
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
		} else if(newFIBLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new FIBAssessmentItemBuilder(qtiService.qtiSerializer()));
		} else if(newEssayLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new EssayAssessmentItemBuilder(qtiService.qtiSerializer()));
		} else if(importFromPoolLink == source) {
			doSelectQItem(ureq);
		} else if(importFromTableLink == source) {
			doImportTable(ureq);
		}
	}
	
	private void doSelectQItem(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(selectQItemCtrl);
		
		selectQItemCtrl = new SelectItemController(ureq, getWindowControl(), QTI21Constants.QTI_21_FORMAT);
		listenTo(selectQItemCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), selectQItemCtrl.getInitialComponent(), true, translate("title.add") );
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doImportTable(UserRequest ureq) {
		removeAsListenerAndDispose(importTableWizard);

		final AssessmentItemsPackage importPackage = new AssessmentItemsPackage();
		final ImportOptions options = new ImportOptions();
		options.setShuffle(!survey);
		Step start = new QImport_1_InputStep(ureq, importPackage, options, null);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest uureq, WindowControl wControl, StepsRunContext runContext) {
				runContext.put("importPackage", importPackage);
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		importTableWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("tools.import.table"), "o_mi_table_import_wizard");
		listenTo(importTableWizard);
		getWindowControl().pushAsModalDialog(importTableWizard.getInitialComponent());
	}
	
	private void doInsert(UserRequest ureq, List<QuestionItemView> items) {
		TreeNode selectedNode = menuTree.getSelectedNode();
		TreeNode sectionNode = getNearestSection(selectedNode);
		
		String firstItemId = null;
		try {
			AssessmentSection section = (AssessmentSection)sectionNode.getUserObject();
			for(QuestionItemView item:items) {
				AssessmentItem assessmentItem = qti21QPoolServiceProvider.exportToQTIEditor(item, getLocale(), unzippedDirRoot);
				AssessmentItemRef itemRef = doInsert(section, assessmentItem);
				if(firstItemId == null) {
					firstItemId = itemRef.getIdentifier().toString();
				}
			}
		} catch (IOException | URISyntaxException e) {
			showError("error.import.question");
			logError("", e);
		}
		
		updateTreeModel();
		
		TreeNode newItemNode = menuTree.getTreeModel().getNodeById(firstItemId);
		menuTree.setSelectedNode(newItemNode);
		menuTree.open(newItemNode);
		partEditorFactory(ureq, newItemNode);
	}

	private void doInsert(UserRequest ureq, AssessmentItemsPackage importPackage) {
		TreeNode selectedNode = menuTree.getSelectedNode();
		TreeNode sectionNode = getNearestSection(selectedNode);
		
		String firstItemId = null;
		try {
			AssessmentSection section = (AssessmentSection)sectionNode.getUserObject();
			
			List<AssessmentItemAndMetadata> itemsAndMetadata = importPackage.getItems();
			for(AssessmentItemAndMetadata itemAndMetadata:itemsAndMetadata) {
				AssessmentItemBuilder itemBuilder = itemAndMetadata.getItemBuilder();
				AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();
				AssessmentItemRef itemRef = doInsert(section, assessmentItem);
				ManifestMetadataBuilder metadata = manifestBuilder.getResourceBuilderByHref(itemRef.getHref().toString());
				metadata.setQtiMetadata(itemBuilder.getInteractionNames());
				itemAndMetadata.transfer(metadata, getLocale());
				if(firstItemId == null) {
					firstItemId = itemRef.getIdentifier().toString();
				}
			}
		} catch (URISyntaxException e) {
			showError("error.import.question");
			logError("", e);
		}
		
		//persist metadata
		manifestBuilder.write(new File(unzippedDirRoot, "imsmanifest.xml"));
		
		updateTreeModel();
		
		TreeNode newItemNode = menuTree.getTreeModel().getNodeById(firstItemId);
		menuTree.setSelectedNode(newItemNode);
		menuTree.open(newItemNode);
		partEditorFactory(ureq, newItemNode);
	}
	
	private AssessmentItemRef doInsert(AssessmentSection section, AssessmentItem assessmentItem)
	throws URISyntaxException {
		AssessmentItemRef itemRef = new AssessmentItemRef(section);
		String itemId = assessmentItem.getIdentifier();
		itemRef.setIdentifier(Identifier.parseString(itemId));
		File itemFile = new File(unzippedDirRoot, itemId + ".xml");
		itemRef.setHref(new URI(itemFile.getName()));
		section.getSectionParts().add(itemRef);
		
		qtiService.persistAssessmentObject(itemFile, assessmentItem);
		
		URI testUri = resolvedAssessmentTest.getTestLookup().getSystemId();
		File testFile = new File(testUri);
		qtiService.updateAssesmentObject(testFile, resolvedAssessmentTest);

		manifestBuilder.appendAssessmentItem(itemFile.getName());
		manifestBuilder.write(new File(unzippedDirRoot, "imsmanifest.xml"));
		return itemRef;
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
			newSection = AssessmentTestFactory.appendAssessmentSection((TestPart)parentPart);
		} else if(parentPart instanceof AssessmentSection) {
			newSection = AssessmentTestFactory.appendAssessmentSection((AssessmentSection)parentPart);
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
			String itemId = IdentifierGenerator.newAsString(itemBuilder.getQuestionType().getPrefix());
			itemRef.setIdentifier(Identifier.parseString(itemId));
			File itemFile = new File(unzippedDirRoot, itemId + ".xml");
			itemRef.setHref(new URI(itemFile.getName()));
			section.getSectionParts().add(itemRef);
			
			AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();
			qtiService.persistAssessmentObject(itemFile, assessmentItem);
			
			URI testUri = resolvedAssessmentTest.getTestLookup().getSystemId();
			File testFile = new File(testUri);
			qtiService.updateAssesmentObject(testFile, resolvedAssessmentTest);

			manifestBuilder.appendAssessmentItem(itemFile.getName());
			manifestBuilder.write(new File(unzippedDirRoot, "imsmanifest.xml"));
			
			updateTreeModel();
			
			TreeNode newItemNode = menuTree.getTreeModel().getNodeById(itemId);
			menuTree.setSelectedNode(newItemNode);
			menuTree.open(newItemNode);

			partEditorFactory(ureq, newItemNode);
		} catch (URISyntaxException e) {
			logError("", e);
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
	
	private void doSaveManifest() {
		this.manifestBuilder.write(new File(unzippedDirRoot, "imsmanifest.xml"));
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
				ManifestMetadataBuilder metadata = getMetadataBuilder(itemRef);
				currentEditorCtrl = new AssessmentItemEditorController(ureq, getWindowControl(), testEntry,
						item, itemRef, metadata, unzippedDirRoot);
			}
		}
		
		if(currentEditorCtrl != null) {
			listenTo(currentEditorCtrl);
			mainVC.put("content", currentEditorCtrl.getInitialComponent());
		} else {
			mainVC.put("content", new Panel("empty"));
		}
	}
	
	private ManifestMetadataBuilder getMetadataBuilder(AssessmentItemRef itemRef) {
		URI itemUri = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef).getItemLookup().getSystemId();
		File itemFile = new File(itemUri);
		String relativePathToManifest = unzippedDirRoot.toPath().relativize(itemFile.toPath()).toString();
		ResourceType resource = manifestBuilder.getResourceTypeByHref(relativePathToManifest);
		return manifestBuilder.getMetadataBuilder(resource, true);
	}
}