import io

p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\XuanhuanEarth.java')
s = io.open(p, encoding='utf-8').read()

old = '''    public static final DeferredItem<QiRecoveryPillItem> QI_RECOVERY_PILL = item("qi_recovery_pill",
            props -> new QiRecoveryPillItem(props, "tooltip.earth_xuanxue.qi_recovery_pill"));'''

new = '''    public static final DeferredItem<QiRecoveryPillItem> QI_RECOVERY_PILL = item("qi_recovery_pill",
            props -> new QiRecoveryPillItem(props, "tooltip.earth_xuanxue.qi_recovery_pill"));
    public static final DeferredItem<XuanhuanPillItem> FOUNDATION_PILL = pill(XuanhuanPills.FOUNDATION);
    public static final DeferredItem<XuanhuanPillItem> IRONBODY_PILL = pill(XuanhuanPills.IRONBODY);
    public static final DeferredItem<XuanhuanPillItem> CLOUDBREATH_PILL = pill(XuanhuanPills.CLOUDBREATH);
    public static final DeferredItem<XuanhuanPillItem> GRAINFAST_PILL = pill(XuanhuanPills.GRAINFAST);
    public static final DeferredItem<XuanhuanPillItem> BREAKTHROUGH_PILL = pill(XuanhuanPills.BREAKTHROUGH);
    public static final DeferredItem<XuanhuanPillItem> NIGHTSIGHT_PILL = pill(XuanhuanPills.NIGHTSIGHT);'''

if old in s:
    s = s.replace(old, new, 1)
    print('items registered')
else:
    print('MISS items')

# Helper next to the other item helpers.
anchor = '''    private static <T extends Item> DeferredItem<T> item(String id, Function<Item.Properties, T> factory) {'''
helper = '''    private static DeferredItem<XuanhuanPillItem> pill(XuanhuanPillItem.Pill pill) {
        return item(pill.id(), props -> new XuanhuanPillItem(props, pill.hintKey(), pill));
    }

'''
if anchor in s:
    s = s.replace(anchor, helper + anchor, 1)
    print('helper added')
else:
    print('MISS helper')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
