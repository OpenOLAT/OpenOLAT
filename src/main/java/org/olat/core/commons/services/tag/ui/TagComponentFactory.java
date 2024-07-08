/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.tag.ui;

import java.util.List;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.component.TagComponent;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.velocity.VelocityContainer;

/**
 * Initial date: Jun 26, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TagComponentFactory {

	private TagComponentFactory() {
		//
	}

	public static TagComponent createTagComponent(String name, List<TagInfo> tagInfos, VelocityContainer vc,
												  ComponentEventListener listener, boolean isRemoving) {
		TagComponent tagCmp = createTagComponent(name, tagInfos, isRemoving);
		if (listener != null) {
			tagCmp.addListener(listener);
		}
		if (vc != null) {
			vc.put(tagCmp.getComponentName(), tagCmp);
		}
		return tagCmp;
	}

	static TagComponent createTagComponent(String name, List<TagInfo> tagInfos, boolean isRemoving) {
		return new TagComponent(name, tagInfos, isRemoving);
	}

}
