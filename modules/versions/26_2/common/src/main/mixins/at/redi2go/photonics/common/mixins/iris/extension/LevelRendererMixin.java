package at.redi2go.photonics.common.mixins.iris.extension;

import at.redi2go.photonics.common.iris.IrisUtil;
import at.redi2go.photonics.core.iris.PhotonicsExtension;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    // 26.2 renamed LevelRenderer#renderLevel to #render (Camera -> CameraRenderState, fewer matrix args).
    // The frame-begin hook doesn't use the arguments, so an argument-less HEAD inject stays robust.
    @Inject(
            method = "render",
            at = @At("HEAD"),
            order = 900
    )
    public void photonics$onFrameBegin(CallbackInfo ci) {
        IrisUtil.getPhotonics().ifPresent(PhotonicsExtension::onFrameBegin);
    }
}
