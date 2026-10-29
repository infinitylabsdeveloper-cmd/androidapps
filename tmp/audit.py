import os
import re

src_dir = "app/src/main/java"
res_dir = "app/src/main/res"

# Read all files
all_files = {}
for root, _, files in os.walk(src_dir):
    for f in files:
        if f.endswith(".kt") or f.endswith(".java"):
            p = os.path.join(root, f)
            with open(p, "r", encoding="utf-8", errors="ignore") as fl:
                all_files[p] = fl.read()

# Manifest & layouts/assets/resources
manifest_path = "app/src/main/AndroidManifest.xml"
with open(manifest_path, "r", encoding="utf-8", errors="ignore") as fl:
    all_files[manifest_path] = fl.read()

for root, _, files in os.walk(res_dir):
    for f in files:
        if f.endswith(".xml"):
            p = os.path.join(root, f)
            with open(p, "r", encoding="utf-8", errors="ignore") as fl:
                all_files[p] = fl.read()

screen_dir = "app/src/main/java/com/example/ui/screens"
screen_files = sorted([f for f in os.listdir(screen_dir) if f.endswith(".kt")])

print(f"=== AUDITING {len(screen_files)} SCREENS ===")

unreferenced_screens = []
screen_usages = {}

for sf in screen_files:
    spath = os.path.join(screen_dir, sf)
    content = all_files[spath]
    
    # Get top-level declarations
    declarations = set()
    for line in content.splitlines():
        line = line.strip()
        m = re.match(r"^(?:(?:public|internal|sealed|data)\s+)*(?:fun|class|object|interface)\s+([A-Za-z0-9_]+)", line)
        if m:
            declarations.add(m.group(1))
    
    # Also include the filename base
    declarations.add(sf[:-3])
    
    # Check if any declaration appears in other files
    refs = []
    for p, c in all_files.items():
        if p == spath:
            continue
        for d in declarations:
            # Check fast substring
            if d in c:
                refs.append((os.path.basename(p), d))
                
    refs_set = sorted(list(set(refs)))
    screen_usages[sf] = refs_set
    if not refs_set:
        unreferenced_screens.append(sf)
        print(f"[UNREFERENCED] {sf} -> declarations: {list(declarations)[:3]}")
    else:
        # Check if references are only within test or unused files
        ref_files = sorted(list(set([r[0] for r in refs_set])))
        print(f"[USED] {sf} (in {len(ref_files)} files: {ref_files[:4]})")

print("\n=== TOTAL UNREFERENCED SCREENS:", len(unreferenced_screens))
for s in unreferenced_screens:
    print(" -", s)
