package com.xxsx.earth.xuanxue.client;

import com.xxsx.earth.xuanxue.CultivationFocus;
import com.xxsx.earth.xuanxue.CultivationStatusPayload;
import com.xxsx.earth.xuanxue.XuanhuanJourney;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class XuanhuanHandbookScreen extends Screen {
    private static final int PAPER = 0xFFF0DFBA;
    private static final int EDGE = 0xFFD0AA68;
    private static final int INK = 0xFF2B2117;
    private static final int MUTED = 0xFF6B5639;
    private static final int PURPLE = 0xFF6A3AA5;
    private static final int GREEN = 0xFF2E6840;
    private static final int GOLD = 0xFF956215;
    private static final int BLUE = 0xFF255A78;
    private static final int LINE_HEIGHT = 12;

    private final List<Page> pages = createPages();
    private final List<Button> tabButtons = new ArrayList<>();
    private int page;
    private int scroll;
    private Button prevButton;
    private Button nextButton;

    public XuanhuanHandbookScreen() {
        super(Component.translatable("screen.earth_xuanxue.handbook.title"));
    }

    @Override
    protected void init() {
        tabButtons.clear();
        int left = bookLeft();
        int top = bookTop();
        int tabX = left + 10;
        int tabY = top + 42;
        int tabW = 58;
        for (int i = 0; i < pages.size(); i++) {
            final int index = i;
            Button button = addRenderableWidget(Button.builder(Component.literal(pages.get(i).shortTitle), b -> setPage(index))
                    .bounds(tabX, tabY + i * 18, tabW, 16)
                    .build());
            tabButtons.add(button);
        }
        int bottom = top + bookHeight() - 28;
        prevButton = addRenderableWidget(Button.builder(Component.translatable("screen.earth_xuanxue.handbook.prev"), b -> setPage(page - 1))
                .bounds(left + bookWidth() - 182, bottom, 76, 20).build());
        nextButton = addRenderableWidget(Button.builder(Component.translatable("screen.earth_xuanxue.handbook.next"), b -> setPage(page + 1))
                .bounds(left + bookWidth() - 100, bottom, 76, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.earth_xuanxue.handbook.close"), b -> onClose())
                .bounds(left + 12, bottom, 54, 20).build());
        updateButtonState();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, this.width, this.height, 0xB0000000);
        int left = bookLeft();
        int top = bookTop();
        int bw = bookWidth();
        int bh = bookHeight();
        g.fill(left - 3, top + 5, left + bw + 3, top + bh - 5, 0xA02A1B11);
        g.fill(left, top, left + bw, top + bh, EDGE);
        g.fill(left + 4, top + 3, left + bw - 4, top + bh - 3, PAPER);
        g.outline(left, top, bw, bh, 0xFF50351F);
        drawScrollRails(g, left, top, bw, bh);
        drawBinding(g, left, top, bh);
        String bookTitle = tr("screen.earth_xuanxue.handbook.title");
        g.text(font, bookTitle, left + (bw - font.width(bookTitle)) / 2, top + 8, INK, false);
        g.text(font, tr("screen.earth_xuanxue.handbook.subtitle"), left + 14, top + 24, MUTED, false);
        g.text(font, tr("screen.earth_xuanxue.handbook.page", page + 1, pages.size()), left + bw - 70, top + 24, MUTED, false);
        super.extractRenderState(g, mouseX, mouseY, delta);

        Page current = pages.get(page);
        int contentX = contentLeft();
        int contentY = top + 46;
        int contentW = contentWidth();
        int contentH = Math.max(80, bh - 84);
        g.fill(contentX - 6, contentY - 6, contentX + contentW - 2, contentY + 14, 0x20FFFFFF);
        g.fill(contentX - 6, contentY - 6, contentX - 2, contentY + 14, current.color);
        g.text(font, current.title, contentX, contentY - 3, current.color, false);
        g.fill(contentX, contentY + 12, contentX + Math.min(contentW, 146), contentY + 13, current.color);

        List<Line> wrapped = wrap(current);
        int visible = Math.max(5, (contentH - 24) / LINE_HEIGHT);
        int maxScroll = Math.max(0, wrapped.size() - visible);
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        drawScrollMarker(g, contentX + contentW + 5, contentY + 18, contentH - 26, maxScroll);
        int y = contentY + 18;
        for (int i = scroll; i < Math.min(wrapped.size(), scroll + visible); i++) {
            Line line = wrapped.get(i);
            if (line.blank) {
                y += 4;
                continue;
            }
            g.fill(contentX + line.indent - 5, y + 4, contentX + line.indent - 2, y + 7, current.color);
            g.text(font, line.text, contentX + line.indent, y, line.color, false);
            y += LINE_HEIGHT;
        }
    }

    private void drawScrollRails(GuiGraphicsExtractor g, int left, int top, int width, int height) {
        g.fill(left + 8, top + 2, left + width - 8, top + 5, 0xFF8A5A2D);
        g.fill(left + 8, top + height - 5, left + width - 8, top + height - 2, 0xFF8A5A2D);
        g.fill(left + 14, top + 5, left + 16, top + height - 5, 0x557A4A25);
        int sealX = left + width - 30;
        int sealY = top + 7;
        g.fill(sealX, sealY, sealX + 15, sealY + 15, 0xFF8F2F27);
        g.outline(sealX + 2, sealY + 2, 11, 11, 0xFFF1C58E);
        g.fill(sealX + 5, sealY + 4, sealX + 10, sealY + 11, 0xFFB84538);
    }

    private void drawBinding(GuiGraphicsExtractor g, int left, int top, int height) {
        int x = contentLeft() - 15;
        g.fill(x, top + 31, x + 2, top + height - 34, 0xFF76502D);
        for (int y = top + 48; y < top + height - 40; y += 31) {
            g.fill(x - 3, y, x + 5, y + 2, 0xFF4A321E);
            g.fill(x - 1, y - 3, x + 3, y + 5, 0xFFB58343);
        }
    }

    private void drawScrollMarker(GuiGraphicsExtractor g, int x, int y, int height, int maxScroll) {
        if (maxScroll <= 0 || height < 12) {
            return;
        }
        g.fill(x, y, x + 2, y + height, 0x553B2A1B);
        int markerHeight = Math.max(8, height / (maxScroll + 2));
        int markerY = y + (height - markerHeight) * scroll / maxScroll;
        g.fill(x - 1, markerY, x + 3, markerY + markerHeight, 0xFF956215);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= contentLeft() - 8 && mouseX <= contentLeft() + contentWidth() + 16) {
            scroll = Math.max(0, scroll - (int) Math.signum(scrollY) * 3);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        } else {
            Minecraft.getInstance().setScreen(null);
        }
    }

    private void setPage(int next) {
        if (next < 0 || next >= pages.size()) {
            return;
        }
        page = next;
        scroll = 0;
        updateButtonState();
    }

    private void updateButtonState() {
        for (int i = 0; i < tabButtons.size(); i++) {
            tabButtons.get(i).active = i != page;
        }
        if (prevButton != null) {
            prevButton.active = page > 0;
        }
        if (nextButton != null) {
            nextButton.active = page < pages.size() - 1;
        }
    }

    private List<Line> wrap(Page current) {
        List<Line> result = new ArrayList<>();
        List<Entry> entries = new ArrayList<>();
        if (page == 0) {
            entries.addAll(liveStatusEntries());
        }
        entries.addAll(current.entries);
        for (Entry entry : entries) {
            if (entry.text.isBlank()) {
                result.add(new Line(FormattedCharSequence.EMPTY, 0, INK, true));
                continue;
            }
            for (FormattedCharSequence seq : font.split(FormattedText.of(entry.text), Math.max(30, contentWidth() - entry.indent))) {
                result.add(new Line(seq, entry.indent, entry.color, false));
            }
        }
        return result;
    }

    private List<Entry> liveStatusEntries() {
        CultivationStatusPayload status = XuanhuanEarthClient.cultivationStatus();
        List<Entry> entries = new ArrayList<>();
        int journeyMask = status.journeyMask();
        entries.add(new Entry(Component.translatable(
                "screen.earth_xuanxue.handbook.journey.progress",
                XuanhuanJourney.count(journeyMask), XuanhuanJourney.total()).getString(), 10, GREEN));
        XuanhuanJourney.Milestone nextMilestone = XuanhuanJourney.nextMilestone(journeyMask);
        if (nextMilestone == null) {
            entries.add(new Entry(Component.translatable(
                    "screen.earth_xuanxue.handbook.journey.complete").getString(), 10, GOLD));
        } else {
            entries.add(new Entry(Component.translatable(
                    "screen.earth_xuanxue.handbook.journey.next",
                    Component.translatable(nextMilestone.nextKey())).getString(), 10, GOLD));
        }
        entries.add(new Entry("", 0, INK));
        if (!status.isUnlocked(CultivationFocus.CIRCULATION)) {
            entries.add(new Entry(Component.translatable(
                    "screen.earth_xuanxue.handbook.status.locked").getString(), 10, PURPLE));
            return List.copyOf(entries);
        }

        CultivationFocus focus = CultivationFocus.byId(status.focusId());
        String xp = status.focusXpNeeded() <= 0
                ? "MAX"
                : status.focusXp() + "/" + status.focusXpNeeded();
        int unlocked = Integer.bitCount(status.unlockedMask());
        String nextKey = unlocked < CultivationFocus.values().length
                ? "screen.earth_xuanxue.handbook.status.next.unlock"
                : "screen.earth_xuanxue.handbook.status.next.train";
        entries.add(new Entry(Component.translatable(
                "screen.earth_xuanxue.handbook.status.current",
                Component.translatable(focus.titleKey()), status.focusLevel(), xp, unlocked).getString(),
                10, GREEN));
        entries.add(new Entry(Component.translatable(nextKey).getString(), 10, GOLD));
        entries.add(new Entry("", 0, INK));
        return List.copyOf(entries);
    }

    private int bookWidth() {
        return Math.min(520, Math.max(320, this.width - 18));
    }

    private int bookHeight() {
        return Math.min(300, Math.max(224, this.height - 18));
    }

    private int bookLeft() {
        return (this.width - bookWidth()) / 2;
    }

    private int bookTop() {
        return (this.height - bookHeight()) / 2;
    }

    private int contentLeft() {
        return bookLeft() + 86;
    }

    private int contentWidth() {
        return bookLeft() + bookWidth() - contentLeft() - 20;
    }

    private static List<Page> createPages() {
        boolean earthLoaded = ModList.get().isLoaded("earth_core");
        boolean humanLoaded = ModList.get().isLoaded("earth_human");
        boolean magicLoaded = ModList.get().isLoaded("earth_magic")
                || ModList.get().isLoaded("earth_online_magic");
        if (!Minecraft.getInstance().getLanguageManager().getSelected().toLowerCase(Locale.ROOT).startsWith("zh")) {
            return List.of(
                    page("Start", "1. Cultivation loop", PURPLE,
                            "Craft the Xuanhuan Earth Handbook from dirt, planks, or stone. It only explains the route and is never consumed by technique recipes.",
                            "Craft the separate Qi Guiding Manual from a book, amethyst and redstone. Reading it unlocks circulation; it does not resolve training.",
                            "Start with amethyst and glowstone for spirit crystal shards, then craft a spirit vein node and the Portable Spirit Array.",
                            "Use iron plus a crystal shard in the array to make the first spirit iron blank. That blank unlocks the Alchemy Furnace and Talisman Table."),
                    page("Stations", "2. Cultivation stations", BLUE,
                            "Alchemy Furnace: 2 spirit grass + 1 spring bottle -> 2 pills; 1 crystal shard + 1 spring bottle -> 3 pills.",
                            "Talisman Table: talisman paper + cinnabar spirit ink -> charged basic talisman. Production stores 24 qi; activation only needs 1 mana.",
                            "Portable Spirit Array: iron + crystal -> spirit iron; spirit soil + spring water -> spirit grass; spring water + geological catalyst -> crystal shards.",
                            "Full spirit array: center portable array, spirit soil on four sides, vein or spring foci on four corners. It lowers qi requirement and works faster."),
                    page("Mana", "3. Mana and cooldowns", GREEN,
                            "Qi and magic share one mana pool. Each independently playable mod writes only its own bonus.",
                            "Practice grants experience to the selected route. Levels 1-10 permanently improve mana, absorption or route-specific body adaptation.",
                            "Meditation restores mana but depletes the local chunk field. Pills restore immediately and consume items."),
                    page("Actions", "4. Actions and Earth Human", GOLD,
                            "Press the configurable cultivation-panel key (K by default) to practice anywhere. Free practice runs at 72% efficiency and uses local qi.",
                            "Use the panel button or configurable V key for the selected route's active technique. Skills spend mana and have an independent cooldown.",
                            "The Meditation Cushion is optional: sitting provides full efficiency, extra qi gathering, continuous cycles and stronger Earth Human recovery.",
                            "A basic talisman is a pre-charged single-use carrier. It releases stored qi for short Strength and Resistance and uses only 1 mana as the trigger."),
                    page("Pills", "5. Alchemy ladder", PURPLE,
                            "Pills form a ladder: each tier consumes the previous one, so alchemy is progression rather than six unrelated recipes.",
                            "Foundation Pill (2 qi pills + 2 shards) doubles recovery. Ironbody, Cloudbreath and Grainfast each train one route and grant its effect.",
                            "Breakthrough Pill (2 foundation pills + 4 shards) is the rarest; it is the only way to raise your realm.",
                            "Grainfast and Nightsight can also be made at a crafting table, so the ladder has an entry point before you build a furnace."),
                    page("Implements", "6. Spirit implements", BLUE,
                            "Four implements are crafted from spirit iron blanks: Wind Fan, Stone Seal, Gather Orb and Lantern Rod.",
                            "Sneak-right-click an implement to charge it from the local qi field; right-click to cast. Charge persists on the item.",
                            "Wind Fan pushes and slows enemies. Stone Seal grants resistance while crushing nearby foes. Gather Orb converts charge into mana. Lantern Rod lights the area and marks creatures.",
                            "Beasts: feed a contracted beast spirit crystal shards to raise its rank. Rank drives health, attack and armor, and its name plate shows the rank."),
                    page("Realm", "7. Realm breakthrough", PURPLE,
                            "Route levels cap at 10, so above that the goal is a realm: Mortal, Qi Refining, Foundation, Golden Core, Nascent Soul, Spirit Transformation.",
                            "To break through, every learned route must reach the required level (4, 6, 8, 10) and you must carry a Breakthrough Pill.",
                            "A breakthrough consumes the pill, refills mana and permanently adds 12 to your xuanhuan mana contribution.",
                            "The cultivation panel has a Break Through button that reports exactly what is missing."),
                    page("Links", "5. Optional integrations", GOLD,
                            earthLoaded
                                    ? "Earth on Minecraft connected: geology catalysts, spiritual substrates and mana conductors work directly in cultivation facilities."
                                    : "Earth on Minecraft not installed: vanilla fallback materials keep the cultivation route playable.",
                            humanLoaded
                                    ? "Earth Human connected: meditation, body tempering and breathing practice affect fatigue and body-part recovery."
                                    : "Earth Human not installed: cultivation bonuses still work without realistic-human mechanics.",
                            magicLoaded
                                    ? "Fantasy Earth connected: qi and magic contributions are added into one shared mana pool."
                                    : "Fantasy Earth not installed: this cultivation route remains fully standalone."));
        }
        return List.of(
                page("入门", "1. 修行流程", PURPLE,
                        "用一块泥土、任意木板或任意石头合成《玄幻地球手册》。它只负责指路，后续配方不会消耗手册。",
                        "用书、紫水晶碎片和红石制作独立的《引气诀》。研读只解锁周天路线，不会顺便完成一次修炼。",
                        "先用紫水晶碎片和荧石粉制灵晶碎片，再用深板岩和紫水晶块制灵脉节点，优先做出便携聚灵阵。",
                        "在阵眼放入铁锭和灵晶碎片，制得第一块灵铁胚；灵铁胚会解锁丹炉与符案。寻灵罗盘、灵脉、灵泉和灵土可改善本区块灵气。"),
                page("设施", "2. 三个修行设施怎么用", BLUE,
                        "丹炉：2 灵草 + 1 灵泉瓶 -> 2 回气丹；1 灵晶碎片 + 1 灵泉瓶 -> 3 回气丹。回气丹可立即恢复法力。",
                        "符案：1 符纸 + 1 朱砂灵墨 -> 1 已充能基础符箓。制作时封存 24 灵气，右键激发只需 1 法力。",
                        "便携聚灵阵：铁锭 + 灵晶碎片 -> 灵铁胚；灵土 + 灵泉瓶 -> 2 灵草；灵泉瓶 + 地质催化物 -> 2 灵晶碎片。",
                        "正式聚灵阵：中心便携聚灵阵，四边灵土，四角灵脉节点或灵泉石。成型后外围右键也能打开中心，灵气门槛降低并加速。"),
                page("法力", "3. 法力、灵气和冷却", GREEN,
                        "玄幻灵力和魔幻魔力使用同一条法力值，两边贡献直接相加，但互相只通过共享持久化键沟通。",
                        "每次有效修炼会给当前路线增加经验；路线最高 10 级，升级会永久增加法力、吸收率或对应身体适应。",
                        "冥想恢复不消耗物品，但有冷却并会让本区块暂时枯竭；丹药恢复更直接，但要消耗材料。",
                        "如果修行设施不运行，先看界面状态：可能是没材料、输出满、红石模式不允许，或本地灵气低于配方要求。"),
                page("动作", "4. 修炼、恢复和战斗动作", GOLD,
                        "功法书只负责首次解锁；真正的重复修炼在面板或蒲团结算，并推进当前路线等级。",
                        "按可配置的修炼面板键（默认 K）可在任何位置行一周天；自由修炼效率为 72%，仍会读取并消耗本区块灵气。",
                        "面板按钮或默认 V 键会施展当前路线的主动功法；归元、护体、胎息和养元效果不同，并独立消耗法力与冷却。",
                        "修行蒲团是可选增益：坐下后获得完整效率、额外聚灵、持续周天和更强的地球人恢复；只有坐着时 Shift 才用于离开。",
                        "战斗动作：基础符箓是预先充能的一次性载体，右键只需 1 法力完成激发，释放其中灵力并获得短时间力量与抗性。"),
                page("丹道", "5. 丹药品阶", PURPLE,
                        "丹药是一条阶梯：每一阶都要消耗上一阶的成品，所以炼丹是渐进路线，而不是六张互不相关的配方。",
                        "筑基丹（2 回气丹 + 2 灵晶碎片）回复量翻倍。淬体丹、云息丹、辟谷丹各推进一条路线并附带对应效果。",
                        "破境丹（2 筑基丹 + 4 灵晶碎片）最稀有，是提升境界的唯一途径。",
                        "辟谷丹与夜视丹也可以在普通工作台合成，这样在造出丹炉之前，丹道就已经有入口。"),
                page("法器", "6. 灵铁法器", BLUE,
                        "灵铁胚可打造四件法器：风灵扇、镇岳印、聚灵珠、照幽杖。",
                        "潜行右键为法器充能，充能来自当前区块的灵气场；右键施放。储能保存在物品上，可跨存档保留。",
                        "风灵扇推开并减速敌人；镇岳印提供抗性并震伤近敌；聚灵珠把储能转成法力；照幽杖照亮四周并标记生灵。",
                        "灵兽：给已契约的灵兽喂灵晶碎片可提升品阶。品阶决定生命、攻击与护甲，名牌上会直接显示阶位。"),
                page("境界", "7. 境界突破", PURPLE,
                        "路线等级上限是 10 级，再往上追求的是境界：凡身、炼气、筑基、金丹、元婴、化神。",
                        "破境要求所有已学路线达到对应等级（4、6、8、10），并且身上带有一枚破境丹。",
                        "破境会消耗丹药、回满法力，并永久增加 12 点玄幻法力贡献。",
                        "修炼面板里有“破境”按钮，会直接告诉你还差什么。"),
                page("联动", "5. 与地球 Online 的关系", GOLD,
                        earthLoaded
                                ? "已连接《我的地球》：地质催化物、灵性矿物基底和导能材料可以直接放入修行设施。"
                                : "未安装《我的地球》：当前使用原版材料回退路线，玄幻内容仍可独立游玩。",
                        humanLoaded
                                ? "已连接《地球人》：静修、淬体和胎息会联动疲劳与全身部位恢复。"
                                : "未安装《地球人》：修行属性仍生效，但不会出现真实人体状态联动。",
                        magicLoaded
                                ? "已连接《魔幻地球》：灵力与魔力贡献相加到同一条法力值。"
                                : "未安装《魔幻地球》：玄幻路线保持完整独立。"));
    }

    private static Page page(String shortTitle, String title, int color, String... lines) {
        List<Entry> entries = new ArrayList<>();
        for (String line : lines) {
            entries.add(new Entry(line, line.isBlank() ? 0 : 10, INK));
        }
        return new Page(shortTitle, title, color, List.copyOf(entries));
    }

    private static String tr(String key, Object... args) {
        String raw = Language.getInstance().getOrDefault(key);
        return args.length == 0 ? raw : String.format(Locale.ROOT, raw, args);
    }

    private record Page(String shortTitle, String title, int color, List<Entry> entries) {
    }

    private record Entry(String text, int indent, int color) {
    }

    private record Line(FormattedCharSequence text, int indent, int color, boolean blank) {
    }
}
