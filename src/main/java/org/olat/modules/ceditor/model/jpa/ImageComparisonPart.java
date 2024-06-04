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
package org.olat.modules.ceditor.model.jpa;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.model.ImageComparisonElement;
import org.olat.modules.cemedia.MediaToPagePart;
import org.olat.modules.cemedia.manager.MediaToPagePartDAO;
import org.olat.modules.cemedia.model.MediaToPagePartImpl;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Transient;

/**
 * Initial date: 2024-05-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Entity(name="ceimagecomparisonpart")
public class ImageComparisonPart extends AbstractPart implements ImageComparisonElement {

	@Serial
	private static final long serialVersionUID = 8998957878841775817L;

	@OneToMany(targetEntity = MediaToPagePartImpl.class, fetch = FetchType.LAZY, mappedBy = "pagePart",
			orphanRemoval = true, cascade = {CascadeType.REMOVE})
	@OrderColumn(name = "pos")
	private List<MediaToPagePart> relations = new ArrayList<>();

	public List<MediaToPagePart> getRelations() {
		return relations;
	}

	@Override
	@Transient
	public String getType() {
		return "imagecomparison";
	}

	@Override
	public ImageComparisonPart copy() {
		ImageComparisonPart part = new ImageComparisonPart();
		copy(part);
		return part;
	}

	@Override
	public boolean afterCopy(PagePart oldPart) {
		MediaToPagePartDAO mediaToPagePartDAO = CoreSpringFactory.getImpl(MediaToPagePartDAO.class);
		List<MediaToPagePart> sourceRelations = mediaToPagePartDAO.loadRelations(oldPart);
		for (MediaToPagePart sourceRelation : sourceRelations) {
			mediaToPagePartDAO.persistRelation(this, sourceRelation.getMedia(),
					sourceRelation.getMediaVersion(), sourceRelation.getIdentity());
		}
		return true;
	}
}
