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
package org.olat.modules.curriculum.ui;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementFileType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.ui.list.InfoPageData;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 18 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementInfoPageData implements InfoPageData {

	private final CurriculumElement element;

	@Autowired
	private CurriculumService curriculumService;

	public CurriculumElementInfoPageData(CurriculumElement element) {
		this.element = element;
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_curriculum_element";
	}

	@Override
	public String getExternalRef() {
		return element.getIdentifier();
	}

	@Override
	public String getTranslatedTechnicalType() {
		return element.getType() != null ? element.getType().getDisplayName() : null;
	}

	@Override
	public String getTitle() {
		return element.getDisplayName();
	}

	@Override
	public String getAuthors() {
		return element.getAuthors();
	}

	@Override
	public String getTeaser() {
		return element.getTeaser();
	}

	@Override
	public VFSLeaf getTeaserImage() {
		return curriculumService.getCurriculumElemenFile(element, CurriculumElementFileType.teaserImage);
	}

	@Override
	public VFSLeaf getTeaserMovie() {
		return curriculumService.getCurriculumElemenFile(element, CurriculumElementFileType.teaserVideo);
	}

	@Override
	public RepositoryEntryEducationalType getEducationalType() {
		return element.getEducationalType();
	}

	@Override
	public List<TaxonomyLevel> getTaxonomyLevels() {
		return curriculumService.getTaxonomy(element);
	}

	@Override
	public boolean hasBookmark() {
		return element.getParent() == null;
	}

	@Override
	public OLATResourceable getBookmarkOres() {
		return hasBookmark() ? OresHelper.createOLATResourceableInstance(CurriculumElement.class, element.getKey()) : null;
	}

	@Override
	public String getBookmarkBusinessPath() {
		return hasBookmark() ? "[MyCoursesSite:0][CurriculumElement:" + element.getKey() + "]" : null;
	}

}
