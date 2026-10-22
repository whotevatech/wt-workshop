package net.wtworkshop.tool;
import net.minecraft.world.item.ItemStack;
/** Stable client appearance key. Partial modifier progress deliberately adds no visual. */
public final class ModifierAppearance {
 private ModifierAppearance(){}
 public static final java.util.List<Modifier> STYLES=java.util.List.of(Modifier.HASTE,Modifier.LUCK,Modifier.DIAMOND,Modifier.SHARPNESS,Modifier.REINFORCED,Modifier.FIERY,Modifier.SIPHONING);
 public static int flags(ItemStack stack){var d=WorkshopTool.data(stack);if(d==null)return 0;int flags=0;for(int i=0;i<STYLES.size();i++)if(d.modifierLevel(STYLES.get(i))>0)flags|=1<<i;return flags;}
}
