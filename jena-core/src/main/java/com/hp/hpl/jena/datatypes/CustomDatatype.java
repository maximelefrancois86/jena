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

import fr.emse.customdatatypes.JSCustomDatatype;
import fr.emse.customdatatypes.JSCustomDatatypeFactory;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
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

    private static final boolean USE_CACHE = true;
    private static final int CACHE_LIMIT = 100000;

    private static final ScriptEngineManager engineManager = new ScriptEngineManager();
    private static final ScriptEngine engine = engineManager.getEngineByName("JavaScript");
    private static final Invocable invocable = (Invocable) engine;
    private static final Map<String, JSCustomDatatypeFactory> cdtFactories = new HashMap<>();
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
        JSCustomDatatypeFactory factory = getCustomDatatypeFactory(fileurl);
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
        JSCustomDatatype cdtInterface = invocable.getInterface(cdtMirror, JSCustomDatatype.class);
        if (cdtInterface == null) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Custom Datatype <{0}> MUST implement interface JSCustomDatatype. A basic Datatype will be used instead.", uri);
            return null;
        }
        CustomDatatype datatype = new CustomDatatype(uri, cdtInterface);
        cdts.put(uri, datatype);
        return datatype;
    }

    /**
     * Fetches method getDatatype in a remote custom Datatype definition file.
     *
     * @param fileurl the URL of the custom Datatype definition file
     * @return the factory, null if none was found at this URL.
     */
    public static JSCustomDatatypeFactory getCustomDatatypeFactory(String fileurl) {
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
            JSCustomDatatypeFactory jsCustomDatatypeFactory = invocable.getInterface(JSCustomDatatypeFactory.class);
            cdtFactories.put(fileurl, jsCustomDatatypeFactory);
            return jsCustomDatatypeFactory;
        } catch (MalformedURLException ex) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Malformed custom datatypes definition file URI: <" + fileurl + ">", ex);
        } catch (IOException | ScriptException ex) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Error while loading custom datatypes definition file: <" + fileurl + ">", ex);
        }
        cdtFactories.put(fileurl, null);
        return null;
    }

    private final JSCustomDatatype cdtInterface;
    private final CustomTypedValueCache cache;

    private CustomDatatype(String uri, JSCustomDatatype cdtInterface) {
        super(uri);
        if (uri == null || cdtInterface == null) {
            throw new IllegalArgumentException("Parameters must not be null.");
        }
        this.cache = new CustomTypedValueCache();
        this.cdtInterface = cdtInterface;
    }

    public JSCustomDatatype getInterface() {
        return cdtInterface;
    }

    /**
     * Parse a lexical form of this datatype to a value
     *
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        CustomTypedValue ctv;
        if (USE_CACHE) {
            ctv = cache.getCustomTypedValue(lexicalForm);
            if (ctv == null) {
                ctv = cache.put(lexicalForm);
            }
        } else {
            ctv = new CustomTypedValue(lexicalForm, getURI());
        }
        if (ctv.isLegal()) {
            return ctv;
        }
        throw new DatatypeFormatException("Error while instantiating custom datatype \"" + lexicalForm + "\"^^<" + getURI() + ">.");
    }

    /**
     * Test whether the given string is a legal lexical form of this datatype.
     */
    @Override
    public boolean isValid(String lexicalForm) {
        CustomTypedValue ctv;
        if (USE_CACHE) {
            ctv = cache.getCustomTypedValue(lexicalForm);
            if (ctv == null) {
                ctv = cache.put(lexicalForm);
            }
        } else {
            ctv = new CustomTypedValue(lexicalForm, getURI());
        }
        return ctv.isLegal();
    }

    @Override
    public boolean isValidLiteral(LiteralLabel lit) {
        if (super.isValidLiteral(lit)) {
            return true;
        }
        String datatypeUri = lit.getDatatypeURI();
        if (datatypeUri.equals(getURI())) {
            return isValid(lit.getLexicalForm());
        }
        try {
            ScriptObjectMirror oldMirror = ((CustomTypedValue) getCustomDatatype(lit.getDatatypeURI()).parse(lit.getLexicalForm())).getMirror();
            try {
                if(cdtInterface.importLiteral(oldMirror) != null) {
                    return true;
                }
            } catch (ECMAException ex) {
            }
            try {
                if(oldMirror.callMember("exportTo", getURI()) !=null) {
                    return true;
                }
            } catch (ECMAException ex) {
            }
        } catch (ECMAException ex) {
        }
        return false;
    }

    @Override
    public boolean isEqual(LiteralLabel litLabel1, LiteralLabel litLabel2) {
        if (super.isEqual(litLabel1, litLabel2)) {
            return true;
        }
        if (!litLabel1.getDatatypeURI().equals(getURI())) {
            throw new IllegalArgumentException("Expecting first argument to be of datatype <" + getURI() + ">. Got <" + litLabel1.getDatatypeURI() + ">.");
        }
        try {
            CustomDatatype cdt1 = (CustomDatatype) litLabel1.getDatatype();
            CustomDatatype cdt2 = (CustomDatatype) litLabel2.getDatatype();
            ScriptObjectMirror mirror1 = ((CustomTypedValue) cdt1.parse(litLabel1.getLexicalForm())).getMirror();
            ScriptObjectMirror mirror2 = ((CustomTypedValue) cdt2.parse(litLabel2.getLexicalForm())).getMirror();
            try {
                return (boolean) mirror1.callMember("equals", mirror2);
            } catch(NullPointerException | ECMAException ex) {
                java.util.logging.Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Error while calling member equals.", ex);
            }
            try {
                return (boolean) mirror2.callMember("equals", mirror1);
            } catch(NullPointerException | ECMAException ex) {
                java.util.logging.Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Error while calling member equals.", ex);
            }
        } catch (DatatypeFormatException ex) {
            java.util.logging.Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Error while calling member equals.", ex);
        }
        return false;
    }

    /**
     * Cannonicalise a java Object value to a normal form. Primarily used in
     * cases such as xsd:integer to reduce the Java object representation to the
     * narrowest of the Number subclasses to ensure that indexing of typed
     * literals works.
     */
    @Override
    public Object cannonicalise(Object value) {
        if (!(value instanceof CustomTypedValue)) {
            return null;
        }
        CustomTypedValue ctv = (CustomTypedValue) value;
        try {
            ScriptObjectMirror mirror = (ScriptObjectMirror) ctv.getMirror().callMember("cannonicalise");
            String lexicalForm = (String) mirror.callMember("getLexicalForm");
            return new CustomTypedValue(lexicalForm, getURI(), mirror);
        } catch (NullPointerException | ECMAException ex) {
            Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Error in method cannonicalise for literal \"" + ctv.lexicalValue + "\"^^<" + ctv.datatypeURI + ">", ex);
            return null;
        }
    }

    public class CustomTypedValue extends TypedValue {

        private boolean legalityChecked = false;
        private boolean isLegal = false;

        private boolean isSet = false;
        private ScriptObjectMirror jsMirror = null;

        public CustomTypedValue(String lexicalValue, String datatypeURI) {
            this(lexicalValue, datatypeURI, null);
        }
        
        public CustomTypedValue(String lexicalValue, String datatypeURI, ScriptObjectMirror mirror) {
            super(lexicalValue, datatypeURI);
            if(mirror!=null) {
                this.isSet = true;
                this.jsMirror = mirror;
            }
        }
        
        public boolean isLegal() {
            if (!legalityChecked) {
                legalityChecked = true;
                try {
                    isLegal = cdtInterface.isLegal(lexicalValue);
                } catch (ECMAException ex) {
                    Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Error while evaluating validity of lexical form \"" + lexicalValue + "\" for datatype <" + getURI() + ">.", ex);
                }
            }
            return isLegal;
        }

        public ScriptObjectMirror getMirror() {
            if (isLegal() && !isSet) {
                try {
                    jsMirror = cdtInterface.createLiteral(lexicalValue);
                } catch (ECMAException ex) {
                    Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Error while instantiating custom literal \"" + lexicalValue + "\"^^<" + getURI() + ">", ex);
                }
                if (jsMirror == null) {
                    Logger.getLogger(CustomDatatype.class.getName()).log(Level.WARNING, "Error while instantiating custom literal \"{0}\"^^<{1}> got null.", new Object[]{lexicalValue, getURI()});
                }
                isSet = true;
            }
            return jsMirror;
        }
    }

    private class CustomTypedValueCache {

        private final Queue<String> valid = new LinkedList<>();
        private final Map<String, CustomTypedValue> cachedItems = new HashMap<>();

        public boolean contains(String key) {
            return valid.contains(key);
        }

        public CustomTypedValue getCustomTypedValue(String lexicalForm) {
            return cachedItems.get(lexicalForm);
        }

        public CustomTypedValue put(String lexicalForm) {
            if (valid.size() >= CACHE_LIMIT) {
                String oldest = valid.poll();
                cachedItems.remove(oldest);
            }
            CustomTypedValue customTypedValue = new CustomTypedValue(lexicalForm, getURI());
            cachedItems.put(lexicalForm, customTypedValue);
            return customTypedValue;
        }

    }
}
