package net.wtworkshop.gametest;
import net.wtworkshop.compat.*;
public final class ViewerCaptureJei {
 public static boolean ready(){return WorkshopJei.runtime!=null&&WorkshopJei.registeredCount>0;}
 public static void open(ViewerRecipes.Kind kind){WorkshopJei.runtime.getRecipesGui().showTypes(java.util.List.of(WorkshopJei.TYPES.get(kind)));}
}
