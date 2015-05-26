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
import org.olat.ims.qti21.ui.AssessmentItemDisplayController;
import org.olat.repository.RepositoryEntry;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;

/**
 * 
 * Initial date: 22.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemEditorController extends BasicController {
	
	private final ResolvedAssessmentItem resolvedAssessmentItem;
	private final VelocityContainer mainVC;
	
	private AssessmentItemDisplayController displayCtrl;
	
	public AssessmentItemEditorController(UserRequest ureq, WindowControl wControl, RepositoryEntry testEntry,
			ResolvedAssessmentItem resolvedAssessmentItem, AssessmentItemRef itemRef, File unzippedDirectory) {
		super(ureq, wControl);
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		
		mainVC = createVelocityContainer("assessment_item_editor");
		
		displayCtrl = new AssessmentItemDisplayController(ureq, getWindowControl(), testEntry, resolvedAssessmentItem, itemRef, unzippedDirectory);
		listenTo(displayCtrl);
		mainVC.put("display", displayCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
		
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	

}