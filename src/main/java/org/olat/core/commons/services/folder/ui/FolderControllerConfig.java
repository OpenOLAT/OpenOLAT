/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.folder.ui;

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;

/**
 * 
 * Initial date: 21 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FolderControllerConfig {
	
	private static final FolderControllerConfig DEFAULT_CONFIG = new FolderControllerConfigBuilder().build();
	
	private final boolean displaySubscription;
	private final boolean displayWebDAVLink;
	private final boolean displayQuotaLink;
	private final boolean displaySearch;
	private final boolean displayUnzip;
	private final boolean fileHub;
	private final FolderEmailFilter emailFilter;
	private final CustomLinkTreeModel customLinkTreeModel;
	
	private FolderControllerConfig(FolderControllerConfigBuilder builder) {
		this.displaySubscription = builder.displaySubscription;
		this.displayWebDAVLink = builder.displayWebDAVLink;
		this.displayQuotaLink = builder.displayQuotaLink;
		this.displaySearch = builder.displaySearch;
		this.displayUnzip = builder.displayUnzip;
		this.fileHub = builder.fileHub;
		this.emailFilter = builder.emailFilter;
		this.customLinkTreeModel = builder.customLinkTreeModel;
	}
	
	public boolean isDisplaySubscription() {
		return displaySubscription;
	}

	public boolean isDisplayWebDAVLink() {
		return displayWebDAVLink;
	}

	public boolean isDisplayQuotaLink() {
		return displayQuotaLink;
	}

	public boolean isDisplaySearch() {
		return displaySearch;
	}

	public boolean isDisplayUnzip() {
		return displayUnzip;
	}

	public boolean isFileHub() {
		return fileHub;
	}

	public FolderEmailFilter getEmailFilter() {
		return emailFilter;
	}

	public CustomLinkTreeModel getCustomLinkTreeModel() {
		return customLinkTreeModel;
	}

	public static FolderControllerConfig defaultConfig() {
		return DEFAULT_CONFIG;
	}
	
	public static FolderControllerConfigBuilder builder() {
		return new FolderControllerConfigBuilder();
	}
	
	public static final class FolderControllerConfigBuilder {
		
		private boolean displaySubscription = true;
		private boolean displayWebDAVLink = true;
		private boolean displayQuotaLink = true;
		private boolean displaySearch = true;
		private boolean displayUnzip = true;
		private boolean fileHub = false;
		private FolderEmailFilter emailFilter = FolderEmailFilter.always;
		private CustomLinkTreeModel customLinkTreeModel = null;
		
		private FolderControllerConfigBuilder() {
			//
		}
		
		public FolderControllerConfigBuilder withDisplaySubscription(boolean enabled) {
			this.displaySubscription = enabled;
			return this;
		}
		
		public FolderControllerConfigBuilder withDisplayWebDAVLinkEnabled(boolean enabled) {
			this.displayWebDAVLink = enabled;
			return this;
		}
		
		public FolderControllerConfigBuilder withDisplayQuotaLink(boolean enabled) {
			this.displayQuotaLink = enabled;
			return this;
		}
		
		public FolderControllerConfigBuilder withSearchEnabled(boolean enabled) {
			this.displaySearch = enabled;
			return this;
		}
		
		public FolderControllerConfigBuilder withUnzipEnabled(boolean enabled) {
			this.displayUnzip = enabled;
			return this;
		}
		
		public FolderControllerConfigBuilder withFileHub(boolean fileHub) {
			this.fileHub = fileHub;
			return this;
		}
		
		public FolderControllerConfigBuilder withMail(FolderEmailFilter emailFilter) {
			this.emailFilter = emailFilter;
			return this;
		}
		
		public FolderControllerConfigBuilder withCustomLinkTreeModel(CustomLinkTreeModel model) {
			this.customLinkTreeModel = model;
			return this;
		}
		
		public FolderControllerConfig build() {
			return new FolderControllerConfig(this);
		}
		
	}

}
