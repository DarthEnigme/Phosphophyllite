package net.roguelogix.phosphophyllite.fluids;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidHandlerWrapper implements IPhosphophylliteFluidHandler {
    
    public static IPhosphophylliteFluidHandler wrap(IFluidHandler handler) {
        if (handler instanceof IPhosphophylliteFluidHandler) {
            return (IPhosphophylliteFluidHandler) handler;
        }
        return new FluidHandlerWrapper(handler);
    }
    
    IFluidHandler handler;
    
    private FluidHandlerWrapper(IFluidHandler handler) {
        this.handler = handler;
    }
    
    @Override
    public int tankCount() {
        return handler.getTanks();
    }
    
    @Override
    public long tankCapacity(int tank) {
        return handler.getTankCapacity(tank);
    }
    
    @Override
    public Fluid fluidTypeInTank(int tank) {
        return handler.getFluidInTank(tank).getFluid();
    }
    
    @Nullable
    @Override
    public DataComponentPatch fluidTagInTank(int tank) {
        return IPhosphophylliteFluidHandler.componentsOf(handler.getFluidInTank(tank));
    }
    
    @Override
    public long fluidAmountInTank(int tank) {
        return handler.getFluidInTank(tank).getAmount();
    }
    
    @Override
    public boolean fluidValidForTank(int tank, Fluid fluid) {
        if(fluid == Fluids.EMPTY){
            return false;
        }
        return handler.isFluidValid(tank, new FluidStack(fluid, 1));
    }
    
    @Override
    public long fill(Fluid fluid, @Nullable DataComponentPatch tag, long amount, boolean simulate) {
        if(fluid == Fluids.EMPTY || amount <= 0){
            return 0;
        }
        return handler.fill(stackOf(fluid, tag, amount), simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
    }
    
    @Override
    public long drain(Fluid fluid, @Nullable DataComponentPatch tag, long amount, boolean simulate) {
        if(fluid == Fluids.EMPTY || amount <= 0){
            return 0;
        }
        FluidStack drained = handler.drain(stackOf(fluid, tag, amount), simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
        return drained.getAmount();
    }
    
    private static FluidStack stackOf(Fluid fluid, @Nullable DataComponentPatch tag, long amount) {
        return new FluidStack(fluid.builtInRegistryHolder(), (int) Math.min(Integer.MAX_VALUE, amount), tag == null ? DataComponentPatch.EMPTY : tag);
    }
}
