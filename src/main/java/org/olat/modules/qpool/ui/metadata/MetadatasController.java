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
package org.olat.modules.qpool.ui.metadata;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemSecurityCallback;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.events.QItemEdited;
import org.olat.modules.qpool.ui.events.QPoolEvent;

/**
 * 
 * Initial date: 24.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MetadatasController extends BasicController {

	private final VelocityContainer mainVC;
	private final SharingController sharingCtrl;
	private final GeneralMetadataController generalCtrl;
	private final EducationalMetadataController educationalCtrl;
	private final QuestionMetadataController questionCtrl;
	private final LifecycleMetadataController lifecycleCtrl;
	private final TechnicalMetadataController technicalCtrl;
	private final RightsMetadataController rightsCtrl;
	
	private GeneralMetadataEditController generalEditCtrl;
	private EducationalMetadataEditController educationalEditCtrl;
	private QuestionMetadataEditController questionEditCtrl;
	private LifecycleMetadataEditController lifecycleEditCtrl;
	private TechnicalMetadataEditController technicalEditCtrl;
	private RightsMetadataEditController rightsEditCtrl;
	
	private QuestionItem item;
	private final QuestionItemSecurityCallback securityCallback; 
	private final QPoolService qpoolService;
	
	public MetadatasController(UserRequest ureq, WindowControl wControl, QuestionItem item, QuestionItemSecurityCallback securityCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		
		this.item = item;
		this.securityCallback = securityCallback;
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);

		generalCtrl = new GeneralMetadataController(ureq, wControl, item, securityCallback.canEditMetadata());
		listenTo(generalCtrl);
		educationalCtrl = new EducationalMetadataController(ureq, wControl, item, securityCallback.canEditMetadata());
		listenTo(educationalCtrl);
		questionCtrl = new QuestionMetadataController(ureq, wControl, item, securityCallback.canEditMetadata());
		listenTo(questionCtrl);
		lifecycleCtrl = new LifecycleMetadataController(ureq, wControl, item, securityCallback.canEditLifecycle());
		listenTo(lifecycleCtrl);
		technicalCtrl = new TechnicalMetadataController(ureq, wControl, item, securityCallback.canEditMetadata());
		listenTo(technicalCtrl);
		rightsCtrl = new RightsMetadataController(ureq, wControl, item, securityCallback.canEditMetadata());
		listenTo(rightsCtrl);
		sharingCtrl = new SharingController(ureq, wControl, item);
		listenTo(sharingCtrl);

		mainVC = createVelocityContainer("item_metadatas");
		mainVC.put("details_general", generalCtrl.getInitialComponent());
		mainVC.put("details_educational", educationalCtrl.getInitialComponent());
		mainVC.put("details_question", questionCtrl.getInitialComponent());
		mainVC.put("details_lifecycle", lifecycleCtrl.getInitialComponent());
		mainVC.put("details_technical", technicalCtrl.getInitialComponent());
		mainVC.put("details_rights", rightsCtrl.getInitialComponent());
		mainVC.put("details_sharing", sharingCtrl.getInitialComponent());
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public QuestionItem getItem() {
		return item;
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(QPoolEvent.EDIT.endsWith(event.getCommand())) {
			if(securityCallback.canEditMetadata()) {
				if(source == generalCtrl) {
					doEditGeneralMetadata(ureq);
				} else if(source == educationalCtrl) {
					doEditEducationalMetadata(ureq);
				} else if(source == questionCtrl) {
					doEditQuestionMetadata(ureq);
				} else if(source == technicalCtrl) {
					doEditTechnicalMetadata(ureq);
				} else if(source == rightsCtrl) {
					doEditRightsMetadata(ureq);
				}
			} else if (securityCallback.canEditLifecycle()) {
				if(source == lifecycleCtrl) {
					doEditLifecycleMetadata(ureq);
				}
			}
		} else if(event instanceof QItemEdited) {
			QItemEdited editEvent = (QItemEdited)event;
			if (source == generalEditCtrl) {
				doGeneralMetadataFinishEditing();
			} else if (source == educationalEditCtrl) {
				doEducationalMetadataFinishEditing();
			}	else if(source == questionEditCtrl) {
				doQuestionMetadataFinishEditing();
			} else if(source == lifecycleEditCtrl) {
				doLifecycleMetadataFinishEditing();
			} else if(source == technicalEditCtrl) {
				doTechnicalMetadataFinishEditing();
			} else if(source == rightsEditCtrl) {
				doRightsMetadataFinishEditing();
			}
			reloadData(editEvent.getItem());
			fireEvent(ureq, editEvent);
		} else if(event == Event.CANCELLED_EVENT) {
			if (source == generalEditCtrl) {
				doGeneralMetadataFinishEditing();
			} else if (source == educationalEditCtrl) {
				doEducationalMetadataFinishEditing();
			} else if(source == questionEditCtrl) {
				doQuestionMetadataFinishEditing();
			} else if(source == lifecycleEditCtrl) {
				doLifecycleMetadataFinishEditing();
			} else if(source == technicalEditCtrl) {
				doTechnicalMetadataFinishEditing();
			} else if(source == rightsEditCtrl) {
				doRightsMetadataFinishEditing();
			}
		}
	}
	
	private void doEditGeneralMetadata(UserRequest ureq) {
		generalEditCtrl = new GeneralMetadataEditController(ureq, getWindowControl(), item);
		listenTo(generalEditCtrl);
		mainVC.put("details_general", generalEditCtrl.getInitialComponent());
	}
	
	private void doGeneralMetadataFinishEditing() {
		removeAsListenerAndDispose(generalEditCtrl);
		generalEditCtrl = null;
		mainVC.put("details_general", generalCtrl.getInitialComponent());
	}
	
	private void doEditEducationalMetadata(UserRequest ureq) {
		removeAsListenerAndDispose(educationalEditCtrl);
		educationalEditCtrl= new EducationalMetadataEditController(ureq, getWindowControl(), item);
		listenTo(educationalEditCtrl);
		mainVC.put("details_educational", educationalEditCtrl.getInitialComponent());
	}
	
	private void doEducationalMetadataFinishEditing() {
		removeAsListenerAndDispose(educationalEditCtrl);
		educationalEditCtrl = null;
		mainVC.put("details_educational", educationalCtrl.getInitialComponent());
	}
	
	private void doEditQuestionMetadata(UserRequest ureq) {
		removeAsListenerAndDispose(questionEditCtrl);
		questionEditCtrl= new QuestionMetadataEditController(ureq, getWindowControl(), item);
		listenTo(questionEditCtrl);
		mainVC.put("details_question", questionEditCtrl.getInitialComponent());
	}
	
	private void doQuestionMetadataFinishEditing() {
		removeAsListenerAndDispose(questionEditCtrl);
		questionEditCtrl = null;
		mainVC.put("details_question", questionCtrl.getInitialComponent());
	}
	
	private void doEditLifecycleMetadata(UserRequest ureq) {
		lifecycleEditCtrl= new LifecycleMetadataEditController(ureq, getWindowControl(), item);
		listenTo(lifecycleEditCtrl);
		mainVC.put("details_lifecycle", lifecycleEditCtrl.getInitialComponent());
	}
	
	private void doLifecycleMetadataFinishEditing() {
		removeAsListenerAndDispose(lifecycleEditCtrl);
		lifecycleEditCtrl = null;
		mainVC.put("details_lifecycle", lifecycleCtrl.getInitialComponent());
	}
	
	private void doEditTechnicalMetadata(UserRequest ureq) {
		technicalEditCtrl= new TechnicalMetadataEditController(ureq, getWindowControl(), item);
		listenTo(technicalEditCtrl);
		mainVC.put("details_technical", technicalEditCtrl.getInitialComponent());
	}
	
	private void doTechnicalMetadataFinishEditing() {
		removeAsListenerAndDispose(technicalEditCtrl);
		technicalEditCtrl = null;
		mainVC.put("details_technical", technicalCtrl.getInitialComponent());
	}
	
	private void doEditRightsMetadata(UserRequest ureq) {
		rightsEditCtrl= new RightsMetadataEditController(ureq, getWindowControl(), item);
		listenTo(rightsEditCtrl);
		mainVC.put("details_rights", rightsEditCtrl.getInitialComponent());
	}
	
	private void doRightsMetadataFinishEditing() {
		removeAsListenerAndDispose(rightsEditCtrl);
		rightsEditCtrl = null;
		mainVC.put("details_rights", rightsCtrl.getInitialComponent());
	}
	
	public void updateShares() {
		sharingCtrl.setItem(getItem());
	}
	
	public QuestionItem updateVersionNumber() {
		if(item instanceof QuestionItemImpl && StringHelper.containsNonWhitespace(item.getItemVersion())) {
			String version = item.getItemVersion();
			int lastPoint = version.lastIndexOf('.');
			if(lastPoint > 1 && lastPoint < version.length() - 1) {
				String prefix = version.substring(0, lastPoint);
				String suffix = version.substring(lastPoint + 1);
				if(StringHelper.isLong(suffix)) {
					long lastDigit = Long.parseLong(suffix);
					String newVersion = prefix + "." + (++lastDigit);
					QuestionItemImpl itemImpl = (QuestionItemImpl)item;
					itemImpl.setItemVersion(newVersion);
					QuestionItem mergedItem = qpoolService.updateItem(item);
					reloadData(mergedItem);
				}	
			}
		}
		return item;
	}
	
	private void reloadData(QuestionItem reloadedItem) {
		this.item = reloadedItem;
		generalCtrl.setItem(reloadedItem);
		educationalCtrl.setItem(reloadedItem);
		questionCtrl.setItem(reloadedItem);
		lifecycleCtrl.setItem(reloadedItem);
		rightsCtrl.setItem(reloadedItem);
		technicalCtrl.setItem(reloadedItem);
	}
}