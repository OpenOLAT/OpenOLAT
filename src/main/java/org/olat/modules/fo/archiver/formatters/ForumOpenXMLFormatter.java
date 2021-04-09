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
package org.olat.modules.fo.archiver.formatters;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.openxml.DocReference;
import org.olat.core.util.openxml.OpenXMLDocument;
import org.olat.core.util.openxml.OpenXMLDocument.Spacing;
import org.olat.core.util.openxml.OpenXMLDocument.Style;
import org.olat.core.util.tree.Visitor;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSLeafButSystemFilter;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.archiver.MessageNode;

/**
 * 
 * Initial date: 13.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumOpenXMLFormatter implements Visitor {

	private boolean firstThread = true;
	private boolean isTopThread = false;
	
	private final Translator translator;
	
	private final Formatter formatter;
	private final VFSContainer forumContainer;
	private final OpenXMLDocument document = new OpenXMLDocument();
	
	private final Set<String> attachmentsFilenames = new HashSet<>();
	private final Map<File,DocReference> fileToAttachmentsMap = new HashMap<>();

	public ForumOpenXMLFormatter(VFSContainer forumContainer, Locale locale) {
		translator = Util.createPackageTranslator(Forum.class, locale);
		document.setMediaContainer(forumContainer);
		this.forumContainer = forumContainer;
		formatter = Formatter.getInstance(locale);
	}
	
	public OpenXMLDocument getOpenXMLDocument() {
		return document;
	}
	
	public Map<File,DocReference> getAttachments() {
		return fileToAttachmentsMap;
	}
	
	public void openForum() {
		//
	}

	public void openThread() {
		if(firstThread) {
			firstThread = false;
		} else {
			document.appendPageBreak();
		}
		isTopThread = true;
	}

	@Override
	public void visit(INode node) {
		MessageNode m = (MessageNode) node;
		
		StringBuilder creatorAndDate = new StringBuilder();
		Identity creator = m.getCreator();
		if(StringHelper.containsNonWhitespace(m.getPseudonym())) {
			creatorAndDate.append(m.getPseudonym())
			  .append(" ");
			if(m.isGuest()) {
				creatorAndDate.append(translator.translate("guest.suffix"));
			} else {
				creatorAndDate.append(translator.translate("pseudonym.suffix"));
			}
		} else if(m.isGuest()) {
			creatorAndDate.append(translator.translate("guest"));
		} else if(creator != null) {
			creatorAndDate.append(creator.getUser().getProperty(UserConstants.FIRSTNAME, null));
			creatorAndDate.append(" ");
			creatorAndDate.append(creator.getUser().getProperty(UserConstants.LASTNAME, null));
		} else {
			creatorAndDate.append("???");
		}
		creatorAndDate.append(" ");
		creatorAndDate.append(formatter.formatDateAndTime(m.getCreationDate()));

		if (isTopThread) {
			document.appendHeading1(m.getTitle(), creatorAndDate.toString());
			isTopThread = false;
		} else {
			document.appendHeading2(m.getTitle(), creatorAndDate.toString());
		}

		Identity modifier = m.getModifier();
		if (modifier != null) {
			StringBuilder modSb = new StringBuilder();
			if(modifier.equals(creator) && StringHelper.containsNonWhitespace(m.getPseudonym())) {
				modSb.append(m.getPseudonym())
				  .append(" ");
				if(m.isGuest()) {
					modSb.append(translator.translate("guest.suffix"));
				} else {
					modSb.append(translator.translate("pseudonym.suffix"));
				}
			} else {
				modSb.append(translator.translate("msg.modified")).append(": ")
				     .append(modifier.getUser().getProperty(UserConstants.FIRSTNAME, null))
				     .append(" ")
				     .append(modifier.getUser().getProperty(UserConstants.LASTNAME,  null))
				     .append(" ")
				     .append(formatter.formatDateAndTime(m.getModifiedDate()));
			}
			document.appendSubtitle(modSb.toString());
		}
		
		String body = m.getBody();
		if(body != null) {
			body = body.replace("<p>&nbsp;", "<p>");
		}
		
		String mapperPath = m.getKey().toString();
		body = FilterFactory.getBaseURLToMediaRelativeURLFilter(mapperPath).filter(body);
		document.appendHtmlText(body, new Spacing(180, 0));
		
		// message attachments
		VFSItem attachmentsItem = forumContainer.resolve(m.getKey().toString());
		if(attachmentsItem instanceof VFSContainer) {
			processAttachments((VFSContainer)attachmentsItem);
		}
	}
	
	private void processAttachments(VFSContainer attachmentsContainer) {
		List<VFSItem> attachments = new ArrayList<>(attachmentsContainer.getItems(new VFSLeafButSystemFilter()));
		for(VFSItem attachment:attachments) {
			if(attachment instanceof LocalFileImpl) {
				//add the text
				document.appendText(translator.translate("attachments"), true, Style.bold);
			}
		}

		for(VFSItem attachment:attachments) {
			if(attachment instanceof LocalFileImpl) {
				File file = ((LocalFileImpl)attachment).getBasefile();
				String filename = file.getName();
				int lastDot = filename.lastIndexOf('.');
				
				boolean attach = true;
				if (lastDot > 0) {
					String extension = filename.substring(lastDot + 1).toLowerCase();
					if("jpeg".equals(extension) || "jpg".equals(extension) || "gif".equals(extension) || "png".equals(extension)) {
						attach = !document.appendImage(file);
					}
				}
				
				if(attach) {
					StringBuilder attachSb = new StringBuilder(64);
					String uniqueFilename = getUniqueFilename(file);
					fileToAttachmentsMap.put(file, new DocReference("", uniqueFilename, null, file));
					attachSb.append(filename).append(": /attachments/").append(uniqueFilename);
					document.appendText(attachSb.toString(), true);
				}
			}
		}
	}

	public StringBuilder closeForum() {
		return new StringBuilder();
	}
	
	private String getUniqueFilename(File image) {
		String filename = image.getName().toLowerCase();
		int extensionIndex = filename.lastIndexOf('.');
		if(extensionIndex > 0) {
			String name = filename.substring(0, extensionIndex);
			String extension = filename.substring(extensionIndex);
			filename = StringHelper.transformDisplayNameToFileSystemName(name) + extension;
		} else {
			filename = StringHelper.transformDisplayNameToFileSystemName(filename);
		}
		if(attachmentsFilenames.contains(filename)) {
			for(int i=1; i<1000; i++) {
				String nextFilename = i +"_" + filename;
				if(!attachmentsFilenames.contains(nextFilename)) {
					filename = nextFilename;
					attachmentsFilenames.add(filename);
					break;
				}
			}
		} else {
			attachmentsFilenames.add(filename);
		}
		return filename;	
	}
}