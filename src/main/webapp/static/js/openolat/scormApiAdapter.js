var debug = false;
// fallback in case dump() is not available
if (!window.dump) {
	if (window.console) {
		window.dump = function(m) {console.log(m)};	
	} else {
		window.dump = function(m) {alert(m)};
	}	
}
// Show that the SCORM Adapter is here. 
// Explicitly set it to the current window to make it also work when js code is loade via an AJAX call
var API = window;	

/*
* TODO:gs:a try to replace with prototype.js ajax stuff which works for most browsers
* right now only used for moz, as ie has some problems with state handler
* room for improvement by makeing it into one function that works for both
*/
function scormApiRequest(remoteUrl) {
	this.remoteOLATurl = remoteUrl;
	this.reqCount = 0;
}
	
	
/*************************************/


// retrieve XML document (reusable generic function);
function loadHTMLDoc(url, async, apiCall, param1, param2) {
    var scormResponse;
    var req = new XMLHttpRequest();
	req.onreadystatechange = function (event) {	
		// only if req shows "loaded"
		if (req.readyState == 4) {
			// only if "OK"
			if (req.status == 200) {
				scormResponse = loadHTMLDocExtractResponse(req.responseText);
				if (debug) dump(scormResponse);
			} else if (debug) {
				dump("There was a problem retrieving the XMLHttpRequest data:\n"+ req.statusText+"\n");
			}
		}
	};
    req.open("POST", url, async);
    req.setRequestHeader('Content-Type','application/x-www-form-urlencoded;charset=UTF-8'); 
    req.send('apiCall='+ apiCall + '&apiCallParamOne='+ param1 + '&apiCallParamTwo='+ encodeURIComponent(param2));
    if (debug) dump('apiCall='+ apiCall + '&apiCallParamOne='+ param1 + '&apiCallParamTwo='+ encodeURIComponent(param2));
    return scormResponse;
}

function loadHTMLDocExtractResponse(responseText) {
	if("<html><body></body></html>" == responseText) {
		return "";// initial call
	}
	return responseText.substring(responseText.indexOf("<p>") + 3, responseText.indexOf("</p>"));
}

/*****************************************************************
* function to produce incremental ints
* to cheat ie chache
******************************************************************/
var counter = 0;
function increment(){
	return counter++;
}


/******************************************************************
* passes the scorm api calls to the backend
* over a synchronous XmlHttpRequest.
* Code uses different ways for moz and ie.
*******************************************************************/

function passApiCall(apiCall, param1, param2) {
	try {
		return loadHTMLDoc(olatCommandUri, false, apiCall, param1, param2);
	} catch(e) {
		if(window.console) console.log(e);
	}
}

/**
* triggers the onunload stuff often used in scorm content to finish an sco by
* opening the iframe document for writing (IE) or replacing the content doc (Mozilla).
*/
function olatonunload(){
	if (debug) dump("func:olatonunload: is called\n");
	if(window.frameId && document.getElementById(frameId) && this.frames[frameId]){
		// Mozilla and others
		var iframeDoc = document.getElementById(frameId).contentDocument;
		iframeDoc.location.replace("about:blank");
		iframeDoc = null;
		
		var delayReq = new XMLHttpRequest();
		//"false" waits until the result arrived;
		delayReq.open('GET', olatCommandUri, false );
		delayReq.send(null);
	}
	return true;
}

var openolatScormUnloadQueue = new Array();

function queueCall(apiCall, param1, param2) {
	var entry = new Object();
	entry.apiCall = apiCall;
	entry.param1 = param1;
	entry.param2 = param2;
	openolatScormUnloadQueue.push(entry);
}

/******************************************************************
* SCORM API FUNCTIONS 
* These functions are used in the SCORM content to
* communicate with the LMS.
*******************************************************************/
function LMSInitialize (s) {
	return passApiCall('LMSInitialize',s,'');
}
function LMSFinish (s) {
	var finishedResult = passApiCall('LMSFinish', s, '');
	if(typeof val === "undefined") {
		// Communication problem, try to send all queued data as beacon
		queueCall('LMSFinish', s, '');
		var data = JSON.stringify(openolatScormUnloadQueue);
		openolatScormUnloadQueue = new Array();
		navigator.sendBeacon(olatCommandUri + "/batch/data/", data);
	}
	// Immediately close module, ping OpenOlat main window to take over control
	setTimeout(function(){
		try {
			pingAfterFinish();					
		} catch(e) {
			if (window.console) console.log("Problem pinging OpenOlat: ", e);
		}	
	});
	return finishedResult;
}
function LMSSetValue (l, r) {
	var val = passApiCall('LMSSetValue',l,r);
	if(typeof val === "undefined") {
		queueCall('LMSSetValue', l, r);
		val = r;
	}
	return val;
}
function LMSGetValue (s) {
	return passApiCall('LMSGetValue',s,'');
}
function LMSGetLastError () {
	var val = passApiCall('LMSGetLastError','','');
	if(typeof val === "undefined") {
		val = "0";
	}
	return val;
}
function LMSGetErrorString (s) {
	var val = passApiCall('LMSGetErrorString',s,'');
	if(typeof val === "undefined") {
		val = "No Error";
	}
	return val;
}
function LMSGetDiagnostic (s) {
	return passApiCall('LMSGetDiagnostic',s,'');
}
function LMSCommit (s) {
	var val = passApiCall('LMSCommit',s,'');
	if(typeof val === "undefined") {
		queueCall('LMSCommit', s, '');
		val = r;
	}
	return val;
}