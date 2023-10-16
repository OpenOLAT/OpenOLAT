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
package org.olat.modules.cemedia;

import java.io.File;
import java.util.Locale;

import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.user.manager.ManifestBuilder;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface MediaHandler {
	
	String getType();
	
	boolean acceptMimeType(String mimeType);
	
	String getIconCssClass(MediaVersion media);
	
	boolean hasMediaThumbnail(MediaVersion media);
	
	VFSLeaf getThumbnail(MediaVersion media, Size size);
	
	MediaHandlerUISettings getUISettings();
	
	/**
	 * Return some informations to prefill the media/artefact creation form.
	 * @param mediaObject
	 */
	MediaInformations getInformations(Object mediaObject);
	
	Media createMedia(String title, String description, String altText, Object mediaObject, String businessPath,
			Identity author, MediaLog.Action action);
	
	Controller getMediaController(UserRequest ureq, WindowControl wControl, MediaVersion version, RenderingHints hints);
	
	Controller getEditMetadataController(UserRequest ureq, WindowControl wControl, Media media);
	
	Controller getNewVersionController(UserRequest ureq, WindowControl wControl, Media media, CreateVersion createVersion);
	
	/**
	 * Export the user data.
	 * 
	 * @param identity The identity
	 * @param manifest A manifest for all the files added to the archive
	 * @param archiveDirectory The directory where the files can be safely saved.
	 * @param locale The language
	 */
	void export(Media media, ManifestBuilder manifest, File mediaArchiveDirectory, Locale locale);
	
	boolean hasDownload();
	
	public enum CreateVersion {
		CREATE,
		UPLOAD
	}
}
