package net.wtworkshop.tool;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.wtworkshop.WorkshopConfig;
@EventBusSubscriber(modid="wt_workshop")
public final class SiphoningEvents {
 private record Pending(DamageSource source,ServerPlayer player,int level,float healthBefore){}
 private static final Map<LivingEntity,Pending> PENDING=new WeakHashMap<>();
 private static final String LAST_HEAL="wt_workshop_siphoning_tick";
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void beforeDamage(LivingDamageEvent.Pre event){
  var target=event.getEntity();if(target.level().isClientSide)return;PENDING.remove(target);
  var source=event.getSource();if(!(source.getEntity() instanceof ServerPlayer player)||source.getDirectEntity()!=player||!source.is(DamageTypes.PLAYER_ATTACK)||target==player)return;
  if(target instanceof Player&&!WorkshopConfig.siphoningPvp())return;
  var stack=player.getMainHandItem();if(!(stack.getItem() instanceof WorkshopTool tool)||!WorkshopTool.usable(stack)||!Modifier.SIPHONING.allowedOn(tool.type))return;
  int level=Math.min(WorkshopTool.data(stack).modifierLevel(Modifier.SIPHONING),WorkshopConfig.siphoningMaxLevel());
  if(level>0)PENDING.put(target,new Pending(source,player,level,target.getHealth()));
 }
 @SubscribeEvent public static void afterDamage(LivingDamageEvent.Post event){
  if(event.getEntity().level().isClientSide)return;var pending=PENDING.remove(event.getEntity());if(pending==null||pending.source()!=event.getSource())return;
  float lost=Math.min(Math.max(0,pending.healthBefore()-event.getEntity().getHealth()),Math.max(0,event.getNewDamage()));
  var player=pending.player();if(lost<=0||!player.isAlive()||player.getHealth()>=player.getMaxHealth())return;
  long now=player.level().getGameTime();var tag=player.getPersistentData();if(tag.contains(LAST_HEAL)){long last=tag.getLong(LAST_HEAL);if(now>=last&&now-last<WorkshopConfig.siphoningCooldown())return;}
  float heal=Math.min(WorkshopConfig.siphoningCap(),lost*pending.level()*WorkshopConfig.siphoningFraction());
  if(heal>0){player.heal(heal);tag.putLong(LAST_HEAL,now);}
 }
}
