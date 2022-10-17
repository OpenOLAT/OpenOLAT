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
package org.olat.modules.taxonomy.ui;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.co
 *
 */
public abstract class TaxonomyLevelImageMapper implements Mapper {
	
	@Autowired
	private TaxonomyService taxonomyService;
	
	public TaxonomyLevelImageMapper() {
		CoreSpringFactory.autowireObject(this);
	}
	
	protected abstract String getUrlPart();
	
	protected abstract VFSLeaf getImage(TaxonomyService taxonomyService, TaxonomyLevel level);

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		// clean end of rel path
		int lastIndex = relPath.lastIndexOf('.');
		if (lastIndex >= 0) {
			relPath = relPath.substring(0, lastIndex);
		}
		if (relPath.endsWith("/")) {
			relPath = relPath.substring(0, relPath.length() -1);
		}
		
		// Remove the cache part
		lastIndex = relPath.lastIndexOf('/');
		if (lastIndex >= 0) {
			relPath = relPath.substring(lastIndex + 1, relPath.length());
		}
		
		if (StringHelper.isLong(relPath)) {
			TaxonomyLevelRefImpl ref = new TaxonomyLevelRefImpl(Long.valueOf(relPath));
			TaxonomyLevel level = taxonomyService.getTaxonomyLevel(ref);
			VFSLeaf image = getImage(taxonomyService, level);
			if (image != null) {
				return new VFSMediaResource(image);
			}
		}
		return new NotFoundMediaResource();
	}

	public String getImageUrl(TaxonomyLevel level) {
		VFSItem image = getImage(taxonomyService, level);
		if (image instanceof VFSLeaf) {
			long lastModified = image.getLastModified();
			String cachePart = lastModified > 0? String.valueOf(lastModified): UUID.randomUUID().toString().replace("-", "");
			return getUrlPart() + "/" + cachePart + "/" + level.getKey();
		}
		return null;
	}
}
