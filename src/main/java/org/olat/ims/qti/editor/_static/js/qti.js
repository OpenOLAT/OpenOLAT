function shadeFormElement(elementId){
	// Makes a form input element uneditable. Uses CSS to shade content
	// PARAM: The ID of the element to be shaded
	// PARAM: The default value used when unshading element
	// (fg)
	var elem = document.getElementById(elementId);
	elem.className = "b_disabled";
	elem.readOnly = true;
	// set input focus (cursor) to current element
	elem.focus();
}
function unShadeFormElement(elementId){
	// Makes a form input element editable
	// PARAM: The ID of the element to be unshaded
	// (fg)
	var elem = document.getElementById(elementId);
	elem.className = "";
	elem.readOnly = false;
	// set input focus (cursor) to current element
	elem.focus();
}
function shadeFormTextareas(){
	// Makes all form input element uneditable. Uses CSS to shade content
	// (fg)
	var areas = document.getElementsByTagName("textarea");
	if (areas == null) return;
	for (var i=0; i < areas.length; ++i){
		areas[i].className = "b_disabled";
		areas[i].readOnly = true;
	}
}

function unShadeFormTextareas(){
	// Makes all form input text areas elements editable
	// (fg)
	var areas = document.getElementsByTagName("textarea");
	if (areas == null) return;
	for (var i=0; i < areas.length; ++i){
		areas[i].className = "";
		areas[i].readOnly = false;
	}
}
// moved to package js - delete here if enabeld: guido
function changeQtiPreviewImage(mediaBaseURL){
	// Changes the image preview for the currently selected image
	// PARAM: mediaBaseURL will be prependend to the image 
	// (fg)
	var select = document.getElementById('mediaselect');

	if (select == null) { return; }
	var selected = select.value;

	var previewImage = document.getElementById('previewImage');
	if (selected == ""){
		previewImage.src = conpath + "/qti/empty.gif";	
	} else {
		previewImage.src = mediaBaseURL + selected;
		previewImage.alt = selected;
	}
	// check file type radio
	document.getElementById("type_file").checked = "checked";
}

function changeQtiPreviewImageFromUri(){
	// Changes the preview image when entering an new media uri
	var uri = document.getElementById('imguri');
	var previewImage = document.getElementById('previewImage');
	if(previewImage){
		previewImage.src = uri.value;
		previewImage.alt = uri.value;
	}
	// check uri type radio
	document.getElementById("type_uri").checked = "checked";
}