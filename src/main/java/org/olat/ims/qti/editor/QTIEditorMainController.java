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

package org.olat.ims.qti.editor;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.SelectionTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.VetoableCloseController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dialog.DialogController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.memento.Memento;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.tree.TreePosition;
import org.olat.fileresource.types.FileResource;
import org.olat.ims.qti.QTIChangeLogMessage;
import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.editor.beecom.objects.ChoiceQuestion;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.QTIObject;
import org.olat.ims.qti.editor.beecom.objects.Question;
import org.olat.ims.qti.editor.beecom.objects.Response;
import org.olat.ims.qti.editor.beecom.objects.Section;
import org.olat.ims.qti.editor.beecom.parser.ItemParser;
import org.olat.ims.qti.editor.tree.AssessmentNode;
import org.olat.ims.qti.editor.tree.GenericQtiNode;
import org.olat.ims.qti.editor.tree.InsertItemTreeModel;
import org.olat.ims.qti.editor.tree.ItemNode;
import org.olat.ims.qti.editor.tree.QTIEditorTreeModel;
import org.olat.ims.qti.editor.tree.SectionNode;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.QTIEditorResolver;
import org.olat.ims.qti.qpool.QTIQPoolServiceProvider;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.iq.IQDisplayController;
import org.olat.modules.iq.IQManager;
import org.olat.modules.iq.IQPreviewSecurityCallback;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.ui.QItemEvent;
import org.olat.modules.qpool.ui.SelectItemController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.references.ReferenceImpl;

/**
 * Description: <br>
 * QTIEditorMainController is started from within the repository. A persistent
 * lock is set to prevent more than one user working on the same document, even
 * if the current working author has no active session. If the document is
 * already linked to a node in a course, it is opened for corrections only. This
 * restricted editing function prohibits structural changes which would
 * interfere with already existing results.
 * <p>
 * Furthermore, if a document is loaded into the editor, it is not available for
 * linking in a course. Therefore, a document in the editor can always be saved
 * back safely to the repository. But it must be locked that users starting the
 * document from an already referencing building block wait until the edited
 * document is committed completly to the repository.
 * <P>
 * Initial Date: Oct 21, 2004 <br>
 * 
 * @author mike
 */
public class QTIEditorMainController extends MainLayoutBasicController implements VetoableCloseController {
	/*
	 * Toolbox Commands
	 */
	private static final String CMD_TOOLS_CLOSE_EDITOR = "cmd.close";
	private static final String CMD_TOOLS_PREVIEW = "cmd.preview";
	private static final String CMD_TOOLS_CHANGE_MOVE = "cmd.move";
	private static final String CMD_TOOLS_CHANGE_COPY = "cmd.copy";
	private static final String CMD_TOOLS_CHANGE_DELETE = "cmd.delete";
	private static final String CMD_TOOLS_ADD_PREFIX = "cmd.add.";
	private static final String CMD_TOOLS_ADD_FREETEXT = CMD_TOOLS_ADD_PREFIX + "essay";
	private static final String CMD_TOOLS_ADD_FIB = CMD_TOOLS_ADD_PREFIX + "fib";
	private static final String CMD_TOOLS_ADD_MULTIPLECHOICE = CMD_TOOLS_ADD_PREFIX + "mc";
	private static final String CMD_TOOLS_ADD_SINGLECHOICE = CMD_TOOLS_ADD_PREFIX + "sc";
	private static final String CMD_TOOLS_ADD_KPRIM = CMD_TOOLS_ADD_PREFIX + "kprim";
	private static final String CMD_TOOLS_ADD_SECTION = CMD_TOOLS_ADD_PREFIX + "section";
	private static final String CMD_TOOLS_ADD_QPOOL = "cmd.import.qpool";
	private static final String CMD_TOOLS_EXPORT_QPOOL = "cmd.export.qpool";

	private static final String CMD_EXIT_SAVE = "exit.save";
	private static final String CMD_EXIT_DISCARD = "exit.discard";
	private static final String CMD_EXIT_CANCEL = "exit.cancel";
	
	// REVIEW:2008-11-20: patrickb, scalability project issue -> read/write lock in distributed system
	//
	// Problem:
	// - Editor Session holds a copy to work, in case the work copy is "committed" e.g. saved - the qti file(s)
	//   are copied and replaced -> this may lead to "uncommitted" reads of users starting the qti test, during the
	//   very same moment the files are written.
	// - Because qti tests may hold media files and the like the copying and replacing can last surprisingly long.
	//
	// This means saving a test must be an exclusive operation. Reads for test sessions should be concurrent.
	//
	// History of solutions:
	// 1) An OLAT wide lock (object) was used -> possible congestion, delay if many qti tests are started or edited.
	// 2) Read/Write Lock used to grant non-congestion on reading, only OLAT wide write lock - still not optimal
	// 2a) Optimal solution in non-distributed system (singleVM) - ReadWriteLock on specific resource, instead OLAT wide.
	// 3) Scalability Project: how often does it happen compared to how often a test is started only?
	//    => pragmatic solution to protect but not slow down starting of many concurrent readers.
	//    An open and active editor session for a specific test -> possible writes!! => no reads
	//    An open but not active editor session for a specific test -> no possible writes to expect. => allow reads
	//    No open
  // ----|start editor session=>copy files for working copy | work on copy |     close browser                     | restart work on copy   | commit work|---
	// ----|~~~~~~~~~~~~~~~~~~~~~~~~~~~~active session  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|  Sessiontimeout    |~~~~ active sesson ~~~~~~~~~~~~~~~~~|      
	//                                  qti file locked                                             ,no more active         qti file locked    
	//                                                                                              session, qti file
	//                                                                                              free for read
	// Case 1: Starting editor session (acquiring lock) at the same time as starting some qti test session(s) (polling lock only)
	//          => only a problem if user exits and saves as fast as possible and some big qti file. (unlikely)
	// Case 2: Active editor session (acquired lock) and trying to start test
	//          => annoying for users in the course, not best service of the LMS for its authors, but pragmatic. (rare case) 
	// Case 3: Closing editor session (releasing lock) while somebody tries to start the test (polling lock only)
	//          => annoying for user, as he just missed it for some milliseconds to be able to start. (very rare case)
	// Case 4: No qti editor session or no active editor session
	//          => benefit of fast starting qti tests, best service for the LMS clients (98.512% of cases)
	// 
	// This leads to the solution as follows:
	// - (as it was already the case) A persistent lock for started qti sessions, used to prevent multiple authors "branching" test versions and overwriting changes of the others.
	// - a non persistent GUI lock to signal an active editor session, this can be polled before starting a qti test.
	// - lock out qti readers in the case of an active editor session
	//
	//public static final ReentrantReadWriteLock IS_SAVING_RWL = new ReentrantReadWriteLock();

