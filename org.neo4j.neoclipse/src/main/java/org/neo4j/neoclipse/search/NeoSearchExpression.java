/*
 * Licensed to "Neo Technology," Network Engine for Objects in Lund AB
 * (http://neotechnology.com) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at (http://www.apache.org/licenses/LICENSE-2.0). Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.neo4j.neoclipse.search;

import java.util.regex.Pattern;

/**
 * This class is a Neo search expression that is based on Java regular expressions.
 * 
 * @author	Peter H&auml;nsgen
 */
public class NeoSearchExpression
{
    /**
     * The compiled regular expression.
     */
    private Pattern pattern;

    /**
     * The constructor.
     */
    public NeoSearchExpression(Pattern p)
    {
        this.pattern = p;
    }

    /**
     * Returns true, if the given value represents a match.
     */
    public boolean matches(Object value)
    {
        String v = String.valueOf(value);
        
        return pattern.matcher(v).matches();
    }

    /**
     * Returns the search expression string, e.g. the regular expression.
     */
    public String getExpression()
    {
        return pattern.pattern();
    }
}
