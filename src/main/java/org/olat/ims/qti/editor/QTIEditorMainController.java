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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.olat.NewControllerFactory;
import org.olat.admin.quota.QuotaConstants;
import org.olat.admin.quota.QuotaImpl;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.tree.TreePosition;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.VetoableCloseController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.memento.Memento;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeHelper;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.FullAccessWithQuotaCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti.QTIChangeLogMessage;
import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.QTIModule;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.editor.beecom.objects.Assessment;
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
import org.olat.ims.qti.export.QTIWordExport;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.QTIEditorResolver;
import org.olat.ims.qti.qpool.QTIQPoolServiceProvider;
import org.olat.ims.qti.questionimport.ImportOptions;
import org.olat.ims.qti.questionimport.ItemAndMetadata;
import org.olat.ims.qti.questionimport.ItemsPackage;
import org.olat.ims.qti.questionimport.QImport_1_InputStep;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.iq.IQDisplayController;
import org.olat.modules.iq.IQManager;
import org.olat.modules.iq.IQPreviewSecurityCallback;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.ui.SelectItemController;
import org.olat.modules.qpool.ui.events.QItemViewEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.author.CreateRepositoryEntryController;
import org.olat.resource.references.Reference;
import org.olat.resource.references.ReferenceManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

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
public class QTIEditorMainController extends MainLayoutBasicController implements Activateable2, VetoableCloseController {
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
	private static final String CMD_TOOLS_EXPORT_DOCX = "cmd.export.docx";
	private static final String CMD_TOOLS_IMPORT_TABLE = "cmd.import.xls";
	private static final String CMD_TOOLS_CONVERT_TO_QTI21 = "cmd.convert.qti.21";

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

	private VelocityContainer main, exitVC, chngMsgFormVC;
	private MenuTree menuTree;
	private Panel mainPanel;
	private TooledStackedPanel stackedPanel;
	private LayoutMain3ColsController columnLayoutCtr;
	
	private Link previewLink, exportPoolLink, convertQTI21Link, exportDocLink, importTableLink, closeLink;
	private Link addPoolLink, addSectionLink, addSCLink, addMCLink, addFIBLink, addKPrimLink, addEssayLink;
	private Link deleteLink, moveLink, copyLink;
	private Link convertQTI21Button;

	private QTIEditorTreeModel menuTreeModel;
	private DialogBoxController deleteDialog;
	private DialogBoxController deleteMediaDialog;
	private IQDisplayController previewController;
	private LockResult lockEntry;
	private boolean restrictedEdit;
	private boolean blockedEdit;
	private Map<String, Memento> history = null;
	private String startedWithTitle;
	private List<Reference> referencees;
	private ChangeMessageForm chngMsgFrom;
	private DialogBoxController proceedRestricedEditDialog;
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
	private StepsMainRunController importTableWizard;
	private CreateRepositoryEntryController createConvertedTestController;
	private InsertNodeController moveCtrl, copyCtrl, insertCtrl;
	private CountDownLatch exportLatch;
	private RepositoryEntry qtiEntry;

	@Autowired
	private IQManager iqManager;
	@Autowired
	private QTIModule qtiModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private QuotaManager quotaManager;
	@Autowired
	private QTIResultManager qtiResultManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private ReferenceManager referenceManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private QTIQPoolServiceProvider qtiQpoolServiceProvider;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	
	public QTIEditorMainController(UserRequest ureq, WindowControl wControl, RepositoryEntry qtiEntry, List<Reference> referencees, FileResource fileResource) {
		super(ureq, wControl);
		
		this.qtiEntry = qtiEntry;
		blockedEdit = !qtiModule.isEditResourcesEnabled();

		for(Iterator<Reference> iter = referencees.iterator(); iter.hasNext(); ) {
			Reference ref = iter.next();
			if ("CourseModule".equals(ref.getSource().getResourceableTypeName())) {
				try {
					ICourse course = CourseFactory.loadCourse(ref.getSource().getResourceableId());
					CourseNode courseNode = course.getEditorTreeModel().getCourseNode(ref.getUserdata());
					if(courseNode == null) {
						courseNode = course.getRunStructure().getNode(ref.getUserdata());
					}
						
					if(courseNode == null) {
						referenceManager.delete(ref);	
					} else {
						String repositorySoftKey = (String) courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
						//check softly that the setting if ok
						if(qtiEntry.getSoftkey().equals(repositorySoftKey)) {
							restrictedEdit = ((CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(course, null))
								|| qtiResultManager.countResults(course.getResourceableId(), courseNode.getIdent(), qtiEntry.getKey()) > 0) ? true : false;
						} else {
							logError("The course node soft key doesn't match the test/survey sotf key. Course resourceable id: "
						      + course.getResourceableId() + " (" + course.getCourseTitle() + ") course node: " + courseNode.getIdent() + " (" + courseNode.getShortTitle() + " )"
						      + " soft key of test/survey in course: " + repositorySoftKey + "  test/survey soft key: " + qtiEntry.getSoftkey(), null);
						}
					}
				} catch(CorruptedCourseException e) {
					logError("", e);
					referenceManager.delete(ref);
				}
			}
			if(restrictedEdit) {
				break;
			}
		}
		this.referencees = referencees;
		

		Quota defQuota = quotaManager.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_REPO);
		//unlimited for author
		Quota quota = new QuotaImpl(defQuota.getPath(), defQuota.getQuotaKB() * 100, defQuota.getUlLimitKB() * 100);
		VFSSecurityCallback secCallback = new FullAccessWithQuotaCallback(quota, null);
		qtiPackage = new QTIEditorPackageImpl(ureq.getIdentity(), fileResource, secCallback, getTranslator());

