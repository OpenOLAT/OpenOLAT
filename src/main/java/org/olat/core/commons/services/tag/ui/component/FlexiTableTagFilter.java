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
package org.olat.core.commons.services.tag.ui.component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.TagRef;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 6 Mar 2023<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FlexiTableTagFilter extends FlexiTableFilter implements FlexiTableExtendedFilter {

	private static final int MAX_EXPLANATION_LENGTH = 32;
	
	private final List<? extends TagInfo> allTags;
	
	private List<String> value;
	
	public FlexiTableTagFilter(String label, String filter, List<? extends TagInfo> allTags,
			boolean defaultVisible) {
		super(label, filter, defaultVisible);
		setDefaultVisible(defaultVisible);
		this.allTags = allTags;
	}
	
	public List<? extends TagInfo> getAllTags() {
		return allTags;
	}

	@Override
	public List<String> getValues() {
		return value;
	}

	public void setValues(List<String> value) {
		this.value = value;
	}
	
	@Override
	public void setValue(Object val) {
		this.value = convert(val);
		if(val == null) {
			this.value = null;
		} else if(val instanceof List) {
			@SuppressWarnings("unchecked")
			List<String> vals = (List<String>)val;
			this.value = new ArrayList<>(vals);
		} else {
			this.value = new ArrayList<>();
			this.value.add(val.toString());
		}
	}
	
	private static List<String> convert(Object val) {
		if(val == null) {
			return null;
		}
		if(val instanceof List) {
			@SuppressWarnings("unchecked")
			List<String> vals = (List<String>)val;
			return new ArrayList<>(vals);
		}
		List<String> list = new ArrayList<>();
		list.add(val.toString());
		return list;
	}

	@Override
	public void reset() {
		value = null;
	}
	
	public List<TagRef> getTags() {
		if(value == null) return null;
		
		return allTags.stream()
				.filter(tag -> value.contains(tag.getKey().toString()))
				.collect(Collectors.toList());
	}
	
	@Override
	public String getDecoratedLabel(boolean withHtml) {
		return getDecoratedLabel(value, withHtml);
	}

	@Override
	public List<String> getHumanReadableValues() {
		List<String> hrValues = new ArrayList<>();
		if (value != null && !value.isEmpty()) {
			allTags.stream()
				.filter(tag -> value.contains(tag.getKey().toString()))
				.map(Tag::getDisplayName)
				.collect(Collectors.toList());
		}
		return hrValues;
	}

	@Override
	public String getDecoratedLabel(Object objectValue, boolean withHtml) {
		StringBuilder label = new StringBuilder(getLabel());
		List<String> list = convert(objectValue);
		if(list != null && !list.isEmpty()) {
			List<String> displayNames = allTags.stream()
				.filter(tag -> value.contains(tag.getKey().toString()))
				.map(Tag::getDisplayName)
				.sorted()
				.collect(Collectors.toList());
			int currentLength = 0;
			for(String valForLabel:displayNames) {
				if(valForLabel != null) {
					if(currentLength == 0) {
						label.append(": ");
						if(withHtml) {
							label.append("<small>");
						}
						label.append("\"");
					} else {
						if(withHtml && currentLength + valForLabel.length() > MAX_EXPLANATION_LENGTH) {
							label.append("\u2026");
							break;
						}
						label.append(", ");
					}
					
					label.append(StringHelper.escapeHtml(valForLabel));
					currentLength += valForLabel.length();
				}
			}
			
			if(currentLength > 0) {
				label.append("\"");
				if(withHtml) {
					label.append("</small>");
				}
			}
		}
		return label.toString();
	}

	@Override
	public boolean isSelected() {
		return value != null && !value.isEmpty();
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl, Translator translator) {
		return new TagFilterSelectionController(ureq, wControl, this, getValues());
	}

	@Override
	public Controller getController(UserRequest ureq, WindowControl wControl, Translator translator, Object preselectedValue) {
		List<String> preselectedKeys = convert(preselectedValue);
		return new TagFilterSelectionController(ureq, wControl, this, preselectedKeys);
	}
}
