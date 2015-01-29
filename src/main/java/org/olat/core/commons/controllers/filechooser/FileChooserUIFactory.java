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

package org.olat.core.commons.controllers.filechooser;

import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSContainerFilter;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
 * <h3>Description:</h3>
 * UI Factory to handle the file chooser package
 * <p>
 * Initial Date: 13.06.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class FileChooserUIFactory {
	private static final VFSItemFilter containerFilter = new VFSContainerFilter();

	/**
	 * Factory method to create a file chooser workflow controller that allows the
	 * usage of a custom vfs item filter. The tree will display a title
	 * and a description above the tree.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param rootContainer The root container that should be selected from
	 * @param customItemFilter The custom filter to be used or NULL to not use any
	 *          filter at all
	 * @param onlyLeafsSelectable true: container elements can't be selected;
	 *          false: all items can be selected
	 */
	public static FileChooserController createFileChooserController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer, VFSItemFilter customItemFilter, boolean onlyLeafsSelectable) {
		return new FileChooserController(ureq, wControl, rootContainer, customItemFilter, onlyLeafsSelectable);
	}

	/**
	 * Factory method to create a file chooser workflow controller that allows the
	 * usage of a custom vfs item filter. The tree will not have a title,
	 * just the tree
	 * 
	 * @param ureq
	 * @param wControl
	 * @param rootContainer The root container that should be selected from
	 * @param customItemFilter The custom filter to be used or NULL to not use any
	 *          filter at all
	 * @param onlyLeafsSelectable true: container elements can't be selected;
	 *          false: all items can be selected
	 */
	public static FileChooserController createFileChooserControllerWithoutTitle(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer, VFSItemFilter customItemFilter, boolean onlyLeafsSelectable) {
		return new FileChooserController(ureq, wControl, rootContainer, customItemFilter, onlyLeafsSelectable);
	}

	/**
	 * Factory method to create a file chooser workflow controller allows
	 * filtering of files by setting a boolean. The tree will display a title
	 * and a description above the tree.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param rootContainer
	 *            The root container that should be selected from
	 * @param showLeafs
	 *            true: show directories and files; false: show only directories
	 * @param onlyLeafsSelectable
	 *            true: container elements can't be selected; false: all items
	 *            can be selected
	 */
	public static FileChooserController createFileChooserController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer, boolean showLeafs, boolean onlyLeafsSelectable) {
		return new FileChooserController(ureq, wControl, rootContainer, (showLeafs ? null : containerFilter), onlyLeafsSelectable);
	}

	/**
	 * Factory method to create a file chooser workflow controller allows
	 * filtering of files by setting a boolean. The tree will not have a title,
	 * just the tree
	 * 
	 * @param ureq
	 * @param wControl
	 * @param rootContainer
	 *            The root container that should be selected from
	 * @param showLeafs
	 *            true: show directories and files; false: show only directories
	 * @param onlyLeafsSelectable
	 *            true: container elements can't be selected; false: all items
	 *            can be selected
	 */
	public static FileChooserController createFileChooserControllerWithoutTitle(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer, boolean showLeafs, boolean onlyLeafsSelectable) {
		return new FileChooserController(ureq, wControl, rootContainer, (showLeafs ? null : containerFilter), onlyLeafsSelectable);
	}

	/**
	 * Get the vfs item that was selected by the user
	 * 
	 * @param event
	 *            The file choosen event
	 * 
	 * @return
	 */
	public static VFSItem getSelectedItem(FileChoosenEvent event) {
		return event.getSelectedItem();
	}


	/**
	 * Get the path as string of the selected item relative to the root
	 * container and the relative base path
	 * 
	 * @param event The file choosen event
	 * @param rootContainer
	 *            The root container for which the relative path should be
	 *            calculated
	 * @param relativeBasePath
	 *            when NULL, the path will be calculated relative to the
	 *            rootContainer; when NULL, the relativeBasePath must
	 *            represent a relative path within the root container that
	 *            serves as the base. In this case, the calculated relative item
	 *            path will start from this relativeBasePath
	 * @return 
	 */
	public static String getSelectedRelativeItemPath(FileChoosenEvent event, VFSContainer rootContainer, String relativeBasePath) {
		// 1) Create path absolute to the root container
		VFSItem selectedItem = event.getSelectedItem();
		return VFSManager.getRelativeItemPath(selectedItem, rootContainer, relativeBasePath);
	}
	
	/**
	 * Get the path as string of the selected item relative to the root
	 * container and the relative base path
	 * 
	 * @param event The folder event
	 * @param rootContainer
	 *            The root container for which the relative path should be
	 *            calculated
	 * @param relativeBasePath
	 *            when NULL, the path will be calculated relative to the
	 *            rootContainer; when NULL, the relativeBasePath must
	 *            represent a relative path within the root container that
	 *            serves as the base. In this case, the calculated relative item
	 *            path will start from this relativeBasePath
	 * @return 
	 */
	public static String getSelectedRelativeItemPath(FolderEvent event, VFSContainer rootContainer, String relativeBasePath) {
		// 1) Create path absolute to the root container
		VFSItem selectedItem = event.getItem();
		return VFSManager.getRelativeItemPath(selectedItem, rootContainer, relativeBasePath);
	}
}
