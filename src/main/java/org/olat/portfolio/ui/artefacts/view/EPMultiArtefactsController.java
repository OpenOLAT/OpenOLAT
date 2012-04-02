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
package org.olat.portfolio.ui.artefacts.view;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.portfolio.model.artefacts.AbstractArtefact;

/**
 * Description:<br>
 * interface for controllers which are able to show multiple artefacts and allow to reset their content
 * the setter is needed to speed up creation of all child controllers, as repaint some artefacts is faster than disposing all and display them again!
 * 
 * <P>
 * Initial Date:  16.11.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public interface EPMultiArtefactsController extends Controller, Activateable2 {

	/**
	 * signal the controller to refresh its childs or artefact-representations
	 * @param ureq
	 * @param artefacts
	 */
	public void setNewArtefactsList(UserRequest ureq, List<AbstractArtefact> artefacts);
	
}
