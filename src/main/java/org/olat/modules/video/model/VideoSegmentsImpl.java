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
import java.util.List;
import java.util.Optional;

import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;

/**
 * Initial date: 2022-12-19<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class VideoSegmentsImpl implements VideoSegments {
	private final List<VideoSegment> segments = new ArrayList<>();
	private final List<VideoSegmentCategory> categories = new ArrayList<>();

	@Override
	public List<VideoSegment> getSegments() {
		return segments;
	}

	@Override
	public List<VideoSegmentCategory> getCategories() {
		return categories;
	}

	@Override
	public Optional<VideoSegmentCategory> getCategory(String categoryId) {
		return categories.stream().filter((c) -> categoryId.equals(c.getId())).findFirst();
	}

	@Override
	public Optional<VideoSegment> getSegment(String videoSegmentId) {
		return segments.stream().filter((s) -> videoSegmentId.equals(s.getId())).findFirst();
	}
}
