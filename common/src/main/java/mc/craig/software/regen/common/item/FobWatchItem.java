package mc.craig.software.regen.common.item;

import mc.craig.software.regen.Regeneration;
import mc.craig.software.regen.common.objects.RItems;
import mc.craig.software.regen.common.objects.RSounds;
import mc.craig.software.regen.common.regen.IRegen;
import mc.craig.software.regen.common.regen.RegenerationData;
import mc.craig.software.regen.common.regen.state.RegenStates;
import mc.craig.software.regen.config.RegenConfig;
import mc.craig.software.regen.util.ClientUtil;
import mc.craig.software.regen.util.PlayerUtil;
import mc.craig.software.regen.util.RegenUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;


/**
 * Created by Sub on 16/09/2018.
 */
public class FobWatchItem extends Item {

    public FobWatchItem() {
        super(new Item.Properties().tab(RItems.MAIN).stacksTo(1).durability(12));
    }

    public static CompoundTag getStackTag(ItemStack stack) {
        CompoundTag stackTag = stack.getOrCreateTag();
        if (!stackTag.contains("is_open") || !stackTag.contains("is_gold")) {
            stackTag.putBoolean("is_open", false);
            stackTag.putBoolean("is_gold", RegenUtil.RAND.nextBoolean());
        }
        return stackTag;
    }

    public static boolean getEngrave(ItemStack stack) {
        return getStackTag(stack).getBoolean("is_gold");
    }

    public static void setEngrave(ItemStack stack, boolean isGold) {
        getStackTag(stack).putBoolean("is_gold", isGold);
    }

    public static boolean isOpen(ItemStack stack) {
        return getStackTag(stack).getBoolean("is_open");
    }

    public static void setOpen(ItemStack stack, boolean isOpen) {
        getStackTag(stack).putBoolean("is_open", isOpen);
    }

    @Override
    public void onCraftedBy(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull Player playerIn) {
        super.onCraftedBy(stack, worldIn, playerIn);
        stack.setDamageValue(0);
        setOpen(stack, false);
    }

    @Override
    public void inventoryTick(ItemStack stack, @NotNull Level worldIn, @NotNull Entity entityIn, int itemSlot, boolean isSelected) {
        if (stack.getItem() instanceof FobWatchItem) {
            if (isOpen(stack)) {
                if (entityIn.tickCount % 600 == 0) {
                    setOpen(stack, false);
                }
            }
        }
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);
        IRegen cap = RegenerationData.get(player).orElseGet(null);

        if (!player.isShiftKeyDown()) { // transferring watch->player
            if (stack.getDamageValue() == getMaxDamage())
                return msgUsageFailed(player, "regen.messages.transfer.empty_watch", stack);
            else if (cap.regens() == getMaxDamage())
                return msgUsageFailed(player, "regen.messages.transfer.max_regens", stack);

            int supply = getMaxDamage() - stack.getDamageValue(), needed = getMaxDamage() - cap.regens(), used = Math.min(supply, needed);

            if (cap.canRegenerate()) {
                setOpen(stack, true);
                PlayerUtil.sendMessage(player, Component.translatable("regen.messages.gained_regens", used), true);
            } else {
                if (!world.isClientSide) {
                    setOpen(stack, true);
                    PlayerUtil.sendMessage(player, Component.translatable("regen.messages.now_timelord"), true);
                }
            }

            if (!world.isClientSide()) {
                ServerLevel serverWorld = (ServerLevel) world;
                BlockPos blockPos = player.blockPosition();
                //TODO Custom Particle
                serverWorld.sendParticles(ParticleTypes.ANGRY_VILLAGER, blockPos.getX(), (double) blockPos.getY() + 1D, blockPos.getZ(), 8, 0.5D, 0.25D, 0.5D, 0.0D);
            }

            if (used < 0)
                Regeneration.LOGGER.warn(player.getName().getString() + ": Fob watch used <0 regens (supply: " + supply + ", needed:" + needed + ", used:" + used + ", capacity:" + getMaxDamage() + ", damage:" + stack.getDamageValue()+ ", regens:" + cap.regens());

            stack.setDamageValue(stack.getDamageValue() + used);

            if (world.isClientSide) {
                ClientUtil.playPositionedSoundRecord(RSounds.FOB_WATCH.get(), 1.0F, 2.0F);
            } else {
                setOpen(stack, true);
                cap.addRegens(used);
            }

        } else { // transferring player->watch
            if (!cap.canRegenerate()) return msgUsageFailed(player, "regen.messages.transfer.no_regens", stack);

            if (cap.regenState() != RegenStates.ALIVE) {
                return msgUsageFailed(player, "regen.messages.not_alive", stack);
            }

            if (stack.getDamageValue() == 0)
                return msgUsageFailed(player, "regen.messages.transfer.full_watch", stack);

            stack.setDamageValue(stack.getDamageValue() - 1);
            PlayerUtil.sendMessage(player, "regen.messages.transfer.success", true);

            if (world.isClientSide) {
                ClientUtil.playPositionedSoundRecord(SoundEvents.FIRE_EXTINGUISH, 5.0F, 2.0F);
            } else {
                setOpen(stack, true);
                cap.extractRegens(1);
            }

        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }


    private InteractionResultHolder<ItemStack> msgUsageFailed(Player player, String message, ItemStack stack) {
        PlayerUtil.sendMessage(player, message, true);
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, @NotNull ItemStack repair) {
        return false;
    }


    @Override
    public boolean canBeDepleted() {
        return super.canBeDepleted();
    }

    @Override
    public boolean shouldOverrideMultiplayerNbt() {
        return true;
    }

}