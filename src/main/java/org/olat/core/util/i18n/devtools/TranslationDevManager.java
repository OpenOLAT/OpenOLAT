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
package org.olat.core.util.i18n.devtools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.logging.OLATRuntimeException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Description:<br>
 * Description for TranslationDevManager
 * 
 * <P>
 * Initial Date: 23.09.2008 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
@Service("translationDevManager")
public class TranslationDevManager {
	private static final Logger log = Tracing.createLoggerFor(TranslationDevManager.class);

	private StringBuffer logText = new StringBuffer();
	
	@Autowired
	private 	I18nManager i18nMgr;
	@Autowired
	private I18nModule i18nModule;
	

	protected Set<String> getAllLanguages() {
		return i18nModule.getAvailableLanguageKeys();
	}

	protected void renameKeyTask(String bundleName, String origKey, String targetKey) {
		moveKeyTask(bundleName, bundleName, origKey, targetKey);
	}
	
	public void moveKeyToOtherBundle(String originBundleName, String targetBundleName, String key){
		moveKeyTask(originBundleName, targetBundleName, key, key);
	}

	//set to protected or default visibility -> problems with test
	public void moveKeyTask(String originBundleName, String targetBundleName, String origKey, String targetKey) {
		Set<String> allLangs = getAllLanguages();
		// move meta-data
		if ( ( !originBundleName.equals(targetBundleName) || !origKey.equals(targetKey) ) ){
			//move annotations
			String annotationOrigKey = origKey + I18nManager.METADATA_ANNOTATION_POSTFIX;
			String annotationTargetKey = targetKey + I18nManager.METADATA_ANNOTATION_POSTFIX;
			moveSingleKey(null, originBundleName, targetBundleName, annotationOrigKey, annotationTargetKey);
			//move priority info
			String priorityOrigKey = origKey + I18nManager.METADATA_KEY_PRIORITY_POSTFIX;
			String priorityTargetKey = targetKey + I18nManager.METADATA_KEY_PRIORITY_POSTFIX;
			moveSingleKey(null, originBundleName, targetBundleName, priorityOrigKey, priorityTargetKey);
		}
		
		//look for references and replace them
		changeReferencesInValues(originBundleName, targetBundleName, origKey, targetKey);

		for (String key : allLangs) {
		    Locale locale = i18nMgr.getLocaleOrNull(key);
				//move key/value itself
				moveSingleKey(locale, originBundleName, targetBundleName, origKey, targetKey);
		}
	}
	
	protected void changeReferencesInValues(String originBundleName, String targetBundleName, String origKey, String targetKey){
		//operation:
		boolean movePackage = false;
		boolean renameKey = false;
		if (!originBundleName.equals(targetBundleName)) movePackage = true;
		if (!origKey.equals(targetKey)) renameKey = true;
		int counter = 0;
		Pattern resolvingKeyPattern = Pattern.compile("\\$\\{?("+originBundleName+")+:([\\w\\.\\-]*[\\w\\-])\\}?");
		
		List<String> allBundles = i18nModule.getBundleNamesContainingI18nFiles();
		Set<String> allLangs = getAllLanguages();
		for (String langKey : allLangs) {
	    Locale locale = i18nMgr.getLocaleOrNull(langKey);
 
			for (String bundleName : allBundles) {				
				Properties properties = i18nMgr.getPropertiesWithoutResolvingRecursively(locale, bundleName);
				Set<Object> keys = properties.keySet();
				
				for (Object keyObj : keys) {
					String key = (String) keyObj;
					String value = properties.getProperty(key);
					
					Matcher matcher = resolvingKeyPattern.matcher(value);
					int lastPos = 0;
					while (matcher.find()) {
						String matchedKey = matcher.group(2);
						String matchedBundle = matcher.group(1);
						if (matchedKey.equals(origKey) 
								&& ( (matchedBundle== null && bundleName.equals(originBundleName)  ) 
								|| originBundleName.equals(matchedBundle)) ){
							StringBuilder newValue = new StringBuilder();
							newValue.append(value.substring(0, matcher.start()));
							newValue.append("$");
							if (movePackage){
								if (!targetBundleName.equals(matchedBundle) ){
									newValue.append(targetBundleName);
								}
							}
							newValue.append(":");
							if (renameKey){
								newValue.append(targetKey);
							} else {
								newValue.append(origKey);
							}
					
							lastPos = matcher.end();
							newValue.append(value.substring(lastPos));
							log.info("Key:: " + key + " should get changed to value:: " + newValue.toString());
							logText.append(i18nMgr.getPropertiesFile(locale, bundleName, i18nModule.getPropertyFilesBaseDir(locale, bundleName)) + 
									" update reference in lang::" + locale.getLanguage() + " bundle::" + bundleName + " key::" + key + " value::" + value + " \n\t to new value:: " + newValue.toString() + "\n");	
							counter ++;
//					changeValueForSingleKey(locale, bundleName, key, newValue.toString());	
						}
					}
				} //each key
			}
		}		
		log.info(counter + " values have been updated.");
	}
	
	
	protected void changeValueForSingleKey(Locale locale, String bundleName, String key, String newValue){
		deleteKey(locale, bundleName, key);
		addKey(locale, bundleName, key, newValue);
	}
	
