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
package org.olat.core.commons.modules.bc.commands;

import java.util.List;

import org.olat.core.commons.modules.bc.FileSelection;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  29 sept. 2009 <br>
 *
 * @author srosse
 */
public class FolderCommandHelper {
	
	public static String renderLockedMessageAsHtml(Translator trans, List<String> files) {
		StringBuilder sb = new StringBuilder();
		sb.append(trans.translate("lock.description")).append("<p>").append(renderAsHtml(files)).append("</p>");
		return sb.toString();
	}

	/**
	 * Render pathset as HTML.
	 * 
	 * @return HTML Fragment.
	 */
	private static String renderAsHtml(List<String> files) {
		StringBuilder sb = new StringBuilder();
		sb.append("<ul>");
		for (String file : files) {
			sb.append("<li>").append(file).append("</li>");
		}
		sb.append("</ul>");
		return sb.toString();
	}
	
	/**
	 * The moduleURI from the UserRequest has been decoded (URL decoded). Which means that
	 * encoded character as + came as %B2, decoded from Tomcat as + and by OpenOLAT after
	 * as blank.
	 * @param ureq
	 * @param folderComponent
	 * @return
	 */
	public static VFSItem tryDoubleDecoding(UserRequest ureq, FolderComponent folderComponent) {
		VFSItem vfsfile = null;
		
		//double decoding of ++
		String requestUri = ureq.getHttpReq().getRequestURI();
		String uriPrefix = ureq.getUriPrefix();
		if(uriPrefix.length() < requestUri.length()) {
			requestUri = requestUri.substring(uriPrefix.length());
			int nextPath = requestUri.indexOf('/');
			if(nextPath > 0 && nextPath < requestUri.length()) {
				String path = requestUri.substring(nextPath + 1, requestUri.length());
				vfsfile = folderComponent.getRootContainer().resolve(path);
			}
		}
		
		return vfsfile;
	}
	
	/**
	 * Check if the FolderComponent is ok
	 * @param wControl
	 * @param fc
	 * @return
	 */
	public static final int sanityCheck(WindowControl wControl, FolderComponent fc) {
		if(fc.getCurrentContainer() == null || !fc.getCurrentContainer().exists()) {
			wControl.setError(fc.getTranslator().translate("FileDoesNotExist"));
			return FolderCommandStatus.STATUS_FAILED;
		}
		return FolderCommandStatus.STATUS_SUCCESS;
	}
		
	/**
	 * Check if an item exists
	 * @param wControl
	 * @param fc
	 * @param ureq
	 * @param currentItem
	 * @return
	 */
	public static final int sanityCheck2(WindowControl wControl, FolderComponent fc, VFSItem currentItem) {
		if(currentItem == null || !currentItem.exists()) {
			wControl.setError(fc.getTranslator().translate("FileDoesNotExist"));
			return FolderCommandStatus.STATUS_FAILED;
		}
		return FolderCommandStatus.STATUS_SUCCESS;		
	}
	
	/**
	 * Checks whether the given VFSItem is valid for online-editing.<br />
	 * Checks whether the file is a VFSLeaf see: OO-57
	 * 
	 * notice: htmlEditor and plaintext editor check for file-size and show
	 * appropriate message to user
	 * 
	 * @param currentItem
	 *            the VFSItem to check
	 * @return Returns STATUS_FAILED if item is a directory. Returns
	 *         STATUS_SUCCESS if vfsItem is a leaf
	 */
	public static final int fileEditSanityCheck(VFSItem currentItem) {
		if ((currentItem instanceof VFSLeaf)) {
			return FolderCommandStatus.STATUS_SUCCESS;
		} else {
			return FolderCommandStatus.STATUS_FAILED;
		}
	}
	
	/**
	 * Check if a FileSelection exist
	 * @param wControl
	 * @param fc
	 * @param selection
	 * @return
	 */
	public static final int sanityCheck3(WindowControl wControl, FolderComponent fc, FileSelection selection) {
		VFSContainer container = fc.getCurrentContainer();
		for(String filename : selection.getFiles()) {
			if(container.resolve(filename) == null) {
				wControl.setError(fc.getTranslator().translate("FileDoesNotExist"));
				return FolderCommandStatus.STATUS_FAILED;
			}
		}
		return FolderCommandStatus.STATUS_SUCCESS;	
	}
}
