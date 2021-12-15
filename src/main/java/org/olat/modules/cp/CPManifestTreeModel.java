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

package org.olat.modules.cp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XMLParser;
import org.olat.ims.resources.IMSEntityResolver;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
*  Description:<br>
* @author Felix Jost
*/
public class CPManifestTreeModel extends GenericTreeModel {

	private static final long serialVersionUID = 9216107936843069562L;
	private static final Logger log = Tracing.createLoggerFor(CPManifestTreeModel.class);
	
	private Element rootElement;
	private final Map<String,String>	nsuris = new HashMap<>(2);
	private final Map<String,TreeNode> hrefToTreeNode = new HashMap<>();
	private Map<String,String> resources; // keys: resource att 'identifier'; values: resource att 'href'
	private final List<TreeNode> treeNodes = new ArrayList<>();
	private final String identPrefix;
	private final CPAssessmentProvider cpAssessmentProvider;

	/**
	 * Constructor of the content packaging tree model
	 * @param manifest the imsmanifest.xml file
	 * @param cpAssessmentProvider 
	 */
	CPManifestTreeModel(VFSLeaf manifest, String identPrefix, CPAssessmentProvider cpAssessmentProvider) throws IOException {
		this.identPrefix = identPrefix;
		this.cpAssessmentProvider = cpAssessmentProvider;
		Document doc = loadDocument(manifest);
		initDocument(doc);
	}
	
	CPManifestTreeModel(String manifest,  String identPrefix) throws IOException {
		this.identPrefix = identPrefix;
		this.cpAssessmentProvider = DryRunAssessmentProvider.create();
		Document doc = loadDocument(manifest);
		initDocument(doc);
	}
	
