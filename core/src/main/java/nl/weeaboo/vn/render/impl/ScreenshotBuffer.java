package nl.weeaboo.vn.render.impl;

import java.io.Serializable;
import java.util.Collection;

import com.google.common.collect.Lists;

import nl.weeaboo.vn.image.IScreenshotBuffer;
import nl.weeaboo.vn.image.IWritableScreenshot;
import nl.weeaboo.vn.render.IDrawBuffer;

public class ScreenshotBuffer implements IScreenshotBuffer {

    private static final long serialVersionUID = RenderImpl.serialVersionUID;

    private final Collection<ScreenshotEntry> screenshots = Lists.newArrayList();

    @Override
    public void add(IWritableScreenshot ss, boolean clip) {
        screenshots.add(new ScreenshotEntry(ss, clip));
    }

    public void flush(IDrawBuffer d) {
        for (ScreenshotEntry entry : screenshots) {
            d.screenshot(entry.screenshot, entry.clip);
        }
        screenshots.clear();
    }

    @Override
    public boolean isEmpty() {
        return screenshots.isEmpty();
    }

    private static class ScreenshotEntry implements Serializable {

        private static final long serialVersionUID = 1L;

        final IWritableScreenshot screenshot;
        final boolean clip;

        public ScreenshotEntry(IWritableScreenshot ss, boolean clip) {
            this.screenshot = ss;
            this.clip = clip;
        }

    }

}
