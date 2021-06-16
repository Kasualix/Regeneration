package me.suff.mc.regen.common.item;

import net.minecraft.item.Item;
import net.minecraft.item.Rarity;

/**
 * Created by Craig
 * on 02/05/2020 @ 11:34
 */
public class IngotItem extends Item {

    public IngotItem() {
        super(new Item.Properties().rarity(Rarity.UNCOMMON).tab(ItemGroups.REGEN_TAB));
    }
}
