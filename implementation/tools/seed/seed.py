"""Bookmate demo-data seed tool.

Part B: config/plan, 4 demo accounts, 3 description-only books.
Safe to run more than once. Run:  .venv\Scripts\python.exe seed.py [--only plan|accounts|books]
"""
import argparse
import getpass
import re
import sys
from datetime import datetime, timedelta, timezone
from pathlib import Path

import firebase_admin
from firebase_admin import auth, credentials, firestore

import gutenberg

EXPECTED_PROJECT = "bookmate-690a5"
KEY_PATH = Path(__file__).with_name("serviceAccountKey.json")

PLAN = {
    "name": "Bookmate Premium",
    "price": 4.99,
    "priceLabel": "$4.99 / month (sample)",
    "periodDays": 30,
}

DESCRIPTION_ONLY_BOOKS = [
    {
        "id": "atomic-habits",
        "title": "Atomic Habits",
        "author": "James Clear",
        "isbn": "9780735211292",
        "genre": "Self-help",
        "accessType": "free",
        "description": (
            "A practical guide to building good habits and breaking bad ones "
            "through small, steady changes that add up over time."
        ),
    },
    {
        "id": "clean-code",
        "title": "Clean Code",
        "author": "Robert C. Martin",
        "isbn": "9780132350884",
        "genre": "Technology",
        "accessType": "premium",
        "description": (
            "A guide to writing readable, maintainable software, with examples "
            "of how to name, structure and improve code."
        ),
    },
    {
        "id": "a-brief-history-of-time",
        "title": "A Brief History of Time",
        "author": "Stephen Hawking",
        "isbn": "9780553380163",
        "genre": "Science",
        "accessType": "premium",
        "description": (
            "An introduction to the universe for general readers, from the Big "
            "Bang to black holes and the search for a unified theory."
        ),
    },
]

ACCOUNTS = [
    {
        "email": "admin@bookmate.app",
        "name": "Library Admin",
        "role": "admin",
        "genres": [],
        "onboardingDone": True,
        "subscription": None,
    },
    {
        "email": "member.active@bookmate.app",
        "name": "Active Member",
        "role": "member",
        "genres": ["Classic", "Mystery"],
        "onboardingDone": True,
        "subscription": {"startDays": -5, "endDays": 25, "startedBy": "admin"},
    },
    {
        "email": "member.expired@bookmate.app",
        "name": "Expired Member",
        "role": "member",
        "genres": ["Science", "History"],
        "onboardingDone": True,
        "subscription": {"startDays": -40, "endDays": -10, "startedBy": "member"},
    },
    {
        "email": "member.new@bookmate.app",
        "name": "New Member",
        "role": "member",
        "genres": [],
        "onboardingDone": False,
        "subscription": None,
    },
]

_password_cache = None


def init_db():
    if not KEY_PATH.exists():
        sys.exit("serviceAccountKey.json was not found next to seed.py")
    cred = credentials.Certificate(str(KEY_PATH))
    if cred.project_id != EXPECTED_PROJECT:
        sys.exit(f"Wrong project '{cred.project_id}'. Expected '{EXPECTED_PROJECT}'. Stopped.")
    firebase_admin.initialize_app(cred)
    print(f"Project: {cred.project_id}")
    return firestore.client()


def make_keywords(*parts):
    words = set()
    for part in parts:
        for word in re.split(r"[^a-z0-9]+", part.lower()):
            if len(word) >= 2:
                words.add(word)
    return sorted(words)


def upsert(ref, data, created=False, updated=False):
    snap = ref.get()
    payload = dict(data)
    if updated:
        payload["updatedAt"] = firestore.SERVER_TIMESTAMP
    if created and not snap.exists:
        payload["createdAt"] = firestore.SERVER_TIMESTAMP
    ref.set(payload, merge=True)
    return not snap.exists


def get_password():
    global _password_cache
    while _password_cache is None:
        value = getpass.getpass(
            "Choose a demo password for the demo accounts (8+ characters, not saved anywhere): "
        )
        if len(value) >= 8:
            _password_cache = value
        else:
            print("Too short.")
    return _password_cache


def seed_plan(db):
    db.collection("config").document("plan").set(PLAN)
    print("plan: written (config/plan)")


