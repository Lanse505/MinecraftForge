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
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nullable;

public interface IForgeFluidState
{
    default FluidState getFluidState()
    {
        return (FluidState)this;
    }

    /**
     * Called when the entity is inside this block, may be used to determined if the entity can breathing,
     * display material overlays, or if the entity can swim inside a block.
     *
     * @param world that is being tested.
     * @param pos position thats being tested.
     * @param entity that is being tested.
     * @param yToTest, primarily for testingHead, which sends the the eye level of the entity, other wise it sends a y that can be tested vs liquid height.
     * @param tag to test for.
     * @param testingHead when true, its testing the entities head for vision, breathing ect... otherwise its testing the body, for swimming and movement adjustment.
     */
    default boolean isEntityInside(IWorldReader world, BlockPos pos, Entity entity, double yToTest, Tag<Fluid> tag, boolean testingHead)
    {
//        return ifluidstate.isTagged(p_213290_1_) && d0 < (double)((float)blockpos.getY() + ifluidstate.getActualHeight(this.world, blockpos) + 0.11111111F);
        return getFluidState().getFluid().isEntityInside(getFluidState(), world, pos, entity, yToTest, tag, testingHead);
    }



    /**
     * Location sensitive version of getExplosionResistance
     *
     * @param world The current world
     * @param pos Block position in world
     * @param explosion The explosion
     * @return The amount of the explosion absorbed.
     */
    default float getExplosionResistance(IBlockReader world, BlockPos pos, Explosion explosion)
    {
        return getFluidState().getFluid().getExplosionResistance(getFluidState(), world, pos, explosion);
    }

    /**
     * Queries if this fluidstate should render in a given layer.
     * A custom {@link IBakedModel} can use {@link net.minecraftforge.client.MinecraftForgeClient#getRenderLayer()} to alter the model based on layer.
     */
    default boolean canRenderInLayer(RenderType layer)
    {
        return getFluidState().getFluid().canRenderInLayer(getFluidState(), layer);
    }

    /**
     * Queried for the Fluids Base PathNodeType.
     * Used to determine what the PathNode priority value is for the fluid.
     * Negative Values = Untraversable
     * 0 = Best
     * Highest = Worst
     * @return Null for default behaviour. Returns the PathNodeType for the Fluid for Pathfinding purposes.
     */
    @Nullable
    default PathNodeType getPathNodeType() {
        return getFluidState().getFluid().getPathNodeType(getFluidState());
    }

    /**
     * Queried for the Fluids Danger PathNodeType.
     * Used to alter what the PathNodeType priority is for any adjacent blocks.
     * Negative Values = Untraversable
     * 0 = Best
     * Highest = Worst
     * @return Null for default behaviour. Returns the Danger PathNodeType for the Fluid for Pathfinding purposes.
     */
    @Nullable
    default PathNodeType getAiDangerPathNodeType() {
        return getFluidState().getFluid().getAiDangerPathNodeType(getFluidState());
    }
}
