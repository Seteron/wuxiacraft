package com.airesnor.wuxiacraft.networking;

import com.airesnor.wuxiacraft.cultivation.techniques.ICultTech;
import com.airesnor.wuxiacraft.utils.CultivationUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RemoveTechniqueMessageHandler implements IMessageHandler<RemoveTechniqueMessage, IMessage> {
	@Override
	public IMessage onMessage(RemoveTechniqueMessage message, MessageContext ctx) {
		if (ctx.side == Side.SERVER) {
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
				EntityPlayerMP player = ctx.getServerHandler().player;
				ICultTech cultTech = CultivationUtils.getCultTechFromEntity(player);
				cultTech.remTechnique(message.toBeRemoved);
			});
		}
		return null;
	}
}