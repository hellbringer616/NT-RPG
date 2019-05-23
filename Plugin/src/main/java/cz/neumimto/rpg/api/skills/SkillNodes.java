package cz.neumimto.rpg.api.skills;

import cz.neumimto.rpg.scripting.JsBinding;
import cz.neumimto.rpg.skills.ISkillNode;

/**
 * Created by ja on 22.10.2016.
 */
@JsBinding(JsBinding.Type.CLASS)
public enum SkillNodes implements ISkillNode {

	DAMAGE("damage"),
	RADIUS("radius"),
	MANACOST("manacost"),
	COOLDOWN("cooldown"),
	VELOCITY("velocity"),
	HPCOST("hpcost"),
	PROJECTILE_TYPE("projectile-type"),
	RANGE("range"),
	DURATION("duration"),
	AMOUNT("amount"),
	PERIOD("period"),
	CHANCE("chance"),
	MULTIPLIER("multiplier"),
	HEALED_AMOUNT("healed_amount"),
	AMPLIFIER("amplifier");

	private final String str;

	SkillNodes(String str) {
		this.str = str;
	}

	@Override
	public String value() {
		return str;
	}


}