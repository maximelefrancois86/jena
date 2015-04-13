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

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.runtime.ECMAException;

/**
 *
 * @author Maxime Lefrançois
 */
public interface JSCustomDatatype {

    String getUri();

    /**
     * Checks if the lexical form is in the lexical space of this Datatype
     *
     * @param lexicalForm
     * @return
     */
    boolean isLegal(String lexicalForm);

    /**
     * Instantiates a new literal with this datatype and the given lexical form.
     * Must not be null, and must enable the obtention of an instance of JSCustomLiteral:
     * <code>
     * ScriptObjectMirror mirror = jsCustomDatatype.createLiteral(uri);
     * Invocable invocable = (Invocable) jsEngine;
     * JSCustomLiteral jsCustomLiteral = invocable.getInterface(mirror, JSCustomLiteral.class);
     * </code>
     *
     * @param lexicalForm
     * @return The literal. Must conform to interface JSCustomLiteral.
     * @throws ECMAException If the lexical form is not in the lexical space of
     * this Datatype
     */
    ScriptObjectMirror createLiteral(String lexicalForm) throws ECMAException;

    ScriptObjectMirror importLiteral(ScriptObjectMirror literal);
}
