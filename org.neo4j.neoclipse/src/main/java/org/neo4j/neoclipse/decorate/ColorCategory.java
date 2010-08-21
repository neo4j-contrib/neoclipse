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

    public float getSaturation()
    {
        return saturation;
    }

    public float getBrightness()
    {
        return brightness;
    }
}
