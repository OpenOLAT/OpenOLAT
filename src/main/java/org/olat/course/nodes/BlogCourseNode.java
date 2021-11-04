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

import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.feed.blog.BlogCourseNodeConfiguration;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.modules.webFeed.ui.FeedUIFactory;
import org.olat.modules.webFeed.ui.blog.BlogUIFactory;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.repository.ui.author.copy.wizard.CopyCourseOverviewRow;

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

	public BlogCourseNode() {
		super(TYPE);
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
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCourse, CopyCourseContext context) {
		if (context != null) {
			CopyType resourceCopyType = null;
			
			if (context.isCustomConfigsLoaded()) {
				CopyCourseOverviewRow nodeSettings = context.getCourseNodesMap().get(getIdent());
				
				if (nodeSettings != null) {
					resourceCopyType = nodeSettings.getResourceCopyType();
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
					importCopyResource(context.getExecutingIdentity());
					break;
				case ignore:
					// Remove the config, must be configured later
					removeReference(getModuleConfiguration());
					break;
				default:
					break;
				}
			}
		}
		
		super.postCopy(envMapper, processType, course, sourceCourse, context);
	}

}
