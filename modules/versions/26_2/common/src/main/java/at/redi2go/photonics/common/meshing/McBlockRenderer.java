package at.redi2go.photonics.common.meshing;

import at.redi2go.photonics.api.mc.Id;
import at.redi2go.photonics.common.iris.IrisUtil;
import at.redi2go.photonics.core.rendering.world.bakery.BlockBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// 26.2 rebuilt the block render pipeline. This is a best-effort port of the geometry capture against
// the decompiled vanilla mesher; it compiles but is unvalidated and needs a runtime check:
//   - Block models come from ModelManager#getBlockStateModelSet().get(state) -> BlockStateModel.
//   - Geometry is captured by feeding ModelBlockRenderer#tesselateBlock a BlockQuadOutput that delegates
//     to VertexConsumer#putBlockBakedQuad (which expands each BakedQuad into addVertex/setColor/setUv,
//     i.e. exactly what BlockBuilder records). A no-AO / no-cull ModelBlockRenderer matches the old
//     tesselateWithoutAO behaviour.
//   - Fluids go through the new FluidRenderer + FluidStateModelSet.
//   - Dynamic block-entity geometry is NOT captured (see meshBlock): 26.2's FeatureRenderDispatcher
//     needs RenderBuffers/GameRenderState and drains a SubmitNodeStorage instead of a MultiBufferSource,
//     so the old buffer-source interception no longer applies. The block's static model is still captured.
public class McBlockRenderer {
    private static final Id BLOCK_ATLAS = (Id) (Object) TextureAtlas.LOCATION_BLOCKS;
    private static final Set<Fluid> WHITELISTED_FLUIDS = Set.of(Fluids.LAVA, Fluids.FLOWING_LAVA);

    private final RandomSource randomSource = RandomSource.create();

    private final ModelBlockRenderer modelBlockRenderer =
            new ModelBlockRenderer(false, false, Minecraft.getInstance().getBlockColors());
    private final FluidRenderer fluidRenderer =
            new FluidRenderer(Minecraft.getInstance().getModelManager().getFluidStateModelSet());

    private final SimpleMeshState.HashStorage hashStorage = new SimpleMeshState.HashStorage();

    public McMeshState extractMeshState(
            Vector3i blockChunkOffset,
            BlockPos blockPos,
            BlockState blockState,
            BlockAndTintGetter blockAndTintGetter
    ) {
        if (blockState.getBlock() == Blocks.END_GATEWAY)
            return EmptyMeshState.INSTANCE;

        int blockId = IrisUtil.getBlockId(blockState);
        FluidState fluidState = blockState.getFluidState();

        List<BlockStateModelPart> parts;
        if (blockState.getRenderShape() == RenderShape.MODEL) {
            parts = new ArrayList<>();

            randomSource.setSeed(blockState.getSeed(blockPos));
            var model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
            model.collectParts(randomSource, parts);
        } else parts = List.of();

        if (blockState.hasBlockEntity()) return new DynamicMeshState(blockId, fluidState, parts);
        if (WHITELISTED_FLUIDS.contains(fluidState.getType())) return new DynamicMeshState(blockId, fluidState, parts);

        if (parts.isEmpty())
            return EmptyMeshState.INSTANCE;

        var meshState = new SimpleMeshState(blockState.getBlock(), blockId, parts);
        meshState.computeHash(
                hashStorage,
                blockState,
                blockPos,
                blockAndTintGetter
        );

        return meshState;
    }

    public void meshBlock(
            McMeshState meshState,
            Vector3i blockChunkOffset,
            BlockPos pos,
            BlockState blockState,
            BlockAndTintGetter blockAndTintGetter,
            BlockBuilder builder
    ) {
        if (meshState == EmptyMeshState.INSTANCE) return;

        builder.useBlockId(meshState.blockId());

        FluidState fluidState = meshState.fluidState();
        if (!fluidState.isEmpty()) {
            submitFluid(pos, blockAndTintGetter, builder, blockState, fluidState);
        }

        if (blockState.hasBlockEntity()) {
            // TODO(26.2): capture dynamic block-entity geometry. Needs the new FeatureRenderDispatcher
            // (RenderBuffers + GameRenderState) and draining a SubmitNodeStorage; the static model below
            // is still captured.
        }

        List<BlockStateModelPart> parts = meshState.blockModel();
        if (!parts.isEmpty()) {
            submitBlock(pos, blockState, blockAndTintGetter, builder);
        }
    }

    private void submitFluid(
            BlockPos blockPos,
            BlockAndTintGetter blockAndTintGetter,
            BlockBuilder builder,
            BlockState blockState,
            FluidState fluidState
    ) {
        if (!WHITELISTED_FLUIDS.contains(fluidState.getType())) return;

        builder.useAtlas(BLOCK_ATLAS);
        builder.useOffset(
                -(blockPos.getX() & 15),
                -(blockPos.getY() & 15),
                -(blockPos.getZ() & 15)
        );

        VertexConsumer vertexConsumer = (VertexConsumer) builder;
        fluidRenderer.tesselate(blockAndTintGetter, blockPos, layer -> vertexConsumer, blockState, fluidState);
    }

    private void submitBlock(
            BlockPos pos,
            BlockState blockState,
            BlockAndTintGetter blockAndTintGetter,
            BlockBuilder builder
    ) {
        builder.useAtlas(BLOCK_ATLAS);
        builder.useOffset(0f, 0f, 0f);

        VertexConsumer vertexConsumer = (VertexConsumer) builder;
        var model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);

        // BlockQuadOutput is a @FunctionalInterface with the same shape as VertexConsumer#putBlockBakedQuad.
        modelBlockRenderer.tesselateBlock(
                vertexConsumer::putBlockBakedQuad,
                0f, 0f, 0f,
                blockAndTintGetter,
                pos,
                blockState,
                model,
                blockState.getSeed(pos)
        );
    }
}
