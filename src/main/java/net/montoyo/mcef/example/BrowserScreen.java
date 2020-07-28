package net.montoyo.mcef.example;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.montoyo.mcef.MCEF;
import net.montoyo.mcef.api.API;
import net.montoyo.mcef.api.IBrowser;
import net.montoyo.mcef.api.MCEFApi;

public class BrowserScreen extends Screen {
    
    IBrowser browser = null;
    private ButtonWidget back = null;
    private ButtonWidget fwd = null;
    private ButtonWidget go = null;
    private ButtonWidget min = null;
    private ButtonWidget vidMode = null;
    private TextFieldWidget url = null;
    private String urlToLoad = null;

    private static final String YT_REGEX1 = "^https?://(?:www\\.)?youtube\\.com/watch\\?v=([a-zA-Z0-9_\\-]+)$";
    private static final String YT_REGEX2 = "^https?://(?:www\\.)?youtu\\.be/([a-zA-Z0-9_\\-]+)$";
    private static final String YT_REGEX3 = "^https?://(?:www\\.)?youtube\\.com/embed/([a-zA-Z0-9_\\-]+)(\\?.+)?$";

    public BrowserScreen() {
        super(new LiteralText("TODO"));
        urlToLoad = MCEF.HOME_PAGE;
    }

    public BrowserScreen(String url) {
        super(new LiteralText("TODO"));
        urlToLoad = (url == null) ? MCEF.HOME_PAGE : url;
    }
    
    @Override
    public void init() {
        TextRenderer fontRenderer = client.textRenderer;

        ExampleMod.INSTANCE.hudBrowser = null;

        if(browser == null) {
            //Grab the API and make sure it isn't null.
            API api = MCEFApi.getAPI();
            if(api == null)
                return;
            
            //Create a browser and resize it to fit the screen
            browser = api.createBrowser((urlToLoad == null) ? MCEF.HOME_PAGE : urlToLoad, false);
            urlToLoad = null;
        }
        
        //Resize the browser if window size changed
        if(browser != null)
            browser.resize(client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight() - scaleY(20));
        
        //Create GUI
//        Keyboard.enableRepeatEvents(true);
        buttons.clear();
        
        if(url == null) {
            buttons.add(back = (new ButtonWidget(0, 0, 20, 20, new LiteralText("<"), a -> browser.goBack())));
            buttons.add(fwd = (new ButtonWidget(20, 0, 20, 20, new LiteralText(">"), a -> browser.goForward())));
            buttons.add(go = (new ButtonWidget(width - 60, 0, 20, 20, new LiteralText("Go"), a -> browser.loadURL(url.getText()))));
            buttons.add(min = (new ButtonWidget(width - 20, 0, 20, 20, new LiteralText("_"), a-> {
                ExampleMod.INSTANCE.setBackup(this);
                client.openScreen(null);
            })));
            buttons.add(vidMode = (new ButtonWidget(width - 40, 0, 20, 20, new LiteralText("YT"), a -> {
                String loc = browser.getURL();
                String vId = null;
                boolean redo = false;

                if(loc.matches(YT_REGEX1))
                    vId = loc.replaceFirst(YT_REGEX1, "$1");
                else if(loc.matches(YT_REGEX2))
                    vId = loc.replaceFirst(YT_REGEX2, "$1");
                else if(loc.matches(YT_REGEX3))
                    redo = true;

                if(vId != null || redo) {
                    ExampleMod.INSTANCE.setBackup(this);
                    client.openScreen(new ScreenCfg(browser, vId));
                }
            })));
            vidMode.active = false;
            
            url = new TextFieldWidget(fontRenderer, 40, 0, width - 100, 20, new LiteralText("")); // 4
            url.setMaxLength(65535);
            //url.setText("mod://mcef/home.html");
        } else {
            buttons.add(back);
            buttons.add(fwd);
            buttons.add(go);
            buttons.add(min);
            buttons.add(vidMode);
            
            //Handle resizing
            vidMode.x = width - 40;
            go.x = width - 60;
            min.x = width - 20;
            
            String old = url.getText();
            url = new TextFieldWidget(fontRenderer, 40, 0, width - 100, 20, new LiteralText("")); // 5
            url.setMaxLength(65535);
            url.setText(old);
        }
    }
    
