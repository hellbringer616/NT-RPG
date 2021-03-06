package cz.neumimto.rpg.common.skills;

import cz.neumimto.rpg.api.skills.CommandSkill;
import cz.neumimto.rpg.api.skills.PropertySkill;
import cz.neumimto.rpg.api.skills.RepeatingSkill;
import cz.neumimto.rpg.api.skills.WrappedSkill;
import cz.neumimto.rpg.api.skills.tree.SkillTreeSpecialization;
import cz.neumimto.rpg.api.skills.types.CharacterAttributeSkill;
import cz.neumimto.rpg.common.skills.types.ItemAccessSkill;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SkillConfigLoaders {

    static SkillConfigLoader SKILLTREE_PATH = new SkillConfigLoader("specialization", SkillTreeSpecialization.class);
    static SkillConfigLoader ITEM_ACCESS = new SkillConfigLoader("item-access", ItemAccessSkill.class);
    static SkillConfigLoader ATTRIBUTE = new SkillConfigLoader("attribute", CharacterAttributeSkill.class);
    static SkillConfigLoader PROPERTY = new SkillConfigLoader("property", PropertySkill.class);
    static SkillConfigLoader EXECUTE_COMMAND = new SkillConfigLoader("command", CommandSkill.class);
    static SkillConfigLoader WRAPPER = new SkillConfigLoader("wrapper", WrappedSkill.class);
    static SkillConfigLoader REPEATING = new SkillConfigLoader("repeating", RepeatingSkill.class);

    private static Map<String, SkillConfigLoader> internalCache = new HashMap<>();


    static {
        register(SKILLTREE_PATH);
        register(ITEM_ACCESS);
        register(ATTRIBUTE);
        register(PROPERTY);
        register(EXECUTE_COMMAND);
        register(WRAPPER);
        register(REPEATING);
    }

    public static Optional<SkillConfigLoader> getById(String id) {
        return Optional.ofNullable(internalCache.get(id.toLowerCase()));
    }

    public static void register(String id, SkillConfigLoader l) {
        internalCache.put(id.toLowerCase(), l);
    }

    public static void register(SkillConfigLoader l) {
        register(l.getId(), l);
    }
}
