package net.wtworkshop.tool;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.wtworkshop.Registration;
import net.wtworkshop.part.ExpansionItem;
public enum SlotExpansion {
 COPPER,GILDED,ASTRAL;
 public String id(){return name().toLowerCase(Locale.ROOT);}
 public int tier(){return ordinal()+1;}
 public Component displayName(){return Component.translatable("item.wt_workshop."+id()+"_expansion_brace");}
 public ItemStack item(){return new ItemStack(Registration.EXPANSIONS.get(this).get());}
 public static SlotExpansion of(ItemStack stack){return stack.getItem() instanceof ExpansionItem item?item.expansion:null;}
}
