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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.util.servlets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSItem;

public class WebDAVProviderFactory {

	private static final WebDAVProviderFactory INSTANCE = new WebDAVProviderFactory();
	private static Map webdavProviders;
	
	private WebDAVProviderFactory() {
		// singleton
	}
	
	public static final WebDAVProviderFactory getInstance() { return INSTANCE; }
	
	/**
	 * Returns a mountable root containing all entries which will be exposed to the webdav mount.
	 * @return
	 */
	public VFSItem getMountableRoot(Identity identity) {
		MergeSource vfsRoot = new MergeSource(null, "webdavroot");
		for (Iterator iter = webdavProviders.keySet().iterator(); iter.hasNext();) {
			WebDAVProvider provider = (WebDAVProvider)webdavProviders.get(iter.next());
			vfsRoot.addContainer(new NamedContainerImpl(provider.getMountPoint(), provider.getContainer(identity)));
		}
		return vfsRoot;
	}
	
	/**
	 * Set the list of webdav providers.
	 * @param webdavProviders
	 */
	public void setWebdavProviderList(List webdavProviders) {
		if (webdavProviders == null)
			throw new AssertException("null value for webdavProviders not allowed.");
		
		for (Iterator iter = webdavProviders.iterator(); iter.hasNext();) {
			WebDAVProvider provider = (WebDAVProvider) iter.next();
			addWebdavProvider(provider);
		}
	}
	
	/**
	 * Add a new webdav provider.
	 * @param provider
	 */
	public void addWebdavProvider(WebDAVProvider provider) {
		if (webdavProviders == null) webdavProviders = new HashMap();
		if (webdavProviders.containsKey(provider.getMountPoint()))
			throw new AssertException("May not add two providers with the same mount point.");
		webdavProviders.put(provider.getMountPoint(), provider);
		Tracing.logInfo("Adding webdav mountpoint '" + provider.getMountPoint() + "'.", WebDAVProviderFactory.class);
	}
}
