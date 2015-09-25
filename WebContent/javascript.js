/*
Created by Rafael Goncalves
Information Management Group (IMG)
School of Computer Science
University of Manchester
Last updated: 25-Apr-2013
*/

/**
 * The original code was extended and modified by Ruvin Yusubov
 * MSc in Advance Computer Science
 * School of Computer Science
 * The University of Manchester
 **/

var curAlarmClass = "";
var curAlarmCategory = "";
var curPrintoutCategory = "";

var buttonPressed = false;

var ctrlPressed = false;
var clickedItems = "";
var currentInstancesString = "";
var currentInstancesArray;
var curentNumOfClikedItems = 0;

// Toggle subtree & swap image
function toggleSubTree(id, img_id) {
	var e = document.getElementById(id);
	var img = document.getElementById(img_id);
	if (e.style.display == '') {// ===
		e.style.display = 'none';
		img.src = 'images/button-closed.png';
	} else {
		e.style.display = '';
		img.src = 'images/button-open.png';
	}
}

// Open subtree
function open(divid) {
	divid.style.display = '';
}

// Close subtree
function close(divid) {
	divid.style.display = 'none';
}

// Switch to open (minus) picture
function openPic(divid) {
	var img = document.getElementById(divid);
	if (img != null)
		img.src = 'images/button-open.png';
}

// Switch to closed (plus) picture
function closePic(divid) {
	var img = document.getElementById(divid);
	if (img != null)
		img.src = 'images/button-closed.png';
}

// Replace the details-pane text with the usage of a selected term
function showUsg(terms) {
	var ele = document.getElementById('details');
	var tArray = terms.split(';');
	var string = "<ul>";
	for (var i = 0; i < tArray.length; i++) {
		if (!tArray[i].startsWith('Class: ')
				&& !tArray[i].startsWith('Individual: '))
			string += "<li style='list-style-type:square'>" + tArray[i]
					+ "</li>";
	}
	string += "</ul>";
	ele.innerHTML = string;
}

