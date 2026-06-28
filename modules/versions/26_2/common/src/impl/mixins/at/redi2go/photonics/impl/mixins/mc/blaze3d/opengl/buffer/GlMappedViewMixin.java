package at.redi2go.photonics.impl.mixins.mc.blaze3d.opengl.buffer;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;

// 26.2 moved buffer mapping off the command encoder; a mapped view is now GpuBufferSlice.MappedView,
// returned by GpuBuffer#map / GpuBufferSlice#map.
@Mixin(GpuBufferSlice.MappedView.class)
public abstract class GlMappedViewMixin implements IGpuBuffer.MappedView {
    @Shadow
    public abstract ByteBuffer data();

    @Override
    public ByteBuffer ph$data() {
        return data();
    }
}
