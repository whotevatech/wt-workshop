package net.wtworkshop.beeblock;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.wtworkshop.tool.*;
@EventBusSubscriber(modid="wt_beeblock_workshop")
public final class HoneyboundEvents {
 private record Hit(DamageSource source,ServerPlayer player,int level,float health){}
 private static final Map<LivingEntity,Hit> HITS=new WeakHashMap<>();
 private static final String LAST="wt_beeblock_workshop_honey_tick";
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void before(LivingDamageEvent.Pre e){var target=e.getEntity();if(target.level().isClientSide)return;HITS.remove(target);var s=e.getSource();if(!(s.getEntity() instanceof ServerPlayer p)||s.getDirectEntity()!=p||!s.is(DamageTypes.PLAYER_ATTACK)||p==target||target instanceof Player&&!BeeWorkshop.pvp())return;var item=p.getMainHandItem();if(!(item.getItem() instanceof WorkshopTool tool)||!WorkshopTool.usable(item)||!(tool.type==ToolType.SWORD||tool.type==ToolType.AXE||tool.type==ToolType.BROAD_AXE))return;int level=Math.min(WorkshopTool.data(item).modifierLevel(BeeWorkshop.HONEYBOUND),BeeWorkshop.honeyMax());if(level>0)HITS.put(target,new Hit(s,p,level,target.getHealth()));}
 @SubscribeEvent public static void after(LivingDamageEvent.Post e){var hit=HITS.remove(e.getEntity());if(hit==null||hit.source()!=e.getSource()||e.getEntity().getHealth()>=hit.health()||!e.getEntity().isAlive())return;var p=hit.player();long now=p.level().getGameTime();var tag=p.getPersistentData();if(tag.contains(LAST)){long last=tag.getLong(LAST);if(now>=last&&now-last<BeeWorkshop.cooldown())return;}if(e.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,BeeWorkshop.duration(),hit.level()-1),p))tag.putLong(LAST,now);}
}