	protected void moveSingleKey(Locale locale, String originBundleName, String targetBundleName, String origKey, String targetKey) {
		Properties prop = i18nMgr.getPropertiesWithoutResolvingRecursively(locale, originBundleName);
		String value = prop.getProperty(origKey);
		deleteKey(locale, originBundleName, origKey);
		addKey(locale, targetBundleName, targetKey, value);
	}

	protected void deleteKey(Locale locale, String bundleName, String key) {
		Properties tempProp = i18nMgr.getPropertiesWithoutResolvingRecursively(locale, bundleName);
		tempProp.remove(key);
		i18nMgr.saveOrUpdateProperties(tempProp, locale, bundleName);
		
		checkForEmptyPropertyAndDelete(locale, bundleName);
	}

	protected void addKey(Locale locale, String bundleName, String key, String value) {
		I18nItem i18nItem = new I18nItem(bundleName, key, locale, I18nManager.DEFAULT_BUNDLE_PRIORITY, I18nManager.DEFAULT_KEY_PRIORITY);
		i18nMgr.saveOrUpdateI18nItem(i18nItem, value);
	}

	private void checkForEmptyPropertyAndDelete(Locale locale, String bundleName) {
		if (i18nMgr.getPropertiesWithoutResolvingRecursively(locale, bundleName).isEmpty()) {
			i18nMgr.deleteProperties(locale, bundleName);
		}
	}

	public void movePackageTask(String originBundleName, String targetBundleName) {
		//remove package priority from metadata first
		deleteKey(null, originBundleName, I18nManager.METADATA_BUNDLE_PRIORITY_KEY);
		// copy all local string files and also the metadata file
		try {
			File sourceDir = getBundlePath(originBundleName);
			File destDir = getBundlePath(targetBundleName);
			copyDirectory(sourceDir, destDir);
		} catch (IOException e) {
			log.error("Files could not be copied from " + originBundleName + " to " + targetBundleName);
		}
		deletePackage(originBundleName);
	}
	