	private QTIEditorPackageImpl qtiPackage;

	private VelocityContainer main, exitVC, chngMsgFormVC, restrictedEditWarningVC;
	private ToolController mainToolC;
	private MenuTree menuTree;
	private Panel mainPanel;
	private LayoutMain3ColsController columnLayoutCtr;

	private QTIEditorTreeModel menuTreeModel;
	private DialogBoxController deleteDialog;
	private DialogBoxController deleteMediaDialog;
	private IQDisplayController previewController;
	private SelectionTree moveTree, copyTree, insertTree;
	private InsertItemTreeModel insertTreeModel;
	private LockResult lockEntry;
	private boolean restrictedEdit;
	private Map history = null;
	private String startedWithTitle;
	private List<ReferenceImpl> referencees;
	private ChangeMessageForm chngMsgFrom;
	private DialogController proceedRestricedEditDialog;
	private ContactMessage changeEmail;
	private ContactFormController cfc;
	private String changeLog = null;
	private CloseableModalController cmc, cmcPrieview, cmcExit;
	private SelectItemController selectQItemCtrl;
	private Panel exitPanel;
	private boolean notEditable;
	private LockResult activeSessionLock;
	private Link notEditableButton; 
	private Set<String> deletableMediaFiles;

	private final QPoolService qpoolService;
	private final QTIQPoolServiceProvider qtiQpoolServiceProvider;
	
	public QTIEditorMainController(List<ReferenceImpl> referencees, UserRequest ureq, WindowControl wControl, FileResource fileResource) {
		super(ureq, wControl);
		
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		qtiQpoolServiceProvider = (QTIQPoolServiceProvider)CoreSpringFactory.getBean("qtiPoolServiceProvider");

		for(Iterator<ReferenceImpl> iter = referencees.iterator(); iter.hasNext(); ) {
			ReferenceImpl ref = iter.next();
			if ("CourseModule".equals(ref.getSource().getResourceableTypeName())) {
				ICourse course = CourseFactory.loadCourse(ref.getSource().getResourceableId());
				CourseNode courseNode = course.getEditorTreeModel().getCourseNode(ref.getUserdata());
				String repositorySoftKey = (String) courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		    Long repKey = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
				List<QTIResult> results = QTIResultManager.getInstance().selectResults(course.getResourceableId(), courseNode.getIdent(), repKey, 1);
				this.restrictedEdit = ((CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(course, null)) || (results != null && results.size() > 0)) ? true : false;
			}
			if(restrictedEdit) break;
		}
		if(CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(fileResource, null)) {
			this.restrictedEdit = true;
		}
		this.referencees = referencees;
		
		qtiPackage = new QTIEditorPackageImpl(ureq.getIdentity(), fileResource, getTranslator());

		// try to get lock which lives longer then the browser session in case of a closing browser window
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker().aquirePersistentLock(qtiPackage.getRepresentingResourceable(), ureq.getIdentity(), null);
		if (lockEntry.isSuccess()) {
			// acquired a lock for the duration of the session only
			//fileResource has the RepositoryEntre.getOlatResource within, which is used in qtiPackage
			activeSessionLock = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(qtiPackage.getRepresentingResourceable(), ureq.getIdentity(), null);
			//
			if (qtiPackage.getQTIDocument() == null) {
				notEditable = true;				
			} else if (qtiPackage.isResumed()) {
				showInfo("info.resumed", null);
			}
			//
			init(ureq); // initialize the gui
		} else {
			wControl.setWarning( getTranslator().translate("error.lock", new String[] { lockEntry.getOwner().getName(),
				Formatter.formatDatetime(new Date(lockEntry.getLockAquiredTime())) }) );
		}
	}

	/**
	 * This constructor may only be used for new or non-referenced QTI files!
	 * 
	 * @param ureq
	 * @param wControl
	 * @param fileResource
	 */
	public QTIEditorMainController(UserRequest ureq, WindowControl wControl, FileResource fileResource) {
		// super(wControl) is called in referenced constructor
		// null as value for the List referencees sets restrictedEdit := false;
		this(null, ureq, wControl, fileResource);
	}

	private void init(UserRequest ureq) {
		main = createVelocityContainer("index");
		JSAndCSSComponent jsAndCss;
		// Add html header js
		jsAndCss = new JSAndCSSComponent("qitjsandcss", new String[] { "js/openolat/qti.js" }, null);
		main.put("qitjsandcss", jsAndCss);
		
		//
		mainPanel = new Panel("p_qti_editor");
		mainPanel.setContent(main);
		//
		if(notEditable) {		
			//test not editable
			VelocityContainer notEditable = createVelocityContainer("notEditable");
			notEditableButton = LinkFactory.createButton("ok", notEditable, this);
			Panel panel = new Panel("notEditable");
			panel.setContent(notEditable);
			columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, null, panel, null);
			putInitialPanel(columnLayoutCtr.getInitialComponent());
			return;
		}
				
		mainToolC = populateToolC(); // qtiPackage must be loaded previousely
		listenTo(mainToolC);
		
		// initialize the history
		if (qtiPackage.isResumed() && qtiPackage.hasSerializedChangelog()) {
			// there were already changes made -> reload!
			history = qtiPackage.loadChangelog();
		} else {
			// start with a fresh history. Editor is resumed but no changes were made
			// so far.
			history = new HashMap();
		}

		if (restrictedEdit) {
			mainToolC.setEnabled(CMD_TOOLS_ADD_SECTION, false);
			mainToolC.setEnabled(CMD_TOOLS_ADD_SINGLECHOICE, false);
			mainToolC.setEnabled(CMD_TOOLS_ADD_MULTIPLECHOICE, false);
			mainToolC.setEnabled(CMD_TOOLS_ADD_QPOOL, false);

			mainToolC.setEnabled(CMD_TOOLS_ADD_FIB, false);
			if (!qtiPackage.getQTIDocument().isSurvey()) mainToolC.setEnabled(CMD_TOOLS_ADD_KPRIM, false);
			if (qtiPackage.getQTIDocument().isSurvey()) mainToolC.setEnabled(CMD_TOOLS_ADD_FREETEXT, false);
		}
		mainToolC.setEnabled(CMD_TOOLS_CHANGE_DELETE, false);
		mainToolC.setEnabled(CMD_TOOLS_CHANGE_MOVE, false);
		mainToolC.setEnabled(CMD_TOOLS_CHANGE_COPY, false);

