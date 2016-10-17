package nl.weeaboo.vn.test.integration.render;

import org.junit.Test;

import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.styledtext.TextStyle;
import nl.weeaboo.styledtext.layout.ITextLayout;
import nl.weeaboo.styledtext.layout.LayoutParameters;
import nl.weeaboo.styledtext.layout.LayoutUtil;
import nl.weeaboo.vn.core.impl.StaticEnvironment;

public class RenderTextTest extends RenderIntegrationTest {

    @Test
    public void render() {
        ITextLayout layout = createLayout(styledText("--- --- --- --- --- --- --- --- --- --- ---"), 200);

        renderer.startLayer();
        renderer.drawText(0, 0, layout);
        renderer.render();

        checkRenderResult("text");
    }

    private StyledText styledText(String string) {
        return new StyledText(string, new TextStyle(null, 48));
    }

    private ITextLayout createLayout(StyledText text, int wrapWidth) {
        LayoutParameters layoutParams = new LayoutParameters();
        layoutParams.ydir = 1;
        layoutParams.wrapWidth = wrapWidth;
        return LayoutUtil.layout(StaticEnvironment.FONT_STORE.get(), text, layoutParams);
    }

}