	public void movePackageByMovingSingleKeysTask(String originBundleName, String targetBundleName) {
		Properties properties = i18nMgr.getPropertiesWithoutResolvingRecursively(i18nModule.getFallbackLocale(), originBundleName);
		Set<Object> keys = properties.keySet();		
		for (Object keyObj : keys) {
			String key = (String) keyObj;
			moveKeyToOtherBundle(originBundleName, targetBundleName, key);
		}
	}
	
	
	public void mergePackageTask(String originBundleName, String targetBundleName){
		//loop over all langs
		Set<String> allLangs = getAllLanguages();
		for (String langKey : allLangs) {
	    Locale locale = i18nMgr.getLocaleOrNull(langKey);
			Properties originProp = i18nMgr.getPropertiesWithoutResolvingRecursively(locale, originBundleName);
			Properties targetProp = i18nMgr.getPropertiesWithoutResolvingRecursively(locale, targetBundleName);
			//add every key found to target bundle if not existing
			for (Iterator<Entry<Object, Object>> keyIter = originProp.entrySet().iterator(); keyIter.hasNext();) {
				Entry<Object, Object> keyEntry = keyIter.next();
				String keyName = (String)keyEntry.getKey();
				String keyValue = (String)keyEntry.getValue();
				if (!keyValue.equals(targetProp.get(keyName))){
					log.error("There is already a key named " + keyName + " with another value in target bundle " + targetBundleName);
				}
				else {
					addKey(locale, targetBundleName, keyName, keyValue);				
				}
			}
			deletePackage(originBundleName);
		}
	}
	
	public void renameLanguageTask(Locale sourceLocale, Locale targetLocale){
	
		//check if targetLocale exists already
		Set<String> allLangKeys = i18nModule.getAvailableLanguageKeys();
		if (allLangKeys.contains(targetLocale.getLanguage())){
			log.error("Target Language " + targetLocale.getLanguage() + " already exists! ");
		}
		else {
			//get All items from sourceLocale, copy to targetLocale and delete sourceLocale
			List<I18nItem> items = i18nMgr.findExistingI18nItems(sourceLocale, null, true);
			for (I18nItem item : items) {
				String bundleName = item.getBundleName();
				String itemKey = item.getKey();
				I18nItem targetTempItem = new I18nItem(bundleName, itemKey, targetLocale, item.getBundlePriority(), item.getKeyPriority());
				Properties prop = i18nMgr.getPropertiesWithoutResolvingRecursively(sourceLocale, bundleName);
				String value = prop.getProperty(itemKey);
				i18nMgr.saveOrUpdateI18nItem(targetTempItem, value);
			  deleteKey(sourceLocale, bundleName, itemKey);
			}			
		}
	}
	
	public void moveLanguageTask(Locale sourceLocale, String sourceDir, String targetDir, boolean doMoveNoCopy){
		MoveLanguagesVisitor srcVisitor = new MoveLanguagesVisitor(sourceDir, targetDir, sourceLocale, doMoveNoCopy);
		FileUtils.visitRecursively(new File(sourceDir), srcVisitor);
	}
	
	/**
	 * 
	 * @param reallyRemoveIt true: really remove it; false: dry run, only produce logging
	 */
	public void removeXKeysTask(boolean reallyRemoveIt){
		List<String> allBundles = i18nModule.getBundleNamesContainingI18nFiles();
		Set<String> allLangs = getAllLanguages();
		int counter = 0;
		for (String langKey : allLangs) {
	    Locale locale = i18nMgr.getLocaleOrNull(langKey);
			for (String bundleName : allBundles) {
				Properties properties = i18nMgr.getPropertiesWithoutResolvingRecursively(locale, bundleName);
				Set<Object> keys = properties.keySet(); 
				for (Object keyObj : keys) {
					String key = (String) keyObj;
					if (key.endsWith("X")) {
						String value = properties.getProperty(key);
						if (StringHelper.containsNonWhitespace(value)) {
							log.warn("NONEMPTY XKEY detected in lang::" + locale.getLanguage() + " bundle::" + bundleName + " key::" + key + " value::" + value);														
							if (reallyRemoveIt) {
								addKey(locale, bundleName, key.substring(0, key.length()-1), value);
							}
						}
						log.info("XKEY detected in lang::" + locale.getLanguage() + " bundle::" + bundleName + " key::" + key);
						File propertyFileDir = i18nModule.getPropertyFilesBaseDir(locale, bundleName);
						if(propertyFileDir != null) {
							File propertyFile = i18nMgr.getPropertiesFile(locale, bundleName, propertyFileDir);
							logText.append(propertyFile + " XKEY detected in lang::" + locale.getLanguage() + " bundle::" + bundleName + " key::" + key + " value::" + value + "\n");		
							if (reallyRemoveIt) {
								deleteKey(locale, bundleName, key);							
							}
							counter++;
						}
					}
				}
			}
		}
		if (reallyRemoveIt) {
			log.info(counter + " X-Keys got removed!");
		}
	}

