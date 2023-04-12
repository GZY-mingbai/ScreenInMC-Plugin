#include<freerdp/freerdp.h>
#include<string>
#include<thread>
#include<iostream>
#include<freerdp/cache/bitmap.h>
#include<freerdp/update.h>
#include<freerdp/gdi/gdi.h>

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
BOOL freerdp_post_connect(freerdp* instance);
BOOL freerdp_authenticate(freerdp* instance, char** username, char** password, char** domain);
int main();

BOOL update_begin_paint(rdpContext* context);
BOOL update_end_paint(rdpContext* context);

BOOL bridge_Pointer_New(rdpContext* context, rdpPointer* pointer);
void bridge_Pointer_Free(rdpContext* context, rdpPointer* pointer);
BOOL bridge_Pointer_Set(rdpContext* context,const rdpPointer* pointer);
BOOL bridge_Pointer_SetPosition(rdpContext* context, UINT32 x, UINT32 y);
BOOL bridge_Pointer_SetNull(rdpContext* context);
BOOL bridge_Pointer_SetDefault(rdpContext* context);
static BOOL update_desktop_resize(rdpContext* context);