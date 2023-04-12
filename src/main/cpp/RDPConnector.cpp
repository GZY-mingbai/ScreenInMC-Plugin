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
	int i;
	HGDI_WND hwnd;
	int ninvalid;
	rdpGdi* gdi;
	HGDI_RGN cinvalid;
	int x1, y1, x2, y2;
	rdpSettings* settings;

	if (!context || !context->instance){
        cout<<"???1";
    	return FALSE;
    }

	settings = context->settings;

	if (!settings){
        cout<<"???2";
    	return FALSE;
    }

	gdi = context->gdi;

	if (!gdi || !gdi->primary || !gdi->primary->hdc)
    {
        cout<<"???3";
    	return FALSE;
    }

	hwnd = gdi->primary->hdc->hwnd;

	if (!hwnd){
        cout<<"???4";
    	return FALSE;
    }
	ninvalid = hwnd->ninvalid;

	if (ninvalid < 1)
		{
        cout<<"???5";
    	return TRUE;
    }

	cinvalid = hwnd->cinvalid;

	if (!cinvalid)
		{
        cout<<"???6";
    	return FALSE;
    }

	x1 = cinvalid[0].x;
	y1 = cinvalid[0].y;
	x2 = cinvalid[0].x + cinvalid[0].w;
	y2 = cinvalid[0].y + cinvalid[0].h;

	for (i = 0; i < ninvalid; i++)
	{
		x1 = MIN(x1, cinvalid[i].x);
		y1 = MIN(y1, cinvalid[i].y);
		x2 = MAX(x2, cinvalid[i].x + cinvalid[i].w);
		y2 = MAX(y2, cinvalid[i].y + cinvalid[i].h);
    }
    int x = x1;
    int y = y1;
    int w = x2-x1;
    int h = y2-y1;
    printf("%d,%d,%d,%d\n",x,y,w,h);
    return TRUE;
}
void RDPConnector::connect(){
    instance->PostConnect = freerdp_post_connect;
    this->thread = new std::thread([&]()->void{
        if(!freerdp_context_new(instance)){
            printf("freerdp_context_new() error.");
            return;
        }
        // rdpUpdate* update;
        // update = (rdpUpdate*) calloc(1, sizeof(rdpUpdate));
        // update->context = instance->context;
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
BOOL freerdp_post_connect(freerdp* instance){
    if (!gdi_init(instance, PIXEL_FORMAT_RGB24))
		return FALSE;
	rdpPointer pointer = { 0 };
    rdpGraphics* graphics = instance->context->graphics;
	if (!graphics)
		return FALSE;

	pointer.size = sizeof(pointer);
	pointer.New = bridge_Pointer_New;
	pointer.Free = bridge_Pointer_Free;
	pointer.Set = bridge_Pointer_Set;
	pointer.SetNull = bridge_Pointer_SetNull;
	pointer.SetDefault = bridge_Pointer_SetDefault;
	pointer.SetPosition = bridge_Pointer_SetPosition;
	graphics_register_pointer(graphics, &pointer);
    instance->update->BeginPaint = update_begin_paint;
    instance->update->EndPaint = update_end_paint;
    instance->update->DesktopResize = update_desktop_resize;
    
    return TRUE;
}
int main(){
    printf("Connecting");
    RDPConnector* connector = new RDPConnector("192.168.0.202",3389,"root","Gzy20070115!!!");
    connector->connect();
    printf("Connected");
    system("pause");
}
bool RDPConnector::startedWSA=false;
static BOOL bridge_Pointer_New(rdpContext* context, rdpPointer* pointer)
{
if (!context || !pointer || !context->gdi) {
return FALSE;
}
return TRUE;
}

static void bridge_Pointer_Free(rdpContext* context, rdpPointer* pointer)
{
}

static BOOL bridge_Pointer_Set(rdpContext* context, const rdpPointer* pointer)
{
if (!context || !pointer) {
return FALSE;
}
return TRUE;
}

static BOOL bridge_Pointer_SetPosition(rdpContext* context, UINT32 x, UINT32 y)
{
if (!context) {
return FALSE;
}
return TRUE;
}

static BOOL bridge_Pointer_SetNull(rdpContext* context)
{
if (!context) {
return FALSE;
}
return TRUE;
}

static BOOL bridge_Pointer_SetDefault(rdpContext* context)
{
if (!context) {
return FALSE;
}
return TRUE;
}
static BOOL update_desktop_resize(rdpContext* context){
    return TRUE;
}