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
package org.olat.core.commons.services.doceditor.drawio;

import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 1 Sep 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DrawioEditorConfig implements DocEditorConfigs.Config {
	
	public static final String TYPE = "drawio-editor";
	
	private final VFSLeaf svgPreviewLeaf;
	
	private DrawioEditorConfig(Builder builder) {
		this.svgPreviewLeaf = builder.svgPreviewLeaf;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	public VFSLeaf getSvgPreviewLeaf() {
		return svgPreviewLeaf;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static final class Builder {
		
		private VFSLeaf svgPreviewLeaf;

		private Builder() {
			//
		}
		
		public Builder withSvgPreviewLeaf(VFSLeaf svgPreviewLeaf) {
			this.svgPreviewLeaf = svgPreviewLeaf;
			return this;
		}

		public DrawioEditorConfig build() {
			return new DrawioEditorConfig(this);
		}
	}

}
