package net.roguelogix.phosphophyllite.fluids;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;


import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IPhosphophylliteFluidHandler extends IFluidHandler {
    default IFluidHandler fluidHandlerCapability() {
        return this;
    }
    
    @Override
    default int getTanks() {
        return tankCount();
    }
    
    
    @Nonnull
    default FluidStack getFluidInTank(int tank) {
        final var fluid = fluidTypeInTank(tank);
        if (fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        final var amount = (int) Math.min(Integer.MAX_VALUE, fluidAmountInTank(tank));
        if (amount <= 0) {
            return FluidStack.EMPTY;
        }
        final var components = fluidTagInTank(tank);
        return new FluidStack(fluid.builtInRegistryHolder(), amount, components == null ? DataComponentPatch.EMPTY : components);
    }
    
    @Override
    default int getTankCapacity(int tank) {
        return (int) Math.min(Integer.MAX_VALUE, tankCapacity(tank));
    }
    
    @Override
    default boolean isFluidValid(int tank, FluidStack resource) {
        return fluidValidForTank(tank, resource.getFluid());
    }
    
    @Override
    default int fill(FluidStack resource, FluidAction action) {
        return (int) fill(resource.getFluid(), componentsOf(resource), resource.getAmount(), action.simulate());
    }
    
    
    @Nonnull
    @Override
    default FluidStack drain(FluidStack resource, FluidAction action) {
        int drainedAmount = (int) drain(resource.getFluid(), componentsOf(resource), resource.getAmount(), action.simulate());
        if (drainedAmount == 0) {
            return FluidStack.EMPTY;
        }
        return resource.copyWithAmount(drainedAmount);
    }
    
    
    @Nonnull
    @Override
    default FluidStack drain(int maxDrain, FluidAction action) {
        
        int drainedAmount = 0;
        Fluid fluid = null;
        DataComponentPatch components = null;
        for (int i = 0; i < tankCount(); i++) {
            fluid = fluidTypeInTank(i);
            components = fluidTagInTank(i);
            drainedAmount = (int) drain(fluid, components, maxDrain, action.simulate());
            if (drainedAmount != 0) {
                break;
            }
        }
        if (drainedAmount == 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid.builtInRegistryHolder(), drainedAmount, components == null ? DataComponentPatch.EMPTY : components);
    }
    
    /**
     * @return the stack's component patch, or null if it has none, matching the pre-1.20.5 null tag convention
     */
    @Nullable
    static DataComponentPatch componentsOf(FluidStack stack) {
        final var patch = stack.getComponentsPatch();
        return patch.isEmpty() ? null : patch;
    }
    
    int tankCount();
    
    long tankCapacity(int tank);
    
    Fluid fluidTypeInTank(int tank);
    
    @Nullable
    DataComponentPatch fluidTagInTank(int tank);
    
    long fluidAmountInTank(int tank);
    
    boolean fluidValidForTank(int tank, Fluid fluid);
    
    long fill(Fluid fluid, @Nullable DataComponentPatch tag, long amount, boolean simulate);
    
    long drain(Fluid fluid, @Nullable DataComponentPatch tag, long amount, boolean simulate);
}
