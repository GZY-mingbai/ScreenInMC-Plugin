#include<freerdp/freerdp.h>
#include<string>
#include<thread>
#include<iostream>
#include<freerdp/cache/bitmap.h>
#include<freerdp/update.h>

using namespace std;
class RDPConnector{
    private:
        freerdp* instance;
        const char* addr;
        int port;
        thread* thread;
        const char* username;
        const char* password;
        static bool startedWSA;
    public:
        RDPConnector(const char* addr,int port,const char* username,const char* password);
        void connect();
        ~RDPConnector();
};
BOOL freerdp_pre_connect(freerdp* instance);
BOOL freerdp_authenticate(freerdp* instance, char** username, char** password, char** domain);
int main();
BOOL freerdp_onpaint(rdpContext* context, const SURFACE_BITS_COMMAND* surfaceBitsCommand);

typedef struct freerdp_bitmap {
} freerdp_bitmap;
BOOL freerdp_bitmap_new(rdpContext* context, rdpBitmap* bitmap);
BOOL freerdp_bitmap_paint(rdpContext* context, rdpBitmap* bitmap);
void freerdp_bitmap_free(rdpContext* context, rdpBitmap* bitmap);
BOOL freerdp_bitmap_setsurface(rdpContext* context, rdpBitmap* bitmap, BOOL primary);

BOOL update_begin_paint(rdpContext* context);
BOOL update_end_paint(rdpContext* context);