    public int scaleY(int y) {
        double sy = ((double) y) / ((double) height) * client.getWindow().getScaledHeight();
        return (int) sy;
    }
    
    public void loadURL(String url) {
        if(browser == null)
            urlToLoad = url;
        else
            browser.loadURL(url);
    }

    @Override
    public void tick() {
        if(urlToLoad != null && browser != null) {
            browser.loadURL(urlToLoad);
            urlToLoad = null;
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int i1, int i2, float f) {
        //Render the URL box first because it overflows a bit
        url.render(matrixStack, i1, i2, f);
        
        //Render buttons
        super.render(matrixStack, i1, i2, f);
        
        //Renders the browser if itsn't null
        if(browser != null) {
            RenderSystem.disableDepthTest();
            RenderSystem.enableTexture();
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            browser.draw(.0d, height, width, 20.d); //Don't forget to flip Y axis.
            RenderSystem.enableDepthTest();
        }
    }
    
    @Override
    public void onClose() {
        //Make sure to close the browser when you don't need it anymore.
        if(!ExampleMod.INSTANCE.hasBackup() && browser != null)
            browser.close();
        
//        Keyboard.enableRepeatEvents(false);
    }
    
    @Override
    public boolean keyPressed(int key, int num, int k) {
        if(GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            client.openScreen(null);
            return super.keyPressed(key, num, k);
        }
        
        boolean pressed = true;
        boolean focused = url.isFocused();
        
        if(browser != null && !focused) { //Inject events into browser. TODO: Handle keyboard mods.
            if(key != '.' && key != ';' && key != ',') { //Workaround
                if(pressed)
                    browser.injectKeyPressed((char) key, 0);
                else
                    browser.injectKeyReleased((char) key, 0);
            }
            
            if(key != GLFW.GLFW_KEY_UNKNOWN)
                browser.injectKeyTyped((char) key, 0);
        }
        
        //Forward event to text box.
        if(!pressed && focused && num == GLFW.GLFW_KEY_ENTER)
            browser.loadURL(url.getText());
        else if(pressed)
            url.keyPressed(key, num, 0); // TODO

        return super.keyPressed(key, num, k);
    }
        
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int btn) {
        boolean pressed = true;
        int sx = (int) mouseX;
        int sy = (int) mouseY;
        int wheel = 0; // Mouse.getEventDWheel();
        
        if(browser != null) { //Inject events into browser. TODO: Handle mods & leaving.
            int y = client.getWindow().getScaledHeight() - sy - scaleY(20); //Don't forget to flip Y axis.

            if(wheel != 0)
                browser.injectMouseWheel(sx, y, 0, 1, wheel);
            else if(btn == -1)
                browser.injectMouseMove(sx, y, 0, y < 0);
            else
                browser.injectMouseButton(sx, y, 0, btn + 1, pressed, 1);
        }
        
        if(pressed) { //Forward events to GUI.
            int x = sx * width / client.getWindow().getScaledWidth();
            int y = height - (sy * height / client.getWindow().getScaledHeight()) - 1;

            try {
                mouseClicked(x, y, btn);
            } catch(Throwable t) {
                t.printStackTrace();
            }

            url.mouseClicked(x, y, btn);
        }

        return super.mouseClicked(mouseX, mouseY, btn);
    }
    
    //Called by ExampleMod when the current browser's URL changes.
    public void onUrlChanged(IBrowser b, String nurl) {
        if(b == browser && url != null) {
            url.setText(nurl);
            vidMode.active = nurl.matches(YT_REGEX1) || nurl.matches(YT_REGEX2) || nurl.matches(YT_REGEX3);
        }
    }
}
