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
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.InMemoryAssessmentTestSession;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.ims.qti21.ui.assessment.TerminatedStaticCandidateSessionContext;
import org.olat.ims.qti21.ui.components.ItemBodyResultFormItem;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
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
	
	private final String mapperUri;
	private final AssessmentItem item;
	private final URI assessmentObjectUri;
	private final ResourceLocator inputResourceLocator;
	private final ResolvedAssessmentItem resolvedAssessmentItem;
	
	private final ItemSessionController itemSessionController;
	
	@Autowired
	private QTI21Service qtiService;

	public UnkownItemEditorController(UserRequest ureq, WindowControl wControl,
			ResolvedAssessmentItem resolvedAssessmentItem, AssessmentItem item, File fUnzippedDirRoot) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.item = item;
		this.resolvedAssessmentItem = resolvedAssessmentItem;

		itemSessionController = createNewItemSessionStateAndController();
		
		ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
		inputResourceLocator = 
        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
		assessmentObjectUri = qtiService.createAssessmentObjectUri(fUnzippedDirRoot);
		
		mapperUri = registerCacheableMapper(null, "QTI21AlienElement::" + CodeHelper.getRAMUniqueID(),
				new ResourcesMapper(assessmentObjectUri));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormWarning("warning.alien.assessment.item");
		
		String title = StringHelper.escapeHtml(item.getTitle());
		uifactory.addStaticTextElement("title", "form.imd.title", title, formLayout);

		String responseId = "responseBody" + CodeHelper.getRAMUniqueID();
		ItemBodyResultFormItem formItem = new ItemBodyResultFormItem(responseId, resolvedAssessmentItem);
		formLayout.add(responseId, formItem);
		formItem.setLabel("form.imd.descr", null);

		formItem.setItemSessionState(itemSessionController.getItemSessionState());
		formItem.setCandidateSessionContext(new TerminatedStaticCandidateSessionContext(new InMemoryAssessmentTestSession()));
		formItem.setResourceLocator(inputResourceLocator);
		formItem.setAssessmentObjectUri(assessmentObjectUri);
		formItem.setMapperUri(mapperUri);
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
        final ItemSessionController itemSessionController = new ItemSessionController(qtiService.jqtiExtensionManager(),
                itemSessionControllerSettings, itemProcessingMap, itemSessionState);
        itemSessionController.addNotificationListener(new NotificationRecorder(NotificationLevel.ERROR));
        
        
        itemSessionController.initialize(new Date());
        return itemSessionController;
    }

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
