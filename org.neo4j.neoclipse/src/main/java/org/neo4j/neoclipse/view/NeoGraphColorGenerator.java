/*
 * NeoGraphColorGenerator.java
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
    public NeoGraphColorGenerator( float hue )
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
    public NeoGraphColorGenerator( float hue, float saturation, float brightness )
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
        return new Color( Display.getDefault(), new RGB( limitHue( startHue
            + hue ), saturation, brightness ) );
    }

    /**
     * Limit hues to 0-360.
     * @param hue
     *            the hue to limit
     * @return the limited hue
     */
    private float limitHue( float hue )
    {
        while ( hue >= 360.0f )
        {
            hue -= 360.0f;
        }
        return hue;
    }
}
