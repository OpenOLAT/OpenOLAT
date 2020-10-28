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
package org.olat.core.commons.services.doceditor;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 31 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocEditorConfigs {
	
	public static interface Config {
		public String getType();
	}
	
	private final VFSLeaf vfsLeaf;
	private final Mode mode;
	private final boolean metaAvailable;
	private final boolean versionControlled;
	private final boolean downloadEnabled;
	private final Map<String, Config> configs;

	private DocEditorConfigs(Builder builder) {
		this.vfsLeaf = builder.vfsLeaf;
		this.mode = builder.mode;
		this.metaAvailable = builder.metaAvailable;
		this.versionControlled = builder.versionControlled;
		this.downloadEnabled = builder.downloadEnabled;
		this.configs = new HashMap<>(builder.configs);
	}
	
	public VFSLeaf getVfsLeaf() {
		return vfsLeaf;
	}

	/**
	 * Default: VIEW
	 *
	 * @return
	 */
	public Mode getMode() {
		return mode;
	}

	/**
	 * Default: true
	 *
	 * @return
	 */
	public boolean isMetaAvailable() {
		return metaAvailable;
	}

	/**
	 * Default: false
	 *
	 * @return
	 */
	public boolean isVersionControlled() {
		return versionControlled;
	}

	/**
	 * Default: true
	 *
	 * @return
	 */
	public boolean isDownloadEnabled() {
		return downloadEnabled;
	}

	public Config getConfig(String type) {
		return this.configs.get(type);
	}

	public static Builder builder() {
		return new Builder();
	}
	
	public static Builder clone(DocEditorConfigs configs) {
		Builder builder = new Builder()
				.withMode(configs.getMode())
				.withMetaAvailable(configs.isMetaAvailable())
				.withVersionControlled(configs.isVersionControlled())
				.withDownloadEnabled(configs.isDownloadEnabled());
		// No deep copy right now.
		builder.configs = new HashMap<>(configs.configs);
		return builder;
	}

	public static final class Builder {
		private VFSLeaf vfsLeaf;
		private Mode mode = Mode.VIEW;
		private boolean metaAvailable = true;
		private boolean versionControlled = false;
		private boolean downloadEnabled = true;
		private Map<String, Config> configs = new HashMap<>();

		private Builder() {
			//
		}
		
		public Builder withMode(Mode mode) {
			this.mode = mode;
			return this;
		}
		
		public Builder withMetaAvailable(boolean metaAvailable) {
			this.metaAvailable = metaAvailable;
			return this;
		}
		
		public Builder withVersionControlled(boolean versionControlled) {
			this.versionControlled = versionControlled;
			return this;
		}
		
		public Builder withDownloadEnabled(boolean downloadEnabled) {
			this.downloadEnabled = downloadEnabled;
			return this;
		}
		
		public Builder addConfig(Config config) {
			this.configs.put(config.getType(), config);
			return this;
		}

		public DocEditorConfigs build(VFSLeaf vfsLeaf) {
			this.vfsLeaf = vfsLeaf;
			return new DocEditorConfigs(this);
		}
	}
	
}
