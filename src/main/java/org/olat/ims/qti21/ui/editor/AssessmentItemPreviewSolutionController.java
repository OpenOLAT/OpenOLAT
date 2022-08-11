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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Persistable;
import org.olat.core.util.Util;
import org.olat.ims.qti21.AssessmentSessionAuditLogger;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.audit.DefaultAssessmentSessionAuditLogger;
import org.olat.ims.qti21.ui.AssessmentItemDisplayController;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;

/**
 * 
 * The controller show the assessment item in solution mode.
 * 
 * Initial date: 13 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemPreviewSolutionController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private AssessmentItemDisplayController displayCtrl;
	
	private final AssessmentSessionAuditLogger candidateAuditLogger = new DefaultAssessmentSessionAuditLogger();
	
	@Autowired
	private QTI21Service qtiService;
	
	public AssessmentItemPreviewSolutionController(UserRequest ureq, WindowControl wControl,
			ResolvedAssessmentItem resolvedAssessmentItem, File rootDirectory, File itemFile) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentItemDisplayController.class, ureq.getLocale()));
		
		displayCtrl = new AssessmentItemDisplayController(ureq, getWindowControl(),
				resolvedAssessmentItem, rootDirectory, itemFile,
				QTI21DeliveryOptions.defaultSettings(), candidateAuditLogger);
		if(!displayCtrl.isExploded()) {
			displayCtrl.requestSolution(ureq);
		}
		mainVC = createVelocityContainer("assessment_item_preview_solution");
		mainVC.put("display", displayCtrl.getInitialComponent());
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		mainVC.removeListener(this);
		if(displayCtrl != null && displayCtrl.getCandidateSession() instanceof Persistable) {
			qtiService.deleteAssessmentTestSession(displayCtrl.getCandidateSession());
		}
        super.doDispose();
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
}
