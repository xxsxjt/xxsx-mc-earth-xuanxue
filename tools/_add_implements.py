import io

# 1. Register the component type and the four implement items.
p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\XuanhuanEarth.java')
s = io.open(p, encoding='utf-8').read()

anchor = '''    public static final DeferredItem<GuidedXuanhuanItem> SPIRIT_BEAST_SEAL = material('''
addition = '''    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>,
            net.minecraft.core.component.DataComponentType<ImplementComponent>> IMPLEMENT_CHARGE =
            DATA_COMPONENTS.registerComponentType("implement_charge", builder -> builder
                    .persistent(ImplementComponent.CODEC)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.fromCodec(
                            ImplementComponent.CODEC)));
    public static final DeferredItem<SpiritImplementItem> WIND_FAN =
            implement(SpiritImplementItem.Implement.WIND_FAN);
    public static final DeferredItem<SpiritImplementItem> STONE_SEAL =
            implement(SpiritImplementItem.Implement.STONE_SEAL);
    public static final DeferredItem<SpiritImplementItem> GATHER_ORB =
            implement(SpiritImplementItem.Implement.GATHER_ORB);
    public static final DeferredItem<SpiritImplementItem> LANTERN_ROD =
            implement(SpiritImplementItem.Implement.LANTERN_ROD);
    public static final DeferredItem<GuidedXuanhuanItem> SPIRIT_BEAST_SEAL = material('''

if anchor in s:
    s = s.replace(anchor, addition, 1)
    print('implement items registered')
else:
    print('MISS item anchor')

helper_anchor = '''    private static DeferredItem<XuanhuanPillItem> pill(XuanhuanPillItem.Pill pill) {'''
helper = '''    private static DeferredItem<SpiritImplementItem> implement(SpiritImplementItem.Implement kind) {
        return item(kind.id(), props -> new SpiritImplementItem(props.stacksTo(1), kind.hintKey(), kind));
    }

'''
if helper_anchor in s:
    s = s.replace(helper_anchor, helper + helper_anchor, 1)
    print('implement helper added')
else:
    print('MISS helper anchor')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
