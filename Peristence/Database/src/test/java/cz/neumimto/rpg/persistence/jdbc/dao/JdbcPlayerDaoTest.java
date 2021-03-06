package cz.neumimto.rpg.persistence.jdbc.dao;

import cz.neumimto.persistence.TestHelper;
import cz.neumimto.rpg.api.RpgTests;
import cz.neumimto.rpg.api.logging.Log;
import cz.neumimto.rpg.api.persistance.model.CharacterBase;
import cz.neumimto.rpg.api.persistance.model.CharacterClass;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;

import java.util.Optional;

public class JdbcPlayerDaoTest {

    private static JdbcPlayerDao jdbcPlayerDao;

    @BeforeAll
    public static void before() {
        new RpgTests();
        Logger logger = org.slf4j.LoggerFactory.getLogger(JdbcPlayerDaoTest.class);
        Log.setLogger(logger);
        try {
            MariaDbBootstrap.runMigrations();
            jdbcPlayerDao = new JdbcPlayerDao().setDataSource(MariaDbBootstrap.ds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void stop() {
        MariaDbBootstrap.tearDown();
    }

    @Test
    @Disabled
    public void test() {
        CharacterBase characterBase = TestHelper.createCharacterBase();
        CharacterBase loadded = null;
        try {
            jdbcPlayerDao.create(characterBase);
            loadded = jdbcPlayerDao.getCharacter(characterBase.getUuid(), characterBase.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assertions.assertNotNull(characterBase.getId());
        Assertions.assertNotNull(loadded);

        Assertions.assertEquals(characterBase.getAttributePoints(), loadded.getAttributePoints());
        Assertions.assertEquals(characterBase.getAttributePointsSpent(), loadded.getAttributePointsSpent());
        Assertions.assertEquals(characterBase.getAttributes(), loadded.getAttributes());
        Assertions.assertEquals(characterBase.getBaseCharacterAttribute(), loadded.getBaseCharacterAttribute());
        Assertions.assertEquals(characterBase.getCharacterClasses(), loadded.getCharacterClasses());
        Assertions.assertEquals(characterBase.getCharacterSkills(), loadded.getCharacterSkills());
        Assertions.assertEquals(characterBase.getHealthScale(), loadded.getHealthScale());
        Assertions.assertEquals(characterBase.getId(), loadded.getId());

        Assertions.assertEquals(characterBase.getLastKnownPlayerName(), loadded.getLastKnownPlayerName());
        Assertions.assertEquals(characterBase.getMarkedForRemoval(), loadded.getMarkedForRemoval());
        Assertions.assertEquals(characterBase.getName(), loadded.getName());
        Assertions.assertEquals(characterBase.getUuid(), loadded.getUuid());
        Assertions.assertEquals(characterBase.getWorld(), loadded.getWorld());
        Assertions.assertEquals(characterBase.getX(), loadded.getX());
        Assertions.assertEquals(characterBase.getY(), loadded.getY());
        Assertions.assertEquals(characterBase.getZ(), loadded.getZ());
        if (characterBase.getInventoryEquipSlotOrder() != null) {
            Assertions.assertEquals(characterBase.getInventoryEquipSlotOrder().size(), loadded.getInventoryEquipSlotOrder().size());
            for (int i = 0; i < characterBase.getInventoryEquipSlotOrder().size(); i++) {
                Assertions.assertEquals(characterBase.getInventoryEquipSlotOrder().get(i).getSlotIndex(), loadded.getInventoryEquipSlotOrder().get(i).getSlotIndex());
            }
        }

        TestHelper.addClasses(characterBase);
        jdbcPlayerDao.update(characterBase);
        loadded = jdbcPlayerDao.getCharacter(characterBase.getUuid(), characterBase.getName());
        for (CharacterClass characterClass : characterBase.getCharacterClasses()) {
            Optional<CharacterClass> first = loadded.getCharacterClasses().stream().filter(a -> a.getName().equals(characterClass.getName())).findFirst();
            if (!first.isPresent()) {
                throw new IllegalStateException("");
            }
            CharacterClass loadedClass = first.get();

            Assertions.assertNotNull(loadedClass.getId());

            Assertions.assertEquals(characterClass.getCharacterBase().getId(), loadedClass.getCharacterBase().getId());
            Assertions.assertEquals(characterClass.getExperiences(), loadedClass.getExperiences());
            Assertions.assertEquals(characterClass.getLevel(), loadedClass.getLevel());
            Assertions.assertEquals(characterClass.getSkillPoints(), loadedClass.getSkillPoints());
            Assertions.assertEquals(characterClass.getUsedSkillPoints(), loadedClass.getUsedSkillPoints());
        }
    }
}
