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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.vitero.manager;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMContainer;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMXMLParserWrapper;


public class EmptyOMElement implements OMElement {
	
	private static final Iterator<Object> EMPTY_ITERATOR = new EmptyIterator();

	@Override
	public void addChild(OMNode omNode) {
		//
	}

	@Override 
	public Iterator<?> getChildrenWithName(QName elementQName) {
		return EMPTY_ITERATOR;
	}

	@Override
	public Iterator<?> getChildrenWithLocalName(String localName) {
		return EMPTY_ITERATOR;
	}

	@Override
	public Iterator<?> getChildrenWithNamespaceURI(String uri) {
		return EMPTY_ITERATOR;
	}

	@Override
	public OMElement getFirstChildWithName(QName elementQName) throws OMException {
		return null;
	}

	@Override
	public Iterator<?> getChildren() {
		return EMPTY_ITERATOR;
	}

	@Override
	public OMNode getFirstOMChild() {
		return null;
	}

	@Override
	public void buildNext() {
		//
	}

	@Override
	public OMContainer getParent() {
		return null;
	}

	@Override
	public OMNode getNextOMSibling() throws OMException {
		return null;
	}

	@Override
	public boolean isComplete() {
		return false;
	}

	@Override
	public OMNode detach() throws OMException {
		return null;
	}

	@Override
	public void discard() throws OMException {
		//
	}

	@Override
	public void insertSiblingAfter(OMNode sibling) throws OMException {
		//
	}

	@Override
	public void insertSiblingBefore(OMNode sibling) throws OMException {
		//
	}

	@Override
	public int getType() {
		return 0;
	}

	@Override
	public OMNode getPreviousOMSibling() {
		return null;
	}

	@Override
	public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
		//
	}

	@Override
	public void serialize(OutputStream output) throws XMLStreamException {
		//
	}

	@Override
	public void serialize(Writer writer) throws XMLStreamException {
		//
	}

	@Override
	public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {
		//
	}

	@Override
	public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
		//
	}

	@Override
	public void serializeAndConsume(XMLStreamWriter xmlWriter) throws XMLStreamException {
		//
	}

	@Override
	public void serializeAndConsume(OutputStream output) throws XMLStreamException {
		//
	}

	@Override
	public void serializeAndConsume(Writer writer) throws XMLStreamException {
		//
	}

	@Override
	public void serializeAndConsume(OutputStream output, OMOutputFormat format) throws XMLStreamException {
		//
	}

	@Override
	public void serializeAndConsume(Writer writer, OMOutputFormat format) throws XMLStreamException {
		//
	}

	@Override
	public void build() {
		//
	}

	@Override
	public void buildWithAttachments() {
		//
	}

	@Override
	public void close(boolean build) {
		//
	}

	@Override
	public OMFactory getOMFactory() {
		return null;
	}

	@Override
	public Iterator<?> getChildElements() {
		return EMPTY_ITERATOR;
	}

	@Override
	public OMNamespace declareNamespace(String uri, String prefix) {
		return null;
	}

	@Override
	public OMNamespace declareDefaultNamespace(String uri) {
		return null;
	}

	@Override
	public OMNamespace getDefaultNamespace() {
		return null;
	}

	@Override
	public OMNamespace declareNamespace(OMNamespace namespace) {
		return null;
	}

	@Override
	public OMNamespace findNamespace(String uri, String prefix) {
		return null;
	}

	@Override
	public OMNamespace findNamespaceURI(String prefix) {
		return null;
	}

	@Override
	public Iterator<?> getAllDeclaredNamespaces() throws OMException {
		return EMPTY_ITERATOR;
	}

	@Override
	public Iterator<?> getAllAttributes() {
		return EMPTY_ITERATOR;
	}

	@Override
	public OMAttribute getAttribute(QName qname) {
		return null;
	}

	@Override
	public String getAttributeValue(QName qname) {
		return null;
	}

	@Override
	public OMAttribute addAttribute(OMAttribute attr) {
		return null;
	}

	@Override
	public OMAttribute addAttribute(String attributeName, String value, OMNamespace ns) {
		return null;
	}

	@Override
	public void removeAttribute(OMAttribute attr) {
		//
	}

	@Override
	public void setBuilder(OMXMLParserWrapper wrapper) {
		//
	}

	@Override
	public OMXMLParserWrapper getBuilder() {
		return null;
	}

	@Override
	public void setFirstChild(OMNode node) {
		//
	}

	@Override
	public OMElement getFirstElement() {
		return null;
	}

	@Override
	public XMLStreamReader getXMLStreamReader() {
		return null;
	}

	@Override
	public XMLStreamReader getXMLStreamReaderWithoutCaching() {
		return null;
	}

	@Override
	public void setText(String text) {
		//
	}

	@Override
	public void setText(QName text) {
		//
	}

	@Override
	public String getText() {
		return null;
	}

	@Override
	public QName getTextAsQName() {
		return null;
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public void setLocalName(String localName) {
		//
	}

	@Override
	public OMNamespace getNamespace() throws OMException {
		return null;
	}

	@Override
	public void setNamespace(OMNamespace namespace) {
		//
	}

	@Override
	public void setNamespaceWithNoFindInCurrentScope(OMNamespace namespace) {
		//
	}

	@Override
	public QName getQName() {
		return null;
	}

	@Override
	public String toStringWithConsume() throws XMLStreamException {
		return null;
	}

	@Override
	public QName resolveQName(String qname) {
		return null;
	}

	@Override
	public OMElement cloneOMElement() {
		return null;
	}

	@Override
	public void setLineNumber(int lineNumber) {
		//
	}

	@Override
	public int getLineNumber() {
		return 0;
	}
	
    static class EmptyIterator implements Iterator<Object> {
        public boolean hasNext() {
            return false;
        }
        public Object next() {
            throw new NoSuchElementException();
        }
        public void remove() {
            throw new IllegalStateException();
        }
    }
}