	public void sortKeysTask(boolean reallySortIt){
		List<String> allBundles = i18nModule.getBundleNamesContainingI18nFiles();
		Set<String> allLangs = getAllLanguages();
		int counter = 0;
		for (String langKey : allLangs) {
	    Locale locale = i18nMgr.getLocaleOrNull(langKey);
			for (String bundleName : allBundles) {
				Properties properties = i18nMgr.getPropertiesWithoutResolvingRecursively(locale, bundleName);
				if (reallySortIt) {
					// since opened as SortedProperties, save will sort it. Nothing changed, just resorted
					if (properties.size() != 0) {
						i18nMgr.saveOrUpdateProperties(properties, locale, bundleName);
					}
				} else {
					log.info("Sorting " + langKey + ":" + bundleName);
				}					
				counter++;
			}
		}
		log.info("Sorted " + counter + " properties files");
	}

	
	/**
	 * 
	 * @param reallyRemoveIt true: really remove it; false: dry run, only produce logging
	 */
	public void removeTodoKeysTask(boolean reallyRemoveIt) {
		List<String> allBundles = i18nModule.getBundleNamesContainingI18nFiles();
		Set<String> allLangs = getAllLanguages();
		int counter = 0;
		String[] comparisonStrings = {"TODO"};
		for (String langKey : allLangs) {
	    Locale locale = i18nMgr.getLocaleOrNull(langKey);
			for (String bundleName : allBundles) {
				Properties properties = i18nMgr.getPropertiesWithoutResolvingRecursively(locale, bundleName);
				Set<Object> keys = properties.keySet();
				for (Object keyObj : keys) {
					String key = (String) keyObj;
					String value = properties.getProperty(key);
					for (int i = 0; i < comparisonStrings.length; i++) {
						int pos = value.toLowerCase().indexOf(comparisonStrings[i].toLowerCase());
						if (pos != -1 && pos < 2 && !value.toLowerCase().equals("todos")) {
							log.info("TODO-Key detected in lang::" + locale.getLanguage() + " bundle::" + bundleName + " key::" + key + " value::" + value);
							if (value.length() > comparisonStrings[i].length()+1) {
								log.warn("this is a TODO-Key WITH TEXT::" + value.substring(comparisonStrings[i].length()) + "::");
							} else {
								logText.append(i18nMgr.getPropertiesFile(locale, bundleName, i18nModule.getPropertyFilesBaseDir(locale, bundleName)) + 
										" TODO-Key detected in lang::" + locale.getLanguage() + " bundle::" + bundleName + " key::" + key + " value::" + value + "\n");
								if (reallyRemoveIt) {
									deleteKey(locale, bundleName, key);
								} 
							}
							counter++;
						}
					}
				} //each key
			} //each bundle
		} 
		log.info(counter + " TODO-Keys got removed!");
	}
	
	/**
	 * 
	 * @param reallyRemoveIt true: really remove it; false: dry run, only produce logging
	 */
	public void removeEmptyKeysTask(boolean reallyRemoveIt) {
		List<String> allBundles = i18nModule.getBundleNamesContainingI18nFiles();
		int counter = 0;
		Set<String> allLangs = getAllLanguages();
		for (String langKey : allLangs) {
	    Locale locale = i18nMgr.getLocaleOrNull(langKey);
			for (String bundleName : allBundles) {
				Properties properties = i18nMgr.getPropertiesWithoutResolvingRecursively(locale, bundleName);
				Set<Object> keys = properties.keySet();
				for (Object keyObj : keys) {
					String key = (String) keyObj;
					String value = properties.getProperty(key);
					if (!StringHelper.containsNonWhitespace(value) ) {
						log.info("empty Key detected in lang::" + locale.getLanguage() + " bundle::" + bundleName + " key::" + key + " value::" + value);
						File propertyFileDir = i18nModule.getPropertyFilesBaseDir(locale, bundleName);
						if(propertyFileDir != null) {
							File propertyFile = i18nMgr.getPropertiesFile(locale, bundleName, propertyFileDir);
							logText.append(propertyFile + " empty Key detected in lang" + locale.getLanguage() + " bundle::" + bundleName + " key::" + key + " value::" + value + "\n");						
							if (reallyRemoveIt) {
								deleteKey(locale, bundleName, key);
							}
						}
					}
					counter++;
				} //each key
			} //each bundle
		} 
		log.info(counter + " empty Keys got removed!");
	}