function focusOn(someConcept) {
	var xmlhttp;
	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp = new XMLHttpRequest();
	} else {// code for IE6, IE5
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
			
			//alert(buttonPressed);
			
			document.getElementById("progressBackgroundFilter").className = ''; // Hide the image after the response from the server
			document.getElementById("processMessage").className = '';
			document.getElementById("processMessage").innerHTML = '';

			// Javascript function JSON.parse to parse JSON data
			clearSelection();//think about doing it just when ctrl is not pressed!
			
			//if (buttonPressed == true)
				//ctrlPressed = true;

			var jsonObj = JSON.parse(xmlhttp.responseText);

			var tInd = "";
			var tConcepts = "";
			var tParents = "";
			var alarmClass = jsonObj.alarmClass;
			var alarmCategory = jsonObj.alarmCategory;
			var printoutCategory = jsonObj.printoutCategory;
			
			curAlarmClass = alarmClass;
			curAlarmCategory = alarmCategory;
			curPrintoutCategory = printoutCategory;
			
			tInd = (tInd + jsonObj.relatedInstances);
			
			//alert(tInd[0]);
			
			if (tInd.length != 0)
				tInd = tInd.split(',');
			tConcepts = tConcepts + jsonObj.relatedConcepts;

			//alert(tInd[0]);
			
			if (tConcepts.length != 0)
				tConcepts = tConcepts.split(',');

			tParents = tParents + jsonObj.parents;

			expand(tParents);

			var lastInstancesString = "";
			if (ctrlPressed == false) {
				//alert("gfg");
				document.getElementById('alarmclass').value = "";
				document.getElementById('alarmcategory').value = "";
				document.getElementById('printoutcategory').value = "";
				
				curAlarmClass = "";
				curAlarmCategory = "";
				curPrintoutCategory = "";
				
				
				clickedItems = someConcept;
				
				currentInstancesString = "";
				
				for (var i = 0; i < tInd.length; i++){
					if (lastInstancesString != "")
						lastInstancesString = lastInstancesString + "," + tInd[i]; 
					else
						lastInstancesString = tInd[i];
				}
				currentInstancesString = lastInstancesString;
				currentInstancesArray = lastInstancesString.split(',');
			}
			else{
				document.getElementById('alarmclass').value = alarmClass;
				document.getElementById('alarmcategory').value = alarmCategory;
				document.getElementById('printoutcategory').value = printoutCategory;
				
				curAlarmClass = alarmClass;
				curAlarmCategory = alarmCategory;
				curPrintoutCategory = printoutCategory;
				
				if (clickedItems != "")
					clickedItems = clickedItems + "," + someConcept;
				else
					clickedItems = someConcept;
				
				for (var i = 0; i < tInd.length; i++){
					if (lastInstancesString != "")
						lastInstancesString = lastInstancesString + "," + tInd[i]; 
					else
						lastInstancesString = tInd[i];
				}
				
				if (curentNumOfClikedItems > 1)
					resultString = intersectionOfChoices(currentInstancesString, lastInstancesString);
				else
					resultString = lastInstancesString;
				currentInstancesString = resultString;
				currentInstancesArray = resultString.split(',');	
			}
			
			var out = "";
			numOfInstances = 0;
			if (currentInstancesString.length > 0)
				numOfInstances = currentInstancesArray.length;
			
			out = "<u>Document" + " (" + numOfInstances + ") " + "</u>";
			for (var i = 0; i < numOfInstances; i++) {
				tIndNameAndSource = currentInstancesArray[i].split('@');
				out += "<li>\n<img src='images/button-closed.png' style='visibility:hidden'/> \n<a href='" + tIndNameAndSource[1] + "' id='"
						+ tIndNameAndSource[0]
						+ "' name='"
						+ tIndNameAndSource[0]
						+ "' "
						+ " class='instance' target='_blank'>" + tIndNameAndSource[0] + "</a>\n</li>\n";
			}
			
			//if (ctrlPressed == false) {
			//	clearSelection();
			//}
 
			document.getElementById("documentInstances").innerHTML = out;


			for (var i = 0; i < tConcepts.length; i++) {
				var els = document.getElementsByName(tConcepts[i]);
				for (var j = 0; j < els.length; j++) {
					els[j].style.backgroundColor = '#ffd700';//'#fefdd8';
					els[j].style.color = '#101010';
				}
			}
			
			var tClicked = clickedItems.split(',');
			
			for (var i = 0; i < tClicked.length; i++){
				//var els = document.getElementsByName(someConcept);
				var els = document.getElementsByName(tClicked[i]);
				
				for (var j = 0; j < els.length; j++) {
					els[j].style.backgroundColor = '#ff1493';//'#fefdd8';
					els[j].style.color = '#101010';
				}
			}
						
//			var els = document.getElementsByName(tClicked[tClicked.length-1]);
//			
//			for (var j = 0; j < els.length; j++) {
//				els[j].style.backgroundColor = '#ff1493';//'#fefdd8';
//				els[j].style.color = '#101010';
//			}
			buttonPressed = false;
		}
	};
	
	var tArray = document.getElementsByTagName("a"); 
	var alreadyHighlighted = "";
	var isAlarmButtonPressed = "F";
	
	var isValidationRight = true;
	
	var alarmCharacteristics = someConcept.split(',');
	
	var x;
	var x1 = "";
	
	//alert(alarmCharacteristics.legth + someConcept);
	
	if (/*alarmCharacteristics.length > 1*/ buttonPressed ){//can replace with buttonPressed
		ctrlPressed = true;
		isAlarmButtonPressed = "T";
		
		var alarmClass = document.getElementById('alarmclass').value;
		var alarmCategory = document.getElementById('alarmcategory').value;
		var printoutCategory = document.getElementById('printoutcategory').value;
		
		
		if (alarmClass.length == 0 && alarmCategory.length == 0 && printoutCategory.length == 0){
			isValidationRight = false;
			buttonPressed = false;
		}
		
	}
	
	if (isValidationRight){
	if (ctrlPressed == false){
		alreadyHighlighted = "";
		curentNumOfClikedItems = 1;
	}
	else{
		curentNumOfClikedItems = curentNumOfClikedItems + 1;
		
		for(var i = 0; i < tArray.length; i++) {
			if(tArray[i].style.backgroundColor == "rgb(255, 215, 0)" && tArray[i].style.color == "rgb(16, 16, 16)") { 
				
				if (alreadyHighlighted == "")
					alreadyHighlighted = tArray[i].id;
				else
					alreadyHighlighted = alreadyHighlighted + "," + tArray[i].id; 
			} 
		}
	}
	
	if (alreadyHighlighted != "")
		alreadyHighlighted = alreadyHighlighted + "," + someConcept;
	else
		alreadyHighlighted = someConcept;
	//alert(someConcept);
	var isCtrlPressed = "";
	
	if (ctrlPressed == false){
		//alert("hi");
		document.getElementById('alarmclass').disabled = false;
		document.getElementById('alarmcategory').disabled = false;
		document.getElementById('printoutcategory').disabled = false;
		isCtrlPressed = "F";
	}
	else
		isCtrlPressed = "T";
	
	var alarmClass = document.getElementById('alarmclass').value;
	var alarmCategory = document.getElementById('alarmcategory').value;
	var printoutCategory = document.getElementById('printoutcategory').value;
	

	if (!buttonPressed){
		alarmClass = curAlarmClass;
		alarmCategory = curAlarmCategory;
		printoutCategory = curPrintoutCategory;
	}
	else{
		curAlarmClass = alarmClass;
		curAlarmCategory = alarmCategory;
		curPrintoutCategory = printoutCategory;
		if (curAlarmClass != ""){
			document.getElementById('alarmclass').disabled = true;
		}
		
		if (curAlarmCategory != ""){
			document.getElementById('alarmcategory').disabled = true;
		}
		
		if (curPrintoutCategory != ""){
			document.getElementById('printoutcategory').disabled = true;
		}
	}
		
	//alert(isAlarmButtonPressed);
	
	document.getElementById("progressBackgroundFilter").className = 'progressBackgroundFilter';
	document.getElementById("processMessage").className = 'processMessage';
	document.getElementById("processMessage").innerHTML = '<img id="ctl00_Image1" src="images/loading.gif" style="border-width: 0px;" /><br />Loading...';

	xmlhttp.open("POST","http://localhost:8080/MSc_project/SimpleServlet",true);
	xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
	xmlhttp.send("alreadyHighlighted=" + alreadyHighlighted + "&" 
				+ "isCtrlPressed=" + isCtrlPressed + "&"
				+ "isAlarmButtonPressed=" + isAlarmButtonPressed + "&"
				+ "alarmClass=" + alarmClass + "&"
				+ "alarmCategory=" + alarmCategory + "&"
				+ "printoutCategory=" + printoutCategory + "&"
				+ "curentNumOfClikedItems=" + curentNumOfClikedItems);
	}
}

