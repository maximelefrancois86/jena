/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.emse.iscod.lindt;

import com.hp.hpl.jena.datatypes.BaseDatatype.TypedValue;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.custom.CustomDatatype;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
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
    
    public static void main(String[] args) {
        test2();
    }
    
    public static void test1() {
        JenaParameters.enableDiscoveryOfCustomDatatypes = true;
        String dturi =  "http://www.maxime-lefrancois.info/lindt/datatypes.js#length";
        
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
     
    }
    
    public static void test2() {
                
        JenaParameters.enableDiscoveryOfCustomDatatypes = true;
        String dturi =  "http://www.maxime-lefrancois.info/lindt/datatypes.js#length";
        CustomDatatype ldt = (CustomDatatype) TypeMapper.getInstance().getSafeTypeByName(dturi);
        
        Model model1 = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        Resource a = model1.createResource("http://ex.org/a");
        Property p = model1.createProperty("http://ex.org/length");
        String[] strings = {"150m", "15mi", "12mft", "147mm", "1km", "1000m"};
        for(String s: strings) {
            Literal l = model1.createTypedLiteral(s, dturi);
            model1.add(a, p, l);
        }
           
        String queryString = "SELECT ?o1 WHERE { ?x <http://ex.org/length> ?o1 FILTER ( ?o1<=\"153m\"^^<http://www.maxime-lefrancois.info/lindt/datatypes.js#length> ) } ORDER BY ?o1 " ;
        Query query = QueryFactory.create(queryString) ;
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model1)) {
          ResultSet results = qexec.execSelect() ;
          for ( ; results.hasNext() ; )
          {
            QuerySolution soln = results.nextSolution() ;
            Literal l1 = soln.getLiteral("o1") ;   // Get a result variable - must be a literal
            String normalized = ((TypedValue) ldt.cannonicalise(ldt.parse(l1.getLexicalForm()))).lexicalValue;
            System.out.println(l1 + " < 153m -- in meters: " + normalized);
                          
          }
        }

    }
}
