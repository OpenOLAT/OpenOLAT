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
package org.olat.core.commons.services.webdav.manager;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
 * 
 * Initial date: 06.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class WebDAVMergeSource extends MergeSource {
	
	private boolean init = false;
	private final Identity identity;
	private long loadTime;
	
	public WebDAVMergeSource(Identity identity) {
		super(null, null);
		this.identity = identity;
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	@Override
	public VFSStatus canWrite() {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus canDelete() {
		return VFSConstants.NO;
	}
	
	@Override
	public VFSStatus canRename() {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus canCopy() {
		return VFSConstants.NO;
	}

	@Override
	public VFSStatus delete() {
		return VFSConstants.NO;
	}
	
	@Override
	public void setDefaultItemFilter(VFSItemFilter defaultFilter) {
		//
	}

	@Override
	public List<VFSItem> getItems() {
		checkInitialization();
		return super.getItems();
	}

	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		checkInitialization();
		return super.getItems(filter);
	}

	@Override
	public VFSItem resolve(String path) {
		checkInitialization();
		return super.resolve(path);
	}
	
	private void checkInitialization() {
		if(!init) {
			synchronized(this) {
				if(!init) {
					loadTime = System.currentTimeMillis();
					init();
				}
			}
		} else if((System.currentTimeMillis() - loadTime) > 60000) {
			synchronized(this) {
				if((System.currentTimeMillis() - loadTime) > 60000) {
					loadTime = System.currentTimeMillis();
					CoreSpringFactory.getImpl(TaskExecutorManager.class).execute(new AsyncInit());
				}
			}
		}
	}
	
	@Override
	protected void init() {
		List<VFSContainer> containers = loadMergedContainers();
		setMergedContainers(containers);
		loadTime = System.currentTimeMillis();
		init = true;
	}
	
	protected abstract List<VFSContainer> loadMergedContainers();
	
	public class AsyncInit implements Runnable {
		@Override
		public void run() {
			init();
		}
	}
}