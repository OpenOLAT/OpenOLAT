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
package org.olat.ims.qti21.pool;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.StringHelper;

import uk.ac.ed.ph.jqtiplus.node.content.xhtml.hypertext.A;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.image.Img;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.object.Object;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.utils.QueryUtils;

/**
 * 
 * Initial date: 16 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportExportHelper {
	
	protected static List<String> getMaterials(AssessmentItem item) {
		List<String> materials = new ArrayList<>();
		QueryUtils.search(Img.class, item).forEach(img -> {
			if(img.getSrc() != null) {
				materials.add(img.getSrc().toString());
			}
		});
		
		QueryUtils.search(A.class, item).forEach(a -> {
			URI href = a.getHref();
			if(href != null && href.getHost() == null && href.getPath() != null) {
				materials.add(href.getPath());
			}
		});

		QueryUtils.search(Object.class, item).forEach(object -> {
			if(StringHelper.containsNonWhitespace(object.getData())) {
				materials.add(object.getData());
			}
		});
		return materials;
	}
	
	protected static void getMaterials(AssessmentItem item, File itemFile, AssessmentItemsAndResources materials) {
		File directory = itemFile.getParentFile();

		QueryUtils.search(Img.class, item).forEach(img -> {
			if(img.getSrc() != null) {
				String imgPath = img.getSrc().toString();
				File imgFile = new File(directory, imgPath);
				if(imgFile.exists()) {
					materials.addMaterial(new ItemMaterial(imgFile, imgPath));
				}
			}
		});
		
		QueryUtils.search(A.class, item).forEach(a -> {
			URI href = a.getHref();
			if(href != null && href.getHost() == null && href.getPath() != null) {
				String hrefPath = href.getPath();
				File aFile = new File(directory, hrefPath);
				if(aFile.exists()) {
					materials.addMaterial(new ItemMaterial(aFile, hrefPath));
				}
			}
		});

		QueryUtils.search(Object.class, item).forEach(object -> {
			if(StringHelper.containsNonWhitespace(object.getData())) {
				String path = object.getData();
				File objectFile = new File(directory, path);
				if(objectFile.exists()) {
					materials.addMaterial(new ItemMaterial(objectFile, path));
				}
			}
		});
	}
	
	public static final class AssessmentItemsAndResources {
		private final List<ResolvedAssessmentItem> itemEls = new ArrayList<>();
		private final List<ItemMaterial> materials = new ArrayList<>();
		
		public List<ResolvedAssessmentItem> getAssessmentItems() {
			return itemEls;
		}
		
		public void addItemEl(ResolvedAssessmentItem el) {
			itemEls.add(el);
		}
		
		public List<ItemMaterial> getMaterials() {
			return materials;
		}
		
		public void addMaterial(ItemMaterial material) {
			materials.add(material);
		}
	}
	
	public static final class ItemMaterial {
		private final File file;
		private final String exportUri;
		
		public ItemMaterial(File file, String exportUri) {
			this.file = file;
			this.exportUri = exportUri;
		}
		
		public File getFile() {
			return file;
		}
		
		public String getExportUri() {
			return exportUri;
		}
	}

}
