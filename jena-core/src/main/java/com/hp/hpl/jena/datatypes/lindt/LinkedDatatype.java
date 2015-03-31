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
package com.hp.hpl.jena.datatypes.lindt;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 *
 * @author maxime.lefrancois
 */
public class LinkedDatatype extends BaseDatatype {

    private final LindtEngine engine;

    LinkedDatatype(String uri, LindtEngine engine) {
        super(uri);
        this.engine = engine;
    }

    @Override
    public String getURI() {
        return uri;
    }
    
    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
        return ((TypedValue)value).lexicalValue;
    }
    
    /**
     * Parse a lexical form of this datatype to a value
     *
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        try {
            ScriptEngine e = engine.getEngine();
            e.put("uri", getURI());
            e.put("lexicalForm", lexicalForm);
            e.eval("var Datatype = lindt.getDatatype(uri);");
            e.eval("var literal = new Datatype(lexicalForm);");
            return new TypedValue(lexicalForm, getURI());
        } catch (ScriptException ex) {
            throw new DatatypeFormatException(lexicalForm, this, ex.getMessage());
        }
    }

    @Override
    public boolean isValidLiteral(LiteralLabel lit) {
        if(super.isValidLiteral(lit)) {
            return true;
        }
        ScriptEngine e = engine.getEngine();
        e.put("uri1", getURI());
        e.put("uri2", lit.getDatatypeURI());
        e.put("lexicalForm2", lit.getLexicalForm());
        try {
            e.eval("var Datatype1 = lindt.getDatatype(uri1);");
            e.eval("var Datatype2 = lindt.getDatatype(uri2);");
            e.eval("var literal2 = new Datatype2(lexicalForm2);");
            try {
                e.eval("var literal1 = new Datatype1(literal2);");
                return true;                
            } catch (ScriptException ex) {}
            try {
                e.eval("var literal1 = literal2.toDatatype(Datatype1);");
                return true;                
            } catch (ScriptException ex) {}
        } catch (ScriptException ex) { }
        return false;
    }
    
    /**
     * Compares two instances of values of the given datatype.
     * This default requires value and datatype equality.
     */
    @Override
    public boolean isEqual(LiteralLabel litLabel1, LiteralLabel litLabel2) {
        try {
            ScriptEngine e = engine.getEngine();
            e.put("uri1", litLabel1.getDatatypeURI());
            e.put("uri2", litLabel2.getDatatypeURI());
            e.put("lexicalForm1", litLabel1.getLexicalForm());
            e.put("lexicalForm2", litLabel2.getLexicalForm());
            e.eval("var Datatype1 = lindt.getDatatype(uri1);");
            e.eval("var Datatype2 = lindt.getDatatype(uri2);");
            e.eval("var literal1 = new Datatype1(lexicalForm1);");
            e.eval("var literal2 = new Datatype2(lexicalForm2);");
            boolean eq1 = (boolean) e.eval("literal1.equals(literal2)");
            boolean eq2 = (boolean) e.eval("literal2.equals(literal1)");
            return eq1 || eq2;
        } catch (ScriptException ex) { }
        return false;
    }
    
    /**
     * Cannonicalise a java Object value to a normal form.
     * Primarily used in cases such as xsd:integer to reduce
     * the Java object representation to the narrowest of the Number
     * subclasses to ensure that indexing of typed literals works. 
     */
    @Override
    public Object cannonicalise( Object value ) {
        try {
            TypedValue typedValue = (TypedValue) value;
            ScriptEngine e = engine.getEngine();
            e.put("uri", typedValue.datatypeURI);
            e.put("lexicalForm", typedValue.lexicalValue);
            e.eval("var Datatype = lindt.getDatatype(uri);");
            e.eval("var literal = new Datatype(lexicalForm);");
            e.eval("var literal2 = literal.canonicalise();");
            return new TypedValue((String) e.eval("literal2.lexicalValue"), (String) e.eval("literal2.datatypeURI"));
        } catch (ScriptException ex) { }
        return value;
    }
    
    /**
     * Normalization. If the value is narrower than the current data type
     * (e.g. value is xsd:date but the time is xsd:datetime) returns
     * the narrower type for the literal. 
     * If the type is narrower than the value then it may normalize
     * the value (e.g. set the mask of an XSDDateTime)
     * Currently only used to narrow gener XSDDateTime objects
     * to the minimal XSD date/time type.
     * @param value the current object value
     * @param dt the currently set data type
     * @return a narrower version of the datatype based on the actual value range
     */
    @Override
    public RDFDatatype normalizeSubType(Object value, RDFDatatype dt) {
//        ScriptEngine e = engine.getEngine();
//        e.put("uri1", getURI());
//        e.put("uri2", lit.getDatatypeURI());
//        e.put("lexicalForm2", lit.getLexicalForm());
//        try {
//            e.eval("var Datatype1 = lindt.getDatatype(uri1);");
//            e.eval("var Datatype2 = lindt.getDatatype(uri2);");
//            e.eval("var literal2 = new Datatype2(lexicalForm2);");
//            try {
//                e.eval("var literal1 = new Datatype1(literal2);");
//                return true;                
//            } catch (ScriptException ex) {}
//            try {
//                e.eval("var literal1 = literal2.toDatatype(Datatype1);");
//                return true;                
//            } catch (ScriptException ex) {}
//        } catch (ScriptException ex) { }
        return dt;
    }
    
}
