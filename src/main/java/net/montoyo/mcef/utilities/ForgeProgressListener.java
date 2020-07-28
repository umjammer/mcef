package net.montoyo.mcef.utilities;

import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.text.LiteralText;

public class ForgeProgressListener extends ProgressScreen implements IProgressListener {

    @Override
    public void onProgressed(double d) {
        super.progressStagePercentage((int) Util.clamp(d, 0.d, 100.d));
    }

    @Override
    public void onTaskChanged(String name) {
        super.method_15412(new LiteralText(name));
    }

    @Override
    public void onProgressEnd() {
        super.setDone();
    }
}
