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
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.olat.core.gui.control.generic.textmarker.TextMarker;
import org.olat.core.gui.control.generic.textmarker.TextMarkerManager;
import org.olat.core.gui.control.generic.textmarker.TextMarkerManagerImpl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XStreamHelper;

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
public class GlossaryItemManager extends BasicManager {

	private static GlossaryItemManager INSTANCE;
	private static final String OLD_GLOSSARY_FILENAME = "glossary.textmarker.xml";
	private static final String GLOSSARY_FILENAME = "glossary.xml";
	private static final String XML_GLOSSARY_ITEM_NAME = "glossentry";
	private static final String XML_REVISION_NAME = "revision";
	private static final String GLOSSARY_CONFIG_PROPERTIES_FILE = "glossary.properties";
	public static final String NO_MS_VALUE = "ms-none";
	public static final String MS_KEY = "morphological.service.identifier";
	public static final String REGISTER_ONOFF = "register.index.enabled";
	public static final String EDIT_USERS = "edit.by.users.enabled";
	private static final OLATResourceable glossaryEventBus = OresHelper.createOLATResourceableType("glossaryEventBus");
	private CacheWrapper<String, ArrayList<GlossaryItem>> glossaryCache;
	private CoordinatorManager coordinatorManager;

	/**
	 * [spring]
	 */
	private GlossaryItemManager(CoordinatorManager coordinatorManager) {
		this.coordinatorManager = coordinatorManager;
		INSTANCE = this;
	}

	public static GlossaryItemManager getInstance() {
		return INSTANCE;
	}