def seed_books(db):
    for book in DESCRIPTION_ONLY_BOOKS:
        data = {
            "title": book["title"],
            "author": book["author"],
            "isbn": book["isbn"],
            "genre": book["genre"],
            "description": book["description"],
            "accessType": book["accessType"],
            "hasFullText": False,
            "chapterCount": 0,
            "searchKeywords": make_keywords(book["title"], book["author"], book["genre"]),
        }
        is_new = upsert(db.collection("books").document(book["id"]), data, created=True, updated=True)
        print(f"book: {book['id']} ({'created' if is_new else 'updated'})")


def seed_fulltext_books(db):
    for book in gutenberg.GUTENBERG_BOOKS:
        chapters, _ = gutenberg.load_book(book)
        if len(chapters) != book["expectedChapters"]:
            sys.exit(
                f"{book['id']}: found {len(chapters)} chapters, "
                f"expected {book['expectedChapters']}. Stopped."
            )
        book_ref = db.collection("books").document(book["id"])
        data = {
            "title": book["title"],
            "author": book["author"],
            "isbn": "",
            "genre": book["genre"],
            "description": book["description"],
            "accessType": book["accessType"],
            "hasFullText": True,
            "chapterCount": len(chapters),
            "searchKeywords": make_keywords(book["title"], book["author"], book["genre"]),
        }
        upsert(book_ref, data, created=True, updated=True)

        chapters_ref = book_ref.collection("chapters")
        keep = {str(chapter["number"]) for chapter in chapters}
        for old_doc in chapters_ref.stream():
            if old_doc.id not in keep:
                old_doc.reference.delete()

        for start in range(0, len(chapters), 20):
            batch = db.batch()
            for chapter in chapters[start:start + 20]:
                batch.set(
                    chapters_ref.document(str(chapter["number"])),
                    {
                        "number": chapter["number"],
                        "title": chapter["title"],
                        "paragraphs": chapter["paragraphs"],
                    },
                    merge=True,
                )
            batch.commit()

        paragraph_count = sum(len(chapter["paragraphs"]) for chapter in chapters)
        print(
            f"fulltext: {book['id']} ({len(chapters)} chapters, "
            f"{paragraph_count} paragraphs, {book['accessType']})"
        )


def get_or_create_user(email, name):
    try:
        return auth.get_user_by_email(email), False
    except auth.UserNotFoundError:
        user = auth.create_user(email=email, password=get_password(), display_name=name)
        return user, True


def seed_accounts(db):
    now = datetime.now(timezone.utc)
    for acc in ACCOUNTS:
        user, created = get_or_create_user(acc["email"], acc["name"])
        users_ref = db.collection("users").document(user.uid)
        upsert(
            users_ref,
            {
                "name": acc["name"],
                "email": acc["email"],
                "role": acc["role"],
                "favoriteGenres": acc["genres"],
                "onboardingDone": acc["onboardingDone"],
            },
            created=True,
        )
        sub = acc["subscription"]
        if sub is not None:
            db.collection("subscriptions").document(user.uid).set(
                {
                    "userId": user.uid,
                    "startDate": now + timedelta(days=sub["startDays"]),
                    "endDate": now + timedelta(days=sub["endDays"]),
                    "status": "active",
                    "startedBy": sub["startedBy"],
                    "updatedAt": firestore.SERVER_TIMESTAMP,
                }
            )
        sub_text = "no subscription" if sub is None else f"subscription ends in {sub['endDays']} days"
        print(f"account: {acc['email']} ({'auth user created' if created else 'auth user existed'}, {sub_text})")


def main():
    parser = argparse.ArgumentParser(description="Seed Bookmate demo data")
    parser.add_argument("--only", choices=["plan", "accounts", "books", "fulltext"], help="run one part only")
    args = parser.parse_args()
    db = init_db()
    if args.only in (None, "plan"):
        seed_plan(db)
    if args.only in (None, "books"):
        seed_books(db)
    if args.only in (None, "fulltext"):
        seed_fulltext_books(db)
    if args.only in (None, "accounts"):
        seed_accounts(db)
    print("Done.")


if __name__ == "__main__":
    main()