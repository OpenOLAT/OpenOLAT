#!/usr/bin/env python3
"""
Regenerate glossary HTML files from their markdown sources.

Reads the existing HTML files to extract CSS/header/footer templates,
then parses the markdown and produces updated HTML with the same layout.

Usage:
    python3 generate-glossary-html.py
"""

import re
import html
import os

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

# Section ID mapping (shared between glossary and translations)
SECTION_ID_MAP = {
    "System roles": "system-role",
    "Course/Resource roles": "course-resource-role",
    "Curriculum roles": "curriculum-role",
    "Course Planner roles": "cpl-role",
    "Access roles": "access-role",
    "Modules": "module",
    "Features": "feature",
    "Areas": "area",
    "Concepts": "concept",
    "Standards": "standard",
    "Integrations": "integration",
    "Course Elements": "course-element",
    "Learning Resource Types": "learning-resource-type",
}


def decode_unicode_escapes(text):
    """Convert \\uXXXX sequences to actual Unicode characters."""
    def replace_match(m):
        return chr(int(m.group(1), 16))
    return re.sub(r'\\u([0-9A-Fa-f]{4})', replace_match, text)


def html_escape(text):
    """Escape text for safe HTML insertion."""
    return html.escape(text, quote=True)


def extract_template_parts(html_content):
    """Extract everything before <main> and after </main> from existing HTML."""
    # Find the start of <main class="main-content">
    main_start = html_content.find('<main class="main-content">')
    if main_start == -1:
        raise ValueError("Could not find <main class=\"main-content\"> in HTML")

    # Find the end of </main>
    main_end = html_content.find('</main>', main_start)
    if main_end == -1:
        raise ValueError("Could not find </main> in HTML")

    before = html_content[:main_start + len('<main class="main-content">')]
    after = html_content[main_end:]

    return before, after


# ---------------------------------------------------------------------------
# GLOSSARY GENERATION
# ---------------------------------------------------------------------------

def parse_glossary_md(md_path):
    """Parse the glossary markdown into structured data."""
    with open(md_path, 'r', encoding='utf-8') as f:
        content = f.read()

    content = decode_unicode_escapes(content)

    # Extract total terms count
    total_match = re.search(r'\*\*Total terms:\*\*\s*(\d+)', content)
    total_terms = int(total_match.group(1)) if total_match else 0

    sections = []
    current_section = None
    current_term = None
    current_desc_lines = []

    for line in content.split('\n'):
        line_stripped = line.strip()

        # Section header: ## Section Name
        h2_match = re.match(r'^##\s+(.+)$', line_stripped)
        if h2_match and not line_stripped.startswith('###'):
            # Save previous term if any
            if current_term and current_section is not None:
                _finalize_term(current_section, current_term, current_desc_lines)
                current_term = None
                current_desc_lines = []

            section_name = h2_match.group(1).strip()
            if section_name in SECTION_ID_MAP:
                current_section = {
                    'name': section_name,
                    'id': SECTION_ID_MAP[section_name],
                    'terms': []
                }
                sections.append(current_section)
            continue

        # Term header: ### Term Name
        h3_match = re.match(r'^###\s+(.+)$', line_stripped)
        if h3_match:
            # Save previous term
            if current_term and current_section is not None:
                _finalize_term(current_section, current_term, current_desc_lines)

            current_term = h3_match.group(1).strip()
            current_desc_lines = []
            continue

        # Collect description lines for current term
        if current_term is not None and current_section is not None:
            if line_stripped:
                current_desc_lines.append(line_stripped)

    # Don't forget last term
    if current_term and current_section is not None:
        _finalize_term(current_section, current_term, current_desc_lines)

    return total_terms, sections


def _finalize_term(section, term_name, desc_lines):
    """Process collected description lines into a term entry."""
    desc = ''
    key = None

    for line in desc_lines:
        key_match = re.match(r'^\*Canonical key:\*\s*`(.+)`$', line)
        if key_match:
            key = key_match.group(1)
        else:
            if desc:
                desc += ' '
            desc += line

    section['terms'].append({
        'name': term_name,
        'desc': desc,
        'key': key
    })


