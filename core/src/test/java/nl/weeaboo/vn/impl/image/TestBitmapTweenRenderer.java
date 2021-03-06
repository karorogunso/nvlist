package nl.weeaboo.vn.impl.image;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.vn.image.IImageModule;
import nl.weeaboo.vn.image.ITexture;
import nl.weeaboo.vn.impl.image.BitmapTweenConfig;
import nl.weeaboo.vn.impl.image.BitmapTweenRenderer;
import nl.weeaboo.vn.render.IDrawBuffer;
import nl.weeaboo.vn.scene.IDrawable;

public class TestBitmapTweenRenderer extends BitmapTweenRenderer {

    private static final long serialVersionUID = 1L;

    public TestBitmapTweenRenderer(IImageModule imageModule, BitmapTweenConfig config) {
        super(imageModule, config);
    }

    @Override
    protected void renderIntermediate(IDrawBuffer drawBuffer, IDrawable parent, Area2D bounds) {
    }

    @Override
    protected ITexture updateRemapTexture(ITexture remapTexture) {
        return remapTexture;
    }

}
