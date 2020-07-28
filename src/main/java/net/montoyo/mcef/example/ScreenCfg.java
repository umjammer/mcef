package net.montoyo.mcef.example;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.montoyo.mcef.api.IBrowser;

public class ScreenCfg extends Screen {

    private IBrowser browser;
    private int width = 320;
    private int height = 180;
    private int x = 10;
    private int y = 10;
    private int offsetX = 0;
    private int offsetY = 0;
    private boolean dragging = false;
    private boolean resizing = false;
    private boolean drawSquare = true;

    public ScreenCfg(IBrowser b, String vId) {
        super(new LiteralText("TODO"));
        browser = b;
        if(vId != null)
            b.loadURL("https://www.youtube.com/embed/" + vId + "?autoplay=1");

        b.resize(width, height);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            drawSquare = false;
            ExampleMod.INSTANCE.hudBrowser = this;
            browser.injectMouseMove(-10, -10, 0, true);
            client.openScreen(null);
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int btn) {
        boolean pressed = true;
        int sx = (int) mouseX;
        int sy = (int) (client.getWindow().getScaledHeight() - mouseY);

        if(btn == 1 && pressed && sx >= x && sy >= y && sx < x + width && sy < y + height) {
            browser.injectMouseMove(sx - x, sy - y, 0, false);
            browser.injectMouseButton(sx - x, sy - y, 0, 1, true, 1);
            browser.injectMouseButton(sx - x, sy - y, 0, 1, false, 1);
        } else if(dragging) {
            if(btn == 0 && !pressed)
                dragging = false;
            else {
                x = sx + offsetX;
                y = sy + offsetY;
            }
        } else if(resizing) {
            if(btn == 0 && !pressed) {
                resizing = false;
                browser.resize(width, height);
            } else {
                int w = sx - x;
                int h = sy - y;

                if(w >= 32 && h >= 18) {
                    if(h >= w) {
                        double dw = (h) * (16.0 / 9.0);
                        width = (int) dw;
                        height = h;
                    } else {
                        double dh = (w) * (9.0 / 16.0);
                        width = w;
                        height = (int) dh;
                    }
                }
            }
        } else if(pressed && btn == 0 && sx >= x && sy >= y && sx < x + width && sy < y + height) { //In browser rect
            dragging = true;
            offsetX = x - sx;
            offsetY = y - sy;
        } else if(pressed && btn == 0 && sx >= x + width && sy >= y + height && sx < x + width + 10 && sy < y + height + 10) //In resize rect
            resizing = true;

        return super.mouseClicked(mouseX, mouseY, btn);
    }

    @Override
    public void render(MatrixStack matrixStack, int i1, int i2, float f) {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        browser.draw(unscaleX(x), unscaleY(height + y), unscaleX(width + x), unscaleY(y));

        if(drawSquare) {
            Tessellator t = Tessellator.getInstance();
            BufferBuilder vb = t.getBuffer();

            vb.begin(GL11.GL_LINE_LOOP, VertexFormats.POSITION_COLOR);
            vb.vertex(unscaleX(x + width), unscaleY(y + height), 0.0).color(255, 255, 255, 255).next();
            vb.vertex(unscaleX(x + width + 10), unscaleY(y + height), 0.0).color(255, 255, 255, 255).next();
            vb.vertex(unscaleX(x + width + 10), unscaleY(y + height + 10), 0.0).color(255, 255, 255, 255).next();
            vb.vertex(unscaleX(x + width), unscaleY(y + height + 10), 0.0).color(255, 255, 255, 255).next();
            t.draw();
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public double unscaleX(int x) {
        return x / (double) client.getWindow().getScaledWidth() * super.width;
    }

    public double unscaleY(int y) {
        return y / (double) client.getWindow().getScaledHeight() * super.height;
    }
}