	private void initDocument(Document doc) {
		// get all organization elements. need to set namespace
		rootElement = doc.getRootElement();
		String nsuri = rootElement.getNamespace().getURI();
		nsuris.put( "ns", nsuri);

		XPath meta = rootElement.createXPath("//ns:organization");
		meta.setNamespaceURIs(nsuris);
		Element orgaEl = (Element) meta.selectSingleNode(rootElement);
		if (orgaEl == null) throw new AssertException("could not find element organization");

		XPath metares = rootElement.createXPath("//ns:resources");
		metares.setNamespaceURIs(nsuris);
		Element elResources = (Element) metares.selectSingleNode(rootElement);
		if (elResources == null) throw new AssertException("could not find element resources");
		
		List<Element> resourcesList = elResources.elements("resource");
		resources = new HashMap<>(resourcesList.size());
		for (Iterator<Element> iter = resourcesList.iterator(); iter.hasNext();) {
			Element elRes = iter.next();
			String identVal = elRes.attributeValue("identifier");
			String hrefVal = elRes.attributeValue("href");
			if (hrefVal != null) { // href is optional element for resource element
				try {
					hrefVal = URLDecoder.decode(hrefVal, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// each JVM must implement UTF-8
				} 
			}
			resources.put(identVal, hrefVal);
		}
		GenericTreeNode gtn = buildNode(orgaEl);
		setRootNode(gtn);
		rootElement = null; // help gc
		resources = null;
	}
	
	/**
	 * @param href
	 * @return TreeNode representing this href
	 */
	public TreeNode lookupTreeNodeByHref(String href) {
		return hrefToTreeNode.get(href);
	}
	
	public List<TreeNode> getFlattedTree() {
		return new ArrayList<>(treeNodes);
	}
	
	public TreeNode getNextNodeWithContent(TreeNode node) {
		if(node == null) return null;
		int index = treeNodes.indexOf(node);
		
		for(int i=index+1; i<treeNodes.size(); i++) {
			TreeNode nextNode = treeNodes.get(i);
			if(nextNode.getUserObject() != null) {
				return nextNode;
			}
		}
		
		return null;
	}
	
	public TreeNode getPreviousNodeWithContent(TreeNode node) {
		if(node == null) return null;
		int index = treeNodes.indexOf(node);
		
		for(int i=index; i-->0; ) {
			TreeNode nextNode = treeNodes.get(i);
			if(nextNode.getUserObject() != null) {
				return nextNode;
			}
		}

		return null;
	}

	private GenericTreeNode buildNode(Element item) {
		GenericTreeNode gtn = new GenericTreeNode();
		//fxdiff VCRP-13: cp navigation
		treeNodes.add(gtn);

		// extract title
		String title = item.elementText("title");
		if (title == null) title = item.attributeValue("identifier");
		String identifier = item.attributeValue("identifier");
		gtn.setAltText(title);
		gtn.setTitle(title);
		
		if (item.getName().equals("organization")) {
			// Add first level item for organization
			gtn.setIconCssClass("o_cp_org");
			gtn.setAccessible(false);

			// Special case check: CP with only one page: hide the page and show it directly under the organization element
			List<Element> chds = item.elements("item");
			if (chds.size() == 1) {
				// check 1: only one child
				Element childitem = chds.get(0);
				List<Element> grandChds = childitem.elements("item");
				if (grandChds.size() == 0) {
					// check 2: no grand children
					String identifierref = childitem.attributeValue("identifierref");
					String href = resources.get(identifierref);
					if (href != null) {
						// check 3: a resource is attached to the child
						// = success, we have a CP with only one page. Use this page and exit
						XPath meta = rootElement.createXPath("//ns:resource[@identifier='" + identifierref + "']");
						meta.setNamespaceURIs(nsuris);
						gtn.setAccessible(true);
						gtn.setUserObject(new UserObject(identifier, href));
						if (hrefToTreeNode.containsKey(href)){
							log.debug("Duplicate href::" + href + " for identifierref::" + identifierref + " and identifier::" + identifier + ", use first one");
						} else {					
							hrefToTreeNode.put(href, gtn);
						}
						return gtn;
					} 
				}				
			}
		} else if (item.getName().equals("item")) {
			gtn.setIconCssClass("o_cp_item");
			if (cpAssessmentProvider.isLearningPathCSS()) {
				gtn.setCssClass(getItemCssClass(identifier));
			}
			//set resolved file path directly
			String identifierref = item.attributeValue("identifierref");
			if(identifierref != null) {
				gtn.setIdent("cp" + Encoder.md5hash(identPrefix + identifierref));
			}
			XPath meta = rootElement.createXPath("//ns:resource[@identifier='" + identifierref + "']");
			meta.setNamespaceURIs(nsuris);
			String href = resources.get(identifierref);
			if (href != null) {
				gtn.setUserObject(new UserObject(identifier, href));
				// allow lookup of a treenode given a href so we can quickly adjust the menu if the user clicks on hyperlinks within the text
				if (hrefToTreeNode.containsKey(href)){
					log.debug("Duplicate href::" + href + " for identifierref::" + identifierref + " and identifier::" + identifier + ", use first one");
				} else {					
					hrefToTreeNode.put(href, gtn);
				}
			} else {
				gtn.setAccessible(false);
			}
		}
		
		List<Element> chds = item.elements("item");
		int childcnt = chds.size();
		for (int i = 0; i < childcnt; i++) {
			Element childitem = chds.get(i);
			GenericTreeNode gtnchild = buildNode(childitem);
			gtn.addChild(gtnchild);
			// set the first accessible child in the hierarchy as delegate when the node itself is not accessible
			if (gtn.isAccessible() == false) {
				GenericTreeNode nextHierarchyChild = gtnchild;
				while (gtn.getDelegate() == null && nextHierarchyChild != null) {
					if (nextHierarchyChild.isAccessible()) {
						gtn.setDelegate(nextHierarchyChild);
					} else {
						if (nextHierarchyChild.getChildCount() > 0) {
							nextHierarchyChild = (GenericTreeNode) nextHierarchyChild.getChildAt(0);					
						} else {
							nextHierarchyChild = null;
						}
					}
				}
				if (!gtn.isAccessible()) {
					log.debug("No accessible child found that could be used as delegate for identifier::" + identifier);
				}
			}
		}
		return gtn;
	}
	
	private String getItemCssClass(String identifier) {
		AssessmentEntryStatus status = cpAssessmentProvider.getStatus(identifier);
		return getItemCssClass(cpAssessmentProvider.isLearningPathStatus(), status);
	}

	public static String getItemCssClass(boolean learningPathStatus, AssessmentEntryStatus status) {
		String itemCssClass = "o_lp_status ";
		if (learningPathStatus) {
			if (AssessmentEntryStatus.done.equals(status)) {
				itemCssClass += "o_lp_done o_lp_not_in_sequence o_lp_contains_no_sequence";
			} else {
				itemCssClass += "o_lp_ready o_lp_not_in_sequence o_lp_contains_no_sequence";
			}
		}
		return itemCssClass;
	}

	private Document loadDocument(VFSLeaf documentF) throws IOException {
		Document doc = null;
		try(InputStream in = documentF.getInputStream();) {
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			doc = xmlParser.parse(in, false);
		} catch (IOException e) {
			throw e;
		} catch(Exception e) {
			throw new IOException("could not read and parse from file " + documentF, e);
		}
		return doc;
	}
	
	private Document loadDocument(String documentStr) throws IOException {
		Document doc = null;
		try(InputStream in = new ByteArrayInputStream(documentStr.getBytes());) {
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			doc = xmlParser.parse(in, false);
		} catch (IOException e) {
			throw e;
		} catch(Exception e) {
			throw new IOException("could not read and parse from string " + documentStr, e);
		}
		return doc;
	}
	
	public static final class UserObject {
		
		private final String identifier;
		private final String href;

		public UserObject(String identifier, String href) {
			this.identifier = identifier;
			this.href = href;
		}

		public String getIdentifier() {
			return identifier;
		}

		public String getHref() {
			return href;
		}
		
	}
}