	/**
	 * used to save new or changed entries in List
	 * 
	 * @param olatResource
	 * @param glossItemList
	 */
	public void saveGlossaryItemList(VFSContainer glossaryFolder, ArrayList<GlossaryItem> glossItemList) {
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
	private ArrayList<GlossaryItem> removeEmptyGlossaryItems(ArrayList<GlossaryItem> glossItemList){
		ArrayList<GlossaryItem> newList = new ArrayList<GlossaryItem>();
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
			logError("Upgrading Glossary in " + folderContainingGlossary.toString() + ": There is already a new glossary-file. There can't be an old and a new version in the same directory!", null);
		} else { // upgrade it
			TextMarkerManager textMarkerManager = TextMarkerManagerImpl.getInstance();
			List<TextMarker> textMarkerList = textMarkerManager.loadTextMarkerList(textMarkerFile);
			Collections.sort(textMarkerList);
			ArrayList<GlossaryItem> glossaryItemArr = new ArrayList<GlossaryItem>();

			for (TextMarker tm : textMarkerList) {
				String glossTerm = tm.getMarkedMainText();
				String glossDef = tm.getHooverText();
				GlossaryItem glossItem = new GlossaryItem(glossTerm, glossDef);

				// handle alias -> save as synonyms
				String aliasString = tm.getMarkedAliasText();
				if (StringHelper.containsNonWhitespace(aliasString)) {
					String[] aliasArr = aliasString.split(";");
					ArrayList<String> glossSynonyms = new ArrayList<String>();
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

	//TODO:RH:gloss improvement: dtd in xml files
	/**
	 * writes glossary to xml-file
	 * prepend doc-book dtd: 
	 * <!DOCTYPE glossary PUBLIC "-//OASIS//DTD DocBook XML V4.1.2//EN"          "http://www.oasis-open.org/docbook/xml/4.1.2/docbookx.dtd">
	 * 
	 * @param glossaryFile
	 * @param glossaryItemArr
	 */
	private void saveToFile(VFSLeaf glossaryFile, ArrayList<GlossaryItem> glossaryItemArr) {
		// cdata-tags should be used instead of strings, overwrite writer.
		XStream xstream = new XStream(new XppDriver() {
			public HierarchicalStreamWriter createWriter(Writer out) {
				return new PrettyPrintWriter(out) {
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

		xstream.alias(XML_GLOSSARY_ITEM_NAME, GlossaryItem.class);
		xstream.alias(XML_REVISION_NAME, Revision.class);
		glossaryItemArr = removeEmptyGlossaryItems(glossaryItemArr);
		XStreamHelper.writeObject(xstream, glossaryFile, glossaryItemArr);
	}

	//FIXME: VFSItem should be capable of returning an identifier, instead of casting to LocalFolderImpl implement a getIdentifier for it!
	public ArrayList<GlossaryItem> getGlossaryItemListByVFSItem(final VFSContainer glossaryFolder){		
		final String glossaryKey = ((LocalFolderImpl)glossaryFolder).getBasefile().toString();
		if (glossaryCache == null) {
			glossaryCache = coordinatorManager.getCoordinator().getCacher().getCache(GlossaryItemManager.class.getSimpleName(), "glossary");
		}
		//try to load from cache
		ArrayList<GlossaryItem> glossaryItemList = glossaryCache.get(glossaryKey);
		if (glossaryItemList != null){
			if (isLogDebugEnabled()){
				logDebug("Loading glossary from cache.", null);
			}
			return glossaryItemList;
		}
		// load from filesystem
		coordinatorManager.getCoordinator().getSyncer().doInSync(glossaryEventBus, new SyncerExecutor() {
			@SuppressWarnings("synthetic-access")
			public void execute() {
				ArrayList<GlossaryItem> glossaryItemListTemp = new ArrayList<GlossaryItem>();
				if (isLogDebugEnabled()){
					logDebug("Loading glossary from filesystem. Glossary folder: " + glossaryFolder, null);
				}
				glossaryItemListTemp = loadGlossaryItemListFromFile(getGlossaryFile(glossaryFolder));
				glossaryCache.put(glossaryKey, glossaryItemListTemp);				
			}
		});
		//return value from cache, as it was put in there before
		return glossaryCache.get(glossaryKey);
	}

	/**
	 * if changes occur on GlossaryList, cache has to be updated
	 * 
	 * @param olatResource
	 */
	//FIXME: VFSItem should be capable of returning an identifier, instead of casting to LocalFolderImpl implement a getIdentifier for it!
	private void updateCacheForGlossary(VFSContainer glossaryFolder, ArrayList<GlossaryItem> glossItemList) {
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
	private ArrayList<GlossaryItem> loadGlossaryItemListFromFile(VFSLeaf glossaryFile) {
		ArrayList<GlossaryItem> glossaryItemList = new ArrayList<GlossaryItem>();
		if (glossaryFile == null) { return new ArrayList<GlossaryItem>(); }
		XStream xstream = XStreamHelper.createXStreamInstance();
		xstream.alias(XML_GLOSSARY_ITEM_NAME, GlossaryItem.class);
		xstream.alias(XML_REVISION_NAME, Revision.class);
		Object glossObj = XStreamHelper.readObject(xstream, glossaryFile.getInputStream());
		if (glossObj instanceof ArrayList) {
			ArrayList<GlossaryItem> glossItemsFromFile = (ArrayList<GlossaryItem>) glossObj;
			glossaryItemList.addAll(glossItemsFromFile);
		} else {
			logError("The Glossary-XML-File " + glossaryFile.toString() + " seems not to be correct!", null);
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
		ArrayList<GlossaryItem> glossItems = getGlossaryItemListByVFSItem(glossaryFolder);
		StringBuilder sb = new StringBuilder();
		for (GlossaryItem glossItem : glossItems) {
			ArrayList<String> allStrings = glossItem.getAllStringsToMarkup();
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
	
	
	
//			Configuration of a glossary in properties file 
	//TODO: RH: improvement in case fileReads should slow down system
	// implement a GlossaryObject with settings and glossItemList and cache this.
	public Properties getGlossaryConfig(VFSContainer glossaryFolder){
		Properties props = new Properties();
		VFSLeaf glossProp = (VFSLeaf) glossaryFolder.resolve(GLOSSARY_CONFIG_PROPERTIES_FILE);
		if(glossProp!=null){
			try {
				props.load(glossProp.getInputStream());
			} catch (IOException e) {
				logError("Properties in " + glossProp + " could not be read.", e);
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
		try {
			props.store(glossProp.getOutputStream(false), "Settings for the glossary saved in this folder.");
		} catch (IOException e) {
			logError("Properties in " + glossProp + " could not be written.", e);
		}
		
	}
	
}
