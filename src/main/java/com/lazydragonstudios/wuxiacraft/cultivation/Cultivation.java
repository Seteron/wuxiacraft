package com.lazydragonstudios.wuxiacraft.cultivation;

import com.lazydragonstudios.wuxiacraft.cultivation.skills.SkillContainer;
import com.lazydragonstudios.wuxiacraft.cultivation.stats.PlayerElementalStat;
import com.lazydragonstudios.wuxiacraft.cultivation.stats.PlayerStat;
import com.lazydragonstudios.wuxiacraft.cultivation.stats.PlayerSystemStat;
import com.lazydragonstudios.wuxiacraft.event.CultivatingEvent;
import com.lazydragonstudios.wuxiacraft.init.WuxiaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import com.lazydragonstudios.wuxiacraft.capabilities.CultivationProvider;
import com.lazydragonstudios.wuxiacraft.cultivation.technique.AspectContainer;
import net.minecraftforge.common.MinecraftForge;

import java.math.BigDecimal;
import java.util.HashMap;

//TODO add a lives counter
public class Cultivation implements ICultivation {

	/**
	 * The cultivation information for each system
	 */
	public HashMap<System, SystemContainer> systemCultivation;

	/**
	 * Player specific stats
	 */
	private final HashMap<PlayerStat, BigDecimal> playerStats;

	private final HashMap<ResourceLocation, HashMap<PlayerElementalStat, BigDecimal>> playerElementalStats;

	/**
	 * this is for sync with the client and probably vice versa
	 * a substitute for this could've been entity.ticksAlive
	 * but that is not among us anymore
	 */
	private int tickTimer;

	/**
	 * this is for the server and client to convert body energy into essence energy
	 */
	private boolean exercising;

	/**
	 * Known Aspects and proficiency
	 */
	public AspectContainer aspects;

	/**
	 * the skill data for this character
	 */
	public SkillContainer skills;

	public Cultivation() {
		this.systemCultivation = new HashMap<>();
		this.playerStats = new HashMap<>();
		this.playerElementalStats = new HashMap<>();
		for (var stat : PlayerStat.values()) {
			this.playerStats.put(stat, stat.defaultValue);
		}
		for (var system : System.values()) {
			SystemContainer systemData = new SystemContainer(system);
			this.systemCultivation.put(system, systemData);
		}
		this.aspects = new AspectContainer();
		this.skills = new SkillContainer();
		this.exercising = false;
	}

	public static ICultivation get(Player target) {
		var cultOpt = target.getCapability(CultivationProvider.CULTIVATION_PROVIDER).resolve();
		return cultOpt.orElseGet(Cultivation::new);
	}

	@Override
	public BigDecimal getPlayerStat(PlayerStat stat) {
		BigDecimal statValue = this.playerStats.getOrDefault(stat, stat.defaultValue);
		for (var system : System.values()) {
			var data = getSystemData(system);
			statValue = statValue.add(data.getPlayerStat(stat));
		}
		return statValue;
	}

	@Override
	public void setPlayerStat(PlayerStat stat, BigDecimal value) {
		if (stat.isModifiable) {
			this.playerStats.put(stat, value.max(BigDecimal.ZERO));
		}
	}

	@Override
	public SystemContainer getSystemData(System system) {
		return systemCultivation.get(system);
	}

	@Override
	public void calculateStats() {
		for (var stat : PlayerStat.values()) {
			if (stat.isModifiable) continue;
			var statValue = stat.defaultValue;
			for (var system : System.values()) {
				statValue = statValue.add(this.getSystemData(system).getPlayerStat(stat));
			}
		}
		this.skills.knownSkills.clear();
		for (var system : System.values()) {
			var systemData = this.getSystemData(system);
			this.skills.knownSkills.addAll(systemData.techniqueData.modifier.skills);
		}
	}

	@Override
	public boolean addCultivationBase(Player player, System system, BigDecimal amount) {
		if (!MinecraftForge.EVENT_BUS.post(new CultivatingEvent(player, system, amount))) return false;
		var systemData = this.getSystemData(system);
		systemData.setStat(PlayerSystemStat.CULTIVATION_BASE, systemData.getStat(PlayerSystemStat.CULTIVATION_BASE).add(amount).max(BigDecimal.ZERO));
		return true;
	}

