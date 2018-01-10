/*
 * Javascript code used by the text marker infrastructure. 
 * Some code is borrowed from http://www.nsftools.com/misc/SearchAndHighlight.htm
 *
 * Florian Gnaegi, JGS goodsolutions GmbH
 * 2006/07/14
 * Guido Schnider, OLAT
 * 2008/02/25
 * Roman Haag, frentix GmbH, www.frentix.com
 * 2009/03/01
 */
//has to be false in IE (no console there)!
var debug = false; 
 
debug ? console.log("started") : null;

	var myhash = new Hashtable();
	if (typeof o_info != "undefined") o_info["glosshash"] = myhash;
	else {
		var o_info = new Array();
		o_info["glosshash"] = myhash;		
	}
	var lastActiveGlossary = "";


function o_gloss_getLastActiveGlossArray(){
	markerArray = new Array();
	if (lastActiveGlossary != "" && lastActiveGlossary != "undefined"){
		markerArray = eval(jQuery(document).data("o_glossaries")[lastActiveGlossary]);
	}
	return markerArray;
}

function o_gloss_setLastActiveGlossary(glossaryId){
	lastActiveGlossary = glossaryId;
}

/*
 * creates a unique targetId for any occurrence and saves it in a global cache.
 * 
 * March 2009  Roman Haag, roman.haag@frentix.com
 */
function o_gloss_getUniqueTargetId(targetId) {
	var myhash = o_info["glosshash"];
	var hashnow = null;
	if (myhash != null && myhash != undefined) {
		hashnow = myhash.get(targetId);
	}
	if(hashnow == null) {	
		var uniqId = ((new Date()).getTime() + "" + Math.floor(Math.random() * 1000000)).substr(0, 18);
		myhash.put(targetId,uniqId);
	}
	return "o_gloss" + myhash.get(targetId);
} 
 

