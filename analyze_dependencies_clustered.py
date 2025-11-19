#!/usr/bin/env python3
"""
Enhanced Ballerina Lang dependency analysis with clustering
"""

import os
import re
from collections import defaultdict
from pathlib import Path

# Configuration
ROOT_DIR = Path(__file__).parent
EXCLUDE_PATTERNS = ['test', 'tests', 'build', 'gradle']
MAX_FILES = 3000

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
            
            package_match = re.search(r'^\s*package\s+([\w\.]+)\s*;', content, re.MULTILINE)
            if package_match:
                package = package_match.group(1)
            
            import_matches = re.finditer(r'^\s*import\s+([\w\.]+)(?:\.\w+)?\s*;', content, re.MULTILINE)
            for match in import_matches:
                import_pkg = match.group(1)
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

def get_cluster_name(pkg):
    """Get cluster name for a package"""
    if not pkg:
        return "other"
    
    parts = pkg.split('.')
    if len(parts) >= 2:
        return '.'.join(parts[:2])
    return pkg

def analyze_dependencies():
    """Analyze all dependencies"""
    print("Finding Java files...")
    java_files = find_java_files()
    print(f"Found {len(java_files)} Java files")
    
    package_deps = defaultdict(set)
    base_packages = set()
    package_file_count = defaultdict(int)
    
    print("Analyzing dependencies...")
    for i, file_path in enumerate(java_files):
        if i % 500 == 0:
            print(f"Processed {i}/{len(java_files)} files...")
        
        package, imports = extract_package_and_imports(file_path)
        
        if package:
            base_pkg = get_base_package(package)
            if base_pkg:
                base_packages.add(base_pkg)
                package_file_count[base_pkg] += 1
                
                for imp in imports:
                    imp_base = get_base_package(imp)
                    if imp_base and imp_base != base_pkg:
                        package_deps[base_pkg].add(imp_base)
    
    print(f"Found {len(base_packages)} unique base packages")
    return package_deps, base_packages, package_file_count

def generate_clustered_dot(package_deps, base_packages, package_file_count):
    """Generate clustered Graphviz DOT file"""
    dot_content = ['digraph ballerina_dependencies {']
    dot_content.append('  rankdir=TB;')
    dot_content.append('  compound=true;')
    dot_content.append('  concentrate=true;')
    dot_content.append('  graph [fontname="Arial", fontsize=14, label="Ballerina Language Dependency Graph", labelloc=t];')
    dot_content.append('  node [fontname="Arial", fontsize=10, shape=box];')
    dot_content.append('  edge [fontname="Arial", fontsize=8, color=gray50];')
    dot_content.append('')
    
    # Group packages by cluster
    clusters = defaultdict(set)
    for pkg in base_packages:
        cluster_name = get_cluster_name(pkg)
        clusters[cluster_name].add(pkg)
    
    # Define cluster colors
    cluster_colors = {
        'io.ballerina': 'aliceblue',
        'org.ballerinalang': 'mistyrose',
        'io.ballerinalang': 'honeydew',
    }
    
    node_colors = {
        'io.ballerina.compiler': 'lightcoral',
        'io.ballerina.shell': 'lightgreen',
        'io.ballerina.runtime': 'lightyellow',
        'io.ballerina.projects': 'lightpink',
        'io.ballerina.cli': 'lightcyan',
        'org.ballerinalang.compiler': 'peachpuff',
        'org.ballerinalang.langserver': 'lavender',
    }
    
    # Create clusters
    for cluster_idx, (cluster_name, pkgs) in enumerate(sorted(clusters.items())):
        if len(pkgs) > 1:  # Only create cluster if more than 1 package
            dot_content.append(f'  subgraph cluster_{cluster_idx} {{')
            dot_content.append(f'    label="{cluster_name}";')
            bg_color = cluster_colors.get(cluster_name, 'white')
            dot_content.append(f'    style=filled;')
            dot_content.append(f'    fillcolor="{bg_color}";')
            dot_content.append(f'    color=gray60;')
            dot_content.append('')
            
            for pkg in sorted(pkgs):
                color = node_colors.get(pkg, 'lightblue')
                label = pkg.replace(cluster_name + '.', '')
                file_count = package_file_count.get(pkg, 0)
                dot_content.append(f'    "{pkg}" [label="{label}\\n({file_count} files)", fillcolor={color}, style=filled];')
            
            dot_content.append('  }')
            dot_content.append('')
        else:
            # Standalone node
            for pkg in pkgs:
                color = node_colors.get(pkg, 'lightblue')
                file_count = package_file_count.get(pkg, 0)
                dot_content.append(f'  "{pkg}" [label="{pkg}\\n({file_count} files)", fillcolor={color}, style=filled];')
    
    dot_content.append('')
    
    # Add edges
    edge_count = 0
    for src_pkg in sorted(package_deps.keys()):
        for dst_pkg in sorted(package_deps[src_pkg]):
            if dst_pkg in base_packages:
                dot_content.append(f'  "{src_pkg}" -> "{dst_pkg}";')
                edge_count += 1
    
    dot_content.append('}')
    
    print(f"Generated clustered graph with {len(base_packages)} nodes and {edge_count} edges in {len(clusters)} clusters")
    return '\n'.join(dot_content)

def main():
    print("=== Enhanced Ballerina Lang Dependency Analysis ===")
    package_deps, base_packages, package_file_count = analyze_dependencies()
    
    print("\nGenerating clustered DOT file...")
    dot_content = generate_clustered_dot(package_deps, base_packages, package_file_count)
    
    output_file = ROOT_DIR / 'ballerina-dependencies-clustered.dot'
    with open(output_file, 'w') as f:
        f.write(dot_content)
    
    print(f"\nClustered DOT file written to: {output_file}")
    
    print("\nTop 15 packages by dependencies:")
    sorted_pkgs = sorted(package_deps.items(), key=lambda x: len(x[1]), reverse=True)[:15]
    for pkg, deps in sorted_pkgs:
        file_count = package_file_count.get(pkg, 0)
        print(f"  {pkg} ({file_count} files): {len(deps)} dependencies")
    
    print("\nTop 15 packages by file count:")
    sorted_by_files = sorted(package_file_count.items(), key=lambda x: x[1], reverse=True)[:15]
    for pkg, count in sorted_by_files:
        dep_count = len(package_deps.get(pkg, []))
        print(f"  {pkg}: {count} files, {dep_count} deps")

if __name__ == '__main__':
    main()
