package com.z0cken.mc.end.egg;

import com.z0cken.mc.core.FriendsAPI;
import org.bukkit.block.Block;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import java.sql.SQLException;

public class MagicEggCombat extends MagicEgg {

    public MagicEggCombat(Player owner, Block egg) {
        super(owner, egg, true);
    }

    @Nonnull
    @Override
    public MagicEggType getType() {
        return MagicEggType.COMBAT;
    }

    @Override
    public boolean onDeploy() {
        return true;
    }

    @Override
    public void onExpire() {

    }

    @Override
    void activate() {
        AreaEffectCloud cloud = getCloud();
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 25, 1), false);
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 25, 1), false);
    }

    @Override
    public void onCloudApply(AreaEffectCloudApplyEvent event) {
        if(!event.getEntity().equals(getCloud())) return;
        event.getAffectedEntities().removeIf(entity -> entity.getType() != EntityType.PLAYER);
        event.getAffectedEntities().removeIf(entity -> {
            try {
                return !FriendsAPI.areFriends((entity).getUniqueId(), getOwner().getUniqueId());
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return true;
        });
    }
}
