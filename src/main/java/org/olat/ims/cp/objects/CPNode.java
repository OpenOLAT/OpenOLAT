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

package org.olat.ims.cp.objects;


import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;



/**
 * 
 * Description:<br>
 * All classes used in the CP-manifest-datamodel (element-classes) implement this interface...
 * 
 * <P>
 * Initial Date:  02.07.2008 <br>
 * @author Sergio Trentini
 */
public interface CPNode {

	
	/**
	 *  traverses XML-nodes and builds children-objects
	 *  this function is invoked while the CP is instantiating (parsing-process of the manifest-file)
	 */
	public void buildChildren();
	
	
	/**
	 * checks whether required attributes are set, and whether required child-elements are present
	 */
	public boolean validateElement();
	
	/**
	 * generates a DefaultElement with all its Attributes and children, and adds it to parent
	 * This Function is needed to build the DefaultDocument of the ContentPackage
	 * @param parent
	 */
	public void buildDocument(Element parent);
	

	/**
	 * 
	 * @param identifier
	 * @return
	 */
	public DefaultElement getElementByIdentifier(String id);
	
	/**
	 * 
	 * @return Returns the position of this Element
	 */
	public int getPosition();
	
	
	public void setPosition(int pos);
	
}
