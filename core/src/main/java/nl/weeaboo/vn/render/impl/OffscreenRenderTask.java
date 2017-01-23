package nl.weeaboo.vn.render.impl;

import java.io.IOException;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.google.common.base.Stopwatch;
import com.google.common.math.DoubleMath;

import nl.weeaboo.common.Dim;
import nl.weeaboo.common.Insets2D;
import nl.weeaboo.common.Rect;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.gdx.graphics.GLBlendMode;
import nl.weeaboo.gdx.graphics.GdxViewportUtil;
import nl.weeaboo.gdx.res.DisposeUtil;
import nl.weeaboo.vn.image.IImageModule;
import nl.weeaboo.vn.image.ITexture;
import nl.weeaboo.vn.image.impl.PixelTextureData;
import nl.weeaboo.vn.math.Vec2;
import nl.weeaboo.vn.render.IOffscreenRenderTask;
import nl.weeaboo.vn.render.RenderUtil;

/**
 * Renders to an offscreen buffer (FBO) and reads the result back to a texture.
 */
public abstract class OffscreenRenderTask extends AsyncRenderTask implements IOffscreenRenderTask {

    private static final long serialVersionUID = RenderImpl.serialVersionUID;
    private static final Logger LOG = LoggerFactory.getLogger(OffscreenRenderTask.class);

    private final IImageModule imageModule;
    private Dim size;
    private Insets2D padding = Insets2D.EMPTY;

    private transient PixelTextureData resultPixels;
    private Vec2 resultScale = new Vec2();

    public OffscreenRenderTask(IImageModule imageModule, Dim size) {
        this.imageModule = imageModule;
        this.size = size;

        isTransient = true;
    }

    public OffscreenRenderTask(IImageModule imageModule, ITexture tex) {
        this(imageModule, Dim.of(tex.getPixelWidth(), tex.getPixelHeight()));

        resultScale.x = tex.getScaleX();
        resultScale.y = tex.getScaleY();
    }

    protected void scale(double s) {
        int newWidth = roundToInt(size.w * s);
        int newHeight = roundToInt(size.h * s);

        setSize(Dim.of(newWidth, newHeight));
        setResultScale(resultScale.x / s, resultScale.y / s);
    }

    private static int roundToInt(double s) {
        return DoubleMath.roundToInt(s, RoundingMode.HALF_EVEN);
    }

    @Override
    public void cancel() {
        super.cancel();

        resultPixels = null;
    }

    @Override
    public boolean isAvailable() {
        return !isFailed() && resultPixels != null;
    }

    @Override
    public ITexture getResult() {
        if (resultPixels == null) {
            return null;
        }
        return imageModule.createTexture(resultPixels, resultScale.x, resultScale.y);
    }

    @Override
    public final void render() {
        Stopwatch sw = Stopwatch.createStarted();

        Rect innerRect = RenderUtil.roundClipRect(Rect2D.of(padding.left, padding.top,
                size.w - padding.getHorizontal(), size.h - padding.getVertical()));

        RenderContext renderContext = new RenderContext(size, innerRect);
        try {
            renderContext.prepare();

            Pixmap pixels = render(renderContext);

            resultPixels = PixelTextureData.fromPremultipliedPixmap(pixels);
        } catch (Exception e) {
            LOG.error("Error in offscreen render task: {}", this, e);
        } finally {
            renderContext.dispose();
        }

        LOG.debug("Offscreen render task {} took {}", this, sw);
    }

    protected abstract Pixmap render(RenderContext context) throws IOException;

    protected void setSize(Dim size) {
        this.size = size;
    }

    protected void setResultScale(double sx, double sy) {
        resultScale.x = sx;
        resultScale.y = sy;
    }

    protected void setPadding(Insets2D newPadding, boolean adjustSize) {
        final Insets2D oldPadding = padding;

        padding = newPadding;

        if (adjustSize) {
            int dw = roundToInt(newPadding.getHorizontal() - oldPadding.getHorizontal());
            int dh = roundToInt(newPadding.getVertical() - oldPadding.getVertical());
            size = Dim.of(size.w + dw, size.h + dh);
        }
    }

    protected static final class RenderContext implements Disposable {

        public final Dim outerSize;
        public final Rect innerRect;
        public final SpriteBatch batch;

        protected RenderContext(Dim outerSize, Rect innerRect) {
            this.outerSize = outerSize;
            this.innerRect = innerRect;

            batch = new SpriteBatch();
        }

        public void prepare() {
            ScreenViewport viewport = new ScreenViewport();
            GdxViewportUtil.setToOrtho(viewport, outerSize, true);
            viewport.update(outerSize.w, outerSize.h, false);

            batch.setProjectionMatrix(viewport.getCamera().combined);

            GLBlendMode.DEFAULT_PREMULT.apply(batch);
        }

        @Override
        public void dispose() {
            DisposeUtil.dispose(batch);
        }

        public void draw(TextureRegion region, ShaderProgram shader) {
            batch.setShader(shader);
            batch.begin();
            batch.draw(region, 0, 0, outerSize.w, outerSize.h);
            batch.end();
        }

        public void drawInitial(TextureRegion region) {
            batch.setShader(null);
            batch.begin();
            batch.draw(region, innerRect.x, innerRect.y, innerRect.w, innerRect.h);
            batch.end();
        }

    }

}
