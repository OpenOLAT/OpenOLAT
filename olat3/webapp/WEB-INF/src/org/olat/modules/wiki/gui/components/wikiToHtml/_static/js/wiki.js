function changeAnchorTargets(id) {
	var div = document.getElementById(id);
	var anchors = div.getElementsByTagName('a');
	for (var i=0; i < anchors.length; i++){
		var anchor = anchors[i];
		var openInNewWindow = false;
		//set interwiki and externallink link to open in new window 
		if (Element.readAttribute(anchor, "class")) {
			if (Element.readAttribute(anchor, "class") == "externallink") {
				if(Element.readAttribute(anchor,"href").indexOf("mailto:") != -1) {
					anchor.setAttribute("class", "b_link_mailto");
					openInNewWindow = false;
				} else {
					openInNewWindow = true;
				}
			}
			if (Element.readAttribute(anchor, "class") == "interwiki") openInNewWindow = true;
		}
		//open media links in new window, but only if file exists
		if (Element.readAttribute(anchor, "title")) {
			var href = Element.readAttribute(anchor,"href");
			if (!Element.readAttribute(anchor, "class") && Element.readAttribute(anchor, "title").indexOf("Media") != -1) { //normal media link file found
				openInNewWindow = true;
				//modify link to non ajax mode as opening in new window with ajax mode on fails
				if (href.indexOf(":1/") != -1) {
					var pre = href.substr(0, href.indexOf(":1/"));
					var post = href.substr(href.indexOf(":1/")+3, href.length);
					anchor.setAttribute("href", pre+":0/"+post);
				}
			} else if (Element.readAttribute(anchor, "class") == "edit" && Element.readAttribute(anchor, "title").indexOf("Media:") != -1) { //media file not found
				href = href.substr(0, href.indexOf("Edit:topic"));
				href = href+"Upload";
				anchor.setAttribute("href", href);
			}
		}
		if (openInNewWindow) anchor.target = "_blank";
	}
}