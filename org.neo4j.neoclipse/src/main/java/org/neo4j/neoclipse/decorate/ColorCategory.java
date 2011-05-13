/**
 * Licensed to Neo Technology under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo Technology licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.neo4j.neoclipse.decorate;

public enum ColorCategory implements ColorSetting
{
    RELATIONSHIP( 0.8f, 0.7f ),
    NODE_INCOMING( 0.17f, 1.0f ),
    NODE_OUTGOING( 0.08f, 0.95f ),
    RELATIONSHIP_MARKED( 0.8f, 0.5f ),
    NODE_INCOMING_MARKED( 0.3f, 0.7f ),
    NODE_OUTGOING_MARKED( 0.2f, 0.6f );

    private final float saturation;
    private final float brightness;

    ColorCategory( final float saturation, final float brightness )
    {
        this.saturation = saturation;
        this.brightness = brightness;
    }

    @Override
    public float getSaturation()
    {
        return saturation;
    }

    @Override
    public float getBrightness()
    {
        return brightness;
    }
}
