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
package org.olat.core.commons.services.image;

import java.io.File;

import org.olat.core.util.vfs.VFSLeaf;

/**
 * This the bean exposed to the others beans for use.
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ImageHelperBean implements ImageService {
	
	private ImageHelperSPI imageHelperServiceProvider;

	/**
	 * [used by Spring]
	 * @param imageHelperServiceProvider
	 */
	public void setImageHelperServiceProvider(ImageHelperSPI imageHelperServiceProvider) {
		this.imageHelperServiceProvider = imageHelperServiceProvider;
	}
	
	@Override
	public Size thumbnailPDF(VFSLeaf pdfFile, VFSLeaf thumbnailFile, int maxWidth, int maxHeight, boolean fill) {
		return imageHelperServiceProvider.thumbnailPDF(pdfFile, thumbnailFile, maxWidth, maxHeight);
	}

	@Override
	public Size getSize(VFSLeaf image, String suffix) {
		return imageHelperServiceProvider.getSize(image, suffix);
	}

	@Override
	public Size getSize(File image, String suffix) {
		return imageHelperServiceProvider.getSize(image, suffix);
	}

	@Override
	public boolean cropImage(File image, File cropedImage, Crop cropSelection) {
		return imageHelperServiceProvider.cropImage(image, cropedImage, cropSelection);
	}

	@Override
	public Size scaleImage(File image, String extension, File scaledImage, int maxWidth, int maxHeight, boolean fill) {
		return imageHelperServiceProvider.scaleImage(image, extension, scaledImage, maxWidth, maxHeight, fill);
	}

	@Override
	public Size scaleImage(VFSLeaf image, VFSLeaf scaledImage, int maxWidth, int maxHeight, boolean fill) {
		return imageHelperServiceProvider.scaleImage(image, scaledImage, maxWidth, maxHeight, fill);
	}

	@Override
	public Size scaleImage(File image, String imgExt, VFSLeaf scaledImage, int maxWidth, int maxHeight) {
		return imageHelperServiceProvider.scaleImage(image, imgExt, scaledImage, maxWidth, maxHeight);
	}
}
