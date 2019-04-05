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
package org.olat.core.commons.services.vfs;

import org.olat.core.commons.services.vfs.VFSLeafEditor.Mode;

/**
 * 
 * Initial date: 22 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class VFSLeafEditorSecurityCallbackBuilder {
	
	private Mode mode = Mode.VIEW;
	private boolean versionControlled = false;
	private boolean canClose = true;
	
	/**
	 * Default: VIEW
	 *
	 * @param mode
	 * @return
	 */
	public VFSLeafEditorSecurityCallbackBuilder withMode(Mode mode) {
		this.mode = mode;
		return this;
	}
	
	/**
	 * Default: false
	 *
	 * @param versionControlled
	 * @return
	 */
	public VFSLeafEditorSecurityCallbackBuilder withVersionControlled(boolean versionControlled) {
		this.versionControlled = versionControlled;
		return this;
	}
	
	/**
	 * Default: true
	 *
	 * @param canClose
	 * @return
	 */
	public VFSLeafEditorSecurityCallbackBuilder canClose(boolean canClose) {
		this.canClose = canClose;
		return this;
	}
	
	public VFSLeafEditorSecurityCallback build() {
		VFSLeafEditorSecurityCallbackImpl secCallback = new VFSLeafEditorSecurityCallbackImpl();
		secCallback.setMode(this.mode);
		secCallback.setVersionControlled(this.versionControlled);
		secCallback.setCanClose(this.canClose);
		return secCallback;
	}
	
	public static VFSLeafEditorSecurityCallbackBuilder builder() {
		return new VFSLeafEditorSecurityCallbackBuilder();
	}
	
	public static VFSLeafEditorSecurityCallbackBuilder clone(VFSLeafEditorSecurityCallback secCallback) {
		return builder()
				.withMode(secCallback.getMode())
				.withVersionControlled(secCallback.isVersionControlled())
				.canClose(secCallback.canClose());
	}
	
	private VFSLeafEditorSecurityCallbackBuilder() {
	}
	
	private static class VFSLeafEditorSecurityCallbackImpl implements VFSLeafEditorSecurityCallback {

		private Mode mode;
		private boolean versionControlled;
		private boolean canClose;

		@Override
		public Mode getMode() {
			return mode;
		}

		private void setMode(Mode mode) {
			this.mode = mode;
		}

		@Override
		public boolean isVersionControlled() {
			return versionControlled;
		}

		private void setVersionControlled(boolean versionControlled) {
			this.versionControlled = versionControlled;
		}

		@Override
		public boolean canClose() {
			return canClose;
		}
		
		private void setCanClose(boolean canClose) {
			this.canClose = canClose;
		}
		
	}

}
