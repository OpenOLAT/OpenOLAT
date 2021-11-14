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

import org.olat.core.commons.controllers.filechooser.FileChoosenEvent;
import org.olat.core.commons.controllers.filechooser.FileChooserController;
import org.olat.core.commons.controllers.filechooser.FileChooserUIFactory;
import org.olat.core.commons.controllers.linkchooser.CustomMediaChooserController;
import org.olat.core.commons.controllers.linkchooser.URLChoosenEvent;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;

/**
 * 
 * <h3>Description:</h3>
 * A custom medi chooser for the library
 * <p>
 * Initial Date:  2 déc. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class LibraryMediaChooserController extends CustomMediaChooserController {
	
	private Translator translator;
	private FileChooserController fileChooserController;
	
	public LibraryMediaChooserController() {
		super(null);//make Spring happy
	}
	
	protected LibraryMediaChooserController(UserRequest ureq, WindowControl wControl, VFSContainer libraryDir) {
		super(wControl);
		
		translator = Util.createPackageTranslator(LibraryMediaChooserController.class, ureq.getLocale());
		String libraryName = translator.translate("library.title");
		VFSContainer libraryNamedDir = new NamedContainerImpl(libraryName, libraryDir);

		VFSItemFilter dirFilter = new VFSSystemItemFilter();
		fileChooserController = FileChooserUIFactory.createFileChooserController(ureq, getWindowControl(),
				libraryNamedDir, dirFilter, true);
		fileChooserController.addControllerListener(this);
		
		setInitialComponent(fileChooserController.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		fileChooserController.dispose();
        super.doDispose();
	}

	@Override
	public String getTabbedPaneTitle() {
		return translator.translate("library.title");
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == fileChooserController) {
			if (event instanceof FileChoosenEvent) {
				VFSItem item = FileChooserUIFactory.getSelectedItem((FileChoosenEvent)event);
				if(item instanceof VFSLeaf && item.canMeta() == VFSConstants.YES) {
					VFSMetadata info = item.getMetaInfo();
					if(info != null) {
						String url = Settings.getServerContextPathURI() + "/library/" + info.getUuid() + "/" + item.getName();
						String displayName = info.getTitle();
						if(!StringHelper.containsNonWhitespace(displayName)) {
							displayName = item.getName();
						}
						String iconCssClass = info.getIconCssClass();
						fireEvent(ureq, new URLChoosenEvent(url, displayName, "_blank", iconCssClass, -1 , -1));
					} else {
						fireEvent(ureq, Event.CANCELLED_EVENT);
					}
				} else {
					fireEvent(ureq, Event.CANCELLED_EVENT);
				}
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			} else if (event == Event.FAILED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
		super.event(ureq, source, event);
	}
}