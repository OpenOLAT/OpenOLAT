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
* <p>
*/ 

package org.olat.core.gui.render;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.gui.components.ComponentRenderer;

/**
 * Initial Date:  13.10.2005 <br>
 * @author Felix Jost
 */
public class RenderingState {
	private Map renderinfo = new HashMap();
	
	/**
	 * 
	 * @param componentrenderer the classname of the renderer will be taken to generate a namespace per componentrenderer-class 
	 * @param key
	 * @param data
	 */
	public void putRenderInfo(ComponentRenderer componentrenderer, String key, Object data) {
		renderinfo.put(componentrenderer.getClass().getName()+":"+key, data);
	}
	
	/**
	 * 
	 * @param componentrenderer
	 * @param key
	 * @return the object stored for the class of the componentrenderer and the given key
	 */
	public Object getRenderInfo(ComponentRenderer componentrenderer, String key) {
		// componentrenderer is most performant by being a singleton per component class, but we rely on the class rather than on the instance
		return renderinfo.get(componentrenderer.getClass().getName()+":"+key);
	}
	
}
