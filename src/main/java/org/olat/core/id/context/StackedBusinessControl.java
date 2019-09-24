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
* Initial code contributed and copyrighted by<br>
* JGS goodsolutions GmbH, http://www.goodsolutions.ch
* <p>
*/
package org.olat.core.id.context;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.olat.core.logging.AssertException;



/**
 * Description:<br>
 * Part of a linked list of (Stacked)BusinessControls which represent the BusinessPath for a certain
 * created Controller or Controllerstate.
 * <P>
 * Initial Date:  14.06.2006 <br>
 *
 * @author Felix Jost
 */
public class StackedBusinessControl implements BusinessControl {

	private final BusinessControl origBusinessControl;
	private final ContextEntry contextEntry;
	private ContextEntry currentCe;
	private List<ContextEntry> businessControls;

	public List<ContextEntry> getBusinessControls() {
		return businessControls;
	}
	
	public List<ContextEntry> getContextEntryStack() {
		if(contextEntry == null) return null;
		List<ContextEntry> list = null;
		if (origBusinessControl!=null) {
			if (origBusinessControl instanceof StackedBusinessControl) {
				StackedBusinessControl parent = (StackedBusinessControl)origBusinessControl;
				list = parent.getContextEntryStack();
			}
		}
		if (list==null) {
			list = new LinkedList<>();
		}
		list.add(contextEntry);
		return list;
	}
	
	
	/**
	 * internal use only! REVIEW:PB:2009-31-05: better way?
	 * @param contextEntry
	 * @param origBusinessControl
	 */
	public StackedBusinessControl(ContextEntry contextEntry, BusinessControl origBusinessControl) {
		this.contextEntry = contextEntry;
		this.origBusinessControl = origBusinessControl;
		if(contextEntry != null){
			setCurrentContextEntry(contextEntry);
		}
	}

	@Override
	public String toString() {
		return getAsString(); 
	}
	
	@Override
	public String getAsString() {
		if(contextEntry == null || contextEntry.getOLATResourceable() == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(64);
		if(origBusinessControl != null) {
			sb.append(origBusinessControl.getAsString());
		}
		Long key = contextEntry.getOLATResourceable().getResourceableId();
		sb.append("[").append(contextEntry.getOLATResourceable().getResourceableTypeName());
		if(key != null) {
			sb.append(":").append(key.longValue());
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public List<ContextEntry> getEntries() {
		List<ContextEntry> entries = new ArrayList<>();
		if(origBusinessControl != null) {
			entries.addAll(origBusinessControl.getEntries());
		}
		if(contextEntry != null) {
			entries.add(contextEntry);
		}
		return entries;
	}

	@Override
	public List<ContextEntry> getEntriesDownTheControls() {
		return getEntries();
	}

	@Override
	public ContextEntry popLauncherContextEntry() {
		ContextEntry currentToSpawn = popInternalLaucherContextEntry();
		
		if(currentToSpawn != null){
			//- in non user click mode or spawn mode, e.g. come from search or REST/JumpInURI
			//- automatically register each ContextEntry along the path again.
			//- at the end the last controller is spawned and further clicks of the user can be
			//..recorded and later generated business paths are correct.
			setCurrentContextEntry(currentToSpawn);
		}
		
		return currentToSpawn;
	}

	ContextEntry popInternalLaucherContextEntry(){
		if (origBusinessControl instanceof StackedBusinessControl) {
			StackedBusinessControl sbc = (StackedBusinessControl) origBusinessControl;
			return sbc.popInternalLaucherContextEntry();
		}else{
			return origBusinessControl == null ? null : origBusinessControl.popLauncherContextEntry();
		}
	}
	
	@Override
	public void dropLauncherEntries() {
		origBusinessControl.dropLauncherEntries();
	}

	@Override
	public boolean hasContextEntry() {
		return origBusinessControl.hasContextEntry();
	}

	@Override
	public ContextEntry getCurrentContextEntry() {
		return currentCe;
	}

	public void setCurrentContextEntry(ContextEntry ce) {
		if(ce == null) throw new AssertException("ContextEntry can not be null!");
		this.currentCe = ce;
		List<ContextEntry>	ces = new ArrayList<>();
		notifyParent(ces);
	}

	/**
	 * package visible only! internas of StackedBusinessControl!
	 * @param ces
	 */
	void notifyParent(List<ContextEntry> ces) {
		if(origBusinessControl == null){
			//recursion end - we are root StackedBusinesControl
			ces.add(currentCe);
			this.businessControls = ces;
		}else{
			if(origBusinessControl instanceof StackedBusinessControl){
				StackedBusinessControl sbc = (StackedBusinessControl)origBusinessControl;
				ces.add(currentCe);
				sbc.notifyParent(ces);
			}else{
				//EMPTY Sentinel is a BusinessControl only 
				this.businessControls = ces;
			}
		}
	}
}