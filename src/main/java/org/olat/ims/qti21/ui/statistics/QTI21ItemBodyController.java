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
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.InMemoryAssessmentTestSession;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.ims.qti21.ui.assessment.TerminatedStaticCandidateSessionContext;
import org.olat.ims.qti21.ui.components.ItemBodyResultFormItem;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
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
 * The controller is used as wrapper for the item body
 * form item.
 * 
 * Initial date: 19 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ItemBodyController extends FormBasicController {
	
	private final String mapperUri;
	private final URI assessmentObjectUri;
	private final ResourceLocator inputResourceLocator;
	private final ItemSessionController itemSessionController;
	private final ResolvedAssessmentItem resolvedAssessmentItem;
	
	private ItemBodyResultFormItem questionItem;
	
	@Autowired
	private QTI21Service qtiService;
	
	public QTI21ItemBodyController(UserRequest ureq, WindowControl wControl,
			AssessmentItemRef itemRef, ResolvedAssessmentItem resolvedAssessmentItem, QTI21StatisticResourceResult resourceResult) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(AssessmentTestDisplayController.class, getLocale(), getTranslator()));
		
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		itemSessionController = createNewItemSessionStateAndController();
		
		File itemFileRef = resourceResult.getAssessmentItemFile(itemRef);
		File fUnzippedDirRoot = resourceResult.getUnzippedDirectory();
		ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
		inputResourceLocator = ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
		assessmentObjectUri = itemFileRef.toURI();
		mapperUri = registerCacheableMapper(null, "QTI21StatisticsElement::" + CodeHelper.getRAMUniqueID(),
				new ResourcesMapper(assessmentObjectUri, fUnzippedDirRoot));

		initForm(ureq);
	}
	
	public String getMapperUri() {
		return mapperUri;
	}
	
	public String getInteractionsComponentId() {
		return questionItem.getComponent().getDispatchID();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		questionItem = new ItemBodyResultFormItem("question", resolvedAssessmentItem);
		questionItem.setItemSessionState(itemSessionController.getItemSessionState());
		questionItem.setCandidateSessionContext(new TerminatedStaticCandidateSessionContext(new InMemoryAssessmentTestSession()));
		questionItem.setResourceLocator(inputResourceLocator);
		questionItem.setAssessmentObjectUri(assessmentObjectUri);
		questionItem.setMapperUri(mapperUri);
		formLayout.add(questionItem);
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

        sessionController.initialize(new Date());
        return sessionController;
    }

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
