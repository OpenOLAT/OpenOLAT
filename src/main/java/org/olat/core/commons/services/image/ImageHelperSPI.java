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
 * The interface needed to implement a image helper service.
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface ImageHelperSPI {
	
	/**
	 * Make a thumbnail from the first page of the PDF
	 * @param pdfFile
	 * @param thumbnailFile
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 */
	public Size thumbnailPDF(VFSLeaf pdfFile, VFSLeaf thumbnailFile, int maxWidth, int maxHeight);
	
	public Size getSize(VFSLeaf image, String suffix);
	
	public Size getSize(File image, String suffix);
	
	public boolean cropImage(File image, File cropedImage, Crop cropSelection);

	public Size scaleImage(File image, String extension, File scaledImage, int maxWidth, int maxHeight, boolean fill);
	
	public Size scaleImage(VFSLeaf image, VFSLeaf scaledImage, int maxWidth, int maxHeight, boolean fill);

	public Size scaleImage(File image, String imgExt, VFSLeaf scaledImage, int maxWidth, int maxHeight);
	
}
