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
package org.olat.home.controllerCreators;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.id.IdentityEnvironment;
import org.olat.user.PersonalFolderManager;

/**
 * 
 * <h3>Description:</h3>
 * Wrapper to create the personal folder in home
 * <p>
 * Initial Date:  29 nov. 2010 <br>
 * @author srosse, srosse@frentix.com, www.frentix.com
 */
public class PersonalFolderControllerCreator extends AutoCreator  {

	@Override
	public String getClassName() {
		return this.getClass().getCanonicalName();
	}

	public PersonalFolderControllerCreator() {
		super();
	}	

	@Override
	public Controller createController(UserRequest ureq, WindowControl lwControl) {
		IdentityEnvironment identityEnv = ureq.getUserSession().getIdentityEnvironment();
		
		return new FolderRunController(CoreSpringFactory.getImpl(PersonalFolderManager.class).getContainer(identityEnv), true, true, true, ureq, lwControl);
	}
}
