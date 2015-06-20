package nl.weeaboo.vn.image.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import nl.weeaboo.common.Dim;
import nl.weeaboo.entity.Scene;
import nl.weeaboo.entity.World;
import nl.weeaboo.vn.NvlTestUtil;
import nl.weeaboo.vn.core.ILayer;
import nl.weeaboo.vn.core.impl.BasicPartRegistry;
import nl.weeaboo.vn.core.impl.Screen;
import nl.weeaboo.vn.image.IScreenshotBuffer;
import nl.weeaboo.vn.image.ITextureData;
import nl.weeaboo.vn.image.impl.PixelTextureData;
import nl.weeaboo.vn.image.impl.PixmapDecodingScreenshot;
import nl.weeaboo.vn.render.impl.DrawBuffer;
import nl.weeaboo.vn.render.impl.LayerRenderCommand;
import nl.weeaboo.vn.render.impl.RenderCommand;
import nl.weeaboo.vn.render.impl.ScreenshotRenderCommand;
import nl.weeaboo.vn.render.impl.WritableScreenshot;

public class ScreenshotTest {

	@Test
	public void stateTransitions() {
		WritableScreenshot s = new WritableScreenshot((short)0, false);
		Assert.assertFalse(s.isAvailable());
		Assert.assertFalse(s.isCancelled());
		Assert.assertFalse(s.isTransient());
		Assert.assertFalse(s.isVolatile());

		s.markTransient();
		Assert.assertTrue(s.isTransient());
		Assert.assertFalse(s.isVolatile());

		s = new WritableScreenshot((short)0, true);
		Assert.assertTrue(s.isTransient());
		Assert.assertTrue(s.isVolatile());
	}

	@Test
	public void writeScreenshot() {
		int w = 10;
		int h = 10;
		PixelTextureData pixels = TestImageUtil.newTestTextureData(w, h);

		WritableScreenshot s = new WritableScreenshot((short)0, false);
		s.setPixels(pixels, new Dim(w, h));
		TestImageUtil.assertEquals(pixels, s.getPixels());

		s = new WritableScreenshot((short)0, true);
		s.setPixels(pixels, new Dim(w, h));
        TestImageUtil.assertEquals(pixels, s.getPixels());
	}

	@Test
	public void decodingScreenshot() throws IOException {
		int w = 10;
		int h = 10;
		PixelTextureData pixels = TestImageUtil.newTestTextureData(w, h);

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		TestImageUtil.writePng(pixels.getPixels(), bout);

		PixmapDecodingScreenshot ds = new PixmapDecodingScreenshot(bout.toByteArray());
		ITextureData decodedPixels = ds.getPixels();
        TestImageUtil.assertEquals(pixels, decodedPixels);
		Assert.assertEquals(w, decodedPixels.getWidth());
		Assert.assertEquals(h, decodedPixels.getHeight());
	}

	@Test
	public void screenshotBuffer() {
		BasicPartRegistry pr = new BasicPartRegistry();
		World world = new World(pr);
		Scene scene = world.createScene();

        Screen screen = NvlTestUtil.newScreen(pr, scene);
		ILayer root = screen.getRootLayer();

		WritableScreenshot s = new WritableScreenshot((short)0, false);

		IScreenshotBuffer ssb = root.getScreenshotBuffer();
		ssb.add(s, false);

		DrawBuffer buf = new DrawBuffer(pr);
		screen.draw(buf);
		Assert.assertTrue(ssb.isEmpty()); // Screenshot buffer empties into the draw buffer

		LayerRenderCommand lrc = buf.getRootLayerCommand();
		int cstart = buf.getLayerStart(lrc.layerId);
		int cend = buf.getLayerEnd(lrc.layerId);
		Assert.assertEquals(cend-cstart, 1);

		RenderCommand[] cmds = buf.sortCommands(cstart, cend);
		ScreenshotRenderCommand src = (ScreenshotRenderCommand)cmds[cstart];

		// Assert that the correct ScreenshotRenderCommand has been added to the render commands
		Assert.assertEquals(s.getZ(), src.z);
		Assert.assertSame(s, src.ss);
	}

}