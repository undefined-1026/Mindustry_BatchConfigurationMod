package mod;

import arc.Core;
import arc.Events;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.mod.Mod;

public class BatchConfigurationMod extends Mod {
    public BatchConfigurationMod(){
        Log.info("mod创建.");
    }

    @Override
    public void init() {
        Log.info("mod初始化.");
        Events.on(EventType.ClientLoadEvent.class, e->{
            Core.app.post(()->{
                Vars.ui.settings.addCategory("@settings.batch-configuration-mod", t->{
                    t.sliderPref("batch-config-configuration-interval",10,1,60,3,s->s+" tick");
                    t.sliderPref("batch-config-effect-strength",40,0,100,5,s->s+"%");
                });
                Main.init();
            });
        });
    }
}
