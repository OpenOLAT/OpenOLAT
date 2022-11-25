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
package org.olat.modules.library.ui;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.library.model.CatalogItem;

/**
 * 
 * <h3>Description:</h3>
 * Small helper to write the 
 * <p>
 * Initial Date:  30 nov. 2010 <br>
 * @author srosse, srosse@frentix.com, www.frentix.com
 */
public class ThumbnailHelper {
	
	private final Translator translator;
	private final String thumbnailMapperBaseURL;
	
	public ThumbnailHelper(Translator translator, String thumbnailMapperBaseURL) {
		this.translator = translator;
		this.thumbnailMapperBaseURL = thumbnailMapperBaseURL;
	}
	
	public String getThumbnails(CatalogItem item) {
		VFSMetadata metaInfo = item.getMetaInfo();
		if(metaInfo != null && item.isThumbnailAvailable()) {
			StringBuilder sb = new StringBuilder();
			sb.append("<div class='b_briefcase_preview' style='width:200px; height:200px; background-image:url('"); 
			sb.append(thumbnailMapperBaseURL).append(item.getRelativePath());
			sb.append("'); background-repeat:no-repeat; background-position:50% 50%;'>&nbsp;</div>");
			return sb.toString();
		}
		return "";
	}
	
	public String getThumbnailTitle(CatalogItem item) {
		VFSMetadata metaInfo = item.getMetaInfo();
		StringBuilder sb = new StringBuilder();
		if(metaInfo != null && item.isThumbnailAvailable()) {
			sb.append("width:200px; height:200px; float:left; background-image:url('"); 
			sb.append(thumbnailMapperBaseURL).append("/").append(item.getRelativePath()).append("?thumbnail=true");
			sb.append("'); background-repeat:no-repeat; background-position:50% 50%;");
		}
		return sb.toString();
	}
	
	public String getThumbnailInfos(CatalogItem item) {
		VFSMetadata metaInfo = item.getMetaInfo();
		if(metaInfo != null && item.isThumbnailAvailable()) {
			StringBuilder sb = new StringBuilder();
			sb.append("<div class='b_ext_tooltip_wrapper b_briefcase_meta'>");
			if (StringHelper.containsNonWhitespace(metaInfo.getTitle())) {				
				sb.append("<h5>").append(Formatter.escapeDoubleQuotes(metaInfo.getTitle())).append("</h5>");		
			}
			if (StringHelper.containsNonWhitespace(metaInfo.getComment())) {
				sb.append(Formatter.escapeDoubleQuotes(metaInfo.getComment()));			
			}
			if(item.isThumbnailAvailable()) {
				sb.append("<div class='b_briefcase_preview' style='width:200px; height:200px; background-image:url('"); 
				sb.append(thumbnailMapperBaseURL).append("/").append(item.getRelativePath()).append("?thumbnail=true");
				sb.append("'); background-repeat:no-repeat; background-position:50% 50%;'>&nbsp;</div>");
			}
			Identity author = metaInfo.getFileInitializedBy();
			if (author != null) {
				sb.append("<p>")
					.append(Formatter.escapeDoubleQuotes(translator.translate("mf.author")))
					.append(": ")
					.append(Formatter.escapeDoubleQuotes(author.toString()))
					.append("</p>");			
			}
			sb.append("</div>");
			return sb.toString();
		}
		return "";
	}
}
