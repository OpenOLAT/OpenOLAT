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
 * Copyright (c) since 2004 at frentix GmbH, http://www.frentix.com
 * <p>
 */

package org.olat.util.themes;

import java.io.File;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.core.id.Identity;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.servlets.WebDAVProvider;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.filters.VFSItemExcludePrefixFilter;
/**
 * 
 */
public class ThemesWebDAVProvider implements WebDAVProvider {

	private static final String MOUNTPOINT = "themes";
	
	public String getMountPoint() { return MOUNTPOINT; }

	/**
	 * @see org.olat.commons.servlets.util.WebDAVProvider#getContainer(org.olat.core.id.Identity)
	 */
	public VFSContainer getContainer(Identity identity) {
		BaseSecurity secMgr = BaseSecurityManager.getInstance();
		//FIXME: RH: check if it really should return something => why an empty container?
		if (!secMgr.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_ADMIN)){
			return new MergeSource(null, null);
		}		

		// mount /static/themes, filter out CVS!
		String staticAbsPath = WebappHelper.getContextRoot() + "/static/themes";
		File themesFile = new File(staticAbsPath);
		LocalFolderImpl vfsThemes = new LocalFolderImpl(themesFile);
		vfsThemes.setDefaultItemFilter(new VFSItemExcludePrefixFilter(new String[]{"CVS","cvs"} ));
		VFSContainer vfsCont = vfsThemes;
		return vfsCont;
	}


}
