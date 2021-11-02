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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.helpers.Settings;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.InMemoryAssessmentTestSession;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AlienItemAnalyzer;
import org.olat.ims.qti21.model.xml.AlienItemAnalyzer.Report;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.ims.qti21.ui.assessment.TerminatedStaticCandidateSessionContext;
import org.olat.ims.qti21.ui.components.ItemBodyResultFormItem;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.notification.NotificationLevel;
import uk.ac.ed.ph.jqtiplus.notification.NotificationRecorder;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.running.ItemProcessingInitializer;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionControllerSettings;
import uk.ac.ed.ph.jqtiplus.state.ItemProcessingMap;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 26.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UnkownItemEditorController extends FormBasicController {
	
	private FormLink convertLink;
	
	private CloseableModalController cmc;
	private UnkownItemConversionConfirmationController confirmationCtrl;
	
	private final String mapperUri;
	private final File itemFileRef;
	private final AssessmentItem item;
	private final URI assessmentObjectUri;
	private final ResourceLocator inputResourceLocator;
	private final ResolvedAssessmentItem resolvedAssessmentItem;
	
	private final ItemSessionController itemSessionController;
	
	@Autowired
	private QTI21Service qtiService;

	public UnkownItemEditorController(UserRequest ureq, WindowControl wControl,
			ResolvedAssessmentItem resolvedAssessmentItem, AssessmentItem item, File itemFileRef, File fUnzippedDirRoot) {
		super(ureq, wControl, "unkown_assessment_item");
		setTranslator(Util.createPackageTranslator(AssessmentTestDisplayController.class, getLocale(), getTranslator()));
		this.item = item;
		this.itemFileRef = itemFileRef;
		this.resolvedAssessmentItem = resolvedAssessmentItem;

		itemSessionController = createNewItemSessionStateAndController();
		
		ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
		inputResourceLocator = 
        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
		assessmentObjectUri = itemFileRef.toURI();
		
		mapperUri = registerCacheableMapper(null, "QTI21AlienElement::" + CodeHelper.getRAMUniqueID(),
				new ResourcesMapper(assessmentObjectUri, fUnzippedDirRoot));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormWarning("warning.alien.assessment.item");
		
		convertLink = uifactory.addFormLink("convert.alien", formLayout, Link.BUTTON);
		convertLink.setEnabled(canConvert());
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			
			String title = StringHelper.escapeHtml(item.getTitle());
			layoutCont.contextPut("title", title);
	
			String responseId = "responseBody" + CodeHelper.getRAMUniqueID();
			ItemBodyResultFormItem formItem = new ItemBodyResultFormItem(responseId, resolvedAssessmentItem);
			formLayout.add(responseId, formItem);
			layoutCont.contextPut("responseId", responseId);
			formItem.setLabel("form.imd.descr", null);
	
			formItem.setItemSessionState(itemSessionController.getItemSessionState());
			formItem.setCandidateSessionContext(new TerminatedStaticCandidateSessionContext(new InMemoryAssessmentTestSession()));
			formItem.setResourceLocator(inputResourceLocator);
			formItem.setAssessmentObjectUri(assessmentObjectUri);
			formItem.setMapperUri(mapperUri);
		}
	}
	
	private boolean canConvert() {
		AlienItemAnalyzer analyzer = new AlienItemAnalyzer(item);
		Report report = analyzer.analyze();
		return report.getType() != QTI21QuestionType.unkown && !report.isBlocker();
	}
	
	private ItemSessionController createNewItemSessionStateAndController() {
        /* Resolve the underlying JQTI+ object */
        final ItemProcessingMap itemProcessingMap = new ItemProcessingInitializer(resolvedAssessmentItem, true).initialize();

        /* Create fresh state for session */
        final ItemSessionState itemSessionState = new ItemSessionState();
        final ItemSessionControllerSettings itemSessionControllerSettings = new ItemSessionControllerSettings();
        itemSessionControllerSettings.setTemplateProcessingLimit(25);
        itemSessionControllerSettings.setMaxAttempts(10);

        /* Create controller and wire up notification recorder */
        final ItemSessionController sessionController = new ItemSessionController(qtiService.jqtiExtensionManager(),
                itemSessionControllerSettings, itemProcessingMap, itemSessionState);
        sessionController.addNotificationListener(new NotificationRecorder(NotificationLevel.ERROR));
        
        Long randomSeed = new Random().nextLong();
        sessionController.setRandomSeed(randomSeed);
        sessionController.initialize(new Date());
        sessionController.performTemplateProcessing(new Date());
        return sessionController;
    }

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmationCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doConvertItem(ureq, confirmationCtrl.getSelectedQuestionType());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmationCtrl);
		removeAsListenerAndDispose(cmc);
		confirmationCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(convertLink == source) {
			doConfirmConversion(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}


	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doConfirmConversion(UserRequest ureq) {
		Report report = new AlienItemAnalyzer(item).analyze();
		confirmationCtrl = new UnkownItemConversionConfirmationController(ureq, getWindowControl(), report);				
		listenTo(confirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmationCtrl.getInitialComponent(), true, translate("convert.alien"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doConvertItem(UserRequest ureq, QTI21QuestionType alternativeType) {
		item.setToolName(QTI21Constants.TOOLNAME);
		item.setToolVersion(Settings.getVersion());
		if(alternativeType == QTI21QuestionType.matchdraganddrop) {
			addClassToInteraction(QTI21Constants.CSS_MATCH_DRAG_AND_DROP);
		}
		qtiService.updateAssesmentObject(itemFileRef, resolvedAssessmentItem);
		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_NEED_RELOAD, item));
	}
	
	private void addClassToInteraction(String cssClass) {
		List<Interaction> interactions = item.getItemBody().findInteractions();
		for(Interaction interaction:interactions) {
			List<String> cssClasses = interaction.getClassAttr();
			if(cssClasses == null) {
				cssClasses = new ArrayList<>();
			} else {
				cssClasses = new ArrayList<>(cssClasses);
			}
			if(!cssClasses.contains(cssClass)) {
				cssClasses.add(cssClass);
			}
			interaction.setClassAttr(cssClasses);
		}
	}
}
