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
import java.net.URI;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.ui.editor.AssessmentItemPreviewSolutionController;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
/**
 * 
 * Initial date: 21.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21PreviewController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private AssessmentItemPreviewSolutionController previewCtrl;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private QPoolService qpoolService;

	public QTI21PreviewController(UserRequest ureq, WindowControl wControl, QuestionItem qitem) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("qti_preview");
		
		File file = qpoolService.getRootFile(qitem);
		if(file == null) {
			//no data to preview
		} else {
			File resourceDirectory = qpoolService.getRootDirectory(qitem);
			URI assessmentItemUri = file.toURI();
			File itemFile = qpoolService.getRootFile(qitem);
			
			ResolvedAssessmentItem resolvedAssessmentItem = qtiService
					.loadAndResolveAssessmentItem(assessmentItemUri, resourceDirectory);
			previewCtrl = new AssessmentItemPreviewSolutionController(ureq, wControl, resolvedAssessmentItem, resourceDirectory, itemFile);
			listenTo(previewCtrl);
			mainVC.put("preview", previewCtrl.getInitialComponent());
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
