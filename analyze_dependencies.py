#!/usr/bin/env python3
"""
Analyze Ballerina codebase and generate a comprehensive Graphviz diagram
showing all packages and classes with their dependencies.
"""

import os
import re
from collections import defaultdict
from pathlib import Path

class CodebaseAnalyzer:
    def __init__(self, root_path):
        self.root_path = Path(root_path)
        self.packages = defaultdict(set)  # package_name -> set of classes
        self.class_dependencies = defaultdict(set)  # class_full_name -> set of dependencies
        self.package_dependencies = defaultdict(set)  # package -> set of dependent packages
        self.class_to_package = {}  # class_name -> package
        
    def is_test_file(self, file_path):
        """Check if file is a test file"""
        path_str = str(file_path).lower()
        return ('test' in path_str or 
                '/tests/' in path_str or 
                '/build/' in path_str or
                'test.java' in path_str.lower())
    
    def extract_package_name(self, content):
        """Extract package name from Java file content"""
        match = re.search(r'^\s*package\s+([\w.]+)\s*;', content, re.MULTILINE)
        return match.group(1) if match else None
    
    def extract_class_names(self, content):
        """Extract all class, interface, enum names from Java file"""
        classes = []
        # Match class, interface, enum, record declarations
        patterns = [
            r'(?:public|private|protected)?\s*(?:static)?\s*(?:abstract)?\s*class\s+(\w+)',
            r'(?:public|private|protected)?\s*interface\s+(\w+)',
            r'(?:public|private|protected)?\s*enum\s+(\w+)',
            r'(?:public|private|protected)?\s*record\s+(\w+)',
            r'(?:public|private|protected)?\s*@interface\s+(\w+)',
        ]
        for pattern in patterns:
            matches = re.finditer(pattern, content)
            classes.extend([m.group(1) for m in matches])
        return classes
    
    def extract_imports(self, content):
        """Extract import statements from Java file"""
        imports = []
        matches = re.finditer(r'^\s*import\s+(?:static\s+)?([\w.]+)(?:\.\*)?;', content, re.MULTILINE)
        for match in matches:
            imports.append(match.group(1))
        return imports
    
    def extract_dependencies_from_content(self, content):
        """Extract class usage from content (extends, implements, field types, etc.)"""
        dependencies = set()
        
        # Extract extends/implements
        extends_matches = re.finditer(r'\s+extends\s+([\w.]+)', content)
        implements_matches = re.finditer(r'\s+implements\s+([\w.,\s]+)', content)
        
        for match in extends_matches:
            dependencies.add(match.group(1))
        
        for match in implements_matches:
            # Handle multiple interfaces
            interfaces = match.group(1).split(',')
            for intf in interfaces:
                dependencies.add(intf.strip())
        
        return dependencies
    
    def analyze_file(self, file_path):
        """Analyze a single Java file"""
        try:
            with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                content = f.read()
            
            package = self.extract_package_name(content)
            if not package:
                return
            
            class_names = self.extract_class_names(content)
            imports = self.extract_imports(content)
            local_deps = self.extract_dependencies_from_content(content)
            
            # Register classes in this package
            for class_name in class_names:
                full_class_name = f"{package}.{class_name}"
                self.packages[package].add(class_name)
                self.class_to_package[full_class_name] = package
                
                # Process imports for dependencies
                for imp in imports:
                    # Skip java.*, javax.*, org.eclipse.*, etc.
                    if not (imp.startswith('java.') or 
                           imp.startswith('javax.') or 
                           imp.startswith('org.eclipse.') or
                           imp.startswith('org.junit.') or
                           imp.startswith('org.testng.') or
                           imp.startswith('org.mockito.')):
                        self.class_dependencies[full_class_name].add(imp)
                        
                        # Extract package from import
                        if '.' in imp:
                            imp_package = '.'.join(imp.split('.')[:-1])
                            if imp_package != package and imp_package:
                                self.package_dependencies[package].add(imp_package)
                
                # Process local dependencies (extends/implements)
                for dep in local_deps:
                    if '.' not in dep:  # Simple class name, might be from imports
                        for imp in imports:
                            if imp.endswith('.' + dep):
                                self.class_dependencies[full_class_name].add(imp)
                                break
                    else:
                        self.class_dependencies[full_class_name].add(dep)
                        
        except Exception as e:
            print(f"Error analyzing {file_path}: {e}")
    
    def analyze_codebase(self):
        """Analyze all Java files in the codebase"""
        java_files = list(self.root_path.rglob('*.java'))
        total = len(java_files)
        processed = 0
        
        print(f"Found {total} Java files")
        
        for java_file in java_files:
            if not self.is_test_file(java_file):
                self.analyze_file(java_file)
                processed += 1
                if processed % 100 == 0:
                    print(f"Processed {processed} files...")
        
        print(f"Analysis complete: {processed} files analyzed")
        print(f"Packages found: {len(self.packages)}")
        print(f"Total classes: {sum(len(classes) for classes in self.packages.values())}")
    
    def generate_dot_file(self, output_path):
        """Generate Graphviz DOT file"""
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write('digraph BallerinaDependencies {\n')
            f.write('    rankdir=LR;\n')
            f.write('    node [shape=box, style=filled];\n')
            f.write('    concentrate=true;\n')
            f.write('    compound=true;\n\n')
            
            # Group by top-level package (e.g., io.ballerina, org.ballerinalang, etc.)
            top_packages = defaultdict(lambda: defaultdict(set))
            for package, classes in self.packages.items():
                if '.' in package:
                    top = package.split('.')[0] + '.' + package.split('.')[1]
                else:
                    top = package
                top_packages[top][package] = classes
            
            # Generate subgraphs for top-level packages
            cluster_id = 0
            package_colors = ['lightblue', 'lightgreen', 'lightyellow', 'lightpink', 
                            'lavender', 'lightcyan', 'wheat', 'lightgray']
            
            for top_pkg, sub_packages in sorted(top_packages.items()):
                color = package_colors[cluster_id % len(package_colors)]
                cluster_id += 1
                
                f.write(f'    subgraph cluster_{self._sanitize(top_pkg)} {{\n')
                f.write(f'        label="{top_pkg}";\n')
                f.write(f'        style=filled;\n')
                f.write(f'        fillcolor={color};\n')
                f.write(f'        color=black;\n\n')
                
                # Create sub-clusters for each package
                for package, classes in sorted(sub_packages.items()):
                    if len(classes) > 0:
                        f.write(f'        subgraph cluster_{self._sanitize(package)} {{\n')
                        f.write(f'            label="{package}";\n')
                        f.write(f'            style=filled;\n')
                        f.write(f'            fillcolor=white;\n\n')
                        
                        # Add classes
                        for class_name in sorted(classes):
                            node_id = self._sanitize(f"{package}.{class_name}")
                            f.write(f'            "{node_id}" [label="{class_name}"];\n')
                        
                        f.write('        }\n\n')
                
                f.write('    }\n\n')
            
            # Add dependencies between classes
            f.write('    // Class dependencies\n')
            for class_full, deps in self.class_dependencies.items():
                if class_full in self.class_to_package:
                    src_node = self._sanitize(class_full)
                    for dep in deps:
                        # Only add edge if dependency exists in our codebase
                        if dep in self.class_to_package:
                            dst_node = self._sanitize(dep)
                            f.write(f'    "{src_node}" -> "{dst_node}";\n')
            
            f.write('}\n')
        
        print(f"DOT file generated: {output_path}")
    
    def _sanitize(self, name):
        """Sanitize name for DOT format"""
        return name.replace('.', '_').replace('-', '_').replace(' ', '_')
    
    def print_statistics(self):
        """Print analysis statistics"""
        print("\n=== Analysis Statistics ===")
        print(f"Total packages: {len(self.packages)}")
        print(f"Total classes: {sum(len(classes) for classes in self.packages.values())}")
        print(f"Package dependencies: {sum(len(deps) for deps in self.package_dependencies.values())}")
        print(f"Class dependencies: {sum(len(deps) for deps in self.class_dependencies.values())}")
        
        # Top 10 packages by class count
        print("\nTop 10 packages by class count:")
        sorted_packages = sorted(self.packages.items(), key=lambda x: len(x[1]), reverse=True)[:10]
        for pkg, classes in sorted_packages:
            print(f"  {pkg}: {len(classes)} classes")
        
        # Top 10 most connected packages
        print("\nTop 10 most connected packages:")
        sorted_deps = sorted(self.package_dependencies.items(), key=lambda x: len(x[1]), reverse=True)[:10]
        for pkg, deps in sorted_deps:
            print(f"  {pkg}: {len(deps)} dependencies")


if __name__ == '__main__':
    root_path = '/Users/sithi/sandbox/forks/ballerina-lang'
    output_dot = '/Users/sithi/sandbox/forks/ballerina-lang/ballerina_complete_dependencies.dot'
    
    print("Starting Ballerina codebase analysis...")
    analyzer = CodebaseAnalyzer(root_path)
    analyzer.analyze_codebase()
    analyzer.print_statistics()
    analyzer.generate_dot_file(output_dot)
    print("\nAnalysis complete!")
