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
package org.olat.restapi.support;

import java.net.URI;

import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.olat.core.helpers.Settings;
import org.olat.repository.CatalogEntry;
import org.olat.restapi.support.vo.CatalogEntryVO;
import org.olat.restapi.support.vo.LinkVO;

/**
 * 
 * Description:<br>
 * Object factory for the catalog entry
 * 
 * <P>
 * Initial Date:  5 may 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class CatalogVOFactory {

	public static CatalogEntryVO get(CatalogEntry entry) {
		CatalogEntryVO vo = new CatalogEntryVO();
		vo.setKey(entry.getKey());
		vo.setName(entry.getName());
		vo.setDescription(entry.getDescription());
		vo.setExternalURL(entry.getExternalURL());
		vo.setType(entry.getType());
		vo.setParentKey(entry.getParent() == null ? null : entry.getParent().getKey());
		vo.setRepositoryEntryKey(entry.getRepositoryEntry() == null ? null : entry.getRepositoryEntry().getKey());
		return vo;
	}
	
	public static CatalogEntryVO link(CatalogEntryVO entryVo, UriInfo uriInfo) {
		if(uriInfo != null) {
			UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
			URI getUri = baseUriBuilder.path("catalog").path(entryVo.getKey().toString()).build();
			entryVo.getLink().add(new LinkVO("self", getUri.toString(), ""));
			entryVo.getLink().add(new LinkVO("jumpin", Settings.getServerContextPathURI() + "/url/CatalogEntry/" + entryVo.getKey(), ""));
			entryVo.getLink().add(new LinkVO("edit", getUri.toString(), ""));
			entryVo.getLink().add(new LinkVO("delete", getUri.toString(), ""));

			URI childrenUri = baseUriBuilder.path("catalog").path(entryVo.getKey().toString()).path("children").build();
			entryVo.getLink().add(new LinkVO("children", childrenUri.toString(), ""));
		}
		return entryVo;
	}
}
