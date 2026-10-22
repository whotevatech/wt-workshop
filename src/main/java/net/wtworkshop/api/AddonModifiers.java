package net.wtworkshop.api;
import java.util.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.wtworkshop.tool.*;
/** Namespaced metadata registry. Effects may use ordinary NeoForge events. */
public final class AddonModifiers {
 private static final Map<String,AddonModifier> VALUES=new TreeMap<>();
 private static boolean frozen;
 public static synchronized void register(AddonModifier modifier){if(frozen)throw new IllegalStateException("Register modifiers during mod construction");if(modifier.key().getNamespace().equals("minecraft")||modifier.key().getNamespace().equals("wt_workshop"))throw new IllegalArgumentException("Use your own addon namespace");if(VALUES.putIfAbsent(modifier.id(),modifier)!=null)throw new IllegalArgumentException("Duplicate modifier "+modifier.id());}
 public static void freeze(){frozen=true;}
 public static Collection<AddonModifier> values(){return List.copyOf(VALUES.values());}
 public static AddonModifier get(String id){return VALUES.get(id);}
 public static List<AddonModifier> matching(ItemStack stack){return VALUES.values().stream().filter(m->m.valueOf(stack)>0).toList();}
 public static AddonModifier forIngredient(ItemStack stack){AddonModifier found=null;for(var m:VALUES.values())if(m.valueOf(stack)>0){if(found!=null)return null;found=m;}return found;}
 public static List<Component> tooltip(ToolData data,boolean details){var out=new ArrayList<Component>();var ids=new TreeSet<String>(data.modifiers());ids.addAll(data.progress().keySet());for(String id:ids){if(Modifier.byId(id)!=null)continue;var m=get(id);int level=data.modifierLevel(id),progress=data.progressOf(id);if(m==null){out.add(Component.translatable("tooltip.wt_workshop.addon_missing",id));continue;}if(level>0){out.add(m.displayName(level));if(details)out.add(m.description().get());}if(progress>0)out.add(Component.translatable("tooltip.wt_workshop.tool.progress",m.displayName(level+1),progress,m.pointsPerLevel()));}return List.copyOf(out);}
 public static float remainingWearFactor(ToolData data){double remaining=1;for(var m:VALUES.values()){int level=Math.min(data.modifierLevel(m.id()),m.maxLevel());remaining*=1-Math.clamp(level*m.durabilitySavingPerLevel().getAsDouble(),0,0.9);}return(float)remaining;}
}
