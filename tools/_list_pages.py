import io, re

p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\client\XuanhuanHandbookScreen.java')
s = io.open(p, encoding='utf-8').read()
for m in re.finditer(r'page\("([^"]*)"', s):
    line = s[:m.start()].count('\n') + 1
    print(line, m.group(1))
