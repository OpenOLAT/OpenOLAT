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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.olat.core.gui.components.tree.TreeDropEvent;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.VetoableCloseController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.openxml.QTI21WordExport;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentTestBuilder;
import org.olat.ims.qti21.model.xml.AssessmentTestFactory;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.model.xml.QtiNodesHelper;
import org.olat.ims.qti21.model.xml.interactions.EssayAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.EntryType;
import org.olat.ims.qti21.model.xml.interactions.HotspotAssessmentItemBuilder;
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
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AbstractPart;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.SectionPart;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.RootNodeLookup;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.utils.QueryUtils;
import uk.ac.ed.ph.jqtiplus.utils.TreeWalkNodeHandler;

/**
 * Assessment test editor and composer.
 * 
 * 
 * Initial date: 22.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestComposerController extends MainLayoutBasicController implements VetoableCloseController, ToolbarAware {
	
	
	private MenuTree menuTree;
	private Dropdown exportItemTools, addItemTools, changeItemTools;
	private Link newTestPartLink, newSectionLink, newSingleChoiceLink, newMultipleChoiceLink, newKPrimLink,
		newFIBLink, newNumericalLink, newHotspotLink, newEssayLink;
	private Link importFromPoolLink, importFromTableLink, exportToPoolLink, exportToDocxLink;
	private Link deleteLink, copyLink;
	private final TooledStackedPanel toolbar;
	private VelocityContainer mainVC;

	private Controller currentEditorCtrl;
	private CloseableModalController cmc;
	private SelectItemController selectQItemCtrl;
	private DialogBoxController confirmDeleteCtrl;
	private StepsMainRunController importTableWizard;
	private final LayoutMain3ColsController columnLayoutCtr;
	
	private File unzippedDirRoot;
	private VFSContainer unzippedContRoot;
	
	private final RepositoryEntry testEntry;
	private ManifestBuilder manifestBuilder;
	private ResolvedAssessmentTest resolvedAssessmentTest;
	private AssessmentTestBuilder assessmentTestBuilder;
	
	private final boolean survey = false;
	private final boolean restrictedEdit;
	private boolean assessmentChanged = false;
	
	private LockResult lockEntry;
	private LockResult activeSessionLock;
	private CountDownLatch exportLatch;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private QTI21QPoolServiceProvider qti21QPoolServiceProvider;
	
	public AssessmentTestComposerController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar,
			RepositoryEntry testEntry) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentTestDisplayController.class, getLocale(), getTranslator()));
		
		this.toolbar = toolbar;
		this.testEntry = testEntry;
		restrictedEdit = qtiService.isAssessmentTestActivelyUsed(testEntry);
		
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().aquirePersistentLock(testEntry.getOlatResource(), getIdentity(), null);
		if (lockEntry.isSuccess()) {
			// acquired a lock for the duration of the session only
			//fileResource has the RepositoryEntre.getOlatResource within, which is used in qtiPackage
			activeSessionLock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(testEntry.getOlatResource(), getIdentity(), null);
		} else {
			String fullName = userManager.getUserDisplayName(lockEntry.getOwner());
			String msg = translate("error.lock", new String[] { fullName, Formatter.formatDatetime(new Date(lockEntry.getLockAquiredTime())) });
			wControl.setWarning(msg);
			MessageController contentCtr = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), translate("error.lock.title"), msg);
			listenTo(contentCtr);
			columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), contentCtr);
			listenTo(columnLayoutCtr); // auto dispose later
			putInitialPanel(columnLayoutCtr.getInitialComponent());
			return;
		}
		
		// test structure
		menuTree = new MenuTree("atTree");
		menuTree.setExpandSelectedNode(false);
		menuTree.setDragEnabled(!restrictedEdit);
		menuTree.setDropEnabled(!restrictedEdit);
		menuTree.setDropSiblingEnabled(!restrictedEdit);	
		menuTree.setDndAcceptJSMethod("treeAcceptDrop_notWithChildren");	
		menuTree.setElementCssClass("o_assessment_test_editor_menu");
		menuTree.addListener(this);

		FileResourceManager frm = FileResourceManager.getInstance();
		unzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		unzippedContRoot = frm.unzipContainerResource(testEntry.getOlatResource());
		updateTreeModel();
		manifestBuilder = ManifestBuilder.read(new File(unzippedDirRoot, "imsmanifest.xml"));
		
		//add elements
		addItemTools = new Dropdown("editTools", "new.elements", false, getTranslator());
		addItemTools.setIconCSS("o_icon o_icon-fw o_icon_add");
		addItemTools.setVisible(!restrictedEdit);
		
		newSectionLink = LinkFactory.createToolLink("new.section", translate("new.section"), this, "o_mi_qtisection");
		newSectionLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newSectionLink);
		
		newTestPartLink = LinkFactory.createToolLink("new.testpart", translate("new.testpart"), this, "o_qtiassessment_icon");
		newTestPartLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newTestPartLink);

		addItemTools.addComponent(new Dropdown.Spacer("sep-struct"));
		
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
		newNumericalLink = LinkFactory.createToolLink("new.fib.numerical", translate("new.fib.numerical"), this, "o_mi_qtinumerical");
		newNumericalLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newNumericalLink);
		
		newHotspotLink = LinkFactory.createToolLink("new.hotspot", translate("new.hotspot"), this, "o_mi_qtihotspot");
		newHotspotLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newHotspotLink);
		
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
		
		exportItemTools = new Dropdown("exportTools", "tools.export.header", false, getTranslator());
		exportItemTools.setIconCSS("o_icon o_icon_export");
		//export
		exportToPoolLink = LinkFactory.createToolLink("export.pool", translate("tools.export.qpool"), this, "o_mi_qpool_export");
		exportToPoolLink.setIconLeftCSS("o_icon o_icon_table o_icon-fw");
		exportToPoolLink.setDomReplacementWrapperRequired(false);
		exportItemTools.addComponent(exportToPoolLink);
		
		exportToDocxLink = LinkFactory.createToolLink("export.pool", translate("tools.export.docx"), this, "o_mi_docx_export");
		exportToDocxLink.setIconLeftCSS("o_icon o_icon_download o_icon-fw");
		exportToDocxLink.setDomReplacementWrapperRequired(false);
		exportItemTools.addComponent(exportToDocxLink);

		//changes
		changeItemTools = new Dropdown("changeTools", "change.elements", false, getTranslator());
		changeItemTools.setIconCSS("o_icon o_icon-fw o_icon_customize");
		changeItemTools.setVisible(!restrictedEdit);

		deleteLink = LinkFactory.createToolLink("import.pool", translate("tools.change.delete"), this, "o_icon_delete_item");
		deleteLink.setDomReplacementWrapperRequired(false);
		changeItemTools.addComponent(deleteLink);

		copyLink = LinkFactory.createToolLink("import.table", translate("tools.change.copy"), this, "o_icon_copy");
		copyLink.setDomReplacementWrapperRequired(false);
		changeItemTools.addComponent(copyLink);
		
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
		assessmentTestBuilder = new AssessmentTestBuilder(resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful());
	}
	
	public boolean hasChanges() {
		return assessmentChanged;
	}
	
	@Override
	protected void doDispose() {
		if (lockEntry != null && lockEntry.isSuccess()) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releasePersistentLock(lockEntry);
		}
		if (activeSessionLock != null && activeSessionLock.isSuccess()) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(activeSessionLock);			
		}
	}
	
	@Override
	public void initToolbar() {
		toolbar.addTool(exportItemTools, Align.left);
		toolbar.addTool(addItemTools, Align.left);
		toolbar.addTool(changeItemTools, Align.left);
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
				assessmentChanged = true;
				doSaveAssessmentTest();
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
		} else if(confirmDeleteCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event)) { // yes, delete
				doDelete(ureq, (TreeNode)confirmDeleteCtrl.getUserObject());
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(selectQItemCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteCtrl = null;
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
			} else if(event.getCommand().equals(MenuTree.COMMAND_TREENODE_DROP)) {
				TreeDropEvent tde = (TreeDropEvent) event;
				doDrop(ureq, tde.getDroppedNodeId(), tde.getTargetNodeId(), tde.isAsChild());
			}
		} else if(newSectionLink == source) {
			doNewSection(ureq, menuTree.getSelectedNode());
		} else if(newTestPartLink == source) {
			doNewTestPart(ureq);		
		} else if(newSingleChoiceLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new SingleChoiceAssessmentItemBuilder(qtiService.qtiSerializer()));
		} else if(newMultipleChoiceLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new MultipleChoiceAssessmentItemBuilder(qtiService.qtiSerializer()));
		} else if(newKPrimLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new KPrimAssessmentItemBuilder(qtiService.qtiSerializer()));
		} else if(newFIBLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new FIBAssessmentItemBuilder(EntryType.text, qtiService.qtiSerializer()));
		} else if(newNumericalLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new FIBAssessmentItemBuilder(EntryType.numerical, qtiService.qtiSerializer()));
		} else if(newHotspotLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new HotspotAssessmentItemBuilder(qtiService.qtiSerializer()));
		} else if(newEssayLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new EssayAssessmentItemBuilder(qtiService.qtiSerializer()));
		} else if(importFromPoolLink == source) {
			doSelectQItem(ureq);
		} else if(importFromTableLink == source) {
			doImportTable(ureq);
		} else if(exportToPoolLink == source) {
			doExportPool();
		} else if(exportToDocxLink == source) {
			doExportDocx(ureq);
		} else if(deleteLink == source) {
			doConfirmDelete(ureq);
		} else if(copyLink == source) {
			doCopy(ureq);
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
	
	private void doDrop(UserRequest ureq, String droppedNodeId, String targetnodeId, boolean asChild) {
		TreeNode droppedNode = menuTree.getTreeModel().getNodeById(droppedNodeId);
		TreeNode targetNode = menuTree.getTreeModel().getNodeById(targetnodeId);
		if(droppedNode == null || targetNode == null) return;
		
		Object droppedObject = droppedNode.getUserObject();
		Object targetObject = targetNode.getUserObject();
		if(droppedObject == null || targetObject == null || droppedObject == targetObject) return;
		
		if(asChild) {
			if(droppedObject instanceof AssessmentItemRef
					&& (targetObject instanceof AssessmentSection || targetObject instanceof AssessmentItemRef)) {
				AssessmentItemRef droppedItemRef = (AssessmentItemRef)droppedObject;
				droppedItemRef.getParentSection().getSectionParts().remove(droppedItemRef);
				if(targetObject instanceof AssessmentSection) {
					AssessmentSection targetSection = (AssessmentSection)targetObject;
					targetSection.getSectionParts().add(droppedItemRef);
				} else if(targetObject instanceof AssessmentItemRef) {
					AssessmentItemRef targetItemRef = (AssessmentItemRef)targetObject;
					AssessmentSection targetSection = targetItemRef.getParentSection();
					int pos = targetSection.getChildAbstractParts().indexOf(targetItemRef);
					targetSection.getChildAbstractParts().add(pos, droppedItemRef);
				}
				
			} else if(droppedObject instanceof AssessmentSection
					&& (targetObject instanceof AssessmentSection || targetObject instanceof TestPart)) {
				AssessmentSection droppedSection = (AssessmentSection)droppedObject;
				if(droppedSection.getParentSection() != null) {
					droppedSection.getParentSection().getSectionParts().remove(droppedSection);
				} else {
					droppedSection.getParent().getChildAbstractParts().remove(droppedSection);
				}
				if(targetObject instanceof AssessmentSection) {
					AssessmentSection targetSection = (AssessmentSection)targetObject;
					targetSection.getChildAbstractParts().add(droppedSection);
				} else if(targetObject instanceof TestPart) {
					TestPart targetTestPart = (TestPart)targetObject;
					targetTestPart.getAssessmentSections().add(droppedSection);
				}
			}
		} else {
			if(droppedObject instanceof AssessmentItemRef && targetObject instanceof AssessmentItemRef) {
				AssessmentItemRef droppedItemRef = (AssessmentItemRef)droppedObject;
				droppedItemRef.getParentSection().getSectionParts().remove(droppedItemRef);
				AssessmentItemRef targetItemRef = (AssessmentItemRef)targetObject;
				AssessmentSection targetSection = targetItemRef.getParentSection();
				int pos = targetSection.getChildAbstractParts().indexOf(targetItemRef) + 1;
				if(pos < 0) {
					targetSection.getChildAbstractParts().add(droppedItemRef);
				} else if(pos >= targetSection.getChildAbstractParts().size()) {
					targetSection.getChildAbstractParts().add(droppedItemRef);
				} else {
					targetSection.getChildAbstractParts().add(pos, droppedItemRef);
				}
				
			} else if(droppedObject instanceof AssessmentSection
					&& targetObject instanceof AssessmentSection) {
				AssessmentSection droppedSection = (AssessmentSection)droppedObject;
				
				
				if(droppedSection.getParentSection() != null) {
					droppedSection.getParentSection().getSectionParts().remove(droppedSection);
				} else {
					droppedSection.getParent().getChildAbstractParts().remove(droppedSection);
				}

				AssessmentSection targetSection = (AssessmentSection)targetObject;
				if(targetSection.getParentSection() != null) {
					AssessmentSection targetParentSection = targetSection.getParentSection();
					int pos = targetParentSection.getChildAbstractParts().indexOf(targetSection) + 1;
					if(pos >= targetParentSection.getChildAbstractParts().size()) {	
						targetParentSection.getChildAbstractParts().add(droppedSection);
					} else {
						targetParentSection.getChildAbstractParts().add(pos, droppedSection);
					}
				} else if(targetSection.getParent() instanceof TestPart) {
					TestPart targetTestPart = (TestPart)targetSection.getParent();
					int pos = targetTestPart.getChildAbstractParts().indexOf(targetSection) + 1;
					if(pos >= targetTestPart.getChildAbstractParts().size()) {	
						targetTestPart.getChildAbstractParts().add(droppedSection);
					} else {
						targetTestPart.getChildAbstractParts().add(pos, droppedSection);
					}
				}
			}
		}
		
		//quickly saved the assessment test with wrong parent
		doSaveAssessmentTest();
		//reload a clean instance
		updateTreeModel();
		
		TreeNode droppedItemNode = menuTree.getTreeModel().getNodeById(droppedNode.getIdent());
		if(droppedItemNode != null) {
			menuTree.setSelectedNode(droppedItemNode);
			menuTree.open(droppedItemNode);
			partEditorFactory(ureq, droppedItemNode);
		}
	}
	
	private void doExportPool() {
		TreeNode selectedNode = menuTree.getSelectedNode();
		if(selectedNode == null) return;
		
		AtomicInteger counter = new AtomicInteger();
		Object uobject = selectedNode.getUserObject();
		if(uobject instanceof AssessmentItemRef) {
			doExportPool((AssessmentItemRef)uobject);
			counter.incrementAndGet();
		} else if(uobject instanceof QtiNode) {
			QtiNode qtiNode = (QtiNode)uobject;
			QueryUtils.walkTree(new TreeWalkNodeHandler() {
				
				@Override
				public boolean handleNode(QtiNode node) {
					if(node instanceof AssessmentItemRef) {
						doExportPool((AssessmentItemRef)node);
						counter.incrementAndGet();
					}
					return true;
				}
			}, qtiNode);
		}
		
		if(counter.get() > 0) {
			showInfo("export.qpool.successful", counter.toString());
		}
	}
	
	private void doExportPool(AssessmentItemRef itemRef) {
		AssessmentItem assessmentItem = resolvedAssessmentTest
				.getResolvedAssessmentItem(itemRef).getRootNodeLookup().extractIfSuccessful();

		ManifestBuilder clonedManifestBuilder = ManifestBuilder.read(new File(unzippedDirRoot, "imsmanifest.xml"));
		ResourceType resource = getResourceType(clonedManifestBuilder, itemRef);
		ManifestMetadataBuilder metadata = clonedManifestBuilder.getMetadataBuilder(resource, true);

		qti21QPoolServiceProvider
				.importAssessmentItemRef(getIdentity(), itemRef, assessmentItem, metadata, unzippedDirRoot, getLocale());
	}
	
	private void doExportDocx(UserRequest ureq) {
		exportLatch = new CountDownLatch(1);
		MediaResource mr = new QTI21WordExport(resolvedAssessmentTest, unzippedContRoot, getLocale(), "UTF-8", exportLatch);
		ureq.getDispatchResult().setResultingMediaResource(mr);
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
				itemAndMetadata.toBuilder(metadata, getLocale());
				if(firstItemId == null) {
					firstItemId = itemRef.getIdentifier().toString();
				}
			}
		} catch (URISyntaxException e) {
			showError("error.import.question");
			logError("", e);
		}
		
		//persist metadata
		doSaveManifest();
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
		doSaveManifest();
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
	
	/**
	 * Create a new test part and a section. Test part need a section,
	 * section ref as children, it's mandatory.
	 * 
	 * @param ureq
	 */
	private void doNewTestPart(UserRequest ureq) {
		TestPart testPart = AssessmentTestFactory.createTestPart(assessmentTestBuilder.getAssessmentTest());
		AssessmentTestFactory.appendAssessmentSection(testPart);
		
		//save the test
		doSaveAssessmentTest();
		//reload the test
		updateTreeModel();
		
		TreeNode newTestPartNode = menuTree.getTreeModel().getNodeById(testPart.getIdentifier().toString());
		menuTree.setSelectedNode(newTestPartNode);
		menuTree.open(newTestPartNode);

		partEditorFactory(ureq, newTestPartNode);
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
		assessmentChanged = true;

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
			
			doSaveAssessmentTest();
			manifestBuilder.appendAssessmentItem(itemFile.getName());
			doSaveManifest();
			
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
			if(node.getChildCount() > 0) {
				return (TreeNode)node.getChildAt(0);
			}
		}
		return null;
	}
	
	private void doSaveAssessmentTest() {
		assessmentChanged = true;
		recalculateMaxScoreAssessmentTest();
		URI testURI = resolvedAssessmentTest.getTestLookup().getSystemId();
		File testFile = new File(testURI);
		qtiService.updateAssesmentObject(testFile, resolvedAssessmentTest);
	}
	
	private void recalculateMaxScoreAssessmentTest() {
		double sumMaxScore = 0.0d;
		for(ResolvedAssessmentItem resolvedAssessmentItem:resolvedAssessmentTest.getResolvedAssessmentItemBySystemIdMap().values()) {
			AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
			if(assessmentItem != null) {
				Double maxScore = QtiNodesHelper
						.getOutcomeDeclarationDefaultFloatValue(assessmentItem.getOutcomeDeclaration(QTI21Constants.MAXSCORE_IDENTIFIER));
				if(maxScore != null) {
					sumMaxScore += maxScore;
				}
			}
		}
		
		if(sumMaxScore > 0.0d) {
			assessmentTestBuilder.setMaxScore(sumMaxScore);
		} else {
			assessmentTestBuilder.setMaxScore(null);
		}
	}
	
	private void doSaveManifest() {
		manifestBuilder.write(new File(unzippedDirRoot, "imsmanifest.xml"));
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
		if(Settings.isDebuging()) {
			mainVC.contextPut("identifier", selectedNode.getIdent());
		}
		mainVC.contextPut("title", selectedNode.getTitle());
		mainVC.contextPut("restrictedEdit", restrictedEdit);

		Object uobject = selectedNode.getUserObject();
		if(uobject instanceof AssessmentTest) {
			currentEditorCtrl = new AssessmentTestEditorController(ureq, getWindowControl(), assessmentTestBuilder, restrictedEdit);
		} else if(uobject instanceof TestPart) {
			currentEditorCtrl = new AssessmentTestPartEditorController(ureq, getWindowControl(), (TestPart)uobject, restrictedEdit);
		} else if(uobject instanceof AssessmentSection) {
			currentEditorCtrl = new AssessmentSectionEditorController(ureq, getWindowControl(), (AssessmentSection)uobject, restrictedEdit);
		} else if(uobject instanceof AssessmentItemRef) {
			AssessmentItemRef itemRef = (AssessmentItemRef)uobject;
			ResolvedAssessmentItem item = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
			if(item == null || item.getItemLookup() == null) {
				currentEditorCtrl = new BadResourceController(ureq, getWindowControl(),
						null, unzippedDirRoot, itemRef.getHref());
			} else if(item.getItemLookup().getBadResourceException() != null) {
				currentEditorCtrl = new BadResourceController(ureq, getWindowControl(),
						item.getItemLookup().getBadResourceException(), unzippedDirRoot, itemRef.getHref());
			} else {
				URI itemUri = resolvedAssessmentTest.getSystemIdByItemRefMap().get(itemRef);
				File itemFile = new File(itemUri);
				ManifestMetadataBuilder metadata = getMetadataBuilder(itemRef);
				currentEditorCtrl = new AssessmentItemEditorController(ureq, getWindowControl(), testEntry,
						item, itemRef, metadata, unzippedDirRoot, unzippedContRoot, itemFile, restrictedEdit);
			}
		}
		
		if(deleteLink != null) {
			deleteLink.setEnabled(uobject instanceof AssessmentSection || uobject instanceof AssessmentItemRef);
		}
		if(copyLink != null) {
			copyLink.setEnabled(uobject instanceof AssessmentItemRef);
		}
		
		if(currentEditorCtrl != null) {
			listenTo(currentEditorCtrl);
			mainVC.put("content", currentEditorCtrl.getInitialComponent());
		} else {
			mainVC.put("content", new Panel("empty"));
		}
	}
	
	private void doCopy(UserRequest ureq) {
		TreeNode selectedNode = menuTree.getSelectedNode();
		if(selectedNode == null || !(selectedNode.getUserObject() instanceof AssessmentItemRef)) return;

		AssessmentItemRef itemRefToCopy = (AssessmentItemRef)selectedNode.getUserObject();
		AssessmentSection section = itemRefToCopy.getParentSection();
		
		ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRefToCopy);
		AssessmentItem originalAssessmentItem = resolvedAssessmentItem.getItemLookup().extractIfSuccessful();
		QTI21QuestionType type = QTI21QuestionType.getType(originalAssessmentItem);

		File itemFile = null;
		try {
			AssessmentItemRef itemRef = new AssessmentItemRef(section);
			String itemId = IdentifierGenerator.newAsString(type.getPrefix());
			itemRef.setIdentifier(Identifier.parseString(itemId));
			itemFile = new File(unzippedDirRoot, itemId + ".xml");
			itemRef.setHref(new URI(itemFile.getName()));

			try(OutputStream out = new FileOutputStream(itemFile)) {
				//make the copy
				qtiService.qtiSerializer().serializeJqtiObject(originalAssessmentItem, out);
				
				//change identifier and title
				ResolvedAssessmentItem resolvedCopyItem = qtiService.loadAndResolveAssessmentItem(itemFile.toURI(), unzippedDirRoot);
				AssessmentItem copiedAssessmentItem = resolvedCopyItem.getRootNodeLookup().extractIfSuccessful();
				copiedAssessmentItem.setIdentifier(IdentifierGenerator.newAsString(type.getPrefix()));
				copiedAssessmentItem.setTitle(originalAssessmentItem.getTitle() + " (Copy)");
				qtiService.updateAssesmentObject(itemFile, resolvedCopyItem);
				
				//add to section
				section.getSectionParts().add(itemRef);
				doSaveAssessmentTest();
				manifestBuilder.appendAssessmentItem(itemFile.getName());
				doSaveManifest();
			} catch (Exception e) {
				logError("", e);
			}

			updateTreeModel();
			
			TreeNode newItemNode = menuTree.getTreeModel().getNodeById(itemId);
			menuTree.setSelectedNode(newItemNode);
			menuTree.open(newItemNode);
			partEditorFactory(ureq, newItemNode);
		} catch (URISyntaxException e) {
			logError("", e);
		}
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		if(confirmDeleteCtrl != null) return;
		
		TreeNode selectedNode = menuTree.getSelectedNode();
		Object uobject = selectedNode.getUserObject();
		if(uobject instanceof AssessmentTest) {
			showWarning("error.cannot.delete");
		} else if(uobject instanceof TestPart) {
			showWarning("error.cannot.delete");
		} else {
			String msg;
			if(uobject instanceof AssessmentSection) {
				msg = translate("delete.section", selectedNode.getTitle());
			} else if(uobject instanceof AssessmentItemRef) {
				msg = translate("delete.item", selectedNode.getTitle());
			} else {
				showError("error.cannot.delete");
				return;
			}

			confirmDeleteCtrl = activateYesNoDialog(ureq, translate("tools.change.delete"), msg, confirmDeleteCtrl);
			confirmDeleteCtrl.setUserObject(selectedNode);
		}
	}
	
	private void doDelete(UserRequest ureq, TreeNode selectedNode) {
		Object uobject = selectedNode.getUserObject();
		if(uobject instanceof AssessmentSection) {
			doDeleteAssessmentSection((AssessmentSection)uobject);
		} else if(uobject instanceof AssessmentItemRef) {
			doDeleteAssessmentItemRef((AssessmentItemRef)uobject);
		} else {
			return;//cannot delete test or test part
		}
		
		doSaveAssessmentTest();
		doSaveManifest();
		updateTreeModel();

		if(selectedNode != null && selectedNode.getParent() != null) {
			TreeNode parentNode = (TreeNode)selectedNode.getParent();
			menuTree.setSelectedNode(parentNode);
			menuTree.open(parentNode);
			partEditorFactory(ureq, parentNode);
		}
	}
	
	private void doDeleteAssessmentItemRef(AssessmentItemRef itemRef) {
		ResourceType resource = getResourceType(itemRef);
		if(resource != null) {
			manifestBuilder.remove(resource);
		}
		
		boolean deleted = false;
		boolean removed = itemRef.getParentSection().getSectionParts().remove(itemRef);
		
		ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
		if(resolvedAssessmentItem != null) {
			RootNodeLookup<AssessmentItem> rootNode = resolvedAssessmentItem.getItemLookup();
			if(rootNode != null) {
				URI itemUri = rootNode.getSystemId();
				File itemFile = new File(itemUri);
				deleted = itemFile.delete();
			}
		}
		if(deleted) {
			assessmentChanged = true;
		}
		
		logAudit(removed + " " + deleted + " removed item ref", null);
	}
	
	private void doDeleteAssessmentSection(AssessmentSection assessmentSection) {
		for(SectionPart part:assessmentSection.getSectionParts()) {
			if(part instanceof AssessmentItemRef) {
				doDeleteAssessmentItemRef((AssessmentItemRef)part);
			} else if(part instanceof AssessmentSection) {
				doDeleteAssessmentSection((AssessmentSection)part);
			}
		}
		
		if(assessmentSection.getParentSection() != null) {
			assessmentSection.getParentSection().getSectionParts().remove(assessmentSection);
		} else {
			assessmentSection.getParent().getChildAbstractParts().remove(assessmentSection);
		}
	}

	private ResourceType getResourceType(AssessmentItemRef itemRef) {
		return getResourceType(manifestBuilder, itemRef);
	}

	private ResourceType getResourceType(ManifestBuilder builder, AssessmentItemRef itemRef) {
		ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
		if(resolvedAssessmentItem == null) return null;
		RootNodeLookup<AssessmentItem> rootNode = resolvedAssessmentItem.getItemLookup();
		if(rootNode == null) return null;
		
		URI itemUri = rootNode.getSystemId();
		File itemFile = new File(itemUri);
		String relativePathToManifest = unzippedDirRoot.toPath().relativize(itemFile.toPath()).toString();
		return builder.getResourceTypeByHref(relativePathToManifest);
	}
	
	private ManifestMetadataBuilder getMetadataBuilder(AssessmentItemRef itemRef) {
		ResourceType resource = getResourceType(itemRef);
		return manifestBuilder.getMetadataBuilder(resource, true);
	}
}