/*******************************************************************************
 * This file is part of N8EO Browser.
 * 
 * N8EO Browser is licensed under a Creative Commons Attribution 3.0 Unported License.
 * 
 * Copyright 2013, The University of Manchester
 * 
 * To view a copy of the license, visit http://creativecommons.org/licenses/by/3.0/deed.en_US
 ******************************************************************************/

/**
 * The original code was extended and modified by Ruvin Yusubov
 * MSc in Advance Computer Science
 * School of Computer Science
 * The University of Manchester
 **/

package org;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;







import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;
import uk.ac.manchester.cs.jfact.JFactFactory;
//import uk.ac.manchester.cs.n8eo.Classifier;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;

public class PrintingHandler {

	private OWLOntologyManager man;
	private OWLDataFactory df;
	private OWLClass bottom;
	private OWLReasoner reasoner;
	private final String funcAreaDim = "Functional Area", tDim = "Task", docDim = "Document", alDim = "Alarm";
	private OWLOntology ont;
	
	public OWLOntology getOnt() {
		return ont;
	}

	public void setOnt(OWLOntology ont) {
		this.ont = ont;
	}

	//transient private OWLOntology ont;
	private Set<String> facets;
	private SyntaxTransformations syntaxTransformations;
	
	/**
	 * N8EO Constructor
	 * @param ont	OWL Ontology
	 */
	public PrintingHandler(OWLOntology ont) {
		this.ont = ont;
		syntaxTransformations = new SyntaxTransformations(ont);
		facets = new HashSet<String>(Arrays.asList(funcAreaDim, tDim, docDim, alDim)); 
		man = ont.getOWLOntologyManager();
		df = man.getOWLDataFactory();
		bottom = df.getOWLNothing();
		
        reasoner = new JFactFactory().createReasoner(ont);
//		OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
//        OWLReasonerConfiguration config = new SimpleConfiguration();
//        reasoner = reasonerFactory.createReasoner(ont);
		
//		OWLReasonerFactory reasonerFactory = new FaCTPlusPlusReasonerFactory();
//        OWLReasonerConfiguration config = new SimpleConfiguration();
//        reasoner = reasonerFactory.createReasoner(ont);
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
        reasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_ASSERTIONS);
        
