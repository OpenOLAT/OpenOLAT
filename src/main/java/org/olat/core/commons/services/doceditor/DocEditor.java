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
package org.olat.core.commons.services.doceditor;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 13 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface DocEditor {
	
	public enum Mode {EDIT, VIEW};
	
	boolean isEnable();
	
	String getType();
	
	String getDisplayName(Locale locale);
	
	/**
	 * Indicates whether the editor supports the format with that suffix in the appropriate mode.
	 *
	 * @param suffix
	 * @param mode
	 * @param hasMeta 
	 * @return
	 */
	boolean isSupportingFormat(String suffix, Mode mode, boolean hasMeta);
	
	/**
	 * Checks whether a file is locked for this identity and editor.
	 * 
	 * @param vfsLeaf
	 * @param identity
	 * @param mode
	 * @return true if the file is locked and therefore the identity is not able to edit the vfsLeaf with this editor.
	 */
	boolean isLockedForMe(VFSLeaf vfsLeaf, Identity identity, Mode mode);
	
	Controller getRunController(UserRequest ureq, WindowControl wControl, Identity identity, VFSLeaf vfsLeaf,
			DocEditorSecurityCallback securityCallback, DocEditorConfigs configs);


}
