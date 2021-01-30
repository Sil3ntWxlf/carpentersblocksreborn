package com.carpentersblocksreborn.advancements;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemUseTrigger implements ICriterionTrigger<ItemUseTrigger.Instance> {

        public static final ResourceLocation ID = new ResourceLocation("on_item_use");
        private final Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        public void addListener(PlayerAdvancements playerAdvancementsIn, Listener<Instance> listener) {
            ItemUseTrigger.Listeners listeners = this.listeners.computeIfAbsent(playerAdvancementsIn, Listeners::new);
            listeners.add(listener);
        }

        @Override
        public void removeListener(PlayerAdvancements playerAdvancementsIn, Listener<ItemUseTrigger.Instance> listener) {
            ItemUseTrigger.Listeners listeners = this.listeners.get(playerAdvancementsIn);
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
            ItemPredicate item = ItemPredicate.deserialize(json.get("item"));
            BlockPredicate block = BlockPredicate.deserialize(json.get("block"));
            return new ItemUseTrigger.Instance(player, item, block);
        }

        public void trigger(ServerPlayerEntity player, ItemStack item, World world, BlockPos pos) {
            ItemUseTrigger.Listeners listeners = this.listeners.get(player.getAdvancements());
            if (listeners != null) {
                listeners.trigger(item, world, pos);
            }
        }

        public static class Instance extends CriterionInstance {

            private final ItemPredicate item;
            private final BlockPredicate block;

            public Instance(EntityPredicate.AndPredicate player, ItemPredicate item, BlockPredicate block) {
                super(ItemUseTrigger.ID, player);
                this.item = item;
                this.block = block;
            }

            public boolean test(ItemStack item, World world, BlockPos pos) {
                return this.item.test(item) && this.block.test((ServerWorld) world, pos);
            }
        }

        static class Listeners {

            private final PlayerAdvancements playerAdvancements;
            private final Set<Listener<Instance>> listeners = Sets.newHashSet();

            public Listeners(PlayerAdvancements playerAdvancements) {
                this.playerAdvancements = playerAdvancements;
            }

            public boolean isEmpty() {
                return this.listeners.isEmpty();
            }

            public void add(Listener<Instance> listener) {
                this.listeners.add(listener);
            }

            public void remove(Listener<Instance> listener) {
                this.listeners.remove(listener);
            }

            public void trigger(ItemStack item, World world, BlockPos pos) {

                List<Listener<Instance>> list = new ArrayList<>();

                for (Listener<Instance> listener : this.listeners) {
                    if (listener.getCriterionInstance().test(item, world, pos)) {
                        list.add(listener);
                    }
                }

                for (Listener<Instance> listener : list) {
                    listener.grantCriterion(this.playerAdvancements);
                }
            }
        }
    }