	/**
	 * Check for keys that exist in target languages but not in EN or DE. Delete
	 * such keys in the target languages
	 * 
	 * @param reallyRemoveIt true: really delete; false: verbose dry run
	 * @param referenceLanguages array that contains the language keys that serves
	 *          as a reference (e.g. en and de)
	 * @param languages the languages that should be cleaned up
	 */
	public void removeDeletedKeys(boolean reallyRemoveIt, String[] referenceLanguages, Set<String> languages ) {
		// first get all available keys from de and en language
		Set<String> validCombinedKeys = new HashSet<>();
		//copy list to prevent concurrent modification exception
		List<String> allBundles = new ArrayList<>(i18nModule.getBundleNamesContainingI18nFiles());
		for (String bundleName : allBundles) {
			for (String refLangKey : referenceLanguages) {
				Properties properties = i18nMgr.getPropertiesWithoutResolvingRecursively(i18nMgr.getLocaleOrNull(refLangKey), bundleName);
				if (properties == null) {
					throw new OLATRuntimeException("Invalid reference language::" + refLangKey, null);
				} else {
					for (Object keyObj : properties.keySet()) {
						String key = (String) keyObj;
						String combinedKey = bundleName + ":" + key;
						validCombinedKeys.add(combinedKey);
					}						
				} 
			}
		}
		log.info("removeDeletedKeys: found " + validCombinedKeys.size() + " valid keys in " + referenceLanguages);
		//
		// For each language except DE and EN, go through all i18n files and
		// remove keys not in the valid set
		for (String langKey : languages) {
			boolean isRefLang = false;
			for (String refLangKey : referenceLanguages) {
				if (refLangKey.equals(langKey))  {
					isRefLang = true;
					break;
				}
			}
			if (isRefLang) continue;
			// Not a reference language - delete from here
			Locale locale = i18nMgr.getLocaleOrNull(langKey);
			for (String bundleName : allBundles) {
				Properties properties = i18nMgr.getPropertiesWithoutResolvingRecursively(locale, bundleName);				
				int propCount = properties.size();
				// copy keys to prevent concurrent modification
				Set<String> availableKeys = new HashSet<>();
				for (Object key : properties.keySet()) {
					availableKeys.add((String)key);
				}
				for (String key : availableKeys) {
					String combinedKey = bundleName + ":" + key;
					if (!validCombinedKeys.contains(combinedKey)) {
						if (reallyRemoveIt) {
							log.info("Deleting " + langKey + ":" + bundleName + ":" + key + " - does not exist in " + referenceLanguages);
							properties.remove(key);
						} else {
							log.info("Should be deleted: " + langKey + ":" + bundleName + ":" + key + " - does not exist in " + referenceLanguages);
						}
					}
				}
				int delCount = (propCount-properties.size());
				if (reallyRemoveIt && delCount > 0) {
					// only save when changed
					i18nMgr.saveOrUpdateProperties(properties, locale, bundleName);
					log.info("For language::" + langKey + " the in bundle:: " + bundleName + " deleted " + delCount + " keys");						
				}
				// delete empty bundles
				if (reallyRemoveIt && properties.size() == 0) {
					i18nMgr.deleteProperties(locale, bundleName);
					log.info("Bundle:: " + bundleName + " deleted for language " + langKey + "entirely because it was empty");												
				}
			}
		}
	}
	
	
		
	
	/**
	 * once again check for keys in branch (lost keys) and move them to Head
	 * 
	 * reallyCopy: set to true to create Props/keys in Head, false: only log them
	 */
	public void getLostTranslationsFromBranch(boolean reallyCopy, String[] referenceLanguages, String pathToOlatBranch, String pathToCoreBranch){
		List<String> allBundles = new ArrayList<>(i18nModule.getBundleNamesContainingI18nFiles());
		
		Set<String> allLangs = getAllLanguages();
		//loop over all langs
		int totalCounter = 0;
		for (String langKey : allLangs) {
			int langCounter = 0;
			// ignore ref langs
			boolean isRefLang = false;
			for (String refLangKey : referenceLanguages) {
				if (refLangKey.equals(langKey))  {
					isRefLang = true;
					break;
				}
			}
			if (isRefLang) continue;

			// load current language
			Locale locale = i18nMgr.getLocaleOrNull(langKey);
			for (String bundleName : allBundles) {
				int bundleCounter = 0;
				//get valid keys from ref langs and this bundle
				Set<String> allValidKeys = new HashSet<>();
				for (String refLangKey : referenceLanguages) {
					Properties properties = i18nMgr.getPropertiesWithoutResolvingRecursively(i18nMgr.getLocaleOrNull(refLangKey), bundleName);
					if (properties == null) {
						throw new OLATRuntimeException("Invalid reference language::" + refLangKey, null);
					} else {
						for (Object keyObj : properties.keySet()) {
							String key = (String) keyObj;
							allValidKeys.add(key);
						}						
					} 
				} //for
				
				//check if bundle + this locale exists in branch
				String bundlePath = bundleName.replace(".", "/");
				String langPropFileName = I18nModule.LOCAL_STRINGS_FILE_PREFIX + langKey + I18nModule.LOCAL_STRINGS_FILE_POSTFIX;
				File bundleInOlat = new File(pathToOlatBranch + bundlePath + "/" + I18nManager.I18N_DIRNAME + "/" +langPropFileName);
				File bundleInCore = new File(pathToCoreBranch + bundlePath + "/" + I18nManager.I18N_DIRNAME + "/" +langPropFileName);
				File bundleToUse;
				if (bundleInOlat.exists()){
					bundleToUse = bundleInOlat;
				} else if (bundleInCore.exists()){
					bundleToUse = bundleInCore;
				} 
				else { // no bundle found in branch, its not even worth to look after keys
					log.debug("getLostTrans: no OLD prop file found in BRANCH for locale: " + locale + " and bundle: " + bundleName + " => continue with next bundle");
					continue;
				}

				//look after all valid keys in given lang in Head
				
				Properties targetProperties = i18nMgr.getPropertiesWithoutResolvingRecursively(locale, bundleName);
				Set<Object> targetLangBundleKeys = targetProperties.keySet();
				Properties oldProps = new Properties();
				
				try(FileInputStream is = new FileInputStream(bundleToUse)) {
					oldProps.load(is);
				} catch (Exception e) {
					log.error("", e);
				}
				for (Object keyObj : allValidKeys) {
					String key = (String) keyObj;
					if (targetLangBundleKeys.contains(key)){
						//everything ok
					} else {
						//only work on keys found in reference lang (de/en) but not in this bundle
						//try to load key from branch
						if (oldProps.containsKey(key)){
							String oldValue = oldProps.getProperty(key);
							if (StringHelper.containsNonWhitespace(oldValue) && !oldValue.trim().startsWith("TODO")) {
								langCounter++;
								bundleCounter++;
								totalCounter++;
								if (reallyCopy){
									addKey(locale, bundleName, key, oldValue);
								}
								else {
									log.debug("getLostTrans: add a key from BRANCH to locale: " + locale + " and bundle: " + bundleName + " key: " + key);
								}							
							}
							else {
								log.debug("getLostTrans: ignoring invalid value::'" + oldValue + "' from BRANCH to locale: " + locale + " and bundle: " + bundleName + " key: " + key);
							}
						} 					
					} 					
				} // for: keys
				if (bundleCounter > 0) log.info("Changed " + bundleCounter + " keys for locale: " + locale + " and bundle: " + bundleName);
			} // for: bundles
		if (langCounter > 0)	log.info("Changed " + langCounter + " keys for locale: " + locale);
		} // for: langs
		if (totalCounter > 0)	log.info("Changed " + totalCounter + " keys in total");

	}
	
		
	
	
	/**
	 * @param reallyRemoveIt true: really remove it; 
	 * false: dry run, only produce logging
	 */
	public void removeReferenceLanguageCopiesTask(boolean reallyRemoveIt){
		List<String> allBundles = i18nModule.getBundleNamesContainingI18nFiles();
		// don't remove EN and DE here, this is a shared Map!!		
		int counter = 0;
		int aliasCounter = 0;
		//prepare exclusion list
		String exKeys = FileUtils.load(new File(i18nModule.getTransToolApplicationLanguagesSrcDir() + "/org/olat/core/util/i18n/devtools/exclusionKeys.txt"), "UTF-8");
		String[] exArray = exKeys.split("\n");
		List<String> exList = new ArrayList<>(Arrays.asList(exArray));

		Set<String> allLangs = getAllLanguages();
		for (String langKey : allLangs) {
	    Locale locale = i18nMgr.getLocaleOrNull(langKey);
			if (locale.toString().equals("de") || locale.toString().equals("en")) {
				// don't compare with DE and EN itself
				continue;
			}
			for (String bundleName : allBundles) {
				Properties properties = i18nMgr.getPropertiesWithoutResolvingRecursively(locale, bundleName);
				Properties refPropDe = i18nMgr.getPropertiesWithoutResolvingRecursively(new Locale("de"), bundleName);
				Properties refPropEn = i18nMgr.getPropertiesWithoutResolvingRecursively(new Locale("en"), bundleName);
				Set<Object> keys = properties.keySet();
				for (Object keyObj : keys) {
					String key = (String) keyObj;
					//dont handle if in exclusion list
					if (!exList.contains(key)){
						String value = properties.getProperty(key);
						//get ref-lang. value and compare:
						boolean foundInReferenceDe = false;
						boolean foundInReferenceEn = false;
						if (value.equals(refPropDe.getProperty(key))){
							log.info("Value of Key found in reference Language DE. lang::" + locale.getLanguage() + " bundle::" + bundleName + " key::" + key + " value::" + value);
							foundInReferenceDe = true;
						}
						if (value.equals(refPropEn.getProperty(key))){
							log.info("Value of Key found in reference Language EN. lang::" + locale.getLanguage() + " bundle::" + bundleName + " key::" + key + " value::" + value);						
							foundInReferenceEn = true;
						}
						//probably an alias if found in both ref. lang.
						boolean readyToDelete = ( foundInReferenceDe || foundInReferenceEn) ;
						if (foundInReferenceDe && foundInReferenceEn) {
							log.info("Matching value in both reference languages. lang::" + locale.getLanguage() + " bundle::" + bundleName + " key::" + key + " value::" + value);
							readyToDelete = false;
							aliasCounter ++;
						}
						if (readyToDelete && reallyRemoveIt){
							deleteKey(locale, bundleName, key);
						}
						if (readyToDelete) {
							counter++;
							logText.append(i18nMgr.getPropertiesFile(locale, bundleName, i18nModule.getPropertyFilesBaseDir(locale, bundleName)) + 
									" value of key found in reference -> remove lang::" + locale.getLanguage() + " bundle::" + bundleName + " key::" + key + " value::" + value + "\n");			
						}
					}
				}
			}
		}
		log.info(counter + " Keys found/deleted with values copied from only one reference languages!");
		log.info(aliasCounter + " Keys which seems to be alias found and NOT deleted!");
	}
	
	
	// do this only for reference language!
	public List<I18nItem> getDouplicateKeys(){
		Locale refLocale = i18nModule.getDefaultLocale();
		List<I18nItem> doupList = new ArrayList<>();
		List<String> allBundles = i18nModule.getBundleNamesContainingI18nFiles();
		Map<String, String> tempKeyMap = new HashMap<>();
		
		for (String bundleName : allBundles) {
			Properties properties = i18nMgr.getPropertiesWithoutResolvingRecursively(refLocale, bundleName);
			for (Iterator<Entry<Object, Object>> keyIter = properties.entrySet().iterator(); keyIter.hasNext();) {
				Entry<Object, Object> keyEntry = keyIter.next();
				String keyName = (String)keyEntry.getKey();
				String keyValue = (String)keyEntry.getValue();
				if (tempKeyMap.containsKey(keyName)){
					List<I18nItem> tmpItem = i18nMgr.findI18nItemsByKeySearch(keyName, refLocale, refLocale, bundleName, false);
					doupList.addAll(tmpItem);
				}
				else {
					tempKeyMap.put(keyName, keyValue);
				}				
			}			
		}
		
		log.info("found {} duplicated keys", doupList.size());		
		return doupList;
	}
	
