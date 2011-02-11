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

import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;

public class SegmentViewFactory {

	public static SegmentViewComponent createSegmentView(String name, VelocityContainer vc, Controller listeningController) {
		SegmentViewComponent component = new SegmentViewComponent(name);
		component.setAllowMultipleSelection(false);
		component.setAllowNoSelection(false);
		if(listeningController != null) {
			component.addListener(listeningController);
		}
		if(vc != null) {
			vc.put(component.getComponentName(), component);
		}
		return component;
	}
}