//Find and return an intersection between the instances or concepts based on their names. Names separated by ",". Result also will be provided 
//as a set of names separated by ",".
function intersectionOfChoices(s1, s2){
	
	t1 = s1.split(',');
	t2 = s2.split(',');
	resultS = "";
	
	for (var i = 0; i < t1.length; i++){
		if (s2.indexOf(t1[i]) > -1){
			if (resultS != "")
				resultS = resultS + "," + t1[i]; 
			else
				resultS = t1[i];
		}
	}
	
	for (var i = 0; i < t2.length; i++){
		if (s1.indexOf(t2[i]) > -1 && resultS.indexOf(t2[i]) == -1){
			if (resultS != "")
				resultS = resultS + "," + t2[i]; 
			else
				resultS = t2[i];
		}
	}
	
	return resultS;
}


function isKeyPressed(event) {
	
	//event.preventDefault();
	
	if (event.ctrlKey==1){
		ctrlPressed = true;
		//alert("Hi");	If you will unhighlight it then will see that 
	}
	else
		ctrlPressed = false;
}

/*function myFunction1(event) {
	if (event.ctrlKey==1)
		ctrlPressed = true;
	else
		ctrlPressed = false;
}

function myFunction2(event) {
	if (event.ctrlKey==1)
		ctrlPressed = true;
	else
		ctrlPressed = false;	
}*/


// FTemp
// Unfocus all terms
function unFocusAll() {
	var tArray = document.getElementsByTagName("a");
	for (var i = 0; i < tArray.length; i++) {
		if (tArray[i].classList.contains('concept')
				|| tArray[i].classList.contains('instance')) {
			tArray[i].style.backgroundColor = '#feffff';
			tArray[i].style.color = '#8e8e8e';
		}
	}
}

// Focus on all terms
function focusAll() {
	var tArray = document.getElementsByTagName("a");
	for (var i = 0; i < tArray.length; i++) {
		if (tArray[i].classList.contains('concept')
				|| tArray[i].classList.contains('instance')) {
			tArray[i].style.backgroundColor = '#feffff';
			tArray[i].style.color = '#101010';
		}
	}
}

