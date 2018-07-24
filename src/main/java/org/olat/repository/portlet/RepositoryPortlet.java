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

package org.olat.repository.portlet;

import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortlet;
import org.olat.core.gui.control.generic.portal.Portlet;
import org.olat.core.gui.control.generic.portal.PortletToolController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;

/**
 * Description:<br>
 * Displays the list of courses from this user
 * <P>
 * Initial Date: 08.07.2005 <br>
 * 
 * @author gnaegi
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class RepositoryPortlet extends AbstractPortlet {
	private RepositoryPortletRunController runCtr;
	private static final String CONFIG_KEY_ROLE = "role";
	private static final String CONFIG_KEY_ROLE_STUDENT = "student";
	protected static final String CONFIG_KEY_ROLE_TEACHER = "teacher";
	
	@Override
	public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map<String,String> configuration) {
		Translator translator = Util.createPackageTranslator(RepositoryPortlet.class, ureq.getLocale());
		RepositoryPortlet p = new RepositoryPortlet();
		p.setName(getName());
		p.setConfiguration(configuration);
		p.setTranslator(translator);
		p.setDefaultMaxEntries(getDefaultMaxEntries());
		return p;
	}

	@Override
	public String getTitle() {
		if (CONFIG_KEY_ROLE_STUDENT.equals(getConfiguration().get(CONFIG_KEY_ROLE))) {
			return getTranslator().translate("repositoryPortlet.student.title");			
		} else {			
			return getTranslator().translate("repositoryPortlet.teacher.title");			
		}
	}

	@Override
	public String getDescription() {
		if (CONFIG_KEY_ROLE_STUDENT.equals(getConfiguration().get(CONFIG_KEY_ROLE))) {
			return getTranslator().translate("repositoryPortlet.student.description");			
		} else {			
			return getTranslator().translate("repositoryPortlet.teacher.description");			
		}
	}

	@Override
	public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq) {
		if(runCtr != null) runCtr.dispose();
		int maxEntries = getDefaultMaxEntries();
		boolean studentView = CONFIG_KEY_ROLE_STUDENT.equals(getConfiguration().get(CONFIG_KEY_ROLE));
		runCtr = new RepositoryPortletRunController(wControl, ureq, getTranslator(), getName(), maxEntries, studentView);
		return runCtr.getInitialComponent();
	}

	@Override
	public void dispose() {
		disposeRunComponent();
	}

	@Override
	public String getCssClass() {
		if (CONFIG_KEY_ROLE_STUDENT.equals(getConfiguration().get(CONFIG_KEY_ROLE))) {
			return "o_portlet_repository_student";						
		} else {
			return "o_portlet_repository_teacher";			
		}
	}

	@Override
	public void disposeRunComponent() {
		if (this.runCtr != null) {
			this.runCtr.dispose();
			this.runCtr = null;
		}
	}
	
	@Override
	public PortletToolController<RepositoryEntry> getTools(UserRequest ureq, WindowControl wControl) {
		//portlet was not yet visible
		if ( runCtr == null ) {
			boolean studentView = CONFIG_KEY_ROLE_STUDENT.equals(getConfiguration().get(CONFIG_KEY_ROLE));
			runCtr = new RepositoryPortletRunController(wControl, ureq, getTranslator(), getName(), getDefaultMaxEntries(), studentView);
		}
	  return runCtr.createSortingTool(ureq, wControl);
	}
}
