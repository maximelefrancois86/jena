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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * This class embeds a javascript ScriptEngine, and manages the discovery and
 * mapping of custom datatypes and custom functions.
 *
 * @author maxime.lefrancois
 */
public class LindtEngine {

    private static LindtEngine INSTANCE;

    public static LindtEngine get() throws LindtException {
        if (INSTANCE == null) {
            INSTANCE = new LindtEngine();
        }
        return INSTANCE;
    }

    private final ScriptEngine engine;
    private final Set<String> loaded;
    private final Map<String, LinkedDatatype> datatypes;

    private LindtEngine() throws LindtException {
        ScriptEngineManager factory = new ScriptEngineManager();
        this.engine = factory.getEngineByName("JavaScript");
        load("http://www.maxime-lefrancois.info/lindt/lindt.js");
        loaded = new HashSet<>();
        datatypes = new HashMap<>();
    }

    public LinkedDatatype getDatatype(String uri) throws LindtException {
        if(datatypes.containsKey(uri)) {
            return datatypes.get(uri);
        }
        try {
            engine.put("uri", uri);
            if(engine.eval("lindt.getDatatype(uri)")!=null) {
                LinkedDatatype datatype = new LinkedDatatype(uri, this);
                datatypes.put(uri, datatype);
                return datatype;
            }
        } catch (ScriptException ex) {
            throw new LindtException(ex);
        }
        // if it hasn't been loaded yet, load js file at given uri.
        String fileuri = uri.contains("#") ? uri.substring(0, uri.indexOf("#")) : uri;
        if(!loaded.contains(fileuri)) {
            load(fileuri);
            loaded.add(fileuri);
            return getDatatype(uri);
        }
        return null;
    }

    private void load(String uri) throws LindtException {
        if (uri.startsWith("http://")) {
            try {
                URL dest = new URL(uri);
                HttpURLConnection yc = (HttpURLConnection) dest.openConnection();
                yc.setInstanceFollowRedirects(true);
                yc.setUseCaches(false);
                yc.setRequestProperty("Accept", "application/javascript");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(yc.getInputStream()))) {
                    engine.eval(reader);
                }
            } catch (MalformedURLException ex) {
                throw new LindtException(ex);
            } catch (IOException | ScriptException ex) {
                throw new LindtException(ex);
            }
        } else {
            try {
                FileReader reader = new FileReader(new File(uri));
                engine.eval(reader);
            } catch (FileNotFoundException | ScriptException ex) {
                throw new LindtException(ex);
            }
        }
    }

    public ScriptEngine getEngine() {
        return engine;
    }
    
    

}
