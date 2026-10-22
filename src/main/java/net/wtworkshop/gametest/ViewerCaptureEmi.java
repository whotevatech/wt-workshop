package net.wtworkshop.gametest;
import net.wtworkshop.compat.*;
public final class ViewerCaptureEmi {
 public static boolean ready(){if(WorkshopEmi.registeredCount==0||dev.emi.emi.api.EmiApi.getRecipeManager().getRecipes().stream().noneMatch(r->r instanceof WorkshopEmi.Recipe))return false;long n=dev.emi.emi.api.EmiApi.getRecipeManager().getRecipes().stream().filter(r->r.getId()!=null&&r.getId().getNamespace().equals("wt_workshop")&&r.getId().getPath().startsWith("/viewer/")).count();if(n!=WorkshopEmi.registeredCount)throw new IllegalStateException("Duplicate or missing EMI recipes: "+n);return true;}
 public static void open(ViewerRecipes.Kind kind){dev.emi.emi.api.EmiApi.displayRecipeCategory(WorkshopEmi.CATEGORIES.get(kind));}
}
