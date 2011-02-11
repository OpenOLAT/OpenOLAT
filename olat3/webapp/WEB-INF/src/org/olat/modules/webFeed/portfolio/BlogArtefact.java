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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.modules.webFeed.portfolio;

import org.olat.fileresource.types.BlogFileResource;
import org.olat.portfolio.model.artefacts.AbstractArtefact;

/**
 * 
 * Description:<br>
 * Take a post in a blog as an artefact
 * 
 * <P>
 * Initial Date:  3 déc. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BlogArtefact extends AbstractArtefact {
	
	public static final String TYPE = BlogFileResource.TYPE_NAME;
	public static final String BLOG_FILE_NAME = "item.xml";

	@Override
	public String getIcon() {
		return "o_blog_icon";
	}
	
	@Override
	public String getResourceableTypeName() {
		return TYPE;
	}


}
