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

package org.olat.core.gui.components;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.util.component.ComponentTraverser;
import org.olat.core.util.component.ComponentVisitor;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class ComponentHelper {
	
	
	public static List<Component> findAncestorsOrSelfByID(Component startFrom, Component target) {
		List<Component> ancestors = new ArrayList<>();
		dofindAncestors(startFrom, target, ancestors);
		return ancestors;
	}
	
	private static boolean dofindAncestors(Component current, Component target, List<Component> ancestors) {
		if (target == current) {
			ancestors.add(target);
			return true;
		}
		if (current instanceof ComponentCollection) {
			ComponentCollection co = (ComponentCollection) current;
			for (Component child : co.getComponents()) {
				boolean found = dofindAncestors(child, target, ancestors);
				if (found) {
					ancestors.add(current);
					return found;
				}
			}
		}
		return false;
	}
	
	/**
	 * @param startFrom
	 * @param id
	 * @param foundPath
	 * @return
	 */
	public static Component findDescendantOrSelfByID(Component startFrom, String id, List<Component> foundPath) {
		return dofind(startFrom, id, foundPath);
	}

	private static Component dofind(Component current, String id, List<Component> foundPath) {
		if (id.equals(current.getDispatchID())) return current;
		if (current instanceof ComponentCollection) {
			ComponentCollection co = (ComponentCollection)current;
			for (Component child : co.getComponents()) {
				Component found = dofind(child, id, foundPath);
				if (found != null) {
					foundPath.add(child);
					return found;
				}
			}
		}
		return null;
	}

	/**
	 * @param ureq
	 * @param top
	 * @param vr
	 */
	public static void validateComponentTree(UserRequest ureq, ComponentCollection top, ValidationResult vr) {
		doValidate(ureq, top, vr);
	}

	/**
	 * validates all the visible components
	 * 
	 * @param vr
	 */
	private static void doValidate(UserRequest ureq, Component current, ValidationResult vr) {
		if (!current.isVisible()) return; // invisible components are not validated,
		// since they are not displayed
		current.validate(ureq, vr);
		if (current instanceof ComponentCollection) { // visit children
			ComponentCollection co = (ComponentCollection)current;
			for (Component child : co.getComponents()) {
				doValidate(ureq, child, vr);
			}
		}
	}
	
	/**
	 * REVIEW:pb: 2008-05-05 -> not referenced from .html or .java
	 * @param wins
	 * @param compToFind
	 * @return
	 * @deprecated
	 */
	protected static Window findWindowWithComponentInIt(Windows wins, final Component compToFind) {
		Window awin = null;
		for (Iterator<Window> it_wins = wins.getWindowIterator(); it_wins.hasNext();) {
			awin = it_wins.next();

			// find the correct component within the window
			MyVisitor v = new MyVisitor(compToFind);
			ComponentTraverser ct = new ComponentTraverser(v, awin.getContentPane(), false);
			ct.visitAll(null);
			if (v.f != null) return awin;

		}
		return null;
	}
	static class MyVisitor implements ComponentVisitor {
		private Component compToFind;
		private Component f = null;

		public MyVisitor(Component compToFind) {
			this.compToFind = compToFind;
		}

		@Override
		public boolean visit(Component comp, UserRequest ureq) {
			if (comp == compToFind) {
				f = comp;
				return false;
			}
			return true;
		}
	}
}