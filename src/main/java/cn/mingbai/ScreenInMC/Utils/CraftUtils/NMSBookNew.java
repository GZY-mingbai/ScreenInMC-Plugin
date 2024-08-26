package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Utils.LangUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NMSBookNew extends NMSItemStackNew implements NMSBook{
    public NMSBookNew() {
        super(new String[]{"written_book"}, 1);
    }
    private LangUtils.JsonText[] content;
    private String author;
    private String title;
    private int generation=0;
    private boolean resolved=true;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public void setContent(LangUtils.JsonText[] content){
        this.content = content;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
    static Class FilterableClass;
    static Constructor FilterableConstructor;
    static Class WrittenBookContentClass;
    static Constructor WrittenBookContentConstructor;
    static Object WrittenBookContentDataComponentType;
    public static void init() throws Exception{
        FilterableClass = CraftUtils.getMinecraftClass("Filterable");
        FilterableConstructor = FilterableClass.getDeclaredConstructor(Object.class, Optional.class);
        WrittenBookContentClass = CraftUtils.getMinecraftClass("WrittenBookContent");
        WrittenBookContentConstructor = WrittenBookContentClass.getDeclaredConstructor(FilterableClass,String.class,int.class,List.class,boolean.class);
        WrittenBookContentDataComponentType = NMSItemStackNew.getDataComponentType("written_book_content");
    }
    @Override
    protected void setNbtNew(Object itemStack) {
        super.setNbtNew(itemStack);
        try {
            List contents = new ArrayList();
            for(LangUtils.JsonText i : content){
                Object c = i.toComponent();
                contents.add(FilterableConstructor.newInstance(c,Optional.of(c)));
            }
            Object patchedDataComponentMap = DataComponentHolderGetComponents.invoke(itemStack);
            PatchedDataComponentMapSet.invoke(patchedDataComponentMap,WrittenBookContentDataComponentType,WrittenBookContentConstructor.newInstance(
                    FilterableConstructor.newInstance(title,Optional.of(title)),
                    author,
                    generation,
                    contents,
                    resolved
            ));
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