def generate_glossary_html(total_terms, sections, template_before, template_after):
    """Generate the full glossary HTML content."""
    # Update term count in header meta
    updated_before = re.sub(
        r'<span>\d+ Terms</span>',
        f'<span>{total_terms} Terms</span>',
        template_before
    )

    num_categories = len(sections)

    # Build sidebar nav
    nav_links = ''
    for section in sections:
        nav_links += f'<a href="#{section["id"]}">{html_escape(section["name"])}</a>'

    # Replace the nav group content
    updated_before = re.sub(
        r'(<div class="nav-group">\n).*?(\n</div>\n\n</nav>)',
        f'\\1{nav_links}\n\\2',
        updated_before,
        flags=re.DOTALL
    )

    # Build main content
    parts = ['\n']

    # Stats box
    parts.append(f'<div class="stats-box">\n')
    parts.append(f'<strong>{total_terms} terms</strong> across {num_categories} categories.\n')
    parts.append('These are terms that have specific meaning within the OpenOlat LMS context\n')
    parts.append('and should be used consistently across translations, documentation, and code.\n')
    parts.append('</div>\n')

    # Sections and terms
    for section in sections:
        parts.append(f'\n<h2 id="{section["id"]}">{html_escape(section["name"])}</h2>')
        for term in section['terms']:
            desc_escaped = html_escape(term['desc'])
            # Convert markdown-style quotes in descriptions
            desc_escaped = desc_escaped.replace('&quot;', '&quot;')
            parts.append(f'<div class="term-card">\n')
            parts.append(f'  <strong>{html_escape(term["name"])}</strong>\n')
            parts.append(f'  <div class="desc">{desc_escaped}</div>\n')
            if term['key']:
                parts.append(f'  <div class="key">Key: <code>{html_escape(term["key"])}</code></div>\n')
            parts.append(f'</div>')

    parts.append('\n\n')

    return updated_before + ''.join(parts) + template_after


# ---------------------------------------------------------------------------
# TRANSLATIONS GENERATION
# ---------------------------------------------------------------------------

def parse_translations_md(md_path):
    """Parse the translations markdown into structured data."""
    with open(md_path, 'r', encoding='utf-8') as f:
        content = f.read()

    content = decode_unicode_escapes(content)

    sections = []
    current_section = None
    in_table = False
    table_rows = []
    total_terms = 0

    for line in content.split('\n'):
        line_stripped = line.strip()

        # Section header
        h2_match = re.match(r'^##\s+(.+)$', line_stripped)
        if h2_match:
            section_name = h2_match.group(1).strip()
            # Skip "How to use this document" etc.
            if section_name not in SECTION_ID_MAP:
                in_table = False
                continue

            # Save previous section's table
            if current_section is not None and table_rows:
                current_section['rows'] = table_rows
                total_terms += len(table_rows)

            current_section = {
                'name': section_name,
                'id': 't-' + SECTION_ID_MAP[section_name],
                'rows': []
            }
            sections.append(current_section)
            in_table = False
            table_rows = []
            continue

        # Table header line
        if current_section is not None and line_stripped.startswith('| English'):
            in_table = True
            table_rows = []
            continue

        # Table separator line
        if current_section is not None and in_table and re.match(r'^\|[-|]+\|$', line_stripped):
            continue

        # Table data row
        if current_section is not None and in_table and line_stripped.startswith('|'):
            cells = [c.strip() for c in line_stripped.split('|')]
            # Remove empty first and last from split
            cells = cells[1:-1] if len(cells) > 2 else cells
            if len(cells) >= 5:
                table_rows.append(cells[:5])
            continue

        # Non-table, non-header line while in a section
        if line_stripped == '' and in_table:
            # End of table block
            pass

    # Save last section
    if current_section is not None and table_rows:
        current_section['rows'] = table_rows
        total_terms += len(table_rows)

    return total_terms, sections


