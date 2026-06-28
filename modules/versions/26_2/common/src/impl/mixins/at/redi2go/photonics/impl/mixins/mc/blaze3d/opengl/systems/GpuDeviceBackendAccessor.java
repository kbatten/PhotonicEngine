package at.redi2go.photonics.impl.mixins.mc.blaze3d.opengl.systems;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Exposes the backend held by the 26.2 GpuDevice wrapper so backend-specific APIs
// (e.g. GlDevice#debugLabels) can be reached from RenderSystem.getDevice().
@Mixin(GpuDevice.class)
public interface GpuDeviceBackendAccessor {
    @Accessor("backend")
    GpuDeviceBackend photonics$getBackend();
}
