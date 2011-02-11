/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
* <p>
*/
package org.olat.portfolio.model.artefacts;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.portfolio.EPAbstractHandler;
import org.olat.portfolio.ui.artefacts.view.details.TextArtefactDetailsController;

/**
 * Description:<br>
 * Handler for the text-artefact
 * 
 * <P>
 * Initial Date:  01.09.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPTextArtefactHandler extends EPAbstractHandler<EPTextArtefact> {

	@Override
	public String getType() {
		return EPTextArtefact.TEXT_ARTEFACT_TYPE;
	}

	@Override
	public EPTextArtefact createArtefact() {
		EPTextArtefact textArtefact = new EPTextArtefact();
		return textArtefact;
	}

	@Override
	public Controller createDetailsController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, boolean readOnlyMode) {
		return new TextArtefactDetailsController(ureq, wControl, artefact, readOnlyMode);
	}
	
	
}