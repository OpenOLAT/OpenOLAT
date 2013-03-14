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
package org.olat.modules.qpool.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.ui.edit.EducationalMetadataController;
import org.olat.modules.qpool.ui.edit.EducationalMetadataEditController;
import org.olat.modules.qpool.ui.edit.GeneralMetadataController;
import org.olat.modules.qpool.ui.edit.GeneralMetadataEditController;
import org.olat.modules.qpool.ui.edit.LifecycleMetadataController;
import org.olat.modules.qpool.ui.edit.LifecycleMetadataEditController;
import org.olat.modules.qpool.ui.edit.QItemEdited;
import org.olat.modules.qpool.ui.edit.QuestionMetadataController;
import org.olat.modules.qpool.ui.edit.QuestionMetadataEditController;
import org.olat.modules.qpool.ui.edit.RightsMetadataController;
import org.olat.modules.qpool.ui.edit.RightsMetadataEditController;
import org.olat.modules.qpool.ui.edit.TechnicalMetadataController;
import org.olat.modules.qpool.ui.edit.TechnicalMetadataEditController;

/**
 * 
 * Initial date: 24.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MetadatasController extends BasicController {

	private final VelocityContainer mainVC;
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
	
	public MetadatasController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		super(ureq, wControl);
		this.item = item;

		generalCtrl = new GeneralMetadataController(ureq, wControl, item);
		listenTo(generalCtrl);
		educationalCtrl = new EducationalMetadataController(ureq, wControl, item);
		listenTo(educationalCtrl);
		questionCtrl = new QuestionMetadataController(ureq, wControl, item);
		listenTo(questionCtrl);
		lifecycleCtrl = new LifecycleMetadataController(ureq, wControl, item);
		listenTo(lifecycleCtrl);
		technicalCtrl = new TechnicalMetadataController(ureq, wControl, item);
		listenTo(technicalCtrl);
		rightsCtrl = new RightsMetadataController(ureq, wControl, item);
		listenTo(rightsCtrl);

		mainVC = createVelocityContainer("item_metadatas");
		mainVC.put("details_general", generalCtrl.getInitialComponent());
		mainVC.put("details_educational", educationalCtrl.getInitialComponent());
		mainVC.put("details_question", questionCtrl.getInitialComponent());
		mainVC.put("details_lifecycle", lifecycleCtrl.getInitialComponent());
		mainVC.put("details_technical", technicalCtrl.getInitialComponent());
		mainVC.put("details_rights", rightsCtrl.getInitialComponent());
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
			if(source == generalCtrl) {
				doEditGeneralMetadata(ureq);
			} else if(source == educationalCtrl) {
				doEditEducationalMetadata(ureq);
			} else if(source == questionCtrl) {
				doEditQuestionMetadata(ureq);
			} else if(source == lifecycleCtrl) {
				doEditLifecycleMetadata(ureq);
			} else if(source == technicalCtrl) {
				doEditTechnicalMetadata(ureq);
			} else if(source == rightsCtrl) {
				doEditRightsMetadata(ureq);
			}
		} else if(event instanceof QItemEdited) {
			QItemEdited editEvent = (QItemEdited)event;
			if (source == generalEditCtrl) {
				doGeneralMetadataFinishEditing(ureq);
			} else if (source == educationalEditCtrl) {
				doEducationalMetadataFinishEditing(ureq);
			}	else if(source == questionEditCtrl) {
				doQuestionMetadataFinishEditing(ureq);
			} else if(source == lifecycleEditCtrl) {
				doLifecycleMetadataFinishEditing(ureq);
			} else if(source == technicalEditCtrl) {
				doTechnicalMetadataFinishEditing(ureq);
			} else if(source == rightsEditCtrl) {
				doRightsMetadataFinishEditing(ureq);
			}
			reloadData(editEvent.getItem());
		} else if(event == Event.CANCELLED_EVENT) {
			if (source == generalEditCtrl) {
				doGeneralMetadataFinishEditing(ureq);
			} else if (source == educationalEditCtrl) {
				doEducationalMetadataFinishEditing(ureq);
			} else if(source == questionEditCtrl) {
				doQuestionMetadataFinishEditing(ureq);
			} else if(source == lifecycleEditCtrl) {
				doLifecycleMetadataFinishEditing(ureq);
			} else if(source == technicalEditCtrl) {
				doTechnicalMetadataFinishEditing(ureq);
			} else if(source == rightsEditCtrl) {
				doRightsMetadataFinishEditing(ureq);
			}
		}
	}
	
	private void doEditGeneralMetadata(UserRequest ureq) {
		generalEditCtrl = new GeneralMetadataEditController(ureq, getWindowControl(), item);
		listenTo(generalEditCtrl);
		mainVC.put("details_general", generalEditCtrl.getInitialComponent());
	}
	
	private void doGeneralMetadataFinishEditing(UserRequest ureq) {
		removeAsListenerAndDispose(generalEditCtrl);
		generalEditCtrl = null;
		mainVC.put("details_general", generalCtrl.getInitialComponent());
	}
	
	private void doEditEducationalMetadata(UserRequest ureq) {
		educationalEditCtrl= new EducationalMetadataEditController(ureq, getWindowControl(), item);
		listenTo(educationalEditCtrl);
		mainVC.put("details_educational", educationalEditCtrl.getInitialComponent());
	}
	
	private void doEducationalMetadataFinishEditing(UserRequest ureq) {
		removeAsListenerAndDispose(educationalEditCtrl);
		educationalEditCtrl = null;
		mainVC.put("details_educational", educationalCtrl.getInitialComponent());
	}
	
	private void doEditQuestionMetadata(UserRequest ureq) {
		questionEditCtrl= new QuestionMetadataEditController(ureq, getWindowControl(), item);
		listenTo(questionEditCtrl);
		mainVC.put("details_question", questionEditCtrl.getInitialComponent());
	}
	
	private void doQuestionMetadataFinishEditing(UserRequest ureq) {
		removeAsListenerAndDispose(questionEditCtrl);
		questionEditCtrl = null;
		mainVC.put("details_question", questionCtrl.getInitialComponent());
	}
	
	private void doEditLifecycleMetadata(UserRequest ureq) {
		lifecycleEditCtrl= new LifecycleMetadataEditController(ureq, getWindowControl(), item);
		listenTo(lifecycleEditCtrl);
		mainVC.put("details_lifecycle", lifecycleEditCtrl.getInitialComponent());
	}
	
	private void doLifecycleMetadataFinishEditing(UserRequest ureq) {
		removeAsListenerAndDispose(lifecycleEditCtrl);
		lifecycleEditCtrl = null;
		mainVC.put("details_lifecycle", lifecycleCtrl.getInitialComponent());
	}
	
	private void doEditTechnicalMetadata(UserRequest ureq) {
		technicalEditCtrl= new TechnicalMetadataEditController(ureq, getWindowControl(), item);
		listenTo(technicalEditCtrl);
		mainVC.put("details_technical", technicalEditCtrl.getInitialComponent());
	}
	
	private void doTechnicalMetadataFinishEditing(UserRequest ureq) {
		removeAsListenerAndDispose(technicalEditCtrl);
		technicalEditCtrl = null;
		mainVC.put("details_technical", technicalCtrl.getInitialComponent());
	}
	
	private void doEditRightsMetadata(UserRequest ureq) {
		rightsEditCtrl= new RightsMetadataEditController(ureq, getWindowControl(), item);
		listenTo(rightsEditCtrl);
		mainVC.put("details_rights", rightsEditCtrl.getInitialComponent());
	}
	
	private void doRightsMetadataFinishEditing(UserRequest ureq) {
		removeAsListenerAndDispose(rightsEditCtrl);
		rightsEditCtrl = null;
		mainVC.put("details_rights", rightsCtrl.getInitialComponent());
	}
	
	private void reloadData(QuestionItem item) {
		this.item = item;
		generalCtrl.setItem(item);
		educationalCtrl.setItem(item);
		questionCtrl.setItem(item);
		lifecycleCtrl.setItem(item);
		rightsCtrl.setItem(item);
		technicalCtrl.setItem(item);
	}
}