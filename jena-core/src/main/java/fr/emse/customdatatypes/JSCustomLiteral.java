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
public interface JSCustomLiteral {

    /**
     * Gets the lexical form of this literal.
     *
     * @return The lexical form.
     * @throws ECMAException
     */
    String getLexicalForm() throws ECMAException;

    /**
     * Gets the datatype uri of this literal
     *
     * @return The uri of the custom datatype.
     * @throws ECMAException
     */
    String getDatatypeUri() throws ECMAException;

    /**
     * Gets the lexical form of the cannonic literal with the same datatype and
     * the same value as this literal.
     *
     * @return The canonic literal.
     * @throws ECMAException Must never be thrown.
     */
    ScriptObjectMirror cannonicalise() throws ECMAException;

    /**
     * Checks whether this literal has the same value as the given literal.
     *
     * @param literal
     * @return
     * @throws ECMAException If the custom datatype of this literal does not
     * recognise the custom datatype of the given literal, or if the given
     * literal is not valid.
     */
    boolean equals(ScriptObjectMirror literal) throws ECMAException;

    /**
     * Compares this literal with the given literal.
     *
     * @param literal
     * @return -1, 0, or 1 depending on if this literal is lower, greater, or
     * equal to the given literal.
     * @throws ECMAException If the custom datatype of this literal does not
     * recognise the custom datatype of the given literal, or if the two
     * literals are not comparable
     */
    int compareTo(ScriptObjectMirror literal) throws ECMAException;

    /**
     * Gives the lexical form of a literal with the given datatype, and a value
     * equal to that of this literal.
     *
     * @param datatypeUri
     * @return -1, 0, or 1 depending on if this literal is lower, greater, or
     * equal to the given literal.
     * @throws ECMAException If the custom datatype of this literal does not
     * recognise the given datatype
     */
    String exportTo(String datatypeUri);
}
