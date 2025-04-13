package com.pivoo.stockperipheral.Peripherals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.pivoo.stockperipheral.CCTweakedCreatestockperipheral;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.LuaTable;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import com.simibubi.create.content.logistics.packagerLink.PackagerLinkBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.*;

import dan200.computercraft.shared.util.NBTUtil;

//public class StockPeripheral implements IPeripheral {
public class StockPeripheral implements IPeripheral {
    private final PackagerLinkBlockEntity _stockLink;

    public StockPeripheral(PackagerLinkBlockEntity stockLink){
        this._stockLink = stockLink;
    }

    public IPeripheral getPeripheral(){
        return (IPeripheral) this;
    }

    public PackagerLinkBlockEntity getStockLink() {
        return _stockLink;
    }
    //region Overrides Methods

    @Override
    public String getType() {
        return "stock_link";
    }

    @Override
    public void attach(IComputerAccess computer) {
        IPeripheral.super.attach(computer);
    }

    @Override
    public void detach(IComputerAccess computer) {
        IPeripheral.super.detach(computer);
    }

    @Override
    public @Nullable Object getTarget() {
        return IPeripheral.super.getTarget();
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return false;
    }

    //endregion

    //region Lua Methods

    @LuaFunction
    public String helloWorld(){
        return "Hello World";
    }

    @LuaFunction
    public void playEffect(){
        this.getStockLink().playEffect();
    }

    @LuaFunction
    public String getData(){
        Level level = this.getStockLink().getLevel();
        return this.getStockLink().saveWithoutMetadata(level.registryAccess()).toString();
    }

    @LuaFunction
    public String getFreq(){
        return getFrequencyID().toString();
    }

    @LuaFunction
    public Object getSummary(){
        CCTweakedCreatestockperipheral.Log(this.getFrequencyID().toString());
        InventorySummary summary = getAccurateSummary();

//        CCTweakedCreatestockperipheral.Log(gson.toJson(summary));
//        for(for )
        List<Object> result = new ArrayList<>();
        for(BigItemStack stack: summary.getStacks()){
            Map<String,Object> map = new HashMap<>();
            CCTweakedCreatestockperipheral.Log(BuiltInRegistries.ITEM.getKey(stack.stack.getItem()).toString());
            map.put("name", BuiltInRegistries.ITEM.getKey(stack.stack.getItem()).toString());
            map.put("count", stack.count);
            result.add(map);
        }

        return result;
    }

    @LuaFunction
    public boolean sendRequest(String resource, int count, String address){
        try{
        List<BigItemStack> order = new ArrayList<>();
        order.add(new BigItemStack(
            createStack(resource,1),
            count
        ));
        return broadcastPackageRequest(order, address);
        } catch (Exception ex){
            CCTweakedCreatestockperipheral.Log(ex.getMessage());
            for(StackTraceElement trace : ex.getStackTrace()){
                CCTweakedCreatestockperipheral.Log(trace.toString());
            }
            throw ex;
        }
    }


    //endregion

    private UUID getFrequencyID(){
//        Level level = this.getStockLink().getLevel();
//        CompoundTag tag =  this.getStockLink().saveWithoutMetadata(level.registryAccess());
//        return tag.getUUID("Freq");
        return this.getStockLink().behaviour.freqId;
    }

    private ItemStack createStack(String fullId, int count) {
        ResourceLocation id = ResourceLocation.parse(fullId);
        Item item = BuiltInRegistries.ITEM.get(id);
        return new ItemStack(item, count);
    }

    private InventorySummary getAccurateSummary(){
        return LogisticsManager.getSummaryOfNetwork(this.getFrequencyID(), true);
//        return this.getStockLink()
    }

    private boolean broadcastPackageRequest(List<BigItemStack> items, String address){
        PackageOrderWithCrafts order = PackageOrderWithCrafts.simple(items);
        CCTweakedCreatestockperipheral.Log(this.getFrequencyID().toString());
        return LogisticsManager.broadcastPackageRequest(
                this.getFrequencyID(),
                LogisticallyLinkedBehaviour.RequestType.REDSTONE,
                order,
                null,
                address

        );
    }
}