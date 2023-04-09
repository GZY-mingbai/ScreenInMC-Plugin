#include "RDPConnector.h"
RDPConnector::RDPConnector(const char* addr,int port,const char* username,const char* password){
    if(!RDPConnector::startedWSA){
        WORD wVersionRequested;
        WSADATA wsaData;
        wVersionRequested = MAKEWORD(2, 2);
        WSAStartup(wVersionRequested, &wsaData);
        RDPConnector::startedWSA=true;
    }
    this->addr = addr;
    this->port = port;
    this->username = username;
    this->password = password;
    this->instance = freerdp_new();
};
RDPConnector::~RDPConnector(){
    if(this->instance!=NULL){
        freerdp_disconnect(instance);
        freerdp_free(instance);
    }
    if(this->thread!=NULL && this->thread->joinable()){
        this->thread->join();
        delete this->thread;
    }
}
BOOL update_begin_paint(rdpContext* context)
{
    // rdpUpdate* update = context->update;

    // if (update->BeginPaint)
    // {
    //     update->BeginPaint(update->context);
    // }
    return TRUE;
}

BOOL update_end_paint(rdpContext* context)
{
    // rdpUpdate* update = context->update;

    // if (update->EndPaint)
    // {
    //     update->EndPaint(update->context);
    // }
    return TRUE;
}
void RDPConnector::connect(){
    instance->PreConnect = freerdp_pre_connect;
    this->thread = new std::thread([&]()->void{
        if(!freerdp_context_new(instance)){
            printf("freerdp_context_new() error.");
            return;
        }
        // rdpUpdate* update;
        // update = (rdpUpdate*) calloc(1, sizeof(rdpUpdate));
        // update->context = instance->context;
        instance->update->BeginPaint = update_begin_paint;
        instance->update->EndPaint = update_end_paint;
        // instance->update = update;

        // this->instance->settings = freerdp_settings_new(0);
        freerdp_settings_set_string(instance->settings, FreeRDP_ServerHostname, this->addr);
        instance->settings->ServerPort=(UINT32) this->port;
        freerdp_settings_set_string(instance->settings, FreeRDP_Username, this->username);
        freerdp_settings_set_string(instance->settings, FreeRDP_Password, this->password);
        // instance->settings->AuthenticationOnly = (this->password==NULL)? FALSE : TRUE;
        instance->settings->IgnoreCertificate = TRUE;
        instance->settings->TcpConnectTimeout = 10000;
        instance->settings->ColorDepth = 32;
        // instance->settings->RemoteFxCodec = TRUE;
        // instance->settings->FastPathOutput = TRUE;
        // instance->settings->FrameAcknowledge = FALSE;
        // instance->settings->PerformanceFlags = 0;
		// instance->settings->LargePointerFlag = 1;
		// instance->settings->GlyphCache = FALSE;
		// instance->settings->BitmapCacheEnabled = 0;
        instance->settings->TlsSecLevel = 0;
        // freerdp_settings_set_string(instance->settings, FreeRDP_Domain, this->addr);
        if(!freerdp_connect(this->instance)){
            printf("freerdp_connect() error.");
            return;
        }
        if(!freerdp_check_fds(instance)){
            printf("freerdp_check_fds() error.");
            return;    
        }
    });
}
BOOL freerdp_onpaint(rdpContext* context, const SURFACE_BITS_COMMAND* surfaceBitsCommand){
    printf("update");
    return TRUE;
}
BOOL freerdp_pre_connect(freerdp* instance){
    rdpContext* context = instance->context;
    rdpGraphics* graphics = context->graphics;
    bitmap_cache_register_callbacks(instance->update);
    // graphics_register_bitmap(graphics, &bitmap);
    return TRUE;
}
int main(){
    printf("Connecting");
    RDPConnector* connector = new RDPConnector("s.mb233.net",3389,"Administrator","Gzy20070115!!!");
    connector->connect();
    printf("Connected");
    system("pause");
}
bool RDPConnector::startedWSA=false;

BOOL freerdp_bitmap_new(rdpContext* context, rdpBitmap* bitmap){
    printf("new()");
    return TRUE;
}
BOOL freerdp_bitmap_paint(rdpContext* context, rdpBitmap* bitmap){
    return TRUE;
}
void freerdp_bitmap_free(rdpContext* context, rdpBitmap* bitmap){
}
BOOL freerdp_bitmap_setsurface(rdpContext* context, rdpBitmap* bitmap, BOOL primary){
    return TRUE;
}