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
package fr.emse.customdatatypes;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.runtime.ECMAException;

/**
 *
 * @author Maxime Lefrançois
 */
public interface JSCustomDatatype {

    /**
     * Gets the uri of this datatype.
     *
     * @throws ECMAException
     */
    String getUri() throws ECMAException;

    /**
     * Checks if the given lexical form is in the lexical space of this
     * datatype.
     *
     * @param lexicalForm
     * @return true if the lexical form is in the lexical spece of this
     * datatype.
     * @throws ECMAException
     */
    boolean isLegal(String lexicalForm) throws ECMAException;

    /**
     * Instantiates a new literal with this datatype and the given lexical form.
     *
     * <code>
     * ScriptObjectMirror mirror = jsCustomDatatype.createLiteral(uri);
     * Invocable invocable = (Invocable) jsEngine;
     * JSCustomLiteral jsCustomLiteral = invocable.getInterface(mirror, JSCustomLiteral.class);
     * </code>
     *
     * @param lexicalForm
     * @return The literal. MUST not be null, MUST conform to interface
     * JSCustomLiteral.
     * @throws ECMAException If the lexical form is not in the lexical space of
     * this Datatype, or if something went wrong.
     */
    ScriptObjectMirror createLiteral(String lexicalForm) throws ECMAException;

    /**
     * <p>
     * Checks whether this datatype recognises the datatype with the given uri.
     * Let l1 be a literal with datatype dt1. If a datatype dt1 does not
     * recognise a datatype dt2, then:
     * <ul>
     * <li>this.importLiteral called with a literal of datatype dt2 must throw
     * an exception.</li>
     * <li>l1.equals(lf2,dt2.getUri()) MUST throw an exception.</li>
     * <li>l1.compareTo(lf2,dt2.getUri()) MUST throw an exception.</li>
     * <li>l1.exportTo(dt2.getUri()) MUST throw an exception.</li>
     * </ul>
     * </p>
     *
     * @param lexicalForm
     * @return true if this custom datatype recognises the datatype with the
     * given uri.
     * @throws ECMAException
     */
    boolean recognises(String uri) throws ECMAException;
    
    /**
     * Gets a lexical form of a literal l with this datatype, such that l has
     * the same value as the given literal.
     *
     * @param literal
     * @return The lexical form. MUST not be null.
     * @throws ECMAException If the datatype of this literal does not recognise
     * the datatype of the given literal, or if there exists no literal with
     * this datatype that has the same value as the given literal, or if
     * something went wrong.
     */
    ScriptObjectMirror importLiteral(ScriptObjectMirror literal) throws ECMAException;

}
