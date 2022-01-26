package wuxiacraft.init;

import net.minecraftforge.registries.IForgeRegistry;
import wuxiacraft.cultivation.CultivationRealm;
import wuxiacraft.cultivation.CultivationStage;
import wuxiacraft.cultivation.Element;
import wuxiacraft.cultivation.technique.aspects.TechniqueAspect;

public class WuxiaRegistries {

	public static IForgeRegistry<CultivationRealm> CULTIVATION_REALMS;

	public static IForgeRegistry<CultivationStage> CULTIVATION_STAGES;
	public static IForgeRegistry<Element> ELEMENTS;

	public static IForgeRegistry<TechniqueAspect> TECHNIQUE_ASPECT;
}