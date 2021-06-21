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
package org.olat.core.commons.modules.glossary;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.olat.core.gui.control.generic.textmarker.TextMarker;
import org.olat.core.gui.control.generic.textmarker.TextMarkerManager;
import org.olat.core.helpers.Settings;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XStreamHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

/**
 * Description:<br>
 * holds the glossary item list in a cache, to deliver it faster and to prevent
 * from filesystem access or multiple loading into ram. each glossary will only
 * be loaded once, until it changes and cache is invalidated. read and writes
 * the enhanced docBook-glossary format from/to filesystem.
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
@Service
public class GlossaryItemManager {
	
	private static final Logger log = Tracing.createLoggerFor(GlossaryItemManager.class);
	
	private static final String OLD_GLOSSARY_FILENAME = "glossary.textmarker.xml";
	private static final String GLOSSARY_FILENAME = "glossary.xml";
	private static final String XML_GLOSSARY_ITEM_NAME = "glossentry";
	private static final String XML_REVISION_NAME = "revision";
	private static final String GLOSSARY_CONFIG_PROPERTIES_FILE = "glossary.properties";
	public static final String NO_MS_VALUE = "ms-none";
	public static final String MS_KEY = "morphological.service.identifier";
	public static final String REGISTER_ONOFF = "register.index.enabled";
	public static final String EDIT_USERS = "edit.by.users.enabled";
	
	private static final XStream xstreamReader = XStreamHelper.createXStreamInstance();
	static {
		XStreamHelper.allowDefaultPackage(xstreamReader);
		xstreamReader.alias(XML_GLOSSARY_ITEM_NAME, GlossaryItem.class);
		xstreamReader.alias(XML_REVISION_NAME, Revision.class);
	}
	
	private static final XStream xstreamWriter = new XStream(new XppDriver() {
		@Override
		public HierarchicalStreamWriter createWriter(Writer out) {
			return new PrettyPrintWriter(out) {
				@Override
				protected void writeText(QuickWriter writer, String text) {
					if (text.contains("<")||text.contains(">")||text.contains("&")){
						writer.write("<![CDATA[");
						writer.write(text);
						writer.write("]]>");
					} else {
						writer.write(text);
					}
				}
			};
		}
	});
	
	static {
		XStreamHelper.allowDefaultPackage(xstreamReader);
		xstreamWriter.alias(XML_GLOSSARY_ITEM_NAME, GlossaryItem.class);
		xstreamWriter.alias(XML_REVISION_NAME, Revision.class);
	}

	private CacheWrapper<String, List<GlossaryItem>> glossaryCache;
	
	@Autowired
	private TextMarkerManager textMarkerManager;
	@Autowired
	private CoordinatorManager coordinatorManager;

	/**
	 * used to save new or changed entries in List
	 * 
	 * @param olatResource
	 * @param glossItemList
	 */
	public void saveGlossaryItemList(VFSContainer glossaryFolder, List<GlossaryItem> glossItemList) {
		VFSLeaf glossaryFile = getGlossaryFile(glossaryFolder);
		saveToFile(glossaryFile, glossItemList);
		glossItemList = removeEmptyGlossaryItems(glossItemList);
		updateCacheForGlossary(glossaryFolder, glossItemList);
	}
	
	/**
	 * returns GlossaryItem-array containing only Items with a non-empty term 
	 * @param glossItemList
	 * @return
	 */
	private List<GlossaryItem> removeEmptyGlossaryItems(List<GlossaryItem> glossItemList){
		List<GlossaryItem> newList = new ArrayList<>();
		for (Iterator<GlossaryItem> iterator = glossItemList.iterator(); iterator.hasNext();) {
			GlossaryItem glossaryItem = iterator.next();
			if (StringHelper.containsNonWhitespace(glossaryItem.getGlossTerm())){
				newList.add(glossaryItem);
			}
		}
		return newList;
	}

	/**
	 * upgrades the old textmarker-format into the new DocBook-glossary-format
	 * 
	 * @param folderContainingGlossary
	 * @param textMarkerFile
	 */
	protected void upgradeAndDeleteOldGlossary(VFSContainer folderContainingGlossary, VFSLeaf textMarkerFile) {
		// check if a new glossary exists, warn
		if (folderContainingGlossary.resolve(GLOSSARY_FILENAME) != null) {
			log.error("Upgrading Glossary in " + folderContainingGlossary.toString() + ": There is already a new glossary-file. There can't be an old and a new version in the same directory!");
		} else { // upgrade it
			List<TextMarker> textMarkerList = textMarkerManager.loadTextMarkerList(textMarkerFile);
			Collections.sort(textMarkerList);
			ArrayList<GlossaryItem> glossaryItemArr = new ArrayList<>();

			for (TextMarker tm : textMarkerList) {
				String glossTerm = tm.getMarkedMainText();
				String glossDef = tm.getHooverText();
				GlossaryItem glossItem = new GlossaryItem(glossTerm, glossDef);

				// handle alias -> save as synonyms
				String aliasString = tm.getMarkedAliasText();
				if (StringHelper.containsNonWhitespace(aliasString)) {
					String[] aliasArr = aliasString.split(";");
					ArrayList<String> glossSynonyms = new ArrayList<>();
					glossSynonyms.addAll(Arrays.asList(aliasArr));
					glossItem.setGlossSynonyms(glossSynonyms);
				}
				glossaryItemArr.add(glossItem);
			}

			VFSLeaf glossaryFile = folderContainingGlossary.createChildLeaf(GLOSSARY_FILENAME);
			saveToFile(glossaryFile, glossaryItemArr);
			// keep a backup in debug mode:
			if (Settings.isDebuging()) {
				File tmFile = ((LocalFileImpl) textMarkerFile).getBasefile();
				File tmCont = ((LocalFolderImpl) folderContainingGlossary).getBasefile();
				FileUtils.copyFileToDir(tmFile, new File(tmCont + "/bkp"), "backup old glossary");
			}
			textMarkerFile.delete();
		}
	}


	/**
	 * returns the glossary file. if an old-format is found in directory of the
	 * glossary it automatically gets updated.
	 * 
	 * @param folderContainingGlossary physical disk-path
	 * @return the glossary file
	 */
	public VFSLeaf getGlossaryFile(VFSContainer folderContainingGlossary) {
		VFSLeaf glossaryFile = (VFSLeaf) folderContainingGlossary.resolve(OLD_GLOSSARY_FILENAME);
		if (glossaryFile != null) {
			// old glossary
			upgradeAndDeleteOldGlossary(folderContainingGlossary, glossaryFile);
		}
		// look for new glossary, or use new after upgrading
		glossaryFile = (VFSLeaf) folderContainingGlossary.resolve(GLOSSARY_FILENAME);

		if (glossaryFile == null) {
			// create an empty file on the fly and initialize it
			glossaryFile = folderContainingGlossary.createChildLeaf(GLOSSARY_FILENAME);
			saveToFile(glossaryFile, new ArrayList<GlossaryItem>());
		}
		return glossaryFile;
	}
	
	public Long getGlossaryLastModifiedTime(VFSContainer folderContainingGlossary) {
		return getGlossaryFile(folderContainingGlossary).getLastModified();
	}

	public boolean isFolderContainingGlossary(VFSContainer folderContainingGlossary) {
		VFSLeaf glossaryFileOld = (VFSLeaf) folderContainingGlossary.resolve(OLD_GLOSSARY_FILENAME);
		VFSLeaf glossaryFileNew = (VFSLeaf) folderContainingGlossary.resolve(GLOSSARY_FILENAME);
		if (glossaryFileNew == null && glossaryFileOld == null) return false;
		else return true;
	}

	/**
	 * writes glossary to xml-file
	 * prepend doc-book dtd: 
	 * <!DOCTYPE glossary PUBLIC "-//OASIS//DTD DocBook XML V4.1.2//EN"          "http://www.oasis-open.org/docbook/xml/4.1.2/docbookx.dtd">
	 * 
	 * @param glossaryFile
	 * @param glossaryItemArr
	 */
	protected final void saveToFile(VFSLeaf glossaryFile, List<GlossaryItem> glossaryItemArr) {
		// cdata-tags should be used instead of strings, overwrite writer.
		glossaryItemArr = removeEmptyGlossaryItems(glossaryItemArr);
		XStreamHelper.writeObject(xstreamWriter, glossaryFile, glossaryItemArr);
	}

	public List<GlossaryItem> getGlossaryItemListByVFSItem(final VFSContainer glossaryFolder){		
		final String glossaryKey = ((LocalFolderImpl)glossaryFolder).getBasefile().toString();
		if (glossaryCache == null) {
			glossaryCache = coordinatorManager.getCoordinator().getCacher().getCache(GlossaryItemManager.class.getSimpleName(), "glossary");
		}
		//try to load from cache
		List<GlossaryItem> glossaryItemList = glossaryCache.get(glossaryKey);
		if (glossaryItemList != null){
			if (log.isDebugEnabled()){
				log.debug("Loading glossary from cache.");
			}
			return glossaryItemList;
		}
		
		return glossaryCache.computeIfAbsent(glossaryKey, key -> {
			if (log.isDebugEnabled()){
				log.debug("Loading glossary from filesystem. Glossary folder: " + glossaryFolder);
			}
			return loadGlossaryItemListFromFile(getGlossaryFile(glossaryFolder));
		});
	}

	/**
	 * if changes occur on GlossaryList, cache has to be updated
	 * 
	 * @param olatResource
	 */
	private void updateCacheForGlossary(VFSContainer glossaryFolder, List<GlossaryItem> glossItemList) {
		final String glossaryKey = ((LocalFolderImpl)glossaryFolder).getBasefile().toString();
		glossaryCache.update(glossaryKey, glossItemList);
	}

	/**
	 * load a list of glossaryItem from glossary.xml - File
	 * 
	 * @param glossaryFile
	 * @return list with GlossaryItem's
	 */
	@SuppressWarnings("unchecked")
	protected final List<GlossaryItem> loadGlossaryItemListFromFile(VFSLeaf glossaryFile) {
		List<GlossaryItem> glossaryItemList = new ArrayList<>();
		if (glossaryFile == null) { return new ArrayList<>(); }
		
		Object glossObj = XStreamHelper.readObject(xstreamReader, glossaryFile);
		if (glossObj instanceof ArrayList) {
			ArrayList<GlossaryItem> glossItemsFromFile = (ArrayList<GlossaryItem>) glossObj;
			glossaryItemList.addAll(glossItemsFromFile);
		} else {
			log.error("The Glossary-XML-File " + glossaryFile.toString() + " seems not to be correct!");
		}

		Collections.sort(glossaryItemList);
		return glossaryItemList;
	}

	/**
	 * needed for search engine
	 * @param olatResource
	 * @return
	 */
	public String getGlossaryContent(VFSContainer glossaryFolder){
		List<GlossaryItem> glossItems = getGlossaryItemListByVFSItem(glossaryFolder);
		StringBuilder sb = new StringBuilder(1024);
		for (GlossaryItem glossItem : glossItems) {
			List<String> allStrings = glossItem.getAllStringsToMarkup();
			for (String markupStr : allStrings) {
				sb.append(markupStr);
				sb.append("\n");
			}
			sb.append("\n");
			sb.append(glossItem.getGlossDef());
			sb.append("\n\n");
		}
		return sb.toString();
	}
	
	// Configuration of a glossary in properties file 
	// implement a GlossaryObject with settings and glossItemList and cache this.
	public Properties getGlossaryConfig(VFSContainer glossaryFolder){
		Properties props = new Properties();
		VFSLeaf glossProp = (VFSLeaf) glossaryFolder.resolve(GLOSSARY_CONFIG_PROPERTIES_FILE);
		if(glossProp!=null) {
			try {
				props.load(glossProp.getInputStream());
			} catch (IOException e) {
				log.error("Properties in " + glossProp + " could not be read.", e);
			}
		} else {
			//set default config
			props.put(MS_KEY, NO_MS_VALUE);
			props.put(REGISTER_ONOFF, "true");
			setGlossaryConfig(glossaryFolder, props);
		}
		return props;
	}
	
	public void setGlossaryConfig(VFSContainer glossaryFolder, Properties props){
		VFSLeaf glossProp = (VFSLeaf) glossaryFolder.resolve(GLOSSARY_CONFIG_PROPERTIES_FILE);
		if (glossProp==null){
			glossProp = glossaryFolder.createChildLeaf(GLOSSARY_CONFIG_PROPERTIES_FILE);
		}
		try(OutputStream out=glossProp.getOutputStream(false)) {
			props.store(out, "Settings for the glossary saved in this folder.");
		} catch (IOException e) {
			log.error("Properties in " + glossProp + " could not be written.", e);
		}
	}
}
