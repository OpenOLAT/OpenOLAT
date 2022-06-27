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

package org.olat.modules.cp;

import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * Description:<br>
 * SAX handler for the HtmlParser
 * 
 * <P>
 * Initial Date:  18 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class HtmlPageHandler extends DefaultHandler {
	private final StringBuilder header = new StringBuilder(4096);
	private final StringBuilder body = new StringBuilder(8192);
	
	private static final String HEAD = "head";
	private static final String BODY = "body";
	private static final String SCRIPT = "script";
	
	private boolean pauseOutput;
	private StringBuilder output;
	private String relativePath;
	
	private final TreeNode node;
	private final String baseUri;
	private final VFSLeaf document;
	private final VFSContainer rootContainer;
	
	public HtmlPageHandler(TreeNode node, VFSLeaf document, VFSContainer rootDir, String baseUri) {
		this.document = document;
		this.rootContainer = rootDir;
		this.baseUri = baseUri;
		this.node = node;
	}
	
	public boolean isEmpty() {
		return document == null;
	}
	
	public String getTitle() {
		return node.getTitle();
	}
	
	public int getLevel() {
		int count = 0;
		INode currentNode = node;
		while(currentNode != null) {
			currentNode = currentNode.getParent();
			count++;
		}
		return count;
	}

	public VFSLeaf getDocument() {
		return document;
	}

	public StringBuilder getHeader() {
		return header;
	}
	
	public StringBuilder getBody() {
		return body;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	@Override
	public final void startDocument() {
		//
	}

	@Override
	public final void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (HEAD.equalsIgnoreCase(localName)) {
			output = header;
		} else if (BODY.equalsIgnoreCase(localName)) {
			output = body;
		} else if (SCRIPT.equalsIgnoreCase(localName)) {
			pauseOutput = true;
		} else if (output != null) {
			pauseOutput = false;
			output.append("<").append(localName);
			int numOfAttributes = attributes.getLength();
			for(int i=0; i<numOfAttributes; i++) {
				String attrName = attributes.getLocalName(i);
				String attrValue = attributes.getValue(i);
				output.append(' ').append(attrName).append('=');
				boolean useSingle = attrValue.indexOf('"') > 0;
				if(useSingle) {
					output.append('\'');
				} else {
					output.append('"');
				}
				
				if(attrName.equalsIgnoreCase("href") || attrName.equalsIgnoreCase("src")) {
					output.append(normalizeUri(attrValue));
				} else {
					output.append(attrValue);
				}
				
				if(useSingle) {
					output.append('\'');
				} else {
					output.append('"');
				}
			}
			output.append(">");
		}
	}
	
	private final String normalizeUri(String uri) {
		if(uri.indexOf("://") > 0 || uri.startsWith("/") || uri.startsWith("data:")) {
			return uri;//absolute link or image data uri, nothing to do
		}
		
		String contextPath = WebappHelper.getServletContextPath();
		if(StringHelper.containsNonWhitespace(contextPath) && uri.startsWith(contextPath)) {
			return uri;//absolute within olat
		}
		
		if(uri.startsWith("..")) {
			VFSContainer startDir;
			if(relativePath == null) {
				startDir = rootContainer;
			} else {
				startDir = (VFSContainer)rootContainer.resolve(relativePath);
			}
			
			String tmpUri = uri;
			VFSContainer tmpDir = startDir;
			while(tmpUri.startsWith("../") && tmpDir != null) {
				tmpDir = tmpDir.getParentContainer();
				tmpUri = tmpUri.substring(3);
			}
			if (tmpDir == null) {
				// no local file uri, return unchanged
				return uri;
			}
			
			String diffPath = getRelativeResultingPath(tmpDir);
			if(StringHelper.containsNonWhitespace(diffPath)) {
				return diffPath + tmpUri;
			}
			return tmpUri;
		}
		if (relativePath != null) {
			uri = relativePath + uri;
		}
		return baseUri + "/" + uri;
	}
	
	private String getRelativeResultingPath(VFSContainer tmpDir) {
		String diffPath = "";
		while(!tmpDir.isSame(rootContainer)) {
			diffPath = tmpDir.getName() + "/" + diffPath;
			tmpDir = tmpDir.getParentContainer();
		}
		return diffPath;
	}
	
	@Override
	public final void characters(char[] ch, int start, int length) {
		if(output != null && !pauseOutput) {
			output.append(ch, start, length);
		}
	}
	
	@Override
	public final void endElement(String uri, String localName, String qName) {
		if(HEAD.equals(localName)) {
			output = null;
		} else if (BODY.equals(localName)) {
			output = null;
		} else if (SCRIPT.equals(localName)) {
			pauseOutput = false;
		} else if (output != null) {
			output.append("</").append(localName).append(">");
		}
	}
}