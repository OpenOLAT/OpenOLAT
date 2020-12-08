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
package org.olat.core.util.resource;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.Window;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 2 juil. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WindowedResourceableList {
	
	private static final Logger log = Tracing.createLoggerFor(WindowedResourceableList.class);

	private final Deque<WindowedResourceable> registeredResources = new ArrayDeque<>();
	
	public synchronized boolean registerResourceable(OLATResourceable resource, String subIdent, Window window) {
		WindowedResourceable wResource = new WindowedResourceable(window.getInstanceId(), resource, subIdent);

		boolean uniqueResource = true;

		boolean add = true;
		for(WindowedResourceable registeredResource:registeredResources) {
			if(registeredResource.equals(wResource)) {
				uniqueResource = false;
				add = false;
				break;
			} else if(registeredResource.matchResourceOnDifferentWindow(wResource)) {
				uniqueResource = false;
			}
		}
		
		if(add) {
			registeredResources.add(wResource);
		}

		return uniqueResource;
	}
	
	public synchronized void deregisterResourceable(OLATResourceable resource, String subIdent, Window window) {
		try {
			WindowedResourceable wResource = new WindowedResourceable(window.getInstanceId(), resource, subIdent);
			Collection<WindowedResourceable> wResources = Collections.singletonList(wResource);
			registeredResources.removeAll(wResources);
		} catch (Exception e) {
			log.error("", e);
		}
	}
}
