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
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
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
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.AssessmentTestHelper;
import org.olat.ims.qti21.AssessmentTestHelper.AssessmentTestVisitor;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21LoggingAction;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentTestBuilder;
import org.olat.ims.qti21.model.xml.AssessmentTestFactory;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.model.xml.QtiMaxScoreEstimator;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.model.xml.interactions.DrawingAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.EssayAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.EntryType;
import org.olat.ims.qti21.model.xml.interactions.HotspotAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.HottextAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.KPrimAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MatchAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MultipleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.OrderAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.UploadAssessmentItemBuilder;
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
import org.olat.ims.qti21.ui.editor.events.DetachFromPoolEvent;
import org.olat.ims.qti21.ui.editor.events.OpenTestConfigurationOverviewEvent;
import org.olat.ims.qti21.ui.editor.events.SelectEvent;
import org.olat.ims.qti21.ui.editor.events.SelectEvent.SelectionTarget;
import org.olat.ims.qti21.ui.editor.metadata.MetadataChangedEvent;
import org.olat.ims.qti21.ui.editor.overview.AssessmentTestOverviewConfigurationController;
import org.olat.imscp.xml.manifest.FileType;
import org.olat.imscp.xml.manifest.ResourceType;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.site.QuestionPoolSiteDef;
import org.olat.modules.qpool.ui.SelectItemController;
import org.olat.modules.qpool.ui.events.QItemViewEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.RepositoryEntryRuntimeController.ToolbarAware;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AbstractPart;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.ControlObject;
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
	private Dropdown addItemTools, changeItemDropDown;
	private Link newTestPartLink, newSectionLink, newSingleChoiceLink, newMultipleChoiceLink,
			newKPrimLink, newMatchLink, newMatchDragAndDropLink, newMatchTrueFalseLink,
			newFIBLink, newNumericalLink, newHotspotLink, newHottextLink, newOrderLink,
			newEssayLink, newUploadLink, newDrawingLink;
	private Link importFromPoolLink;
	private Link importFromTableLink;
	private Link exportToPoolLink;
	private Link reloadInCacheLink;
	private Link deleteLink;
	private Link copyLink;
	private Link configurationOverviewLink;
	private final TooledStackedPanel toolbar;
	private VelocityContainer mainVC;

	private Controller currentEditorCtrl;
	private CloseableModalController cmc;
	private SelectItemController selectQItemCtrl;
	private DialogBoxController confirmDeleteCtrl;
	private StepsMainRunController importTableWizard;
	private LayoutMain3ColsController columnLayoutCtr;
	private AssessmentTestOverviewConfigurationController overviewConfigCtrl;
	
	private File unzippedDirRoot;
	private VFSContainer unzippedContRoot;
	
	private final RepositoryEntry testEntry;
	private ManifestBuilder manifestBuilder;
	private ResolvedAssessmentTest resolvedAssessmentTest;
	private AssessmentTestBuilder assessmentTestBuilder;
	
	private final boolean restrictedEdit;
	
	private boolean assessmentChanged = false;
	private boolean deleteAuthorSesssion = false;
	
	private LockResult lockEntry;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private SiteDefinitions sitesModule;
	@Autowired
	private QTI21QPoolServiceProvider qti21QPoolServiceProvider;
	
	public AssessmentTestComposerController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar,
			RepositoryEntry testEntry) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentTestDisplayController.class, ureq.getLocale()));
		
		this.toolbar = toolbar;
		this.testEntry = testEntry;
		restrictedEdit = qtiService.isAssessmentTestActivelyUsed(testEntry);
		
		FileResourceManager frm = FileResourceManager.getInstance();
		unzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		unzippedContRoot = frm.unzipContainerResource(testEntry.getOlatResource());
		
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(testEntry.getOlatResource(), getIdentity(), null, getWindow());
		if (!lockEntry.isSuccess()) {
			String fullName = userManager.getUserDisplayName(lockEntry.getOwner());
			String i18nMsg = lockEntry.isDifferentWindows() ? "error.lock.same.user" : "error.lock";
			String msg = translate(i18nMsg, fullName, Formatter.formatDatetime(new Date(lockEntry.getLockAquiredTime())));
			wControl.setWarning(msg);
			MessageController contentCtr = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), translate("error.lock.title"), msg);
			listenTo(contentCtr);
			columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), contentCtr);
			listenTo(columnLayoutCtr); // auto dispose later
			putInitialPanel(columnLayoutCtr.getInitialComponent());
			return;
		}
		
		addLoggingResourceable(LoggingResourceable.wrapTest(testEntry));
	
		if(!checkResolvedAssessmentTest()) {
			VelocityContainer errorVC = createVelocityContainer("error");
			putInitialPanel(errorVC);
			return;
		}
		
		// test structure
		menuTree = new MenuTree("atTree");
		menuTree.setExpandSelectedNode(false);

		menuTree.setDropSiblingEnabled(!restrictedEdit);	
		menuTree.setDndAcceptJSMethod("treeAcceptDrop_notWithChildren");	
		menuTree.setElementCssClass("o_assessment_test_editor_menu");
		menuTree.addListener(this);

		updateTreeModel(false);
		manifestBuilder = ManifestBuilder.read(new File(unzippedDirRoot, "imsmanifest.xml"));
		//is the test editable ?
		menuTree.setDragEnabled(!restrictedEdit && assessmentTestBuilder.isEditable());
		menuTree.setDropEnabled(!restrictedEdit && assessmentTestBuilder.isEditable());
		
		//add elements
		addItemTools = new Dropdown("editTools", "new.elements", false, getTranslator());
		addItemTools.setIconCSS("o_icon o_icon-fw o_icon_add");
		addItemTools.setElementCssClass("o_sel_qti_elements");
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
		newMatchLink = LinkFactory.createToolLink("new.match", translate("new.match"), this, "o_mi_qtimatch");
		newMatchLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newMatchLink);
		newMatchDragAndDropLink = LinkFactory.createToolLink("new.matchdraganddrop", translate("new.matchdraganddrop"), this, "o_mi_qtimatch_draganddrop");
		newMatchDragAndDropLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newMatchDragAndDropLink);
		newMatchTrueFalseLink = LinkFactory.createToolLink("new.matchtruefalse", translate("new.matchtruefalse"), this, "o_mi_qtimatch_truefalse");
		newMatchTrueFalseLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newMatchTrueFalseLink);

		newFIBLink = LinkFactory.createToolLink("new.fib", translate("new.fib"), this, "o_mi_qtifib");
		newFIBLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newFIBLink);
		newNumericalLink = LinkFactory.createToolLink("new.fib.numerical", translate("new.fib.numerical"), this, "o_mi_qtinumerical");
		newNumericalLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newNumericalLink);
		newHottextLink = LinkFactory.createToolLink("new.hottext", translate("new.hottext"), this, "o_mi_qtihottext");
		newHottextLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newHottextLink);
		
		newHotspotLink = LinkFactory.createToolLink("new.hotspot", translate("new.hotspot"), this, "o_mi_qtihotspot");
		newHotspotLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newHotspotLink);
		newOrderLink = LinkFactory.createToolLink("new.order", translate("new.order"), this, "o_mi_qtiorder");
		newOrderLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newOrderLink);
		
		newEssayLink = LinkFactory.createToolLink("new.essay", translate("new.essay"), this, "o_mi_qtiessay");
		newEssayLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newEssayLink);
		newUploadLink = LinkFactory.createToolLink("new.upload", translate("new.upload"), this, "o_mi_qtiupload");
		newUploadLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newUploadLink);
		newDrawingLink = LinkFactory.createToolLink("new.drawing", translate("new.drawing"), this, "o_mi_qtidrawing");
		newDrawingLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(newDrawingLink);
		
		addItemTools.addComponent(new Dropdown.Spacer("sep-import"));
		//import
		boolean questionPoolEnabled = sitesModule.isSiteEnabled(QuestionPoolSiteDef.class);
		if(questionPoolEnabled) {
			importFromPoolLink = LinkFactory.createToolLink("import.pool", translate("tools.import.qpool"), this, "o_mi_qpool_import");
			importFromPoolLink.setDomReplacementWrapperRequired(false);
			addItemTools.addComponent(importFromPoolLink);
		}
		
		importFromTableLink = LinkFactory.createToolLink("import.table", translate("tools.import.table"), this, "o_mi_table_import");
		importFromTableLink.setIconLeftCSS("o_icon o_icon_table o_icon-fw");
		importFromTableLink.setDomReplacementWrapperRequired(false);
		addItemTools.addComponent(importFromTableLink);
		
		Roles roles = ureq.getUserSession().getRoles();
		if(roles.isAdministrator() || roles.isSystemAdmin()) {
			reloadInCacheLink = LinkFactory.createToolLink("replace.in.cache.pool", translate("tools.reload.from.files"), this, "o_icon_refresh");
			reloadInCacheLink.setTooltip(translate("tools.reload.from.files.tooltip"));
		}
		
		configurationOverviewLink = LinkFactory.createToolLink("configuration.overview", translate("configuration.overview"), this, "o_icon_description");
		configurationOverviewLink.setDomReplacementWrapperRequired(false);
		

		mainVC = createVelocityContainer("assessment_test_composer");
		//changes
		deleteLink = LinkFactory.createButton("tools.change.delete", mainVC, this);
		deleteLink.setIconLeftCSS("o_icon o_icon_delete_item");
		deleteLink.setVisible(!restrictedEdit);
		
		changeItemDropDown = new Dropdown("changeTools", null, false, getTranslator());
		changeItemDropDown.setElementCssClass("o_sel_qti_change_node");
		changeItemDropDown.setCarretIconCSS("o_icon o_icon_commands");
		changeItemDropDown.setButton(true);
		changeItemDropDown.setEmbbeded(true);
		changeItemDropDown.setOrientation(DropdownOrientation.right);
		mainVC.put("cmds", changeItemDropDown);
		
		copyLink = LinkFactory.createToolLink("import.table", translate("tools.change.copy"), this, "o_icon_copy");
		copyLink.setVisible(!restrictedEdit);
		changeItemDropDown.addComponent(copyLink);
		
		if(questionPoolEnabled) {
			exportToPoolLink = LinkFactory.createToolLink("export.pool", translate("tools.export.qpool"), this, "o_icon_table");
			changeItemDropDown.addComponent(exportToPoolLink);
		}
		
		// main layout
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, mainVC, "at" + testEntry.getKey());			
		columnLayoutCtr.addCssClassToMain("o_editor");
		listenTo(columnLayoutCtr);		
		StackedPanel initPanel = new SimpleStackedPanel("qti21editpanel", "o_edit_mode");
		initPanel.setContent(columnLayoutCtr.getInitialComponent());
		putInitialPanel(initPanel);
		
		// init
		TreeNode selectedNode = doOpenFirstItem();
		if(selectedNode == null) {
			selectedNode = menuTree.getTreeModel().getRootNode();
		}
		partEditorFactory(ureq, selectedNode);
	}
	
	private boolean checkResolvedAssessmentTest() {
		ResolvedAssessmentTest resolvedObject;
		try {
			resolvedObject = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false, true);
			if(resolvedObject == null) {
				logError("QTI 2.1 AssessmentTest is null: " + testEntry, null);
				return false;
			}
			return resolvedObject.getRootNodeLookup().extractIfSuccessful() != null;
		} catch (Exception e) {
			logError("QTI 2.1 AssessmentTest is corrupted: " + testEntry, e);
			return false;
		}
	}
	
	private void updateTreeModel(boolean forceReload) {
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, forceReload, true);
		menuTree.setTreeModel(new AssessmentTestEditorAndComposerTreeModel(resolvedAssessmentTest));
		assessmentTestBuilder = new AssessmentTestBuilder(resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful());
	}
	
	public boolean hasChanges() {
		return assessmentChanged;
	}
	
	@Override
	protected void doDispose() {
		if (lockEntry != null && lockEntry.isSuccess()) {
			try {
				CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			} catch (AssertException e) {
				logWarn("Lock was already released", e);
			}
		}
        super.doDispose();
	}
	
	@Override
	public void initToolbar() {
		toolbar.addTool(addItemTools, Align.left);
		toolbar.addTool(reloadInCacheLink, Align.left);
		toolbar.addTool(configurationOverviewLink, Align.right);
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
				AssessmentTest ast = assessmentTestBuilder.getAssessmentTest();
				assessmentChanged(ureq);
				doSaveAssessmentTest(ureq, null);
				doUpdate(ast.getIdentifier(), ast.getTitle(), false);
			}
		} else if(event instanceof AssessmentTestPartEvent) {
			AssessmentTestPartEvent atpe = (AssessmentTestPartEvent)event;
			if(atpe == AssessmentTestPartEvent.ASSESSMENT_TEST_PART_CHANGED_EVENT) {
				assessmentChanged(ureq);
				doSaveAssessmentTest(ureq, null);
			}
		} else if(event instanceof AssessmentSectionEvent) {
			AssessmentSectionEvent ase = (AssessmentSectionEvent)event;
			if(AssessmentSectionEvent.ASSESSMENT_SECTION_CHANGED.equals(ase.getCommand())) {
				doSaveAssessmentTest(ureq, null);
				doUpdate(ase.getSection().getIdentifier(), ase.getSection().getTitle(), maxScoreWarning(ase.getSection()));
				doSaveManifest();
			}
		} else if(event instanceof AssessmentItemEvent) {
			AssessmentItemEvent aie = (AssessmentItemEvent)event;
			if(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED.equals(aie.getCommand())) {
				assessmentChanged(ureq);
				doSaveAssessmentTest(ureq, null);
				doUpdate(aie.getAssessmentItemRef().getIdentifier(), aie.getAssessmentItem().getTitle(), false);
				doUpdateParentSection(aie.getAssessmentItemRef().getIdentifier());
				doSaveManifest();
			} else if(AssessmentItemEvent.ASSESSMENT_ITEM_METADATA_CHANGED.equals(aie.getCommand())) {
				doSaveManifest();
			} else if(AssessmentItemEvent.ASSESSMENT_ITEM_NEED_RELOAD.equals(aie.getCommand())) {
				doReloadItem(ureq);
			}
		} else if(event instanceof DetachFromPoolEvent) {
			DetachFromPoolEvent dfpe = (DetachFromPoolEvent)event;
			doDetachItemFromPool(ureq, dfpe.getItemRef());
		} else if(event instanceof OpenTestConfigurationOverviewEvent) {
			doConfigurationOverview(ureq);
		} else if(event instanceof MetadataChangedEvent) {
			doSaveManifest();
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
		} else if(overviewConfigCtrl == source) {
			if(event instanceof SelectEvent) {
				SelectEvent se = (SelectEvent)event;
				if(doSelect(ureq, se.getControlObject(), se.getTarget())) {
					cleanUp();
				}
			}
			
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private boolean maxScoreWarning(AssessmentSection section) {
		int selectNum = section.getSelection() != null ? section.getSelection().getSelect() : 0;
		return selectNum > 0 && !QtiMaxScoreEstimator.sameMaxScore(section, resolvedAssessmentTest);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(overviewConfigCtrl);
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(selectQItemCtrl);
		removeAsListenerAndDispose(cmc);
		overviewConfigCtrl = null;
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
				if (MenuTree.COMMAND_TREENODE_CLICKED.equals(cmd)
						&& te.getNodeId() != null
						&& te.getNodeId().equals(menuTree.getSelectedNodeId())) {
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
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new SingleChoiceAssessmentItemBuilder(translate("new.sc"), translate("new.answer"), qtiService.qtiSerializer()));
		} else if(newMultipleChoiceLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new MultipleChoiceAssessmentItemBuilder(translate("new.mc"), translate("new.answer"), qtiService.qtiSerializer()));
		} else if(newKPrimLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new KPrimAssessmentItemBuilder(translate("new.kprim"), translate("new.answer"), qtiService.qtiSerializer()));
		} else if(newMatchLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new MatchAssessmentItemBuilder(translate("new.match"), QTI21Constants.CSS_MATCH_MATRIX, qtiService.qtiSerializer()));
		} else if(newMatchDragAndDropLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new MatchAssessmentItemBuilder(translate("new.matchdraganddrop"), QTI21Constants.CSS_MATCH_DRAG_AND_DROP, qtiService.qtiSerializer()));
		} else if(newMatchTrueFalseLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new MatchAssessmentItemBuilder(translate("new.matchtruefalse"), QTI21Constants.CSS_MATCH_TRUE_FALSE,
					translate("match.unanswered"), translate("match.true"), translate("match.false"), qtiService.qtiSerializer()));
		} else if(newFIBLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new FIBAssessmentItemBuilder(translate("new.fib"), EntryType.text, qtiService.qtiSerializer()));
		} else if(newNumericalLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new FIBAssessmentItemBuilder(translate("new.fib.numerical"), EntryType.numerical, qtiService.qtiSerializer()));
		} else if(newHotspotLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new HotspotAssessmentItemBuilder(translate("new.hotspot"), qtiService.qtiSerializer()));
		} else if(newHottextLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new HottextAssessmentItemBuilder(translate("new.hottext"), translate("new.hottext.start"), translate("new.hottext.text"), qtiService.qtiSerializer()));
		} else if(newOrderLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new OrderAssessmentItemBuilder(translate("new.order"), translate("new.answer"), qtiService.qtiSerializer()));
		} else if(newEssayLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new EssayAssessmentItemBuilder(translate("new.essay"), qtiService.qtiSerializer()));
		} else if(newUploadLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new UploadAssessmentItemBuilder(translate("new.upload"), qtiService.qtiSerializer()));
		} else if(newDrawingLink == source) {
			doNewAssessmentItem(ureq, menuTree.getSelectedNode(), new DrawingAssessmentItemBuilder(translate("new.drawing"), qtiService.qtiSerializer()));
		} else if(importFromPoolLink == source) {
			doSelectQItem(ureq);
		} else if(importFromTableLink == source) {
			doImportTable(ureq);
		} else if(exportToPoolLink == source) {
			doExportPool();
		} else if(deleteLink == source) {
			doConfirmDelete(ureq);
		} else if(copyLink == source) {
			doCopy(ureq);
		} else if(reloadInCacheLink == source) {
			doForceReloadFiles(ureq);
		} else if(configurationOverviewLink == source) {
			doConfigurationOverview(ureq);
		}
	}
	
	private boolean doSelect(UserRequest ureq, ControlObject<?> uobject, SelectionTarget target) {
		TreeNode selectedNode = TreeHelper.findNodeByUserObject(uobject, menuTree.getTreeModel().getRootNode());
		if(selectedNode != null) {
			toolbar.popUpToController(this);
			
			partEditorFactory(ureq, selectedNode);
			if(currentEditorCtrl instanceof Activateable2) {
				List<ContextEntry> entries = BusinessControlFactory.getInstance()
						.createCEListFromString(OresHelper.createOLATResourceableType(target.name()));
				((Activateable2)currentEditorCtrl).activate(ureq, entries, null);
			}
			if(currentEditorCtrl != null) {
				menuTree.setSelectedNode(selectedNode);
			}
			return currentEditorCtrl != null;
		}
		return false;
	}
	
	private void doSelectQItem(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(selectQItemCtrl);
		
		selectQItemCtrl = new SelectItemController(ureq, getWindowControl(), QTI21Constants.QTI_21_FORMAT);
		listenTo(selectQItemCtrl);

		String title = translate("title.add.from.pool");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), selectQItemCtrl.getInitialComponent(), true, title);
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
					&& (targetObject instanceof AssessmentSection || targetObject instanceof TestPart
							|| (targetObject instanceof AssessmentTest && ((AssessmentTest)targetObject).getTestParts().size() == 1))) {
				AssessmentSection droppedSection = (AssessmentSection)droppedObject;
				if(droppedSection.getParentSection() != null) {
					droppedSection.getParentSection().getSectionParts().remove(droppedSection);
				} else {
					droppedSection.getParent().getChildAbstractParts().remove(droppedSection);
				}
				if(targetObject instanceof AssessmentSection) {
					AssessmentSection targetSection = (AssessmentSection)targetObject;
					boolean shuffledSections = AssessmentTestFactory.shuffledSections(targetSection);
					targetSection.getChildAbstractParts().add(droppedSection);
					if(shuffledSections) {
						droppedSection.setFixed(Boolean.FALSE);
						droppedSection.setKeepTogether(Boolean.TRUE);
					}
				} else if(targetObject instanceof TestPart) {
					TestPart targetTestPart = (TestPart)targetObject;
					targetTestPart.getAssessmentSections().add(droppedSection);
				} else if(targetObject instanceof AssessmentTest) {
					TestPart targetTestPart = ((AssessmentTest)targetObject).getTestParts().get(0);
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
					boolean shuffledSections = AssessmentTestFactory.shuffledSections(targetParentSection);
					if(shuffledSections) {
						droppedSection.setFixed(Boolean.FALSE);
						droppedSection.setKeepTogether(Boolean.TRUE);
					}
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
		doSaveAssessmentTest(ureq, null);
		//reload a clean instance
		updateTreeModel(false);
		
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
		ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
		RootNodeLookup<AssessmentItem> rootNode = resolvedAssessmentItem.getItemLookup();
		AssessmentItem assessmentItem = rootNode.extractIfSuccessful();

		ManifestBuilder clonedManifestBuilder = ManifestBuilder.read(new File(unzippedDirRoot, "imsmanifest.xml"));
		ResourceType resource = getResourceType(clonedManifestBuilder, itemRef);
		ManifestMetadataBuilder metadata = clonedManifestBuilder.getMetadataBuilder(resource, true);
		if(metadata == null) {
			metadata = new ManifestMetadataBuilder();// not in imsmanifest.xml?
		}

		File itemFile = new File(rootNode.getSystemId());

		qti21QPoolServiceProvider
				.importAssessmentItemRef(getIdentity(), assessmentItem, itemFile, metadata, getLocale());
	}
	
	private void doImportTable(UserRequest ureq) {
		removeAsListenerAndDispose(importTableWizard);

		final AssessmentItemsPackage importPackage = new AssessmentItemsPackage();
		final ImportOptions options = new ImportOptions();
		options.setShuffle(true);
		Step start = new QImport_1_InputStep(ureq, importPackage, options, null);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			runContext.put("importPackage", importPackage);
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		importTableWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("tools.import.table"), "o_mi_table_import_wizard");
		listenTo(importTableWizard);
		getWindowControl().pushAsModalDialog(importTableWizard.getInitialComponent());
	}
	
	private void doInsert(UserRequest ureq, List<QuestionItemView> items) {
		TreeNode selectedNode = menuTree.getSelectedNode();
		TreeNode sectionNode = getNearestSection(selectedNode);
		if(sectionNode == null) {
			showWarning("error.missing.section");
			return;
		}

		boolean allOk = true;
		String firstItemId = null;
		Map<AssessmentItemRef,AssessmentItem> flyingObjects = new HashMap<>();
		try {
			AssessmentSection section = (AssessmentSection)sectionNode.getUserObject();
			for(QuestionItemView item:items) {
				QuestionItemFull qItem = qti21QPoolServiceProvider.getFullQuestionItem(item);
				String container =  qItem.getKey().toString();
				File questionContainer = new File(unzippedDirRoot, container);
				questionContainer.mkdir();
				
				AssessmentItem assessmentItem = qti21QPoolServiceProvider.exportToQTIEditor(qItem, getLocale(), questionContainer);
				if(assessmentItem != null) {
					AssessmentItemRef itemRef = doInsert(section, container, assessmentItem);
					if(firstItemId == null) {
						firstItemId = itemRef.getIdentifier().toString();
					}
					flyingObjects.put(itemRef, assessmentItem);
					
					ManifestMetadataBuilder metadata = manifestBuilder
							.getResourceBuilderByHref(itemRef.getHref().toString());
					metadata.appendMetadataFrom(qItem, assessmentItem, getLocale());
					metadata.setOpenOLATMetadataCopiedAt(new Date());
				} else {
					allOk &= false;
				}
			}
		} catch (IOException | URISyntaxException e) {
			showError("error.import.question");
			logError("", e);
		}
		
		if(!allOk) {
			showError("error.import.question");
		}
		
		if(firstItemId != null) {
			//persist metadata
			doSaveAssessmentTest(ureq, flyingObjects);
			doSaveManifest();
			updateTreeModel(false);
		
			TreeNode newItemNode = menuTree.getTreeModel().getNodeById(firstItemId);
			menuTree.setSelectedNode(newItemNode);
			menuTree.open(newItemNode);
			partEditorFactory(ureq, newItemNode);
		}
	}

	private void doInsert(UserRequest ureq, AssessmentItemsPackage importPackage) {
		TreeNode selectedNode = menuTree.getSelectedNode();
		TreeNode sectionNode = getNearestSection(selectedNode);
		if(sectionNode == null) {
			showWarning("error.missing.section");
			return;
		}
		
		String firstItemId = null;
		boolean errorOnImport = false;
		Map<AssessmentItemRef,AssessmentItem> flyingObjects = new HashMap<>();
		try {
			AssessmentSection section = (AssessmentSection)sectionNode.getUserObject();
			List<AssessmentItemAndMetadata> itemsAndMetadata = importPackage.getItems();
			for(AssessmentItemAndMetadata itemAndMetadata:itemsAndMetadata) {
				if(itemAndMetadata.isHasError()) {
					errorOnImport = true;
					continue;
				}
				
				AssessmentItemBuilder itemBuilder = itemAndMetadata.getItemBuilder();
				AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();
				AssessmentItemRef itemRef = doInsert(section, null, assessmentItem);
				ManifestMetadataBuilder metadata = manifestBuilder.getResourceBuilderByHref(itemRef.getHref().toString());
				metadata.setQtiMetadataInteractionTypes(itemBuilder.getInteractionNames());
				itemAndMetadata.toBuilder(metadata, getLocale());
				if(firstItemId == null) {
					firstItemId = itemRef.getIdentifier().toString();
				}
				flyingObjects.put(itemRef, assessmentItem);
			}
		} catch (URISyntaxException e) {
			errorOnImport = true;
			logError("", e);
		}
		if(errorOnImport) {
			showError("error.import.question");
		}
		
		if(firstItemId != null) {
			//persist metadata
			doSaveAssessmentTest(ureq, flyingObjects);
			doSaveManifest();
			updateTreeModel(false);
		
			TreeNode newItemNode = menuTree.getTreeModel().getNodeById(firstItemId);
			menuTree.setSelectedNode(newItemNode);
			menuTree.open(newItemNode);
			partEditorFactory(ureq, newItemNode);
		}
	}
	
	private AssessmentItemRef doInsert(AssessmentSection section, String container, AssessmentItem assessmentItem)
	throws URISyntaxException {
		AssessmentItemRef itemRef = new AssessmentItemRef(section);
		String itemId = assessmentItem.getIdentifier();
		itemRef.setIdentifier(Identifier.parseString(itemId));
		
		String itemFilename;
		if(StringHelper.containsNonWhitespace(container)) {
			itemFilename = container + "/" + itemId + ".xml";
		} else {
			itemFilename = itemId + ".xml";
		}
		File itemFile = new File(unzippedDirRoot, itemFilename);
		itemRef.setHref(new URI(itemFilename));
		section.getSectionParts().add(itemRef);
		
		qtiService.persistAssessmentObject(itemFile, assessmentItem);
		
		URI testUri = resolvedAssessmentTest.getTestLookup().getSystemId();
		File testFile = new File(testUri);
		qtiService.updateAssesmentObject(testFile, resolvedAssessmentTest);

		manifestBuilder.appendAssessmentItem(itemFilename);
		doSaveManifest();
		return itemRef;
	}
	
	private TreeNode doOpenFirstItem() {
		TreeNode rootNode = menuTree.getTreeModel().getRootNode();
		TreeNode node = null;
		if(rootNode.getChildCount() > 0) {
			node = doOpenFirstItem((TreeNode)rootNode.getChildAt(0));
		}
		if(node == null && rootNode.getChildCount() > 0) {
			node = (TreeNode)rootNode.getChildAt(0);
			menuTree.setSelectedNode(node);
			menuTree.open(node);
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
			for(int i=0; i<node.getChildCount(); i++) {
				TreeNode tNode = doOpenFirstItem((TreeNode)node.getChildAt(i));
				if(tNode != null) {
					return tNode;
				}
			}
		}
		return null;
	}
	
	private TreeNode doDetachItemFromPool(UserRequest ureq, AssessmentItemRef itemRef) {
		ManifestMetadataBuilder metadata = manifestBuilder.getResourceBuilderByHref(itemRef.getHref().toString());
		String identifier = metadata.getOpenOLATMetadataIdentifier();
		metadata.setOpenOLATMetadataMasterIdentifier(identifier);
		metadata.setOpenOLATMetadataIdentifier(UUID.randomUUID().toString());
		metadata.setOpenOLATMetadataCreator(getCreator());
		doSaveManifest();

		// reselect the node (--force)
		String itemId = itemRef.getIdentifier().toString();
		TreeNode newItemNode = menuTree.getTreeModel().getNodeById(itemId);
		menuTree.setSelectedNode(newItemNode);
		menuTree.open(newItemNode);
		return doReloadItem(ureq);
	}
	
	private String getCreator() {
		StringBuilder sb = new StringBuilder();
		String firstName = getIdentity().getUser().getFirstName();
		if(StringHelper.containsNonWhitespace(firstName)) {
			sb.append(firstName);
		}
		String lastName = getIdentity().getUser().getFirstName();
		if(StringHelper.containsNonWhitespace(lastName)) {
			if(StringHelper.containsNonWhitespace(firstName)) sb.append(" ");
			sb.append(lastName);
		}
		return sb.toString();
	}
	
	private TreeNode doReloadItem(UserRequest ureq) {
		TreeNode selectedNode = menuTree.getSelectedNode();
		updateTreeModel(false);
		if(selectedNode != null) {
			menuTree.setSelectedNodeId(selectedNode.getIdent());
			selectedNode = menuTree.getSelectedNode();
			partEditorFactory(ureq, selectedNode);
		}
		return selectedNode;	
	}
	
	/**
	 * Create a new test part and a section. Test part need a section,
	 * section ref as children, it's mandatory.
	 * 
	 * @param ureq
	 */
	private void doNewTestPart(UserRequest ureq) {
		TestPart testPart = AssessmentTestFactory.createTestPart(assessmentTestBuilder.getAssessmentTest());
		AssessmentTestFactory.appendAssessmentSection(translate("new.section"), testPart);
		
		//save the test
		doSaveAssessmentTest(ureq, null);
		//reload the test
		updateTreeModel(false);
		
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
			TreeNode rootNode = menuTree.getTreeModel().getRootNode();
			AssessmentTest assessmentTest = (AssessmentTest)rootNode.getUserObject();
			List<TestPart> parts = assessmentTest.getTestParts();
			if(parts != null && !parts.isEmpty()) {
				parentPart = parts.get(0);
			} else {
				showWarning("error.cannot.create.section");
				return;
			}
		}

		AssessmentSection newSection;
		if(parentPart instanceof TestPart) {
			newSection = AssessmentTestFactory.appendAssessmentSection(translate("new.section"), (TestPart)parentPart);
		} else if(parentPart instanceof AssessmentSection) {
			newSection = AssessmentTestFactory.appendAssessmentSection(translate("new.section"), (AssessmentSection)parentPart);
		} else {
			showWarning("error.cannot.create.section");
			return;
		}
	
		//save the test
		URI testUri = resolvedAssessmentTest.getTestLookup().getSystemId();
		File testFile = new File(testUri);
		qtiService.updateAssesmentObject(testFile, resolvedAssessmentTest);
		assessmentChanged(ureq);

		//reload the test
		updateTreeModel(false);
		
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
			if(sectionNode == null) {
				showWarning("error.missing.section");
				return;
			}
			AssessmentSection section = (AssessmentSection)sectionNode.getUserObject();
			
			AssessmentItemRef itemRef = new AssessmentItemRef(section);
			String itemId = IdentifierGenerator.newAsString(itemBuilder.getQuestionType().getPrefix());
			itemRef.setIdentifier(Identifier.parseString(itemId));
			File itemFile = new File(unzippedDirRoot, itemId + ".xml");
			itemRef.setHref(new URI(itemFile.getName()));
			section.getSectionParts().add(itemRef);
			
			AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();
			qtiService.persistAssessmentObject(itemFile, assessmentItem);
			
			Map<AssessmentItemRef,AssessmentItem> flyingObjects = Collections.singletonMap(itemRef, assessmentItem);
			
			doSaveAssessmentTest(ureq, flyingObjects);
			manifestBuilder.appendAssessmentItem(itemFile.getName());
			doSaveManifest();
			
			updateTreeModel(false);
			
			TreeNode newItemNode = menuTree.getTreeModel().getNodeById(itemId);
			menuTree.setSelectedNode(newItemNode);
			menuTree.open(newItemNode);

			partEditorFactory(ureq, newItemNode);
		} catch (URISyntaxException e) {
			logError("", e);
		}
	}
	
	private TreeNode getNearestSection(TreeNode node) {
		if(node == null) {
			node = menuTree.getTreeModel().getRootNode();
		}
		if(node.getUserObject() instanceof AssessmentTest) {
			//choose the first test part or section
			if(node.getChildCount() > 0) {
				node = (TreeNode)node.getChildAt(0);
			}
		}
		
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
	
	/**
	 * 
	 * @param flyingObjects A list of assessmentItems which are not part of the test but will be.
	 */
	private void doSaveAssessmentTest(UserRequest ureq, Map<AssessmentItemRef,AssessmentItem> flyingObjects) {
		assessmentChanged(ureq);
		recalculateMaxScoreAssessmentTest(flyingObjects);
		assessmentTestBuilder.build();
		URI testURI = resolvedAssessmentTest.getTestLookup().getSystemId();
		File testFile = new File(testURI);
		qtiService.updateAssesmentObject(testFile, resolvedAssessmentTest);
	
		ThreadLocalUserActivityLogger.log(QTI21LoggingAction.QTI_EDIT_RESOURCE, getClass());
	}
	
	private void recalculateMaxScoreAssessmentTest(Map<AssessmentItemRef,AssessmentItem> flyingObjects) {
		DoubleAdder atomicMaxScore = new DoubleAdder();
		
		AssessmentTest assessmentTest = (AssessmentTest)menuTree.getTreeModel().getRootNode().getUserObject();
		
		AssessmentTestHelper.visit(assessmentTest, new AssessmentTestVisitor() {
			@Override
			public void visit(TestPart testPart) { /* */ }
			
			@Override
			public void visit(SectionPart sectionPart) {
				if(sectionPart instanceof AssessmentItemRef) {
					AssessmentItemRef itemRef = (AssessmentItemRef)sectionPart;
					ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
					checkAndFixAbsolutePath(itemRef); 
					
					AssessmentItem assessmentItem = null;
					if(resolvedAssessmentItem != null) {
						assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
					}
					
					if(assessmentItem == null && flyingObjects != null && flyingObjects.containsKey(itemRef)) {
						assessmentItem = flyingObjects.get(itemRef);
					}
						
					if(assessmentItem != null) {
						Double maxScore = QtiNodesExtractor.extractMaxScore(assessmentItem);
						if(maxScore != null) {
							atomicMaxScore.add(maxScore.doubleValue());
						}
					}
				}
			}
		});

		double sumMaxScore = atomicMaxScore.sum();
		if(sumMaxScore > 0.0d) {
			assessmentTestBuilder.setMaxScore(sumMaxScore);
		} else {
			assessmentTestBuilder.setMaxScore(null);
		}
	}
	
	private void checkAndFixAbsolutePath(AssessmentItemRef itemRef) {
		if(itemRef == null || itemRef.getHref() == null) return;
		
		String href = itemRef.getHref().toString();
		if(isAbsolutePath(href)) {
			try {
				String relativeHref = fixAbsolutePath(href);
				itemRef.setHref(new URI(relativeHref));
			} catch (URISyntaxException e) {
				logError("", e);
			}
		}
	}
	
	private void checkAndFixAbsolutePath(ResourceType resource) {
		if(resource == null) return;
		
		if(isAbsolutePath(resource.getHref())) {
			resource.setHref(fixAbsolutePath(resource.getHref()));
			
			List<FileType> files = resource.getFile();
			if(files != null) {
				for(FileType file:files) {
					if(isAbsolutePath(file.getHref())) {
						file.setHref(fixAbsolutePath(file.getHref()));
					}
				}
			}
		}
	}
	
	/**
	 * It check if the path is absolute and in the form of a absolute within an openolat instance.
	 * @param href
	 * @return
	 */
	private boolean isAbsolutePath(String href) {
		return href != null && href.startsWith("/") && href.contains("/bcroot/repository/") && href.contains("/_unzipped_/");
	}
	
	private String fixAbsolutePath(String href) {
		int index = href.indexOf("/_unzipped_/") + ("/_unzipped_/").length();
		return href.substring(index);
	}
	
	private void doSaveManifest() {
		List<ResourceType> resources = manifestBuilder.getResourceList();
		for(ResourceType resource:resources) {
			checkAndFixAbsolutePath(resource);
		}
		manifestBuilder.write(new File(unzippedDirRoot, "imsmanifest.xml"));
	}
	
	private void doUpdateParentSection(Identifier identifier) {
		TreeNode node = menuTree.getTreeModel()
				.getNodeById(identifier.toString());
		for(INode parent=node.getParent(); parent.getParent() != null; parent=parent.getParent()) {
			if(parent instanceof TreeNode) {
				TreeNode parentNode = (TreeNode)parent;
				if(parentNode.getUserObject() instanceof AssessmentSection) {
					AssessmentSection section = (AssessmentSection)parentNode.getUserObject();
					doUpdate(section.getIdentifier(), section.getTitle(), maxScoreWarning(section));
				}
			}
		}
	}

	private void doUpdate(Identifier identifier, String newTitle, boolean warning) {
		doUpdate(identifier.toString(), newTitle, warning);
	}
	
	private void doUpdate(String identifier, String newTitle, boolean warning) {
		TreeNode node = menuTree.getTreeModel()
				.getNodeById(identifier);
		if(node instanceof GenericTreeNode) {
			GenericTreeNode itemNode = (GenericTreeNode)node;
			if(!newTitle.equals(itemNode.getTitle())) {
				itemNode.setTitle(newTitle);
				menuTree.setDirty(true);
				mainVC.contextPut("title", newTitle);
			}
			if(Boolean.compare(warning, StringHelper.containsNonWhitespace(itemNode.getIconDecorator1CssClass())) != 0) {
				if(warning) {
					itemNode.setIconDecorator1CssClass("o_midwarn");
				} else {
					itemNode.setIconDecorator1CssClass(null);
				}
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
			AssessmentTest test = (AssessmentTest)uobject;
			URI testURI = resolvedAssessmentTest.getTestLookup().getSystemId();
			File testFile = new File(testURI);
			TestPart uniqueTestPart = test.getTestParts().size() == 1 ? test.getTestParts().get(0) : null;
			currentEditorCtrl = new AssessmentTestEditorController(ureq, getWindowControl(), testEntry,
					assessmentTestBuilder, resolvedAssessmentTest, uniqueTestPart,
					unzippedDirRoot, unzippedContRoot, testFile, restrictedEdit);
		} else if(uobject instanceof TestPart) {
			currentEditorCtrl = new AssessmentTestPartEditorController(ureq, getWindowControl(), (TestPart)uobject,
					restrictedEdit, assessmentTestBuilder.isEditable());
		} else if(uobject instanceof AssessmentSection) {
			URI testURI = resolvedAssessmentTest.getTestLookup().getSystemId();
			File testFile = new File(testURI);
			currentEditorCtrl = new AssessmentSectionEditorController(ureq, getWindowControl(), (AssessmentSection)uobject,
					resolvedAssessmentTest, unzippedDirRoot, unzippedContRoot, testFile, restrictedEdit, assessmentTestBuilder.isEditable());
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
			if(restrictedEdit) {
				deleteLink.setVisible(false);
			} else if(uobject instanceof AssessmentSection || uobject instanceof AssessmentItemRef) {
				deleteLink.setVisible(true);
			} else if(uobject instanceof TestPart) {
				TestPart testPart = (TestPart)uobject;
				deleteLink.setVisible(testPart.getParent().getTestParts().size() > 1);
			} else {
				deleteLink.setVisible(false);
			}
		}
		
		if(copyLink != null) {
			if(restrictedEdit) {
				deleteLink.setVisible(false);
			} else {
				copyLink.setVisible(uobject instanceof AssessmentItemRef);
			}
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
		if(originalAssessmentItem == null) {
			showError("error.assessment.item");
			return;
		}
		QTI21QuestionType type = QTI21QuestionType.getType(originalAssessmentItem);
		
		String containerToCopy = getItemContainer(itemRefToCopy.getHref());
		String container = null;
		if(containerToCopy != null) {
			File containerToCopyDir = new File(unzippedDirRoot, containerToCopy);
			if(containerToCopyDir.exists()) {
				container = generateContainerName(containerToCopy);
				File containerDir = new File(unzippedDirRoot, container);
				containerDir.mkdir();
				FileUtils.copyDirContentsToDir(containerToCopyDir, containerDir, false, new XMLFileFilter(), "Copy question materials");
			}
		}

		File itemFile = null;
		try {
			AssessmentItemRef itemRef = new AssessmentItemRef(section);
			String itemId = IdentifierGenerator.newAsString(getTypePrefix(type));
			itemRef.setIdentifier(Identifier.parseString(itemId));
			
			String itemFilename;
			if(container == null) {
				itemFilename = itemId + ".xml";
			} else {
				itemFilename = container + "/" + itemId + ".xml";
			}

			itemFile = new File(unzippedDirRoot, itemFilename);
			itemRef.setHref(new URI(itemFilename));

			try(OutputStream out = new FileOutputStream(itemFile)) {
				//make the copy
				qtiService.qtiSerializer().serializeJqtiObject(originalAssessmentItem, out);
				
				//change identifier and title
				ResolvedAssessmentItem resolvedCopyItem = qtiService.loadAndResolveAssessmentItemForCopy(itemFile.toURI(), unzippedDirRoot);
				AssessmentItem copiedAssessmentItem = resolvedCopyItem.getRootNodeLookup().extractIfSuccessful();
				copiedAssessmentItem.setIdentifier(IdentifierGenerator.newAsString(getTypePrefix(type)));
				copiedAssessmentItem.setTitle(originalAssessmentItem.getTitle() + " (Copy)");
				qtiService.updateAssesmentObject(itemFile, resolvedCopyItem);
				
				//add to section
				section.getSectionParts().add(itemRef);
				
				Map<AssessmentItemRef, AssessmentItem> flyingObjects = Collections.singletonMap(itemRef, copiedAssessmentItem);
				doSaveAssessmentTest(ureq, flyingObjects);
				manifestBuilder.appendAssessmentItem(itemFilename);
				doSaveManifest();
			} catch (Exception e) {
				logError("", e);
			}

			updateTreeModel(false);
			
			TreeNode newItemNode = menuTree.getTreeModel().getNodeById(itemId);
			menuTree.setSelectedNode(newItemNode);
			menuTree.open(newItemNode);
			partEditorFactory(ureq, newItemNode);
		} catch (URISyntaxException e) {
			logError("", e);
		}
	}
	
	private String generateContainerName(String original) {
		String container = original + "_c";
		for(int i=1; i<1000; i++) {
			String containerName = container + i;
			if(!new File(unzippedDirRoot, containerName).exists()) {
				return containerName;
			}	
		}
		return CodeHelper.getUniqueID();
	}
	
	private String getItemContainer(URI itemUri) {
		String itemUriString = itemUri.toString();
		File file = new File(unzippedDirRoot, itemUriString);
		if(file.getParentFile().equals(unzippedDirRoot)) {
			return null;
		}
		
		String itemFilename = file.getName();
		return itemUriString.substring(0, itemUriString.length() - itemFilename.length() - 1);
	}
	
	private String getTypePrefix(QTI21QuestionType type) {
		return type == null ? QTI21QuestionType.unkown.getPrefix() : type.getPrefix();
	}
	
	private void doForceReloadFiles(UserRequest ureq) {
		updateTreeModel(true);
		assessmentChanged(ureq);
	}
	
	private void doConfigurationOverview(UserRequest ureq) {
		removeAsListenerAndDispose(overviewConfigCtrl);
		
		overviewConfigCtrl = new AssessmentTestOverviewConfigurationController(ureq, getWindowControl(), toolbar,
				testEntry, resolvedAssessmentTest, manifestBuilder);
		listenTo(overviewConfigCtrl);
		toolbar.pushController(translate("configuration.overview"), overviewConfigCtrl);
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		if(confirmDeleteCtrl != null) return;
		
		TreeNode selectedNode = menuTree.getSelectedNode();
		if(selectedNode == null) {
			showWarning("warning.atleastone");
			return;
		}
		
		Object uobject = selectedNode.getUserObject();
		if(uobject instanceof AssessmentTest) {
			showWarning("error.cannot.delete");
		} else if(uobject instanceof TestPart) {
			TestPart testPart = (TestPart)uobject;
			if(testPart.getParent().getTestParts().size() == 1) {
				showWarning("error.cannot.delete");
			}
			
			String msg = translate("delete.testPart");
			confirmDeleteCtrl = activateYesNoDialog(ureq, translate("tools.change.delete"), msg, confirmDeleteCtrl);
			confirmDeleteCtrl.setUserObject(selectedNode);
		} else {
			String msg;
			if(uobject instanceof AssessmentSection) {
				AssessmentSection section = (AssessmentSection)uobject;
				if(checkAtLeastOneSection(section)) {
					msg = translate("delete.section", selectedNode.getTitle());
				} else {
					showWarning("warning.atleastonesection");
					return;
				}
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
		if(uobject instanceof TestPart) {
			doDeleteTestPart(ureq, (TestPart)uobject);
		} else if(uobject instanceof AssessmentSection) {
			AssessmentSection section = (AssessmentSection)uobject;
			if(checkAtLeastOneSection(section)) {
				doDeleteAssessmentSection(ureq, section);
			} else {
				showWarning("warning.atleastonesection");
			}
		} else if(uobject instanceof AssessmentItemRef) {
			doDeleteAssessmentItemRef(ureq, (AssessmentItemRef)uobject);
		} else {
			return;//cannot delete test or test part
		}

		doSaveAssessmentTest(ureq, null);
		doSaveManifest();
		updateTreeModel(false);

		if(selectedNode != null && selectedNode.getParent() != null) {
			TreeNode parentNode = (TreeNode)selectedNode.getParent();
			menuTree.setSelectedNode(parentNode);
			menuTree.open(parentNode);
			partEditorFactory(ureq, parentNode);
		}
	}
	
	private boolean checkAtLeastOneSection(AssessmentSection section) {
		AbstractPart parent = section.getParent();
		if(parent instanceof TestPart) {
			TestPart testPart = (TestPart)parent;
			for(AssessmentSection testPartSection:testPart.getAssessmentSections()) {
				if(testPartSection != section) {
					return true;
				}
			}
			return false;
		}
		return true;
	}
	
	private void doDeleteAssessmentItemRef(UserRequest ureq, AssessmentItemRef itemRef) {
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
				List<AssessmentItemRef> itemRefs = resolvedAssessmentTest.getItemRefsBySystemIdMap().get(itemUri);
				if(itemRefs.size() <= 1) {
					File itemFile = new File(itemUri);
					deleted = itemFile.delete();
				}
			}
		}
		if(deleted) {
			assessmentChanged(ureq);
		}
		
		logAudit(removed + " " + deleted + " removed item ref");
	}
	
	private void doDeleteAssessmentSection(UserRequest ureq, AssessmentSection assessmentSection) {
		List<SectionPart> parts = new ArrayList<>(assessmentSection.getSectionParts());
		for(SectionPart part:parts) {
			if(part instanceof AssessmentItemRef) {
				doDeleteAssessmentItemRef(ureq, (AssessmentItemRef)part);
			} else if(part instanceof AssessmentSection) {
				doDeleteAssessmentSection(ureq, (AssessmentSection)part);
			}
		}
		
		if(assessmentSection.getParentSection() != null) {
			assessmentSection.getParentSection().getSectionParts().remove(assessmentSection);
		} else {
			assessmentSection.getParent().getChildAbstractParts().remove(assessmentSection);
		}
	}
	
	private void doDeleteTestPart(UserRequest ureq, TestPart testPart) {
		List<AssessmentSection> sections = new ArrayList<>(testPart.getAssessmentSections());
		for(AssessmentSection section:sections) {
			doDeleteAssessmentSection(ureq, section);
		}
		testPart.getParent().getTestParts().remove(testPart);
	}
	
	private void assessmentChanged(UserRequest ureq) {
		assessmentChanged = true;
		
		if(!deleteAuthorSesssion) {
			deleteAuthorSesssion = true;//delete sessions only once
			qtiService.deleteAuthorsAssessmentTestSession(testEntry);
			fireEvent(ureq, Event.CHANGED_EVENT);
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
	
	/**
	 * Exclude file with XML extension.
	 * 
	 * Initial date: 23 juil. 2020<br>
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 *
	 */
	private static class XMLFileFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			String filename = pathname.getName().toLowerCase();
			return !filename.endsWith(".xml");
		}
	}
}