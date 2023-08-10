package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Utils.LangUtils;

import java.util.List;

public class NMSBook extends NMSItemStack{
    public NMSBook() {
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

    @Override
    protected void setNbt(Object nbt) throws Exception {
        super.setNbt(nbt);
        Object authorNbt = NBTTagStringClassConstructor.newInstance(author);
        Object generationNbt = NBTTagIntClassConstructor.newInstance(generation);
        Object titleNbt = NBTTagStringClassConstructor.newInstance(title);
        Object resolvedNbt = NBTTagByteClassConstructor.newInstance((byte)(resolved?1:0));
        NBTTagCompoundPut.invoke(nbt,"author",authorNbt);
        NBTTagCompoundPut.invoke(nbt,"generation",generationNbt);
        NBTTagCompoundPut.invoke(nbt,"title",titleNbt);
        NBTTagCompoundPut.invoke(nbt,"resolved",resolvedNbt);

        Object pagesNbt = NBTTagListClassConstructor.newInstance();
        List list = (List) NBTTagListNBTBaseList.get(pagesNbt);
        for(LangUtils.JsonText i:content){
            Object singleLoreNbt = NBTTagStringClassConstructor.newInstance(i.toJSONWithoutExtra());
            list.add(singleLoreNbt);
        }
        NBTTagCompoundPut.invoke(nbt,"pages",pagesNbt);
    }
}
