package org.olat.core.util.resource;

import org.olat.core.id.OLATResourceable;

/**
 * 
 * Initial date: 2 juil. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WindowedResourceable {
	
	private final String windowId;
	private final String subIdent;
	private final OLATResourceable resource;
	
	public WindowedResourceable(String windowId, OLATResourceable resource, String subIdent) {
		this.windowId = windowId;
		this.subIdent = subIdent;
		this.resource = resource;
	}

	public String getWindowId() {
		return windowId;
	}

	public OLATResourceable getResource() {
		return resource;
	}
	
	public String getSubIdent() {
		return subIdent;
	}
	
	public boolean matchResourceOnDifferentWindow(WindowedResourceable wResource) {
		return OresHelper.equals(resource, wResource.getResource())
				&& ((subIdent == null && wResource.getSubIdent() == null) || (subIdent != null && subIdent.equals(wResource.getSubIdent())))
				&& (windowId  == null || !windowId.equals(wResource.getWindowId()));
	}
	
	@Override
	public int hashCode() {
		return windowId.hashCode() + resource.getResourceableId().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof WindowedResourceable) {
			WindowedResourceable wr = (WindowedResourceable)obj;
			return windowId != null && windowId.equals(wr.getWindowId())
					&& ((subIdent == null && wr.getSubIdent() == null) || (subIdent != null && subIdent.equals(wr.getSubIdent())))
					&& resource != null && OresHelper.equals(resource, wr.getResource());
		}
		return false;
	}
}
