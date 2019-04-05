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
package org.olat.core.commons.services.vfs.ui.media;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;

/**
 * 
 * Initial date: 5 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSMetadataMediaResource extends VFSMediaResource {

	private final VFSMetadata metadata;

	public VFSMetadataMediaResource(VFSMetadata metadata) {
		super(null);
		this.metadata = metadata;
	}

	@Override
	public VFSLeaf getLeaf() {
		VFSLeaf leaf = super.getLeaf();
		if(leaf == null) {
			VFSItem revFile = CoreSpringFactory.getImpl(VFSRepositoryService.class).getItemFor(metadata);
			if(revFile instanceof VFSLeaf) {
				leaf = (VFSLeaf)revFile;
				setLeaf(leaf);
			}
		}
		return leaf;
	}
}
