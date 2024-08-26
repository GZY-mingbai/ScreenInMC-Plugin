package cn.mingbai.ScreenInMC.Utils.CraftUtils;

import cn.mingbai.ScreenInMC.Utils.LangUtils;

public interface NMSBook {
    void setTitle(String title) ;
    void setGeneration(int generation) ;
    void setResolved(boolean resolved) ;
    void setContent(LangUtils.JsonText[] content);
    void setAuthor(String author);
    void setName(LangUtils.JsonText name);
    Object getItemStack();
    static NMSBook create(){
        if(NMSItemStack.newVersionItem){
            return new NMSBookNew();
        }
        return new NMSBookOld();
    }
}
