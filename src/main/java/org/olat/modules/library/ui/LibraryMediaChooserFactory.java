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
package org.olat.modules.library.ui;

import org.olat.core.commons.controllers.linkchooser.CustomMediaChooserController;
import org.olat.core.commons.controllers.linkchooser.CustomMediaChooserFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.library.LibraryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 04.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("org.olat.core.commons.controllers.linkchooser.CustomMediaChooserFactory")
public class LibraryMediaChooserFactory implements CustomMediaChooserFactory {
	
	@Autowired
	private LibraryManager libraryManager;

	@Override
	public CustomMediaChooserController getInstance(UserRequest ureq, WindowControl wControl) {
		VFSContainer libraryDir = libraryManager.getSharedFolder();
		if(libraryDir != null) {
			return new LibraryMediaChooserController(ureq, wControl, libraryDir);
		}
		return null;
	}
	
	@Override
	public Mapper getMapperInstance() {
		return new LibraryMapper(libraryManager);
	}
}
