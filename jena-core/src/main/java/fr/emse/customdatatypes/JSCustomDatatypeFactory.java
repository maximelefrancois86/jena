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
public interface JSCustomDatatypeFactory {

    /**
     * Gets the custom datatype with the given URI.
     * 
     * @param uri The URI of the custom datatype defined in this custom datatype
     * definition file
     * @return The Custom Datatype. If not null, MUST conform to the JSCustomDatatype interface.
     * @throws ECMAException If something went wrong.
     */
    ScriptObjectMirror getDatatype(String uri) throws ECMAException;
}
