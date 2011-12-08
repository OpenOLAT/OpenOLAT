package org.olat.upgrade;

public class DatabaseUpgrade extends OLATUpgrade {
	
	private final String version;
	
	public DatabaseUpgrade(String version) {
		this.version = version;
	}

	@Override
	public String getVersion() {
		return version;
	}

	
	@Override
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		return false;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		return false;
	}
}