// Highlight a specified term
function highlightToken(token) {
	unHighlightAllTokens();
	var t = document.getElementsByName(token);
	for (var i = 0; i < t.length; i++) {
		t[i].style.fontWeight = 'bold';
		t[i].style.backgroundColor = '#ffd700';//'#fefdd8';
		t[i].style.color = '#101010';
	}
}

// Unhighlight all terms
function unHighlightAllTokens() {
	var tArray = document.getElementsByTagName("a");
	for (var i = 0; i < tArray.length; i++) {
		if (tArray[i].style.fontWeight == 'bold') {
			tArray[i].style.fontWeight = 'normal';
			tArray[i].style.backgroundColor = '#feffff';
		}
	}
	
}

// Clear selected and striken out terms
function clearSelection() {
	focusAll();
	unHighlightAllTokens();
}

function clearSelection2() {
	focusAll();
	unHighlightAllTokens();
	ctrlPressed = false;
	currentInstancesString = "";
	clickedItems = "";
//	currentInstancesArray;
	curentNumOfClikedItems = 0;
	buttonPressed = false;
	
	curAlarmClass = "";
	curAlarmCategory = "";
	curPrintoutCategory = "";
	
	document.getElementById('alarmclass').value = "";
	document.getElementById('alarmcategory').value = "";
	document.getElementById('printoutcategory').value = "";
		
	document.getElementById('alarmclass').disabled = false;
	document.getElementById('alarmcategory').disabled = false;
	document.getElementById('printoutcategory').disabled = false;
}

// Collapse all subtrees of the specified facet
function collapseAll(type) { // Ruvin Need to Add reader Stuff
	var tArray = document.getElementsByTagName("ul");
	for (var i = 0; i < tArray.length; i++) {
		
		 if ((type == "task" && tArray[i].getAttribute("facetType") == "task")
			|| (type == "functionalarea" && tArray[i].getAttribute("facetType") == "functionalarea") 
			|| (type == "reader" && tArray[i].getAttribute("facetType") == "reader")
			|| (type == "alarm" && tArray[i].getAttribute("facetType") == "alarm")
			|| (type == "document" && tArray[i].getAttribute("facetType") == "document")) {
			
			var imgId;
			if (tArray[i].id.indexOf('_Children') > -1) {
				close(tArray[i]);
				imgId = tArray[i].id.replace("_Children", "_Img");
			} else {
				imgId = tArray[i].id.replace("_Main", "_Img");
			}
			closePic(imgId);
		}
	}
}

// Expand all subtrees of the specified facet
function expandAll(type) { // Ruvin Need to Add reader Stuff
	var tArray = document.getElementsByTagName("ul");
	for (var i = 0; i < tArray.length; i++) {

		if ((type == "task" && tArray[i].getAttribute("facetType") == "task")
				|| (type == "functionalarea" && tArray[i].getAttribute("facetType") == "functionalarea") 
			|| (type == "reader" && tArray[i].getAttribute("facetType") == "reader")
			|| (type == "alarm" && tArray[i].getAttribute("facetType") == "alarm")
			|| (type == "document" && tArray[i].getAttribute("facetType") == "document")) {
					
			var imgId;
			if (tArray[i].id.indexOf('_Children') > -1) {
				open(tArray[i]);
				imgId = tArray[i].id.replace("_Children", "_Img");
			} else
				imgId = tArray[i].id.replace("_Main", "_Img");

			openPic(imgId);
		}
	}
}

// Scroll the respective element container to a given element
function scrollTo(ele, container) {
	var success = false;
	console.log("     Triggered a scrollTo in '" + container + "' facet");
	var eles = document.getElementsByName(ele);
	if (eles != null && eles.length > 0) {
		if (container != null) {
			var pos = eles[0].offsetTop;
			console.log("       Element " + eles[0].name + " position: " + pos
					+ " px");
			document.getElementById(container).scrollTop = pos;
			if (pos != 0)
				success = true;
		}
	}
	return success;
}

// Expand the trees (and swap images) of a given set of terms
function expand(terms) {
	var tArray = terms.split(',');
	for (var i = 0; i < tArray.length; i++) {
		var ele = document.getElementById(tArray[i]);
		if (ele !== null) {// !==
			if (ele.style.display === 'none') {// ===
				var eleId = ele.id;
				var imgId = eleId.replace('Children', 'Img');
				toggleSubTree(eleId, imgId);
			}
		}
	}
}

// String.startsWith() function
if (typeof String.prototype.startsWith != 'task') {
	String.prototype.startsWith = function(str) {
		return this.slice(0, str.length) == str;
	};
}