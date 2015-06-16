package nl.weeaboo.vn.image.impl;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.common.Checks;
import nl.weeaboo.gdx.res.IResource;
import nl.weeaboo.vn.image.ITexture;
import nl.weeaboo.vn.render.RenderUtil;

public class TextureAdapter implements ITexture {

	private static final long serialVersionUID = ImageImpl.serialVersionUID;

	private IResource<TextureRegion> res;
	private double scaleX, scaleY;

	public TextureAdapter(IResource<TextureRegion> region, double scaleX, double scaleY) {
	    this.res = Checks.checkNotNull(region);
	    this.scaleX = scaleX;
	    this.scaleY = scaleY;
    }

	@Override
	public String toString() {
		return "TextureAdapter(" + getHandle() + ")";
	}

	protected int getHandle() {
	    return getTexture().getTextureObjectHandle();
	}
	
	public Texture getTexture() {
	    TextureRegion tr = getTextureRegion();
	    if (tr == null) {
	        return null;
	    }
		return tr.getTexture();
	}

	public TextureRegion getTextureRegion() {
		return res.get();
	}

    public TextureRegion getTextureRegion(Area2D uv) {
        uv = RenderUtil.combineUV(getUV(), uv);
        float u = (float)uv.x;
        float u2 = u + (float)uv.w;
        float v = (float)uv.y;
        float v2 = v + (float)uv.h;
        return new TextureRegion(getTexture(), u, v, u2, v2);
    }
	
	@Override
	public Area2D getUV() {
        TextureRegion tr = getTextureRegion();
        if (tr == null) {
            return ITexture.DEFAULT_UV;
        }
        
        float u1 = tr.getU();
        float u2 = tr.getU2();
        float v1 = tr.getV();
        float v2 = tr.getV2();
		return Area2D.of(u1, v1, u2-u1, v2-v1);
	}

	@Override
	public double getWidth() {
       TextureRegion tr = getTextureRegion();
        if (tr == null) {
            return 0.0;
        }
		return tr.getRegionWidth() * scaleX;
	}

	@Override
	public double getHeight() {
       TextureRegion tr = getTextureRegion();
        if (tr == null) {
            return 0.0;
        }
		return tr.getRegionHeight() * scaleY;
	}

	@Override
	public double getScaleX() {
		return scaleX;
	}

	@Override
	public double getScaleY() {
		return scaleY;
	}

	public void setTextureRegion(IResource<TextureRegion> tr, double scale) {
        setTextureRegion(tr, scale, scale);
    }
	public void setTextureRegion(IResource<TextureRegion> tr, double sx, double sy) {
		this.res = Checks.checkNotNull(tr);
		this.scaleX = sx;
		this.scaleY = sy;
	}

}