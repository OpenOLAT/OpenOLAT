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
package org.olat.course.nodes.iq;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Constants;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.editor.beecom.objects.Assessment;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.QTIDocument;
import org.olat.ims.qti.editor.beecom.objects.Section;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions.ShowResultsOnFinish;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.InMemoryOutcomeListener;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.iq.IQManager;
import org.olat.modules.iq.IQPreviewSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import de.bps.onyx.plugin.OnyxModule;
import de.bps.onyx.plugin.course.nodes.iq.IQEditForm;
import de.bps.onyx.plugin.run.OnyxRunController;

/**
 * 
 * Initial date: 26.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IQConfigurationController extends BasicController {

	private static final String VC_CHOSENTEST = "chosentest";

	private VelocityContainer myContent;
	private final BreadcrumbPanel stackPanel;
	
	private Link previewLink, chooseTestButton, changeTestButton, editTestButton;

	private Controller previewLayoutCtr;
	private CloseableModalController cmc;
	private IQEditReplaceWizard replaceWizard;
	private AssessmentTestDisplayController previewQTI21Ctrl;
	private ReferencableEntriesSearchController searchController;
	
	private IQEditForm modOnyxConfigForm;
	private IQ12EditForm mod12ConfigForm;
	private QTI21EditForm mod21ConfigForm;
	
	private String type;
	private ICourse course;
	private List<Identity> learners;
	private ModuleConfiguration moduleConfiguration;
	private AbstractAccessableCourseNode courseNode;

	@Autowired
	private IQManager iqManager;
	@Autowired
	private QTI21Service qti21service;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryManager repositoryManager;

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param stackPanel
	 * @param course
	 * @param courseNode
	 * @param euce
	 * @param type
	 */
	public IQConfigurationController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course,
			AbstractAccessableCourseNode courseNode, UserCourseEnvironment euce, String type) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.moduleConfiguration = courseNode.getModuleConfiguration();
		//o_clusterOk by guido: save to hold reference to course inside editor
		this.course = course;
		this.courseNode = courseNode;
		this.type = type;
		
		
		myContent = createVelocityContainer("edit");		
		chooseTestButton = LinkFactory.createButtonSmall("command.chooseRepFile", myContent, this);
		chooseTestButton.setElementCssClass("o_sel_test_choose_repofile");
		changeTestButton = LinkFactory.createButtonSmall("command.changeRepFile", myContent, this);
		changeTestButton.setElementCssClass("o_sel_test_change_repofile");

		// fetch repository entry
		RepositoryEntry re = getIQReference();
		if(re == null) {
			myContent.contextPut(VC_CHOSENTEST, translate("no.file.chosen"));
		} else {
			String displayName = StringHelper.escapeHtml(re.getDisplayname());
			myContent.contextPut(VC_CHOSENTEST, displayName);
			myContent.contextPut("dontRenderRepositoryButton", new Boolean(true));
			// Put values to velocity container

			boolean isOnyx = OnyxModule.isOnyxTest(re.getOlatResource());
			if(isOnyx) {
				//
			} else if (isEditable(ureq.getIdentity(), ureq.getUserSession().getRoles(), re)) {
				editTestButton = LinkFactory.createButtonSmall("command.editRepFile", myContent, this);
			}

			previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", displayName, Link.NONTRANSLATED, myContent, this);
			previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
			previewLink.setCustomEnabledLinkCSS("o_preview");
			previewLink.setTitle(translate("command.preview"));
		}
		
		if(stackPanel != null) {
			stackPanel.addListener(this);
		}

		myContent.contextPut("type", type);
		
		putInitialPanel(myContent);	
		updateEditController(ureq);
		
		switch(type) {
			case AssessmentInstance.QMD_ENTRY_TYPE_ASSESS:
				myContent.contextPut("repEntryTitle", translate("choosenfile.test"));
				break;
			case AssessmentInstance.QMD_ENTRY_TYPE_SELF:
				myContent.contextPut("repEntryTitle", translate("choosenfile.self"));
				break;
			case AssessmentInstance.QMD_ENTRY_TYPE_SURVEY:
				myContent.contextPut("repEntryTitle", translate("choosenfile.surv"));
				chooseTestButton.setCustomDisplayText(translate("command.createSurvey"));
				break;
		}
	}
	
	protected void updateEditController(UserRequest ureq) {
		removeAsListenerAndDispose(mod12ConfigForm);
		removeAsListenerAndDispose(mod21ConfigForm);
		removeAsListenerAndDispose(modOnyxConfigForm);
		mod12ConfigForm = null;
		mod21ConfigForm = null;
		modOnyxConfigForm = null;
		
		RepositoryEntry re = getIQReference();
		if(re == null) {
			myContent.remove("iqeditform");
		} else if(ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
			boolean needManualCorrection = needManualCorrectionQTI21(re);
			QTI21DeliveryOptions deliveryOptions =  qti21service.getDeliveryOptions(re);
			mod21ConfigForm = new QTI21EditForm(ureq, getWindowControl(), moduleConfiguration, deliveryOptions, needManualCorrection);
			mod21ConfigForm.update(re);
			listenTo(mod21ConfigForm);
			myContent.put("iqeditform", mod21ConfigForm.getInitialComponent());
		} else if(OnyxModule.isOnyxTest(re.getOlatResource())) {
			modOnyxConfigForm = new IQEditForm(ureq, getWindowControl(), moduleConfiguration, courseNode, re);
			modOnyxConfigForm.update(re);
			listenTo(modOnyxConfigForm);
			myContent.put("iqeditform", modOnyxConfigForm.getInitialComponent());
		} else {
			boolean hasEssay = needManualCorrectionQTI12(re);
			mod12ConfigForm = new IQ12EditForm(ureq, getWindowControl(), moduleConfiguration, hasEssay);
			mod12ConfigForm.update(re.getOlatResource());
			listenTo(mod12ConfigForm);
			myContent.put("iqeditform", mod12ConfigForm.getInitialComponent());
		}
	}

	/**
	 * @param identity
	 * @param repository entry
	 * @return
	 */
	private boolean isEditable(Identity identity, Roles roles, RepositoryEntry re) {
		boolean isOnyx = OnyxModule.isOnyxTest(re.getOlatResource());
		if (isOnyx) {
			return false;
		}

		return (securityManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_ADMIN)
				|| repositoryManager.isOwnerOfRepositoryEntry(identity, re)
				|| repositoryManager.isInstitutionalRessourceManagerFor(identity, roles, re));
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (previewLink == source){
			doPreview(ureq);
		} else if (chooseTestButton == source){
			doChooseTestAndSurvey(ureq);
		} else if (changeTestButton == source) {
			RepositoryEntry re = courseNode.getReferencedRepositoryEntry();
			if(re == null) {
				showError("error.test.undefined.long", courseNode.getShortTitle());
			} else if(type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SELF)) {
				doChangeSelfTest(ureq);
			} else if(type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS) || type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY)) {
				doChangeTestAndSurvey(ureq, re);
			}	
		} else if (editTestButton == source) {
			CourseNodeFactory.getInstance().launchReferencedRepoEntryEditor(ureq, getWindowControl(), courseNode);
		} else if (stackPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent pop = (PopEvent)event;
				if(pop.getController() == previewQTI21Ctrl) {
					cleanUpQti21PreviewSession();
				}
			}
		}
	}
	
	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source.equals(searchController)) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				// repository search controller done				
				cmc.deactivate();
				RepositoryEntry re = searchController.getSelectedEntry();
				boolean needManualCorrection = checkManualCorrectionNeeded(re);
				doIQReference(urequest, re, needManualCorrection);
				updateEditController(urequest);
			}
		} else if (source == replaceWizard) {
			if(event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			} else if(event == Event.DONE_EVENT) {
				cmc.deactivate();
				String repositorySoftKey = (String) moduleConfiguration.get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
				Long repKey = repositoryManager.lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
				QTIResultManager.getInstance().deleteAllResults(course.getResourceableId(), courseNode.getIdent(), repKey);
				IQEditController.removeIQReference(moduleConfiguration);
				VFSStatus isDeleted = iqManager.removeQtiSerFiles(course.getResourceableId(), courseNode.getIdent());
				if (!isDeleted.equals(VFSConstants.YES)) {
					// couldn't removed qtiser files
					logWarn("Couldn't removed course node folder! Course resourceable id: " + course.getResourceableId() + ", Course node ident: " + courseNode.getIdent(), null);
				}
				
				RepositoryEntry re = replaceWizard.getSelectedRepositoryEntry();
				boolean needManualCorrection = checkManualCorrectionNeeded(re);
				doIQReference(urequest, re, needManualCorrection);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == mod12ConfigForm) {
			if (event == Event.DONE_EVENT) {
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == mod21ConfigForm) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == modOnyxConfigForm) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}
	
	private void doPreview(UserRequest ureq) {
		removeAsListenerAndDispose(previewLayoutCtr);
		
		RepositoryEntry re = getIQReference();
		if(re != null) {
			Controller previewController;
			if(OnyxModule.isOnyxTest(re.getOlatResource())) {
				Controller previewCtrl = new OnyxRunController(ureq, getWindowControl(), re, false);
				listenTo(previewCtrl);
				previewLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), previewCtrl);
				stackPanel.pushController(translate("preview"), previewLayoutCtr);
			} else if(ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
				cleanUpQti21PreviewSession();//clean up last session
				// need to clean up the assessment test session
				QTI21DeliveryOptions deliveryOptions = qti21service.getDeliveryOptions(re);
				RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				previewQTI21Ctrl = new AssessmentTestDisplayController(ureq, getWindowControl(), new InMemoryOutcomeListener(),
						re, courseEntry, courseNode.getIdent(),
						deliveryOptions, true, true, true);
				listenTo(previewQTI21Ctrl);
				stackPanel.pushController(translate("preview"), previewQTI21Ctrl);
			} else {
				long courseResId = course.getResourceableId().longValue();
				previewController = iqManager.createIQDisplayController(moduleConfiguration, new IQPreviewSecurityCallback(), ureq, getWindowControl(), courseResId, courseNode.getIdent(), null);
				previewLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), previewController);
				stackPanel.pushController(translate("preview"), previewLayoutCtr);
			}
		}
	}
	
	/**
	 * Delete the test session created by the preview controller
	 * for the QTI 2.1 tests.
	 * 
	 */
	private void cleanUpQti21PreviewSession() {
		if(previewQTI21Ctrl != null) {
			AssessmentTestSession previewSession = previewQTI21Ctrl.getCandidateSession();
			qti21service.deleteAssessmentTestSession(previewSession);
		}
	}
	
	private void doChooseTestAndSurvey(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(searchController);
		
		if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY)) {
			searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, 
					SurveyFileResource.TYPE_NAME, translate("command.chooseSurvey"));
		} else { // test and selftest use same repository resource type
			searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, 
					new String[] { TestFileResource.TYPE_NAME, ImsQTI21Resource.TYPE_NAME }, translate("command.chooseTest"));
		}			
		listenTo(searchController);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent(), true, translate("command.chooseRepFile"));
		cmc.activate();
	}
	
	private void doChangeSelfTest(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(searchController);
		
		String[] types = new String[]{ TestFileResource.TYPE_NAME, ImsQTI21Resource.TYPE_NAME };
		searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, types, translate("command.chooseTest"));
		listenTo(searchController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent());
		listenTo(searchController);
		cmc.activate();
	}
	
	private void doChangeTestAndSurvey(UserRequest ureq, RepositoryEntry re) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(searchController);
		
		String[] types;
		if(type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {//test
			types = new String[]{ TestFileResource.TYPE_NAME, ImsQTI21Resource.TYPE_NAME };
		} else {//survey
			types = new String[]{ SurveyFileResource.TYPE_NAME };
		}
		
		if (moduleConfiguration.get(IQEditController.CONFIG_KEY_TYPE_QTI) == null) {
			updateQtiType(re);
		}

		int onyxSuccess = 0;
		if (moduleConfiguration.get(IQEditController.CONFIG_KEY_TYPE_QTI) != null && moduleConfiguration.get(IQEditController.CONFIG_KEY_TYPE_QTI).equals(IQEditController.CONFIG_VALUE_QTI2)) {
			if (courseNode.getClass().equals(IQSURVCourseNode.class)) {
				File surveyDir = new File(course.getCourseEnvironment().getCourseBaseContainer()
						.getBasefile() + File.separator + courseNode.getIdent() + File.separator);
				if (surveyDir != null && surveyDir.exists() && surveyDir.listFiles().length > 0) {
					onyxSuccess = surveyDir.listFiles().length;
				}
			} else {
				onyxSuccess = QTIResultManager.getInstance().countResults(course.getResourceableId(), courseNode.getIdent(), re.getKey());
			}
		}
		
		
		if (moduleConfiguration.get(IQEditController.CONFIG_KEY_TYPE_QTI) != null
				&& moduleConfiguration.get(IQEditController.CONFIG_KEY_TYPE_QTI).equals(IQEditController.CONFIG_VALUE_QTI2)
				&& onyxSuccess > 0) {
			
			replaceWizard = new IQEditReplaceWizard(ureq, getWindowControl(), course, courseNode, types, learners, null, onyxSuccess, true);
			replaceWizard.addControllerListener(this);
			
			String title = replaceWizard.getAndRemoveWizardTitle();
			cmc = new CloseableModalController(getWindowControl(), translate("close"), replaceWizard.getInitialComponent(), true, title);
			cmc.activate();
		} else {
			
			List<QTIResult> results = QTIResultManager.getInstance().selectResults(course.getResourceableId(), courseNode.getIdent(), re.getKey(), null, 1);
			// test was passed from an user
			boolean passed = (results != null && results.size() > 0) ? true : false;
			// test was started and not passed
			// it exists partly results for this test
			List<Identity> identitiesWithQtiSerEntry = iqManager.getIdentitiesWithQtiSerEntry(course.getResourceableId(), courseNode.getIdent());
			if(passed || identitiesWithQtiSerEntry.size() > 0) {
				learners = new ArrayList<Identity>();
				for(QTIResult result : results) {
					Identity identity = result.getResultSet().getIdentity();
					if(identity != null && !learners.contains(identity)){
						learners.add(identity);
					}
				}
				// add identities with qti.ser entry
				for (Identity identity : identitiesWithQtiSerEntry) {
					if(!learners.contains(identity)) {
						learners.add(identity);
					}
				}
				replaceWizard = new IQEditReplaceWizard(ureq, getWindowControl(), course, courseNode, types, learners, results, identitiesWithQtiSerEntry.size(), false);
				replaceWizard.addControllerListener(this);
				
				cmc = new CloseableModalController(getWindowControl(), translate("close"), replaceWizard.getInitialComponent());
				cmc.activate();
			} else {
				if(type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {//test					
					searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, types, translate("command.chooseTest"));
				} else {//survey
					searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, types, translate("command.chooseSurvey"));
				}
				listenTo(searchController);
				
				cmc = new CloseableModalController(getWindowControl(), translate("close"), searchController.getInitialComponent());
				cmc.activate();
			}
		}	
	}

	/**
	 * This method updates the QTI Type in the editortreemodel.
	 * 
	 * @param re Needed to check if this Test is of QTI Type 2.1
	 */
	private void updateQtiType(RepositoryEntry re) {
		boolean isOnyx = OnyxModule.isOnyxTest(re.getOlatResource());
		if (isOnyx) {
			moduleConfiguration.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI2);
		} else {
			moduleConfiguration.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI1);
		}
	}

	private boolean checkManualCorrectionNeeded(RepositoryEntry re) {
		if(OnyxModule.isOnyxTest(re.getOlatResource())) {
			return false;
		}
		if(courseNode instanceof IQSURVCourseNode || courseNode instanceof IQSELFCourseNode) {
			//nothing to do
		} else if(ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
			if(needManualCorrectionQTI21(re)) {
				showWarning("warning.test.with.essay");
				return true;
			}
		} else {
			if(needManualCorrectionQTI12(re)) {
				showWarning("warning.test.with.essay");
				return true;
			}
		}
		return false;
	}
	
	private boolean needManualCorrectionQTI21(RepositoryEntry re) {
		return qti21service.needManualCorrection(re);
	}
	
	private boolean needManualCorrectionQTI12(RepositoryEntry re) {
		boolean needManualCorrection = false;
		QTIDocument doc = TestFileResource.getQTIDocument(re.getOlatResource());
		if(doc != null && doc.getAssessment() != null) {
			Assessment ass = doc.getAssessment();
			//Sections with their Items
			List<Section> sections = ass.getSections();
			for (Section section:sections) {
				List<Item> items = section.getItems();
				for (Item item:items) {
					String ident = item.getIdent();
					if(ident != null && ident.startsWith("QTIEDIT:ESSAY")) {
						needManualCorrection = true;
						break;
					}
				}
			}
		}
		return needManualCorrection;
	}
	
	private void doIQReference(UserRequest urequest, RepositoryEntry re, boolean manualCorrection) {
		// repository search controller done				
		if (re != null) {
			if (CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(re.getOlatResource(), null)) {
				LockResult lockResult = CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(re.getOlatResource(), urequest.getIdentity(), null);
				String fullName = CoreSpringFactory.getImpl(UserManager.class).getUserDisplayName(lockResult.getOwner());
				showError("error.entry.locked", fullName);
				if(lockResult.isSuccess()) {
					//improbable concurrency security
					CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockResult);
				}
			} else {
				if(editTestButton != null) {
					myContent.remove(editTestButton);
				}

				IQEditController.setIQReference(re, moduleConfiguration);
				String displayName = StringHelper.escapeHtml(re.getDisplayname());
				previewLink = LinkFactory.createCustomLink("command.preview", "command.preview", displayName, Link.NONTRANSLATED, myContent, this);
				previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
				previewLink.setCustomEnabledLinkCSS("o_preview");
				previewLink.setTitle(getTranslator().translate("command.preview"));
				myContent.contextPut("dontRenderRepositoryButton", new Boolean(true));
				// If of type test, get min, max, cut - put in module config and push
				// to velocity
				
				boolean isOnyx = OnyxModule.isOnyxTest(re.getOlatResource());
				myContent.contextPut("isOnyx", new Boolean(isOnyx));
				if(isOnyx) {
					myContent.contextPut("onyxDisplayName", displayName);
					moduleConfiguration.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI2);
				} else if(ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
					moduleConfiguration.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI21);
					QTI21DeliveryOptions deliveryOptions = qti21service.getDeliveryOptions(re);
					if(deliveryOptions != null) {
						ShowResultsOnFinish showSummary = deliveryOptions.getShowResultsOnFinish();
						String defaultConfSummary = showSummary == null ? AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT : showSummary.getIQEquivalent();
						moduleConfiguration.set(IQEditController.CONFIG_KEY_SUMMARY, defaultConfSummary);
					}
					if (isEditable(urequest.getIdentity(), urequest.getUserSession().getRoles(), re)) {
						editTestButton = LinkFactory.createButtonSmall("command.editRepFile", myContent, this);
					}
				} else {
					moduleConfiguration.set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI1);
					if (isEditable(urequest.getIdentity(), urequest.getUserSession().getRoles(), re)) {
						editTestButton = LinkFactory.createButtonSmall("command.editRepFile", myContent, this);
					}
				}
				
				if(manualCorrection) {
					myContent.contextPut(IQEditController.CONFIG_CORRECTION_MODE, "manual");
				} else {
					myContent.contextPut(IQEditController.CONFIG_CORRECTION_MODE, "auto");
				}
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}

	/**
	 * Get the qti file soft key repository reference 
	 * @param config
	 * @param strict
	 * @return RepositoryEntry
	 */
	private RepositoryEntry getIQReference() {
		if (moduleConfiguration == null) return null;
		String repoSoftkey = (String)moduleConfiguration.get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		if (repoSoftkey == null) return null;
		return repositoryManager.lookupRepositoryEntryBySoftkey(repoSoftkey, false);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		//child controllers registered with listenTo() get disposed in BasicController
		if (previewLayoutCtr != null) {
			previewLayoutCtr.dispose();
			previewLayoutCtr = null;
		}
		cleanUpQti21PreviewSession();
	}
}