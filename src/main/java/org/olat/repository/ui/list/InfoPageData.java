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

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntryEducationalType;

/**
 * Display values of the info page header, common to a repository entry
 * (course) and a curriculum element (CPL implementation). Booking, start and
 * leave state is not part of this: see DetailsHeaderConfig, consumed by the
 * "Get started" panel.
 *
 * Initial date: 18 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface InfoPageData {

	String getIconCssClass();

	String getExternalRef();

	String getTranslatedTechnicalType();

	String getTitle();

	String getAuthors();

	String getTeaser();

	VFSLeaf getTeaserImage();

	VFSLeaf getTeaserMovie();

	RepositoryEntryEducationalType getEducationalType();

	List<TaxonomyLevel> getTaxonomyLevels();

	/**
	 * @return true for a repository entry, for a top-level curriculum element;
	 *         false for a nested curriculum element (no bookmark today).
	 */
	boolean hasBookmark();

	/**
	 * @return the resourceable to mark, or null if !hasBookmark()
	 */
	OLATResourceable getBookmarkOres();

	/**
	 * @return the business path stored with the bookmark, or null if !hasBookmark()
	 */
	String getBookmarkBusinessPath();

}
