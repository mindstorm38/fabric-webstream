package fr.theorozier.webstreamer.display.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.ResourceManager;
import org.bytedeco.javacv.Frame;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

import java.nio.ByteBuffer;

@Environment(EnvType.CLIENT)
public class DisplayTexture extends AbstractTexture {

    private int width = -1, height = -1;
    private int format = -1;

    public DisplayTexture() {
        GlStateManager._bindTexture(this.getGlId());
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, 0);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, 0);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0F);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP);
    }
    
    private void uploadBind(int internalFormat, int width, int height, int dataWidth, int alignment) {
    
        GlStateManager._bindTexture(this.getGlId());
        
        if (this.width != width || this.height != height || this.format != internalFormat) {
            GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, width, height, 0, GL12.GL_BGR, GL11.GL_UNSIGNED_BYTE, null);
            this.width = width;
            this.height = height;
            this.format = internalFormat;
        }
    
        GlStateManager._pixelStore(GL11.GL_UNPACK_ALIGNMENT, alignment);
        GlStateManager._pixelStore(GL11.GL_UNPACK_ROW_LENGTH, dataWidth);
        GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_ROWS, 0);
        GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_PIXELS, 0);
    
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        
    }

    public void uploadRaw(ByteBuffer data, int internalFormat, int width, int height, int dataWidth, int dataFormat, int alignment) {
        RenderSystem.assertOnRenderThread();
        this.uploadBind(internalFormat, width, height, dataWidth, alignment);
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, dataFormat, GL11.GL_UNSIGNED_BYTE, data);
    }
    
    public void upload(Frame frame) {
        if (frame.imageDepth == Frame.DEPTH_UBYTE && frame.imageChannels == 3) {
            ByteBuffer data = (ByteBuffer) frame.image[0];
            this.uploadRaw(data, GL11.GL_RGB8, frame.imageWidth, frame.imageHeight, frame.imageStride / 3, GL12.GL_BGR, 4);
        }
    }

    @Override
    public void load(ResourceManager manager) { }

}
