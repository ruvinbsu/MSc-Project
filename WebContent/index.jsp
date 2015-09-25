<%-- 
Created by Ruvin Yusubov
MSc in Advance Computer Science
School of Computer Science
The University of Manchester
 --%>

<%@ page import="org.semanticweb.owlapi.util.VersionInfo"%>
<%@ page import="org.semanticweb.owlapi.model.IRI"%>
<%@ page import="org.semanticweb.owlapi.apibinding.OWLManager"%>
<%@ page import="org.semanticweb.owlapi.model.OWLOntologyManager"%>
<%@ page import="org.semanticweb.owlapi.model.OWLOntology"%>
<%@ page import="org.semanticweb.owlapi.model.OWLOntologyCreationException"%>

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="header.html"%>
<body onmousedown="isKeyPressed(event)">
	<!-- <body onkeyup="myFunction1(event)" onkeydown="myFunction2(event)"> -->
	<!-- It is another method of implementing ctrl stuff -->

	<a href="http://www.manchester.ac.uk/" class="regular" target="_blank">
		<img alt="" src="images/manlogo.png" width="106" height="45">
	</a>

	<img alt="" src="" width="65" />
	<img alt="" src="images/logo_final.png" width="750" height="45" />
	<img alt="" src="" width="65" />

	<a href="http://www.ericsson.com/" class="regular" target="_blank">
		<img alt="" src="images/epsrclogo2.png" width="90" height="45" />
	</a>

	<br />
	<br />

	<div id="progressBackgroundFilter"></div>
	<!-- It is to make Loading GIF -->
	<div id="processMessage" align="center"></div>
	<!-- It is to make Loading GIF -->

	<div id="content">
		<br />

		<div class="search" id="docSearch">Document</div>
		<div class="search" id="docDetails">Term Usage</div>
		<div class="search"></div>

		<br />

		<div class="dimension" id="document">
			<%
				String docTree = (String) request.getAttribute("docTree");
				out.print(docTree);
			%>
		</div>

		<div class="details" id="details">
			&nbsp;&nbsp;To do:
			<ul>
				<li style="list-style-type: square">Search functionality on
					each facet</li>
			</ul>
		</div>
		<br />
		<div class="buttons">
			&nbsp; <a href="javascript:;" class="regular"
				onClick="clearSelection2()">Clear Selection</a>&nbsp;|&nbsp; <a
				href="javascript:;" class="regular"
				onClick="collapseAll('document')">Collapse All</a>&nbsp;|&nbsp; <a
				href="javascript:;" class="regular" onClick="expandAll('document')">Expand
				All</a>
		</div>

		<div class="buttons"></div>
		<div class="buttons"></div>

		<br /> <br /> <br />

		<table align="center" border="0">
			<!--  Think about changing align to another attribute -->
			<tr>
				<td>
					<div class="search" id="tSearch">Task</div>
					<div class="search" id="pSearch">Functional Area</div>
				</td>
				<td>
					<div class="search" id="rSearch">Instances</div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="dimension" id="task">
						<%
							String taskTree = (String) request.getAttribute("taskTree");
							out.print(taskTree);
						%>
					</div>

					<div class="dimension" id="functionalarea">
						<%
							String functionalAreaTree = (String) request.getAttribute("functionalAreaTree");
							out.print(functionalAreaTree);
						%>
					</div>

				</td>
				<td rowspan="4">

					<div class="details2" id="instances">
						<%
							String instanceTree = (String) request.getAttribute("instanceTree");
							out.print(instanceTree);
						%>
					</div>
				</td>
			</tr>

			<tr>
				<td>
					<div class="buttons">
						&nbsp; <a href="javascript:;" class="regular"
							onClick="clearSelection2()">Clear Selection</a>&nbsp;|&nbsp; <a
							href="javascript:;" class="regular" onClick="collapseAll('task')">Collapse
							All</a>&nbsp;|&nbsp; <a href="javascript:;" class="regular"
							onClick="expandAll('task')">Expand All</a>
					</div>

					<div class="buttons">
						&nbsp; <a href="javascript:;" class="regular"
							onClick="clearSelection2()">Clear Selection</a>&nbsp;|&nbsp; <a
							href="javascript:;" class="regular"
							onClick="collapseAll('functionalarea')">Collapse All</a>&nbsp;|&nbsp; <a
							href="javascript:;" class="regular"
							onClick="expandAll('functionalarea')">Expand All</a>
					</div>
				</td>
			</tr>

			<tr><td></td></tr>

			<tr>
				<td>
					<fieldset>
						<legend style="font-weight: bold; font-variant: small-caps; font-size: 15px;">Alarm</legend>
						<div class="dimension" id="alarm">
							<%
								String alarmTree = (String) request.getAttribute("alarmTree");
								out.print(alarmTree);
								//System.out.println(alarm);
							%>
						</div>
						<div style="width:10px;display: inline-block;"></div>
						<div class="inputnumbers" id="input">
							<table>
								<tr>
									<td>Alarm Class</td>
									<td><input type="text" id="alarmclass" /></td>
								</tr>
								<tr>
									<td>Alarm Category</td>
									<td><input type="number" id="alarmcategory" min="0"
										max="15" /></td>
								</tr>
								<tr>
									<td>Printout Category</td>
									<td><input type="number" id="printoutcategory" min="38"
										max="44"/></td>
								</tr>
							</table>
							<input type="submit" value="Submit"
								onclick="buttonPressed=true; focusOn(document.getElementById('alarmclass').value + ',' +
					 			document.getElementById('alarmcategory').value + ',' +
					 			document.getElementById('printoutcategory').value);" />
							<!-- 					 			buttonPressed=false;"/> -->
						</div>
						
						<br />
						
						<div class="buttons" style="float:left;">
							&nbsp; <a href="javascript:;" class="regular"
								onClick="clearSelection2()"> Clear Selection</a>&nbsp;|&nbsp; <a
								href="javascript:;" class="regular"
								onClick="collapseAll('alarm')">Collapse All</a>&nbsp;|&nbsp; <a
								href="javascript:;" class="regular" onClick="expandAll('alarm')">Expand
								All</a>
						</div>
					</fieldset>
				</td>
			</tr>

		</table>

		<br /> <br /> <br />
	</div>

	<!-- End Content -->

</body>

<%@ include file="footer.html"%>