	//do this only for reference language!
	public List<I18nItem> getDouplicateValues(){
		Locale refLocale = i18nModule.getDefaultLocale();
		List<I18nItem> doupList = new ArrayList<>();
		List<String> allBundles = i18nModule.getBundleNamesContainingI18nFiles();
		Map<String, String> tempKeyMap = new HashMap<>();
		
		for (String bundleName : allBundles) {
			Properties properties = i18nMgr.getPropertiesWithoutResolvingRecursively(refLocale, bundleName);
			for (Iterator<Entry<Object, Object>> keyIter = properties.entrySet().iterator(); keyIter.hasNext();) {
				Entry<Object, Object> keyEntry = keyIter.next();
				String keyName = (String)keyEntry.getKey();
				String keyValue = (String)keyEntry.getValue();
				if (tempKeyMap.containsKey(keyName)){
					List<I18nItem> tmpItem = i18nMgr.findI18nItemsByValueSearch(keyValue, refLocale, refLocale, bundleName, false);
					doupList.addAll(tmpItem);
				}
				else {
					tempKeyMap.put(keyValue, keyName);
				}				
			}			
		}
		
		log.info("found {} douplicated values in keys", doupList.size());
		return doupList;
	}
	
	public void deletePackage(String bundleName) {
		try {
			File path = getBundlePath(bundleName);
			if (path != null && path.exists()) {
				File[] files = path.listFiles();
				for (int i = 0; i < files.length; i++) {
					Files.deleteIfExists(files[i].toPath());
				}
				Files.delete(path.toPath());
			}
		} catch (IOException e) {
			log.error("Cannot delete bundle: {}", bundleName);
		}
	}

	private File getBundlePath(String bundleName){
		Locale locale = i18nModule.getAllLocales().get("de");
		File baseDir = i18nModule.getPropertyFilesBaseDir(locale, bundleName);
		if (baseDir != null) {
			File deFile = i18nMgr.getPropertiesFile(locale, bundleName, baseDir);
			return deFile.getParentFile();
		}
		return null;
	}

	private void copyDirectory(File sourceDir, File destDir) throws IOException {
		File[] children = sourceDir.listFiles();
		if (children != null) { // can be null when IO error or path is not a dir
			for (File sourceChild : children) {
				String name = sourceChild.getName();
				File destChild = new File(destDir, name);
				copyFile(sourceChild, destChild);
			}			
		}
	}

	private void copyFile(File source, File dest) throws IOException {
		if (!dest.exists()) {
			if (!dest.getParentFile().exists()) {
				dest.getParentFile().mkdirs();
			}
			if(!dest.createNewFile()) {
				log.error("File cannot be created at: {}", dest);
			}
		}
		
		try(InputStream in = new FileInputStream(source);
				OutputStream out = new FileOutputStream(dest)) {
			FileUtils.copy(in, out);
		} catch(Exception e) {
			log.error("", e);
		}
	}
}
