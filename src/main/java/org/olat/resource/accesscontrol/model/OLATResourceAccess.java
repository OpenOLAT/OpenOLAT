package org.olat.resource.accesscontrol.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.resource.OLATResource;

public class OLATResourceAccess {
	
	private OLATResource resource;
	private List<PriceMethodBundle> methods = new ArrayList<PriceMethodBundle>(3);
	
	public OLATResourceAccess() {
		//
	}
	
	public OLATResourceAccess(OLATResource resource, Price price, AccessMethod method) {
		this.resource = resource;
		
		if(method != null) {
			this.methods.add(new PriceMethodBundle(price, method));
		}
	}

	public OLATResource getResource() {
		return resource;
	}
	
	public void setResource(OLATResource resource) {
		this.resource = resource;
	}
	
	public List<PriceMethodBundle> getMethods() {
		return methods;
	}
	
	public void addBundle(Price price, AccessMethod method) {
		if(method != null) {
			this.methods.add(new PriceMethodBundle(price, method));
		}
	}


}
