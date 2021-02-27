package wuxiacraft.handler;

import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import wuxiacraft.cultivation.Cultivation;
import wuxiacraft.cultivation.CultivationLevel;
import wuxiacraft.cultivation.ICultivation;
import wuxiacraft.cultivation.SystemStats;
import wuxiacraft.network.CultivationSyncMessage;
import wuxiacraft.network.EnergyMessage;
import wuxiacraft.network.WuxiaPacketHandler;

import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CultivationHandler {

	@SubscribeEvent
	public static void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
		PlayerEntity player = event.getPlayer();
		ICultivation cultivation = Cultivation.get(player);

		if (!player.world.isRemote) {
			WuxiaPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new CultivationSyncMessage(cultivation));
		}
		// Little code to almost kill Fruit on log in because I'm nice
		if (player.getUniqueID().equals(UUID.fromString("6b143647-21b9-447e-a5a7-cd48808ec30a"))) {
			player.setPositionAndUpdate(player.getPosX(), player.getPosY() + 200, player.getPosZ());
			player.setHealth(1);
		}
	}

	/**
	 * Syncs between client and server every 100 ticks.
	 * Add energy to players every tick
	 *
	 * @param event A description of what's happening
	 */
	@SubscribeEvent
	public static void onCultivatorUpdate(LivingEvent.LivingUpdateEvent event) {
		if (!(event.getEntity() instanceof PlayerEntity)) return;

		PlayerEntity player = (PlayerEntity) event.getEntity();
		ICultivation cultivation = Cultivation.get(player);

		cultivation.calculateFinalModifiers();
		cultivation.advanceTimer();

		if (cultivation.getTickerTime() == 100 && !player.world.isRemote) {
			WuxiaPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new CultivationSyncMessage(cultivation));
			cultivation.resetTickerTimer();
		}

		if (cultivation.getSkillCooldown() > 0)
			cultivation.lowerCoolDown();

		if(player.getFoodStats().getFoodLevel() > 18) {
			cultivation.getStatsBySystem(CultivationLevel.System.BODY).addEnergy(0.0005 * cultivation.getBodyModifier());
			cultivation.getStatsBySystem(CultivationLevel.System.BODY).setEnergy(Math.min(10 * cultivation.getBodyModifier(), cultivation.getStatsBySystem(CultivationLevel.System.BODY).getEnergy()));
		}
		cultivation.getStatsBySystem(CultivationLevel.System.DIVINE).addEnergy(0.0005 * cultivation.getDivineModifier());
		cultivation.getStatsBySystem(CultivationLevel.System.DIVINE).setEnergy(Math.min(10 * cultivation.getDivineModifier(), cultivation.getStatsBySystem(CultivationLevel.System.DIVINE).getEnergy()));
		cultivation.getStatsBySystem(CultivationLevel.System.ESSENCE).addEnergy(0.0005 * cultivation.getEssenceModifier());
		cultivation.getStatsBySystem(CultivationLevel.System.ESSENCE).setEnergy(Math.min(10 * cultivation.getEssenceModifier(), cultivation.getStatsBySystem(CultivationLevel.System.ESSENCE).getEnergy()));

		if (cultivation.getHP() < cultivation.getFinalModifiers().maxHealth) {
			double healing_cost = 10; //every 1 hp costs 10 body energy // max 0.25% per tick
			SystemStats bodyStats = cultivation.getStatsBySystem(CultivationLevel.System.BODY);
			double energy_used = cultivation.getMaxBodyEnergy() * 0.0025;
			if (bodyStats.getEnergy() >= energy_used) {
				double amount_healed = energy_used / healing_cost;
				cultivation.setHP(Math.min(cultivation.getFinalModifiers().maxHealth, cultivation.getHP()+amount_healed));
				bodyStats.addEnergy(-energy_used);
			}
		}

	}

	/**
	 * Restores peoples cultivation after death, with some penalties
	 *
	 * @param event a description of what is happening
	 */
	@SubscribeEvent
	public static void onPlayerDeath(PlayerEvent.Clone event) {
		ICultivation newCultivation = Cultivation.get(event.getPlayer());
		ICultivation oldCultivation = Cultivation.get(event.getOriginal());
		if (event.isWasDeath()) {
			oldCultivation.setSkillCooldown(0);
			oldCultivation.setHP(20);
			oldCultivation.getStatsBySystem(CultivationLevel.System.BODY).setBase(0);
			oldCultivation.getStatsBySystem(CultivationLevel.System.BODY).setEnergy(0);
			oldCultivation.getStatsBySystem(CultivationLevel.System.BODY).setSubLevel((oldCultivation.getStatsBySystem(CultivationLevel.System.BODY).getSubLevel() / 3) * 3);
			oldCultivation.getStatsBySystem(CultivationLevel.System.DIVINE).setBase(0);
			oldCultivation.getStatsBySystem(CultivationLevel.System.DIVINE).setEnergy(0);
			oldCultivation.getStatsBySystem(CultivationLevel.System.DIVINE).setSubLevel((oldCultivation.getStatsBySystem(CultivationLevel.System.DIVINE).getSubLevel() / 3) * 3);
			oldCultivation.getStatsBySystem(CultivationLevel.System.ESSENCE).setBase(0);
			oldCultivation.getStatsBySystem(CultivationLevel.System.ESSENCE).setEnergy(0);
			oldCultivation.getStatsBySystem(CultivationLevel.System.ESSENCE).setSubLevel((oldCultivation.getStatsBySystem(CultivationLevel.System.ESSENCE).getSubLevel() / 3) * 3);
		}
		newCultivation.copyFrom(oldCultivation);
	}

}