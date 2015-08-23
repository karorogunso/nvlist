package nl.weeaboo.vn.script.lua;

import org.luaj.vm2.Varargs;

import nl.weeaboo.entity.Entity;
import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.vn.core.IDrawablePart;
import nl.weeaboo.vn.core.ILayer;
import nl.weeaboo.vn.core.impl.BasicPartRegistry;
import nl.weeaboo.vn.core.impl.DefaultEnvironment;
import nl.weeaboo.vn.image.IImageModule;
import nl.weeaboo.vn.script.ScriptException;
import nl.weeaboo.vn.script.ScriptFunction;
import nl.weeaboo.vn.text.ITextPart;

public class TextLib extends LuaLib {

    private static final long serialVersionUID = 1L;

    private final DefaultEnvironment env;

    public TextLib(DefaultEnvironment env) {
        super("Text");

        this.env = env;
    }

    /**
     * @param args
     *        <ol>
     *        <li>Parent layer
     *        <li>Initial text
     *        </ol>
     */
    @ScriptFunction
    public Varargs createTextDrawable(Varargs args) throws ScriptException {
        ILayer layer = LuaConvertUtil.getLayerArg(args, 1);
        if (layer == null) {
            layer = LuaConvertUtil.getActiveLayer();
        }

        IImageModule imageModule = env.getImageModule();
        BasicPartRegistry pr = env.getPartRegistry();

        Entity e = imageModule.createTextDrawable(layer);

        // Set initial text
        IDrawablePart drawablePart = e.getPart(pr.drawable);
        drawablePart.setBounds(0, 0, layer.getWidth(), layer.getHeight());

        ITextPart textPart = e.getPart(pr.text);
        if (!args.isnil(2)) {
            StyledText stext = args.touserdata(2, StyledText.class);
            if (stext != null) {
                textPart.setText(stext);
            } else {
                textPart.setText(args.tojstring(2));
            }
        }

        return LuaEntity.toUserdata(e, pr);
    }

}