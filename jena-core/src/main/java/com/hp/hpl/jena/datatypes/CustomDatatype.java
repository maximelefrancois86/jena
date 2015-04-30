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
package com.hp.hpl.jena.datatypes;

import fr.emse.customdatatypes.CustomDatatypeFactory;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.runtime.ConsString;
import jdk.nashorn.internal.runtime.ECMAException;

/**
 *
 * @author maxime.lefrancois
 */
public class CustomDatatype extends BaseDatatype {

    private static ScriptEngine engine = (new ScriptEngineManager()).getEngineByName("JavaScript");
    private static Invocable invocable = (Invocable) engine;
    private static final Map<String, CustomDatatypeFactory> cdtFactories = new HashMap<>();
    private static final Map<String, CustomDatatype> cdts = new HashMap<>();

    /**
     * Gets a custom Datatype. The custom Datatype is instantiated using a
     * JavaScript definition file found at the URI of the Datatype.
     *
     * @param uri the URI of the custom Datatype
     * @return the custom datatype, null if it is not defined at the location of
     * its URI.
     */
    public static CustomDatatype getCustomDatatype(String uri) {
        if (cdts.containsKey(uri)) {
            return cdts.get(uri);
        }
        String fileurl = uri.contains("#") ? uri.substring(0, uri.indexOf("#")) : uri;
        // Fetch the Datatype Definition file.
        CustomDatatypeFactory factory = getCustomDatatypeFactory(fileurl);
        if (factory == null) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "No javascript file with function getDatatype(uri) could be found at URL <{0}>. A basic Datatype will be used.", uri);
            return null;
        }
        // Call to JSCustomDatatypeFactory.getDatatype()
        ScriptObjectMirror cdtMirror;
        try {
            cdtMirror = factory.getDatatype(uri);
        } catch (ECMAException ex) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Error while calling function getDatatype(\"" + uri + "\") defined in its definition file. A basic Datatype will be used instead.", ex);
            return null;
        }
        if (cdtMirror == null) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Custom Datatype <{0}> cannot be instantiated using the method getDatatype found at its URL. A basic Datatype will be used instead.", uri);
            return null;
        }
        fr.emse.customdatatypes.CustomDatatype cdtInterface = invocable.getInterface(cdtMirror, fr.emse.customdatatypes.CustomDatatype.class);
        if (cdtInterface == null) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Custom Datatype <{0}> MUST implement interface JSCustomDatatype. A basic Datatype will be used instead.", uri);
            return null;
        }
        Logger.getLogger(CustomDatatype.class.getName()).log(Level.INFO, "Loaded custom Datatype <{0}>.", uri);
        CustomDatatype datatype = new CustomDatatype(uri, cdtInterface);
        cdts.put(uri, datatype);
        return datatype;
    }

    public static void reset() {
        engine = (new ScriptEngineManager()).getEngineByName("JavaScript");
        invocable = (Invocable) engine;
        cdtFactories.clear();
        cdts.clear();
    }

    /**
     * Fetches method getDatatype in a remote custom Datatype definition file.
     *
     * @param fileurl the URL of the custom Datatype definition file
     * @return the factory, null if none was found at this URL.
     */
    public static CustomDatatypeFactory getCustomDatatypeFactory(String fileurl) {
        if (cdtFactories.containsKey(fileurl)) {
            return cdtFactories.get(fileurl);
        }
        try {
            // attempt to load custom datatype definition file.
            URL dest = new URL(fileurl);
            HttpURLConnection conn = (HttpURLConnection) dest.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Accept", "application/javascript");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                engine.eval(reader);
            }
            CustomDatatypeFactory jsCustomDatatypeFactory = invocable.getInterface(CustomDatatypeFactory.class);
            cdtFactories.put(fileurl, jsCustomDatatypeFactory);
            return jsCustomDatatypeFactory;
        } catch (MalformedURLException ex) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Malformed custom datatypes definition file URI: <" + fileurl + ">", ex);
        } catch (IOException | ScriptException ex) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Error while loading custom datatypes definition file: <" + fileurl + ">", ex);
        }
        Logger.getLogger(CustomDatatype.class.getName()).log(Level.INFO, "Loaded custom datatype defintiion file at URL  <{0}>.", fileurl);
        cdtFactories.put(fileurl, null);
        return null;
    }

    private final fr.emse.customdatatypes.CustomDatatype cdtInterface;

    private CustomDatatype(String uri, fr.emse.customdatatypes.CustomDatatype cdtInterface) { 
        super(uri);
        if (uri == null || cdtInterface == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }
        this.cdtInterface = cdtInterface;
    }

    public fr.emse.customdatatypes.CustomDatatype getInterface() {
        return cdtInterface;
    }

    /**
     * Test whether the given string is a legal lexical form of this datatype.
     */
    @Override
    public boolean isValid(String lexicalForm) {
        Object isWellFormed;
        try {
            isWellFormed = cdtInterface.isWellFormed(lexicalForm);
        } catch(Exception ex) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "An exception occured: " + ex.getMessage(), ex);
            return false;
        }
        if(isWellFormed == null) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Return value must be non null");
            return false;
        }
        if(!(isWellFormed instanceof Boolean)) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Return value must be boolean. got: {0}", isWellFormed.getClass());
            return false;
        }
        return (boolean) isWellFormed;
    }

    /**
     * Parse a lexical form of this datatype to a value
     *
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        if (!isValid(lexicalForm)) {
            throw new DatatypeFormatException("Invalid lexical form: \"" + lexicalForm + "\"^^<" + getURI() + ">.");
        }
        Object normalForm;
        try {
            normalForm = cdtInterface.getNormalForm(lexicalForm);
        } catch(Exception ex) {
            throw new DatatypeFormatException(lexicalForm, this, "An exception occured: " + ex.getMessage());
        }
        if(normalForm == null) {
            throw new DatatypeFormatException(lexicalForm, this, "Return value must be non null");
        }
        if(!(normalForm instanceof ConsString)) {
            throw new DatatypeFormatException(lexicalForm, this, "Return value must be string. got: "+normalForm.getClass());
        }
        return new TypedValue(((ConsString)normalForm).toString(), getURI());
    }
    
    @Override
    public boolean isValidLiteral(LiteralLabel lit) {
        Object lexicalForm;
        try {
            lexicalForm = cdtInterface.importLiteral(lit.getLexicalForm(), lit.getDatatypeURI());
        } catch(Exception ex) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "An exception occured: " + ex.getMessage(), ex);
            return false;
        }
        if(lexicalForm == null) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Return value must be non null");
            return false;
        }
        if(!(lexicalForm instanceof String)) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Return value must be string. got: {0}", lexicalForm.getClass());
            return false;
        }
        return true;
    }

    @Override
    public boolean isEqual(LiteralLabel litLabel1, LiteralLabel litLabel2) {
        if (super.isEqual(litLabel1, litLabel2)) {
            return true;
        }
        if(!litLabel1.getDatatypeURI().equals(this.getURI())) {
            throw new  UnsupportedOperationException("Argument was thought to have this RDFDatatype as datatype");
        }
        if (litLabel1.isWellFormed() && litLabel2.isWellFormed() && litLabel1.getDefaultHashcode()==litLabel2.getDefaultHashcode()) {
            return litLabel1.getValue().equals(litLabel2.getValue());
        }
        return false;
    }

    @Override
    public Object cannonicalise(Object value) {
        TypedValue typedValue = (TypedValue) value;
        if(!typedValue.datatypeURI.equals(this.getURI())) {
            throw new  UnsupportedOperationException("Argument was thought to have this RDFDatatype as datatype");
        }
        return parse(typedValue.lexicalValue);
    }

    
}
