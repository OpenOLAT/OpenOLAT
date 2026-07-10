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
package org.olat.ims.qti21.pool;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;

import org.olat.core.commons.services.ai.essay.AiSourceCompanion;
import org.olat.core.commons.services.ai.essay.AiSourceCompanionFileStore;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.ui.editor.AssessmentItemEditorController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.olat.modules.qpool.QPoolItemEditorController;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.events.QItemEdited;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;

/**
 * 
 * Initial date: 08.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21EditorController extends BasicController implements QPoolItemEditorController {
	
	private final VelocityContainer mainVC;
	private AssessmentItemEditorController editorCtrl;
	
	private final File resourceFile;
	private QuestionItem questionItem;
	private final File resourceDirectory;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private AiSourceCompanionFileStore aiSourceCompanionFileStore;
	
	public QTI21EditorController(UserRequest ureq, WindowControl wControl, QuestionItem questionItem,
			boolean readonly) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentItemEditorController.class, ureq.getLocale()));
		this.questionItem = questionItem;
		
		resourceDirectory = qpoolService.getRootDirectory(questionItem);
		VFSContainer resourceContainer = qpoolService.getRootContainer(questionItem);
		resourceFile = qpoolService.getRootFile(questionItem);
		if(resourceFile == null) {
			mainVC = createVelocityContainer("missing_resource");
			mainVC.contextPut("uri", questionItem == null ? null : questionItem.getKey());
		} else {
			URI assessmentItemUri = resourceFile.toURI();
			ResolvedAssessmentItem resolvedAssessmentItem = qtiService
					.loadAndResolveAssessmentItem(assessmentItemUri, resourceDirectory);
			
			editorCtrl = new AssessmentItemEditorController(ureq, wControl, resolvedAssessmentItem, resourceDirectory,
					resourceContainer, resourceFile, false, readonly);
			listenTo(editorCtrl);
			mainVC = createVelocityContainer("pool_editor");
			mainVC.put("editor", editorCtrl.getInitialComponent());
		}
		putInitialPanel(mainVC);
	}
	
	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public QuestionItem getItem() {
		return questionItem;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == editorCtrl) {
			if(event instanceof AssessmentItemEvent aie) {
				AssessmentItem assessmentItem = aie.getAssessmentItem();
				qtiService.persistAssessmentObject(resourceFile, assessmentItem);
				updateQuestionItem(ureq, assessmentItem);
			}
		}
	}

	private void updateQuestionItem(UserRequest ureq, AssessmentItem assessmentItem) {
		if(questionItem instanceof QuestionItemImpl itemImpl) {
			String title = assessmentItem.getTitle();
			itemImpl.setTitle(title);
			Double maxScore = QtiNodesExtractor.extractMaxScore(assessmentItem);
			if(maxScore != null) {
				itemImpl.setMaxScore(BigDecimal.valueOf(maxScore.doubleValue()));
			} else {
				itemImpl.setMaxScore(null);
			}
			
			QItemType type = new QTI21ImportProcessor(getIdentity(), getLocale())
					.convertType(assessmentItem);
			if(type != null && !type.equals(itemImpl.getType())) {
				itemImpl.setType(type);
			}
			
			// Remove the unsupervised flag
			if(itemImpl.getAiUnsupervisedGenerated() != null && itemImpl.getAiUnsupervisedGenerated().booleanValue()) {
				itemImpl.setAiUnsupervisedGenerated(Boolean.FALSE);
				String fullname = userManager.getUserDisplayName(getIdentity());
				itemImpl.setAiSupervisedBy(fullname);
				
				AiSourceCompanion aiSource = aiSourceCompanionFileStore.load(resourceDirectory);
				if(aiSource != null && !StringHelper.containsNonWhitespace(aiSource.getSupervisedBy())) {
					String displayName = userManager.getUserDisplayName(getIdentity());
					aiSource.setSupervisedBy(displayName);
					aiSource.setUnsupervisedGenerated(false);
					aiSourceCompanionFileStore.save(resourceDirectory, aiSource);
				}
			}
			
			qpoolService.updateItem(itemImpl);
			fireEvent(ureq, new QItemEdited(questionItem));
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
