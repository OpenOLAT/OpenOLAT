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
package org.olat.course.nodes.videotask.ui.components;

import java.util.Comparator;
import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.modules.video.VideoTaskSession;

/**
 * 
 * Initial date: 31 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskSessionComparator implements Comparator<VideoTaskSession> {
	
	private final boolean checkCancelled;
	
	public VideoTaskSessionComparator(boolean checkCancelled) {
		this.checkCancelled = checkCancelled;
	}

	@Override
	public int compare(VideoTaskSession v1, VideoTaskSession v2) {
		int c = 0;
		
		Identity id1 = v1.getIdentity();
		Identity id2 = v2.getIdentity();
		if(id1 == null && id2 == null) {
			c = v1.getAnonymousIdentifier().compareTo(v2.getAnonymousIdentifier());
		} else if(id1 == null) {
			c = 1;
		} else if(id2 == null) {
			c = -1;
		} else {
			c = id1.getKey().compareTo(id2.getKey());
		}

		if(c == 0 && checkCancelled) {
			boolean c1 = !v1.isCancelled();
			boolean c2 = !v2.isCancelled();
			if(c1 && !c2) {
				c = 1;
			} else if(!c1 && c2) {
				c = -1;
			}
		}
		
		if(c == 0) {
			Date t1 = getValidDate(v1);
			Date t2 = getValidDate(v2);
			if(t1 == null && t2 == null) {
				c = 0;
			} else if(t2 == null) {
				c = 1;
			} else if(t1 == null) {
				c = -1;
			} else {
				c = t1.compareTo(t2);
			}
		}
		
		if(c == 0) {
			c = v1.getKey().compareTo(v2.getKey());
		}

		return -c;
	}
	
	private final Date getValidDate(VideoTaskSession a) {
		if(checkCancelled && a.isCancelled()) {
			return null;
		}
		
		Date t = a.getFinishTime();
		if(t == null) {
			t = a.getCreationDate();
		}
		return t;
	}
}