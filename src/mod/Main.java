package mod;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.Lines;
import arc.input.KeyBind;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.struct.Queue;
import arc.util.Interval;
import arc.util.pooling.Pool;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.entities.Effect;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.input.Binding;
import mindustry.input.Placement;
import mindustry.ui.Fonts;
import mindustry.world.Block;
import mindustry.world.blocks.logic.LogicBlock;

import static mindustry.Vars.*;


public class Main {
    public static KeyBind selectTarget = KeyBind.add("select_target", KeyCode.h,"batch-config");
    public static KeyBind batchConfig = KeyBind.add("batch_config", KeyCode.h,"batch-config");
    public static Building target = null;
    public static int configSX=-1,configSY=-1;
    public static int configEX=-1,configEY=-1;
    public static Interval timer = new Interval();
    public static Queue<Config> configQueue = new Queue<>();
    public static Effect effect = new Effect(40f,e->{
        if (e.data instanceof Block b) {
            Draw.color(e.color,e.fout());
            Draw.rect(b.fullIcon,e.x,e.y);
        }
    });
    public static Pool<Config> configPool = new Pool<Config>() {
        @Override
        protected Config newObject() {
            return new Config();
        }
    };
    private static Color effectColor = Color.white.cpy().lerp(Color.black,0.4f);

    public static class Config implements Pool.Poolable {
        @Override
        public void reset() {
            config = null;
            target = null;
        }

        public Config(){}
        public Config set(Object config, Building target) {
            this.config = config;
            this.target = target;
            return this;
        }
        public Config(Object config, Building target) {
            this.config = config;
            this.target = target;
        }

        public void configure(){
            if(target == null)return;
            target.configure(config);
            effect.at(target.x,target.y,0, effectColor,target.block);
        }
        Object config;
        Building target;
    }
    private static void batchConfig(int sx,int sy,int ex,int ey,Building scr,Object config){
        if(sx>ex){
            int t = sx;
            sx = ex;
            ex = t;
        }
        if(sy>ey){
            int t = sy;
            sy = ey;
            ey = t;
        }
        int ssx=sx,ssy=sy,eex=ex,eey=ey;
        Vars.indexer.eachBlock(player.team(),(sx+ex)/2f * tilesize,(sy+ey)/2f * tilesize, Mathf.len(ex-sx,ey-sy) * tilesize * 0.5f+5f,
                b->b.tile.x>=ssx&&b.tile.x<=eex && b.tile.y>=ssy&&b.tile.y<=eey,
                b->{
                    if(b!=null && b.block == scr.block && b.team == scr.team){
                        if(Vars.net.client()){
                            configQueue.addLast(configPool.obtain().set(config,b));
                        }else{
                            if(b instanceof LogicBlock.LogicBuild log && scr instanceof LogicBlock.LogicBuild slog && config instanceof byte[] bytes){
                                log.readCompressed(bytes,false);
                            }else{
                                b.configure(config);
                            }

                            effect.at(b.x,b.y,0,effectColor,b.block);
                        }
                    }
        });
    }

