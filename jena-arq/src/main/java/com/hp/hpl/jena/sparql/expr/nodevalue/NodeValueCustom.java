/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hp.hpl.jena.sparql.expr.nodevalue;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.lindt.LinkedDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueVisitor;
import javax.xml.datatype.Duration;

/**
 *
 * @author maxime.lefrancois
 */
public class NodeValueCustom extends NodeValue {

    private final String lexicalForm;
    private final LinkedDatatype datatype;

    public NodeValueCustom(String lexicalForm, LinkedDatatype datatype) {
        this.lexicalForm = lexicalForm;
        this.datatype = datatype;
    }

    public NodeValueCustom(String lexicalForm, LinkedDatatype datatype, Node n) {
        super(n);
        this.lexicalForm = lexicalForm;
        this.datatype = datatype;
    }

    @Override
    public boolean isCustom() {
        return true;
    }
    
    public RDFDatatype getDatatype()  {
        return datatype;
    }

    public String getLexicalForm() {
        return lexicalForm;
    }
    
    @Override
    protected Node makeNode() {
        return NodeFactory.createLiteral(lexicalForm, datatype);
    }

    @Override
    public void visit(NodeValueVisitor visitor) {
        visitor.visit(this);
    }
    
    
    
}
