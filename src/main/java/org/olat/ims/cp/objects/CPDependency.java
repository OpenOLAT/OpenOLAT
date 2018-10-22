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
import org.olat.core.logging.OLATRuntimeException;
import org.olat.ims.cp.CPCore;

/**
 * 
 * Description:<br>
 * This class represents a dependency-element of a IMS-manifest-file
 * 
 * <P>
 * Initial Date: 07.07.2008 <br>
 * 
 * @author Sergio Trentini
 */
public class CPDependency extends DefaultElement implements CPNode {

	private String identifierRef;
	private int position;
	private CPResource parent;

	/**
	 * this constructor is neeeded when building up the cp (parsing XML manifest
	 * file)
	 * 
	 * @param me
	 */
	public CPDependency(Element me) {
		super(me.getName());
		identifierRef = me.attributeValue(CPCore.IDENTIFIERREF);
		if (identifierRef.equals(null)) { throw new OLATRuntimeException(CPOrganizations.class,
				"Invalid IMS-Manifest ( <dependency> element without identifierref )", new Exception()); }
	}

	/**
	 * this constructor is needed when creating a new dependency (for adding to a
	 * resource)
	 */
	public CPDependency() {
		super(CPCore.DEPENDENCY);
		identifierRef = "";
	}

	/**
	 * this constructor is needed when creating a new dependency (for adding to a
	 * resource)
	 * 
	 * @param identifierRef the identifierRef of this dependency
	 */
	public CPDependency(String identifierRef) {
		super(CPCore.DEPENDENCY);
		this.identifierRef = identifierRef;
	}

	@Override
	public void buildDocument(Element parentEl) {
		DefaultElement depElement = new DefaultElement(CPCore.DEPENDENCY);
		depElement.addAttribute(CPCore.IDENTIFIERREF, identifierRef);
		parentEl.add(depElement);
	}

	@Override
	public boolean validateElement() {
		if (this.identifierRef == null || this.identifierRef.equals("")) { throw new OLATRuntimeException(CPOrganizations.class,
				"Invalid IMS-Manifest (missing \"identifierref\" attribute in dependency element )", new Exception()); }
		return true;
	}

	// *** Getter ***

	public String getIdentifierRef() {
		return identifierRef;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public void buildChildren() {
	// dependency has no children

	}

	@Override
	public DefaultElement getElementByIdentifier(String id) {
		// <dependency>-elements do not have an identifier and do not have
		// children...
		return null;
	}

	public CPResource getParentElement(){
		return parent;
	}
	
	// *** Setters ***

	public void setIdentifierRef(String identifierRef) {
		this.identifierRef = identifierRef;
	}

	@Override
	public void setPosition(int pos) {
		position = pos;
	}
	
	public void setParentElement(CPResource parent){
		this.parent = parent;
	}
}
