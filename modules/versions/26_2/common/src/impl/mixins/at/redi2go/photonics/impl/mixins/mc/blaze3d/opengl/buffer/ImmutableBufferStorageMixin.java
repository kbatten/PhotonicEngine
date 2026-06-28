package at.redi2go.photonics.impl.mixins.mc.blaze3d.opengl.buffer;

import at.redi2go.photonics.impl.mc.blaze3d.opengl.buffer.GlBufferHeap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// 26.2 removed BufferStorage$Immutable#tryMapBufferPersistent: persistent mapping now happens eagerly in
// the GlBuffer.Direct constructor when (canPersistentMap && (usage & MAP_READ|MAP_WRITE) != 0). Photonics'
// buffer heaps map ranges themselves (glMapNamedBufferRange), so a persistent map would clash. Force
// canPersistentMap = false for buffers created with the NO_PERSISTENCE_MAPPING flag.
@Mixin(targets = "com.mojang.blaze3d.opengl.BufferStorage$Immutable")
public abstract class ImmutableBufferStorageMixin {
    @WrapOperation(
            method = "createBuffer",
            at = @At(
                    value = "NEW",
                    target = "(Lcom/mojang/blaze3d/opengl/DirectStateAccess;IJIZ)Lcom/mojang/blaze3d/opengl/GlBuffer$Direct;"
            )
    )
    private GlBuffer.Direct photonics$disablePersistentMappingForHeaps(
            DirectStateAccess dsa,
            int usage,
            long size,
            int handle,
            boolean canPersistentMap,
            Operation<GlBuffer.Direct> original
    ) {
        if ((usage & GlBufferHeap.NO_PERSISTENCE_MAPPING) != 0)
            canPersistentMap = false;

        return original.call(dsa, usage, size, handle, canPersistentMap);
    }
}
