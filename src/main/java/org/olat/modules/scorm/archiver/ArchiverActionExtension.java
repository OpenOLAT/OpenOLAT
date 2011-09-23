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
* software distributed under the License is distributed on an "AS IS" BASIS,
* <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 2009 frentix GmbH, Switzerland<br>
* <p>
*/
package org.olat.modules.scorm.archiver;

import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.ICourse;

public class ArchiverActionExtension extends GenericActionExtension {

	@Override
	public Controller createController(UserRequest ureq, WindowControl wControl, Object arg) {
		if(arg instanceof ICourse) {
			return new ScormResultArchiveController(ureq, wControl, (ICourse)arg);
		}
		return super.createController(ureq, wControl, arg);
	}
}
