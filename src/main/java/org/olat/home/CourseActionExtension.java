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
package org.olat.home;

import org.olat.catalog.CatalogModule;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Description:<br>
 * ActionExtension for the "my courses"
 * 
 * <P>
 * Initial Date:  18 jan. 2013 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseActionExtension extends GenericActionExtension {
	
	private final CatalogModule catModule;
	
	public CourseActionExtension(CatalogModule catModule) {
		this.catModule = catModule;
	}
	

	@Override
	public Controller createController(UserRequest ureq, WindowControl wControl, Object arg) {
		return new CourseMainController(ureq, wControl);
	}

	@Override
	public boolean isEnabled() {
		return catModule.isMyCoursesEnabled();
	}
}
