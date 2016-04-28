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

package org.olat.modules.video.model;

import java.io.Serializable;
import java.util.HashMap;

import org.olat.core.commons.services.image.Size;
import org.olat.core.id.OLATResourceable;

/**
 * Model of the videoresource metadata to persist in xml
 *
 * Initial Date:  Apr 9, 2015
 * @author Dirk Furrer
 * 
 */

public class VideoMetadata implements Serializable{
		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

		// Data model versioning
		public static final int CURRENT_MODEL_VERSION = 1;
		
		private Size size;
		private String posterframe;
		private HashMap<String, String> tracks;
		private boolean commentsEnabled;
		private boolean ratingEnabled;
		
		@SuppressWarnings("unused")
		private int modelVersion = 0; 
				
		public VideoMetadata(OLATResourceable resource){
			this.tracks = new HashMap<String, String>();
			// new model constructor, set to current version
			this.modelVersion = CURRENT_MODEL_VERSION;
		}
				
		public Size getSize() {
			return size;
		}

		public void setSize(Size size) {
			this.size = size;
		}

		public String getPosterframe() {
			return posterframe;
		}

		public void setPosterframe(String posterframe) {
			this.posterframe = posterframe;
		}

		public HashMap<String, String> getAllTracks(){
			return tracks;
		}
		
		public void addTrack(String lang, String trackFile){
			tracks.put(lang, trackFile);
		}
		
		public String getTrack(String lang){
			return tracks.get(lang);
		}
		
		public void removeTrack(String lang){
			tracks.remove(lang);
		}
		
		public void setCommentsEnabled(boolean isEnabled){
			this.commentsEnabled = isEnabled;
		}
		
		public boolean getCommentsEnabled(){
			return this.commentsEnabled;
		}
		
		public void setRatingEnabled(boolean isEnabled){
			this.ratingEnabled = isEnabled;
		}
		
		public boolean getRatingEnabled(){
			return this.ratingEnabled;
		}
}