		// The menu tree model represents the structure of the qti document.
		// All insert/move operations on the model are propagated to the structure
		// by the node
		menuTreeModel = new QTIEditorTreeModel(qtiPackage);
		menuTree = new MenuTree("QTIDocumentTree");
		menuTree.setTreeModel(menuTreeModel);
		menuTree.setSelectedNodeId(menuTree.getTreeModel().getRootNode().getIdent());
		menuTree.addListener(this);// listen to the tree
		// remember the qtidoc title when we started this editor, to correctly name
		// the history report
		this.startedWithTitle = menuTree.getSelectedNode().getAltText();
		//
		main.put("tabbedPane", menuTreeModel.getQtiRootNode().createEditTabbedPane(ureq, getWindowControl(), getTranslator(), this));
		main.contextPut("qtititle", menuTreeModel.getQtiRootNode().getAltText());
		main.contextPut("isRestrictedEdit", restrictedEdit ? Boolean.TRUE : Boolean.FALSE);
		//
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, mainToolC.getInitialComponent(), mainPanel, "qtieditor" + qtiPackage.getRepresentingResourceable());
		listenTo(columnLayoutCtr);
		// Add css background
		if (restrictedEdit) {
			columnLayoutCtr.addCssClassToMain("o_editor_qti_correct");
		} else {
			columnLayoutCtr.addCssClassToMain("o_editor_qti");
		}
		putInitialPanel(columnLayoutCtr.getInitialComponent());
		
		if (restrictedEdit) {
			restrictedEditWarningVC = createVelocityContainer("restrictedEditDialog");
			proceedRestricedEditDialog = new DialogController(getWindowControl(), ureq.getLocale(), translate("yes"), translate("no"),translate("qti.restricted.edit.warning")+"<br/><br/>"+createReferenceesMsg(ureq), null, true, null);
			listenTo(proceedRestricedEditDialog);
			restrictedEditWarningVC.put("dialog", proceedRestricedEditDialog.getInitialComponent());
			// we would like to us a modal dialog here, but this does not work! we
			// can't push to stack because the outher workflows pushes us after the
			// controller to the stack. Thus, if we used a modal dialog here the
			// dialog would never show up. 
			columnLayoutCtr.setCol3(restrictedEditWarningVC);
			columnLayoutCtr.hideCol1(true);
			columnLayoutCtr.hideCol2(true);
		}
	}


	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//
		if (source == menuTree) { // catch menu tree clicks
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				GenericQtiNode clickedNode;				
				clickedNode = menuTreeModel.getQtiNode(menuTree.getSelectedNodeId());				
				TabbedPane tabbedPane = clickedNode.createEditTabbedPane(ureq, getWindowControl(), getTranslator(), this);
				if(tabbedPane!=null) {
					main.put("tabbedPane",tabbedPane);
				} else {
					VelocityContainer itemNotEditable = createVelocityContainer("tab_itemAlien");						
					main.put("tabbedPane", itemNotEditable);
					return;					
				}
				
				// enable/disable delete and move
				// if (!restrictedEdit) {
				// only available in full edit mode
				if (clickedNode instanceof AssessmentNode) {
					mainToolC.setEnabled(CMD_TOOLS_CHANGE_DELETE, false);
					mainToolC.setEnabled(CMD_TOOLS_CHANGE_MOVE, false);
					mainToolC.setEnabled(CMD_TOOLS_CHANGE_COPY, false);
				} else {
					mainToolC.setEnabled(CMD_TOOLS_CHANGE_DELETE, true && !restrictedEdit);
					mainToolC.setEnabled(CMD_TOOLS_CHANGE_MOVE, true && !restrictedEdit);
					if (clickedNode instanceof ItemNode) {
						mainToolC.setEnabled(CMD_TOOLS_CHANGE_COPY, true && !restrictedEdit);
					} else {
						mainToolC.setEnabled(CMD_TOOLS_CHANGE_COPY, false);
					}
				}
				// }
			}
		} else if (source == moveTree) { // catch move operations
			cmc.deactivate();
			removeAsListenerAndDispose(cmc);
			cmc = null;
			
			TreeEvent te = (TreeEvent) event;
			if (te.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) {
				// user chose a position to insert a new node
				String nodeId = te.getNodeId();
				TreePosition tp = insertTreeModel.getTreePosition(nodeId);
				GenericQtiNode parentTargetNode = (GenericQtiNode) tp.getParentTreeNode();
				int targetPos = tp.getChildpos();
				GenericQtiNode selectedNode = (GenericQtiNode) menuTree.getSelectedNode();
				int selectedPos = selectedNode.getPosition();
				GenericQtiNode parentSelectedNode = (GenericQtiNode) selectedNode.getParent();
				if (parentTargetNode == parentSelectedNode) {
					// if we're on the same subnode
					if (targetPos > selectedNode.getPosition()) {
						// if we're moving after our current position
						targetPos--;
						// decrease insert pos since we're going to be removed from the
						// parent before re-insert
					}
				}
				// insert into menutree (insert on GenericNode do a remove from parent)
				parentTargetNode.insert(selectedNode, targetPos);
				// insert into model (remove from parent needed prior to insert)
				QTIObject subject = parentSelectedNode.removeQTIObjectAt(selectedPos);
				parentTargetNode.insertQTIObjectAt(subject, targetPos);
				qtiPackage.serializeQTIDocument();
				menuTree.setDirty(true); //force rerendering for ajax mode
			}
		} else if (source == copyTree) { // catch copy operations
			cmc.deactivate();
			removeAsListenerAndDispose(cmc);
			cmc = null;
			
			TreeEvent te = (TreeEvent) event;
			if (te.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) {
				// user chose a position to insert the node to be copied
				String nodeId = te.getNodeId();
				TreePosition tp = insertTreeModel.getTreePosition(nodeId);
				int targetPos = tp.getChildpos();
				ItemNode selectedNode = (ItemNode) menuTree.getSelectedNode();
				// only items are moveable
				// use XStream instead of ObjectCloner
				// Item qtiItem =
				// (Item)xstream.fromXML(xstream.toXML(selectedNode.getUnderlyingQTIObject()));
				Item toClone = (Item) selectedNode.getUnderlyingQTIObject();
				Item qtiItem = (Item) XStreamHelper.xstreamClone(toClone);
				// copy flow label class too, olat-2791
				Question orgQuestion = toClone.getQuestion();
				if (orgQuestion instanceof ChoiceQuestion) {
					String flowLabelClass = ((ChoiceQuestion)orgQuestion).getFlowLabelClass();
					Question copyQuestion =  qtiItem.getQuestion();
					if (copyQuestion instanceof ChoiceQuestion) {
						((ChoiceQuestion)copyQuestion).setFlowLabelClass(flowLabelClass);
					} else {
						throw new AssertException("Could not copy flow-label-class, wrong type of copy question , must be 'ChoiceQuestion' but is " +copyQuestion);
					}
				}
				String editorIdentPrefix = "";
				if (qtiItem.getIdent().startsWith(ItemParser.ITEM_PREFIX_SCQ)) editorIdentPrefix = ItemParser.ITEM_PREFIX_SCQ;
				else if (qtiItem.getIdent().startsWith(ItemParser.ITEM_PREFIX_MCQ)) editorIdentPrefix = ItemParser.ITEM_PREFIX_MCQ;
				else if (qtiItem.getIdent().startsWith(ItemParser.ITEM_PREFIX_KPRIM)) editorIdentPrefix = ItemParser.ITEM_PREFIX_KPRIM;
				else if (qtiItem.getIdent().startsWith(ItemParser.ITEM_PREFIX_FIB)) editorIdentPrefix = ItemParser.ITEM_PREFIX_FIB;
				else if (qtiItem.getIdent().startsWith(ItemParser.ITEM_PREFIX_ESSAY)) editorIdentPrefix = ItemParser.ITEM_PREFIX_ESSAY;
				// set new ident... this is all it needs for our engine to recognise it
				// as a new item.
				qtiItem.setIdent(editorIdentPrefix + CodeHelper.getForeverUniqueID());
				// insert into menutree (insert on GenericNode do a remove from parent)
				GenericQtiNode parentTargetNode = (GenericQtiNode) tp.getParentTreeNode();
				GenericQtiNode newNode = new ItemNode(qtiItem, qtiPackage);
				parentTargetNode.insert(newNode, targetPos);
				// insert into model
				parentTargetNode.insertQTIObjectAt(qtiItem, targetPos);
				// activate copied node
				menuTree.setSelectedNodeId(newNode.getIdent());
				event(ureq, menuTree, new Event(MenuTree.COMMAND_TREENODE_CLICKED));
				qtiPackage.serializeQTIDocument();
			}
		} else if (source == insertTree) { // catch insert operations
			cmc.deactivate();
			removeAsListenerAndDispose(cmc);
			cmc = null;
			
			TreeEvent te = (TreeEvent) event;
			if (te.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) { // insert
				doInsert(ureq, te.getNodeId(), insertTree.getUserObject());
			}
		} else if (source == exitVC) {
			if (event.getCommand().equals(CMD_EXIT_SAVE)) {
				if (isRestrictedEdit() && history.size() > 0) {
					// changes were recorded
					// start work flow:
					// -sending an e-mail to everybody being a stake holder of this qti
					// resource
					// -email with change message
					// -after sending email successfully -> saveNexit is called.
					chngMsgFormVC = createVelocityContainer("changeMsgForm");
					// FIXME:pb:a Bitte diesen Velocity container entfernen und statt
					// dessen den
					// ContentOnlyController verwenden. Es ist äusserst wichtig dass das
					// Layout nie selber gemacht
					// wird sondern immer die Layout controller verwendet werden, d.h. den
					// ContentOnlyController oder
					// den MenuAndToolController. Dort kann das Tool übrigens auch null
					// sein wenn man nur ein Menü braucht.
					// TODO:pb:a extend ContentOnlyController to work also if menu and
					// tool are null, hence only content is desired
					String userN = ureq.getIdentity().getName();
					String lastN = ureq.getIdentity().getUser().getProperty(UserConstants.LASTNAME, ureq.getLocale());
					String firstN = ureq.getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, ureq.getLocale());
					String changeMsg = "Changed by: " + firstN + " " + lastN + " [" + userN + "]\n";
					changeMsg += createChangeMessage();
					changeEmail.setBodyText(changeMsg);
					chngMsgFormVC.contextPut("chngMsg", changeEmail.getBodyText());
					removeAsListenerAndDispose(chngMsgFrom);
					chngMsgFrom = new ChangeMessageForm(ureq, getWindowControl());
					listenTo(chngMsgFrom);
					chngMsgFormVC.put("chngMsgForm", chngMsgFrom.getInitialComponent());
					exitPanel.setContent(chngMsgFormVC);
					
					return;
				} else {
					// remove modal dialog and proceed with exit process
					cmcExit.deactivate();
					removeAsListenerAndDispose(cmcExit);
					cmcExit = null;
					// remove lock, clean tmp dir, fire done event to close editor
					saveAndExit(ureq);
				}
			} else if (event.getCommand().equals(CMD_EXIT_DISCARD)) {
				// remove modal dialog and proceed with exit process
				cmcExit.deactivate();
				removeAsListenerAndDispose(cmcExit);
				cmcExit = null;
				// cleanup, so package does not get resumed
				qtiPackage.cleanupTmpPackageDir();
				// remove lock
				removeLocksAndExit(ureq);
				
			} else if (event.getCommand().equals(CMD_EXIT_CANCEL)) {
				// remove modal dialog and go back to edit mode
				cmcExit.deactivate();
				removeAsListenerAndDispose(cmcExit);
				cmcExit = null;
			}
			
		} else if (source == notEditableButton) {
			fireEvent(ureq, Event.DONE_EVENT); // close editor
		}
	}

	private void removeLocksAndExit(UserRequest ureq) {
		// remove lock
		if (lockEntry.isSuccess()){
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(activeSessionLock);
			CoordinatorManager.getInstance().getCoordinator().getLocker().releasePersistentLock(lockEntry);
		}
		fireEvent(ureq, Event.DONE_EVENT); // close editor
	}

	private void saveAndExit(UserRequest ureq) {
		boolean saveOk = false;
		//
		// acquire write lock
		//IS_SAVING_RWL.writeLock().lock();
		// synchronized(IS_SAVING){
		//try {
			saveOk = qtiPackage.savePackageToRepository();
		//} finally {
		//	IS_SAVING_RWL.writeLock().unlock();
		//}
		// }// release write lock
		if (!saveOk) {
			getWindowControl().setError(translate("error.save"));
			return;
		}
		// cleanup, so package does not get resumed
		qtiPackage.cleanupTmpPackageDir();
		removeLocksAndExit(ureq);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == mainToolC) {
			String cmd = event.getCommand();
			if (cmd.equals(CMD_TOOLS_CLOSE_EDITOR)) { // exitVC hook:
				// save package back to repository
				exitVC = createVelocityContainer("exitDialog");
				exitPanel = new Panel("exitPanel");
				exitPanel.setContent(exitVC);
				cmcExit = new CloseableModalController(getWindowControl(), translate("editor.preview.close"), exitPanel);
				cmcExit.activate();
				listenTo(cmcExit);
				return;
				
			} else if (cmd.equals(CMD_TOOLS_PREVIEW)) { // preview
				previewController = IQManager.getInstance().createIQDisplayController(new QTIEditorResolver(qtiPackage),
						qtiPackage.getQTIDocument().isSurvey() ? AssessmentInstance.QMD_ENTRY_TYPE_SURVEY : AssessmentInstance.QMD_ENTRY_TYPE_SELF,
						new IQPreviewSecurityCallback(), ureq, getWindowControl());
				if (previewController.isReady()) {
					// in case previewController was unable to initialize, a message was
					// set by displayController
					// this is the case if no more attempts or security check was
					// unsuccessfull
					previewController.addControllerListener(this);
					cmcPrieview = new CloseableModalController(getWindowControl(), translate("editor.preview.close"),
							previewController.getInitialComponent());
					cmcPrieview.activate();
					listenTo(cmcPrieview);
					
				} else {
					getWindowControl().setWarning(translate("error.preview"));
				}
			} else if (cmd.equals(CMD_TOOLS_CHANGE_DELETE)) { // prepare delete

				GenericQtiNode clickedNode = menuTreeModel.getQtiNode(menuTree.getSelectedNodeId());
				String msg = "";
				if (clickedNode instanceof SectionNode) {
					if (QTIEditHelper.countSections(qtiPackage.getQTIDocument().getAssessment()) == 1) {
						// At least one section
						getWindowControl().setError(translate("error.atleastonesection"));
						return;
					}
					msg = translate("delete.section", clickedNode.getTitle());
				} else if (clickedNode instanceof ItemNode) {
					if (((SectionNode) clickedNode.getParent()).getChildCount() == 1) {
						// At least one item
						getWindowControl().setError(translate("error.atleastoneitem"));
						return;
					}
					msg = translate("delete.item", clickedNode.getTitle());
				}
				deleteDialog = activateYesNoDialog(ureq, null, msg, deleteDialog);
				deleteDialog.setUserObject(clickedNode);
				return;
			} else if (cmd.equals(CMD_TOOLS_CHANGE_MOVE)) {			
			  //cannot move the last item
				GenericQtiNode clickedNode = menuTreeModel.getQtiNode(menuTree.getSelectedNodeId());
				if (clickedNode instanceof ItemNode && ((SectionNode) clickedNode.getParent()).getChildCount() == 1) {				
					getWindowControl().setError(translate("error.move.atleastoneitem"));
					return;
				}
				TreeNode selectedNode = menuTree.getSelectedNode();
				moveTree = new SelectionTree("moveTree", getTranslator());
				moveTree.setFormButtonKey("submit");
				insertTreeModel = new InsertItemTreeModel(menuTreeModel,
						(selectedNode instanceof SectionNode) ? InsertItemTreeModel.INSTANCE_ASSESSMENT : InsertItemTreeModel.INSTANCE_SECTION);
				moveTree.setTreeModel(insertTreeModel);
				moveTree.addListener(this);
				cmc = new CloseableModalController(getWindowControl(),translate("close"), moveTree, true, translate("title.move"));
				cmc.activate();
				listenTo(cmc);
				
			} else if (cmd.equals(CMD_TOOLS_CHANGE_COPY)) {
				copyTree = new SelectionTree("copyTree", getTranslator());
				copyTree.setFormButtonKey("submit");
				insertTreeModel = new InsertItemTreeModel(menuTreeModel, InsertItemTreeModel.INSTANCE_SECTION);
				copyTree.setTreeModel(insertTreeModel);
				copyTree.addListener(this);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), copyTree, true, translate("title.copy"));
				cmc.activate();
				listenTo(cmc);
				
			} else if (CMD_TOOLS_ADD_QPOOL.equals(cmd)) {
				doSelectQItem(ureq);
			} else if (CMD_TOOLS_EXPORT_QPOOL.equals(cmd)) {
				doExportQItem(ureq);
			} else if (cmd.startsWith(CMD_TOOLS_ADD_PREFIX)) { // add new object
				// fetch new object
				GenericQtiNode insertObject = null;
				if (cmd.equals(CMD_TOOLS_ADD_SECTION)) {
					Section newSection = QTIEditHelper.createSection(getTranslator());
					Item newItem = QTIEditHelper.createSCItem(getTranslator());
					newSection.getItems().add(newItem);
					SectionNode scNode = new SectionNode(newSection, qtiPackage);
					ItemNode itemNode = new ItemNode(newItem, qtiPackage);
					scNode.addChild(itemNode);
					insertObject = scNode;
				} else if (cmd.equals(CMD_TOOLS_ADD_SINGLECHOICE)) {
					insertObject = new ItemNode(QTIEditHelper.createSCItem(getTranslator()), qtiPackage);
				} else if (cmd.equals(CMD_TOOLS_ADD_MULTIPLECHOICE)) {
					insertObject = new ItemNode(QTIEditHelper.createMCItem(getTranslator()), qtiPackage);
				} else if (cmd.equals(CMD_TOOLS_ADD_KPRIM)) {
					insertObject = new ItemNode(QTIEditHelper.createKPRIMItem(getTranslator()), qtiPackage);
				} else if (cmd.equals(CMD_TOOLS_ADD_FIB)) {
					insertObject = new ItemNode(QTIEditHelper.createFIBItem(getTranslator()), qtiPackage);
				} else if (cmd.equals(CMD_TOOLS_ADD_FREETEXT)) {
					insertObject = new ItemNode(QTIEditHelper.createEssayItem(getTranslator()), qtiPackage);
				}
				doSelectInsertionPoint(cmd, insertObject);
			}
		} else if (source == deleteDialog) { // event from delete dialog
			if (DialogBoxUIFactory.isYesEvent(event)) { // yes, delete
				GenericQtiNode clickedNode = (GenericQtiNode) deleteDialog.getUserObject();
			  //check if any media to delete as well
				if(clickedNode.getUnderlyingQTIObject() instanceof Item) {
				  Item selectedItem = (Item)clickedNode.getUnderlyingQTIObject();
				  deletableMediaFiles = QTIEditHelper.getDeletableMedia(qtiPackage.getQTIDocument(), selectedItem); 
				}
													
				// remove from underlying model
				((GenericQtiNode) clickedNode.getParent()).removeQTIObjectAt(clickedNode.getPosition());
												
				// remove from tree model
				clickedNode.removeFromParent();
				qtiPackage.serializeQTIDocument();
				menuTree.setSelectedNodeId(clickedNode.getParent().getIdent());
				event(ureq, menuTree, new Event(MenuTree.COMMAND_TREENODE_CLICKED));
				//ask user to confirm referenced media removal
				if(deletableMediaFiles!=null && deletableMediaFiles.size()>0) {					
					String msg = translate("delete.item.media", deletableMediaFiles.toString());
					deleteMediaDialog = activateYesNoDialog(ureq, null, msg, deleteMediaDialog);
				}		
			}
			// cleanup controller
			removeAsListenerAndDispose(deleteDialog);
			deleteDialog = null;
		
		} else if (source == deleteMediaDialog) { // event from deleteMediaDialog
			if (DialogBoxUIFactory.isYesEvent(event)) { // yes, delete					
				qtiPackage.removeMediaFiles(deletableMediaFiles);
				deleteMediaDialog = null;
				deletableMediaFiles = null;
			}
		} else if (event instanceof NodeBeforeChangeEvent) {
			NodeBeforeChangeEvent nce = (NodeBeforeChangeEvent) event;
			// active node changed some data
			String activeQtiNodeId = menuTree.getSelectedNodeId();
			GenericQtiNode activeQtiNode = menuTreeModel.getQtiNode(activeQtiNodeId);
			menuTree.setDirty(true); //force rerendering for ajax mode
			/*
			 * mementos are only created in restricted mode
			 */
			if (isRestrictedEdit()) {
				String key = nce.getSectionIdent() + "/" + nce.getItemIdent() + "/" + nce.getQuestionIdent() + "/" + nce.getResponseIdent();
				if (!history.containsKey(key)) {
					Memento memento = activeQtiNode.createMemento();
					history.put(key, memento);
					qtiPackage.serializeChangelog(history);
				}
			}

			/*
			 * generate a Memento, store it for further use
			 */
			if (nce.hasNewTitle) {
				// update the treemodel to reflect the change of the underlying qti node
				activeQtiNode.setMenuTitleAndAlt(nce.getNewTitle());
				main.contextPut("qtititle", menuTreeModel.getQtiRootNode().getAltText());
			}
		} else if (source == proceedRestricedEditDialog) {
			// restricted edit warning
			if (event == DialogController.EVENT_FIRSTBUTTON) {
				// remove dialog and continue with real content
				columnLayoutCtr.setCol3(mainPanel);
				columnLayoutCtr.hideCol1(false);
				columnLayoutCtr.hideCol2(false);
				removeAsListenerAndDispose(proceedRestricedEditDialog);
				proceedRestricedEditDialog = null;
			} else {
				// remove lock as user is not interested in restricted edit
				// and quick editor
				removeLocksAndExit(ureq);
			} 
			
		} else if (source == cfc) {
			// dispose the content only controller we live in

			// remove modal dialog and cleanup exit process
			// modal dialog must be removed before fire DONE event
			// within the saveAndExit() call, otherwise the wrong 
			// gui stack is popped see also OLAT-3056
			cmcExit.deactivate();
			removeAsListenerAndDispose(cmcExit);
			cmcExit = null;
			
			if (event == Event.CANCELLED_EVENT) {
				// nothing to do, back to editor			
			} else {
				QTIChangeLogMessage clm = new QTIChangeLogMessage(changeLog, chngMsgFrom.hasInformLearners());
				qtiPackage.commitChangelog(clm);
				StringBuilder traceMsg = new StringBuilder(chngMsgFrom.hasInformLearners() ? "Visible for ALL \n" : "Visible for GROUP only \n");
				Tracing.logAudit(traceMsg.append(changeLog).toString(), QTIEditorMainController.class);
				// save, remove locks and tmp files
				saveAndExit(ureq);
			}
			
			removeAsListenerAndDispose(cfc);
			cfc = null;
		} else if (source == chngMsgFrom) {
			if (event == Event.DONE_EVENT) {
				// the changemessage is created and user is willing to send it
				String userMsg = chngMsgFrom.getUserMsg();
				changeLog = changeEmail.getBodyText();
				if (StringHelper.containsNonWhitespace(userMsg)) {
					changeEmail.setBodyText(userMsg + "\n" + changeLog);
				}// else nothing was added!
				changeEmail.setSubject("Change log for " + startedWithTitle);
				cfc = new ContactFormController(ureq, getWindowControl(), false, true, false, false, changeEmail);
				listenTo(cfc);
				exitPanel.setContent(cfc.getInitialComponent());
				return;
				
			} else {
				// cancel button was pressed
				// just go back to the editor - remove modal dialog
				cmcExit.deactivate();
			}
		} else if (source == selectQItemCtrl) {
			cmc.deactivate();
			cleanUp();
			
			if(event instanceof QItemEvent) {
				QItemEvent e = (QItemEvent)event;
				List<QuestionItemView> items = e.getItemList();
				doSelectInsertionPoint(CMD_TOOLS_ADD_QPOOL, items);
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(selectQItemCtrl);
		cmc = null;
		selectQItemCtrl = null;
	}
	
	private void doSelectInsertionPoint(String cmd, Object userObj) {
	// prepare insert tree
		insertTree = new SelectionTree("insertTree", getTranslator());
		insertTree.setUserObject(userObj);
		insertTree.setFormButtonKey("submit");
		if (cmd.equals(CMD_TOOLS_ADD_SECTION)) {
			insertTreeModel = new InsertItemTreeModel(menuTreeModel, InsertItemTreeModel.INSTANCE_ASSESSMENT);
		} else {
			insertTreeModel = new InsertItemTreeModel(menuTreeModel, InsertItemTreeModel.INSTANCE_SECTION);
		}
		insertTree.setTreeModel(insertTreeModel);
		insertTree.addListener(this);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), insertTree, true, translate("title.add") );
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doInsert(UserRequest ureq, String nodeId, Object toInsert) {
		// new node
		TreePosition tp = insertTreeModel.getTreePosition(nodeId);
		GenericQtiNode parentTargetNode = (GenericQtiNode) tp.getParentTreeNode();
		
		if(toInsert instanceof GenericQtiNode) {
			doInsert(parentTargetNode, (GenericQtiNode)toInsert, tp.getChildpos());
		} else if(toInsert instanceof QuestionItemView) {
			QuestionItemView item = (QuestionItemView)toInsert;
			GenericQtiNode insertNode = doConvertItemToQtiNode(item);
			doInsert(parentTargetNode, insertNode, tp.getChildpos());
		} else if(toInsert instanceof Collection) {
			int position = tp.getChildpos();
			@SuppressWarnings("unchecked")
			Collection<QuestionItemView> items = (Collection<QuestionItemView>)toInsert;
			for(QuestionItemView item:items) {
				GenericQtiNode insertNode = doConvertItemToQtiNode(item);
				doInsert(parentTargetNode, insertNode, position++);
			}
		}

		event(ureq, menuTree, new Event(MenuTree.COMMAND_TREENODE_CLICKED));
		qtiPackage.serializeQTIDocument();
	}
	
	private void doInsert(GenericQtiNode parentTargetNode, GenericQtiNode insertNode, int position) {
		// insert into menu tree
		parentTargetNode.insert(insertNode, position);
		// insert into model
		parentTargetNode.insertQTIObjectAt(insertNode.getUnderlyingQTIObject(), position);
		// activate inserted node
		menuTree.setSelectedNodeId(insertNode.getIdent());
	}
	
	private GenericQtiNode doConvertItemToQtiNode(QuestionItemView qitemv) {
		VFSContainer editorContainer = qtiPackage.getBaseDir();
		Item theItem = qtiQpoolServiceProvider.exportToQTIEditor(qitemv, editorContainer);
		GenericQtiNode node = new ItemNode(theItem, qtiPackage);
		return node;
	}
	
	private void doSelectQItem(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(selectQItemCtrl);
		
		selectQItemCtrl = new SelectItemController(ureq, getWindowControl(), QTIConstants.QTI_12_FORMAT);
		listenTo(selectQItemCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), selectQItemCtrl.getInitialComponent(), true, translate("title.add") );
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doExportQItem(UserRequest ureq) {
		GenericQtiNode selectedNode = menuTreeModel.getQtiNode(menuTree.getSelectedNodeId());
		if(selectedNode instanceof ItemNode) {
			ItemNode itemNode = (ItemNode)selectedNode;
			QTIObject qtiObject = itemNode.getUnderlyingQTIObject();
			if(qtiObject instanceof Item) {
				Item item = (Item)qtiObject;
				VFSContainer editorContainer = qtiPackage.getBaseDir();
				qtiQpoolServiceProvider.importBeecomItem(getIdentity(), item, editorContainer, getLocale());
				showInfo("export.qpool.successful");
			}	
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// controlers disposed by BasicController:		
    // release activeSessionLock upon dispose
		if (activeSessionLock!=null && activeSessionLock.isSuccess()){
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(activeSessionLock);			
		}
	}

	private ToolController populateToolC() {
		ToolController tc = ToolFactory.createToolController(getWindowControl());
		// tools
		tc.addHeader(translate("tools.tools.header"));
		tc.addLink(CMD_TOOLS_PREVIEW, translate("tools.tools.preview"), CMD_TOOLS_PREVIEW, "b_toolbox_preview");
		tc.addLink(CMD_TOOLS_EXPORT_QPOOL, translate("tools.export.qpool"), CMD_TOOLS_EXPORT_QPOOL, "o_mi_qpool_export");
		tc.addLink(CMD_TOOLS_CLOSE_EDITOR, translate("tools.tools.closeeditor"), null, "b_toolbox_close");
		// if (!restrictedEdit) {
		tc.addHeader(translate("tools.add.header"));
		// adds within the qti document level
		tc.addLink(CMD_TOOLS_ADD_QPOOL, translate("tools.import.qpool"), CMD_TOOLS_ADD_QPOOL, "o_mi_qpool_import");
		tc.addLink(CMD_TOOLS_ADD_SECTION, translate("tools.add.section"), CMD_TOOLS_ADD_SECTION, "o_mi_qtisection");
		// adds within a section
		tc.addLink(CMD_TOOLS_ADD_SINGLECHOICE, translate("tools.add.singlechoice"), CMD_TOOLS_ADD_SINGLECHOICE, "o_mi_qtisc");
		tc.addLink(CMD_TOOLS_ADD_MULTIPLECHOICE, translate("tools.add.multiplechoice"), CMD_TOOLS_ADD_MULTIPLECHOICE, "o_mi_qtimc");
		if (!qtiPackage.getQTIDocument().isSurvey()) tc.addLink(CMD_TOOLS_ADD_KPRIM, translate("tools.add.kprim"), CMD_TOOLS_ADD_KPRIM,
				"o_mi_qtikprim");
		tc.addLink(CMD_TOOLS_ADD_FIB, translate("tools.add.cloze"), CMD_TOOLS_ADD_FIB, "o_mi_qtifib");
		if (qtiPackage.getQTIDocument().isSurvey()) tc.addLink(CMD_TOOLS_ADD_FREETEXT, translate("tools.add.freetext"),
				CMD_TOOLS_ADD_FREETEXT, "o_mi_qtiessay");
		// change
		tc.addHeader(translate("tools.change.header"));
		// change actions
		tc.addLink(CMD_TOOLS_CHANGE_DELETE, translate("tools.change.delete"), CMD_TOOLS_CHANGE_DELETE, "b_toolbox_delete");
		tc.addLink(CMD_TOOLS_CHANGE_MOVE, translate("tools.change.move"), CMD_TOOLS_CHANGE_MOVE, "b_toolbox_move");
		tc.addLink(CMD_TOOLS_CHANGE_COPY, translate("tools.change.copy"), CMD_TOOLS_CHANGE_COPY, "b_toolbox_copy");
		// }

		return tc;
	}

	/**
	 * @see org.olat.core.gui.control.VetoableCloseController#requestForClose()
	 */
	public boolean requestForClose() {		
		// enter save/discard dialog if not already in it
		if (cmcExit == null) {
			exitVC = createVelocityContainer("exitDialog");
			exitPanel = new Panel("exitPanel");
			exitPanel.setContent(exitVC);
			cmcExit = new CloseableModalController(getWindowControl(), translate("editor.preview.close"), exitPanel);
			cmcExit.activate();
			listenTo(cmcExit);
		}
		return false;
	}

	/**
	 * helper method to create the message about qti resource stakeholders and
	 * from where the qti resource is referenced.
	 * 
	 * @return
	 */
	private String createReferenceesMsg(UserRequest ureq) {
		/*
		 * problems: A tries to reference this test, after test editor has been
		 * started
		 */
		changeEmail = new ContactMessage(ureq.getIdentity());

		RepositoryManager rm = RepositoryManager.getInstance();
		// the owners of this qtiPkg
		RepositoryEntry myEntry = rm.lookupRepositoryEntry(qtiPackage.getRepresentingResourceable(), false);
		SecurityGroup qtiPkgOwners = myEntry.getOwnerGroup();

		// add qti resource owners as group
		ContactList cl = new ContactList("qtiPkgOwners");
		cl.addAllIdentites(BaseSecurityManager.getInstance().getIdentitiesOfSecurityGroup(qtiPkgOwners));
		changeEmail.addEmailTo(cl);

		StringBuilder result = new StringBuilder();
		result.append(translate("qti.restricted.leading"));
		for (Iterator iter = referencees.iterator(); iter.hasNext();) {
			ReferenceImpl element = (ReferenceImpl) iter.next();
			// FIXME:discuss:possible performance/cache problem
			if ("CourseModule".equals(element.getSource().getResourceableTypeName())) {
				ICourse course = CourseFactory.loadCourse(element.getSource().getResourceableId());

				// the course owners

				RepositoryEntry entry = rm.lookupRepositoryEntry(course, false);
				String courseTitle = course.getCourseTitle();
				SecurityGroup owners = entry.getOwnerGroup();
				List<Identity> stakeHoldersIds = BaseSecurityManager.getInstance().getIdentitiesOfSecurityGroup(owners);

				// add stakeholders as group
				cl = new ContactList(courseTitle);
				cl.addAllIdentites(stakeHoldersIds);
				changeEmail.addEmailTo(cl);

				StringBuilder stakeHolders = new StringBuilder();
				User user = ((Identity) stakeHoldersIds.get(0)).getUser();
				Locale loc = ureq.getLocale();
				stakeHolders.append(user.getProperty(UserConstants.FIRSTNAME, loc)).append(" ").append(user.getProperty(UserConstants.LASTNAME, loc));
				for (int i = 1; i < stakeHoldersIds.size(); i++) {
					user = ((Identity) stakeHoldersIds.get(i)).getUser();
					stakeHolders.append(", ").append(user.getProperty(UserConstants.FIRSTNAME, loc)).append(" ").append(user.getProperty(UserConstants.LASTNAME, loc));
				}

				CourseNode cn = course.getEditorTreeModel().getCourseNode(element.getUserdata());
				String courseNodeTitle = cn.getShortTitle();
				result.append(translate("qti.restricted.course", courseTitle));
				result.append(translate("qti.restricted.node", courseNodeTitle));
				result.append(translate("qti.restricted.owners", stakeHolders.toString()));
			}
		}
		return result.toString();
	}

	/**
	 * helper method to create the change log message
	 * 
	 * @return
	 */
	private String createChangeMessage() {

		// FIXME:pb:break down into smaller pieces
		final StringBuilder result = new StringBuilder();
		if (isRestrictedEdit()) {
			Visitor v = new Visitor() {
				/*
				 * a history key is built as follows
				 * sectionkey+"/"+itemkey+"/"+questionkey+"/"+responsekey
				 */
				String sectionKey = null;
				String itemkey = null;
				int pos = 0;
				Map itemMap = new HashMap();

				public void visit(INode node) {
					if (node instanceof AssessmentNode) {
						AssessmentNode an = (AssessmentNode) node;
						String key = "null/null/null/null";
						if (history.containsKey(key)) {
							// some assessment top level data changed
							Memento mem = (Memento) history.get(key);
							result.append("---+ Changes in test " + formatVariable(startedWithTitle) + ":");
							result.append(an.createChangeMessage(mem));
						}
					} else if (node instanceof SectionNode) {
						SectionNode sn = (SectionNode) node;
						String tmpKey = ((Section) sn.getUnderlyingQTIObject()).getIdent();
						String key = tmpKey + "/null/null/null";
						if (history.containsKey(key)) {
							// some section only data changed
							Memento mem = (Memento) history.get(key);
							result.append("\n---++ Section " + formatVariable(sn.getAltText()) + " changes:");
							result.append(sn.createChangeMessage(mem));
						}
					} else if (node instanceof ItemNode) {
						ItemNode in = (ItemNode) node;
						SectionNode sn = (SectionNode) in.getParent();
						String parentSectkey = ((Section) ((SectionNode) in.getParent()).getUnderlyingQTIObject()).getIdent();
						Item item = (Item) in.getUnderlyingQTIObject();
						Question question = item.getQuestion();
						String itemKey = item.getIdent();
						String prefixKey = "null/" + itemKey;
						String questionIdent = question != null ? question.getQuestion().getId() : "null";
						String key = prefixKey + "/" + questionIdent + "/null";
						StringBuilder changeMessage = new StringBuilder();
						boolean hasChanges = false;

						if (!itemMap.containsKey(itemKey)) {
							Memento questMem = null;
							Memento respMem = null;
							if (history.containsKey(key)) {
								// question changed!
								questMem = (Memento) history.get(key);
								hasChanges = true;
							}
							// if(!hasChanges){
							// check if a response changed
							// new prefix for responses
							prefixKey += "/null/";
							// list contains org.olat.ims.qti.editor.beecom.objects.Response
							List responses = question != null ? question.getResponses() : null;
							if (responses != null && responses.size() > 0) {
								// check for changes in each response
								for (Iterator iter = responses.iterator(); iter.hasNext();) {
									Response resp = (Response) iter.next();
									if (history.containsKey(prefixKey + resp.getIdent())) {
										// this response changed!
										Memento tmpMem = (Memento) history.get(prefixKey + resp.getIdent());
										if (respMem != null) {
											respMem = respMem.getTimestamp() > tmpMem.getTimestamp() ? tmpMem : respMem;
										} else {
											hasChanges = true;
											respMem = tmpMem;
										}
									}
								}
							}
							// }
							// output message
							if (hasChanges) {
								Memento mem = null;
								if (questMem != null && respMem != null) {
									// use the earlier memento
									mem = questMem.getTimestamp() > respMem.getTimestamp() ? respMem : questMem;
								} else if (questMem != null) {
									mem = questMem;
								} else if (respMem != null) {
									mem = respMem;
								}
								changeMessage.append(in.createChangeMessage(mem));
								itemMap.put(itemKey, itemKey);
								if (!parentSectkey.equals(sectionKey)) {
									// either this item belongs to a new section or no section
									// is active
									result.append("\n---++ Section " + formatVariable(sn.getAltText()) + " changes:");
									result.append("\n").append(changeMessage);
									sectionKey = parentSectkey;
								} else {
									result.append("\n").append(changeMessage);
								}
							}

						}
					}
				}

				private String formatVariable(String var) {
					if (StringHelper.containsNonWhitespace(var)) { return var; }
					return "[no entry]";
				}
			};
			TreeVisitor tv = new TreeVisitor(v, menuTreeModel.getRootNode(), false);
			tv.visitAll();
		}
		/*
		 * 
		 */
		return result.toString();
	}

	/**
	 * whether the editor runs in restricted mode or not.
	 * 
	 * @return
	 */
	public boolean isRestrictedEdit() {
		return restrictedEdit;
	}

	public boolean isLockedSuccessfully() {
		return lockEntry.isSuccess();
	}

}