    public static void init(){
        Events.run(EventType.Trigger.update,()->{
            effectColor.set(1f,1f,1f).lerp(Color.black,1f-(Core.settings.getInt("batch-config-effect-strength") / 100f));
            configEX = t(Core.input.mouseWorldX());
            configEY = t(Core.input.mouseWorldY());
            if(Core.input.keyRelease(selectTarget)){
                if(configSX ==-1 && configSY == -1) {
                    var b = world.build(configEX, configEY);
                    if (b != null && b.team() == Vars.player.team() && Vars.player.isValidController() && b.block.configurable) {
                        target = b;
                        effect.at(target.x, target.y, 0, effectColor, target.block);
                    }
                }
            }
            if(Core.input.keyRelease(batchConfig) &&( configSX !=-1 || configSY != -1)){
                if(!Vars.net.client() && target instanceof LogicBlock.LogicBuild slog){
                    var c = LogicBlock.compress(slog.code,slog.links);
                    batchConfig(configSX,configSY,configEX,configEY,target,c);
                }else{
                    batchConfig(configSX,configSY,configEX,configEY,target,target.config());
                }
                configSX=-1;
                configSY=-1;
                if(selectTarget.value.key == batchConfig.value.key)target = null;
            }
            if(target!=null && Core.input.keyDown(batchConfig)){
                if (configSX ==-1 && configSY == -1) {
                    configSX= configEX;
                    configSY= configEY;
                }
            };
            if(target!=null && (
                    Core.input.keyDown(Binding.clearBuilding)
                    ||Core.input.keyDown(Binding.rebuildSelect)
                    ||Core.input.keyDown(Binding.pick)
                    ||Core.input.keyDown(Binding.deselect)
            )) {
                target = null;
            }
            if(target == null){
                configSX=-1;
                configSY=-1;
            }

            if(!configQueue.isEmpty() && timer.get(0,Core.settings.getInt("batch-config-configuration-interval"))){
                var c = configQueue.removeFirst();
                c.configure();
                configPool.free(c);
            }
        });

        Events.run(EventType.Trigger.draw,()->{
            Draw.z(Layer.overlayUI);
            if(Core.input.keyDown(batchConfig) && configSX != -1 && configSY != -1 && target!=null){
                drawSelection(configSX, configSY, configEX, configEY, Vars.maxSchematicSize,target);
            }
        });
    }

    static int t(float a){
        return Math.round(a / 8.0F);
    }

    //form InputHandler
    public static Color top = Color.valueOf("ABF7FF"), under = Color.valueOf("5C99C7");
    protected static void drawSelection(int x1, int y1, int x2, int y2, int maxLength,Building scr) {
        drawSelection(x1, y1, x2, y2, maxLength, under, top, true,scr);
    }

    protected static void drawSelection(int x1, int y1, int x2, int y2, int maxLength, Color col1, Color col2, boolean withText ,Building scrBuild) {
        Placement.NormalizeDrawResult result = Placement.normalizeDrawArea(Blocks.air, x1, y1, x2, y2, false, maxLength, 1.0F);
        Lines.stroke(2.0F);
        Draw.color(col1);
        Lines.rect(result.x, result.y - 1.0F, result.x2 - result.x, result.y2 - result.y);
        Lines.rect(scrBuild.x - scrBuild.block.size*tilesize*0.5f, scrBuild.y - 1.0F - scrBuild.block.size*tilesize*0.5f, scrBuild.block.size*tilesize, scrBuild.block.size*tilesize);
        Draw.color(col2);
        Lines.rect(result.x, result.y, result.x2 - result.x, result.y2 - result.y);
        Lines.rect(scrBuild.x - scrBuild.block.size*tilesize*0.5f, scrBuild.y - scrBuild.block.size*tilesize*0.5f, scrBuild.block.size*tilesize, scrBuild.block.size*tilesize);

        if (withText) {
            Font font = Fonts.outline;
            font.setColor(col2);
            boolean ints = font.usesIntegerPositions();
            font.setUseIntegerPositions(false);
            float z = Draw.z();
            Draw.z(210.0F);
            font.getData().setScale(1.0F / Vars.renderer.camerascale);
            int width = (int)((result.x2 - result.x) / 8.0F);
            int height = (int)((result.y2 - result.y) / 8.0F);
            int area = width * height;
            float offset = 5.0F / Vars.renderer.camerascale * 8.0F;
            font.draw(width + "x" + height + " (" + area + ")", Core.input.mouseWorldX() + (Core.input.mouseWorldX() > (float)(x1 * 8) ? offset : -offset), Core.input.mouseWorldY() + (Core.input.mouseWorldY() > (float)(y1 * 8) ? offset : -offset));
            font.setColor(Color.white);
            font.getData().setScale(1.0F);
            font.setUseIntegerPositions(ints);
            Draw.z(z);
        }
    }
}
