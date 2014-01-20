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

package org.olat.core.gui.components.tree;

import org.olat.core.util.memento.Memento;
import org.olat.core.util.memento.MementoOriginator;

/**
 * Description:<br>
 * Initial Date:  Nov 25, 2005 <br>
 *
 * @author patrick
 */
public abstract class GenericMementoTreeNode extends GenericTreeNode implements MementoOriginator{

	private static final long serialVersionUID = -1898272676202191778L;
	/**
	 * 
	 * @param title
	 * @param userObject
	 */
	public GenericMementoTreeNode(String title, Object userObject) {
		super(title, userObject);
	}

	public GenericMementoTreeNode() {
		super();
	}
	
	public GenericMementoTreeNode(String ident) {
		super(ident);
	}
	
	/**
	 * 
	 * @see org.olat.core.util.memento.MementoOriginator#createMemento()
	 */
	 public abstract Memento createMemento();
	/**
	 * 
	 * @see org.olat.core.util.memento.MementoOriginator#setMemento(org.olat.core.util.memento.Memento)
	 */
	 public abstract void setMemento(Memento state);

}
