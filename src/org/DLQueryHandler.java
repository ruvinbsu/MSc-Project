/*
 * This file is part of the OWL API.
 *
 * The contents of this file are subject to the LGPL License, Version 3.0.
 *
 * Copyright (C) 2011, The University of Manchester
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0
 * in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 *
 * Copyright 2011, The University of Manchester
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * Some parts of the original code was modified by Ruvin Yusubov
 * MSc in Advance Computer Science
 * School of Computer Science
 * The University of Manchester
 */

package org;

import java.util.Collections;
import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.ShortFormProvider;

public class DLQueryHandler {
	
	private OWLReasoner reasoner;
	
	public DLQueryHandler(OWLReasoner reasoner) {
		this.reasoner = reasoner;
	}
	
	/**
	 * This example shows how to perform a "dlquery". The DLQuery view/tab in
	 * Protege 4 works like this.
	 */
	class DLQueryEngine {
		private final DLQueryParser parser;

	    /**
	     * Constructs a DLQueryEngine. This will answer "DL queries" using the
	     * specified reasoner. A short form provider specifies how entities are
	     * rendered.
	     * 
	     * @param reasoner
	     *        The reasoner to be used for answering the queries.
	     * @param shortFormProvider
	     *        A short form provider.
	     */
		public DLQueryEngine(ShortFormProvider shortFormProvider) {
			OWLOntology rootOntology = reasoner.getRootOntology();
			parser = new DLQueryParser(rootOntology, shortFormProvider);
		}

	    /**
	     * Gets the instances of a class expression parsed from a string.
	     * 
	     * @param classExpressionString
	     *        The string from which the class expression will be parsed.
	     * @param direct
	     *        Specifies whether direct instances should be returned or not.
	     * @return The instances of the specified class expression If there was a
	     *         problem parsing the class expression.
	     */
		public Set<OWLNamedIndividual> getInstances(String classExpressionString, boolean direct) throws ParserException {
			if (classExpressionString.trim().length() == 0) {
				return Collections.emptySet();
			}
			OWLClassExpression classExpression = parser
					.parseClassExpression(classExpressionString);
			
			NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(
					classExpression, direct);
	        
			return individuals.getFlattened();
		}
 
	    /**
	     * Gets the equivalent classes of a class expression parsed from a string.
	     * 
	     * @param classExpressionString
	     *        The string from which the class expression will be parsed.
	     * @return The equivalent classes of the specified class expression If there
	     *         was a problem parsing the class expression.
	     */		
		public Set<OWLClass> getEquivalentClasses(String classExpressionString) throws ParserException {
			if (classExpressionString.trim().length() == 0) {
				return Collections.emptySet();
			}
			OWLClassExpression classExpression = parser
					.parseClassExpression(classExpressionString);
			
			
			Node<OWLClass> equivalentClasses = reasoner
					.getEquivalentClasses(classExpression);
			
			Set<OWLClass> result;
			if (classExpression.isAnonymous()) {
				result = equivalentClasses.getEntities();
			} else {
				result = equivalentClasses.getEntitiesMinus(classExpression
						.asOWLClass());
			}
			
			return result;
		}
		
	    /**
	     * Gets the subclasses of a class expression parsed from a string.
	     * 
	     * @param classExpressionString
	     *        The string from which the class expression will be parsed.
	     * @param direct
	     *        Specifies whether direct subclasses should be returned or not.
	     * @return The subclasses of the specified class expression If there was a
	     *         problem parsing the class expression.
	     */
		public Set<OWLClass> getSubClasses(String classExpressionString, boolean direct) throws ParserException {
			if (classExpressionString.trim().length() == 0) {
				return Collections.emptySet();
			}
			OWLClassExpression classExpression = parser
					.parseClassExpression(classExpressionString);
			
			NodeSet<OWLClass> subClasses = reasoner.getSubClasses(
					classExpression, direct);
			
			return subClasses.getFlattened();
		}

	}

	class DLQueryParser {

		private final OWLOntology rootOntology;
		private final BidirectionalShortFormProvider bidiShortFormProvider;

	    /**
	     * Constructs a DLQueryParser using the specified ontology and short form
	     * provider to map entity IRIs to short names.
	     * 
	     * @param rootOntology
	     *        The root ontology. This essentially provides the domain vocabulary
	     *        for the query.
	     * @param shortFormProvider
	     *        A short form provider to be used for mapping back and forth
	     *        between entities and their short names (renderings).
	     */
		public DLQueryParser(OWLOntology rootOntology,
				ShortFormProvider shortFormProvider) {
			this.rootOntology = rootOntology;
			OWLOntologyManager manager = rootOntology.getOWLOntologyManager();
			Set<OWLOntology> importsClosure = rootOntology.getImportsClosure();
			// Create a bidirectional short form provider to do the actual
			// mapping.
			// It will generate names using the input
			// short form provider.
			bidiShortFormProvider = new BidirectionalShortFormProviderAdapter(
					manager, importsClosure, shortFormProvider);
		}

	    /**
	     * Parses a class expression string to obtain a class expression.
	     * 
	     * @param classExpressionString
	     *        The class expression string
	     * @return The corresponding class expression if the class expression string
	     *         is malformed or contains unknown entity names.
	     */
		public OWLClassExpression parseClassExpression(
				String classExpressionString) throws ParserException {
			OWLDataFactory dataFactory = rootOntology.getOWLOntologyManager()
					.getOWLDataFactory();
			// Set up the real parser
			ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(
					dataFactory, classExpressionString);
			parser.setDefaultOntology(rootOntology);
			// Specify an entity checker that wil be used to check a class
			// expression contains the correct names.
			OWLEntityChecker entityChecker = new ShortFormEntityChecker(
					bidiShortFormProvider);
			parser.setOWLEntityChecker(entityChecker);
			// Do the actual parsing
			return parser.parseClassExpression();
		}
	}

	class DLQueryPrinter {

		private final DLQueryEngine dlQueryEngine;
		private final ShortFormProvider shortFormProvider;

	    /**
	     * @param engine
	     *        the engine
	     * @param shortFormProvider
	     *        the short form provider
	     */
		public DLQueryPrinter(DLQueryEngine engine,
				ShortFormProvider shortFormProvider) {
			this.shortFormProvider = shortFormProvider;
			dlQueryEngine = engine;
		}

		/**
		 * @param classExpression
		 *            the class expression to use for interrogation
		 */
		public Set<OWLClass> askQueryForDocumetConcept(String classExpression, boolean bol) {
			if (classExpression.length() == 0) {
				return null;
			} else {
				try {

					Set<OWLClass> equivalentClasses = dlQueryEngine
							.getEquivalentClasses(classExpression);

					
					Set<OWLClass> subClasses; 
					
						
						
					subClasses = dlQueryEngine.getSubClasses(classExpression, bol); 
					
					subClasses.addAll(equivalentClasses);

					if (subClasses.size() != 0)
						return subClasses;
					else
						return null;

				} catch (ParserException e) {
					return null;
				}
			}
		}

		/**
		 * @param classExpression
		 *            the class expression to use for interrogation
		 */
		public Set<OWLNamedIndividual> askQuery(String classExpression) {
			if (classExpression.length() == 0) {
				
				return null;
			} else {
				try {
					
					Set<OWLNamedIndividual> individuals = dlQueryEngine.getInstances(classExpression, false);// false -indirect as well!

					if (individuals.size() != 0)
						return individuals;
					else
						return null;

				} catch (ParserException e) {
					return null;

				}
			}
		}
	}


}
