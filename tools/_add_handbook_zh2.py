import io

p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\client\XuanhuanHandbookScreen.java')
lines = io.open(p, encoding='utf-8').read().split('\n')

# Insert before the 345th line (1-based) which is the Chinese "Links" page.
target = 345 - 1
assert 'page(' in lines[target], lines[target]

zh_pages = '''                page("丹道", "5. 丹药品阶", PURPLE,
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
                        "修炼面板里有“破境”按钮，会直接告诉你还差什么。"),'''

lines[target:target] = zh_pages.split('\n')
io.open(p, 'w', encoding='utf-8', newline='\n').write('\n'.join(lines))
print('inserted at line', 345)
