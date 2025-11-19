#!/usr/bin/env python3
"""
Analyze Ballerina Lang codebase and generate dependency graph
"""

import os
import re
from collections import defaultdict
from pathlib import Path

# Configuration
ROOT_DIR = Path(__file__).parent
EXCLUDE_PATTERNS = ['test', 'tests', 'build', 'gradle']
MAX_FILES = 3000  # Limit to avoid overwhelming the analysis

def should_exclude(path_str):
    """Check if path should be excluded"""
    for pattern in EXCLUDE_PATTERNS:
        if f'/{pattern}/' in path_str or path_str.endswith(f'/{pattern}'):
            return True
    return False

def find_java_files():
    """Find all Java source files"""
    java_files = []
    for root, dirs, files in os.walk(ROOT_DIR):
        # Filter out excluded directories
        dirs[:] = [d for d in dirs if not should_exclude(os.path.join(root, d))]
        
        for file in files:
            if file.endswith('.java'):
                full_path = os.path.join(root, file)
                if not should_exclude(full_path):
                    java_files.append(full_path)
                    if len(java_files) >= MAX_FILES:
                        return java_files
    return java_files

def extract_package_and_imports(file_path):
    """Extract package name and import statements from a Java file"""
    package = None
    imports = set()
    
    try:
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()
            
            # Extract package
            package_match = re.search(r'^\s*package\s+([\w\.]+)\s*;', content, re.MULTILINE)
            if package_match:
                package = package_match.group(1)
            
            # Extract imports
            import_matches = re.finditer(r'^\s*import\s+([\w\.]+)(?:\.\w+)?\s*;', content, re.MULTILINE)
            for match in import_matches:
                import_pkg = match.group(1)
                # Filter to only include ballerina packages
                if 'ballerina' in import_pkg or 'io.ballerina' in import_pkg:
                    imports.add(import_pkg)
    
    except Exception as e:
        print(f"Error processing {file_path}: {e}")
    
    return package, imports

def get_base_package(pkg):
    """Get base package (first 3 levels)"""
    if not pkg:
        return None
    parts = pkg.split('.')
    if len(parts) >= 3:
        return '.'.join(parts[:3])
    return pkg

def analyze_dependencies():
    """Analyze all dependencies"""
    print("Finding Java files...")
    java_files = find_java_files()
    print(f"Found {len(java_files)} Java files")
    
    # Store package to base package mapping
    package_deps = defaultdict(set)
    base_packages = set()
    
    print("Analyzing dependencies...")
    for i, file_path in enumerate(java_files):
        if i % 500 == 0:
            print(f"Processed {i}/{len(java_files)} files...")
        
        package, imports = extract_package_and_imports(file_path)
        
        if package:
            base_pkg = get_base_package(package)
            if base_pkg:
                base_packages.add(base_pkg)
                
                for imp in imports:
                    imp_base = get_base_package(imp)
                    if imp_base and imp_base != base_pkg:
                        package_deps[base_pkg].add(imp_base)
    
    print(f"Found {len(base_packages)} unique base packages")
    return package_deps, base_packages

def generate_dot_file(package_deps, base_packages):
    """Generate Graphviz DOT file"""
    dot_content = ['digraph ballerina_dependencies {']
    dot_content.append('  rankdir=LR;')
    dot_content.append('  node [shape=box, style=filled, fillcolor=lightblue];')
    dot_content.append('  graph [fontname="Arial", fontsize=12];')
    dot_content.append('  node [fontname="Arial", fontsize=10];')
    dot_content.append('  edge [fontname="Arial", fontsize=8];')
    dot_content.append('')
    
    # Define color schemes for different modules
    colors = {
        'io.ballerina.compiler': 'lightcoral',
        'io.ballerina.shell': 'lightgreen',
        'io.ballerina.runtime': 'lightyellow',
        'io.ballerina.projects': 'lightpink',
        'io.ballerina.cli': 'lightcyan',
        'io.ballerina.language': 'lavender',
        'io.ballerinalang.compiler': 'peachpuff',
        'org.ballerinalang': 'wheat',
    }
    
    # Add nodes with colors
    for pkg in sorted(base_packages):
        color = 'lightblue'
        for prefix, pkg_color in colors.items():
            if pkg.startswith(prefix):
                color = pkg_color
                break
        
        # Simplify package name for display
        label = pkg.replace('io.ballerina.', '').replace('io.ballerinalang.', '').replace('org.ballerinalang.', '')
        dot_content.append(f'  "{pkg}" [label="{label}", fillcolor={color}];')
    
    dot_content.append('')
    
    # Add edges
    edge_count = 0
    for src_pkg in sorted(package_deps.keys()):
        for dst_pkg in sorted(package_deps[src_pkg]):
            if dst_pkg in base_packages:
                dot_content.append(f'  "{src_pkg}" -> "{dst_pkg}";')
                edge_count += 1
    
    dot_content.append('}')
    
    print(f"Generated graph with {len(base_packages)} nodes and {edge_count} edges")
    return '\n'.join(dot_content)

def main():
    print("=== Ballerina Lang Dependency Analysis ===")
    package_deps, base_packages = analyze_dependencies()
    
    print("\nGenerating DOT file...")
    dot_content = generate_dot_file(package_deps, base_packages)
    
    output_file = ROOT_DIR / 'ballerina-dependencies.dot'
    with open(output_file, 'w') as f:
        f.write(dot_content)
    
    print(f"\nDOT file written to: {output_file}")
    print("\nTop 10 packages by dependencies:")
    sorted_pkgs = sorted(package_deps.items(), key=lambda x: len(x[1]), reverse=True)[:10]
    for pkg, deps in sorted_pkgs:
        print(f"  {pkg}: {len(deps)} dependencies")

if __name__ == '__main__':
    main()
