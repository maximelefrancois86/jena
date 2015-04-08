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
            if(e.eval("Datatype1.import(lexicalForm2, uri2)")!=null) {
                return true;                
            }
        } catch (ScriptException ex) {}
        try {
            if(e.eval("literal2.exportTo(uri1)")!=null) {
                return true;
            }
        } catch (ScriptException ex) {}
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
            e.put("lexicalForm1", litLabel1.getLexicalForm());
            e.eval("var Datatype1 = lindt.getDatatype(uri1);");
            e.eval("var literal1 = new Datatype1(lexicalForm1);");

            e.put("uri2", litLabel2.getDatatypeURI());
            e.put("lexicalForm2", litLabel2.getLexicalForm());
            e.eval("var Datatype2 = lindt.getDatatype(uri2);");
            e.eval("var literal2 = new Datatype2(lexicalForm2);");
            return (boolean) e.eval("literal1.equals(literal2)") || (boolean) e.eval("literal2.equals(literal1)");
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
            e.eval("var literal2 = literal.cannonicalise();");
            return new TypedValue((String) e.eval("literal2.lexicalForm"), (String) e.eval("literal2.datatype.uri"));
        } catch (ScriptException ex) { }
        return value;
    }
    
    
}
