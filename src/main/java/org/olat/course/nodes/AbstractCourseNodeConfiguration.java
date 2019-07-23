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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.configuration.AbstractConfigOnOff;


/**
 * Common class for all CourseNodeConfigratiuon classes. 
 * @author guretzki
 */
public abstract class AbstractCourseNodeConfiguration extends AbstractConfigOnOff implements CourseNodeConfiguration {
	
	private int order = 0;
	private boolean configDeprecated = false;

	private List<String> alternatives;
	
	public AbstractCourseNodeConfiguration() {
		super();
	}
	
	public void setOrder(int order) {
		this.order = order;
	}
	
	@Override
	public int getOrder() {
		return order;
	}

	public void setDeprecated(boolean configDeprecated) {
		this.configDeprecated = configDeprecated;
	}

	@Override
	public boolean isDeprecated() {
		return configDeprecated;
	}

	@Override
	public List<String> getAlternativeCourseNodes() {
		if(alternatives == null) {
			return Collections.emptyList();
		}
		return alternatives;
	}
	
	public void setAlternativeCourseNodes(List<String> alternatives) {
		if(alternatives == null || alternatives.isEmpty()) {
			this.alternatives = null;
		} else {
			this.alternatives = new ArrayList<>(alternatives);
		}
	}
}