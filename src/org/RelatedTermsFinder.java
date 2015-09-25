/*
Created by Ruvin Yusubov
MSc in Advance Computer Science
School of Computer Science
The University of Manchester
 */

package org;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;













import uk.ac.manchester.cs.jfact.JFactFactory;
//import uk.ac.manchester.cs.n8eo.Query;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;

public class RelatedTermsFinder {

	private final String funcAreaDim = "Functional Area", tDim = "Task", docDim = "Document", alDim = "Alarm";
	private OWLOntology ont;
	private Set<String> facets;
	private OWLOntologyManager man;
	private OWLDataFactory df;
	private OWLReasoner reasoner;
	//private PelletReasoner reasoner;
	private SimpleShortFormProvider fp;
	private SyntaxTransformations syntaxTransformations;
	
	private Set<OWLClass> taskHighestHighlighted;
	private Set<OWLClass> documentHighestHighlighted;
	private Set<OWLClass> alarmHighestHighlighted;
	private Set<OWLClass> functionalAreaHighestHighlighted;

	/**
	 * N8EO Constructor
	 * 
	 * @param ont
	 *            OWL Ontology
	 */
	public RelatedTermsFinder(OWLOntology ont, OWLReasoner reasoner) {
		
		taskHighestHighlighted = new HashSet<OWLClass>();
		documentHighestHighlighted = new HashSet<OWLClass>();
		alarmHighestHighlighted = new HashSet<OWLClass>();
		functionalAreaHighestHighlighted = new HashSet<OWLClass>();

		facets = new HashSet<String>(Arrays.asList(funcAreaDim, tDim, docDim, alDim));
		man = ont.getOWLOntologyManager();
		df = man.getOWLDataFactory();
		syntaxTransformations = new SyntaxTransformations(ont);
		
		this.reasoner = reasoner;
		
		fp = new SimpleShortFormProvider();
		this.ont = ont;

	}

	/**
	 * Given a concept name, return a JavaScript friendly String with the set of
	 * concepts 'related' to it
	 * 
	 * @param concept
	 *            Concept name
	 * @return JavaScript-friendly string with the set of concepts 'related' to
	 *         it
	 */
	public Set<String> getRelatedTerms(/*SimpleServlet n,*/ String concept, Set<String> allHighlightedItems, int curentNumOfClikedItems) {
		Set<String> related = null;

		boolean isAlarmCharacteristics = false;
		//Everything here comes with Alarm Category. Start!
		String[] concepts = concept.split(",", -1);
		if (concepts.length > 1)
			isAlarmCharacteristics = true;
		
		// If concept is from Document
		if (!isAlarmCharacteristics && isSubClassOf(concept, docDim)) {
			related = getRelatedTermsForDocument(concept, allHighlightedItems, curentNumOfClikedItems);
		}
		// If concept from any other facet of elements
		else {
			related = getRelatedItemsForFiller(concept, false, allHighlightedItems, curentNumOfClikedItems);
		}
		
		if (taskHighestHighlighted != null)
			related.addAll(getClassNames(taskHighestHighlighted));
		
		if (functionalAreaHighestHighlighted != null)
			related.addAll(getClassNames(functionalAreaHighestHighlighted));
		
		if (alarmHighestHighlighted != null)
			related.addAll(getClassNames(alarmHighestHighlighted));
		
		if (documentHighestHighlighted != null)
			related.addAll(getClassNames(documentHighestHighlighted));
		
		related.remove("Thing");
		related.remove("Nothing");

		return related;
	}

	public void getHighestHighlighted(Set<String> allHighlightedItems,
			OWLClass owlClass, int curentNumOfClikedItems) {


		String owlClassString = syntaxTransformations
				.getManchesterRendering(owlClass);

		if ((curentNumOfClikedItems > 1 && allHighlightedItems.contains(owlClassString)) || (curentNumOfClikedItems == 1)) {
			
			if (reasoner.isEntailed(df.getOWLSubClassOfAxiom(owlClass, syntaxTransformations.owlifyConcept("Document")))){
				documentHighestHighlighted.add(owlClass);
			}

			
		} else {

			Set<OWLClass> directSubclasses = getDirectSubclasses(owlClass);

			for (OWLClass someClass : directSubclasses) {
				getHighestHighlighted(allHighlightedItems, someClass, curentNumOfClikedItems);
			}
		}
	}
	
	
	/**
	 * Get the set of related terms for a specified concept name
	 * 
	 * @param concept
	 *            Concept name
	 * @return Set of related terms for a specified concept name
	 */
	private Set<String> getRelatedTermsForDocument(String concept, Set<String> allHighlightedItems, int curentNumOfClikedItems) {
		Set<String> related = new HashSet<String>();
		related.addAll(getAllFillers(concept, allHighlightedItems, curentNumOfClikedItems));

		getHighestHighlighted(allHighlightedItems, syntaxTransformations.owlifyConcept(concept), curentNumOfClikedItems);

		return related;
	}
	
