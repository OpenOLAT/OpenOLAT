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
package org.olat.ims.qti;

import org.dom4j.Document;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.ims.qti.process.FilePersister;
import org.olat.ims.qti.render.LocalizedXSLTransformer;

/**
 * 
 * Initial date: 05.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12XSLTResultDetailsController extends BasicController {

	public QTI12XSLTResultDetailsController(UserRequest ureq, WindowControl wControl,
			Identity assessedIdentity, String type, QTIResultSet resultSet) {
		super(ureq, wControl);
		
		VelocityContainer details = createVelocityContainer("qtires_details");
		Document doc = FilePersister.retreiveResultsReporting(assessedIdentity, type, resultSet.getAssessmentID());
		if (doc == null) {
			showInfo("error.resreporting.na");
			return;
		}
		String resultsHTML = LocalizedXSLTransformer.getInstance(ureq.getLocale()).renderResults(doc);
		details.contextPut("reshtml", resultsHTML);
		putInitialPanel(details);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
