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
package org.olat.course.nodes;

import java.util.Locale;
import java.util.Set;

import org.olat.core.id.Organisation;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.editor.overview.OverviewRow;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.feed.blog.BlogCourseNodeConfiguration;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.modules.webFeed.ui.FeedUIFactory;
import org.olat.modules.webFeed.ui.blog.BlogUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryToOrganisation;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;

/**
 * The blog course node.
 * 
 * <P>
 * Initial Date: Mar 30, 2009 <br>
 * 
 * @author gwassmann
 */
public class BlogCourseNode extends AbstractFeedCourseNode {

	private static final long serialVersionUID = 2263518867494681801L;

	@SuppressWarnings("deprecation")
	private static final String TRANSLATOR_PACKAGE = Util.getPackageName(BlogCourseNodeConfiguration.class);
	
	public static final String TYPE = FeedManager.KIND_BLOG;

	public BlogCourseNode(INode parent) {
		super(TYPE, parent);
	}

	@Override
	protected String getResourceablTypeName() {
		return BlogFileResource.TYPE_NAME;
	}

	@Override
	protected String getTranslatorPackage() {
		return TRANSLATOR_PACKAGE;
	}

	@Override
	protected FeedUIFactory getFeedUIFactory(Locale locale) {
		return BlogUIFactory.getInstance(locale);
	}

	@Override
	protected String geIconCssClass() {
		return "o_blog_icon";
	}
	
	@Override
	protected String getPeekviewWrapperCssClass() {
		return "o_blog_peekview";
	}

	@Override
	protected String getEditHelpUrl() {
		return "Knowledge Transfer#_blog";
	}
	
	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCrourse, CopyCourseContext context) {
		if (context != null) {
			CopyType resourceCopyType = null;
			
			if (context.isCustomConfigsLoaded()) {
				OverviewRow nodeSettings = context.getCourseNodesMap().get(getIdent());
				
				if (nodeSettings != null && nodeSettings.getResourceChooser() != null) {
					resourceCopyType = CopyType.valueOf(nodeSettings.getResourceChooser().getSelectedKey());
				}
			} else if (context.getBlogCopyType() != null) {
				resourceCopyType = context.getBlogCopyType();				
			}
			
			if (resourceCopyType != null) {
				switch (resourceCopyType) {
				case reference:
					// Nothing to do here, this is the default behavior
					break;
				case createNew:
					// Create a new empty blog with the same name
					RepositoryEntry blog = getReferencedRepositoryEntry();
					
					if (blog != null) {
						RepositoryHandlerFactory handlerFactory = RepositoryHandlerFactory.getInstance();
						
						Set<RepositoryEntryToOrganisation> organisations = blog.getOrganisations();
						Organisation organisation = null;
						if (organisations != null && organisations.size() > 1) {
							organisation = organisations.stream().filter(rel -> rel.isMaster()).map(rel -> rel.getOrganisation()).findFirst().orElse(null);
						} else if (organisations != null) {
							organisation = organisations.stream().map(rel -> rel.getOrganisation()).findFirst().orElse(null);
						}
						
						RepositoryEntry newBlog = handlerFactory.getRepositoryHandler(blog).createResource(context.getExecutingIdentity(), blog.getDisplayname(), blog.getDescription(), null, organisation, null);
						
						if (newBlog != null) {
							AbstractFeedCourseNode.setReference(getModuleConfiguration(), newBlog);
						}
					}
					break;
				case ignore:
					// Remove the config, must be configured later
					AbstractFeedCourseNode.removeReference(getModuleConfiguration());
					break;
				default:
					break;
				}
			}
		}
		
		super.postCopy(envMapper, processType, course, sourceCrourse, context);
	}

}
