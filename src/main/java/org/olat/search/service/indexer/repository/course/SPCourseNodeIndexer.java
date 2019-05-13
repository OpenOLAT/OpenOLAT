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

package org.olat.search.service.indexer.repository.course;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.sp.SPEditController;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.CourseNodeDocument;
import org.olat.search.service.indexer.LeafIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Indexer for SP (SinglePage) course-node.
 * @author Christian Guretzki
 */
public class SPCourseNodeIndexer extends LeafIndexer implements CourseNodeIndexer {
	private static final Logger log = Tracing.createLoggerFor(SPCourseNodeIndexer.class);

	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public final static String TYPE = "type.course.node.sp";

	private final static String SUPPORTED_TYPE_NAME = "org.olat.course.nodes.SPCourseNode";
	private final static boolean indexOnlyChosenFile = false;
  
	private static final Pattern HREF_PATTERN = Pattern.compile("href=\\\"(?!http:\\/\\/|https:\\/\\/|javascript:|mailto:|tel:|\\/|:|#|\\.\\.)([^\\\"]*)\\\"", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
	private static final String HTML_SUFFIXES = "html htm xhtml xml";

	@Override
	public void doIndex(SearchResourceContext courseResourceContext, ICourse course, CourseNode courseNode, OlatFullIndexer indexWriter) throws IOException,InterruptedException  {
		if (log.isDebugEnabled()) log.debug("Index SinglePage...");

		SearchResourceContext courseNodeResourceContext = createSearchResourceContext(courseResourceContext, courseNode, TYPE);
		Document nodeDocument = CourseNodeDocument.createDocument(courseNodeResourceContext, courseNode);
		indexWriter.addDocument(nodeDocument);
		
		// The root of the configured single page. Depends on the configuration
		// whether to follow relative links or not. When relative links are
		// followed, the root is the course folder root, if not, it is folder
		// where the configured file is in
		VFSContainer rootContainer;
		// The filename of the configured file relative to the rootContainer
		String chosenFile;
		
		// Read the course node configuration
		VFSContainer courseFolderContainer = course.getCourseEnvironment().getCourseFolderContainer();
		boolean allowRelativeLinks = courseNode.getModuleConfiguration().getBooleanSafe(SPEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS);
		String fileName = (String) courseNode.getModuleConfiguration().get(SPEditController.CONFIG_KEY_FILE);

		// *** IF YOU CHANGE THIS LOGIC, do also change it in SinglePageController! ***
		if (allowRelativeLinks) {
			// Case 1: relative links are allowed. The root is the root of the
			// course, the file name is relative to the root
			rootContainer = courseFolderContainer;
			chosenFile = fileName;
		} else {
			// Case 2: relative links are NOT allowed. We have to calculate the
			// new root and remove the relative path to the course folder form
			// the file.
			String startURI = ( (fileName.charAt(0) == '/')? fileName.substring(1) : fileName);
			int sla = startURI.lastIndexOf('/');
			if (sla != -1) {
				// Some subfolder path is detected, create basecontainer from it
				String root = startURI.substring(0,sla);
				startURI = startURI.substring(sla+1);
				// Create new root folder from the relative folder path
				VFSContainer newroot = (VFSContainer)courseFolderContainer.resolve(root);
				newroot.setParentContainer(null);
				rootContainer = newroot;
			} else {
				// No subpath detected, just use course base container
				rootContainer = courseFolderContainer;				
			}
			chosenFile = startURI;
		}

		VFSLeaf leaf = (VFSLeaf)rootContainer.resolve(chosenFile);
		if (leaf != null) {
			String filePath = getPathFor(leaf);
			// Use inherited method from LeafIndexer for the actual indexing of the content 

			SearchResourceContext fileContext = new SearchResourceContext(courseNodeResourceContext);
			doIndexVFSLeafByMySelf(fileContext, leaf, indexWriter, filePath);
			
			if (!indexOnlyChosenFile) {
				if (log.isDebugEnabled()) log.debug("Index sub pages in SP.");
				Set<String> alreadyIndexFileNames = new HashSet<String>();
				alreadyIndexFileNames.add(chosenFile);
				// Check if page has links to subpages and index those as well
				indexSubPages(courseNodeResourceContext,rootContainer,indexWriter,leaf,alreadyIndexFileNames,0,filePath);
			} else if (log.isDebugEnabled()) {
				log.debug("Index only chosen file in SP.");
			}
		} else if (log.isDebugEnabled()) {
			log.debug("Can not found choosen file in SP => Nothing indexed.");
		}
	}

	@Override
	public String getSupportedTypeName() {
		return SUPPORTED_TYPE_NAME;
	}

	private void indexSubPages(SearchResourceContext courseNodeResourceContext, VFSContainer rootContainer,
			OlatFullIndexer indexWriter, VFSLeaf leaf, Set<String> alreadyIndexFileNames, int subPageLevel,
			String rootFilePath) throws IOException, InterruptedException {
		int mySubPageLevel = subPageLevel;
		// check deepness of recursion
		if (mySubPageLevel++ <= 5) {
			List<String> links = getLinkListFrom(leaf);
			for (String link : links) {
				if (log.isDebugEnabled())
					log.debug("link=" + link);
				if ((rootFilePath != null) && !rootFilePath.equals("")) {
					if (rootFilePath.endsWith("/")) {
						link = rootFilePath + link;
					} else {
						link = rootFilePath + "/" + link;
					}
				}
				if (!alreadyIndexFileNames.contains(link)) {
					VFSItem item = rootContainer.resolve(link);
					if ((item != null) && (item instanceof VFSLeaf)) {
						VFSLeaf subPageLeaf = (VFSLeaf) item;
						if (log.isDebugEnabled())
							log.debug("subPageLeaf=" + subPageLeaf);
						String filePath = getPathFor(subPageLeaf);

						String newRootFilePath = filePath;

						doIndexVFSLeafByMySelf(courseNodeResourceContext, subPageLeaf, indexWriter, filePath);
						alreadyIndexFileNames.add(link);
						
						indexSubPages(courseNodeResourceContext, rootContainer, indexWriter, subPageLeaf, alreadyIndexFileNames, mySubPageLevel, newRootFilePath);
					} else {
						if (log.isDebugEnabled())
							log.debug("Could not found sub-page for link=" + link);
					}
				} else {
					if (log.isDebugEnabled())
						log.debug("sub-page already indexed, link=" + link);
				}
			}
		} else {
			if (log.isDebugEnabled())
				log.debug("Reach to many sub-page levels. Go not further with indexing sub-pages last leaf=" + leaf.getName());
		}
	}

	private List<String> getLinkListFrom(VFSLeaf leaf) {
		List<String> linkList = new ArrayList<String>();
		//only dive into file if it is a html file
		String suffix = getSuffix(leaf.getName());
		if (HTML_SUFFIXES.contains(suffix)) {
			BufferedInputStream bis = new BufferedInputStream(leaf.getInputStream());
			String inputString = FileUtils.load(bis, "utf-8");
		    // Remove all HTML Tags
			if (log.isDebugEnabled()) log.debug(inputString);	
			extractSubpageLinks(inputString, linkList);
		}
		return linkList;
	}
	
	/**
	 * Extract links to subpages from given page content
	 * @param pageContent HTML content
	 * @param linkList found links are added to this list
	 */
	public static void extractSubpageLinks(String pageContent, List<String> linkList) {
		Matcher m = HREF_PATTERN.matcher(pageContent);
	    String match;
		while (m.find()) {
			int groupCount = m.groupCount();
			if (groupCount > 0) {
				match = m.group(1); // e.g. 'seite2.html'
				linkList.add(match);
			}
		}
	}

	
	private  String getSuffix(String fileName) {
		int dotpos = fileName.lastIndexOf('.');
		if (dotpos < 0 || dotpos == fileName.length() - 1) {
			return "";
		}
		String suffix = fileName.substring(dotpos+1).toLowerCase();
		return suffix;
	}
}