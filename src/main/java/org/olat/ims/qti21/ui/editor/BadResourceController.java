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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.ims.qti21.model.xml.BadRessourceHelper;

import uk.ac.ed.ph.jqtiplus.provision.BadResourceException;

/**
 * 
 * Initial date: 10.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BadResourceController extends FormBasicController {
	
	private final URI resourceURI;
	private final File unzippedDirectory;
	private final BadResourceException resourceException;
	
	public BadResourceController(UserRequest ureq, WindowControl wControl,
			BadResourceException resourceException, File unzippedDirectory, URI resourceURI) {
		super(ureq, wControl, "bad_resource");
		this.resourceException = resourceException;
		this.resourceURI = resourceURI;
		this.unzippedDirectory = unzippedDirectory;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;

			StringBuilder out = new StringBuilder();
			if(resourceException != null) {
				BadRessourceHelper.extractMessage(resourceException, out);
				layoutCont.contextPut("message", out.toString());
			}
			layoutCont.contextPut("directory", unzippedDirectory.toString());
			layoutCont.contextPut("uri", resourceURI.toASCIIString());
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
