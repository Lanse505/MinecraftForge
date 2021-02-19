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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
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
     * Default Implementation mimicking water.
     *
     * @param state  The current FluidState
     * @param entity Entity whose motion is being scaled.
     * @return Returns if it could scale the motion.
     */
    default boolean handleFluidAcceleration(FluidState state, Entity entity) {
        AxisAlignedBB axisalignedbb = entity.getBoundingBox().shrink(0.001D);
        int i = MathHelper.floor(axisalignedbb.minX);
        int j = MathHelper.ceil(axisalignedbb.maxX);
        int k = MathHelper.floor(axisalignedbb.minY);
        int l = MathHelper.ceil(axisalignedbb.maxY);
        int i1 = MathHelper.floor(axisalignedbb.minZ);
        int j1 = MathHelper.ceil(axisalignedbb.maxZ);
        if (!entity.world.isAreaLoaded(i, k, i1, j, l, j1)) {
            return false;
        } else {
            double d0 = 0.0D;
            boolean flag = entity.isPushedByWater();
            boolean flag1 = false;
            Vector3d vector3d = Vector3d.ZERO;
            int k1 = 0;
            BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

            for (int l1 = i; l1 < j; ++l1) {
                for (int i2 = k; i2 < l; ++i2) {
                    for (int j2 = i1; j2 < j1; ++j2) {
                        blockpos$mutable.setPos(l1, i2, j2);
                        double d1 = (float) i2 + state.getActualHeight(entity.world, blockpos$mutable);
                        if (d1 >= axisalignedbb.minY) {
                            flag1 = true;
                            d0 = Math.max(d1 - axisalignedbb.minY, d0);
                            if (flag) {
                                Vector3d vector3d1 = state.getFlow(entity.world, blockpos$mutable);
                                if (d0 < 0.4D) {
                                    vector3d1 = vector3d1.scale(d0);
                                }

                                vector3d = vector3d.add(vector3d1);
                                ++k1;
                            }
                        }
                    }
                }
            }

            if (vector3d.length() > 0.0D) {
                if (k1 > 0) {
                    vector3d = vector3d.scale(1.0D / (double) k1);
                }

                if (!(this instanceof PlayerEntity)) {
                    vector3d = vector3d.normalize();
                }

                Vector3d vector3d2 = entity.getMotion();
                vector3d = vector3d.scale(getFluid().getAttributes().getMotionScale(entity, state));
                double d2 = 0.003D;
                double scaler = 0.0045000000000000005D;
                if (Math.abs(vector3d2.x) < d2 && Math.abs(vector3d2.z) < d2 && vector3d.length() < scaler) {
                    vector3d = vector3d.normalize().scale(scaler);
                }

                entity.setMotion(entity.getMotion().add(vector3d));

            }
            entity.customEyeFluidLevelMap.put(state, d0);
            entity.fluidInsideOf = entity.world.getFluidState(entity.getPosition());
            return flag1;
        }
    }

    /**
     * This method handles "swimming" or "traveling" in Custom Fluids, by default this will be using a modified version of vanilla water behaviour.
     * Override this at your own risk to change the "Travel" characteristics of your fluid.
     */
    default void handleFluidTravel(FluidState state, @Nullable Vector3d travelVector, LivingEntity entity, boolean isFluidJump) {
        if (isFluidJump) {
             entity.setMotion(entity.getMotion().add(0.0D, (double)0.04F * entity.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).getValue(), 0.0D));
        } else {
            ModifiableAttributeInstance gravity = entity.getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get());
            boolean flag = entity.getMotion().y <= 0.0D;
            double y = entity.getPosY();
            float slowdownMultiplier = entity.isSprinting() ? 0.9F : entity.getEntityWaterSlowDown();
            float swimSpeed = 0.02F;
            float striderModifier = (float) EnchantmentHelper.getDepthStriderModifier(entity);
            if (striderModifier > 3.0F) {
                striderModifier = 3.0F;
            }

            if (!entity.isOnGround()) {
                striderModifier *= 0.5F;
            }

            if (striderModifier > 0.0F) {
                slowdownMultiplier += (0.54600006F - slowdownMultiplier) * striderModifier / 3.0F;
                swimSpeed += (entity.getAIMoveSpeed() - swimSpeed) * striderModifier / 3.0F;
            }

            if (entity.isPotionActive(Effects.DOLPHINS_GRACE)) {
                slowdownMultiplier = 0.96F;
            }

            swimSpeed *= (float)entity.getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).getValue();
            entity.moveRelative(swimSpeed, travelVector);
            entity.move(MoverType.SELF, entity.getMotion());
            Vector3d motion = entity.getMotion();
            if (entity.collidedHorizontally && entity.isOnLadder()) {
                motion = new Vector3d(motion.x, 0.2D, motion.z);
            }

            entity.setMotion(motion.mul(slowdownMultiplier, 0.8F, slowdownMultiplier));
            Vector3d gravityPull = entity.func_233626_a_(gravity.getValue(), flag, entity.getMotion());
            entity.setMotion(gravityPull);
            if (entity.collidedHorizontally && entity.isOffsetPositionInLiquid(gravityPull.x, gravityPull.y + (double)0.6F - entity.getPosY() + y, gravityPull.z)) {
                entity.setMotion(gravityPull.x, 0.3F, gravityPull.z);
            }
        }
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