	/**
	 * Get all fillers F that hold for the query Concept => role some F
	 * 
	 * @param concept
	 *            Concept name
	 * @param role
	 *            Role name
	 * @return Query result
	 */
	public Set<String> getAllFillers(String concept, Set<String> allHighlightedItems, int curentNumOfClikedItems) {
		Set<OWLClass> results = new HashSet<OWLClass>();
		OWLObjectProperty p1 = syntaxTransformations.owlifyRole("usedFor");
		OWLObjectProperty p2 = syntaxTransformations.owlifyRole("relatesTo");
		OWLObjectProperty p4 = syntaxTransformations.owlifyRole("about");
		
		OWLClass lhs = syntaxTransformations.owlifyConcept(concept);

		//OWLClass documentDimension = syntaxTransformations.owlifyConcept("Task");
		OWLClass taksDimension = syntaxTransformations.owlifyConcept("Task");
		OWLClass alarmDimension = syntaxTransformations.owlifyConcept("Alarm");
		OWLClass functionalAreaDimension = syntaxTransformations.owlifyConcept("Functional Area");

		
		//getHighestHighlighted(allHighlightedItems, documentDimension, p1, lhs, curentNumOfClikedItems);
		getHighestHighlighted(allHighlightedItems, taksDimension, p1, lhs, curentNumOfClikedItems);
		getHighestHighlighted(allHighlightedItems, functionalAreaDimension, p2, lhs, curentNumOfClikedItems);
		getHighestHighlighted(allHighlightedItems, alarmDimension, p4, lhs, curentNumOfClikedItems);
		
		
		return getClassNames(results);
	}
	
	public void getHighestHighlighted(Set<String> allHighlightedItems, OWLClass owlClass, OWLObjectProperty p, 
									OWLClassExpression lhs, int curentNumOfClikedItems){
		
		OWLClassExpression rhs = df.getOWLObjectSomeValuesFrom(p, owlClass);
		
		OWLAxiom ax = df.getOWLSubClassOfAxiom(rhs, lhs);
		
		String owlClassString = syntaxTransformations.getManchesterRendering(owlClass);
		
		if ((curentNumOfClikedItems > 1 && allHighlightedItems.contains(owlClassString) && reasoner.isEntailed(ax)) || 
				(curentNumOfClikedItems == 1 && (reasoner.isEntailed(ax)))){
			if (reasoner.isEntailed(df.getOWLSubClassOfAxiom(owlClass, syntaxTransformations.owlifyConcept("Task"))))
				taskHighestHighlighted.add(owlClass);
		
			if (reasoner.isEntailed(df.getOWLSubClassOfAxiom(owlClass, syntaxTransformations.owlifyConcept("Functional Area"))))
				functionalAreaHighestHighlighted.add(owlClass);
					
			if (reasoner.isEntailed(df.getOWLSubClassOfAxiom(owlClass, syntaxTransformations.owlifyConcept("Alarm"))))
				alarmHighestHighlighted.add(owlClass);

		}
		else{
			
			Set<OWLClass> directSubclasses = getDirectSubclasses(owlClass);
			
			for (OWLClass someClass: directSubclasses){
				getHighestHighlighted(allHighlightedItems, someClass, p, lhs, curentNumOfClikedItems);
			}

		}
		
	}
	
	
	/////////////////////
		
