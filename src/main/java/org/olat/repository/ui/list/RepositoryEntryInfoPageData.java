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
package org.olat.repository.ui.list;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 18 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryInfoPageData implements InfoPageData {

	private final RepositoryEntry entry;
	private final Translator translator;

	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private RepositoryService repositoryService;

	public RepositoryEntryInfoPageData(RepositoryEntry entry, Translator translator) {
		this.entry = entry;
		this.translator = translator;
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public String getIconCssClass() {
		return RepositoyUIFactory.getIconCssClass(entry);
	}

	@Override
	public String getExternalRef() {
		return entry.getExternalRef();
	}

	@Override
	public String getTranslatedTechnicalType() {
		if (StringHelper.containsNonWhitespace(entry.getTechnicalType())) {
			NodeAccessType type = NodeAccessType.of(entry.getTechnicalType());
			return ConditionNodeAccessProvider.TYPE.equals(type.getType())
					? translator.translate("CourseModule")
					: nodeAccessService.getNodeAccessTypeName(type, translator.getLocale());
		}
		return translator.translate(entry.getOlatResource().getResourceableTypeName());
	}

	@Override
	public String getTitle() {
		return entry.getDisplayname();
	}

	@Override
	public String getAuthors() {
		return entry.getAuthors();
	}

	@Override
	public String getTeaser() {
		return entry.getTeaser();
	}

	@Override
	public VFSLeaf getTeaserImage() {
		return repositoryService.getIntroductionImage(entry);
	}

	@Override
	public VFSLeaf getTeaserMovie() {
		return repositoryService.getIntroductionMovie(entry);
	}

	@Override
	public RepositoryEntryEducationalType getEducationalType() {
		return entry.getEducationalType();
	}

	@Override
	public List<TaxonomyLevel> getTaxonomyLevels() {
		return repositoryService.getTaxonomy(entry);
	}

	@Override
	public boolean hasBookmark() {
		return true;
	}

	@Override
	public OLATResourceable getBookmarkOres() {
		return OresHelper.clone(entry);
	}

	@Override
	public String getBookmarkBusinessPath() {
		return "[RepositoryEntry:" + entry.getKey() + "]";
	}

}
