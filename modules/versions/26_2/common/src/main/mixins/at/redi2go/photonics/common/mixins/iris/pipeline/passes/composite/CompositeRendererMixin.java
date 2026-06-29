package at.redi2go.photonics.common.mixins.iris.pipeline.passes.composite;

import at.redi2go.photonics.common.iris.IrisUtil;
import at.redi2go.photonics.common.iris.pipeline.CompositeRendererPassExt;
import at.redi2go.photonics.common.iris.pipeline.framebuffer.InternalIrisFramebuffer;
import at.redi2go.photonics.common.iris.pipeline.renderer.PhotonicsRenderer;
import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.gl.program.ComputeProgram;
import net.irisshaders.iris.gl.program.Program;
import net.irisshaders.iris.pipeline.CompositePass;
import net.irisshaders.iris.pipeline.CompositeRenderer;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CompositeRenderer.class)
public abstract class CompositeRendererMixin {
    @Shadow
    @Final
    private WorldRenderingPipeline pipeline;

    @Shadow
    @Final
    private ImmutableList<?> passes;

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/ImmutableList$Builder;add(Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList$Builder;",
                    ordinal = 1
            )
    )
    private ImmutableList.Builder<Object> addPass(
            ImmutableList.Builder<Object> instance,
            Object element,
            Operation<ImmutableList.Builder<Object>> original,
            @Local(name = "i") int i
    ) {
        ((CompositeRendererPassExt) element).setIndex(i);

        return original.call(instance, element);
    }

    @WrapOperation(
            method = "renderAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/pipeline/CompositePass;name()Ljava/lang/String;"
            )
    )
    private String replaceName(CompositePass instance, Operation<String> original) {
        return (Object) this instanceof PhotonicsRenderer renderer ? renderer.getName() : original.call(instance);
    }

    @WrapOperation(
            method = "renderAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/gl/program/ComputeProgram;use()V"
            )
    )
    private void useCompute(ComputeProgram instance, Operation<Void> original) {
        IrisUtil.bindBuffers(pipeline, instance.getProgramId());
        original.call(instance);
    }

    @WrapOperation(
            method = "renderAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/gl/program/Program;use()V"
            )
    )
    private void use(Program instance, Operation<Void> original) {
        IrisUtil.bindBuffers(pipeline, instance.getProgramId());
        original.call(instance);
    }

    // 26.2/Iris 1.11.1: composite passes are no longer drawn through RenderPass#drawIndexed; renderAll
    // now issues a raw GlStateManager._drawElements. Wrap that and recover the current pass from the
    // loop local (CompositeRenderer$Pass implements the public CustomPass interface) to unbind the
    // Photonics framebuffer after each pass.
    @WrapOperation(
            method = "renderAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/opengl/GlStateManager;_drawElements(IIIJ)V"
            )
    )
    public void renderAll(
            int mode,
            int count,
            int type,
            long indices,
            Operation<Void> original,
            // The loop variable is the package-private CompositeRenderer$Pass, which no @Local
            // discriminator can bind by its (inaccessible) type. Capture the loop index instead — a
            // plain int that binds reliably by name — and read the pass out of the shadowed list.
            @Local(name = "i") int passIndex
    ) {
        original.call(mode, count, type, indices);
        ((CompositeRendererPassExt) passes.get(passIndex))
                .getFramebuffer()
                .ifPresent(e -> ((InternalIrisFramebuffer) e).unbind());
    }
}
