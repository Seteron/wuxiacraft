package wuxiacraft.cultivation;

import net.minecraft.entity.LivingEntity;
import wuxiacraft.cultivation.skill.Skill;
import wuxiacraft.cultivation.technique.Technique;
import wuxiacraft.cultivation.technique.TechniqueModifiers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface ICultivation {

	int getTickerTime();

	void advanceTimer();

	void advanceRank(CultivationLevel.System system);

	void addBaseToSystem(double amount, CultivationLevel.System system);

	double getHP();

	void setHP(double HP);

	@Nonnull
	SystemStats getStatsBySystem(CultivationLevel.System system);

	@Nullable
	KnownTechnique getTechniqueBySystem(CultivationLevel.System system);

	void addTechnique(Technique technique);

	void setKnownTechnique(Technique technique, double proficiency);

	double getResistanceToElement(Element element);

	double getHealingCost();

	double getBodyEnergyRegen();

	double getDivineEnergyRegen();

	double getEssenceEnergyRegen();

	double getHealingAmount();

	TechniqueModifiers getFinalModifiers();

	double getMaxBodyEnergy();

	double getMaxDivineEnergy();

	double getMaxEssenceEnergy();

	double getMovementSpeed();

	void setMovementSpeed(double movementSpeed);

	double getBreakSpeed();

	void setBreakSpeed(double breakSpeed);

	double getJumpSpeed();

	void setJumpSpeed(double jumpSpeed);

	/**
	 * @return the increment in stepHeight
	 */
	double getStepHeight();

	/**
	 * sets the stepHeight for a player
	 * @param stepHeight the increment in stepHeight
	 */
	void setStepHeight(double stepHeight);

	/**
	 * @return the walking speed added in blocks per second capped at 3.5
	 */
	double getWalkingSpeed();

	/**
	 * @return the running speed added in blocks per second capped at 8.5
	 */
	double getRunningSpeed();

	boolean isPowerWalk();

	void setPowerWalk(boolean powerWalk);

	abstract void copyFrom(ICultivation cultivation);

	double getEssenceModifier();

	double getBodyModifier();

	double getDivineModifier();

	List<Skill> getKnownSkills();

	void addKnownSkill(Skill skill);

	boolean removeKnownSkill(Skill skill);

	void resetKnownSkills();


	List<Integer> getSelectedSkills();

	double getSkillCooldown();

	void setSkillCooldown(double skillCooldown);

	List<Skill> getAllKnownSkills();

	Skill getActiveSkill(int id);

	void calculateFinalModifiers();

	void resetTickerTimer();

	double getCastSpeed();

	void lowerCoolDown();

	boolean isMeditating();

	void setMeditating(boolean meditating);

	boolean isExercising();

	void setExercising(boolean exercising);

	float getExerciseAnimation();

	void setExerciseAnimation(float exerciseAnimation);
}