	/**
	 * Get direct OWL subclasses of a given concept, and whether each of those
	 * has subclasses
	 * 
	 * @param c
	 *            OWL class
	 * @return A map of direct OWL subclasses and whether each of these has
	 *         subclasses
	 * Code was written by Rafael Goncalves
	 */
	public Set<OWLClass> getDirectSubclasses(OWLClass c) {
		Set<OWLClass> set = new HashSet<OWLClass>();
		for (OWLClass sub : reasoner.getSubClasses(c, true).getFlattened()) {
			if (!sub.isBottomEntity() && !sub.isTopEntity() && !sub.equals(c)) {
				Set<OWLClass> subcs = reasoner.getSubClasses(sub, true)
						.getFlattened();
				subcs.remove(df.getOWLNothing());
				set.add(sub);
			}
		}
		return set;
	}

	
	
	////////////////////
	
	

	/**
	 * Given a set of OWLClass objects, get the set of corresponding class names
	 * 
	 * @param classes
	 *            Set of OWLClass objects
	 * @return Set of class names
	 * Code was written by Rafael Goncalves
	 */
	public Set<String> getClassNames(Set<OWLClass> classes) {
		Set<String> result = new HashSet<String>();
		for (OWLClass c : classes) {
			result.add(syntaxTransformations.getManchesterRendering(c));
		}
		return result;
	}

	/**
	 * Given a concept name, get the set of all its subclasses (including
	 * indirect)
	 * 
	 * @param concept
	 *            Concept name
	 * @return Set of concept names
	 * Code was written by Rafael Goncalves
	 */
	public Set<String> getAllSubclassNames(String concept) {
		return syntaxTransformations.getManchesterRendering(getAllSubclasses(concept));
	}

	/**
	 * Given a concept name, get the set of all its subclasses (including
	 * indirect)
	 * 
	 * @param concept
	 *            Concept name
	 * @return Set of OWL subclasses
	 * Code was written by Rafael Goncalves
	 */
	public Set<OWLClass> getAllSubclasses(String concept) {
		return getAllSubclasses(syntaxTransformations.owlifyConcept(concept));
	}

	
	
	/**
	 * Given a concept name, get the set of all its subclasses (including
	 * indirect)
	 * 
	 * @param concept
	 *            OWL class
	 * @return Set of OWL subclasses
	 * Code was written by Rafael Goncalves
	 */
	public Set<OWLClass> getAllSubclasses(OWLClass concept) {
		return reasoner.getSubClasses(concept, false).getFlattened();
	}
	
	/**
	 * Get the set of parent terms that need expanding based on a given set of
	 * terms
	 * 
	 * @param terms
	 *            Set of terms
	 * @return Set of parents of the given terms that need expanding
	 * Code was written by Rafael Goncalves
	 */
	public Set<String> getParentsToExpand(Set<String> terms) { 
		
		Set<String> parents = new HashSet<String>();
		
		for (String s : terms) {
			for (String supc : getAllSuperclassNames(s)) {
				if (!facets.contains(supc) && !supc.equals("Thing")) {
						parents.add(supc + "_Children"); 
				}
			}
		}
		return parents;
	}
	
	
	/**
	 * Get the set of parent terms that need expanding based on a given set of
	 * terms
	 * 
	 * @param terms
	 *            Set of terms
	 * @return Set of parents of the given terms that need expanding
	 * Code was written by Rafael Goncalves
	 */
	public Set<String> getParentsToExpand2(Set<String> terms) { // After PR, before was just 1st
												// parameter
		Set<String> parents = new HashSet<String>();
		
		for (String s : terms) 
				parents.add(s + "_Children"); 
		
		return parents;
	}
	

	/**
	 * Given a concept name, get the set of all its superclass names (including
	 * indirect ones)
	 * 
	 * @param concept
	 *            Concept name
	 * @return Set of concept names
	 * Code was written by Rafael Goncalves
	 */
	public Set<String> getAllSuperclassNames(String concept) {
		Set<OWLClass> superclasses = reasoner.getSuperClasses(
				syntaxTransformations.owlifyConcept(concept), false).getFlattened();
		return syntaxTransformations.getManchesterRendering(superclasses);
	}

