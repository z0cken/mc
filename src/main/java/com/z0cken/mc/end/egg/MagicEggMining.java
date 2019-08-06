package com.z0cken.mc.end.egg;

import org.bukkit.block.Block;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class MagicEggMining extends MagicEgg {

    public MagicEggMining(Player owner, Block egg) {
        super(owner, egg, true);
    }

    @Override
    public MagicEggType getType() {
        return MagicEggType.MINING;
    }

    //Call only through deploy()
    @Override
    public boolean onDeploy() {
        return true;
    }

    //Call only through expire()
    @Override
    public void onExpire() {

    }

    @Override
    protected void activate() {
        AreaEffectCloud cloud = getCloud();
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 25, 1), false);
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 25, 1), false);
    }
}
