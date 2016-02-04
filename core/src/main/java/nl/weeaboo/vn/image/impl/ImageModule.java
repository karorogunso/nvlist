package nl.weeaboo.vn.image.impl;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import nl.weeaboo.common.Checks;
import nl.weeaboo.common.Dim;
import nl.weeaboo.gdx.res.IResource;
import nl.weeaboo.vn.core.IEnvironment;
import nl.weeaboo.vn.core.IRenderEnv;
import nl.weeaboo.vn.core.ResourceLoadInfo;
import nl.weeaboo.vn.core.impl.DefaultEnvironment;
import nl.weeaboo.vn.core.impl.FileResourceLoader;
import nl.weeaboo.vn.image.IImageModule;
import nl.weeaboo.vn.image.IScreenshot;
import nl.weeaboo.vn.image.ITexture;
import nl.weeaboo.vn.image.ITextureData;
import nl.weeaboo.vn.image.IWritableScreenshot;
import nl.weeaboo.vn.scene.IButton;
import nl.weeaboo.vn.scene.IImageDrawable;
import nl.weeaboo.vn.scene.ILayer;
import nl.weeaboo.vn.scene.ITextDrawable;
import nl.weeaboo.vn.scene.impl.EntityHelper;
import nl.weeaboo.vn.script.IScriptContext;

public class ImageModule implements IImageModule {

    private static final long serialVersionUID = ImageImpl.serialVersionUID;
    private static final Logger LOG = LoggerFactory.getLogger(ImageModule.class);

    protected final IEnvironment env;
    protected final FileResourceLoader resourceLoader;
    protected final EntityHelper entityHelper;

    private final TextureManager texManager;

    private Dim imageResolution;

    public ImageModule(DefaultEnvironment env) {
        this(env, new ImageResourceLoader(env), new TextureManager());
    }

    public ImageModule(DefaultEnvironment env, FileResourceLoader resourceLoader, TextureManager texManager) {
        this.env = env;
        this.resourceLoader = resourceLoader;
        this.entityHelper = new EntityHelper();

        this.texManager = texManager;

        IRenderEnv renderEnv = env.getRenderEnv();
        imageResolution = renderEnv.getVirtualSize();
    }

    @Override
    public void destroy() {
    }

    @Override
    public void update() {
    }

    @Override
    public IImageDrawable createImage(ILayer layer) {
        return entityHelper.createImage(layer);
    }

    @Override
    public ITextDrawable createTextDrawable(ILayer layer) {
        return entityHelper.createText(layer);
    }

    @Override
    public IButton createButton(ILayer layer, IScriptContext scriptContext) {
        return entityHelper.createButton(layer, scriptContext);
    }

    @Override
    public ITexture getTexture(String filename) {
        return getTexture(new ResourceLoadInfo(filename), false);
    }

    @Override
    public ITexture getTexture(ResourceLoadInfo loadInfo, boolean suppressErrors) {
        String filename = loadInfo.getFilename();
        resourceLoader.checkRedundantFileExt(filename);

        String normalized = resourceLoader.normalizeFilename(filename);
        if (normalized == null) {
            if (!suppressErrors) {
                LOG.debug("Unable to find image file: " + filename);
            }
            return null;
        }

        return getTextureNormalized(normalized, loadInfo);
    }

    /**
     * Is called from {@link #getTexture(ResourceLoadInfo, boolean)}
     */
    protected ITexture getTextureNormalized(String filename, ResourceLoadInfo loadInfo) {
        IResource<TextureRegion> tr = getTexRectNormalized(filename, loadInfo);

        double scale = getImageScale();
        return texManager.newTexture(tr, scale, scale);
    }

    private IResource<TextureRegion> getTexRectNormalized(String filename, ResourceLoadInfo loadInfo) {
        resourceLoader.logLoad(loadInfo);

        return texManager.getTexture(resourceLoader, filename);
    }

    @Override
    public ITexture createTexture(ITextureData texData, double sx, double sy) {
        return texManager.generateTexture((PixelTextureData)texData, sx, sy);
    }

    @Override
    public ITexture createTexture(IScreenshot ss) {
        IRenderEnv renderEnv = env.getRenderEnv();
        double sx = renderEnv.getWidth() / (double)ss.getScreenSize().w;
        double sy = renderEnv.getHeight() / (double)ss.getScreenSize().h;
        ITextureData pixels = ss.getPixels();
        return createTexture(pixels, sx, sy);
    }

    @Override
    public IScreenshot screenshot(ILayer layer, short z, boolean isVolatile, boolean clipEnabled) {
        IWritableScreenshot ss = new WritableScreenshot(z, isVolatile);
        layer.getScreenshotBuffer().add(ss, clipEnabled);
        return ss;
    }

    protected void onImageScaleChanged() {

    }

    @Override
    public Collection<String> getImageFiles(String folder) {
        return resourceLoader.getMediaFiles(folder);
    }

    protected double getImageScale() {
        IRenderEnv renderEnv = env.getRenderEnv();
        return Math.min(renderEnv.getWidth() / (double)imageResolution.w,
                renderEnv.getHeight() / (double)imageResolution.h);
    }

    @Override
    public void setImageResolution(Dim size) {
        size = Checks.checkNotNull(size);
        if (!imageResolution.equals(size)) {
            imageResolution = size;
            onImageScaleChanged();
        }
    }

}
