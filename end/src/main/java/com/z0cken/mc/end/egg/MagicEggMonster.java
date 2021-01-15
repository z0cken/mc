package com.z0cken.mc.end.egg;

import org.bukkit.block.Block;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class MagicEggMonster extends MagicEgg {

    public MagicEggMonster(Player owner, Block egg) {
        super(owner, egg, true);
    }

    @Override
    public MagicEggType getType() {
        return MagicEggType.MONSTER;
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
        cloud.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
    }

    @Override
    public void onCloudApply(AreaEffectCloudApplyEvent event) {
        if(!event.getEntity().equals(getCloud())) return;
        event.getAffectedEntities().removeIf(entity -> !(entity instanceof Monster));
    }
}
