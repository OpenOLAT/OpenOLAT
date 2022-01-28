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
package org.olat.core.gui.components.segmentedview;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;

public class SegmentViewComponent extends AbstractComponent  {
	
	private boolean reselect;
	private boolean allowNoSelection;
	private boolean allowMultipleSelection;
	private boolean dontShowSingleSegment;
	
	private SegmentViewRendererType rendererType = SegmentViewRendererType.segmented;
	
	private final Set<Component> selectedSegments = new HashSet<>();
	private final List<Component> segments = new ArrayList<>();
	
	private static final LinkedViewRenderer LINKED_RENDERER = new LinkedViewRenderer();
	private static final SegmentViewRenderer SEGMENTED_RENDERER = new SegmentViewRenderer();

	public SegmentViewComponent(String name) {
		super(name);
	}
	
	public boolean isAllowMultipleSelection() {
		return allowMultipleSelection;
	}

	public void setAllowMultipleSelection(boolean allowMultipleSelection) {
		this.allowMultipleSelection = allowMultipleSelection;
	}

	public boolean isAllowNoSelection() {
		return allowNoSelection;
	}

	public void setAllowNoSelection(boolean allowNoSelection) {
		this.allowNoSelection = allowNoSelection;
	}

	public boolean isDontShowSingleSegment() {
		return dontShowSingleSegment;
	}

	public void setDontShowSingleSegment(boolean dontShowSingleSegment) {
		this.dontShowSingleSegment = dontShowSingleSegment;
	}

	public SegmentViewRendererType getRendererType() {
		return rendererType;
	}

	public void setRendererType(SegmentViewRendererType rendererType) {
		this.rendererType = rendererType;
	}

	public boolean isReselect() {
		return reselect;
	}

	/**
	 * If a segment is selectable and clicked, send
	 * a select event. Default is false.
	 * @param reselect
	 */
	public void setReselect(boolean reselect) {
		this.reselect = reselect;
	}

	@Override
	public boolean isDirty() {
		boolean dirty = super.isDirty();
		for(Component segment:segments) {
			dirty |= segment.isDirty();
		}
		return dirty;
	}

	public boolean isEmpty() {
		return segments.isEmpty();
	}
	
	public int size() {
		return segments.size();
	}
	
	public List<Component> getSegments() {
		return segments;
	}
	
	public void setSegments(List<Link> links) {
		segments.clear();
		selectedSegments.clear();
		segments.addAll(links);
		setDirty(true);
	}
	
	public void addSegment(Link link) {
		segments.add(link);
		setDirty(true);
	}
	
	public void addSegment(int index, Link link) {
		segments.add(index, link);
		setDirty(true);
	}
	
	/**
	 * This is a convenience method which add the class
	 * to draw a button.
	 * 
	 * @param link The link to add in the segment view
	 * @param selected If the segement is selected or not
	 */
	public void addSegment(Link link, boolean selected) {
		addSegment(-1, link, selected);
	}
	
	/**
	 * This is a convenience method which add the class
	 * to draw a button.
	 * 
	 * @param position Position in the segment view
	 * @param link The link to add in the segment view
	 * @param selected If the segement is selected or not
	 */
	public void addSegment(int position, Link link, boolean selected) {
		if(selected) {
			link.setCustomEnabledLinkCSS("btn btn-primary");
			link.setCustomDisabledLinkCSS("btn btn-primary");
		} else {
			link.setCustomEnabledLinkCSS("btn btn-default");
			link.setCustomDisabledLinkCSS("btn btn-default");
		}
		if(position >= 0 && position < segments.size()) {
			segments.add(position, link);
		} else {
			segments.add(link);
		}
		if(selected) {
			selectedSegments.add(link);
		} else {
			selectedSegments.remove(link);
		}
		setDirty(true);
	}
	
	public void removeSegment(String name) {
		for(Iterator<Component> it=segments.iterator(); it.hasNext(); ) {
			Component segment = it.next();
			if(name.equals(segment.getComponentName())) {
				it.remove();
				selectedSegments.remove(segment);
				setDirty(true);
			}
		}
	}
	
	public void removeSegment(Component cmp) {
		for(Iterator<Component> it=segments.iterator(); it.hasNext(); ) {
			Component segment = it.next();
			if(cmp == segment) {
				it.remove();
				selectedSegments.remove(segment);
				setDirty(true);
			}
		}
	}
	
	public boolean isSelected(Component component) {
		return selectedSegments.contains(component);
	}
	
	public void select(Component component) {
		if(segments.contains(component)) {
			if(!isAllowMultipleSelection()) {
				deselectAllSegments();
			}
			selectSegment(component);
			setDirty(true);
		}
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		Event e = null;
		String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);
		int count = 0;
		for(Component segment:segments) {
			if(segment.getComponentName().equals(cmd)) {
				boolean selected = selectedSegments.contains(segment);
				if(selected) {
					if(isAllowNoSelection() || selectedSegments.size() > 1) {
						e = new SegmentViewEvent(SegmentViewEvent.DESELECTION_EVENT, segment.getComponentName(), count);
						selectedSegments.remove(segment);
					} else if(isReselect()) {
						e = new SegmentViewEvent(SegmentViewEvent.SELECTION_EVENT, segment.getComponentName(), count);
					}
				} else {
					if(!isAllowMultipleSelection()) {
						deselectAllSegments();
					}
					e = new SegmentViewEvent(SegmentViewEvent.SELECTION_EVENT, segment.getComponentName(), count);
					selectSegment(segment);
				}
				break;
			}
			count++;
		}
		
		if(e != null) {
			setDirty(true);
			fireEvent(ureq, e);
		}
	}
	
	public Component getSelectedComponent() {
		if(selectedSegments.size() == 1) {
			return selectedSegments.iterator().next();
		}
		return null;
	}
	
	private void selectSegment(Component segment) {
		if(segment instanceof Link) {
			((Link)segment).setCustomEnabledLinkCSS("btn btn-primary");
		}
		selectedSegments.add(segment);
	}
	
	private void deselectAllSegments() {
		for(Component segment:selectedSegments) {
			if(segment instanceof Link) {
				((Link)segment).setCustomEnabledLinkCSS("btn btn-default");
			}
		}
		selectedSegments.clear();
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		if(rendererType == SegmentViewRendererType.linked) {
			return LINKED_RENDERER ;
		}
		return SEGMENTED_RENDERER;
	}
}
