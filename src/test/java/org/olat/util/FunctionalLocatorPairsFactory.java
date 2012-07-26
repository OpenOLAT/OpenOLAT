package org.olat.util;

public class FunctionalLocatorPairsFactory {
	private FunctionalUtil functionalUtil;
	
	public FunctionalLocatorPairsFactory(FunctionalUtil functionalUtil){
		this.functionalUtil = functionalUtil;
	}
	
	public String getLocatorOfSite(String site){
		StringBuffer locatorBuffer = new StringBuffer();
		
		locatorBuffer.append("css=.")
		.append(functionalUtil.getOlatNavigationSiteCss())
		.append(".")
		.append(site)
		.append(" a");
		
		return(locatorBuffer.toString());
	}
	
	public String getApprovalOfSite(String site){
		StringBuffer approvalBuffer = new StringBuffer();
		
		approvalBuffer.append("css=.")
		.append(functionalUtil.getOlatNavigationSiteCss())
		.append(".")
		.append(functionalUtil.getOlatActiveNavigationSiteCss())
		.append(".")
		.append(site);
		
		return(approvalBuffer.toString());
	}

	public FunctionalUtil getFunctionalUtil() {
		return functionalUtil;
	}

	public void setFunctionalUtil(FunctionalUtil functionalUtil) {
		this.functionalUtil = functionalUtil;
	}
}
