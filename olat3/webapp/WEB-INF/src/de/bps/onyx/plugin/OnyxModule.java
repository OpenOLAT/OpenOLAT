
/**
 *
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 *
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.onyx.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.id.OLATResourceable;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti.process.ImsRepositoryResolver;
import org.olat.ims.qti.process.Resolver;

/**
 * @author Ingmar Kroll
 */
public class OnyxModule extends AbstractOLATModule {

	private static String onyxPluginWSLocation;
	public static ArrayList<PlayerTemplate> PLAYERTEMPLATES;
	/* holds the local config name which is sent to the remote onyxplugin -> onyxplugin must have 
	 * a config corresponding to this name */
	private static String configName;

	/**
	 * @return Returns the configName.
	 */
	public static String getConfigName() {
		return configName;
	}

	/**
	 * @param configName The configName to set.
	 */
	public void setConfigName(String configName) {
		this.configName = configName;
	}

	/**
	 * @param pluginWSLocation The pluginWSLocation to set.
	 */
	public void setOnyxPluginWSLocation(String onyxPluginWSLocation) {
		this.onyxPluginWSLocation = onyxPluginWSLocation;
	}
	
	/**
	 * @return Returns the userViewLocation.
	 */
	public static String getUserViewLocation() {
		return onyxPluginWSLocation + "/onyxrun";
	}

	/**
	 * @return Returns the pluginWSLocation.
	 */
	public static String getPluginWSLocation() {
		return onyxPluginWSLocation + "/services";
	}

	/**
	 * [used by spring]
	 */
	private OnyxModule() {
		//
	}


	@Override
	public void init() {
		PLAYERTEMPLATES = new ArrayList<PlayerTemplate>();
		PlayerTemplate pt = new PlayerTemplate("onyxdefault", "templatewithtree");
		PLAYERTEMPLATES.add(pt);
		pt = new PlayerTemplate("onyxwithoutnav", "templatewithouttree");
		PLAYERTEMPLATES.add(pt);
	}

	@Override
	protected void initDefaultProperties() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initFromChangedProperties() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

	public class PlayerTemplate {
		public String id;
		public String i18nkey;

		/**
		 * @param id
		 * @param i18nkey
		 */
		public PlayerTemplate(String id, String i18nkey) {
			this.id = id;
			this.i18nkey = i18nkey;
		}
	}

	public static boolean isOnyxTest(OLATResourceable res){
	if(res.getResourceableTypeName().equals(TestFileResource.TYPE_NAME) || res.getResourceableTypeName().equals(SurveyFileResource.TYPE_NAME) ){
		Resolver resolver = new ImsRepositoryResolver(res);
		//search for qti.xml, it not exists for qti2
		if (resolver.getQTIDocument()==null){
			return true;
		}else{
			return false;
		}
	}else{
		return false;
	}
}

public static boolean isOnyxTest(File zipfile){
	BufferedReader br = null;
	try {
			File mani = new File(zipfile.getAbsolutePath()+"/imsmanifest.xml");
			br = new BufferedReader(new FileReader(mani));
			while (br.ready()) {
				String l = br.readLine();
				if (l.indexOf("imsqti_xmlv2p1") != -1 ||
						l.indexOf("imsqti_test_xmlv2p1") != -1 ||
						l.indexOf("imsqti_assessment_xmlv2p1") != -1) {
					br.close();
					return true;
					}
			}
			br.close();
		} catch (Exception e) {
			try {
				br.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
			}
		}

		return false;

	}
}

