package de.bps.webservices.clients.onyxreporter;
//<ONYX-705>
class OnyxReporterTarget {

	private static String target;
	
	private OnyxReporterTarget(String target) {
		OnyxReporterTarget.target = target;
	}
	
	static String getTarget(){
		return target;
	}
}
