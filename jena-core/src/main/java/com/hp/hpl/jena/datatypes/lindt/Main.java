package com.hp.hpl.jena.datatypes.lindt;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.impl.JenaParameters;

/**
 *
 * @author maxime.lefrancois
 */
public class Main {
    
    
    public static void main(String[] args) throws LindtException {
        JenaParameters.enableDiscoveryOfCustomLinkedDatatypes = true;
        Model model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        Literal l1 = model.createTypedLiteral("one", "lindt/randomdt.js#cardinal");
        Literal l2 = model.createTypedLiteral(1);
        Literal l3 = model.createTypedLiteral("one", "lindt/randomdt.js#cardinal");
        Literal l4 = model.createTypedLiteral("two", "lindt/randomdt.js#cardinal");
        Literal l5 = model.createTypedLiteral("two", "lindt/randomdt.js#cardinal");
        System.out.println(l1.getLexicalForm());
        Object v1 = l1.getValue();
//        System.out.println(l1.sameValueAs(l1));
        System.out.println(l1.sameValueAs(l2));
        System.out.println(l2.sameValueAs(l1));
//        System.out.println(l1.sameValueAs(l3));
//        System.out.println(l4.sameValueAs(l5));
    }
    
}
