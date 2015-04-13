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
package com.hp.hpl.jena.datatypes.custom;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.impl.JenaParameters;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

/**
 *
 * @author maxime.lefrancois
 */
public class Main {
    
    public static void main(String[] args) throws ScriptException {
        test1();
        

    }
    
    public static void test1() {
        JenaParameters.enableDiscoveryOfCustomDatatypes = true;
        String dturi =  "http://www.maxime-lefrancois.info/lindt/datatypes.js#length";
//        RDFDatatype lengthdt = TypeMapper.getInstance().getSafeTypeByName(dturi);

//        System.setProperty("rhino.opt.level", -1);
        
        long t1 = System.currentTimeMillis();
        Model model1 = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
        for(int i=0;i<100;i++) {
            Resource r1 = model1.createResource("http://ex.org/a"+i);
            Property p1 = model1.createProperty("http://ex.org/length"+i);
            Literal l1 = model1.createTypedLiteral(Math.random()*i+"m", dturi);
            model1.add(r1, p1, l1);
        }
        long t = System.currentTimeMillis()-t1 ;
        System.out.println("processed in " + t + "ms");
        
//
//        Resource r2 = model1.createResource("http://ex.org/a");
//        Property p2 = model1.createProperty("http://ex.org/length");
//        Literal l2 = model1.createTypedLiteral("1km", dturi);
//        Statement s = model1.createStatement(r2, p2, l2);
//
//        System.out.println(((TypedValue) lengthdt.cannonicalise(lengthdt.parse(l2.getLexicalForm()))).lexicalValue);
        
        
    }
    
    static void test2() throws ScriptException {
        ScriptEngine engine = (new ScriptEngineManager()).getEngineByName("JavaScript");
        Invocable invocable = (Invocable) engine;
        
        engine.eval("var say = function() {"
                + " return \"hello\";"
                + "};");
        
        Hello h = invocable.getInterface(Hello.class);
        System.out.println(h.say());

        
        engine.eval("var say = function() {"
                + " return \"goodbye\";"
                + "};");
        
        Hello h2 = invocable.getInterface(Hello.class);
        System.out.println(h.say());
        System.out.println(h2.say());

    }    
    static void test3() throws ScriptException {
        ScriptEngine engine = (new ScriptEngineManager()).getEngineByName("JavaScript");
        Invocable invocable = (Invocable) engine;
        
        engine.eval("var say = function() {"
                + " throw new Error();"
                + "};");
        
        Hello h = invocable.getInterface(Hello.class);
        try {
            System.out.println(h.say());
        } catch (Exception e) {
            System.out.println(e.getClass().getSimpleName());
            // returns ECMAException
        }


    }    
    static void test4() throws ScriptException {
        ScriptEngine engine = (new ScriptEngineManager()).getEngineByName("JavaScript");
        Invocable invocable = (Invocable) engine;
        
        engine.eval("var i=0;"
                + "var add = function() {"
                + " i=i+1;"
                + "};"
                + "var get = function() {"
                + " return i;"
                + "}");
        
        II h = invocable.getInterface(II.class);
        long t = 0;
        for(int i=0;i<1000;i++) {
            long t1 = System.currentTimeMillis();
            h.add();
            t += System.currentTimeMillis() - t1;
        }
        System.out.println(h.get() + " : " + t);
        t = -System.currentTimeMillis();
        for(int i=0;i<1000;i++) {
            h.add();
        }
        t += System.currentTimeMillis();
        System.out.println(h.get() + " : " + t);


        System.out.println(h.get() + " : " + t);
        t = -System.currentTimeMillis();
        for(int i=0;i<1000;i++) {
            engine.eval("add()");
        }
        t += System.currentTimeMillis();
        System.out.println(h.get() + " : " + t);
        System.out.println(engine.eval("get()") + " : " + t);


    }
    public interface Hello {
        String say();
    }
    public interface II {
        String add();
        int get();
    }

}
