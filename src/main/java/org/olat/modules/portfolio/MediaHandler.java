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
package org.olat.modules.portfolio;

import java.io.File;
import java.util.Locale;

import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.user.manager.ManifestBuilder;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface MediaHandler {
	
	public String getType();
	
	public boolean acceptMimeType(String mimeType);
	
	public String getIconCssClass(MediaLight media);
	
	public VFSLeaf getThumbnail(MediaLight media, Size size);
	
	/**
	 * Return some informations to prefill the media/artefact creation form.
	 * @param mediaObject
	 */
	public MediaInformations getInformations(Object mediaObject);
	
	public Media createMedia(String title, String description, Object mediaObject, String businessPath, Identity author);
	
	public Controller getMediaController(UserRequest ureq, WindowControl wControl, Media media, MediaRenderingHints hints);
	
	public Controller getEditMediaController(UserRequest ureq, WindowControl wControl, Media media);
	
	/**
	 * Export the user data.
	 * 
	 * @param identity The identity
	 * @param manifest A manifest for all the files added to the archive
	 * @param archiveDirectory The directory where the files can be safely saved.
	 * @param locale The language
	 */
	public void export(Media media, ManifestBuilder manifest, File mediaArchiveDirectory, Locale locale);

}
