"""Download Project Gutenberg texts and split them into chapters and paragraphs.

Run this file directly to print a check report (nothing is written to Firebase):
    .venv\\Scripts\\python.exe gutenberg.py
"""
import json
import re
import sys
from pathlib import Path

import requests

CACHE_DIR = Path(__file__).with_name("cache")
USER_AGENT = "Mozilla/5.0 (compatible; BookmateSeed/1.0; student project)"
MIN_CHAPTER_CHARS = 300
MAX_CHAPTER_BYTES = 900_000

GUTENBERG_BOOKS = [
    {
        "id": "pride-and-prejudice",
        "gutenbergId": 1342,
        "title": "Pride and Prejudice",
        "author": "Jane Austen",
        "genre": "Romance",
        "accessType": "premium",
        "description": (
            "Elizabeth Bennet and the proud Mr. Darcy must overcome first "
            "impressions and family pressures in Regency England."
        ),
        "headingWords": "chapter",
        "expectedChapters": 61,
    },
    {
        "id": "sense-and-sensibility",
        "gutenbergId": 161,
        "title": "Sense and Sensibility",
        "author": "Jane Austen",
        "genre": "Classic",
        "accessType": "premium",
        "description": (
            "Two sisters, one guided by reason and one by feeling, face love "
            "and money troubles after their family loses its home."
        ),
        "headingWords": "chapter",
        "expectedChapters": 50,
    },
    {
        "id": "emma",
        "gutenbergId": 158,
        "title": "Emma",
        "author": "Jane Austen",
        "genre": "Classic",
        "accessType": "premium",
        "description": (
            "A confident young woman enjoys matchmaking for her friends and "
            "slowly learns how little she understands her own heart."
        ),
        "headingWords": "chapter",
        "expectedChapters": 55,
    },
    {
        "id": "northanger-abbey",
        "gutenbergId": 121,
        "title": "Northanger Abbey",
        "author": "Jane Austen",
        "genre": "Classic",
        "accessType": "free",
        "description": (
            "A young woman who loves Gothic novels visits a grand abbey and "
            "lets her imagination run ahead of reality."
        ),
        "headingWords": "chapter",
        "expectedChapters": 31,
        "endCutPattern": r"^A NOTE ON THE TEXT$",
    },
    {
        "id": "frankenstein",
        "gutenbergId": 84,
        "title": "Frankenstein",
        "author": "Mary Shelley",
        "genre": "Horror",
        "accessType": "free",
        "description": (
            "A young scientist creates a living being and then faces the "
            "terrible consequences of abandoning his creation."
        ),
        "headingWords": "chapter|letter",
        "expectedChapters": 28,
    },
    {
        "id": "dracula",
        "gutenbergId": 345,
        "title": "Dracula",
        "author": "Bram Stoker",
        "genre": "Horror",
        "accessType": "free",
        "description": (
            "A solicitor's journey to Transylvania unleashes an ancient "
            "vampire, and a group of friends races to stop him."
        ),
        "headingWords": "chapter",
        "expectedChapters": 27,
    },
]

START_RE = re.compile(r"\*\*\*\s*START OF (?:THE|THIS) PROJECT GUTENBERG EBOOK.*?\*\*\*", re.I | re.S)
END_RE = re.compile(r"\*\*\*\s*END OF (?:THE|THIS) PROJECT GUTENBERG EBOOK", re.I)
VOLUME_RE = re.compile(r"(?mi)^[ \t]*VOLUME[ \t]+[IVXLC]+\.?[ \t]*$")


def fetch_text(gutenberg_id):
    CACHE_DIR.mkdir(exist_ok=True)
    cached = CACHE_DIR / f"pg{gutenberg_id}.txt"
    if cached.exists():
        return cached.read_bytes().decode("utf-8-sig")
    mirror_dir = "/".join(str(gutenberg_id)[:-1]) or "0"
    urls = [
        f"https://gutenberg.pglaf.org/{mirror_dir}/{gutenberg_id}/{gutenberg_id}-0.txt",
        f"https://gutenberg.pglaf.org/{mirror_dir}/{gutenberg_id}/{gutenberg_id}.txt",
        f"https://www.gutenberg.org/cache/epub/{gutenberg_id}/pg{gutenberg_id}.txt",
    ]
    last_error = None
    for url in urls:
        try:
            response = requests.get(url, headers={"User-Agent": USER_AGENT}, timeout=60)
            response.raise_for_status()
            text = response.content.decode("utf-8-sig")
            cached.write_bytes(response.content)
            return text
        except Exception as error:  # try the next address
            last_error = error
    raise RuntimeError(f"Could not download Gutenberg book {gutenberg_id}: {last_error}")


