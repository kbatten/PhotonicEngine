package at.redi2go.photonics.impl.mixins.mc.blaze3d.opengl.systems;

import at.redi2go.photonics.api.gpu.buffers.IGpuBuffer;
import at.redi2go.photonics.api.gpu.buffers.heap.IGpuBufferHeap;
import at.redi2go.photonics.api.gpu.systems.ICommandEncoder;
import at.redi2go.photonics.api.gpu.systems.IGpuDevice;
import at.redi2go.photonics.api.gpu.textures.IAddressMode;
import at.redi2go.photonics.api.gpu.textures.IFilterMode;
import at.redi2go.photonics.api.gpu.textures.IGpuSampler;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture2D;
import at.redi2go.photonics.api.gpu.textures.IGpuTexture3D;
import at.redi2go.photonics.api.gpu.textures.ITextureFormat;
import at.redi2go.photonics.impl.mc.blaze3d.common.GpuDeviceImpl;
import at.redi2go.photonics.impl.mc.blaze3d.common.TextureFormats;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.GlTextureFormats;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.buffer.GlBufferHeap;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.textures.GlTexture2D;
import at.redi2go.photonics.impl.mc.blaze3d.opengl.textures.GlTexture3D;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.OptionalDouble;
import java.util.function.Supplier;

// 26.2 split GpuDevice into a public wrapper (com.mojang.blaze3d.systems.GpuDevice) that delegates
// to a GpuDeviceBackend (e.g. the OpenGL GlDevice). RenderSystem.getDevice() now returns the wrapper,
// so the mod's IGpuDevice is implemented on the wrapper and forwards to the backend.
@Mixin(GpuDevice.class)
public abstract class GlDeviceMixin implements GpuDeviceImpl, IGpuDevice {
    @Shadow
    @Final
    private GpuDeviceBackend backend;

    @Override
    public ICommandEncoder ph$createCommandEncoder() {
        return (ICommandEncoder) backend.createCommandEncoder();
    }

    @Override
    public IGpuSampler ph$createSampler(
            IAddressMode addressModeU,
            IAddressMode addressModeV,
            IFilterMode minFilter,
            IFilterMode magFilter,
            int maxAnisotropy,
            OptionalDouble maxLod
    ) {
        return (IGpuSampler) backend.createSampler(
                (AddressMode) (Object) addressModeU,
                (AddressMode) (Object) addressModeV,
                (FilterMode) (Object) minFilter,
                (FilterMode) (Object) magFilter,
                maxAnisotropy,
                maxLod
        );
    }

    @Override
    public IGpuTexture2D ph$createTexture2D(
            @Nullable Supplier<String> label,
            int usage,
            ITextureFormat textureFormat,
            int width, int height,
            int mipLevels
    ) {
        //TODO: Verify arguments
        return new GlTexture2D(label, usage, textureFormat, new Vector2i(width, height), mipLevels);
    }

    @Override
    public IGpuTexture3D ph$createTexture3D(
            @Nullable Supplier<String> label,
            int usage, ITextureFormat textureFormat,
            int width, int height, int depth,
            int mipLevels
    ) {
        //TODO: Verify arguments
        return new GlTexture3D(label, usage, textureFormat, new Vector3i(width, height, depth), mipLevels);
    }

    @Override
    public IGpuBuffer ph$createBuffer(@Nullable Supplier<String> label, long byteSize, int usage) {
        return (IGpuBuffer) backend.createBuffer(label, usage, byteSize);
    }

    @Override
    public IGpuBufferHeap ph$createBufferHeap(@Nullable Supplier<String> label, long byteSize, int usage) {
        return new GlBufferHeap(this, label, byteSize, usage);
    }

    @Override
    public TextureFormats getTextureFormats() {
        return GlTextureFormats.INSTANCE;
    }
}
