package nl.weeaboo.vn.impl.scene;

import org.junit.Assert;
import org.junit.Test;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.test.RectAssert;
import nl.weeaboo.vn.core.BlendMode;
import nl.weeaboo.vn.core.Direction;
import nl.weeaboo.vn.image.ITexture;
import nl.weeaboo.vn.impl.image.TestTexture;
import nl.weeaboo.vn.impl.image.TextureStub;
import nl.weeaboo.vn.impl.test.CoreTestUtil;

public class ImageDrawableTest {

    private static final double E = CoreTestUtil.EPSILON;

    @Test
    public void transform() {
        int x = -50;
        int y = -50;
        int w = 100;
        int h = 100;

        ImageDrawable image = new ImageDrawable();
        image.setTexture(new TestTexture(w, h));

        // Bounds
        image.setBounds(x, y, w, h);
        RectAssert.assertEquals(Rect2D.of(x, y, w, h), image.getVisualBounds(), E);

        image.setPos(100, 100);
        RectAssert.assertEquals(Rect2D.of(100, 100, w, h), image.getVisualBounds(), E);
        image.setSize(200, 200);
        RectAssert.assertEquals(Rect2D.of(100, 100, 200, 200), image.getVisualBounds(), E);

        image.setBounds(1, 2, 3, 4);
        RectAssert.assertEquals(Rect2D.of(1, 2, 3, 4), image.getVisualBounds(), E);

        image.setX(x);
        image.setY(y);
        image.setWidth(w);
        image.setHeight(h);
        RectAssert.assertEquals(Rect2D.of(x, y, w, h), image.getVisualBounds(), E);

        // Rotated bounds
        image.setRotation(64); // Rotate 1/8th circle clockwise around top-left
        final double diagonal = Math.sqrt(w * w + h * h);
        RectAssert.assertEquals(Rect2D.of(x - diagonal / 2, y, diagonal, diagonal), image.getVisualBounds(), E);

        // Scaled
        image.setRotation(0);
        image.setScale(0.5, 2);
        RectAssert.assertEquals(Rect2D.of(x, y, w * .5, h * 2), image.getVisualBounds(), E);

        // Align
        image.setPos(0, 0);
        image.setScale(1, 1);
        image.setAlign(0.5, 0.5);
        RectAssert.assertEquals(Rect2D.of(x, y, w, h), image.getVisualBounds(), E);
    }

    @Test
    public void drawableColor() {
        ImageDrawable image = CoreTestUtil.newImage();

        // Getters/setters using doubles
        double alpha = 0.35;

        image.setAlpha(alpha);
        Assert.assertEquals(alpha, image.getAlpha(), E);

        image.setColor(.2, .4, .6);
        Assert.assertEquals(.2, image.getRed(), E);
        Assert.assertEquals(.4, image.getGreen(), E);
        Assert.assertEquals(.6, image.getBlue(), E);
        Assert.assertEquals(alpha, image.getAlpha(), E);

        // Getters/setters using ints
        image.setColorRGB(0xA0806040);
        Assert.assertEquals(alpha, image.getAlpha(), E);
        Assert.assertEquals(0x806040, image.getColorRGB());

        image.setColorARGB(0x20406080);
        Assert.assertEquals(0x406080, image.getColorRGB());
        Assert.assertEquals(0x20406080, image.getColorARGB());
    }

    @Test
    public void drawableAttributes() {
        ImageDrawable image = CoreTestUtil.newImage();

        // Z
        Assert.assertEquals(0, image.getZ());
        image.setZ(Short.MAX_VALUE);
        Assert.assertEquals(Short.MAX_VALUE, image.getZ());
        image.setZ(Short.MIN_VALUE);
        Assert.assertEquals(Short.MIN_VALUE, image.getZ());

        // Visible
        image.setAlpha(0.2);
        Assert.assertTrue(image.isVisible());
        Assert.assertTrue(image.isVisible(0.2));
        Assert.assertFalse(image.isVisible(0.3));
        image.setVisible(false);
        Assert.assertFalse(image.isVisible());

        // Clipping
        Assert.assertTrue(image.isClipEnabled());
        image.setClipEnabled(false);
        Assert.assertFalse(image.isClipEnabled());

        // Blend mode
        Assert.assertEquals(BlendMode.DEFAULT, image.getBlendMode());
        image.setBlendMode(BlendMode.ADD);
        Assert.assertEquals(BlendMode.ADD, image.getBlendMode());
    }

    @Test
    public void imageAttributes() {
        ImageDrawable image = CoreTestUtil.newImage();

        final ITexture alpha = new TextureStub(50, 50);
        final ITexture beta = new TextureStub(100, 100);

        image.setTexture(beta);
        Assert.assertEquals(0, image.getX(), E);
        Assert.assertEquals(0, image.getY(), E);

        image.setTexture(alpha, Direction.CENTER);
        RectAssert.assertEquals(Rect2D.of(25, 25, 50, 50), image.getVisualBounds(), E);
    }

}
