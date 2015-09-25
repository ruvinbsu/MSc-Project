/**
 * The methods and functions of this class was created by Rafael Goncalves
 * Information Management Group (IMG)
 * School of Computer Science
 * University of Manchesterer
 **/
package org;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer;

public class SyntaxTransformations {
	
	private final String ontURI = "http://www.semanticweb.org/ruvin/ontologies/2014/1/final_ontology.owl#";
	private SimpleShortFormProvider fp = new SimpleShortFormProvider();
	private OWLOntology ont;
	private OWLOntologyManager man;
	private OWLDataFactory df;
	
	public SyntaxTransformations(OWLOntology ont){
		this.ont = ont;
		man = ont.getOWLOntologyManager();
		df= man.getOWLDataFactory();
	}
	
	/**
	 * Get usage of a concept
	 * @param term	Concept name
	 * @return Usage of concept
	 */
	public Set<String> getConceptUsage(String term) {
		return getUsageOfConcept(term);
	}
	
	/**
	 * Get usage of a concept
	 * 
	 * @param c
	 *            Concept c
	 * @return Axioms that mention this concept
	 */
	public Set<String> getUsageOfConcept(String c) {
		return getManchesterRendering(ont.getReferencingAxioms(owlifyConcept(c)));
	}
	
	/**
	 * Convert a set of strings into a single, JavaScript-interpretable string
	 * @param strings	Set of terms
	 * @return JavaScript-ready string
	 */
	public String stringify(Set<String> strings, String sep) {
		String result = "";
		int counter = 0;
		for(String s : strings) {
			counter++;
			result += s;
			if(counter < strings.size())
				result += sep;
		}
		return result;
	}
	
	/**
	 * Given a concept name, return an OWLClass object
	 * 
	 * @param concept
	 *            Concept name
	 * @return OWLClass for the given concept
	 */
	public OWLClass owlifyConcept(String concept) {
		if (!concept.startsWith("http://"))
			concept = ontURI + concept;
		
		concept = concept.replaceAll(" ", "_");
		return df.getOWLClass(IRI.create(concept));
	}
	
	/**
	 * Given a role name, return an OWLObjectProperty object
	 * 
	 * @param role
	 *            Role name
	 * @return OWLObjectProperty for the given role
	 */
	public OWLObjectProperty owlifyRole(String role) {
		if (!role.startsWith("http://"))
			role = ontURI + role;
		role = role.replaceAll(" ", "_");
		return df.getOWLObjectProperty(IRI.create(role));
	}
	
	public OWLNamedIndividual owlifyIndividual(String ind) {
		if (!ind.startsWith("http://"))
			ind = ontURI + ind;
		ind = ind.replaceAll(" ", "_");
		return df.getOWLNamedIndividual(IRI.create(ind));
	}
	
	/**
	 * Get Manchester syntax of an OWL object
	 * 
	 * @param obj
	 *            Instance of OWLObject
	 * @return Object name in Manchester syntax
	 */
	public String getManchesterRendering(OWLObject obj) {
		StringWriter wr = new StringWriter();
		ManchesterOWLSyntaxObjectRenderer render = new ManchesterOWLSyntaxObjectRenderer(wr, fp);
		render.setUseWrapping(false);
		obj.accept(render);
		String result = wr.getBuffer().toString();
		result = result.replaceAll("_", " ");
		return result;
	}
	
	/**
	 * Get Manchester syntax of a set of OWL classes
	 * 
	 * @param objs
	 *            Set of OWL classes
	 * @return Set of Manchester syntax class names
	 */
	public Set<String> getManchesterRendering(Set<? extends OWLObject> objs) {
		Set<String> results = new HashSet<String>();
		for (OWLObject o : objs) {
			results.add(getManchesterRendering(o));
		}
		return results;
	}
}
