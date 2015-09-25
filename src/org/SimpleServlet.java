/*
 * Created by Ruvin Yusubov
 * MSc in Advance Computer Science
 * School of Computer Science
 * The University of Manchester
 */

package org;

import java.awt.print.Printable;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.HermiT.Reasoner;


/**
 * Servlet implementation class SimpleServlet
 */
@WebServlet(description = "A simple servlet", urlPatterns = { "/SimpleServlet" })
public class SimpleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		long tDelta = 0;
		double elapsedSeconds = 0;
		
	
		long tSOverallTime = System.currentTimeMillis();
		
		SyntaxTransformations syntaxTransformations = null;
		RelatedTermsFinder n = null;
		OWLReasoner reasoner = null;
		
		if (request.getSession().getAttribute("reasoner") instanceof OWLReasoner)
			reasoner = (OWLReasoner) request.getSession().getAttribute("reasoner");
		
		OWLOntology ont = reasoner.getRootOntology();		
		
		syntaxTransformations = new SyntaxTransformations(ont);
		n = new RelatedTermsFinder(ont, reasoner);
		
		//////////2. Obtaining data from Client (Ajax).
		String allHighlightedItemsRaw = request.getParameter("alreadyHighlighted");
		String[] allHighlightedItems = allHighlightedItemsRaw.split(",", -1);
		
		String alarmClass = request.getParameter("alarmClass");
		String alarmCategory = request.getParameter("alarmCategory");
		String printoutCategory = request.getParameter("printoutCategory");
		
		//String ctrlPressed = request.getParameter("isCtrlPressed");
		String alarmButtonPressed = request.getParameter("isAlarmButtonPressed");
		Integer curentNumOfClikedItems = Integer.parseInt(request.getParameter("curentNumOfClikedItems"));
		
		String lastClickedConcept;
		
		int allHighlightedItemsLength = allHighlightedItems.length - 1;//??????????????????????????????????????????????????????
		
		if (alarmButtonPressed.equals("T")){
			//System.out.println(allHighlightedItems[allHighlightedItems.length-3]);// + " " + allHighlightedItems[allHighlightedItems.length-2] + " " 
		//+ allHighlightedItems[allHighlightedItems.length-1]);
			lastClickedConcept = allHighlightedItems[allHighlightedItems.length-3] + ","
										+ allHighlightedItems[allHighlightedItems.length-2] + ","
										+ allHighlightedItems[allHighlightedItems.length-1];
			allHighlightedItemsLength = allHighlightedItems.length-3;
		}
		else
			lastClickedConcept = allHighlightedItems[allHighlightedItems.length-1];
			
		//////////2.
		
		System.out.println(lastClickedConcept);
		
		//////////3. Creation of objects that will store at the end all the elements that need to be highlighted.
		Set<String> highlightedDocument = new HashSet<String>();
		Set<String> highlightedTask = new HashSet<String>();
		Set<String> highlightedAlarm = new HashSet<String>();
		Set<String> highlightedFunctionalArea = new HashSet<String>();
		//////////3.
		
		//System.out.println(lastClickedConcept);
		Set<String> allHighlightedItemsSet = new HashSet<String>();
		
		for (int i = 0; i < allHighlightedItemsLength; i++){
			allHighlightedItemsSet.add(allHighlightedItems[i]);
		}
		
		//System.out.println("here " + allHighlightedItemsSet);
		
		long tSGetRelatedItems = System.currentTimeMillis();
		
		Set<String> rTerms = n.getRelatedTerms(/*this,*/lastClickedConcept, allHighlightedItemsSet, curentNumOfClikedItems);
		
		long tEGetRelatedItems = System.currentTimeMillis();
		tDelta = tEGetRelatedItems - tSGetRelatedItems;
		elapsedSeconds = tDelta / 1000.0;
		System.out.println("Time to get all related terms: " + elapsedSeconds);
		
		//if (alarmButtonPressed.equals("F"))//????????????????????????????????????????????????????????????????????????????????
			//rTerms.add(lastClickedConcept);
		for (String i: rTerms){

			if (n.isSubClassOf(i, "Document")){
				highlightedDocument.add(i);
			}

			if (n.isSubClassOf(i, "Task"))
				highlightedTask.add(i);
					
			if (n.isSubClassOf(i, "Alarm"))
				highlightedAlarm.add(i);
				
			if (n.isSubClassOf(i, "Functional Area"))
				highlightedFunctionalArea.add(i);

		}
				
		// ////////*. Resolve Expansion for all elements.
		Set<String> toExpand = new HashSet<String>();
		Set<String> toExpand2 = new HashSet<String>();
		//Set<String> toExpand3 = new HashSet<String>();

		
		if (alarmButtonPressed.equals("F")){
			for (String i : highlightedDocument){
				toExpand.add(i);
				rTerms.addAll(n.getAllSubclassNames(i));
			}
		}
		
		
		if (alarmButtonPressed.equals("F")){
			for (String i : highlightedTask){
				toExpand.add(i);
				rTerms.addAll(n.getAllSubclassNames(i));
			}
		}

		if (alarmButtonPressed.equals("F")){
			for (String i : highlightedFunctionalArea){
				toExpand.add(i);
				rTerms.addAll(n.getAllSubclassNames(i));
			}
		}

		if (alarmButtonPressed.equals("F")){
			for (String i : highlightedAlarm){
				toExpand.add(i);
				rTerms.addAll(n.getAllSubclassNames(i));
			}
		}

		if (alarmButtonPressed.equals("F")){
			toExpand2.add(lastClickedConcept);
			
			if (n.isSubClassOf(lastClickedConcept, "Document")) {
				if (highlightedDocument.size() == 0)
					toExpand2.remove(lastClickedConcept);
			}

			if (n.isSubClassOf(lastClickedConcept, "Task")) {
				if (highlightedTask.size() == 0)
					toExpand2.remove(lastClickedConcept);
			}

			if (n.isSubClassOf(lastClickedConcept, "Functional Area")) {
				if (highlightedFunctionalArea.size() == 0)
					toExpand2.remove(lastClickedConcept);
			}

			if (n.isSubClassOf(lastClickedConcept, "Alarm")) {
				if (highlightedAlarm.size() == 0)
					toExpand2.remove(lastClickedConcept);
			}
		}
		

		Set<String> allParents = n.getParentsToExpand(toExpand); //if need to collapse in js, then here instead of toExpandTerms need to use rTerms
		Set<String> allParents2 = n.getParentsToExpand2(toExpand2); 
		
		allParents.addAll(allParents2);
		//////////*. 
		
		Set<String> rInst = n.addRelatedInstances(lastClickedConcept);
				
		
		String relatedConcepts = syntaxTransformations.stringify(rTerms, ",");
		String relatedInstances = syntaxTransformations.stringify(rInst, ",");
		String parents = syntaxTransformations.stringify(allParents, ",");
		
		//Convert the given results into JSON format (because easy to parse) and sent to the js (Ajax).
		String result = "{" + "\"relatedInstances\": " + "\"" + relatedInstances
				+ "\"," + "\"relatedConcepts\": " + "\"" + relatedConcepts
				+ "\"," + "\"alarmClass\": " + "\"" + alarmClass
				+ "\"," + "\"alarmCategory\": " + "\"" + alarmCategory
				+ "\"," + "\"printoutCategory\": " + "\"" + printoutCategory
				+ "\"," + "\"parents\": " + "\"" + parents + "\"" + "}";
		
		response.getWriter().write(result);
		long tEOverallTime = System.currentTimeMillis();
		tDelta = tEOverallTime - tSOverallTime;
		elapsedSeconds = tDelta / 1000.0;
		System.out.println("Overall time: " + elapsedSeconds);
		System.out.println();

	}
}
