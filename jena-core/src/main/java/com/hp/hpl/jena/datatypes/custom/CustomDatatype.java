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

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.runtime.ECMAException;

/**
 *
 * @author maxime.lefrancois
 */
public class CustomDatatype extends BaseDatatype {

    private static final ScriptEngineManager engineManager = new ScriptEngineManager();
    private static final ScriptEngine engine = engineManager.getEngineByName("JavaScript");
    private static final Invocable invocable = (Invocable) engine;
    private static final Map<String, JSCustomDatatypeFactory> customDatatypeFactories = new HashMap<>();
    private static final Map<String, CustomDatatype> customDatatypes = new HashMap<>();

    /**
     * Gets a custom Datatype. The custom Datatype is instantiated using a
     * JavaScript definition file found at the URI of the Datatype.
     *
     * @param uri the URI of the custom Datatype
     * @return the custom datatype, null if it is not defined at the location of
     * its URI.
     */
    public static CustomDatatype getCustomDatatype(String uri) {
        if (customDatatypes.containsKey(uri)) {
            return customDatatypes.get(uri);
        }
        String fileurl = uri.contains("#") ? uri.substring(0, uri.indexOf("#")) : uri;
        // Fetch the Datatype Definition file.
        JSCustomDatatypeFactory factory = getCustomDatatypeFactory(fileurl);
        if (factory == null) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "No Datatype Definition file found at URL <{0}>. A basic Datatype will be used.", uri);
            return null;
        }
        ScriptObjectMirror jsMirror = factory.getDatatype(uri);
        if (jsMirror == null) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Custom Datatype <{0}> cannot be instantiated using the method getDatatype found at its URL. A basic Datatype will be used instead.", uri);
            return null;
        }
        JSCustomDatatype jsCustomDatatype = invocable.getInterface(jsMirror, JSCustomDatatype.class);
        if (jsCustomDatatype == null) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Custom Datatype <{0}> must implement interface JSCustomDatatype. A basic Datatype will be used instead.", uri);
            return null;
        }
        CustomDatatype datatype = new CustomDatatype(uri, jsMirror, jsCustomDatatype);
        customDatatypes.put(uri, datatype);
        return datatype;
    }

    /**
     * Fetches method getDatatype in a remote custom Datatype definition file.
     *
     * @param fileurl the URL of the custom Datatype definition file
     * @return the factory, null if none was found at this URL.
     */
    public static JSCustomDatatypeFactory getCustomDatatypeFactory(String fileurl) {
        if (customDatatypeFactories.containsKey(fileurl)) {
            return customDatatypeFactories.get(fileurl);
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
            JSCustomDatatypeFactory jsCustomDatatypeFactory = invocable.getInterface(JSCustomDatatypeFactory.class);
            customDatatypeFactories.put(fileurl, jsCustomDatatypeFactory);
            return jsCustomDatatypeFactory;
        } catch (MalformedURLException ex) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Malformed custom datatypes definition file URI: <" + fileurl + ">", ex);
        } catch (IOException | ScriptException ex) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Error while loading custom datatypes definition file: <" + fileurl + ">", ex);
        }
        customDatatypeFactories.put(fileurl, null);
        return null;
    }

    private final ScriptObjectMirror jsCustomDatatypeMirror;
    private final JSCustomDatatype jsCustomDatatype;
    private final CustomTypedValueCache cache;

    private CustomDatatype(String uri, ScriptObjectMirror jsCustomDatatypeMirror, JSCustomDatatype jsCustomDatatype) {
        super(uri);
        if (uri == null || jsCustomDatatypeMirror == null || jsCustomDatatype == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }
        this.cache = new CustomTypedValueCache();
        this.jsCustomDatatypeMirror = jsCustomDatatypeMirror;
        this.jsCustomDatatype = jsCustomDatatype;
    }

    /**
     * Get a CustomTypedValue in the cache or create one for the specified
     * lexical form
     *
     * @param lexicalForm
     * @return never null
     */
    private CustomTypedValue getCustomTypedValue(String lexicalForm) {
        return getCustomTypedValue(lexicalForm, null);
    }

    /**
     * Get a CustomTypedValue in the cache or create one for the specified
     * lexical form
     *
     * @param lexicalForm
     * @return never null
     */
    private CustomTypedValue getCustomTypedValue(String lexicalForm, ScriptObjectMirror litMirror) {
        if (cache.contains(lexicalForm)) {
            return cache.getCustomTypedValue(lexicalForm);
        }
        if (litMirror == null) {
            try {
                litMirror = jsCustomDatatype.createLiteral(lexicalForm);
            } catch (ECMAException ex) {
                Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Error while instantiating custom literal \"" + lexicalForm + "\"^^<" + getURI() + ">", ex);
                return cache.put(lexicalForm, null, null);
            }
            if (litMirror == null) {
                Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Error while instantiating custom literal \"{0}\"^^<{1}> got null.", new Object[]{lexicalForm, getURI()});
                return cache.put(lexicalForm, null, null);
            }
        }
        JSCustomLiteral litInterface = invocable.getInterface(litMirror, JSCustomLiteral.class);
        if (litInterface == null) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Custom literal \"{0}\"^^<{1}> must implement interface JSCustomLiteral", new Object[]{lexicalForm, getURI()});
            return cache.put(lexicalForm, null, null);
        }
        return cache.put(lexicalForm, litMirror, litInterface);
    }

    /**
     * Parse a lexical form of this datatype to a value
     *
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        CustomTypedValue customTypedValue = getCustomTypedValue(lexicalForm);
        if (customTypedValue.getMirror() == null) {
            throw new DatatypeFormatException("Error while instantiating custom datatype \"" + lexicalForm + "\"^^<" + getURI() + ">. See log for more details.");
        }
        return customTypedValue;
    }

    /**
     * Test whether the given string is a legal lexical form of this datatype.
     */
    @Override
    public boolean isValid(String lexicalForm) {
        return jsCustomDatatype.isLegal(lexicalForm);
    }

    @Override
    public boolean isValidLiteral(LiteralLabel lit) {
        if (super.isValidLiteral(lit)) {
            return true;
        }
        CustomDatatype otherCustomDatatype = getCustomDatatype(lit.getDatatypeURI());
        if (otherCustomDatatype == null) {
            return false;
        }
        CustomTypedValue otherCustomTypedValue = otherCustomDatatype.getCustomTypedValue(lit.getLexicalForm());
        if (otherCustomTypedValue == null || otherCustomTypedValue.getMirror() == null || otherCustomTypedValue.getInterface() == null) {
            return false;
        }
        // attempt importing.
        ScriptObjectMirror myMirror = jsCustomDatatype.importLiteral(otherCustomTypedValue.getMirror());
        if (myMirror == null) {
            return false;
        }
        String myLexicalForm = (String) myMirror.getMember("lexicalForm");
        if (myLexicalForm == null) {
            return false;
        }
        CustomTypedValue myCustomTypedValue = getCustomTypedValue(myLexicalForm, myMirror);
        if (myCustomTypedValue.getInterface() != null && myCustomTypedValue.getMirror() != null) {
            return true;
        }
        // attempt exporting.
        myMirror = otherCustomTypedValue.getInterface().exportTo(jsCustomDatatypeMirror);
        if (myMirror == null) {
            return false;
        }
        myLexicalForm = (String) myMirror.getMember("lexicalForm");
        if (myLexicalForm == null) {
            return false;
        }
        myCustomTypedValue = getCustomTypedValue(myLexicalForm, myMirror);
        if (myCustomTypedValue.getInterface() != null && myCustomTypedValue.getMirror() != null) {
            return true;
        }
        return false;
    }

    /**
     * Compares two instances of values of the given datatype. This default
     * requires value and datatype equality.
     */
    @Override
    public boolean isEqual(LiteralLabel litLabel1, LiteralLabel litLabel2) {
        if (super.isEqual(litLabel1,litLabel2)) {
            return true;
        }
        CustomDatatype customDatatype1 = getCustomDatatype(litLabel1.getDatatypeURI());
        if (customDatatype1 == null) {
            return false;
        }
        CustomTypedValue customTypedValue1 = customDatatype1.getCustomTypedValue(litLabel1.getLexicalForm());
        if (customTypedValue1 == null || customTypedValue1.getMirror() == null || customTypedValue1.getInterface() == null) {
            return false;
        }
        CustomDatatype customDatatype2 = getCustomDatatype(litLabel2.getDatatypeURI());
        if (customDatatype2 == null) {
            return false;
        }
        CustomTypedValue customTypedValue2 = customDatatype2.getCustomTypedValue(litLabel2.getLexicalForm());
        if (customTypedValue2 == null || customTypedValue2.getMirror() == null || customTypedValue2.getInterface() == null) {
            return false;
        }
        return customTypedValue1.getInterface().equals(customTypedValue2.getMirror()) 
                || customTypedValue2.getInterface().equals(customTypedValue1.getMirror());
    }

    /**
     * Cannonicalise a java Object value to a normal form. Primarily used in
     * cases such as xsd:integer to reduce the Java object representation to the
     * narrowest of the Number subclasses to ensure that indexing of typed
     * literals works.
     */
    @Override
    public Object cannonicalise(Object value) {
        if(! (value instanceof CustomTypedValue)) {
            return null;
        }
        CustomTypedValue customTypedValue = (CustomTypedValue) value;
        JSCustomLiteral jsInterface = customTypedValue.getInterface();
        if(jsInterface == null) {
            return null;
        }
        ScriptObjectMirror newMirror = jsInterface.cannonicalise();
        if(newMirror == null) {
            return null;
        }
        String newLexicalForm = (String) newMirror.getMember("lexicalForm");
        if (newLexicalForm == null) {
            return null;
        }
        CustomTypedValue newCustomTypedValue = getCustomTypedValue(newLexicalForm, newMirror);
        if(newCustomTypedValue.getMirror()==null) {
            return null;
        }
        return newCustomTypedValue;
    }

    public static class CustomTypedValue extends TypedValue {

        private final ScriptObjectMirror jsMirror;
        private final JSCustomLiteral jsInterface;

        public CustomTypedValue(String lexicalValue, ScriptObjectMirror jsMirror, JSCustomLiteral jsInterface, String datatypeURI) {
            super(lexicalValue, datatypeURI);
            this.jsMirror = jsMirror;
            this.jsInterface = jsInterface;
        }

        public ScriptObjectMirror getMirror() {
            return jsMirror;
        }

        public JSCustomLiteral getInterface() {
            return jsInterface;
        }

    }

    private class CustomTypedValueCache {

        private static final int CACHE_LIMIT = 100000;
        private final Queue<String> valid = new LinkedList<>();
        private final Map<String, CustomTypedValue> cachedItems = new HashMap<>();

        public boolean contains(String key) {
            return valid.contains(key);
        }

        public CustomTypedValue getCustomTypedValue(String lexicalForm) {
            return cachedItems.get(lexicalForm);
        }

        public CustomTypedValue put(String lexicalForm, ScriptObjectMirror jsMirror, JSCustomLiteral jsInterface) {
            if (valid.size() >= CACHE_LIMIT) {
                String oldest = valid.poll();
                cachedItems.remove(oldest);
            }
            CustomTypedValue customTypedValue = new CustomTypedValue(lexicalForm, jsMirror, jsInterface, getURI());
            cachedItems.put(lexicalForm, customTypedValue);
            return customTypedValue;
        }

    }
}
