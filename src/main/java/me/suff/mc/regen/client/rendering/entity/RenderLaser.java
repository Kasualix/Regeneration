package me.suff.mc.regen.client.rendering.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.suff.mc.regen.common.entities.LaserProjectile;
import me.suff.mc.regen.util.RenderHelp;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Vector3f;

/* Created by Craig on 01/03/2021 */
public class RenderLaser extends EntityRenderer<LaserProjectile> {

    public RenderLaser(EntityRenderDispatcher renderManager) {
        super(renderManager);
    }

    @Override
    public void render(LaserProjectile entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLightIn) {
        matrixStack.pushPose();
        Vec3 vec1 = new Vec3(entity.xOld, entity.yOld, entity.zOld);
        Vec3 vec2 = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        vec1 = vec2.subtract(vec1);
        vec2 = vec2.subtract(vec2);
        vec1 = vec1.normalize();
        double x_ = vec2.x - vec1.x;
        double y_ = vec2.y - vec1.y;
        double z_ = vec2.z - vec1.z;
        double diff = Mth.sqrt(x_ * x_ + z_ * z_);
        float yaw = (float) (Math.atan2(z_, x_) * 180.0D / 3.141592653589793D) - 90.0F;
        float pitch = (float) -(Math.atan2(y_, diff) * 180.0D / 3.141592653589793D);
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(-yaw));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(pitch));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        VertexConsumer vertexBuilder = bufferIn.getBuffer(RenderType.lightning());
        RenderHelp.drawGlowingLine(matrixStack.last().pose(), vertexBuilder, 1F, 0.05F, 0, 0, 1, 1F, 15728640);
        matrixStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(LaserProjectile entity) {
        return null;
    }


}
