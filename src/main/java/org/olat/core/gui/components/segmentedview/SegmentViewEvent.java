/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.core.gui.components.segmentedview;

import org.olat.core.gui.control.Event;

public class SegmentViewEvent extends Event {
	
	private static final long serialVersionUID = 7899365979427801298L;
	
	public static final String SELECTION_EVENT = "seg-view-selection";
	public static final String DESELECTION_EVENT = "seg-view-deselection";
	
	private final String componentName;
	private final int index;
	
	public SegmentViewEvent(String name, String componentName, int index) {
		super(name);
		this.componentName = componentName;
		this.index = index;
	}

	public String getComponentName() {
		return componentName;
	}

	public int getIndex() {
		return index;
	}
}
