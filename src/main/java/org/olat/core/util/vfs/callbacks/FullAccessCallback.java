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

package org.olat.core.util.vfs.callbacks;

import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.util.vfs.Quota;

public class FullAccessCallback implements VFSSecurityCallback {
	
	@Override
	public boolean canRead() {
		return true;
	}
	
	@Override
	public boolean canWrite() {
		return true;
	}

	@Override
	public boolean canCreateFolder() {
		return true;
	}
	
	@Override
	public boolean canDelete() {
		return true;
	}
	
	@Override
	public boolean canList() {
		return true;
	}
	
	@Override
	public boolean canCopy() {
		return true;
	}
	
	@Override
	public boolean canDeleteRevisionsPermanently() {
		return true;
	}

	@Override
	public Quota getQuota() {
		return null;
	}
	
	@Override
	public void setQuota(Quota quota) {
		// nothing to do here.
	}
	
	@Override
	public SubscriptionContext getSubscriptionContext() {
		return null;
	}

}
