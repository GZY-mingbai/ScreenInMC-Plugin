import cn.mingbai.ScreenInMC.BrowserCoreInitializations.ChromiumCoreInitialization;
import cn.mingbai.ScreenInMC.Main;
import cn.mingbai.ScreenInMC.Natives.GPUDither;
import cn.mingbai.ScreenInMC.Utils.ImageUtils;
import cn.mingbai.ScreenInMC.Utils.LangUtils;
import cn.mingbai.ScreenInMC.Utils.Utils;

import java.util.ArrayList;

public class test {
    public static void main(String[] args) throws Exception{
        int x=1;
        int y=1;
        int w=257;
        int h = 64;
        int a = x/128;
        int b = y/128;
        int c = x%128;
        int d = y%128;
        int e,f;
        if(w+c<128){
            e=w;
        }else{
            e=128-c;
        }
        if(h+d<128){
            f=h;
        }else{
            f=128-d;
        }
        byte[] r1 = new byte[e*f];
        for(int i=0;i<f;i++){
//            System.arraycopy(colors, i * w, r1, i * e, e);
        }
        int k = (c+w)/128+1;
        int l = (d+h)/128+1;
        for(int i=0;i<l;i++){
            for(int j=0;j<k;j++){
                if(i==0&&j==0){
                    continue;
                }
                int m,n,o,p,q,s;
                if(j==0){
                    m=c;
                }else{
                    m=0;
                }
                if(i==0){
                    n=d;
                }else{
                    n=0;
                }
                if(j==0){
                    if(k==1){
                        o=w;
                    }else{
                        o=128-m;
                    }
                } else if (j==k-1) {
                    o=w-e-(k-2)*128;
                } else {
                    o=128;
                }
                if(i==0){
                    if(l==1){
                        p=h;
                    }else{
                        p=128-n;
                    }
                } else if (i==l-1) {
                    p=h-f-(l-2)*128;
                } else {
                    p=128;
                }
                if(j==0){
                    q=0;
                }else{
                    q=e+(j-1)*128;
                }
                if(i==0){
                    s=0;
                }else{
                    s=f+(i-1)*128;
                }
                byte[] r2 = new byte[o*p];
                if(r2.length==0){
                    continue;
                }
//                for(int aaa=0;aaa<r2.length;aaa++){
//                    r2[aaa]=16;
//                }
                for(int r=0;r<p;r++){
//                    System.arraycopy(colors, (r+s) * w+q, r2, r * o, o);
                }
                System.out.println((a+j)+" "+(b+i)+" "+m+" "+n+" "+o+" "+p);
            }
        }
    }
}