	/**
	 * Given a concept name, get the set of all its direct superclass names
	 * 
	 * @param concept
	 *            Concept name
	 * @return Set of concept names
	 * Code was written by Rafael Goncalves
	 */
	public Set<String> getAllDirectSuperclassNames(String concept) {
		Set<OWLClass> superclasses = reasoner.getSuperClasses(
				syntaxTransformations.owlifyConcept(concept), false).getFlattened();
		return syntaxTransformations.getManchesterRendering(superclasses);
		
	}

	
	/**
	 * Check whether a concept name is a subclass of another
	 * 
	 * @param lhs
	 *            Subsumee-concept name
	 * @param rhs
	 *            Subsumer-concept name
	 * @return true if there exists a subclass relation, false otherwise
	 * Code was written by Rafael Goncalves
	 */
	public boolean isSubClassOf(String lhs, String rhs) {
		lhs = lhs.replace(" ", "_");
		return reasoner.isEntailed(df.getOWLSubClassOfAxiom(syntaxTransformations.owlifyConcept(lhs),
				syntaxTransformations.owlifyConcept(rhs)));
	}

	// ////////////////////find related documents for a clicked concepts
	//NEED TO ADD ALL COMBINATIONS OF ALARM APART FROM: TASK, PRODUCT, READER!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	public Set<String> getRelatedItemsForFiller(String concept, boolean bol, Set<String> allHighlightedItems, int curentNumOfClikedItems) {//false means even indirect one for DL query of getting subclasses, true - opposite
		Set<OWLClass> results1 = new HashSet<OWLClass>();
		Set<String> result = new HashSet<String>();

		ShortFormProvider shortFormProvider = new SimpleShortFormProvider();
		DLQueryHandler dlQueryHandler = new DLQueryHandler(reasoner);
		DLQueryHandler.DLQueryPrinter dlQueryPrinter = dlQueryHandler.new DLQueryPrinter(dlQueryHandler.new DLQueryEngine(
				shortFormProvider), shortFormProvider);

		boolean isAlarmCharacteristics = false;
		//Everything here comes with Alarm Category. Start!
		String[] concepts = concept.split(",", -1);
		if (concepts.length > 1)
			isAlarmCharacteristics = true;
				
		OWLObjectProperty p3 = syntaxTransformations.owlifyRole("aboutAlarmWithPrintoutCategory");
		OWLObjectProperty p5 = syntaxTransformations.owlifyRole("aboutAlarmWithAlarmCategory");
		OWLObjectProperty p6 = syntaxTransformations.owlifyRole("aboutAlarmWithAlarmClass");
		
		OWLObjectProperty p4 = syntaxTransformations.owlifyRole("about");
		
		Set<OWLClass> alarmClasses = getAllSubclasses("Alarm");
		
		if (isAlarmCharacteristics){
			Set<String> relatedToAlarmClass = new HashSet<String>();
			Set<String> relatedToAlarmCategory = new HashSet<String>();
			Set<String> relatedToPrintoutCategory = new HashSet<String>();
			
			if (concepts[0].length() > 0){

				OWLClass lhs = syntaxTransformations.owlifyConcept(concepts[0]);
								
						for (OWLClass c : alarmClasses) {
							OWLAxiom ax = df.getOWLSubClassOfAxiom(df.getOWLObjectSomeValuesFrom(p4, c), df.getOWLObjectSomeValuesFrom(p6, lhs));
							
							String owlClassString = syntaxTransformations.getManchesterRendering(c);
							
							if ((curentNumOfClikedItems > 1 && allHighlightedItems.contains(owlClassString) || curentNumOfClikedItems == 1) && reasoner.isEntailed(ax))
								relatedToAlarmClass.add(syntaxTransformations.getManchesterRendering(c));
						}
			}

			if (concepts[1].length() > 0){
				
				OWLClass lhs = syntaxTransformations.owlifyConcept(concepts[1]);
					
						for (OWLClass c : alarmClasses) {
							OWLAxiom ax = df.getOWLSubClassOfAxiom(df.getOWLObjectSomeValuesFrom(p4, c), df.getOWLObjectSomeValuesFrom(p5, lhs));
							
							String owlClassString = syntaxTransformations.getManchesterRendering(c);
							
							
							if ((curentNumOfClikedItems > 1 && allHighlightedItems.contains(owlClassString) || curentNumOfClikedItems == 1) && reasoner.isEntailed(ax))
								relatedToAlarmCategory.add(syntaxTransformations.getManchesterRendering(c));
						}
					
				if (concepts[0].length() > 0){
					relatedToAlarmClass.retainAll(relatedToAlarmCategory);
					relatedToAlarmCategory.clear();
				}
			}

			if (concepts[2].length() > 0){
			
				OWLClass lhs = syntaxTransformations.owlifyConcept(concepts[2]);
					
						for (OWLClass c : alarmClasses) {
							OWLAxiom ax = df.getOWLSubClassOfAxiom(df.getOWLObjectSomeValuesFrom(p4, c), df.getOWLObjectSomeValuesFrom(p3, lhs));

							String owlClassString = syntaxTransformations.getManchesterRendering(c);
							
							
							if ((curentNumOfClikedItems > 1 && allHighlightedItems.contains(owlClassString) || curentNumOfClikedItems == 1) && reasoner.isEntailed(ax)){
								relatedToPrintoutCategory.add(syntaxTransformations.getManchesterRendering(c));
							}
						}
				
				if (concepts[0].length() > 0){
					relatedToAlarmClass.retainAll(relatedToPrintoutCategory);
					relatedToPrintoutCategory.clear();
				}
				else if (concepts[1].length() > 0){
					relatedToAlarmCategory.retainAll(relatedToPrintoutCategory);
					relatedToPrintoutCategory.clear();
				}
					
			}

			result.addAll(relatedToAlarmClass);
			result.addAll(relatedToAlarmCategory);
			result.addAll(relatedToPrintoutCategory);
			
		}
		
