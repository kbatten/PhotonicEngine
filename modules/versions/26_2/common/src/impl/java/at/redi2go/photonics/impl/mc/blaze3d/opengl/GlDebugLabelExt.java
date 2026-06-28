package at.redi2go.photonics.impl.mc.blaze3d.opengl;

import at.redi2go.photonics.impl.mc.blaze3d.opengl.textures.IGlTexture;
import at.redi2go.photonics.impl.mixins.mc.blaze3d.opengl.systems.GpuDeviceBackendAccessor;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import com.mojang.blaze3d.systems.RenderSystem;

public interface GlDebugLabelExt {
    void applyLabel(IGlTexture texture2D);

    static GlDebugLabelExt getInstance() {
        GpuDeviceBackend backend = ((GpuDeviceBackendAccessor) RenderSystem.getDevice()).photonics$getBackend();
        return (GlDebugLabelExt) ((GlDevice) backend).debugLabels();
    }
}
