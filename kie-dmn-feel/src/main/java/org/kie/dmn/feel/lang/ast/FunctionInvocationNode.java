/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.dmn.feel.lang.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import org.kie.dmn.feel.lang.EvaluationContext;
import org.kie.dmn.feel.lang.runtime.FEELFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionInvocationNode
        extends BaseNode {

    private static final Logger logger = LoggerFactory.getLogger( FunctionInvocationNode.class );

    private BaseNode name;
    private ListNode params;

    public FunctionInvocationNode(ParserRuleContext ctx, BaseNode name, ListNode params) {
        super( ctx );
        this.name = name;
        this.params = params;
    }

    public BaseNode getName() {
        return name;
    }

    public void setName(BaseNode name) {
        this.name = name;
    }

    public ListNode getParams() {
        return params;
    }

    public void setParams(ListNode params) {
        this.params = params;
    }

    @Override
    public Object evaluate(EvaluationContext ctx) {
        FEELFunction function = null;
        Object value = null;
        if ( name instanceof NameRefNode ) {
            // simple name
            value = ctx.getValue( name.getText() );
        } else {
            QualifiedNameNode qn = (QualifiedNameNode) name;
            String[] qns = qn.getPartsAsStringArray();
            value = ctx.getValue( qns );
        }
        if ( value instanceof FEELFunction ) {
            function = (FEELFunction) value;
        }
        if ( function != null ) {
            Object[] p = params.getElements().stream().map( e -> e.evaluate( ctx ) ).toArray( Object[]::new );
            Object result = function.applyReflectively( p );
            return result;
        } else {
            logger.error( "Function not found: '" + name.getText() + "'" );
        }
        return null;
    }
}