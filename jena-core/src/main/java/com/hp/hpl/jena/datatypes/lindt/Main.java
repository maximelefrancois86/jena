package com.hp.hpl.jena.datatypes.lindt;

import com.hp.hpl.jena.datatypes.BaseDatatype.TypedValue;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.impl.JenaParameters;

/**
 *
 * @author maxime.lefrancois
 */
public class Main {
    
    public static void main(String[] args) throws LindtException {
        test1();
        

    }
    
    public static void test1() {
        JenaParameters.enableDiscoveryOfCustomLinkedDatatypes = true;
        String dturi =  "http://www.maxime-lefrancois.info/lindt/datatypes.js#length";
        RDFDatatype lengthdt = TypeMapper.getInstance().getSafeTypeByName(dturi);
        
        Model model1 = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        Resource r1 = model1.createResource("http://ex.org/a");
        Property p1 = model1.createProperty("http://ex.org/length");
        Literal l1 = model1.createTypedLiteral("1000m", dturi);
        model1.add(r1, p1, l1);
        
        Resource r2 = model1.createResource("http://ex.org/a");
        Property p2 = model1.createProperty("http://ex.org/length");
        Literal l2 = model1.createTypedLiteral("1km", dturi);
        Statement s = model1.createStatement(r2, p2, l2);
        System.out.println(model1.contains(s));
        
        System.out.println(((TypedValue) lengthdt.cannonicalise(lengthdt.parse(l2.getLexicalForm()))).lexicalValue);
        
        
        
    }
    
}
