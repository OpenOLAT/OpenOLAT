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

package org.olat.core.commons.modules.bc.vfs;

import java.io.File;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.MetaInfoFactory;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.OlatRelPathImpl;
import org.olat.core.util.vfs.VFSContainer;

public class OlatRootFileImpl extends LocalFileImpl implements OlatRelPathImpl, MetaTagged {

	private String fileRelPath;
	
	/**
	 * @param Path to the file, relative to <code>bcroot</code>
	 * @param parentContainer Optional VFS parent container
	 */
	public OlatRootFileImpl(String fileRelPath, VFSContainer parentContainer) {
		super(new File(FolderConfig.getCanonicalRoot() + fileRelPath), parentContainer);
		this.fileRelPath = fileRelPath;
	}

	@Override
	public String getRelPath() {
		return fileRelPath;
	}

	@Override
	public MetaInfo getMetaInfo() {
		return CoreSpringFactory.getImpl(MetaInfoFactory.class).createMetaInfoFor(getBasefile());
	}
	
	@Override
	public String toString() {
		MetaInfo meta = getMetaInfo();
		if (meta != null && StringHelper.containsNonWhitespace(meta.getTitle())) {
			return meta.getTitle();
		} else {
			return getName();
		}
	}
}
