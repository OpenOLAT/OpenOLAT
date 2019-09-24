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
*/

package org.olat.ims.qti.process;

import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.core.logging.AssertException;
import org.olat.ims.qti.editor.QTIEditorPackage;

/**
 * Initial Date:  07.08.2003
 *
 * @author Mike Stock
 */
public class QTIEditorResolver implements Resolver {

	private Document doc;
	private String staticsBaseURI;
	private boolean isSurvey = false;
	
	/**
	 * @param qtiPackage
	 */
	public QTIEditorResolver(QTIEditorPackage qtiPackage) {
		doc = qtiPackage.getQTIDocument().getDocument();
		staticsBaseURI = qtiPackage.getMediaBaseURL();
	}
	
	/**
	 * @see org.olat.ims.qti.process.Resolver#getObjectBank(java.lang.String)
	 */
	public Element getObjectBank(String ident) {
		throw new AssertException("Not implemented.");
	}

	/**
	 * @see org.olat.ims.qti.process.Resolver#getQTIDocument()
	 */
	public Document getQTIDocument() { return doc; }

	/**
	 * @param ident
	 * @return Section
	 */
	public Element getSection(String ident) {
		// improve: put sections in a hashmap - see the timing difference
		Element el_section = (Element) doc.selectSingleNode("questestinterop/assessment/section[@ident='"+ident+"']");
		return el_section;
	}

	/**
	 * @param ident
	 * @return Item
	 */
	@Override
	public Element getItem(String ident) {
		// ident of item must be "globally unique"(qti...), unique within a qti document
		return (Element) doc.selectSingleNode("//item[@ident='"+ident+"']");
	}

	@Override
	public String getStaticsBaseURI() { return staticsBaseURI; }

	/**
	 * @return true when the editor is in survey mode, false when in test/selftest mode
	 */
	public boolean isSurvey(){
		return isSurvey;
	}

	@Override
	public boolean hasAutocompleteFiles() {
		return false;
	}
}
