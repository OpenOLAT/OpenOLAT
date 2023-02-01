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
package org.olat.course.nodes.videotask.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.videotask.ui.VideoTaskSessionRow.CategoryColumn;
import org.olat.course.nodes.videotask.ui.components.CategoryAlphabeticalComparator;
import org.olat.course.nodes.videotask.ui.components.CategoryPresetComparator;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.VideoTaskSegmentResult;
import org.olat.modules.video.VideoTaskSegmentSelection;

/**
 * 
 * Initial date: 26 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskHelper {
	
	public static void sortCategories(List<VideoSegmentCategory> categories, CourseNode courseNode, Locale locale) {
		sortCategories(categories, courseNode.getModuleConfiguration(), locale);
	}
	
	public static void sortCategories(List<VideoSegmentCategory> categories, ModuleConfiguration config, Locale locale) {
		String sort = config.getStringValue(VideoTaskEditController.CONFIG_KEY_SORT_CATEGORIES, VideoTaskEditController.CONFIG_KEY_SORT_CATEGORIES_PRESET);
		sortCategories(categories, sort, locale);
	}
	
	public static void sortCategories(List<VideoSegmentCategory> categories, String sort, Locale locale) {
		if(VideoTaskEditController.CONFIG_KEY_SORT_CATEGORIES_ALPHABETICAL.equals(sort)) {
			categories.sort(new CategoryAlphabeticalComparator(locale));
		} else {
			categories.sort(new CategoryPresetComparator());
		}
	}
	
	public static List<VideoSegmentCategory> getSelectedCategories(VideoSegments segments, List<String> selectedCategoriesIds) {
		if(segments == null || segments.getCategories() == null || selectedCategoriesIds == null) {
			return List.of();
		}
		
		List<VideoSegmentCategory> categories = new ArrayList<>();
		for(VideoSegmentCategory category:segments.getCategories()) {
			if(selectedCategoriesIds.contains(category.getId())) {
				categories.add(category);
			}
		}
		return categories;
	}
	
	public static List<VideoSegment> getSelectedSegments(VideoSegments segments, List<String> selectedCategoriesIds) {
		if(segments == null || segments.getSegments() == null || selectedCategoriesIds == null) {
			return List.of();
		}
		
		List<VideoSegment> segmentsList = new ArrayList<>();
		for(VideoSegment segment:segments.getSegments()) {
			if(selectedCategoriesIds.contains(segment.getCategoryId())) {
				segmentsList.add(segment);
			}
		}
		return segmentsList;
	}
	
	public static int correctlyAssignedSegments(List<VideoSegment> segments, List<VideoTaskSegmentResult> results) {
		Set<String> segmentsIds = segments.stream()
				.map(VideoSegment::getId)
				.collect(Collectors.toSet());
		
		for(VideoTaskSegmentResult result:results) {
			if(result.isCorrect()) {
				segmentsIds.remove(result.getSegmentId());
			}
		}
		return segments.size() - segmentsIds.size();
	}
	
	public static int incorrectlyAssignedSegments(List<VideoSegment> segments, List<VideoTaskSegmentResult> results) {
		Set<String> segmentsIds = segments.stream()
				.map(VideoSegment::getId)
				.collect(Collectors.toSet());
		
		for(VideoTaskSegmentResult result:results) {
			if(!result.isCorrect()) {
				segmentsIds.remove(result.getSegmentId());
			}
		}
		return segments.size() - segmentsIds.size();
	}
	
	public static int notAssignedSegments(List<VideoSegment> segments, List<VideoTaskSegmentResult> results) {
		Set<String> segmentsIds = segments.stream()
				.map(VideoSegment::getId)
				.collect(Collectors.toSet());
		
		for(VideoTaskSegmentResult result:results) {
			segmentsIds.remove(result.getSegmentId());
		}
		return segmentsIds.size();
	}
	
	public static int unsuccessfulSegments(List<VideoSegment> segments, List<VideoTaskSegmentResult> results) {
		Set<String> segmentsIds = segments.stream()
				.map(VideoSegment::getId)
				.collect(Collectors.toSet());
		
		for(VideoTaskSegmentResult result:results) {
			if(result.isCorrect()) {
				segmentsIds.remove(result.getSegmentId());
			}
		}
		
		int unsuccessful = 0;
		for(VideoTaskSegmentResult result:results) {
			if(!result.isCorrect() && segmentsIds.contains(result.getSegmentId())) {
				unsuccessful++;
			}
		}
		
		return unsuccessful;
	}
	
	public static CategoryColumn[] calculateScoring(List<VideoSegmentCategory> categories, List<VideoTaskSegmentSelection> taskSelections) {
		CategoryColumn[] scoring = new CategoryColumn[categories.size()];
		for(int i=0; i<categories.size(); i++) {
			VideoSegmentCategory category = categories.get(i);
			CategoryColumn col = new CategoryColumn();
			
			int correct = 0;
			int notCorrect = 0;
			for(VideoTaskSegmentSelection selection:taskSelections) {
				if(category.getId().equals(selection.getCategoryId())) {
					if(selection.getCorrect() != null && selection.getCorrect().booleanValue()) {
						correct++;
					} else {
						notCorrect++;
					}
				}	
			}

			col.setCategory(category);
			col.setCorrect(correct);
			col.setNotCorrect(notCorrect);
			scoring[i] = col;
		}
		return scoring;
	}
	

}
