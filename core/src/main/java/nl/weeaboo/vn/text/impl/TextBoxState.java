package nl.weeaboo.vn.text.impl;

import static nl.weeaboo.vn.text.impl.TextUtil.toStyledText;

import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.vn.core.NovelPrefs;
import nl.weeaboo.vn.scene.ITextDrawable;
import nl.weeaboo.vn.text.ITextBoxState;
import nl.weeaboo.vn.text.ITextLog;

public class TextBoxState implements ITextBoxState {

    private static final long serialVersionUID = TextImpl.serialVersionUID;

    private double baseTextSpeed;
    private StyledText stext = toStyledText("");
    private ITextLog textLog;
    private ITextDrawable textDrawable;

    public TextBoxState() {
        // TODO: Share a single textlog between all screens
        textLog = new TextLog();

        baseTextSpeed = NovelPrefs.TEXT_SPEED.getDefaultValue();
    }

    @Override
    public void update() {
    }

    protected void onTextSpeedChanged() {
        if (textDrawable != null) {
            textDrawable.setTextSpeed(getTextSpeed());
        }
    }

    @Override
    public ITextLog getTextLog() {
        return textLog;
    }

    @Override
    public ITextDrawable getTextDrawable() {
        return textDrawable;
    }

    @Override
    public double getTextSpeed() {
        return baseTextSpeed;
    }

    @Override
    public void setText(String s) {
        setText(toStyledText(s));
    }

    @Override
    public void setText(StyledText st) {
        stext = st;

        if (textDrawable != null) {
            textDrawable.setVisibleText(0, 0);
        }
        setTextDrawableText(st);
    }

    @Override
    public void appendText(String s) {
        appendText(toStyledText(s));
    }

    @Override
    public void appendText(StyledText st) {
        stext = StyledText.concat(stext, st);
        setTextDrawableText(stext);
    }

    @Override
    public void appendTextLog(String s, boolean newPage) {
        appendTextLog(toStyledText(s), newPage);
    }

    @Override
    public void appendTextLog(StyledText st, boolean newPage) {
        if (newPage) {
            textLog.setText(StyledText.EMPTY_STRING);
        }
        textLog.appendText(st);
    }

    private void setTextDrawableText(StyledText stext) {
        if (textDrawable == null) {
            return;
        }

        int sl = textDrawable.getStartLine();
        double visible = textDrawable.getVisibleText();
        int maxVisible = textDrawable.getVisibleLayout().getGlyphCount();
        if (visible < 0 || visible >= maxVisible) {
            visible = maxVisible;
        }

        textDrawable.setText(stext);
        textDrawable.setVisibleText(sl, visible);
    }

    @Override
    public void setTextDrawable(ITextDrawable td) {
        if (textDrawable != td) {
            textDrawable = td;

            if (textDrawable != null) {
                textDrawable.setText(stext);
                textDrawable.setTextSpeed(getTextSpeed());
            }
        }
    }

    @Override
    public void setTextSpeed(double ts) {
        if (baseTextSpeed != ts) {
            baseTextSpeed = ts;
            onTextSpeedChanged();
        }
    }

}
