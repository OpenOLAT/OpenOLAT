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
package org.olat.core.commons.editor.htmleditor;

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.services.vfs.VFSLeafEditorConfigs;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.edusharing.VFSEdusharingProvider;

/**
 * 
 * Initial date: 31 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class HTMLEditorConfig implements VFSLeafEditorConfigs.Config {

	public static final String TYPE = "oo-html-editor";
	
	private final VFSContainer vfsContainer;
	private final String filePath;
	private final String mediaPath;
	private final CustomLinkTreeModel customLinkTreeModel;
	private final VFSEdusharingProvider edusharingProvider;
	private final boolean disableMedia;
	private final boolean allowCustomMediaFactory;

	private HTMLEditorConfig(Builder builder) {
		this.vfsContainer = builder.vfsContainer;
		this.filePath = builder.filePath;
		this.mediaPath = builder.mediaPath;
		this.customLinkTreeModel = builder.customLinkTreeModel;
		this.edusharingProvider = builder.edusharingProvider;
		this.disableMedia = builder.disableMedia;
		this.allowCustomMediaFactory = builder.allowCustomMediaFactory;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	public VFSContainer getVfsContainer() {
		return vfsContainer;
	}

	public String getFilePath() {
		return filePath;
	}

	public String getMediaPath() {
		return mediaPath;
	}

	public CustomLinkTreeModel getCustomLinkTreeModel() {
		return customLinkTreeModel;
	}

	public VFSEdusharingProvider getEdusharingProvider() {
		return edusharingProvider;
	}

	public boolean isDisableMedia() {
		return disableMedia;
	}

	public boolean isAllowCustomMediaFactory() {
		return allowCustomMediaFactory;
	}

	public static Builder builder(VFSContainer vfsContainer, String filePath) {
		return new Builder(vfsContainer, filePath);
	}

	public static final class Builder {
		private VFSContainer vfsContainer;
		private String filePath;
		private String mediaPath;
		private CustomLinkTreeModel customLinkTreeModel;
		private VFSEdusharingProvider edusharingProvider;
		private boolean disableMedia;
		private boolean allowCustomMediaFactory = true;

		private Builder(VFSContainer vfsContainer, String filePath) {
			this.vfsContainer = vfsContainer;
			this.filePath = filePath;
		}
		
		public Builder withMediaPath(String mediaPath) {
			this.mediaPath = mediaPath;
			return this;
		}

		public Builder withCustomLinkTreeModel(CustomLinkTreeModel customLinkTreeModel) {
			this.customLinkTreeModel = customLinkTreeModel;
			return this;
		}

		public Builder withEdusharingProvider(VFSEdusharingProvider edusharingProvider) {
			this.edusharingProvider = edusharingProvider;
			return this;
		}

		public Builder withDisableMedia(boolean disableMedia) {
			this.disableMedia = disableMedia;
			return this;
		}

		public Builder withAllowCustomMediaFactory(boolean allowCustomMediaFactory) {
			this.allowCustomMediaFactory = allowCustomMediaFactory;
			return this;
		}

		public HTMLEditorConfig build() {
			return new HTMLEditorConfig(this);
		}
	}

}
