package net.minecraftforge.debug.fluid.logic_tests;

import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@Mod(FluidTraversabilityTest.MODID)
public class FluidTraversabilityTest
{
    public static final String MODID = "fluid_traversability_test";
    private static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final ResourceLocation FLUID_STILL = new ResourceLocation("minecraft:block/brown_mushroom_block");
    public static final ResourceLocation FLUID_FLOWING = new ResourceLocation("minecraft:block/mushroom_stem");
    public static final ResourceLocation FLUID_OVERLAY = new ResourceLocation("minecraft:block/obsidian");

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MODID);

    private static ForgeFlowingFluid.Properties makeProperties()
    {
        return new ForgeFlowingFluid.Properties(test_fluid, test_fluid_flowing,
            FluidAttributes.builder(FLUID_STILL, FLUID_FLOWING).overlay(FLUID_OVERLAY).color(0x3F1080FF).shouldExtinguishFlames(false).motionScale((entity, state) -> 0.014D))
            .bucket(test_fluid_bucket).block(test_fluid_block);
    }

    public static RegistryObject<FlowingFluid> test_fluid = FLUIDS.register("test_fluid", () ->
        new Source(makeProperties())
    );
    public static RegistryObject<FlowingFluid> test_fluid_flowing = FLUIDS.register("test_fluid_flowing", () ->
        new Flowing(makeProperties())
    );

    public static RegistryObject<FlowingFluidBlock> test_fluid_block = BLOCKS.register("test_fluid_block", () ->
        new FlowingFluidBlockCustom(test_fluid, Block.Properties.create(Material.WATER).doesNotBlockMovement().hardnessAndResistance(100.0F).noDrops())
    );
    public static RegistryObject<Item> test_fluid_bucket = ITEMS.register("test_fluid_bucket", () ->
        new BucketItem(test_fluid, new Item.Properties().containerItem(Items.BUCKET).maxStackSize(1).group(ItemGroup.MISC))
    );

    private static class Source extends ForgeFlowingFluid.Source {
        public Source(Properties properties) {
            super(properties);
        }

        @Nullable
        @Override
        public PathNodeType getPathNodeType(FluidState state) {
            return PathNodeType.WATER;
        }
    }

    private static class Flowing extends ForgeFlowingFluid.Flowing {
        public Flowing(Properties properties) {
            super(properties);
        }

        @Nullable
        @Override
        public PathNodeType getPathNodeType(FluidState state) {
            return PathNodeType.DAMAGE_OTHER;
        }

        @Nullable
        @Override
        public PathNodeType getDangerModifierType(FluidState state) {
            return PathNodeType.DANGER_OTHER;
        }
    }

    private static class FlowingFluidBlockCustom extends FlowingFluidBlock {
        public FlowingFluidBlockCustom(Supplier<? extends FlowingFluid> supplier, Properties properties) {
            super(supplier, properties);
        }
    }

    public FluidTraversabilityTest() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        FLUIDS.register(modEventBus);
    }


}
