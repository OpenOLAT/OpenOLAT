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

package org.olat.modules.webFeed.portfolio;

import java.io.InputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.modules.webFeed.model.ItemImpl;
import org.olat.portfolio.EPAbstractHandler;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.search.service.SearchResourceContext;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Description:<br>
 * Artefact handler for blog entry
 * 
 * <P>
 * Initial Date:  3 déc. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BlogArtefactHandler extends EPAbstractHandler<BlogArtefact> {
	
	private static final Logger log = Tracing.createLoggerFor(BlogArtefactHandler.class);
	
	@Override
	public String getType() {
		return BlogArtefact.TYPE;
	}

	@Override
	public BlogArtefact createArtefact() {
		return new BlogArtefact();
	}
	
	/**
	 * @see org.olat.portfolio.EPAbstractHandler#prefillArtefactAccordingToSource(org.olat.portfolio.model.artefacts.AbstractArtefact, java.lang.Object)
	 */
	@Override
	public void prefillArtefactAccordingToSource(AbstractArtefact artefact, Object source) {
		super.prefillArtefactAccordingToSource(artefact, source);
		Feed feed = null;
		if (source instanceof Feed) {
			feed = (Feed)source;
			String subPath = getItemUUID(artefact.getBusinessPath());
			for(Item item:FeedManager.getInstance().loadItems(feed)) {
				if(subPath.equals(item.getGuid())) {
					prefillBlogArtefact(artefact, feed, item);
				}
			}
			artefact.setSignature(70); 
		}
		String origBPath = artefact.getBusinessPath();
		String artSource = "";
		BusinessControl bc = BusinessControlFactory.getInstance().createFromString(origBPath);
		if (origBPath.contains(CourseNode.class.getSimpleName())){
			// blog-post from inside a course, rebuild "course-name - feed-name"
			OLATResourceable ores = bc.popLauncherContextEntry().getOLATResourceable();
			RepositoryEntry repoEntry = RepositoryManager.getInstance().lookupRepositoryEntry(ores.getResourceableId());
			artSource = repoEntry.getDisplayname();
			if (feed!=null) {
				artSource += " - " + feed.getTitle();
			}
		} else if (origBPath.contains(RepositoryEntry.class.getSimpleName())){
			// blog-post from blog-LR, only get name itself
			if (feed!=null) {
				artSource = feed.getTitle();
			}			
		} else {
			// collecting a post from live-blog, [Identity:xy]
			if (feed!=null) {
				artSource = feed.getTitle();
			}			
		}		
		artefact.setSource(artSource);
	}

	private void prefillBlogArtefact(AbstractArtefact artefact, Feed feed, Item item) {
		VFSContainer itemContainer = FeedManager.getInstance().getItemContainer(item);
		artefact.setFileSourceContainer(itemContainer);
		artefact.setTitle(item.getTitle());
		artefact.setDescription(item.getDescription());
		
		VFSLeaf itemXml = (VFSLeaf)itemContainer.resolve(BlogArtefact.BLOG_FILE_NAME);
		if(itemXml != null) {
			InputStream in = itemXml.getInputStream();
			String xml = FileUtils.load(in, "UTF-8");
			artefact.setFulltextContent(xml);
			FileUtils.closeSafely(in);
		}
		String origBPath = artefact.getBusinessPath();
		String artSource = "";
		BusinessControl bc = BusinessControlFactory.getInstance().createFromString(origBPath);
		if (origBPath.contains(CourseNode.class.getSimpleName())){
			// blog-post from inside a course, rebuild "course-name - feed-name"
			OLATResourceable ores = bc.popLauncherContextEntry().getOLATResourceable();
			RepositoryEntry repoEntry = RepositoryManager.getInstance().lookupRepositoryEntry(ores.getResourceableId());
			artSource = repoEntry.getDisplayname();
			if (feed!=null) {
				artSource += " - " + feed.getTitle();
			}
		} else if (origBPath.contains(RepositoryEntry.class.getSimpleName())){
			// blog-post from blog-LR, only get name itself
			if (feed!=null) {
				artSource = feed.getTitle();
			}			
		} else {
			// collecting a post from live-blog, [Identity:xy]
			if (feed!=null) {
				artSource = feed.getTitle();
			}			
		}		
		artefact.setSource(artSource);		
	}

	@Override
	public Controller createDetailsController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, boolean readOnlyMode) {
		BlogArtefactDetailsController ctrl = new BlogArtefactDetailsController(ureq, wControl, (BlogArtefact)artefact, readOnlyMode);
		return ctrl;
	}
	
	@Override
	protected void getContent(AbstractArtefact artefact, StringBuilder sb, SearchResourceContext context, EPFrontendManager ePFManager) {
		String content = ePFManager.getArtefactFullTextContent(artefact);
		if(content != null) {
			try {
				XStream xstream = XStreamHelper.createXStreamInstance();
				xstream.alias("item", ItemImpl.class);
				ItemImpl item = (ItemImpl)xstream.fromXML(content);
				
				String mapperBaseURL = "";
				Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(mapperBaseURL);
				sb.append(mediaUrlFilter.filter(item.getDescription())).append(" ")
					.append(mediaUrlFilter.filter(item.getContent()));
			} catch (Exception e) {
				log.warn("Cannot read an artefact of type blog while indexing", e);
			}
		}
	}

	private String getItemUUID(String businessPath) {
		int start = businessPath.lastIndexOf("item=");
		int stop = businessPath.lastIndexOf(":0]");
		if(start < stop && start > 0 && stop > 0) {
			return businessPath.substring(start + 5, stop);
		} else {
			return null;
		}
	}
}