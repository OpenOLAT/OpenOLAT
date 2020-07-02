package org.olat.core.util.resource;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;

import org.olat.core.gui.components.Window;
import org.olat.core.id.OLATResourceable;

/**
 * 
 * Initial date: 2 juil. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WindowedResourceableList {

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
		WindowedResourceable wResource = new WindowedResourceable(window.getInstanceId(), resource, subIdent);
		Collection<WindowedResourceable> wResources = Collections.singletonList(wResource);
		registeredResources.removeAll(wResources);
	}

}
