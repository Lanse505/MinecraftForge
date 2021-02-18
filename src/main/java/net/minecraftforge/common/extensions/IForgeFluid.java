/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.common.extensions;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.fluids.FluidAttributes;

import javax.annotation.Nullable;
import java.util.Set;

public interface IForgeFluid
{
    default Fluid getFluid()
    {
        return (Fluid) this;
    }

    /**
     * Called when the entity is inside this block, may be used to determined if the entity can breathing,
     * display material overlays, or if the entity can swim inside a block.
     *
     * @param world that is being tested.
     * @param pos position thats being tested.
     * @param entity that is being tested.
     * @param yToTest, primarily for testingHead, which sends the the eye level of the entity, other wise it sends a y that can be tested vs liquid height.
     * @param testingHead when true, its testing the entities head for vision, breathing ect... otherwise its testing the body, for swimming and movement adjustment.
     */
    default boolean isEntityInside(FluidState state, IWorldReader world, BlockPos pos, Entity entity, double yToTest, boolean testingHead)
    {
        return getFluid() != Fluids.EMPTY && (testingHead ? entity.getEyeHeight(entity.getPose()) < (double) (pos.getY() + state.getActualHeight(world, pos)) + 0.11111111F : yToTest < (double) (pos.getY() + state.getActualHeight(world, pos) + 0.11111111F));
    }

    /**
     * Called when boats or fishing hooks are inside the block to check if they are inside
     * the material requested.
     *
     * @param world world that is being tested.
     * @param pos block thats being tested.
     * @param boundingBox box to test, generally the bounds of an entity that are besting tested.
     * @param materialIn to check for.
     * @return null for default behavior, true if the box is within the material, false if it was not.
     */
    @Nullable
    default Boolean isAABBInsideMaterial(FluidState state, IWorldReader world, BlockPos pos, AxisAlignedBB boundingBox, Material materialIn)
    {
        return getFluid() != Fluids.EMPTY && state.getBlockState().getMaterial() == materialIn && boundingBox.intersects(AxisAlignedBB.fromVector(state.getFlow(world, pos)));
    }

    /**
     * Called when entities are moving to check if they are inside a liquid
     *
     * @param world world that is being tested.
     * @param pos block thats being tested.
     * @param boundingBox box to test, generally the bounds of an entity that are besting tested.
     * @return null for default behavior, true if the box is within the material, false if it was not.
     */
    @Nullable
    default Boolean isAABBInsideLiquid(FluidState state, IWorldReader world, BlockPos pos, AxisAlignedBB boundingBox)
    {
        return getFluid() != Fluids.EMPTY && boundingBox.intersects(AxisAlignedBB.fromVector(state.getFlow(world, pos)));
    }

    /**
     * Location sensitive version of getExplosionResistance
     *
     * @param world The current world
     * @param pos Block position in world
     * @param explosion The explosion
     * @return The amount of the explosion absorbed.
     */
    @SuppressWarnings("deprecation")
    default float getExplosionResistance(FluidState state, IBlockReader world, BlockPos pos, Explosion explosion)
    {
        return state.getExplosionResistance();
    }

    /**
     * Queries if this fluid should render in a given layer.
     * A custom {@link IBakedModel} can use {@link net.minecraftforge.client.MinecraftForgeClient#getRenderLayer()} to alter the model based on layer.
     */
    default boolean canRenderInLayer(FluidState state, RenderType layer)
    {
        return RenderTypeLookup.canRenderInLayer(state, layer);
    }

    /**
     * Queried for the Fluids Base {@code PathNodeType}.
     * Used to determine what the PathNode priority value is for the fluid.
     * Negative Values = Untraversable
     * 0 = Best
     * Highest = Worst
     * @param state The current FluidState.
     * @return {@code null} for default behaviour. Returns the PathNodeType for the Fluid for Pathfinding purposes.
     */
    @Nullable
    default PathNodeType getPathNodeType(FluidState state) {
        return PathNodeType.WATER;
    }

    /**
     * Queried for the Fluids Danger {@code PathNodeType}.
     * Used to alter what the {@code PathNodeType} priority is for any adjacent blocks.
     * Negative Values = Untraversable
     * 0 = Best
     * Highest = Worst
     * @param state The current FluidState.
     * @return {@code null} for default behaviour. Returns the Danger PathNodeType for the Fluid for Pathfinding purposes.
     */
    @Nullable
    default PathNodeType getDangerModifierType(FluidState state) {
        return null;
    }


    /**
     * Retrieves a list of tags names this is known to be associated with.
     * This should be used in favor of TagCollection.getOwningTags, as this caches the result and automatically updates when the TagCollection changes.
     */
    Set<ResourceLocation> getTags();

    /**
     * Retrieves the non-vanilla fluid attributes, including localized name.
     */
    FluidAttributes getAttributes();
}