def strip_boilerplate(text):
    text = text.replace("\r\n", "\n").replace("\r", "\n")
    start = START_RE.search(text)
    if not start:
        raise ValueError("Gutenberg START marker not found")
    end = END_RE.search(text, start.end())
    if not end:
        raise ValueError("Gutenberg END marker not found")
    return text[start.end():end.start()]


def to_paragraphs(chunk):
    paragraphs = []
    for block in re.split(r"\n[ \t]*\n", chunk):
        text = " ".join(line.strip() for line in block.split("\n"))
        text = re.sub(r"\s+", " ", text.replace("_", "")).strip()
        if not text:
            continue
        if re.fullmatch(r"[\*\s]+", text):
            continue
        if text.startswith("[Illustration") or text == "]":
            continue
        paragraphs.append(text)
    return paragraphs


END_MARK_RE = re.compile(r"(?i)^(THE END|FINIS)[\s\.\]]*$")


def roman_to_int(text):
    values = {"I": 1, "V": 5, "X": 10, "L": 50, "C": 100, "D": 500, "M": 1000}
    total = 0
    previous = 0
    for char in reversed(text.upper()):
        value = values[char]
        if value < previous:
            total -= value
        else:
            total += value
            previous = value
    return total


def heading_value(text):
    return int(text) if text.isdigit() else roman_to_int(text)


def split_chapters(raw_text, heading_words, end_cut_pattern=None):
    """Return (chapters, dropped). Each chapter: {number, title, paragraphs}."""
    body = VOLUME_RE.sub("", strip_boilerplate(raw_text))
    pattern = re.compile(rf"(?mi)^[ \t]*({heading_words})[ \t]+([IVXLCDM]+|\d+)\.?\]?[ \t]*$")
    matches = list(pattern.finditer(body))
    chapters = []
    counters = {}
    dropped = 0
    for index, match in enumerate(matches):
        end = matches[index + 1].start() if index + 1 < len(matches) else len(body)
        paragraphs = to_paragraphs(body[match.end():end])
        if sum(len(p) for p in paragraphs) < MIN_CHAPTER_CHARS:
            dropped += 1
            continue
        if not chapters and index + 1 < len(matches):
            # Front matter after a table of contents: the next heading restarts the numbering.
            if heading_value(match.group(2)) >= heading_value(matches[index + 1].group(2)):
                dropped += 1
                continue
        word = match.group(1).capitalize()
        counters[word] = counters.get(word, 0) + 1
        chapters.append(
            {
                "number": len(chapters) + 1,
                "title": f"{word} {counters[word]}",
                "paragraphs": paragraphs,
            }
        )
    if chapters:
        cut = re.compile(end_cut_pattern, re.I) if end_cut_pattern else END_MARK_RE
        last = chapters[-1]["paragraphs"]
        for position, text in enumerate(last):
            if position > 0 and cut.match(text):
                del last[position:]
                break
    return chapters, dropped


def load_book(book):
    """Download (or read from cache) and split one book from GUTENBERG_BOOKS."""
    return split_chapters(fetch_text(book["gutenbergId"]), book["headingWords"], book.get("endCutPattern"))


def chapter_bytes(chapter):
    return len(json.dumps(chapter["paragraphs"], ensure_ascii=False).encode("utf-8"))


def report():
    problems = 0
    for book in GUTENBERG_BOOKS:
        try:
            chapters, dropped = load_book(book)
        except Exception as error:
            print(f"{book['id']}: ERROR {error}")
            problems += 1
            continue
        count = len(chapters)
        status = "OK" if count == book["expectedChapters"] else "CHECK"
        if status != "OK":
            problems += 1
        sizes = [sum(len(p) for p in c["paragraphs"]) for c in chapters] or [0]
        biggest_kb = max((chapter_bytes(c) for c in chapters), default=0) // 1024
        total_paragraphs = sum(len(c["paragraphs"]) for c in chapters)
        print(f"{book['id']}: chapters={count} (expected {book['expectedChapters']}) {status}")
        print(
            f"  paragraphs={total_paragraphs}  chars min/max={min(sizes)}/{max(sizes)}"
            f"  biggest chapter={biggest_kb} KB  dropped short sections={dropped}"
        )
        if chapters:
            first, last = chapters[0], chapters[-1]
            print(f"  first: {first['title']} | {first['paragraphs'][0][:70]}")
            print(f"  last : {last['title']} | ...{last['paragraphs'][-1][-60:]}")
        if chapters and max(chapter_bytes(c) for c in chapters) > MAX_CHAPTER_BYTES:
            print("  WARNING: a chapter is too large for one Firestore document")
            problems += 1
    print("ALL OK" if problems == 0 else f"{problems} thing(s) to check")


if __name__ == "__main__":
    report()
    sys.exit(0)