        setReasoner(reasoner);
    }
	
	public OWLReasoner getReasoner() {
		return reasoner;
	}

	public void setReasoner(OWLReasoner reasoner) {
		this.reasoner = reasoner;
	}

	/**
	 * Given a concept name, get its subtree as a string
	 * @param concept	Root concept name
	 * @return Subtree of a given concept
	 * Idea was proposed by Rafael Goncalves and code was modified by Ruvin Yusubov
	 */
	public String printHierarchy(String concept, String type) {

		Map<String, Boolean> map = getDirectSubclasses(concept);//if concept has children then will be <someConcept, true>
		String output = "";

		if(!map.isEmpty()) {
			if(facets.contains(concept))
				output += "<ul id='" + concept + "_Main" + "' facetType='" + type + "'>\n"; 
			else	
				output += "<ul id='" + concept + "_Children" + "' style='display:none' facetType='" + type + "'>\n";
			for(String s : map.keySet()) {
				output += recurseOnConcept(s, map.get(s), type);
			}
			output += "</ul>\n";
		}
		return output;
	}
	
	/**
	 * Given a concept name and whether it has subclasses, produce the appropriate list items
	 * @param concept	Concept name
	 * @param hasChildren	Boolean value: true if concept has any children
	 * @return The appropriate list items and sub-hierarchies, where appropriate
	 * Idea was proposed by Rafael Goncalves and code was modified by Ruvin Yusubov
	 */
	public String recurseOnConcept(String concept, boolean hasChildren, String type) {
				
		Set<String> allUsage = syntaxTransformations.getConceptUsage(concept);
		String usage = syntaxTransformations.stringify(allUsage, ";");
		
		String output = "";

		if(hasChildren) {
			output += "<li>\n<a href='javascript:;' onClick=\"toggleSubTree('" + concept + "_Children" +  "','" + concept + "_Img" + "')\">"
				+ "\n<img src='images/button-closed.png' id='" + concept + "_Img" + "'/></a> \n<a href='javascript:;' id='" + concept + "' name='" + concept + 
				"' onClick=\"focusOn('" + concept + "'); showUsg('" + usage + "')\" class='concept'>" + concept + "</a>\n</li>\n";
			output += printHierarchy(concept, type);
		} 
		else
			output += "<li>\n<img src='images/button-closed.png' style='visibility:hidden'/> \n<a href='javascript:;' id='" + concept + "' name='" + concept + "' " +
				"onClick=\"focusOn('" + concept + "'); showUsg('" + usage + "')\" class='concept'>" + concept + "</a>\n</li>\n";
		
		return output;
	}
	
	/**
	 * Print the equipment and location instances
	 * @return HTML-formatted string 
	 * Idea was proposed by Rafael Goncalves and code was modified by Ruvin Yusubov
	 */
	public String printInstancesHierarchy() {
		String output = "";
		output += "<ul id='documentInstances'><u>Document</u>";
		output += getInstances(docDim);
		output += "</ul>";
		return output;
	}
	
	/**
	 * Get instances of a given concept name
	 * @param concept	Concept name
	 * @return HTML-formatted string
	 */
	private String getInstances(String concept) {
		String out = "";
		
		for(String s : getInstancesOf(concept)) {
			OWLAnnotationProperty label = df.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());
			OWLLiteral val = null;
			
			OWLNamedIndividual ind = syntaxTransformations.owlifyIndividual(s);
			
			////System.out.println(ind);
			
			String st = null;
	        
			for (OWLAnnotation annotation : ind.getAnnotations(ont, label)) 
	           	if (annotation.getValue() instanceof OWLLiteral)
	           		val = (OWLLiteral) annotation.getValue();
			
	        if (val!=null)
	        	st= val.getLiteral().toString();
	        else
	        	st= "Technical_Documents/IST FS 13A.pdf";//temporary (until will not get all the 50 documents)
			
			out += "<li>\n<img src='images/button-closed.png' style='visibility:hidden'/> \n<a href='" + st + "' id='"
					+ s
					+ "' name='"
					+ s
					+ "' "
					+ " class='instance' target='_blank'>" + s + "</a>\n</li>\n";

		}
		return out;
	}
	
	/**
	 * Get instances of a given concept name
	 * 
	 * @param concept
	 *            Concept name
	 * @return Set of instance names
	 */
	public Set<String> getInstancesOf(String concept) {
		return syntaxTransformations.getManchesterRendering(reasoner.getInstances(
				syntaxTransformations.owlifyConcept(concept), false).getFlattened());
	}

	/**
	 * Get direct subclass names of a given class, and whether each of those has
	 * subclasses
	 * 
	 * @param concept
	 *            Concept name
	 * @return A map of direct subclass names and whether each of these has
	 *         subclasses
	 * Idea was proposed by Rafael Goncalves and code was modified by Ruvin Yusubov
	 */
	public Map<String, Boolean> getDirectSubclasses(String concept) {
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		Map<OWLClass, Boolean> classMap = getDirectSubclasses(syntaxTransformations.owlifyConcept(concept));
		////System.out.println("hi");
		for (OWLClass subc : classMap.keySet()) {
			map.put(syntaxTransformations.getManchesterRendering(subc), classMap.get(subc));
		}
		return map;
	}
	
	/**
	 * Get direct OWL subclasses of a given concept, and whether each of those
	 * has subclasses
	 * 
	 * @param c
	 *            OWL class
	 * @return A map of direct OWL subclasses and whether each of these has
	 *         subclasses
	 * Idea was proposed by Rafael Goncalves and code was modified by Ruvin Yusubov
	 */
	public Map<OWLClass, Boolean> getDirectSubclasses(OWLClass c) {
		Map<OWLClass, Boolean> map = new HashMap<OWLClass, Boolean>();
		for (OWLClass sub : reasoner.getSubClasses(c, true).getFlattened()) {
			if (!sub.isBottomEntity() && !sub.isTopEntity() && !sub.equals(c)) {
				Set<OWLClass> subcs = reasoner.getSubClasses(sub, true)
						.getFlattened();
				subcs.remove(bottom);
				map.put(sub, !subcs.isEmpty());
			}
		}
		return map;
	}
	
	/**
	 * Given a concept name, get the set of all its subclasses (including indirect ones) 
	 * @param concept	OWL class
	 * @return Set of concept names
	 */
	public Set<String> getAllSubclassNames(OWLClass c) {
		return syntaxTransformations.getManchesterRendering(getAllSubclasses(c));
	}

	/**
	 * Given a concept name, get the set of all its subclasses (including indirect)
	 * @param concept	OWL class
	 * @return Set of OWL subclasses
	 */
	public Set<OWLClass> getAllSubclasses(OWLClass concept) {
		return reasoner.getSubClasses(concept, false).getFlattened();
	}
	
}
