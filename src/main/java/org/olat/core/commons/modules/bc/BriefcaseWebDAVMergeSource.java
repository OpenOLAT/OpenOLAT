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
package org.olat.core.commons.modules.bc;

import java.util.List;

import org.olat.core.commons.modules.bc.vfs.OlatNamedContainerImpl;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.callbacks.FullAccessWithQuotaCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.user.PersonalFolderManager;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BriefcaseWebDAVMergeSource  extends MergeSource {
	private boolean init = false;
	private final Identity identity;
		
	public BriefcaseWebDAVMergeSource(Identity identity) {
		super(null, identity.getName());
		this.identity = identity;
	}
	
	public BriefcaseWebDAVMergeSource(Identity identity, String name) {
		super(null, name);
		this.identity = identity;
	}

	@Override
	public List<VFSItem> getItems() {
		if(!init) {
			init();
		}
		return super.getItems();
	}

	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		if(!init) {
			init();
		}
		return super.getItems(filter);
	}

	@Override
	public VFSItem resolve(String path) {
		if(!init) {
			init();
		}
		return super.resolve(path);
	}
	
	@Override
	public VFSSecurityCallback getLocalSecurityCallback() {
		if(super.getLocalSecurityCallback() == null) {
			//set quota for this merge source
			QuotaManager qm = QuotaManager.getInstance();
			Quota quota = qm.getCustomQuotaOrDefaultDependingOnRole(identity, PersonalFolderManager.getRootPathFor(identity));
			FullAccessWithQuotaCallback secCallback = new FullAccessWithQuotaCallback(quota);
			setLocalSecurityCallback(secCallback);
		}
		return super.getLocalSecurityCallback();
	}

	@Override
	protected void init() {
		super.init();
		// mount /public
		String rootPath = PersonalFolderManager.getRootPathFor(identity);
		OlatRootFolderImpl vfsPublic = new OlatRootFolderImpl(rootPath + "/public", this);
		//vfsPublic.getBasefile().mkdirs(); // lazy initialize folders
		// we do a little trick here and wrap it again in a NamedContainerImpl so
		// it doesn't show up as a OlatRootFolderImpl to prevent it from editing its MetaData
		OlatNamedContainerImpl vfsNamedPublic = new OlatNamedContainerImpl("public", vfsPublic);
		addContainer(vfsNamedPublic);
		
		// mount /private
		OlatRootFolderImpl vfsPrivate = new OlatRootFolderImpl(rootPath + "/private", this);
		//vfsPrivate.getBasefile().mkdirs(); // lazy initialize folders
		// we do a little trick here and wrap it again in a NamedContainerImpl so
		// it doesn't show up as a OlatRootFolderImpl to prevent it from editing its MetaData
		OlatNamedContainerImpl vfsNamedPrivate = new OlatNamedContainerImpl("private", vfsPrivate);
		addContainer(vfsNamedPrivate);
		
		init = true;
	}
}