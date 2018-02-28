package cz.neumimto.rpg;

import cz.neumimto.rpg.effects.model.EffectModelMapper;

import java.util.Map;

/**
 * Created by NeumimTo on 6.1.2018.
 */
public class TestModelMapper extends EffectModelMapper {

    public TestModelMapper(Class<?> t) {
        super(t);
    }

    //todo  asm-fy this later
    public Object parse(Map<String, String> data) {
        TestModel model = new TestModel();
        //model.q = (String) typeMapperMap.get(String.class).apply(data.get("q"));
        model.l = (int) getCache().get(int.class).apply(data.get("l"));
        model.v = (Double) typeMapperMap.get(Double.class).apply(data.get("v"));
        return model;
    }

}
