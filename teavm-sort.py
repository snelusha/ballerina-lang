def get_method_symbol(line):
    if not line.startswith("Method"):
        return None

    return line.split(" ")[1]


def get_class(line):
    if not line.startswith("Class"):
        return None

    return line.split(" ")[1]


def search(entries, term):
    results = []
    for entry in entries:
        if term in entry[0]:
            results.append(entry)
    return results


classes = []
methods = []

with open("t-latest.txt", "r") as f:
    lines = f.readlines()
    for i in range(len(lines)):
        line = lines[i].strip()

        stacktrace = []

        for j in range(i + 1, len(lines)):
            next_line = lines[j].strip()
            if not next_line.startswith("at"):
                break
            stacktrace.append(next_line)

        if line.startswith("Class"):
            class_name = get_class(line)
            if class_name:
                classes.append((class_name, stacktrace))

        elif line.startswith("Method"):
            method_symbol = get_method_symbol(line)
            if method_symbol:
                methods.append((method_symbol, stacktrace))

results = search(classes, "java.nio.file.Path")
results.sort(key=lambda x: len(x[1]), reverse=True)

for result in results:
    print(result[0])
    for entry in result[1]:
        print(f"  {entry}")
    print()
