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
package org.neo4j.neoclipse.view;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Tool that creates colors that differ as much as possible regarding hue.
 * @author Anders Nawroth
 */
public class NeoGraphColorGenerator
{
    /**
     * Saturation of output colors.
     */
    private float saturation = 0.9f;
    /**
     * Brightness of output colors.
     */
    private float brightness = 0.8f;
    /**
     * Default first color to use.
     */
    private float startHue = 60.0f;
    /**
     * Current color (not corrected for the start hue).
     */
    private float hue = 0;
    /**
     * Amount to move from one color to the next.
     */
    private float moveAngle = 180.0f;
    /**
     * At what insertion point to start the next series of colors.
     */
    private float startAngle = 180.0f;
    /**
     * Number of steps rendering current series of colors.
     */
    private int totalSteps = 1;
    /**
     * Counter for the step in the current series of colors was created. The
     * value -2 makes sure the first series is created correctly.
     */
    private int currentStep = -2;

    /**
     * Common constructor.
     */
    public NeoGraphColorGenerator()
    {
        // Correct for the addition of 180 in the first call of next().
        startHue = limitHue( startHue + 180.0f );
    }

    /**
     * Construct the color generator with a specified start hue.
     * @param hue
     *            hue of the first color generated
     */
    public NeoGraphColorGenerator( final float hue )
    {
        startHue = limitHue( hue + 180.0f );
    }

    /**
     * Construct the color generator with a specified start hue, saturation and
     * brightness.
     * @param hue
     *            hue of the first color generated
     * @param saturation
     *            the saturation of generated colors
     * @param brightness
     *            the brightness of generated colors
     */
    public NeoGraphColorGenerator( final float hue, final float saturation,
        final float brightness )
    {
        this( hue );
        this.saturation = saturation;
        this.brightness = brightness;
    }

    /**
     * Provides a series of well distributed colors.
     * @return next color to use
     */
    public Color next()
    {
        return new Color( Display.getDefault(), new RGB( nextHue(), saturation,
            brightness ) );
    }

    public float nextHue()
    {
        currentStep++;
        if ( currentStep >= totalSteps )
        {
            totalSteps *= 2;
            currentStep = 0;
            moveAngle = startAngle;
            startAngle /= 2.0f;
            hue = startAngle;
        }
        hue = limitHue( hue + moveAngle );
        return limitHue( startHue + hue );
    }

    /**
     * Limit hues to 0-360.
     * @param hue
     *            the hue to limit
     * @return the limited hue
     */
    private float limitHue( final float hue )
    {
        float newHue = hue;
        while ( newHue >= 360.0f )
        {
            newHue -= 360.0f;
        }
        return newHue;
    }
}