		// try to get lock which lives longer then the browser session in case of a closing browser window
		lockEntry = iqManager.aquirePersistentLock(qtiPackage.getRepresentingResourceable(), ureq.getIdentity(), null);
		if (lockEntry.isSuccess()) {
			// acquired a lock for the duration of the session only
			//fileResource has the RepositoryEntre.getOlatResource within, which is used in qtiPackage
			activeSessionLock = CoordinatorManager.getInstance().getCoordinator().getLocker()
					.acquireLock(qtiPackage.getRepresentingResourceable(), ureq.getIdentity(), null, getWindow());
			//
			if (qtiPackage.getQTIDocument() == null) {
				notEditable = true;				
			} else if (qtiPackage.isResumed()) {
				showInfo("info.resumed");
			}
			//
			init(ureq); // initialize the gui
			updateWarning();
		} else {
			String fullName = userManager.getUserDisplayName(lockEntry.getOwner());
			wControl.setWarning( getTranslator().translate("error.lock", new String[] { fullName,
				Formatter.formatDatetime(new Date(lockEntry.getLockAquiredTime())) }) );
		}
	}

	private void init(UserRequest ureq) {
		main = createVelocityContainer("index");
		JSAndCSSComponent jsAndCss;
		// Add html header js
		jsAndCss = new JSAndCSSComponent("qitjsandcss", new String[] { "js/openolat/qti.js" }, null);
		main.put("qitjsandcss", jsAndCss);
		
		mainPanel = new Panel("p_qti_editor");
		mainPanel.setContent(main);
		
		if(notEditable) {		
			//test not editable
			VelocityContainer notEditableVc = createVelocityContainer("notEditable");
			notEditableButton = LinkFactory.createButton("ok", notEditableVc, this);
			Panel panel = new Panel("notEditable");
			panel.setContent(notEditableVc);
			columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, panel, null);
			putInitialPanel(columnLayoutCtr.getInitialComponent());
			return;
		}

		stackedPanel = new TooledStackedPanel("qtiEditorStackedPanel", getTranslator(), this);
		stackedPanel.setCssClass("o_edit_mode");
		
		// initialize the history
		if (qtiPackage.isResumed() && qtiPackage.hasSerializedChangelog()) {
			// there were already changes made -> reload!
			history = qtiPackage.loadChangelog();
		} else {
			// start with a fresh history. Editor is resumed but no changes were made
			// so far.
			history = new HashMap<>();
		}
		
		convertQTI21Button = LinkFactory.createButton("tools.convert.qti21", main, this);
		convertQTI21Button.setIconLeftCSS("o_icon o_FileResource-IMSQTI21_icon");

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
		startedWithTitle = menuTree.getSelectedNode().getAltText();
		//
		main.put("tabbedPane", menuTreeModel.getQtiRootNode().createEditTabbedPane(ureq, getWindowControl(), getTranslator(), this));
		main.contextPut("qtititle", menuTreeModel.getQtiRootNode().getAltText());
		main.contextPut("isRestrictedEdit", restrictedEdit ? Boolean.TRUE : Boolean.FALSE);
		//
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), menuTree, mainPanel, "qtieditor" + qtiPackage.getRepresentingResourceable());
		listenTo(columnLayoutCtr);
		stackedPanel.pushController("Editor", columnLayoutCtr);
		populateToolC(); // qtiPackage must be loaded previousely
		
		// Add css background
		if (restrictedEdit || blockedEdit) {
			addSectionLink.setEnabled(false);
			addSCLink.setEnabled(false);
			addMCLink.setEnabled(false);
			addPoolLink.setEnabled(false);
			addFIBLink.setEnabled(false);
			if (!qtiPackage.getQTIDocument().isSurvey()) {
				addKPrimLink.setEnabled(false);
			}
			addEssayLink.setEnabled(false);
			
			columnLayoutCtr.addCssClassToMain("o_editor_qti_correct");
		} else {
			columnLayoutCtr.addCssClassToMain("o_editor_qti");
		}
		if(blockedEdit) {
			importTableLink.setEnabled(false);
			exportPoolLink.setEnabled(false);
		}

		deleteLink.setEnabled(false);
		moveLink.setEnabled(false);
		copyLink.setEnabled(false);
		
		putInitialPanel(stackedPanel);
		
		if (restrictedEdit) {
			// we would like to us a modal dialog here, but this does not work! we
			// can't push to stack because the outher workflows pushes us after the
			// controller to the stack. Thus, if we used a modal dialog here the
			// dialog would never show up. 
			columnLayoutCtr.setCol3(new Panel("empty"));
			columnLayoutCtr.hideCol1(true);
			columnLayoutCtr.hideCol2(true);
			
			String text = translate("qti.restricted.edit.warning") + "<br/><br/>" + createReferenceesMsg(ureq);
			proceedRestricedEditDialog = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(), null, text);
			listenTo(proceedRestricedEditDialog);
			proceedRestricedEditDialog.activate();
		}
	}
	
	private void updateWarning() {
		boolean warningEssay = false;
		if(qtiPackage.getQTIDocument() != null && !qtiPackage.getQTIDocument().isSurvey()) {
			//check if the test contains some essay
			List<TreeNode> flattedTree = new ArrayList<>();
			TreeHelper.makeTreeFlat(menuTreeModel.getRootNode(), flattedTree);
			for(TreeNode node:flattedTree) {
				Object uo = node.getUserObject();
				if(uo instanceof String && ((String)uo).startsWith("QTIEDIT:ESSAY")) {
					warningEssay = true;
					break;
				}
			}
		}
		main.contextPut("warningEssay", Boolean.valueOf(warningEssay));
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(proceedRestricedEditDialog != null) {
			proceedRestricedEditDialog.activate();
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
				Component tabbedPane = clickedNode.createEditTabbedPane(ureq, getWindowControl(), getTranslator(), this);
				if(tabbedPane!=null) {
					main.put("tabbedPane",tabbedPane);
				} else {
					VelocityContainer itemNotEditable = createVelocityContainer("tab_itemAlien");						
					main.put("tabbedPane", itemNotEditable);
					return;					
				}
				
				// enable/disable delete and move
				// only available in full edit mode
				if (clickedNode instanceof AssessmentNode) {
					deleteLink.setEnabled(false);
					moveLink.setEnabled(false);
					copyLink.setEnabled(false);
					stackedPanel.setDirty(true);
				} else {
					deleteLink.setEnabled(!restrictedEdit && !blockedEdit);
					moveLink.setEnabled(!restrictedEdit && !blockedEdit);
					if (clickedNode instanceof ItemNode) {
						copyLink.setEnabled(!restrictedEdit && !blockedEdit);
					} else {
						copyLink.setEnabled(false);
					}
					stackedPanel.setDirty(true);
				}
			}
		} else if (source == exitVC) {
			if (CMD_EXIT_SAVE.equals(event.getCommand())) {
				if (isRestrictedEdit() && history.size() > 0) {
					// changes were recorded
					// start work flow:
					// -sending an e-mail to everybody being a stake holder of this qti
					// resource
					// -email with change message
					// -after sending email successfully -> saveNexit is called.
					chngMsgFormVC = createVelocityContainer("changeMsgForm");
					String userN = UserManager.getInstance().getUserDisplayEmail(ureq.getIdentity(), ureq.getLocale());
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
			} else if (CMD_EXIT_DISCARD.equals(event.getCommand())) {
				// remove modal dialog and proceed with exit process
				cmcExit.deactivate();
				removeAsListenerAndDispose(cmcExit);
				cmcExit = null;
				// cleanup, so package does not get resumed
				qtiPackage.cleanupTmpPackageDir();
				// remove lock
				removeLocksAndExit(ureq);
				
			} else if (CMD_EXIT_CANCEL.equals(event.getCommand())) {
				// remove modal dialog and go back to edit mode
				cmcExit.deactivate();
				removeAsListenerAndDispose(cmcExit);
				cmcExit = null;
			}
			
		} else if (source == notEditableButton) {
			fireEvent(ureq, Event.DONE_EVENT); // close editor
		} else if (closeLink == source) { // exitVC hook:
			// save package back to repository
			exitVC = createVelocityContainer("exitDialog");
			exitPanel = new Panel("exitPanel");
			exitPanel.setContent(exitVC);
			cmcExit = new CloseableModalController(getWindowControl(), translate("editor.preview.close"), exitPanel, true, translate("exit.header"));
			cmcExit.activate();
			listenTo(cmcExit);
			return;
			
		} else if (previewLink == source) { // preview
			previewController = CoreSpringFactory.getImpl(IQManager.class).createIQDisplayController(new QTIEditorResolver(qtiPackage),
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
		} else if (deleteLink == source) { // prepare delete
			if(deleteDialog != null) return;//multi return in Firefox

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
		} else if (moveLink == source) {			
		  //cannot move the last item
			GenericQtiNode clickedNode = menuTreeModel.getQtiNode(menuTree.getSelectedNodeId());
			if (clickedNode instanceof ItemNode && ((SectionNode) clickedNode.getParent()).getChildCount() == 1) {				
				getWindowControl().setError(translate("error.move.atleastoneitem"));
			} else {
				TreeNode selectedNode = menuTree.getSelectedNode();
				int type = (selectedNode instanceof SectionNode) ? InsertItemTreeModel.INSTANCE_ASSESSMENT : InsertItemTreeModel.INSTANCE_SECTION;
				InsertItemTreeModel treeModel = new InsertItemTreeModel(menuTreeModel, selectedNode, type);
				moveCtrl = new InsertNodeController(ureq, getWindowControl(), treeModel);
				listenTo(moveCtrl);
				cmc = new CloseableModalController(getWindowControl(), "close", moveCtrl.getInitialComponent(), true, translate("title.move"));
				cmc.activate();
				listenTo(cmc);
			}
		} else if (copyLink == source) {
			InsertItemTreeModel treeModel = new InsertItemTreeModel(menuTreeModel, menuTree.getSelectedNode(), InsertItemTreeModel.INSTANCE_SECTION);
			copyCtrl = new InsertNodeController(ureq, getWindowControl(), treeModel);
			listenTo(copyCtrl);
			cmc = new CloseableModalController(getWindowControl(), "close", copyCtrl.getInitialComponent(), true, translate("title.copy"));
			cmc.activate();
			listenTo(cmc);
		} else if (addPoolLink == source) {
			doSelectQItem(ureq);
		} else if (exportPoolLink == source) {
			doExportQItem();
		} else if (exportDocLink == source) {
			doExportDocx(ureq);
		} else if (convertQTI21Link == source || convertQTI21Button == source) {
			doConvertToQTI21(ureq);
		} else if (importTableLink == source) {
			doImportTable(ureq);
		} else if (addSectionLink == source) {
			Section newSection = QTIEditHelper.createSection(getTranslator());
			Item newItem = QTIEditHelper.createSCItem(getTranslator());
			newSection.getItems().add(newItem);
			SectionNode scNode = new SectionNode(newSection, qtiPackage);
			ItemNode itemNode = new ItemNode(newItem, qtiPackage);
			scNode.addChild(itemNode);
			doSelectInsertionPoint(ureq, CMD_TOOLS_ADD_SECTION, scNode);
		} else if (addSCLink == source) {
			ItemNode insertObject = new ItemNode(QTIEditHelper.createSCItem(getTranslator()), qtiPackage);
			doSelectInsertionPoint(ureq, CMD_TOOLS_ADD_SINGLECHOICE, insertObject);
		} else if (addMCLink == source) {
			ItemNode insertObject = new ItemNode(QTIEditHelper.createMCItem(getTranslator()), qtiPackage);
			doSelectInsertionPoint(ureq, CMD_TOOLS_ADD_MULTIPLECHOICE, insertObject);
		} else if (addKPrimLink == source) {
			ItemNode insertObject = new ItemNode(QTIEditHelper.createKPRIMItem(getTranslator()), qtiPackage);
			doSelectInsertionPoint(ureq, CMD_TOOLS_ADD_KPRIM, insertObject);
		} else if (addFIBLink == source) {
			ItemNode insertObject = new ItemNode(QTIEditHelper.createFIBItem(getTranslator()), qtiPackage);
			doSelectInsertionPoint(ureq, CMD_TOOLS_ADD_FIB, insertObject);
		} else if (addEssayLink == source) {
			ItemNode insertObject = new ItemNode(QTIEditHelper.createEssayItem(getTranslator()), qtiPackage);
			doSelectInsertionPoint(ureq, CMD_TOOLS_ADD_FREETEXT, insertObject);
		}
	}

	private void removeLocksAndExit(UserRequest ureq) {
		// remove lock
		if (lockEntry.isSuccess()){
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(activeSessionLock);
			iqManager.releasePersistentLock(lockEntry);
		}
		fireEvent(ureq, Event.DONE_EVENT); // close editor
	}

	private void saveAndExit(UserRequest ureq) {
		boolean saveOk = false;
		saveOk = qtiPackage.savePackageToRepository();
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
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == deleteDialog) { // event from delete dialog
			if (DialogBoxUIFactory.isYesEvent(event)) { // yes, delete
				GenericQtiNode clickedNode = (GenericQtiNode) deleteDialog.getUserObject();
				doDelete(clickedNode);
				event(ureq, menuTree, new Event(MenuTree.COMMAND_TREENODE_CLICKED));
				//ask user to confirm referenced media removal
				if(deletableMediaFiles!=null && deletableMediaFiles.size()>0) {					
					String msg = translate("delete.item.media", deletableMediaFiles.toString());
					deleteMediaDialog = activateYesNoDialog(ureq, null, msg, deleteMediaDialog);
				}
				updateWarning();
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
			if(DialogBoxUIFactory.isYesEvent(event)) {
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
				logAudit(traceMsg.append(changeLog).toString());
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
					changeEmail.setBodyText("<p>" + userMsg + "</p>\n<pre>" + changeLog + "</pre>");
				}// else nothing was added!
				changeEmail.setSubject("Change log for " + startedWithTitle);
				cfc = new ContactFormController(ureq, getWindowControl(), true, false, false, changeEmail);
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
			
			if(event instanceof QItemViewEvent) {
				QItemViewEvent e = (QItemViewEvent)event;
				List<QuestionItemView> items = e.getItemList();
				doSelectInsertionPoint(ureq, CMD_TOOLS_ADD_QPOOL, items);
			}
		} else if(source == importTableWizard) {
			ItemsPackage importPackage = (ItemsPackage)importTableWizard.getRunContext().get("importPackage");
			getWindowControl().pop();
			cleanUp();
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doSelectInsertionPoint(ureq, CMD_TOOLS_ADD_QPOOL, importPackage);
			}
		} else if(createConvertedTestController == source) {
			cmc.deactivate();
			if(event == Event.DONE_EVENT) {
				showInfo("test.converted");
				RepositoryEntry convertedEntry = createConvertedTestController.getAddedEntry();
				String businessPath = "[RepositoryEntry:" + convertedEntry.getKey() + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			}
			cleanUp();
		} else if (source == insertCtrl) { // catch insert operations
			cmc.deactivate();
			if(event == Event.DONE_EVENT) {
				TreePosition tp = insertCtrl.getInsertPosition();
				if(tp != null) {
					doInsert(ureq, tp, insertCtrl.getUserObject());
					updateWarning();
				}
			}
			cleanUp();
		} else if (source == moveCtrl) { 
			cmc.deactivate();
			if (Event.DONE_EVENT == event) {
				TreePosition tp = moveCtrl.getInsertPosition();
				if(tp != null) {
					doMove(tp);
					menuTree.setDirty(true); //force rerendering for ajax mode
					updateWarning();
				}
			}
			cleanUp();
		} else if (source == copyCtrl) {
			cmc.deactivate();
			if (Event.DONE_EVENT == event) {
				TreePosition tp = copyCtrl.getInsertPosition();
				if(tp != null) {
					doCopy(ureq, tp);
					updateWarning();
				}
			}
			cleanUp();
		} 
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(insertCtrl);
		removeAsListenerAndDispose(selectQItemCtrl);
		removeAsListenerAndDispose(importTableWizard);
		removeAsListenerAndDispose(createConvertedTestController);
		cmc = null;
		insertCtrl = null;
		selectQItemCtrl = null;
		importTableWizard = null;
		createConvertedTestController = null;
	}
	
	private void doSelectInsertionPoint(UserRequest ureq, String cmd, Object userObj) {
		InsertItemTreeModel insertTreeModel;
		if (cmd.equals(CMD_TOOLS_ADD_SECTION)) {
			insertTreeModel = new InsertItemTreeModel(menuTreeModel, userObj, InsertItemTreeModel.INSTANCE_ASSESSMENT);
		} else {
			insertTreeModel = new InsertItemTreeModel(menuTreeModel, userObj, InsertItemTreeModel.INSTANCE_SECTION);
		}
		
		insertCtrl = new InsertNodeController(ureq, getWindowControl(), insertTreeModel);
		insertCtrl.setUserObject(userObj);
		listenTo(insertCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", insertCtrl.getInitialComponent(),
				true, translate("title.add") );
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doInsert(UserRequest ureq, TreePosition tp, Object toInsert) {
		// new node
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
		} else if(toInsert instanceof ItemsPackage) {
			ItemsPackage itemsToImport = (ItemsPackage)toInsert;
			List<ItemAndMetadata> items = itemsToImport.getItems();
			int pos = tp.getChildpos();
			for(ItemAndMetadata item:items) {
				GenericQtiNode insertNode = new ItemNode(item.getItem(), qtiPackage);
				doInsert(parentTargetNode, insertNode, pos++);
			}
		}

		event(ureq, menuTree, new Event(MenuTree.COMMAND_TREENODE_CLICKED));
		qtiPackage.serializeQTIDocument();
		updateWarning();
	}
	
	private void doInsert(GenericQtiNode parentTargetNode, GenericQtiNode insertNode, int position) {
		// insert into menu tree
		parentTargetNode.insert(insertNode, position);
		// insert into model
		parentTargetNode.insertQTIObjectAt(insertNode.getUnderlyingQTIObject(), position);
		// activate inserted node
		menuTree.setSelectedNodeId(insertNode.getIdent());
		
		parentTargetNode.childNodeChanges();
	}
	
	private void doDelete(GenericQtiNode clickedNode) {
		//check if any media to delete as well
		if(clickedNode.getUnderlyingQTIObject() instanceof Item) {
		  Item selectedItem = (Item)clickedNode.getUnderlyingQTIObject();
		  deletableMediaFiles = QTIEditHelper.getDeletableMedia(qtiPackage.getQTIDocument(), selectedItem); 
		}
											
		// remove from underlying model
		GenericQtiNode parentNode = (GenericQtiNode)clickedNode.getParent();
		parentNode.removeQTIObjectAt(clickedNode.getPosition());
										
		// remove from tree model
		clickedNode.removeFromParent();
		qtiPackage.serializeQTIDocument();
		menuTree.setSelectedNodeId(clickedNode.getParent().getIdent());
		
		parentNode.childNodeChanges();
	}
	
	private void doMove(TreePosition tp) {
		// user chose a position to insert a new node
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
		
		parentSelectedNode.childNodeChanges();
		parentTargetNode.childNodeChanges();
	}
	
	private void doCopy(UserRequest ureq, TreePosition tp) {
		// user chose a position to insert the node to be copied
		int targetPos = tp.getChildpos();
		ItemNode selectedNode = (ItemNode) menuTree.getSelectedNode();
		// only items are moveable
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
		
		parentTargetNode.childNodeChanges();
	}
	
	private GenericQtiNode doConvertItemToQtiNode(QuestionItemView qitemv) {
		VFSContainer editorContainer = qtiPackage.getBaseDir();
		Item theItem = qtiQpoolServiceProvider.exportToQTIEditor(qitemv, editorContainer);
		return new ItemNode(theItem, qtiPackage);
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
	
	private void doConvertToQTI21(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(createConvertedTestController);

		RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(ImsQTI21Resource.TYPE_NAME);
		createConvertedTestController = new CreateRepositoryEntryController(ureq, getWindowControl(), handler, false);
		createConvertedTestController.setCreateObject(qtiEntry.getOlatResource());
		createConvertedTestController.setDisplayname(qtiEntry.getDisplayname());
		listenTo(createConvertedTestController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), createConvertedTestController.getInitialComponent(), true, translate("title.convert.qti21") );
		cmc.activate();
		listenTo(cmc);
	}

	private void doExportDocx(UserRequest ureq) {
		AssessmentNode rootNode = (AssessmentNode)menuTreeModel.getRootNode();
		VFSContainer editorContainer = qtiPackage.getBaseDir();
		exportLatch = new CountDownLatch(1);
		MediaResource mr = new QTIWordExport(rootNode, editorContainer, getLocale(), "UTF-8", exportLatch);
		ureq.getDispatchResult().setResultingMediaResource(mr);
	}
	
	private void doExportQItem() {
		GenericQtiNode selectedNode = menuTreeModel.getQtiNode(menuTree.getSelectedNodeId());
		if(selectedNode instanceof ItemNode) {
			ItemNode itemNode = (ItemNode)selectedNode;
			QTIObject qtiObject = itemNode.getUnderlyingQTIObject();
			if(qtiObject instanceof Item) {
				ItemAndMetadata item = new ItemAndMetadata((Item)qtiObject);
				VFSContainer editorContainer = qtiPackage.getBaseDir();
				qtiQpoolServiceProvider.importBeecomItem(getIdentity(), item, editorContainer, getLocale());
				showInfo("export.qpool.successful", "1");
			}	
		} else if(selectedNode instanceof SectionNode) {
			SectionNode sectionNode = (SectionNode)selectedNode;
			QTIObject qtiObject = sectionNode.getUnderlyingQTIObject();
			if(qtiObject instanceof Section) {
				int count = doExportSection((Section)qtiObject);
				showInfo("export.qpool.successful", Integer.toString(count));
			}
		} else if(selectedNode instanceof AssessmentNode) {
			AssessmentNode assessmentNode = (AssessmentNode)selectedNode;
			QTIObject qtiObject = assessmentNode.getUnderlyingQTIObject();
			if(qtiObject instanceof Assessment) {
				int count = doExportAssessment((Assessment)qtiObject);
				
				showInfo("export.qpool.successful", Integer.toString(count));
			}
		}
	}
	
	private int doExportAssessment(Assessment assessment) {
		int count = 0;
		if(assessment.getSections() != null) {
			for(Section section:assessment.getSections()) {
				count += doExportSection(section);
			}
		}
		return count;
	}
	
	private int doExportSection(Section section) {
		if(section.getItems() != null) {
			VFSContainer editorContainer = qtiPackage.getBaseDir();
			for(Item item:section.getItems()) {
				ItemAndMetadata itemAndMetadata = new ItemAndMetadata(item);
				qtiQpoolServiceProvider.importBeecomItem(getIdentity(), itemAndMetadata, editorContainer, getLocale());
			}
			return section.getItems().size();
		}
		return 0;
	}
	
	private void doImportTable(UserRequest ureq) {
		removeAsListenerAndDispose(importTableWizard);

		final ItemsPackage importPackage = new ItemsPackage();
		final ImportOptions options = new ImportOptions();
		options.setShuffle(!qtiPackage.getQTIDocument().isSurvey());
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
	
	private void populateToolC() {
		//tools
		Dropdown exportTools = new Dropdown("exportTools", "tools.export.header", false, getTranslator());
		exportTools.setIconCSS("o_icon o_icon_export");
		stackedPanel.addTool(exportTools, Align.left);
		
		exportTools.addComponent(previewLink);
		exportPoolLink = LinkFactory.createToolLink(CMD_TOOLS_EXPORT_QPOOL, translate("tools.export.qpool"), this, "o_mi_qpool_export");
		exportPoolLink.setIconLeftCSS("o_icon o_icon_download");
		exportTools.addComponent(exportPoolLink);
		exportDocLink = LinkFactory.createToolLink(CMD_TOOLS_EXPORT_DOCX, translate("tools.export.docx"), this, "o_mi_docx_export");
		exportDocLink.setIconLeftCSS("o_icon o_icon_download");
		exportTools.addComponent(exportDocLink);
		convertQTI21Link = LinkFactory.createToolLink(CMD_TOOLS_CONVERT_TO_QTI21, translate("tools.convert.qti21"), this, "o_FileResource-IMSQTI21_icon");
		convertQTI21Link.setIconLeftCSS("o_icon o_FileResource-IMSQTI21_icon");
		exportTools.addComponent(convertQTI21Link);

		//add
		Dropdown addItemTools = new Dropdown("editTools", "tools.add.header", false, getTranslator());
		addItemTools.setIconCSS("o_icon o_icon-fw o_icon_add");
		stackedPanel.addTool(addItemTools, Align.left);
		
		addSectionLink = LinkFactory.createToolLink(CMD_TOOLS_ADD_SECTION, translate("tools.add.section"), this, "o_mi_qtisection");
		addItemTools.addComponent(addSectionLink);
		addSCLink = LinkFactory.createToolLink(CMD_TOOLS_ADD_SINGLECHOICE, translate("tools.add.singlechoice"), this, "o_mi_qtisc");
		addItemTools.addComponent(addSCLink);
		addMCLink = LinkFactory.createToolLink(CMD_TOOLS_ADD_MULTIPLECHOICE, translate("tools.add.multiplechoice"), this, "o_mi_qtimc");
		addItemTools.addComponent(addMCLink);
		if (!qtiPackage.getQTIDocument().isSurvey()) {
			addKPrimLink = LinkFactory.createToolLink(CMD_TOOLS_ADD_KPRIM, translate("tools.add.kprim"), this, "o_mi_qtikprim");
			addItemTools.addComponent(addKPrimLink);
		}
		addFIBLink = LinkFactory.createToolLink(CMD_TOOLS_ADD_FIB, translate("tools.add.cloze"), this, "o_mi_qtifib");
		addItemTools.addComponent(addFIBLink);
		addEssayLink = LinkFactory.createToolLink(CMD_TOOLS_ADD_FREETEXT, translate("tools.add.freetext"), this, "o_mi_qtiessay");
		addItemTools.addComponent(addEssayLink);

		addItemTools.addComponent(new Spacer(""));
		addPoolLink = LinkFactory.createToolLink(CMD_TOOLS_ADD_QPOOL, translate("tools.import.qpool"), this, "o_mi_qpool_import");
		addItemTools.addComponent(addPoolLink);
	
		importTableLink = LinkFactory.createToolLink(CMD_TOOLS_IMPORT_TABLE, translate("tools.import.table"), this, "o_mi_table_import");
		importTableLink.setIconLeftCSS("o_icon o_icon_table o_icon-fw");
		addItemTools.addComponent(importTableLink);
		
		// delete / move / copy 
		Dropdown customizeTools = new Dropdown("customizeTools", "tools.change.header", false, getTranslator());
		customizeTools.setIconCSS("o_icon o_icon_customize");
		stackedPanel.addTool(customizeTools, Align.left);
		
		deleteLink = LinkFactory.createToolLink(CMD_TOOLS_CHANGE_DELETE, translate("tools.change.delete"), this);
		deleteLink.setIconLeftCSS("o_icon o_icon_delete_item");
		customizeTools.addComponent(deleteLink);
		moveLink = LinkFactory.createToolLink(CMD_TOOLS_CHANGE_MOVE, translate("tools.change.move"), this);
		moveLink.setIconLeftCSS("o_icon o_icon_move");
		customizeTools.addComponent(moveLink);
		copyLink = LinkFactory.createToolLink(CMD_TOOLS_CHANGE_COPY, translate("tools.change.copy"), this);
		copyLink.setIconLeftCSS("o_icon o_icon_copy");
		customizeTools.addComponent(copyLink);
		
		previewLink = LinkFactory.createToolLink(CMD_TOOLS_PREVIEW, translate("tools.tools.preview"), this);
		previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		stackedPanel.addTool(previewLink, Align.right);
		closeLink = LinkFactory.createToolLink(CMD_TOOLS_CLOSE_EDITOR, translate("tools.tools.closeeditor"), this, "o_toolbox_close");
		closeLink.setIconLeftCSS("o_icon o_icon_publish");
		stackedPanel.addTool(closeLink, Align.right);
	}

	@Override
	public boolean requestForClose(UserRequest ureq) {		
		// enter save/discard dialog if not already in it
		if(exportLatch != null) {
			try {
				if(!exportLatch.await(30, TimeUnit.SECONDS)) {
					logWarn("Cannot close editor in 30s.", null);
				}
			} catch (InterruptedException e) {
				logError("", e);
			}
		}
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

		// the owners of this qtiPkg
		RepositoryEntry myEntry = repositoryManager.lookupRepositoryEntry(qtiPackage.getRepresentingResourceable(), false);
		
		// add qti resource owners as group
		ContactList cl = new ContactList("qtiPkgOwners");
		cl.addAllIdentites(repositoryService.getMembers(myEntry, RepositoryEntryRelationType.all, GroupRoles.owner.name()));
		changeEmail.addEmailTo(cl);

		StringBuilder result = new StringBuilder();
		result.append(translate("qti.restricted.leading"));
		for (Iterator<Reference> iter = referencees.iterator(); iter.hasNext();) {
			Reference element = iter.next();
			if ("CourseModule".equals(element.getSource().getResourceableTypeName())) {
				ICourse course = null;
				try {
					course = CourseFactory.loadCourse(element.getSource().getResourceableId());
					if(course == null) {
						continue;
					}
				} catch(CorruptedCourseException ex) {
					logError("", ex);
					continue;
				}
				
				CourseNode cn = course.getEditorTreeModel().getCourseNode(element.getUserdata());
				if(cn == null) {
					logError("Cannot find course element " + element.getUserdata() + " in course " + course, null);
					continue;
				}

				String courseTitle = course.getCourseTitle();
				StringBuilder stakeHolders = new StringBuilder();
				
				// the course owners
				RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(course, false);
				if(entry != null) {//OO-1300
					List<Identity> stakeHoldersIds = repositoryService.getMembers(entry, RepositoryEntryRelationType.all, GroupRoles.owner.name());
					if(stakeHoldersIds != null && !stakeHoldersIds.isEmpty()) {
						// add stakeholders as group
						cl = new ContactList(courseTitle);
						cl.addAllIdentites(stakeHoldersIds);
						changeEmail.addEmailTo(cl);
		
						for (Identity stakeHoldersId:stakeHoldersIds) {
							if(stakeHolders.length() > 0) stakeHolders.append(", ");
							User user = stakeHoldersId.getUser();
							stakeHolders.append(user.getProperty(UserConstants.FIRSTNAME, getLocale())).append(" ").append(user.getProperty(UserConstants.LASTNAME, getLocale()));
						}
					}
				}

				String courseNodeTitle = cn.getShortTitle();
				result.append(translate("qti.restricted.course", StringHelper.escapeHtml(courseTitle)));
				result.append(translate("qti.restricted.node", StringHelper.escapeHtml(courseNodeTitle)));
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
				Map<String,String> itemMap = new HashMap<>();

				public void visit(INode node) {
					if (node instanceof AssessmentNode) {
						AssessmentNode an = (AssessmentNode) node;
						String key = "null/null/null/null";
						if (history.containsKey(key)) {
							// some assessment top level data changed
							Memento mem = history.get(key);
							result.append("---+ Changes in test " + formatVariable(startedWithTitle) + ":");
							result.append(an.createChangeMessage(mem));
						}
					} else if (node instanceof SectionNode) {
						SectionNode sn = (SectionNode) node;
						String tmpKey = ((Section) sn.getUnderlyingQTIObject()).getIdent();
						String key = tmpKey + "/null/null/null";
						if (history.containsKey(key)) {
							// some section only data changed
							Memento mem = history.get(key);
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
								questMem = history.get(key);
								hasChanges = true;
							}
							// if(!hasChanges){
							// check if a response changed
							// new prefix for responses
							prefixKey += "/null/";
							// list contains org.olat.ims.qti.editor.beecom.objects.Response
							List<Response> responses = question != null ? question.getResponses() : null;
							if (responses != null && responses.size() > 0) {
								// check for changes in each response
								for (Iterator<Response> iter = responses.iterator(); iter.hasNext();) {
									Response resp = iter.next();
									if (history.containsKey(prefixKey + resp.getIdent())) {
										// this response changed!
										Memento tmpMem = history.get(prefixKey + resp.getIdent());
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
	
	public boolean isBlockedEdit() {
		return blockedEdit;
	}

	public boolean isLockedSuccessfully() {
		return lockEntry.isSuccess();
	}

}