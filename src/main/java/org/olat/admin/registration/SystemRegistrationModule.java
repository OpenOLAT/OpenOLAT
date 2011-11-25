package org.olat.admin.registration;

import java.util.UUID;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.util.StringHelper;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  25 nov. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SystemRegistrationModule extends AbstractOLATModule {
	
	protected static final String CONF_KEY_PUBLISH_WEBSITE = "publishWebsite";
	protected static final String CONF_KEY_WEBSITE_DESCRIPTION = "websiteDescription";
	protected static final String CONF_KEY_NOTIFY_RELEASES = "notifyReleases";
	protected static final String CONF_KEY_EMAIL = "email";
	// not configurable by user
	protected static final String CONF_KEY_REGISTRATION_CRON = "registrationCron";
	protected static final String CONF_KEY_IDENTIFYER = "instanceIdentifyer";
	//location described by language, e.g. "Winterthurerstrasse 190, Zürich", or "Dresden"....
	protected static final String CONF_KEY_LOCATION = "location";
	// the geolocation derived with a google maps service for usage to place markers on a google map
	protected static final String CONF_KEY_LOCATION_COORDS="location_coords";
	// on first registration request, the registration.olat.org creates a secret key - needed for future updates
	private static final String CONF_SECRETKEY = "secret_key";
	
	private boolean publishWebsite;
	private String websiteDescription;
	private boolean notifyReleases;
	private String email;
	private String location;
	private String locationCoordinates;
	private String secretKey;
	private String instanceIdentifier;

	@Override
	public void init() {
		String secretKeyObj = getStringPropertyValue(CONF_SECRETKEY, true);
		if(StringHelper.containsNonWhitespace(secretKeyObj)) {
			secretKey = secretKeyObj;
		}
		String publishWebsiteObj = getStringPropertyValue(CONF_KEY_PUBLISH_WEBSITE, true);
		if(StringHelper.containsNonWhitespace(publishWebsiteObj)) {
			publishWebsite = "true".equals(publishWebsiteObj);
		}
		String websiteDescriptionObj = getStringPropertyValue(CONF_KEY_WEBSITE_DESCRIPTION, true);
		if(StringHelper.containsNonWhitespace(websiteDescriptionObj)) {
			websiteDescription = websiteDescriptionObj;
		}
		String notifyReleasesObj = getStringPropertyValue(CONF_KEY_NOTIFY_RELEASES, true);
		if(StringHelper.containsNonWhitespace(notifyReleasesObj)) {
			notifyReleases = "true".equals(notifyReleasesObj);
		}
		
		String emailObj = getStringPropertyValue(CONF_KEY_EMAIL, true);
		if(StringHelper.containsNonWhitespace(emailObj)) {
			email = emailObj;
		}
		String locationObj = getStringPropertyValue(CONF_KEY_LOCATION, true);
		if(StringHelper.containsNonWhitespace(locationObj)) {
			location = locationObj;
		}
		String locationCoordinatesObj = getStringPropertyValue(CONF_KEY_LOCATION_COORDS, true);
		if(StringHelper.containsNonWhitespace(locationCoordinatesObj)) {
			locationCoordinates = locationCoordinatesObj;
		}
		
		// Check if instance identifier property exists
		String instanceIdentifierObj = getStringPropertyValue(CONF_KEY_IDENTIFYER, false);
		if (StringHelper.containsNonWhitespace(instanceIdentifierObj)) {
			instanceIdentifier = instanceIdentifierObj;
		} else {
			instanceIdentifier = UUID.randomUUID().toString();
			moduleConfigProperties.setStringProperty(CONF_KEY_IDENTIFYER, instanceIdentifier, true);
		}
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

	@Override
	protected void initDefaultProperties() {
		publishWebsite = getBooleanConfigParameter(CONF_KEY_PUBLISH_WEBSITE, false);
		websiteDescription = getStringConfigParameter(CONF_KEY_WEBSITE_DESCRIPTION, "", true);
		notifyReleases = getBooleanConfigParameter(CONF_KEY_NOTIFY_RELEASES, false);
		email = getStringConfigParameter(CONF_KEY_EMAIL, "", false);
		location = getStringConfigParameter(CONF_KEY_LOCATION, "", false);
		locationCoordinates = getStringConfigParameter(CONF_KEY_LOCATION_COORDS, "", false);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	public String getSecretKey() {
		return secretKey;
	}
	
	public void setSecretKey(String secretKey) {
		setStringProperty(CONF_SECRETKEY, secretKey, true);
	}

	public String getInstanceIdentifier() {
		return instanceIdentifier;
	}

	public boolean isPublishWebsite() {
		return publishWebsite;
	}

	public void setPublishWebsite(boolean publishWebsite) {
		setBooleanProperty(CONF_KEY_PUBLISH_WEBSITE, publishWebsite, true);
	}

	public String getWebsiteDescription() {
		return websiteDescription;
	}

	public void setWebsiteDescription(String websiteDescription) {
		setStringProperty(CONF_KEY_WEBSITE_DESCRIPTION, websiteDescription, true);
	}

	public boolean isNotifyReleases() {
		return notifyReleases;
	}

	public void setNotifyReleases(boolean notifyReleases) {
		setBooleanProperty(CONF_KEY_NOTIFY_RELEASES, notifyReleases, true);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		setStringProperty(CONF_KEY_EMAIL, email, true);
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		setStringProperty(CONF_KEY_LOCATION, location, true);
	}

	public String getLocationCoordinates() {
		return locationCoordinates;
	}

	public void setLocationCoordinates(String locationCoordinates) {
		setStringProperty(CONF_KEY_LOCATION_COORDS, locationCoordinates, true);
	}
	
	
	
	
	
	
	
	
	

}