		//long tEnd5 = System.currentTimeMillis();
		//long tDelta = tEnd5 - tStart5;
		//double elapsedSeconds = tDelta / 1000.0;
		//System.out.println("Get Related Terms time for alarm: " + elapsedSeconds);	
		
		if (!isAlarmCharacteristics){
			String st = null;

			st = syntaxTransformations.getManchesterRendering(syntaxTransformations.owlifyConcept(concept));
			
			st = st.replaceAll(" ", "_");
		
			String classExpression = "";
		
			
			OWLObjectProperty p1 = syntaxTransformations.owlifyRole("usedFor");
			OWLObjectProperty p2 = syntaxTransformations.owlifyRole("relatesTo");
			
			OWLClass lhs = syntaxTransformations.owlifyConcept(concept);
			
			
			OWLClass taskDimension = syntaxTransformations.owlifyConcept("Task");
			OWLClass alarmDimension = syntaxTransformations.owlifyConcept("Alarm");
			OWLClass functionalAreaDimension = syntaxTransformations.owlifyConcept("Functional Area");

			
			if (isSubClassOf(st, "Task")) {
				
				
				classExpression = "usedFor" + " some " + st;
				
				getHighestHighlighted(allHighlightedItems, taskDimension, p1, df.getOWLObjectSomeValuesFrom(p1, lhs), curentNumOfClikedItems);
				getHighestHighlighted(allHighlightedItems, functionalAreaDimension, p2, df.getOWLObjectSomeValuesFrom(p1, lhs), curentNumOfClikedItems);
				getHighestHighlighted(allHighlightedItems, alarmDimension, p4, df.getOWLObjectSomeValuesFrom(p1, lhs), curentNumOfClikedItems);

				//System.out.println("Recursion stuff for fillers: " + elapsedSeconds);

			}
			
			if (isSubClassOf(st, "Functional Area")) {
				classExpression = "relatesTo" + " some " + st;
				
				getHighestHighlighted(allHighlightedItems, taskDimension, p1, df.getOWLObjectSomeValuesFrom(p2, lhs), curentNumOfClikedItems);
				getHighestHighlighted(allHighlightedItems, functionalAreaDimension, p2, df.getOWLObjectSomeValuesFrom(p2, lhs), curentNumOfClikedItems);
				getHighestHighlighted(allHighlightedItems, alarmDimension, p4, df.getOWLObjectSomeValuesFrom(p2, lhs), curentNumOfClikedItems);

				
			}
			
			if (isSubClassOf(st, "Alarm")) {
				classExpression = "about" + " some " + st;
				
				getHighestHighlighted(allHighlightedItems, taskDimension, p1, df.getOWLObjectSomeValuesFrom(p4, lhs), curentNumOfClikedItems);
				getHighestHighlighted(allHighlightedItems, functionalAreaDimension, p2, df.getOWLObjectSomeValuesFrom(p4, lhs), curentNumOfClikedItems);
				getHighestHighlighted(allHighlightedItems, alarmDimension, p4, df.getOWLObjectSomeValuesFrom(p4, lhs), curentNumOfClikedItems);
				

			}
			
			
			long tStart2 = System.currentTimeMillis();

			results1 = dlQueryPrinter.askQueryForDocumetConcept(classExpression.trim(), bol);
		
			long tEnd2 = System.currentTimeMillis();
			long tDelta = tEnd2 - tStart2;
			double elapsedSeconds = tDelta / 1000.0;
			System.out.println("DL query from fillers for documents: " + elapsedSeconds);

		// /Should be separate function or this will be done 3 or more times
		// (for each facet)
						
			if (results1!=null)
				for (OWLClass ind : results1) {
					st = syntaxTransformations.getManchesterRendering(ind);
					result.add(st);
				}
			
		
		}
//		long tStart4 = System.currentTimeMillis();
//		long tEnd4 = System.currentTimeMillis();
//		tDelta = tEnd4 - tStart4;
//		elapsedSeconds = tDelta / 1000.0;
//		System.out.println("DL query from fillers for documents:: " + elapsedSeconds);

		
		return result;
	}

	// ////////////////////find related instances for a clicked concepts

	public Set<String> addRelatedInstances(String concept) {
		Set<OWLNamedIndividual> results1 = new HashSet<OWLNamedIndividual>();
		Set<String> result = new HashSet<String>();

		
		ShortFormProvider shortFormProvider = new SimpleShortFormProvider();
		DLQueryHandler dlQueryHandler = new DLQueryHandler(reasoner);
		DLQueryHandler.DLQueryPrinter dlQueryPrinter = dlQueryHandler.new DLQueryPrinter(dlQueryHandler.new DLQueryEngine(
				shortFormProvider), shortFormProvider);

		String classExpression = "";
		
		boolean isAlarmCharacteristics = false;
		//Everything here comes with Alarm Category. Start!
		String[] concepts = concept.split(",", -1);
		if (concepts.length > 1)
			isAlarmCharacteristics = true;
		
		String st = "";
		
		if (isAlarmCharacteristics){
			
			String stAlarmClass = "";
			String stAlarmCategory = "";
			String stPrintoutCategory = "";
			
			String classExpressionForAlarmClass = "";
			String classExpressionForAlarmCategory = "";
			String classExpressionForPrintoutCategory = "";
			
			if (concepts[0].length() > 0)
				stAlarmClass = syntaxTransformations.getManchesterRendering(syntaxTransformations.owlifyConcept(concepts[0]));//the same for concepts[1], concepts[2]  
			
			if (concepts[1].length() > 0)
				stAlarmCategory = syntaxTransformations.getManchesterRendering(syntaxTransformations.owlifyConcept(concepts[1]));//the same for concepts[1], concepts[2]  
		
			if (concepts[2].length() > 0)
				stPrintoutCategory = syntaxTransformations.getManchesterRendering(syntaxTransformations.owlifyConcept(concepts[2]));//the same for concepts[1], concepts[2]  
				
			stAlarmClass = stAlarmClass.replaceAll(" ", "_");
			stAlarmCategory = stAlarmCategory.replaceAll(" ", "_");
			stPrintoutCategory = stPrintoutCategory.replaceAll(" ", "_");
			
			Set<OWLNamedIndividual> resultsAlarmClass = new HashSet<OWLNamedIndividual>();
			Set<OWLNamedIndividual> resultsAlarmCategory = new HashSet<OWLNamedIndividual>();
			Set<OWLNamedIndividual> resultsPrintoutCategory = new HashSet<OWLNamedIndividual>();
			
			Set<String> resultsAlarmClassStr = new HashSet<String>();
			Set<String> resultsAlarmCategoryStr = new HashSet<String>();
			Set<String> resultsPrintoutCategoryStr = new HashSet<String>();
			Set<String> resultsInstersectionStr = new HashSet<String>();
			
			classExpressionForAlarmClass = "aboutAlarmWithAlarmClass" + " some " + stAlarmClass;
			classExpressionForAlarmCategory = "aboutAlarmWithAlarmCategory" + " some " + stAlarmCategory;
			classExpressionForPrintoutCategory = "aboutAlarmWithPrintoutCategory" + " some " + stPrintoutCategory;
			
			
			if (concepts[0].length() > 0)
				resultsAlarmClass = dlQueryPrinter.askQuery(classExpressionForAlarmClass.trim());
			
			if (resultsAlarmClass != null){
				for (OWLNamedIndividual ind : resultsAlarmClass) {
					// redefinition of st in order not to create again new String
					String tempStr = syntaxTransformations.getManchesterRendering(ind);
					resultsAlarmClassStr.add(tempStr);
				}
			}
			
			if (concepts[1].length() > 0)
				resultsAlarmCategory = dlQueryPrinter.askQuery(classExpressionForAlarmCategory.trim());

			if (resultsAlarmCategory != null){
				for (OWLNamedIndividual ind : resultsAlarmCategory) {
					// redefinition of st in order not to create again new String
					String tempStr = syntaxTransformations.getManchesterRendering(ind);
					resultsAlarmCategoryStr.add(tempStr);
				}
			}

			if (concepts[2].length() > 0)
				resultsPrintoutCategory = dlQueryPrinter.askQuery(classExpressionForPrintoutCategory.trim());
			
			if (resultsPrintoutCategory != null){
				for (OWLNamedIndividual ind : resultsPrintoutCategory) {
					// redefinition of st in order not to create again new String
					String tempStr = syntaxTransformations.getManchesterRendering(ind);
					resultsPrintoutCategoryStr.add(tempStr);
				}
			}
			
			if (concepts[1].length() > 0){
				if (concepts[0].length() > 0){
					resultsAlarmClassStr.retainAll(resultsAlarmCategoryStr);
					resultsAlarmCategoryStr.clear();
				}
			}
				
			if (concepts[2].length() > 0){
				if (concepts[0].length() > 0){
					resultsAlarmClassStr.retainAll(resultsPrintoutCategoryStr);
					resultsPrintoutCategoryStr.clear();
				}
				else if (concepts[1].length() > 0){
					resultsAlarmCategoryStr.retainAll(resultsPrintoutCategoryStr);
					resultsPrintoutCategoryStr.clear();
				}
			}
			
			resultsInstersectionStr.addAll(resultsAlarmClassStr);
			resultsInstersectionStr.addAll(resultsAlarmCategoryStr);
			resultsInstersectionStr.addAll(resultsPrintoutCategoryStr);
			
			for (String str: resultsInstersectionStr){
				OWLNamedIndividual ind = syntaxTransformations.owlifyIndividual(str);
				results1.add(ind);
			}
		}
		else{
		//Everything here comes with Alarm Category. Finish!
			st = syntaxTransformations.getManchesterRendering(syntaxTransformations.owlifyConcept(concept));
		
			st = st.replaceAll(" ", "_");
		
			if (isSubClassOf(st, "Document")) {
				classExpression = st;
			}
			if (isSubClassOf(st, "Task")) {
				classExpression = "usedFor" + " some " + st;
			}
		
			if (isSubClassOf(st, "Functional Area")) {
				classExpression = "relatesTo" + " some " + st;
			}

			if (isSubClassOf(st, "Alarm")) {
				classExpression = "about" + " some " + st;
			}

			long tSDLqueryForInstances = System.currentTimeMillis();
			
			results1 = dlQueryPrinter.askQuery(classExpression.trim());
			
			long tEDLqueryForInstances = System.currentTimeMillis();
			long tDelta = tEDLqueryForInstances - tSDLqueryForInstances;
			double elapsedSeconds = tDelta / 1000.0;
			System.out.println("Get Related Instances time: " + elapsedSeconds);
		}
		
		if (results1 != null){
			for (OWLNamedIndividual ind : results1) {
				// redefinition of st in order not to create again new String
				st = syntaxTransformations.getManchesterRendering(ind);
				//st = st.replaceAll(" ", "_"); LOOK HERE or THE LAST THING WHICH YOU COMMENTED!

				OWLAnnotationProperty label = df
						.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT
								.getIRI());
				OWLLiteral val = null;

				for (OWLAnnotation annotation : ind.getAnnotations(ont, label))
					if (annotation.getValue() instanceof OWLLiteral)
						val = (OWLLiteral) annotation.getValue();

				if (val != null)
					st += "@" + val.getLiteral().toString();
				else
					// temporary (until will not get all the 50 documents)
					st += "@" + "Technical_Documents/IST FS 13A.pdf";

				result.add(st);
			}
		}
		return result;
	}
}
