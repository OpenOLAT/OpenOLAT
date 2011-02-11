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
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.core.commons.modules.bc.meta;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.modules.bc.FileSelection;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.UserSession;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.OlatRelPathImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;

/**
 * 
 * Description:<br>
 * Helper class to check to lock flag in MetaInfo against the roles of a user
 * 
 * <P>
 * Initial Date: 23 sept. 2009 <br>
 * 
 * @author srosse
 */
public class MetaInfoHelper {

	/**
	 * Check if the file is locked for this user
	 * 
	 * @param item
	 * @param ureq
	 * @return
	 */
	public static boolean isLocked(VFSItem item, UserRequest ureq) {
		boolean isAdm = ureq.getUserSession().getRoles().isOLATAdmin();
		return isLocked(item, ureq.getIdentity(), isAdm);
	}

	/**
	 * Check if the file is locked for this user
	 * 
	 * @param item
	 * @param ureq
	 * @return
	 */
	public static boolean isLocked(VFSItem item, UserSession usess) {
		boolean isAdm = usess.getRoles().isOLATAdmin();
		return isLocked(item, usess.getIdentity(), isAdm);
	}

	/**
	 * Check if the file is locked for this user
	 * 
	 * @param item
	 * @param ureq
	 * @return
	 */
	public static boolean isLocked(VFSItem item, IdentityEnvironment identityEnv) {
		boolean isAdm = identityEnv.getRoles().isOLATAdmin();
		return isLocked(item, identityEnv.getIdentity(), isAdm);
	}
	
	public static boolean isLocked(MetaInfo info, IdentityEnvironment identityEnv) {
		boolean isAdm = identityEnv.getRoles().isOLATAdmin();
		return isLocked(info, identityEnv.getIdentity(), isAdm);
	}

	/**
	 * Check if the file is locked for this user
	 * 
	 * @param item
	 * @param identity
	 * @param isAdmin
	 * @return
	 */
	public static boolean isLocked(VFSItem item, Identity identity, boolean isAdmin) {
		MetaInfo info = null;
		if (item instanceof OlatRelPathImpl) {
			info = MetaInfoFactory.createMetaInfoFor((OlatRelPathImpl)item);
		}
		else if (item instanceof MetaTagged) {
			info = ((MetaTagged) item).getMetaInfo();
		}
		return isLocked(info, identity, isAdmin);
	}
		
	public static boolean isLocked(MetaInfo info, Identity identity, boolean isAdmin) {
		if(info == null || !info.isLocked()) {
			return false;
		}

		Long lockedBy = info.getLockedBy();
		if (lockedBy == null) { return false; }

		if (identity != null) {
			if (isAdmin) { return false; }
			if (lockedBy.equals(identity.getKey())) { return false; }
		}
		return true;
	}

	/**
	 * Check if there are locked files in the list for this user
	 * 
	 * @param container
	 * @param selection
	 * @param ureq
	 * @return
	 */
	public static List<String> hasLockedFiles(VFSContainer container, FileSelection selection, UserRequest ureq) {
		List<String> lockedFiles = new ArrayList<String>();

		for (String file : selection.getFiles()) {
			VFSItem item = container.resolve(file);
			if (isLocked(item, ureq)) {
				lockedFiles.add(file);
			}
		}
		return lockedFiles;
	}

	public static String renderLockedMessageAsHtml(Translator trans, VFSContainer container, List<String> files) {
		StringBuilder sb = new StringBuilder();
		sb.append(trans.translate("lock.description")).append("<p>").append(renderAsHtml(container, files)).append("</p>");
		return sb.toString();
	}

	/**
	 * Render pathset as HTML.
	 * 
	 * @return HTML Fragment.
	 */
	public static String renderAsHtml(VFSContainer container, List<String> files) {
		StringBuilder sb = new StringBuilder();
		sb.append("<ul>");
		for (String file : files) {
			sb.append("<li>").append(file).append("</li>");
		}
		sb.append("</ul>");
		return sb.toString();
	}
	
	/**
	 * Add a filter for some special cases: sharedfolders...
	 * @param item
	 * @return
	 */
	public static boolean canMetaInfo(VFSItem item) {
		if (item instanceof NamedContainerImpl) {
			item = ((NamedContainerImpl)item).getDelegate();
		}
		if(item instanceof VFSContainer) {
			String name = item.getName();
			if(name.equals("_sharedfolder_") || name.equals("_courseelementdata")) {
				return false;
			}			
		}
		return true;
	}
}