	@Override
	public CompoundTag serialize() {
		CompoundTag tag = new CompoundTag();
		for (var stat : this.playerStats.keySet()) {
			if (!stat.isModifiable) continue;
			tag.putString("stat-" + stat.name().toLowerCase(), this.playerStats.get(stat).toPlainString());
		}
		var elementStatsTag = new CompoundTag();
		for (var element : this.playerElementalStats.keySet()) {
			var currentElementStatsTag = new CompoundTag();
			for (var stat : PlayerElementalStat.values()) {
				if (!stat.isModifiable) continue;
				currentElementStatsTag.putString("stat-" + stat.name().toLowerCase(),
						this.playerElementalStats.get(element).getOrDefault(stat, BigDecimal.ZERO).toPlainString());
			}
			elementStatsTag.put("element-stats-" + element, currentElementStatsTag);
		}
		tag.put("elemental-stats", elementStatsTag);
		tag.put("body-data", getSystemData(System.BODY).serialize());
		tag.put("divine-data", getSystemData(System.DIVINE).serialize());
		tag.put("essence-data", getSystemData(System.ESSENCE).serialize());
		tag.put("aspect-data", this.aspects.serialize());
		tag.put("skills-data", this.skills.serialize());
		return tag;
	}

	@Override
	public void deserialize(CompoundTag tag) {
		for (var stat : this.playerStats.keySet()) {
			if (!stat.isModifiable) continue;
			if (tag.contains("stat-" + stat.name().toLowerCase())) {
				this.playerStats.put(stat, new BigDecimal(tag.getString("stat-" + stat.name().toLowerCase())));
			} else {
				this.playerStats.put(stat, new BigDecimal("0"));
			}
		}
		if (tag.contains("elemental-stats")) {
			var rawElementalStatsTag = tag.get("elemental-stats");
			if (rawElementalStatsTag instanceof CompoundTag elementalStatsTag) {
				for (var element : WuxiaRegistries.ELEMENTS.getKeys()) {
					if (elementalStatsTag.contains("element-stats-" + element)) {
						for (var stat : PlayerElementalStat.values()) {
							if (!stat.isModifiable) continue;
							if (elementalStatsTag.contains("stat-" + stat.name().toLowerCase())) {
								var value = elementalStatsTag.getString("stat-" + stat.name().toLowerCase());
								this.playerElementalStats.putIfAbsent(element, new HashMap<>());
								this.playerElementalStats.get(element).put(stat, new BigDecimal(value));
							}
						}
					}
				}
			}
		}
		if (tag.contains("body-data")) {
			getSystemData(System.BODY).deserialize(tag.getCompound("body-data"), this);
		}
		if (tag.contains("divine-data")) {
			getSystemData(System.DIVINE).deserialize(tag.getCompound("divine-data"), this);
		}
		if (tag.contains("essence-data")) {
			getSystemData(System.ESSENCE).deserialize(tag.getCompound("essence-data"), this);
		}
		if (tag.contains("aspect-data")) {
			this.aspects.deserialize(tag.getCompound("aspect-data"));
		}
		if (tag.contains("skills-data")) {
			this.skills.deserialize(tag.getCompound("skills-data"));
		}
		calculateStats();
	}

	@Override
	public boolean isExercising() {
		return exercising;
	}

	@Override
	public void setExercising(boolean exercising) {
		this.exercising = exercising;
	}

	@Override
	public AspectContainer getAspects() {
		return aspects;
	}

	@Override
	public SkillContainer getSkills() {
		return skills;
	}

	/**
	 * Utility to increment to the tick timer
	 */
	@Override
	public void advanceTimer() {
		this.tickTimer++;
	}

	/**
	 * Utility to reset timer.
	 * Should only be used when a sync message is sent
	 */
	@Override
	public void resetTimer() {
		this.tickTimer = 0;
	}

	/**
	 * @return the time ticker. It's just for not exposing the ticker.
	 */
	@Override
	public int getTimer() {
		return this.tickTimer;
	}

}
