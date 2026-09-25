package com.betalook.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.betalook.BetaLook;
import com.betalook.registry.BetaContent;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

public class BetaLookClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Po przeladowaniu zasobow rejestry moga zawierac inne obiekty,
        // wiec cache tozsamosciowy trzeba wyczyscic.
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(new IdentifiableResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return ResourceLocation.fromNamespaceAndPath(
                                BetaLook.MOD_ID, "cache_invalidation");
                    }

                    @Override
                    public CompletableFuture<Void> reload(PreparationBarrier barrier,
                                                          ResourceManager manager,
                                                          Executor prepareExecutor,
                                                          Executor applyExecutor) {
                        return barrier.wait(null)
                                .thenRunAsync(BetaContent::invalidateCaches, applyExecutor);
                    }
                });
    }
}
