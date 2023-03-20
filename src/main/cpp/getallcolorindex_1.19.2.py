import math
def colordistance(rgb_1, rgb_2):
     R_1,G_1,B_1 = rgb_1
     R_2,G_2,B_2 = rgb_2
     rmean = (R_1 +R_2 ) / 2
     R = R_1 - R_2
     G = G_1 -G_2
     B = B_1 - B_2
     return math.sqrt((2+rmean/256)*(R**2)+4*(G**2)+(2+(255-rmean)/256)*(B**2))
class MaterialColor:
    index = 0
    rgb = 0
    def __init__(self, index, rgb):
        self.index = index
        self.rgb = rgb
    def getcolor(self, brightness):
        var1 = brightness
        var2 = (self.rgb >> 16 & 255) * var1 / 255
        var3 = (self.rgb >> 8 & 255) * var1 / 255
        var4 = (self.rgb & 255) * var1 / 255
        return -16777216 | int(var4) << 16 | int(var3) << 8 | int(var2)
colors = {
    "NONE" : MaterialColor(0, 0),
    "GRASS" : MaterialColor(1, 8368696),
    "SAND" : MaterialColor(2, 16247203),
    "WOOL" : MaterialColor(3, 13092807),
    "FIRE" : MaterialColor(4, 16711680),
    "ICE" : MaterialColor(5, 10526975),
    "METAL" : MaterialColor(6, 10987431),
    "PLANT" : MaterialColor(7, 31744),
    "SNOW" : MaterialColor(8, 16777215),
    "CLAY" : MaterialColor(9, 10791096),
    "DIRT" : MaterialColor(10, 9923917),
    "STONE" : MaterialColor(11, 7368816),
    "WATER" : MaterialColor(12, 4210943),
    "WOOD" : MaterialColor(13, 9402184),
    "QUARTZ" : MaterialColor(14, 16776437),
    "COLOR_ORANGE" : MaterialColor(15, 14188339),
    "COLOR_MAGENTA" : MaterialColor(16, 11685080),
    "COLOR_LIGHT_BLUE" : MaterialColor(17, 6724056),
    "COLOR_YELLOW" : MaterialColor(18, 15066419),
    "COLOR_LIGHT_GREEN" : MaterialColor(19, 8375321),
    "COLOR_PINK" : MaterialColor(20, 15892389),
    "COLOR_GRAY" : MaterialColor(21, 5000268),
    "COLOR_LIGHT_GRAY" : MaterialColor(22, 10066329),
    "COLOR_CYAN" : MaterialColor(23, 5013401),
    "COLOR_PURPLE" : MaterialColor(24, 8339378),
    "COLOR_BLUE" : MaterialColor(25, 3361970),
    "COLOR_BROWN" : MaterialColor(26, 6704179),
    "COLOR_GREEN" : MaterialColor(27, 6717235),
    "COLOR_RED" : MaterialColor(28, 10040115),
    "COLOR_BLACK" : MaterialColor(29, 1644825),
    "GOLD" : MaterialColor(30, 16445005),
    "DIAMOND" : MaterialColor(31, 6085589),
    "LAPIS" : MaterialColor(32, 4882687),
    "EMERALD" : MaterialColor(33, 55610),
    "PODZOL" : MaterialColor(34, 8476209),
    "NETHER" : MaterialColor(35, 7340544),
    "TERRACOTTA_WHITE" : MaterialColor(36, 13742497),
    "TERRACOTTA_ORANGE" : MaterialColor(37, 10441252),
    "TERRACOTTA_MAGENTA" : MaterialColor(38, 9787244),
    "TERRACOTTA_LIGHT_BLUE" : MaterialColor(39, 7367818),
    "TERRACOTTA_YELLOW" : MaterialColor(40, 12223780),
    "TERRACOTTA_LIGHT_GREEN" : MaterialColor(41, 6780213),
    "TERRACOTTA_PINK" : MaterialColor(42, 10505550),
    "TERRACOTTA_GRAY" : MaterialColor(43, 3746083),
    "TERRACOTTA_LIGHT_GRAY" : MaterialColor(44, 8874850),
    "TERRACOTTA_CYAN" : MaterialColor(45, 5725276),
    "TERRACOTTA_PURPLE" : MaterialColor(46, 8014168),
    "TERRACOTTA_BLUE" : MaterialColor(47, 4996700),
    "TERRACOTTA_BROWN" : MaterialColor(48, 4993571),
    "TERRACOTTA_GREEN" : MaterialColor(49, 5001770),
    "TERRACOTTA_RED" : MaterialColor(50, 9321518),
    "TERRACOTTA_BLACK" : MaterialColor(51, 2430480),
    "CRIMSON_NYLIUM" : MaterialColor(52, 12398641),
    "CRIMSON_STEM" : MaterialColor(53, 9715553),
    "CRIMSON_HYPHAE" : MaterialColor(54, 6035741),
    "WARPED_NYLIUM" : MaterialColor(55, 1474182),
    "WARPED_STEM" : MaterialColor(56, 3837580),
    "WARPED_HYPHAE" : MaterialColor(57, 5647422),
    "WARPED_WART_BLOCK" : MaterialColor(58, 1356933),
    "DEEPSLATE" : MaterialColor(59, 6579300),
    "RAW_IRON" : MaterialColor(60, 14200723),
    "GLOW_LICHEN" : MaterialColor(61, 8365974),
}
newcolors = []
for i in colors:
    for j in [180,220,255,135]:
        newcolors.append(colors[i].getcolor(j))
ok = []
for r in range(40):
    for g in range(40):
       for b in range(40):
            near = 1000000000
            c = None
            for i in range(len(newcolors)):
                if(i<4):
                    continue
                newnear = colordistance([(newcolors[i] >> 16 & 255),(newcolors[i] >> 8 & 255),(newcolors[i] & 255)],[r/40*256,g/40*256,b/40*256])
                if(newnear<near):
                    near = newnear
                    c=i
            ok.append(c)
print("A:")
print(ok[0:32000])
print("B:")
print(ok[32000::])