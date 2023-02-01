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
package org.olat.course.nodes.videotask.model;

import org.olat.course.nodes.VideoTaskCourseNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 1 févr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskArchiveSearchParams {
	
	private RepositoryEntry entry;
	private RepositoryEntry videoEntry;
	private VideoTaskCourseNode courseNode;
	
	public VideoTaskArchiveSearchParams(RepositoryEntry entry, RepositoryEntry videoEntry,
			VideoTaskCourseNode courseNode) {
		this.entry = entry;
		this.videoEntry = videoEntry;
		this.courseNode = courseNode;
	}

	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	public RepositoryEntry getVideoEntry() {
		return videoEntry;
	}

	public void setVideoEntry(RepositoryEntry videoEntry) {
		this.videoEntry = videoEntry;
	}

	public VideoTaskCourseNode getCourseNode() {
		return courseNode;
	}

	public void setCourseNode(VideoTaskCourseNode courseNode) {
		this.courseNode = courseNode;
	}
}
