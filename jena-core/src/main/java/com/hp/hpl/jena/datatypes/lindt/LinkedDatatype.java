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
import com.hp.hpl.jena.datatypes.TypeMapper;
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

/**
 *
 * @author maxime.lefrancois
 */
public class LinkedDatatype extends BaseDatatype {
    
    private static final ScriptEngineManager factory = new ScriptEngineManager();
    private static final ScriptEngine engine = factory.getEngineByName("JavaScript");

    /** Map of already discovered datatypes */
    public static final Map<String, LinkedDatatype> datatypes = new HashMap<>();

    public LinkedDatatype(String uri) {
        super(uri);
        
    }
    
    public static void attemptDiscovery(String uri, TypeMapper tm) {
        try {
            URL dest = new URL(uri);
            HttpURLConnection yc = (HttpURLConnection) dest.openConnection();
            yc.setInstanceFollowRedirects(true);
            yc.setUseCaches(false);
            yc.setRequestProperty("Accept", "application/javascript");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(yc.getInputStream()))) {
                engine.eval(br); // security and stability concerns !
            }
            engine.eval("get "); // security and stability concerns !
        } catch (MalformedURLException ex) {
            Logger.getLogger(LinkedDatatype.class.getName()).log(Level.INFO, null, ex);
        } catch (IOException | ScriptException ex) {
            Logger.getLogger(LinkedDatatype.class.getName()).log(Level.INFO, null, ex);
        }
        
// for each datatype that has been discovered
//            tm.registerDatatype(dt);
        
// for each datatype that has been discovered            
//            tm.registerDatatype(dt);

    }
    
    public static void main(String[] args) throws NoSuchMethodException {
        try {
            engine.eval("function load() {\n" +
"	res = {};\n" +
"	res[\"http://ex.org/dt1\"] = { \n" +
"		uri:\"http://ex.org/dt1\",\n" +
"		isValid: function(lexicalForm) {\n" +
"			return /^\\(\\s*([0-9]+)\\s*,\\s*([0-9]+)\\s*\\)$/.test(lexicalForm);\n" +
"		}\n" +
"	};\n" +
"	return res;\n" +
"}");
            Invocable e = (Invocable) engine;
            Object o = e.invokeFunction("load");
            System.out.println(o);
        } catch (ScriptException ex) {
            Logger.getLogger(LinkedDatatype.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
