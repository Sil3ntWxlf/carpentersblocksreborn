package com.carpentersblocksreborn.advancements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.Set;

public class CraftHammerTrigger implements ICriterionTrigger<CraftHammerTrigger.Instance> {

    public static final ResourceLocation ID = new ResourceLocation("craft_cbr_hammer");
    private final Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<CraftHammerTrigger.Instance> listener) {
        CraftHammerTrigger.Listeners listeners = this.listeners.computeIfAbsent(playerAdvancementsIn, Listeners::new);
        listeners.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<CraftHammerTrigger.Instance> listener) {
        CraftHammerTrigger.Listeners listeners = this.listeners.get(playerAdvancementsIn);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                this.listeners.remove(playerAdvancementsIn);
            }
        }
    }

    @Override
    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn) {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public Instance deserialize(JsonObject json, ConditionArrayParser condition) {
        EntityPredicate.AndPredicate player = EntityPredicate.AndPredicate.deserializeJSONObject(json, "player", condition);
        return new CraftHammerTrigger.Instance(player);
    }

    public void trigger(ServerPlayerEntity player) {
        CraftHammerTrigger.Listeners listeners = this.listeners.get(player.getAdvancements());
        if (listeners != null) {
            listeners.trigger();
        }
    }

    public static class Instance extends CriterionInstance {

        public Instance(EntityPredicate.AndPredicate player) {
            super(CraftHammerTrigger.ID, player);
        }
    }

    static class Listeners {

        private final PlayerAdvancements playerAdvancements;
        private final Set<Listener<Instance>> listeners = Sets.newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn) {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void add(ICriterionTrigger.Listener<CraftHammerTrigger.Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(ICriterionTrigger.Listener<CraftHammerTrigger.Instance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger() {
            for (ICriterionTrigger.Listener<CraftHammerTrigger.Instance> listener : Lists.newArrayList(this.listeners)) {
                listener.grantCriterion(this.playerAdvancements);
            }
        }
    }
}
