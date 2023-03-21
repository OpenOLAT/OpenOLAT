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
package org.olat.modules.video.ui.editor;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;

/**
 * Initial date: 2023-03-20<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class SegmentCategoryController extends FormBasicController {
	private SelectionValues categoriesKV;
	private final VideoSegment segment;
	private final VideoSegments segments;

	protected SegmentCategoryController(UserRequest ureq, WindowControl wControl, VideoSegments segments,
										VideoSegment segment) {
		super(ureq, wControl, "segment_category");
		this.segments = segments;
		this.segment = segment;

		categoriesKV = new SelectionValues();

		setValues();

		initForm(ureq);
	}

	private void setValues() {
		if (segment == null) {
			return;
		}

		categoriesKV = new SelectionValues();
		for (VideoSegmentCategory category : segments.getCategories()) {
			categoriesKV.add(SelectionValues.entry(category.getId(), category.getLabelAndTitle()));
		}

		flc.contextPut("categoriesAvailable", !categoriesKV.isEmpty());
		flc.contextPut("categories", segments.getCategories());
		flc.contextPut("categoryUsedCounts", getCategoryUsedCounts());
		flc.contextPut("categoryId", segment.getCategoryId());
	}

	private Map<String, Integer> getCategoryUsedCounts() {
		Map<String, Integer> result = new HashMap<>();
		segments.getSegments().stream().map(VideoSegment::getCategoryId)
				.forEach(cid -> result.put(cid, result.containsKey(cid) ? result.get(cid) + 1 : 1));
		segments.getCategories().stream().map(VideoSegmentCategory::getId)
				.forEach(cid -> {
					if (!result.containsKey(cid)) {
						result.put(cid, 0);
					}
				});
		return result;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (flc == source) {
			if ("ONCLICK".equals(event.getCommand())) {
				String categoryId = ureq.getParameter("categoryId");
				if (categoryId != null) {
					doSetCategory(ureq, categoryId);
				}
			}
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}


	private void doSetCategory(UserRequest ureq, String categoryId) {
		flc.contextPut("categoryId", categoryId);

		if (segment == null) {
			return;
		}
		segment.setCategoryId(categoryId);
		setValues();

		fireEvent(ureq, Event.DONE_EVENT);
	}
}
