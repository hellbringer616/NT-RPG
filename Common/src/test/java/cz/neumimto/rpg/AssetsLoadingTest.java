package cz.neumimto.rpg;

import com.google.inject.Injector;
import cz.neumimto.rpg.api.scripting.IScriptEngine;
import cz.neumimto.rpg.api.skills.SkillService;
import cz.neumimto.rpg.api.skills.scripting.ScriptExecutorSkill;
import cz.neumimto.rpg.common.assets.AssetService;
import cz.neumimto.rpg.junit.NtRpgExtension;
import cz.neumimto.rpg.junit.TestGuiceModule;
import name.falgout.jeffrey.testing.junit.guice.GuiceExtension;
import name.falgout.jeffrey.testing.junit.guice.IncludeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleBindings;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

@ExtendWith({GuiceExtension.class, NtRpgExtension.class})
@IncludeModule(TestGuiceModule.class)
public class AssetsLoadingTest {

    @Inject
    private SkillService skillService;

    @Inject
    private IScriptEngine jsLoader;

    @Inject
    private Injector injector;

    @Inject
    private TestApiImpl api;

    @Inject
    private AssetService assetService;

    @BeforeEach
    public void beforeEach() throws Exception {
        new RpgTest(api);
        Path scriptsRootFolder = jsLoader.getScriptsRootFolder();

        scriptsRootFolder.resolve("skillhandlerstest.js").toFile().delete();
        assetService.copyToFile("skillhandlerstest.js", scriptsRootFolder.resolve("skillhandlerstest.js"));

        jsLoader.initEngine();
        Bindings bindings = jsLoader.getEngine().getBindings(ScriptContext.GLOBAL_SCOPE);
        bindings = bindings == null ? new SimpleBindings() : bindings;
        bindings.put("ScriptExecutorSkill", ScriptExecutorSkill.class);
        jsLoader.getEngine().eval("var ScriptExecutorSkill = Java.type(\"" + ScriptExecutorSkill.class.getCanonicalName() + "\")");
        jsLoader.getEngine().setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
        skillService.load();
    }

    @Test
    public void testJSSkillLoading() throws URISyntaxException {
        File file = new File(getClass().getClassLoader().getResource("testconfig/Skills-Definition.conf").getFile());
        jsLoader.loadSkillDefinitionFile(new URLClassLoader(new URL[]{}, this.getClass().getClassLoader()), file);
        Assertions.assertTrue(skillService.getById("ntrpg:jstest").isPresent());
    }
}
