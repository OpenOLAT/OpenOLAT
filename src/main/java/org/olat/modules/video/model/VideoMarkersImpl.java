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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.modules.video.VideoMarker;
import org.olat.modules.video.VideoMarkers;
import org.olat.modules.video.ui.marker.VideoMarkerRowComparator;

/**
 * 
 * Initial date: 27 nov. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoMarkersImpl implements VideoMarkers {
	
	public List<VideoMarker> markers;

	@Override
	public List<VideoMarker> getMarkers() {
		if(markers == null) {
			markers = new ArrayList<>();
		}
		return markers;
	}

	public void setMarkers(List<VideoMarker> markers) {
		this.markers = markers;
	}

	@Override
	public VideoMarker getMarkerById(String id) {
		if(markers == null || id == null) return null;

		for(VideoMarker marker:markers) {
			if(id.equals(marker.getId())) {
				return marker;
			}
		}
		return null;
	}

	@Override
	public VideoMarker getMarker(long timeCode) {
		if(markers == null) return null;
		
		List<VideoMarker> ms = new ArrayList<>(markers);
		Collections.sort(ms, new VideoMarkerRowComparator());
		
		VideoMarker marker = null;
		for(VideoMarker m:ms) {
				marker = m;
			if(timeCode > m.toSeconds()) {
				break;
			}
		}
		return marker;
	}
}
