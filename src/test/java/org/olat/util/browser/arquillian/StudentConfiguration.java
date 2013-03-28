package org.olat.util.browser.arquillian;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.configuration.ConfigurationMapper;
import org.jboss.arquillian.drone.selenium.configuration.SeleniumConfiguration;
import org.jboss.arquillian.drone.spi.DroneConfiguration;

public class StudentConfiguration implements DroneConfiguration<StudentConfiguration> {
	public static final String CONFIGURATION_NAME = "student";

    private int serverPort = 14444;

    private String serverHost = "localhost";

    private String url = "http://localhost:8080";

    private int timeout = 60000;

    private int speed = 0;

    private String browser = "*firefox";
    
	private int count = 2;
	
	public StudentConfiguration(){
	}
	
	public String getConfigurationName() {
        return CONFIGURATION_NAME;
    }
	
	@Override
	public StudentConfiguration configure(ArquillianDescriptor descriptor,
			Class<? extends Annotation> qualifier) {
		ConfigurationMapper.fromArquillianDescriptor(descriptor, this, qualifier);
        return ConfigurationMapper.fromSystemConfiguration(this, qualifier);
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getServerHost() {
		return serverHost;
	}

	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public String getBrowser() {
		return browser;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	
}
