package com.airesnor.wuxiacraft.handlers;

import com.airesnor.wuxiacraft.WuxiaCraft;
import com.airesnor.wuxiacraft.capabilities.CultivationProvider;
import com.airesnor.wuxiacraft.config.WuxiaCraftConfig;
import com.airesnor.wuxiacraft.cultivation.ICultivation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class RendererHandler {

	public static final ResourceLocation bar_bg = new ResourceLocation(WuxiaCraft.MODID, "textures/gui/overlay/bar_bg.png");
	public static final ResourceLocation energy_bar = new ResourceLocation(WuxiaCraft.MODID, "textures/gui/overlay/energy_bar.png");
	public static final ResourceLocation progress_bar = new ResourceLocation(WuxiaCraft.MODID, "textures/gui/overlay/progress_bar.png");

	@SubscribeEvent
	public void onRenderHud(RenderGameOverlayEvent event) {
		if (event.isCancelable() || event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
			return;
		}
		drawHudElements();
	}

	public static void enableBoxRendering() {
		GlStateManager.disableTexture2D();
		GlStateManager.disableDepth();
		GlStateManager.depthMask( false );
		GlStateManager.blendFunc( GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA );
		GlStateManager.enableBlend();
		GlStateManager.glLineWidth( (float) 2f);
	}

	public static void disableBoxRendering() {
		GlStateManager.disableBlend();
		GlStateManager.enableDepth();
		GlStateManager.depthMask( true );
		GlStateManager.enableTexture2D();
	}

	@SideOnly(Side.CLIENT)
	public void drawHudElements() {
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution res = new ScaledResolution(mc);
		int width = res.getScaledWidth();
		int height = res.getScaledHeight();//res.getScaledWidth();


		EntityPlayer player = mc.world.getPlayerEntityByUUID(mc.player.getUniqueID());
		ICultivation cultivation = player.getCapability(CultivationProvider.CULTIVATION_CAP, null);


		float energy_fill = cultivation.getEnergy() * 100 / cultivation.getCurrentLevel().getMaxEnergyByLevel(cultivation.getCurrentSubLevel());
		float progress_fill = cultivation.getCurrentProgress() * 100 / cultivation.getCurrentLevel().getProgressBySubLevel(cultivation.getCurrentSubLevel());

		int posX = (width)/2;
		int posY = (height)/2;

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_LIGHTING);

		GL11.glPushMatrix();
		GL11.glTranslatef(posX,height, 0F);
		GL11.glScalef(0.3F,0.3F,1F);
		GL11.glTranslatef(-25f,-110f,0f);

		mc.renderEngine.bindTexture(bar_bg);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0,1); GL11.glVertex2f(0,0);
		GL11.glTexCoord2f(1,1); GL11.glVertex2f(50F,0);
		GL11.glTexCoord2f(1,0); GL11.glVertex2f(50,-100);
		GL11.glTexCoord2f(0,0); GL11.glVertex2f(0,-100);
		GL11.glEnd();

		mc.renderEngine.bindTexture(energy_bar);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0,1); GL11.glVertex2f(0,0);
		GL11.glTexCoord2f(1,1); GL11.glVertex2f(50F,0);
		GL11.glTexCoord2f(1,1-(energy_fill/100)); GL11.glVertex2f(50,-(energy_fill));
		GL11.glTexCoord2f(0,1-(energy_fill/100)); GL11.glVertex2f(0,-(energy_fill));
		GL11.glEnd();


		mc.renderEngine.bindTexture(progress_bar);

		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0,1); GL11.glVertex2f(0,0);
		GL11.glTexCoord2f(1,1); GL11.glVertex2f(50F,0);
		GL11.glTexCoord2f(1,1-(progress_fill/100)); GL11.glVertex2f(50,-(progress_fill));
		GL11.glTexCoord2f(0,1-(progress_fill/100)); GL11.glVertex2f(0,-(progress_fill));
		GL11.glEnd();

		GL11.glPopMatrix();

		String message = String.format("Energy: %.0f (%.2f%%)",cultivation.getEnergy(), energy_fill);
		mc.ingameGUI.drawString(mc.fontRenderer, message, 5, 20, Integer.parseInt("FFAA00",16));

		message = String.format("Progress: %.2f (%.2f%%)",cultivation.getCurrentProgress(), progress_fill);
		mc.ingameGUI.drawString(mc.fontRenderer, message, 5, 30, Integer.parseInt("FFAA00",16));

		message = String.format("Player: %s, %s",player.getDisplayNameString(), cultivation.getCurrentLevel().getLevelName(cultivation.getCurrentSubLevel()));
		mc.ingameGUI.drawString(mc.fontRenderer, message, 5, 10, Integer.parseInt("FFAA00",16));

		message = String.format("Speed: %.3f(%.3f->%d%%)",player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue(),cultivation.getCurrentLevel().getSpeedModifierBySubLevel(cultivation.getCurrentSubLevel()), WuxiaCraftConfig.speedHandicap);
		mc.ingameGUI.drawString(mc.fontRenderer, message, 5, 40, Integer.parseInt("FFAA00",16));

		message = String.format("Strength: %.1f(%.3f)",player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue(),cultivation.getCurrentLevel().getStrengthModifierBySubLevel(cultivation.getCurrentSubLevel()));
		mc.ingameGUI.drawString(mc.fontRenderer, message, 5, 50, Integer.parseInt("FFAA00",16));

		message = String.format("Fall Distance: %.2f",player.fallDistance);
		mc.ingameGUI.drawString(mc.fontRenderer, message, 5, 60, Integer.parseInt("FFAA00",16));
	}
}