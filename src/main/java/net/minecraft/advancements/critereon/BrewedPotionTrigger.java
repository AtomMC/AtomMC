package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionType;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

public class BrewedPotionTrigger implements ICriterionTrigger<BrewedPotionTrigger.Instance>
{
    private static final ResourceLocation ID = new ResourceLocation("brewed_potion");
    private final Map<PlayerAdvancements, Listeners> listeners = Maps.<PlayerAdvancements, Listeners>newHashMap();

    public ResourceLocation getId()
    {
        return ID;
    }

    public void addListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<Instance> listener)
    {
        Listeners brewedpotiontrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (brewedpotiontrigger$listeners == null)
        {
            brewedpotiontrigger$listeners = new Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, brewedpotiontrigger$listeners);
        }

        brewedpotiontrigger$listeners.addListener(listener);
    }

    public void removeListener(PlayerAdvancements playerAdvancementsIn, ICriterionTrigger.Listener<Instance> listener)
    {
        Listeners brewedpotiontrigger$listeners = this.listeners.get(playerAdvancementsIn);

        if (brewedpotiontrigger$listeners != null)
        {
            brewedpotiontrigger$listeners.removeListener(listener);

            if (brewedpotiontrigger$listeners.isEmpty())
            {
                this.listeners.remove(playerAdvancementsIn);
            }
        }
    }

    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn)
    {
        this.listeners.remove(playerAdvancementsIn);
    }

    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
    {
        PotionType potiontype = null;

        if (json.has("potion"))
        {
            ResourceLocation resourcelocation = new ResourceLocation(JsonUtils.getString(json, "potion"));

            if (!PotionType.REGISTRY.containsKey(resourcelocation))
            {
                throw new JsonSyntaxException("Unknown potion '" + resourcelocation + "'");
            }

            potiontype = PotionType.REGISTRY.getObject(resourcelocation);
        }

        return new Instance(potiontype);
    }

    public void trigger(EntityPlayerMP player, PotionType potionIn)
    {
        Listeners brewedpotiontrigger$listeners = this.listeners.get(player.getAdvancements());

        if (brewedpotiontrigger$listeners != null)
        {
            brewedpotiontrigger$listeners.trigger(potionIn);
        }
    }

    public static class Instance extends AbstractCriterionInstance
        {
            private final PotionType potion;

            public Instance(@Nullable PotionType potion)
            {
                super(BrewedPotionTrigger.ID);
                this.potion = potion;
            }

            public boolean test(PotionType potion)
            {
                return this.potion == null || this.potion == potion;
            }
        }

    static class Listeners
        {
            private final PlayerAdvancements playerAdvancements;
            private final Set<ICriterionTrigger.Listener<Instance>> listeners = Sets.<ICriterionTrigger.Listener<Instance>>newHashSet();

            public Listeners(PlayerAdvancements playerAdvancementsIn)
            {
                this.playerAdvancements = playerAdvancementsIn;
            }

            public boolean isEmpty()
            {
                return this.listeners.isEmpty();
            }

            public void addListener(ICriterionTrigger.Listener<Instance> listener)
            {
                this.listeners.add(listener);
            }

            public void removeListener(ICriterionTrigger.Listener<Instance> listener)
            {
                this.listeners.remove(listener);
            }

            public void trigger(PotionType potion)
            {
                List<ICriterionTrigger.Listener<Instance>> list = null;

                for (ICriterionTrigger.Listener<Instance> listener : this.listeners)
                {
                    if (((Instance)listener.getCriterionInstance()).test(potion))
                    {
                        if (list == null)
                        {
                            list = Lists.<ICriterionTrigger.Listener<Instance>>newArrayList();
                        }

                        list.add(listener);
                    }
                }

                if (list != null)
                {
                    for (ICriterionTrigger.Listener<Instance> listener1 : list)
                    {
                        listener1.grantCriterion(this.playerAdvancements);
                    }
                }
            }
        }
}