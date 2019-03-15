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

package org.olat.core.commons.modules.bc.commands;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.ForbiddenMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;

public class CmdServeThumbnailResource implements FolderCommand {
	
	private int status = FolderCommandStatus.STATUS_SUCCESS;
	
	private final VFSRepositoryService vfsRepositoryservice;
	
	public CmdServeThumbnailResource() {
		vfsRepositoryservice = CoreSpringFactory.getImpl(VFSRepositoryService.class);
	}
	
	@Override
	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator) {
		VFSSecurityCallback inheritedSecCallback = VFSManager.findInheritedSecurityCallback(folderComponent.getCurrentContainer());
		
		MediaResource mr = null;
		if (inheritedSecCallback != null && !inheritedSecCallback.canRead()) {
			mr = new ForbiddenMediaResource();
		} else {
			// extract file
			String path = ureq.getModuleURI();
			
			VFSLeaf vfsLeaf = (VFSLeaf)folderComponent.getRootContainer().resolve(path);
			if(vfsLeaf == null) {
				//double decoding of ++
				vfsLeaf = (VFSLeaf)FolderCommandHelper.tryDoubleDecoding(ureq, folderComponent);
			}
			
			if(vfsLeaf != null && vfsLeaf.canMeta() == VFSConstants.YES) {
				VFSLeaf thumbnail = vfsRepositoryservice.getThumbnail(vfsLeaf, 200, 200, false);
				if(thumbnail != null) {
					mr = new VFSMediaResource(thumbnail);
				}
			}
			if(mr == null) {
				mr = new NotFoundMediaResource();
			}
		}
		ureq.getDispatchResult().setResultingMediaResource(mr);
		return null;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public boolean runsModal() {
		return false;
	}

	@Override
	public String getModalTitle() {
		return null;
	}
}