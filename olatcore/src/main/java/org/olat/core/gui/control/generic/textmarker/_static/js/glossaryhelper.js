/*
 * Javascript code used by the text marker infrastructure. 
 * Some code is borrowed from http://www.nsftools.com/misc/SearchAndHighlight.htm
 *
 * Florian Gnägi, JGS goodsolutions GmbH
 * 2006/07/14
 * Guido Schnider, OLAT
 * 2008/02/25
 * Roman Haag, frentix GmbH, www.frentix.com
 * 2009/03/01
 */
//has to be false in IE (no console there)!
var debug = false; 
 
debug ? console.log("started") : null;

	var myhash = $H();
	if (typeof o_info != "undefined") o_info["glosshash"] = myhash;
	else {
		var o_info = new Array();
		o_info["glosshash"] = myhash;		
	}
	var lastActiveGlossary = "";


function o_gloss_getLastActiveGlossArray(){
	markerArray = new Array();
	if (lastActiveGlossary != "" && lastActiveGlossary != "undefined"){
		markerArray = eval(o_glossaries[lastActiveGlossary]);
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
function o_gloss_getUniqueTargetId(targetId){
	var myhash = o_info["glosshash"];
	if (myhash != null && myhash != undefined){
		var hashnow = myhash.get(targetId);
	} else { var hashnow; }
	
	if(hashnow == undefined){	
		debug ? console.log("hash->value not found for target: " + targetId): null;
		var uniqId = ((new Date()).getTime() + "" + Math.floor(Math.random() * 1000000)).substr(0, 18);
		myhash.set(targetId,uniqId);
	}
		
	return "o_gloss" + myhash.get(targetId);
} 
 

