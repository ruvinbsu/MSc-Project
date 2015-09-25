/*
Created by Ruvin Yusubov
MSc in Advance Computer Science
School of Computer Science
The University of Manchester
 */
package org;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.HermiT.Reasoner;

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

/**
 * Servlet implementation class FirstLoading
 */
@WebServlet("/FirstLoading")
public class FirstLoading extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FirstLoading() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		long tSLoadingTime = System.currentTimeMillis();
		
		//Initialization of ontology.
		OWLOntologyManager man = null;
		OWLOntology ont = null;
		
		PrintingHandler n;
		man = OWLManager.createOWLOntologyManager();

		try {
			String webOntFile = "http://localhost:8080/MSc_project/ontology/final_ontology.owl";

			ont = man.loadOntologyFromOntologyDocument(IRI.create(webOntFile));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		n = new PrintingHandler(ont);
	
		OWLReasoner reasoner = n.getReasoner(); 
		request.getSession().setAttribute("reasoner", reasoner);
		
		//Each of the following String contains necessary HTML code in order to display correspondent facet in the web-page.
		String docTree = n.printHierarchy("Document", "document");
		String alarmTree = n.printHierarchy("Alarm", "alarm");
		String functionalAreaTree = n.printHierarchy("Functional Area", "functionalarea");
		String taskTree = n.printHierarchy("Task", "task");
		String instanceTree = n.printInstancesHierarchy();
						
		request.setAttribute("docTree", docTree);
		request.setAttribute("alarmTree", alarmTree);
		request.setAttribute("functionalAreaTree", functionalAreaTree);
		request.setAttribute("taskTree", taskTree);
		request.setAttribute("instanceTree", instanceTree);
		
		//Sending all the HTML code described above to JSP in order to display facets in the web-page.
		request.getRequestDispatcher("/index.jsp").forward(request, response);
		
		long tELoadingTime = System.currentTimeMillis();
		long tDelta = tELoadingTime - tSLoadingTime;
		double elapsedSeconds = tDelta / 1000.0;
		System.out.println("Get Loading Time: " + elapsedSeconds);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
