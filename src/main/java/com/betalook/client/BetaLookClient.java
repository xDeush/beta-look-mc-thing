package com.betalook.client;

import com.betalook.BetaLook;
import com.betalook.registry.BetaContent;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BetaLookClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(new IdentifiableResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.of(BetaLook.MOD_ID, "cache_invalidation");
                    }

                    @Override
                    public CompletableFuture<Void> reload(Synchronizer sync, ResourceManager manager,
                                                          Profiler prepare, Profiler apply,
                                                          Executor prepareExec, Executor applyExec) {
                        return sync.whenPrepared(null)
                                .thenRunAsync(BetaContent::invalidateCaches, applyExec);
                    }
                });
    }
}
