import sys

classes = set()
methods = set()

t = None
q = None

args = sys.argv[1:]

if len(args) > 1:
    t = args[0]
    q = args[1]
elif len(args) == 1:
    t = args[0]

with open("teavm.txt", "r") as f:
    for line in f:
        if line.strip().startswith("at"):
            continue
        line = line.strip()

        if line.startswith("Class"):
            classes.add(line.split(" ")[1])
        elif line.startswith("Method"):
            methods.add(line.split(" ")[1])

if t == "classes":
    for c in classes:
        if q is None or q in c:
            print(c)
elif t == "methods":
    for m in methods:
        if q is None or q in m:
            print(m)