def generate_translations_html(total_terms, sections, template_before, template_after):
    """Generate the full translations HTML content."""
    # Update term count in stats-box reference (we'll rebuild it anyway)
    updated_before = template_before

    # Build sidebar nav
    nav_links = ''
    for section in sections:
        nav_links += f'<a href="#{section["id"]}">{html_escape(section["name"])}</a>'

    # Replace the nav group content
    updated_before = re.sub(
        r'(<div class="nav-group">\n).*?(\n</div>\n\n</nav>)',
        f'\\1{nav_links}\n\\2',
        updated_before,
        flags=re.DOTALL
    )

    # Build main content
    parts = ['\n']

    # Stats box
    parts.append('<div class="stats-box">\n')
    parts.append(f'Translation reference for <strong>{total_terms} product-specific terms</strong> in 5 languages.\n')
    parts.append('Use these exact translations when translating i18n keys to maintain consistency across the application.\n')
    parts.append('</div>\n')

    # Sections and tables
    for section in sections:
        parts.append(f'\n<h2 id="{section["id"]}">{html_escape(section["name"])}</h2>')
        parts.append('<table>')
        parts.append('<tr><th>English</th><th>Deutsch</th><th>Fran\u00e7ais</th><th>Espa\u00f1ol</th><th>Italiano</th></tr>')

        for row in section.get('rows', []):
            parts.append('<tr>')
            for i, cell in enumerate(row):
                cell_content = cell.strip()
                if i == 0:
                    # First column: English term in bold
                    parts.append(f'<td><strong>{html_escape(cell_content)}</strong></td>')
                elif cell_content == '' or cell_content == '-':
                    parts.append('<td><span class="missing">&mdash;</span></td>')
                else:
                    parts.append(f'<td>{html_escape(cell_content)}</td>')
            parts.append('</tr>')

        parts.append('</table>')

    parts.append('\n\n')

    return updated_before + ''.join(parts) + template_after


# ---------------------------------------------------------------------------
# MAIN
# ---------------------------------------------------------------------------

def main():
    glossary_html_path = os.path.join(SCRIPT_DIR, 'openolat-glossary.html')
    glossary_md_path = os.path.join(SCRIPT_DIR, 'openolat-glossary.md')
    translations_html_path = os.path.join(SCRIPT_DIR, 'openolat-glossary-translations.html')
    translations_md_path = os.path.join(SCRIPT_DIR, 'openolat-glossary-translations.md')

    # --- GLOSSARY ---
    print("Reading glossary HTML template...")
    with open(glossary_html_path, 'r', encoding='utf-8') as f:
        glossary_html = f.read()
    g_before, g_after = extract_template_parts(glossary_html)

    print("Parsing glossary markdown...")
    g_total, g_sections = parse_glossary_md(glossary_md_path)
    print(f"  Found {g_total} terms across {len(g_sections)} sections")

    for s in g_sections:
        print(f"    {s['name']}: {len(s['terms'])} terms")

    print("Generating glossary HTML...")
    new_glossary_html = generate_glossary_html(g_total, g_sections, g_before, g_after)

    with open(glossary_html_path, 'w', encoding='utf-8') as f:
        f.write(new_glossary_html)
    print(f"  Written to {glossary_html_path}")

    # --- TRANSLATIONS ---
    print("\nReading translations HTML template...")
    with open(translations_html_path, 'r', encoding='utf-8') as f:
        translations_html = f.read()
    t_before, t_after = extract_template_parts(translations_html)

    print("Parsing translations markdown...")
    t_total, t_sections = parse_translations_md(translations_md_path)
    print(f"  Found {t_total} terms across {len(t_sections)} sections")

    for s in t_sections:
        print(f"    {s['name']}: {len(s.get('rows', []))} terms")

    print("Generating translations HTML...")
    new_translations_html = generate_translations_html(t_total, t_sections, t_before, t_after)

    with open(translations_html_path, 'w', encoding='utf-8') as f:
        f.write(new_translations_html)
    print(f"  Written to {translations_html_path}")

    print("\nDone.")


if __name__ == '__main__':
    main()
